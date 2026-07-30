package com.evs.electricvehiclestore.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.evs.electricvehiclestore.dto.CheckoutRequest;
import com.evs.electricvehiclestore.dto.AccessoryDTO;
import com.evs.electricvehiclestore.dto.CreditCardDTO;
import com.evs.electricvehiclestore.dto.OrderItemDTO;
import com.evs.electricvehiclestore.dto.OrderSummaryDTO;
import com.evs.electricvehiclestore.dto.PaymentResultDTO;
import com.evs.electricvehiclestore.dto.ShippingInfoDTO;
import com.evs.electricvehiclestore.entity.Cart;
import com.evs.electricvehiclestore.entity.CartItem;
import com.evs.electricvehiclestore.entity.Accessory;
import com.evs.electricvehiclestore.entity.Order;
import com.evs.electricvehiclestore.entity.OrderItem;
import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.CartItemRepository;
import com.evs.electricvehiclestore.repository.CartRepository;
import com.evs.electricvehiclestore.repository.AccessoryRepository;
import com.evs.electricvehiclestore.repository.OrderItemRepository;
import com.evs.electricvehiclestore.repository.OrderRepository;
import com.evs.electricvehiclestore.repository.VehicleRepository;

/**
 * @author Uzma Alam
 */
@Service
public class OrderService {

    private static final String STATUS_PENDING_PAYMENT = "PENDING_PAYMENT";
    private static final String STATUS_PROCESSED = "PROCESSED";
    private static final String STATUS_DENIED = "DENIED";

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentService paymentService;
    private final AccessoryRepository accessoryRepository;

    public OrderService(CartRepository cartRepository,
                         CartItemRepository cartItemRepository,
                         VehicleRepository vehicleRepository,
                         OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         PaymentService paymentService,
                         AccessoryRepository accessoryRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentService = paymentService;
        this.accessoryRepository = accessoryRepository;
    }

    /**
     * UC7 (Checkout) + UC9 (shipping half): turns the customer's cart into a
     * pending order and records the shipping address.
     */
    @Transactional
    public OrderSummaryDTO checkout(CheckoutRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No cart found for this user"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }

        double total = 0.0;
        for (CartItem cartItem : cartItems) {
            Vehicle vehicle = vehicleRepository.findById(cartItem.getVehicleId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Vehicle " + cartItem.getVehicleId() + " no longer exists"));
            total += (vehicle.getPrice() + accessoryTotal(cartItem.getAccessoryIds()))
                    * cartItem.getQuantity();
        }

        ShippingInfoDTO shipping = request.getShippingInfo();

        Order order = new Order(request.getUserId(), total, STATUS_PENDING_PAYMENT);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingStreet(shipping.getStreet());
        order.setShippingCity(shipping.getCity());
        order.setShippingProvince(shipping.getProvince());
        order.setShippingCountry(shipping.getCountry());
        order.setShippingZip(shipping.getZip());
        order.setShippingPhone(shipping.getPhone());
        order = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            Vehicle vehicle = vehicleRepository.findById(cartItem.getVehicleId()).orElseThrow();
            OrderItem orderItem = new OrderItem(order.getId(), vehicle.getId(),
                    cartItem.getQuantity(), vehicle.getPrice() + accessoryTotal(cartItem.getAccessoryIds()));
            orderItem.setAccessoryIds(cartItem.getAccessoryIds());
            orderItemRepository.save(orderItem);
        }

        return buildSummary(order);
    }

    /**
     * UC8 (Make Payment) + UC9 (credit card half): processes the payment for a pending order and updates its status accordingly.
     * If the order is already processed or denied, it returns a conflict error.
     * If the credit card is expired, it returns a bad request error.
     * If the payment is approved, the order status is updated to "PROCESSED".
     * If the payment is denied, the order status is updated to "DENIED".
     */
    @Transactional
    public PaymentResultDTO confirmOrder(Long orderId, CreditCardDTO creditCard) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Order " + orderId + " has already been " + order.getStatus().toLowerCase());
        }

        if (isExpired(creditCard)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credit card is expired");
        }

        boolean approved = paymentService.processPayment();
        order.setStatus(approved ? STATUS_PROCESSED : STATUS_DENIED);
        orderRepository.save(order);

        // A successful purchase consumes the cart. A denied payment keeps the
        // cart intact so the customer can correct the payment details and retry.
        if (approved) {
            cartRepository.findByUserId(order.getUserId())
                    .ifPresent(cart -> cartItemRepository.deleteAll(
                            cartItemRepository.findByCartId(cart.getId())
                    ));
        }

        String message = approved ? "Order Successfully Completed." : "Credit Card Authorization Failed.";
        return new PaymentResultDTO(order.getId(), approved, message, creditCard.maskedNumber());
    }

    public OrderSummaryDTO getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return buildSummary(order);
    }

    private boolean isExpired(CreditCardDTO card) {
        YearMonth expiry = YearMonth.of(Integer.parseInt(card.getExpiryYear()), Integer.parseInt(card.getExpiryMonth()));
        return expiry.isBefore(YearMonth.now());
    }

    private OrderSummaryDTO buildSummary(Order order) {
        List<OrderItemDTO> itemDTOs = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(oi -> {
                    Vehicle vehicle = vehicleRepository.findById(oi.getVehicleId()).orElse(null);
                    String brand = vehicle != null ? vehicle.getBrand() : "Unknown";
                    String model = vehicle != null ? vehicle.getModel() : "Unknown";
                    List<AccessoryDTO> accessories = accessoryRepository.findAllById(oi.getAccessoryIds()).stream()
                            .map(accessory -> new AccessoryDTO(
                                    accessory.getId(), accessory.getName(), accessory.getPrice()))
                            .toList();
                    return new OrderItemDTO(
                            oi.getVehicleId(), brand, model, oi.getQuantity(), oi.getPrice(), accessories);
                })
                .collect(Collectors.toList());

        ShippingInfoDTO shippingInfo = new ShippingInfoDTO();
        shippingInfo.setStreet(order.getShippingStreet());
        shippingInfo.setCity(order.getShippingCity());
        shippingInfo.setProvince(order.getShippingProvince());
        shippingInfo.setCountry(order.getShippingCountry());
        shippingInfo.setZip(order.getShippingZip());
        shippingInfo.setPhone(order.getShippingPhone());

        return new OrderSummaryDTO(order.getId(), order.getUserId(), order.getStatus(),
                order.getTotalAmount(), order.getOrderDate(), shippingInfo, itemDTOs);
    }

    private double accessoryTotal(java.util.Set<Long> accessoryIds) {
        if (accessoryIds == null || accessoryIds.isEmpty()) return 0.0;
        return accessoryRepository.findAllById(accessoryIds).stream()
                .filter(Accessory::isAvailable)
                .mapToDouble(Accessory::getPrice)
                .sum();
    }
}
