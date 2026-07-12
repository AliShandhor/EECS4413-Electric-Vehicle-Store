package com.evs.electricvehiclestore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.evs.electricvehiclestore.dto.CartResponse;
import com.evs.electricvehiclestore.dto.CartResponse.CartItemView;
import com.evs.electricvehiclestore.dto.CartResponse.SavedVehicleView;
import com.evs.electricvehiclestore.entity.Cart;
import com.evs.electricvehiclestore.entity.CartItem;
import com.evs.electricvehiclestore.entity.SavedVehicle;
import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.CartItemRepository;
import com.evs.electricvehiclestore.repository.CartRepository;
import com.evs.electricvehiclestore.repository.SavedVehicleRepository;
import com.evs.electricvehiclestore.repository.VehicleRepository;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final SavedVehicleRepository savedVehicleRepository;
    private final VehicleRepository vehicleRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            SavedVehicleRepository savedVehicleRepository,
            VehicleRepository vehicleRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.savedVehicleRepository = savedVehicleRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        validateUserId(userId);

        Optional<Cart> cart = cartRepository.findByUserId(userId);

        return buildResponse(
                userId,
                cart.orElse(null),
                "Cart loaded successfully"
        );
    }

    @Transactional
    public CartResponse addItem(
            Long userId,
            Long vehicleId,
            int quantity
    ) {
        validateUserId(userId);
        validateQuantity(quantity);

        Vehicle vehicle = getVehicle(vehicleId);

        if (!vehicle.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    vehicle.getBrand() + " " + vehicle.getModel()
                            + " is currently unavailable"
            );
        }

        Cart cart = getOrCreateCart(userId);

        if (cartItemRepository.existsByCartIdAndVehicleId(
                cart.getId(),
                vehicleId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This vehicle is already in your cart"
            );
        }

        cartItemRepository.save(
                new CartItem(cart.getId(), vehicleId, quantity)
        );

        savedVehicleRepository
                .findByUserIdAndVehicleId(userId, vehicleId)
                .ifPresent(savedVehicleRepository::delete);

        return buildResponse(
                userId,
                cart,
                vehicle.getBrand() + " " + vehicle.getModel()
                        + " was added to your cart"
        );
    }

    @Transactional
    public CartResponse updateQuantity(
            Long userId,
            Long vehicleId,
            int quantity
    ) {
        validateUserId(userId);
        validateQuantity(quantity);

        Cart cart = getExistingCart(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndVehicleId(cart.getId(), vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "This vehicle is not in your cart"
                ));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return buildResponse(
                userId,
                cart,
                "Cart quantity updated successfully"
        );
    }

    @Transactional
    public CartResponse removeItem(
            Long userId,
            Long vehicleId
    ) {
        validateUserId(userId);

        Cart cart = getExistingCart(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndVehicleId(cart.getId(), vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "This vehicle is not in your cart"
                ));

        Vehicle vehicle = getVehicle(vehicleId);

        cartItemRepository.delete(item);

        return buildResponse(
                userId,
                cart,
                vehicle.getBrand() + " " + vehicle.getModel()
                        + " was removed from your cart"
        );
    }

    @Transactional
    public CartResponse saveForLater(
            Long userId,
            Long vehicleId
    ) {
        validateUserId(userId);

        Vehicle vehicle = getVehicle(vehicleId);

        Optional<Cart> cart = cartRepository.findByUserId(userId);
        boolean movedFromCart = false;

        if (cart.isPresent()) {
            Optional<CartItem> cartItem =
                    cartItemRepository.findByCartIdAndVehicleId(
                            cart.get().getId(),
                            vehicleId
                    );

            if (cartItem.isPresent()) {
                cartItemRepository.delete(cartItem.get());
                movedFromCart = true;
            }
        }

        if (savedVehicleRepository.existsByUserIdAndVehicleId(
                userId,
                vehicleId
        )) {
            if (movedFromCart) {
                return buildResponse(
                        userId,
                        cart.orElse(null),
                        vehicle.getBrand() + " " + vehicle.getModel()
                                + " was removed from your cart and was "
                                + "already saved for later"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This vehicle is already saved for later"
            );
        }

        savedVehicleRepository.save(
                new SavedVehicle(userId, vehicleId)
        );

        String message;

        if (movedFromCart) {
            message = vehicle.getBrand() + " " + vehicle.getModel()
                    + " was moved from your cart to Save for Later";
        } else {
            message = vehicle.getBrand() + " " + vehicle.getModel()
                    + " was saved for later";
        }

        return buildResponse(
                userId,
                cart.orElse(null),
                message
        );
    }

    @Transactional
    public CartResponse moveSavedVehicleToCart(
            Long userId,
            Long vehicleId
    ) {
        validateUserId(userId);

        Vehicle vehicle = getVehicle(vehicleId);

        if (!vehicle.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    vehicle.getBrand() + " " + vehicle.getModel()
                            + " is still saved, but is currently unavailable"
            );
        }

        SavedVehicle savedVehicle = savedVehicleRepository
                .findByUserIdAndVehicleId(userId, vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "This vehicle is not in Save for Later"
                ));

        Cart cart = getOrCreateCart(userId);

        if (!cartItemRepository.existsByCartIdAndVehicleId(
                cart.getId(),
                vehicleId
        )) {
            cartItemRepository.save(
                    new CartItem(cart.getId(), vehicleId, 1)
            );
        }

        savedVehicleRepository.delete(savedVehicle);

        return buildResponse(
                userId,
                cart,
                vehicle.getBrand() + " " + vehicle.getModel()
                        + " was moved to your cart"
        );
    }

    @Transactional
    public CartResponse removeSavedVehicle(
            Long userId,
            Long vehicleId
    ) {
        validateUserId(userId);

        SavedVehicle savedVehicle = savedVehicleRepository
                .findByUserIdAndVehicleId(userId, vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "This vehicle is not in Save for Later"
                ));

        Vehicle vehicle = getVehicle(vehicleId);

        savedVehicleRepository.delete(savedVehicle);

        Cart cart = cartRepository
                .findByUserId(userId)
                .orElse(null);

        return buildResponse(
                userId,
                cart,
                vehicle.getBrand() + " " + vehicle.getModel()
                        + " was removed from Save for Later"
        );
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository
                .findByUserId(userId)
                .orElseGet(() ->
                        cartRepository.save(new Cart(userId))
                );
    }

    private Cart getExistingCart(Long userId) {
        return cartRepository
                .findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No shopping cart exists for this user"
                ));
    }

    private Vehicle getVehicle(Long vehicleId) {
        if (vehicleId == null || vehicleId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid vehicle ID is required"
            );
        }

        return vehicleRepository
                .findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found with ID: " + vehicleId
                ));
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid user ID is required"
            );
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be at least 1"
            );
        }
    }

    private CartResponse buildResponse(
            Long userId,
            Cart cart,
            String message
    ) {
        List<CartItem> cartItems;

        if (cart == null) {
            cartItems = List.of();
        } else {
            cartItems = cartItemRepository
                    .findByCartId(cart.getId())
                    .stream()
                    .sorted(Comparator.comparing(CartItem::getId))
                    .toList();
        }

        List<SavedVehicle> savedVehicles =
                savedVehicleRepository
                        .findByUserIdOrderByIdDesc(userId);

        Set<Long> vehicleIds = new LinkedHashSet<>();

        cartItems.forEach(
                item -> vehicleIds.add(item.getVehicleId())
        );

        savedVehicles.forEach(
                savedVehicle ->
                        vehicleIds.add(savedVehicle.getVehicleId())
        );

        Map<Long, Vehicle> vehiclesById = vehicleIds.isEmpty()
                ? Map.of()
                : vehicleRepository
                        .findAllById(vehicleIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Vehicle::getId,
                                Function.identity()
                        ));

        List<CartItemView> itemViews = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (CartItem item : cartItems) {
            Vehicle vehicle = vehiclesById.get(item.getVehicleId());

            if (vehicle == null) {
                continue;
            }

            BigDecimal price =
                    BigDecimal.valueOf(vehicle.getPrice());

            BigDecimal lineTotal = price.multiply(
                    BigDecimal.valueOf(item.getQuantity())
            );

            itemViews.add(new CartItemView(
                    item.getId(),
                    vehicle.getId(),
                    vehicle.getBrand(),
                    vehicle.getModel(),
                    vehicle.getModelYear(),
                    price,
                    vehicle.getMileage(),
                    vehicle.getShape(),
                    vehicle.isHotDeal(),
                    vehicle.isAvailable(),
                    item.getQuantity(),
                    lineTotal
            ));

            subtotal = subtotal.add(lineTotal);
            totalQuantity += item.getQuantity();
        }

        List<SavedVehicleView> savedViews =
                savedVehicles.stream()
                        .map(savedVehicle -> {
                            Vehicle vehicle = vehiclesById.get(
                                    savedVehicle.getVehicleId()
                            );

                            if (vehicle == null) {
                                return null;
                            }

                            return new SavedVehicleView(
                                    savedVehicle.getId(),
                                    vehicle.getId(),
                                    vehicle.getBrand(),
                                    vehicle.getModel(),
                                    vehicle.getModelYear(),
                                    BigDecimal.valueOf(
                                            vehicle.getPrice()
                                    ),
                                    vehicle.getMileage(),
                                    vehicle.getShape(),
                                    vehicle.isHotDeal(),
                                    vehicle.isAvailable()
                            );
                        })
                        .filter(view -> view != null)
                        .toList();

        return new CartResponse(
                cart == null ? null : cart.getId(),
                userId,
                itemViews.size(),
                totalQuantity,
                subtotal,
                itemViews,
                savedViews.size(),
                savedViews,
                message
        );
    }
}