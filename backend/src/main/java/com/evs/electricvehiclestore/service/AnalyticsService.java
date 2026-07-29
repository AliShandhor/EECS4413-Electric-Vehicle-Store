package com.evs.electricvehiclestore.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evs.electricvehiclestore.dto.SalesReportResponse;
import com.evs.electricvehiclestore.dto.SalesReportResponse.VehicleSalesView;
import com.evs.electricvehiclestore.entity.Order;
import com.evs.electricvehiclestore.entity.OrderItem;
import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.OrderItemRepository;
import com.evs.electricvehiclestore.repository.OrderRepository;
import com.evs.electricvehiclestore.repository.VehicleRepository;

@Service
public class AnalyticsService {

    /*
     * These statuses do not represent completed vehicle sales.
     * Other statuses, such as COMPLETED, PAID, SHIPPED,
     * DELIVERED, CONFIRMED, or PROCESSING, are included.
     */
    private static final Set<String> NON_SALE_STATUSES = Set.of(
            "UNKNOWN",
            "NEW",
            "CREATED",
            "PENDING",
            "PENDING_PAYMENT",
            "PAYMENT_PENDING",
            "PAYMENT_FAILED",
            "FAILED",
            "DENIED",
            "DECLINED",
            "CANCELLED",
            "CANCELED",
            "REFUNDED"
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VehicleRepository vehicleRepository;

    public AnalyticsService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            VehicleRepository vehicleRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public SalesReportResponse vehicleSalesReport() {
        List<Order> allOrders = orderRepository.findAll();

        Map<String, Long> ordersByStatus =
                createStatusBreakdown(allOrders);

        List<Order> completedOrders = allOrders.stream()
                .filter(this::isSuccessfulSale)
                .toList();

        if (completedOrders.isEmpty()) {
            String message = allOrders.isEmpty()
                    ? "No orders are available yet"
                    : "No completed vehicle sales are available yet";

            return new SalesReportResponse(
                    Instant.now(),
                    allOrders.size(),
                    0,
                    allOrders.size(),
                    0,
                    money(BigDecimal.ZERO),
                    money(BigDecimal.ZERO),
                    ordersByStatus,
                    List.of(),
                    null,
                    message
            );
        }

        List<Long> completedOrderIds = completedOrders.stream()
                .map(Order::getId)
                .toList();

        List<OrderItem> soldItems =
                orderItemRepository.findByOrderIdIn(
                        completedOrderIds
                );

        Set<Long> vehicleIds = soldItems.stream()
                .map(OrderItem::getVehicleId)
                .filter(vehicleId -> vehicleId != null)
                .collect(Collectors.toCollection(
                        LinkedHashSet::new
                ));

        Map<Long, Vehicle> vehiclesById = vehicleIds.isEmpty()
                ? Map.of()
                : vehicleRepository.findAllById(vehicleIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Vehicle::getId,
                                Function.identity()
                        ));

        Map<Long, VehicleSalesAccumulator> accumulatedSales =
                new HashMap<>();

        int totalVehiclesSold = 0;

        for (OrderItem orderItem : soldItems) {
            if (orderItem.getVehicleId() == null
                    || orderItem.getQuantity() <= 0) {
                continue;
            }

            Long vehicleId = orderItem.getVehicleId();
            Vehicle vehicle = vehiclesById.get(vehicleId);

            String brand = vehicle == null
                    ? "Unknown"
                    : vehicle.getBrand();

            String model = vehicle == null
                    ? "Vehicle " + vehicleId
                    : vehicle.getModel();

            VehicleSalesAccumulator accumulator =
                    accumulatedSales.computeIfAbsent(
                            vehicleId,
                            ignored -> new VehicleSalesAccumulator(
                                    vehicleId,
                                    brand,
                                    model
                            )
                    );

            BigDecimal lineRevenue = BigDecimal
                    .valueOf(orderItem.getPrice())
                    .multiply(BigDecimal.valueOf(
                            orderItem.getQuantity()
                    ));

            accumulator.unitsSold += orderItem.getQuantity();
            accumulator.revenue =
                    accumulator.revenue.add(lineRevenue);

            totalVehiclesSold += orderItem.getQuantity();
        }

        List<VehicleSalesView> vehicleSales =
                accumulatedSales.values()
                        .stream()
                        .map(accumulator ->
                                new VehicleSalesView(
                                        accumulator.vehicleId,
                                        accumulator.brand,
                                        accumulator.model,
                                        accumulator.unitsSold,
                                        money(accumulator.revenue)
                                )
                        )
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                VehicleSalesView::unitsSold
                                        )
                                        .reversed()
                                        .thenComparing(
                                                VehicleSalesView::revenue,
                                                Comparator.reverseOrder()
                                        )
                                        .thenComparing(
                                                VehicleSalesView::brand
                                        )
                                        .thenComparing(
                                                VehicleSalesView::model
                                        )
                        )
                        .toList();

        BigDecimal grossRevenue = completedOrders.stream()
                .map(Order::getTotalAmount)
                .map(BigDecimal::valueOf)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        BigDecimal averageOrderValue = grossRevenue.divide(
                BigDecimal.valueOf(completedOrders.size()),
                2,
                RoundingMode.HALF_UP
        );

        VehicleSalesView topSellingVehicle =
                vehicleSales.isEmpty()
                        ? null
                        : vehicleSales.get(0);

        return new SalesReportResponse(
                Instant.now(),
                allOrders.size(),
                completedOrders.size(),
                allOrders.size() - completedOrders.size(),
                totalVehiclesSold,
                money(grossRevenue),
                money(averageOrderValue),
                ordersByStatus,
                vehicleSales,
                topSellingVehicle,
                "Sales report generated successfully"
        );
    }

    public String usageReport() {
        return "Usage report not implemented yet";
    }

    private Map<String, Long> createStatusBreakdown(
            List<Order> orders
    ) {
        Map<String, Long> sortedStatuses = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> normalizeStatus(
                                order.getStatus()
                        ),
                        TreeMap::new,
                        Collectors.counting()
                ));

        return new LinkedHashMap<>(sortedStatuses);
    }

    private boolean isSuccessfulSale(Order order) {
        String status = normalizeStatus(order.getStatus());

        return !NON_SALE_STATUSES.contains(status);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "UNKNOWN";
        }

        return status.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_');
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private static class VehicleSalesAccumulator {

        private final Long vehicleId;
        private final String brand;
        private final String model;

        private int unitsSold;
        private BigDecimal revenue = BigDecimal.ZERO;

        private VehicleSalesAccumulator(
                Long vehicleId,
                String brand,
                String model
        ) {
            this.vehicleId = vehicleId;
            this.brand = brand;
            this.model = model;
        }
    }
}