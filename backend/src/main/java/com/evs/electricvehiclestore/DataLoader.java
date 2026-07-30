package com.evs.electricvehiclestore;

import com.evs.electricvehiclestore.entity.Accessory;
import com.evs.electricvehiclestore.entity.User;
import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.repository.AccessoryRepository;
import com.evs.electricvehiclestore.repository.UserRepository;
import com.evs.electricvehiclestore.repository.VehicleRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;
    private final AccessoryRepository accessoryRepository;
    private final UserRepository userRepository;

    public DataLoader(
            VehicleRepository vehicleRepository,
            AccessoryRepository accessoryRepository,
            UserRepository userRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.accessoryRepository = accessoryRepository;
        this.userRepository = userRepository;
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

        if (accessoryRepository.count() == 0) {
            accessoryRepository.save(new Accessory(
                    "All-Weather Floor Mats", "Durable fitted mats for every season", 249, true));
            accessoryRepository.save(new Accessory(
                    "Home Charging Kit", "Level 2 home charging cable and wall mount", 899, true));
            accessoryRepository.save(new Accessory(
                    "Roof Cargo Box", "Aerodynamic 420-litre cargo box", 649, true));
            accessoryRepository.save(new Accessory(
                    "Winter Wheel Package", "Four winter tires mounted on alloy wheels", 1899, true));
        }

        if (!userRepository.existsByEmail("admin@evstore.ca")) {
            String password = new BCryptPasswordEncoder().encode("Admin123!");
            userRepository.save(new User("EVS Administrator", "admin@evstore.ca", password, "ADMIN"));
        }
    }
}
