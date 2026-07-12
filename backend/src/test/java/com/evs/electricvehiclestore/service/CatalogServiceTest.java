package com.evs.electricvehiclestore.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void listVehiclesReturnsAllVehicles() {
        Vehicle tesla = new Vehicle(
                "Tesla",
                "Model 3",
                2023,
                42000,
                15000,
                "Sedan",
                true
        );

        Vehicle nissan = new Vehicle(
                "Nissan",
                "Leaf",
                2021,
                24000,
                30000,
                "Hatchback",
                false
        );

        when(vehicleRepository.findAll()).thenReturn(List.of(tesla, nissan));

        List<Vehicle> vehicles = catalogService.listVehicles();

        assertEquals(2, vehicles.size());
        assertEquals("Tesla", vehicles.get(0).getBrand());
        assertEquals("Nissan", vehicles.get(1).getBrand());
    }

    @Test
    void getVehicleReturnsVehicleWhenIdExists() {
        Vehicle vehicle = new Vehicle(
                "Tesla",
                "Model Y",
                2024,
                56000,
                8000,
                "SUV",
                false
        );

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle result = catalogService.getVehicle(1L);

        assertEquals("Tesla", result.getBrand());
        assertEquals("Model Y", result.getModel());
    }

    @Test
    void getVehicleThrowsNotFoundWhenIdDoesNotExist() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> catalogService.getVehicle(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(
                "Vehicle not found with ID: 99",
                exception.getReason()
        );
    }

    @Test
    void searchVehiclesReturnsMatchingBrandsOrModels() {
        Vehicle vehicle = new Vehicle(
                "Tesla",
                "Model 3",
                2023,
                42000,
                15000,
                "Sedan",
                true
        );

        when(
                vehicleRepository
                        .findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                                "Tesla",
                                "Tesla"
                        )
        ).thenReturn(List.of(vehicle));

        List<Vehicle> results = catalogService.searchVehicles(" Tesla ");

        assertEquals(1, results.size());
        assertEquals("Tesla", results.get(0).getBrand());

        verify(vehicleRepository)
                .findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                        "Tesla",
                        "Tesla"
                );
    }

    @Test
    void searchVehiclesRejectsBlankKeyword() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> catalogService.searchVehicles("   ")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "A search keyword is required",
                exception.getReason()
        );
    }

    @Test
    void filterByPriceRangeReturnsMatchingVehicles() {
        Vehicle vehicle = new Vehicle(
                "Hyundai",
                "Ioniq 5",
                2023,
                48000,
                12000,
                "SUV",
                false
        );

        when(vehicleRepository.findByPriceBetween(40000, 50000))
                .thenReturn(List.of(vehicle));

        List<Vehicle> results =
                catalogService.filterByPriceRange(40000, 50000);

        assertEquals(1, results.size());
        assertEquals("Hyundai", results.get(0).getBrand());
        assertEquals(48000.0, results.get(0).getPrice());
    }

    @Test
    void filterByPriceRangeRejectsInvalidRange() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> catalogService.filterByPriceRange(60000, 30000)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "The price range is invalid",
                exception.getReason()
        );
    }

    @Test
    void sortByPriceReturnsVehiclesFromLowestToHighest() {
        Vehicle expensiveVehicle = new Vehicle(
                "Tesla",
                "Model Y",
                2024,
                56000,
                8000,
                "SUV",
                false
        );

        Vehicle affordableVehicle = new Vehicle(
                "Nissan",
                "Leaf",
                2021,
                24000,
                30000,
                "Hatchback",
                true
        );

        when(vehicleRepository.findAll())
                .thenReturn(List.of(expensiveVehicle, affordableVehicle));

        List<Vehicle> sortedVehicles = catalogService.sortByPrice();

        assertEquals("Nissan", sortedVehicles.get(0).getBrand());
        assertEquals(24000.0, sortedVehicles.get(0).getPrice());
        assertEquals("Tesla", sortedVehicles.get(1).getBrand());
        assertEquals(56000.0, sortedVehicles.get(1).getPrice());
    }
}