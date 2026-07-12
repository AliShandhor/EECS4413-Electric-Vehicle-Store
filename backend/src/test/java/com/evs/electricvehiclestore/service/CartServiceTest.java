package com.evs.electricvehiclestore.service;

import com.evs.electricvehiclestore.dto.CartResponse;
import com.evs.electricvehiclestore.entity.Cart;
import com.evs.electricvehiclestore.entity.CartItem;
import com.evs.electricvehiclestore.entity.SavedVehicle;
import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.CartItemRepository;
import com.evs.electricvehiclestore.repository.CartRepository;
import com.evs.electricvehiclestore.repository.SavedVehicleRepository;
import com.evs.electricvehiclestore.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private SavedVehicleRepository savedVehicleRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addItemCreatesCartAutomaticallyAndReturnsUpdatedCart() {
        Vehicle vehicle = vehicle(
                1L,
                "Tesla",
                "Model 3",
                42000,
                true
        );

        Cart createdCart = new Cart(7L);
        createdCart.setId(10L);

        CartItem storedItem = new CartItem(10L, 1L, 1);
        storedItem.setId(20L);

        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(vehicle));
        when(cartRepository.findByUserId(7L))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(createdCart);
        when(cartItemRepository.existsByCartIdAndVehicleId(10L, 1L))
                .thenReturn(false);
        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(storedItem));
        when(savedVehicleRepository.findByUserIdAndVehicleId(7L, 1L))
                .thenReturn(Optional.empty());
        when(savedVehicleRepository.findByUserIdOrderByIdDesc(7L))
                .thenReturn(List.of());
        when(vehicleRepository.findAllById(any()))
                .thenReturn(List.of(vehicle));

        CartResponse response = cartService.addItem(7L, 1L, 1);

        assertEquals(10L, response.cartId());
        assertEquals(1, response.distinctItemCount());
        assertEquals(1, response.totalQuantity());
        assertMoneyEquals(42000, response.subtotal());
        assertEquals("Tesla", response.items().get(0).brand());
        assertEquals("Model 3", response.items().get(0).model());
        assertTrue(response.message().contains("added to your cart"));

        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItemRejectsDuplicateVehicle() {
        Vehicle vehicle = vehicle(
                1L,
                "Tesla",
                "Model 3",
                42000,
                true
        );

        Cart cart = new Cart(7L);
        cart.setId(10L);

        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(vehicle));
        when(cartRepository.findByUserId(7L))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.existsByCartIdAndVehicleId(10L, 1L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addItem(7L, 1L, 1)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "This vehicle is already in your cart",
                exception.getReason()
        );

        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addItemRejectsUnavailableVehicle() {
        Vehicle vehicle = vehicle(
                1L,
                "Chevrolet",
                "Bolt EV",
                30000,
                false
        );

        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(vehicle));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.addItem(7L, 1L, 1)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("currently unavailable"));

        verify(cartRepository, never()).findByUserId(7L);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void getCartCalculatesCountsAndSubtotal() {
        Cart cart = new Cart(7L);
        cart.setId(10L);

        Vehicle tesla = vehicle(
                1L,
                "Tesla",
                "Model 3",
                42000,
                true
        );

        Vehicle nissan = vehicle(
                2L,
                "Nissan",
                "Leaf",
                24000,
                true
        );

        Vehicle ford = vehicle(
                3L,
                "Ford",
                "Mustang Mach-E",
                51000,
                true
        );

        CartItem teslaItem = new CartItem(10L, 1L, 2);
        teslaItem.setId(20L);

        CartItem nissanItem = new CartItem(10L, 2L, 1);
        nissanItem.setId(21L);

        SavedVehicle savedFord = new SavedVehicle(7L, 3L);
        savedFord.setId(30L);

        when(cartRepository.findByUserId(7L))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(teslaItem, nissanItem));
        when(savedVehicleRepository.findByUserIdOrderByIdDesc(7L))
                .thenReturn(List.of(savedFord));
        when(vehicleRepository.findAllById(any()))
                .thenReturn(List.of(tesla, nissan, ford));

        CartResponse response = cartService.getCart(7L);

        assertEquals(2, response.distinctItemCount());
        assertEquals(3, response.totalQuantity());
        assertMoneyEquals(108000, response.subtotal());
        assertEquals(1, response.savedCount());
        assertEquals(
                "Mustang Mach-E",
                response.savedForLater().get(0).model()
        );
    }

    @Test
    void saveForLaterCanSaveVehicleDirectlyFromCatalogue() {
        Vehicle vehicle = vehicle(
                1L,
                "Tesla",
                "Model Y",
                56000,
                true
        );

        SavedVehicle savedVehicle = new SavedVehicle(7L, 1L);
        savedVehicle.setId(30L);

        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(vehicle));
        when(cartRepository.findByUserId(7L))
                .thenReturn(Optional.empty());
        when(savedVehicleRepository.existsByUserIdAndVehicleId(7L, 1L))
                .thenReturn(false);
        when(savedVehicleRepository.findByUserIdOrderByIdDesc(7L))
                .thenReturn(List.of(savedVehicle));
        when(vehicleRepository.findAllById(any()))
                .thenReturn(List.of(vehicle));

        CartResponse response = cartService.saveForLater(7L, 1L);

        assertNull(response.cartId());
        assertEquals(0, response.distinctItemCount());
        assertEquals(1, response.savedCount());
        assertEquals("Model Y", response.savedForLater().get(0).model());
        assertTrue(response.message().contains("saved for later"));

        verify(savedVehicleRepository)
                .save(any(SavedVehicle.class));
    }

    @Test
    void saveForLaterMovesVehicleOutOfCart() {
        Vehicle vehicle = vehicle(
                1L,
                "Tesla",
                "Model 3",
                42000,
                true
        );

        Cart cart = new Cart(7L);
        cart.setId(10L);

        CartItem cartItem = new CartItem(10L, 1L, 1);
        cartItem.setId(20L);

        SavedVehicle savedVehicle = new SavedVehicle(7L, 1L);
        savedVehicle.setId(30L);

        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(vehicle));
        when(cartRepository.findByUserId(7L))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndVehicleId(10L, 1L))
                .thenReturn(Optional.of(cartItem));
        when(savedVehicleRepository.existsByUserIdAndVehicleId(7L, 1L))
                .thenReturn(false);
        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of());
        when(savedVehicleRepository.findByUserIdOrderByIdDesc(7L))
                .thenReturn(List.of(savedVehicle));
        when(vehicleRepository.findAllById(any()))
                .thenReturn(List.of(vehicle));

        CartResponse response = cartService.saveForLater(7L, 1L);

        assertEquals(0, response.distinctItemCount());
        assertEquals(1, response.savedCount());
        assertTrue(response.message().contains("moved from your cart"));

        verify(cartItemRepository).delete(cartItem);
        verify(savedVehicleRepository)
                .save(any(SavedVehicle.class));
    }

    @Test
    void moveSavedVehicleToCartChecksAvailabilityAndMovesVehicle() {
        Vehicle vehicle = vehicle(
                1L,
                "Hyundai",
                "Ioniq 5",
                48000,
                true
        );

        SavedVehicle savedVehicle = new SavedVehicle(7L, 1L);
        savedVehicle.setId(30L);

        Cart cart = new Cart(7L);
        cart.setId(10L);

        CartItem storedItem = new CartItem(10L, 1L, 1);
        storedItem.setId(20L);

        when(vehicleRepository.findById(1L))
                .thenReturn(Optional.of(vehicle));
        when(savedVehicleRepository.findByUserIdAndVehicleId(7L, 1L))
                .thenReturn(Optional.of(savedVehicle));
        when(cartRepository.findByUserId(7L))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.existsByCartIdAndVehicleId(10L, 1L))
                .thenReturn(false);
        when(cartItemRepository.findByCartId(10L))
                .thenReturn(List.of(storedItem));
        when(savedVehicleRepository.findByUserIdOrderByIdDesc(7L))
                .thenReturn(List.of());
        when(vehicleRepository.findAllById(any()))
                .thenReturn(List.of(vehicle));

        CartResponse response =
                cartService.moveSavedVehicleToCart(7L, 1L);

        assertEquals(1, response.distinctItemCount());
        assertEquals(0, response.savedCount());
        assertEquals("Ioniq 5", response.items().get(0).model());
        assertTrue(response.message().contains("moved to your cart"));

        verify(cartItemRepository).save(any(CartItem.class));
        verify(savedVehicleRepository).delete(savedVehicle);
    }

    @Test
    void updateQuantityRejectsQuantityBelowOne() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> cartService.updateQuantity(7L, 1L, 0)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "Quantity must be at least 1",
                exception.getReason()
        );
    }

    private Vehicle vehicle(
            Long id,
            String brand,
            String model,
            double price,
            boolean available
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
        vehicle.setAvailable(available);

        return vehicle;
    }

    private void assertMoneyEquals(
            double expected,
            BigDecimal actual
    ) {
        assertEquals(
                0,
                BigDecimal.valueOf(expected).compareTo(actual)
        );
    }
}