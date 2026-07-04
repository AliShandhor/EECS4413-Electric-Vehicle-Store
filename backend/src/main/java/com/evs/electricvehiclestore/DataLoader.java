package com.evs.electricvehiclestore;

import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;

    public DataLoader(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public void run(String... args) {
        if (vehicleRepository.count() == 0) {
            vehicleRepository.save(new Vehicle("Tesla", "Model 3", 2023, 42000, 15000, "Sedan", true));
            vehicleRepository.save(new Vehicle("Tesla", "Model Y", 2024, 56000, 8000, "SUV", false));
            vehicleRepository.save(new Vehicle("Nissan", "Leaf", 2021, 24000, 30000, "Hatchback", true));
            vehicleRepository.save(new Vehicle("Hyundai", "Ioniq 5", 2023, 48000, 12000, "SUV", false));
            vehicleRepository.save(new Vehicle("Ford", "Mustang Mach-E", 2022, 51000, 18000, "SUV", true));
        }
    }
}