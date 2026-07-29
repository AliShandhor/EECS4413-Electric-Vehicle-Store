package com.evs.electricvehiclestore.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyCollection;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evs.electricvehiclestore.dto.SalesReportResponse;
import com.evs.electricvehiclestore.entity.Order;
import com.evs.electricvehiclestore.entity.OrderItem;
import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.OrderItemRepository;
import com.evs.electricvehiclestore.repository.OrderRepository;
import com.evs.electricvehiclestore.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void vehicleSalesReportCalculatesSalesMetrics() {
        Order completedOrder = order(
                1L,
                84000,
                "COMPLETED"
        );

        Order deliveredOrder = order(
                2L,
                48000,
                "DELIVERED"
        );

        Order cancelledOrder = order(
                3L,
                30000,
                "CANCELLED"
        );

        OrderItem teslaItem =
                new OrderItem(1L, 1L, 2, 42000);

        OrderItem hyundaiItem =
                new OrderItem(2L, 2L, 1, 48000);

        Vehicle tesla = vehicle(
                1L,
                "Tesla",
                "Model 3",
                42000
        );

        Vehicle hyundai = vehicle(
                2L,
                "Hyundai",
                "Ioniq 5",
                48000
        );

        when(orderRepository.findAll()).thenReturn(
                List.of(
                        completedOrder,
                        deliveredOrder,
                        cancelledOrder
                )
        );

        when(orderItemRepository.findByOrderIdIn(
                anyCollection()
        )).thenReturn(
                List.of(teslaItem, hyundaiItem)
        );

        when(vehicleRepository.findAllById(
                anyCollection()
        )).thenReturn(
                List.of(tesla, hyundai)
        );

        SalesReportResponse report =
                analyticsService.vehicleSalesReport();

        assertEquals(3, report.totalOrders());
        assertEquals(2, report.completedSales());
        assertEquals(1, report.excludedOrders());
        assertEquals(3, report.totalVehiclesSold());

        assertMoneyEquals(
                "132000.00",
                report.grossRevenue()
        );

        assertMoneyEquals(
                "66000.00",
                report.averageOrderValue()
        );

        assertEquals(
                1L,
                report.ordersByStatus().get("COMPLETED")
        );

        assertEquals(
                1L,
                report.ordersByStatus().get("DELIVERED")
        );

        assertEquals(
                1L,
                report.ordersByStatus().get("CANCELLED")
        );

        assertEquals(2, report.vehicleSales().size());

        assertNotNull(report.topSellingVehicle());
        assertEquals(
                "Tesla",
                report.topSellingVehicle().brand()
        );
        assertEquals(
                "Model 3",
                report.topSellingVehicle().model()
        );
        assertEquals(
                2,
                report.topSellingVehicle().unitsSold()
        );

        assertEquals(
                "Sales report generated successfully",
                report.message()
        );
    }

    @Test
    void vehicleSalesReportReturnsEmptyReportWhenNoOrdersExist() {
        when(orderRepository.findAll())
                .thenReturn(List.of());

        SalesReportResponse report =
                analyticsService.vehicleSalesReport();

        assertEquals(0, report.totalOrders());
        assertEquals(0, report.completedSales());
        assertEquals(0, report.totalVehiclesSold());

        assertMoneyEquals(
                "0.00",
                report.grossRevenue()
        );

        assertTrue(report.vehicleSales().isEmpty());
        assertTrue(report.ordersByStatus().isEmpty());
        assertNull(report.topSellingVehicle());

        assertEquals(
                "No orders are available yet",
                report.message()
        );

        verify(orderItemRepository, never())
                .findByOrderIdIn(anyCollection());
    }

    @Test
    void vehicleSalesReportExcludesPendingDeniedAndCancelledOrders() {
        Order pendingOrder = order(
                1L,
                42000,
                "PENDING_PAYMENT"
        );

        Order deniedOrder = order(
                2L,
                48000,
                "DENIED"
        );

        Order cancelledOrder = order(
                3L,
                30000,
                "CANCELLED"
        );

        when(orderRepository.findAll()).thenReturn(
                List.of(
                        pendingOrder,
                        deniedOrder,
                        cancelledOrder
                )
        );

        SalesReportResponse report =
                analyticsService.vehicleSalesReport();

        assertEquals(3, report.totalOrders());
        assertEquals(0, report.completedSales());
        assertEquals(3, report.excludedOrders());
        assertEquals(0, report.totalVehiclesSold());

        assertEquals(
                "No completed vehicle sales are available yet",
                report.message()
        );

        verify(orderItemRepository, never())
                .findByOrderIdIn(anyCollection());
    }

    @Test
    void vehicleSalesReportHandlesMissingVehicleDetails() {
        Order completedOrder = order(
                1L,
                30000,
                "PAID"
        );

        OrderItem item =
                new OrderItem(1L, 99L, 1, 30000);

        when(orderRepository.findAll())
                .thenReturn(List.of(completedOrder));

        when(orderItemRepository.findByOrderIdIn(
                anyCollection()
        )).thenReturn(List.of(item));

        when(vehicleRepository.findAllById(
                anyCollection()
        )).thenReturn(List.of());

        SalesReportResponse report =
                analyticsService.vehicleSalesReport();

        assertNotNull(report.topSellingVehicle());

        assertEquals(
                "Unknown",
                report.topSellingVehicle().brand()
        );

        assertEquals(
                "Vehicle 99",
                report.topSellingVehicle().model()
        );

        assertEquals(
                1,
                report.topSellingVehicle().unitsSold()
        );
    }

    private Order order(
            Long id,
            double totalAmount,
            String status
    ) {
        Order order = new Order(
                10L,
                totalAmount,
                status
        );

        order.setId(id);

        return order;
    }

    private Vehicle vehicle(
            Long id,
            String brand,
            String model,
            double price
    ) {
        Vehicle vehicle = new Vehicle(
                brand,
                model,
                2024,
                price,
                10000,
                "SUV",
                false
        );

        vehicle.setId(id);

        return vehicle;
    }

    private void assertMoneyEquals(
            String expected,
            BigDecimal actual
    ) {
        assertEquals(
                0,
                new BigDecimal(expected).compareTo(actual)
        );
    }
}