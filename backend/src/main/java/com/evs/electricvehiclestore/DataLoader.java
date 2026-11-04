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
            // Tesla
            vehicleRepository.save(new Vehicle("Tesla", "Model 3", 2023, 42000, 15000, "Sedan", true));
            vehicleRepository.save(new Vehicle("Tesla", "Model Y", 2024, 56000, 8000, "SUV", false));
            vehicleRepository.save(new Vehicle("Tesla", "Model S", 2023, 89000, 6000, "Sedan", false));
            vehicleRepository.save(new Vehicle("Tesla", "Model X", 2022, 98000, 21000, "SUV", false));

            // Nissan
            vehicleRepository.save(new Vehicle("Nissan", "Leaf", 2021, 24000, 30000, "Hatchback", true));
            vehicleRepository.save(new Vehicle("Nissan", "Ariya", 2023, 47000, 9500, "SUV", false));

            // Hyundai
            vehicleRepository.save(new Vehicle("Hyundai", "Ioniq 5", 2023, 48000, 12000, "SUV", false));
            vehicleRepository.save(new Vehicle("Hyundai", "Ioniq 6", 2024, 46000, 4000, "Sedan", true));
            vehicleRepository.save(new Vehicle("Hyundai", "Kona Electric", 2022, 35000, 24000, "Hatchback", false));

            // Ford
            vehicleRepository.save(new Vehicle("Ford", "Mustang Mach-E", 2022, 51000, 18000, "SUV", true));
            vehicleRepository.save(new Vehicle("Ford", "F-150 Lightning", 2023, 68000, 11000, "Truck", false));

            // Chevrolet
            vehicleRepository.save(new Vehicle("Chevrolet", "Bolt EUV", 2023, 29000, 16000, "Hatchback", true));
            vehicleRepository.save(new Vehicle("Chevrolet", "Equinox EV", 2024, 39000, 3000, "SUV", false));

            // Kia
            vehicleRepository.save(new Vehicle("Kia", "EV6", 2022, 38000, 22000, "Hatchback", true));
            vehicleRepository.save(new Vehicle("Kia", "Niro EV", 2023, 41000, 14000, "SUV", false));

            // BMW
            vehicleRepository.save(new Vehicle("BMW", "iX3", 2023, 67000, 5000, "SUV", false));
            vehicleRepository.save(new Vehicle("BMW", "i4", 2023, 59000, 9000, "Sedan", false));
            vehicleRepository.save(new Vehicle("BMW", "iX", 2024, 92000, 2500, "SUV", false));

            // Audi
            vehicleRepository.save(new Vehicle("Audi", "e-tron GT", 2024, 105000, 2000, "Sedan", false));
            vehicleRepository.save(new Vehicle("Audi", "Q4 e-tron", 2023, 58000, 10000, "SUV", false));

            // Volkswagen
            vehicleRepository.save(new Vehicle("Volkswagen", "ID.4", 2022, 43000, 18000, "SUV", true));

            // Volvo
            vehicleRepository.save(new Vehicle("Volvo", "XC40 Recharge", 2023, 55000, 8500, "SUV", false));

            // Polestar
            vehicleRepository.save(new Vehicle("Polestar", "2", 2023, 53000, 13000, "Hatchback", true));

            // Rivian
            vehicleRepository.save(new Vehicle("Rivian", "R1T", 2023, 78000, 7000, "Truck", false));
            vehicleRepository.save(new Vehicle("Rivian", "R1S", 2023, 82000, 6500, "SUV", false));

            // Porsche
            vehicleRepository.save(new Vehicle("Porsche", "Taycan", 2024, 99000, 3500, "Sedan", false));

            // Cadillac
            vehicleRepository.save(new Vehicle("Cadillac", "Lyriq", 2024, 65000, 4200, "SUV", false));

            // Toyota
            vehicleRepository.save(new Vehicle("Toyota", "bZ4X", 2023, 44000, 15500, "SUV", true));

            // Genesis
            vehicleRepository.save(new Vehicle("Genesis", "GV60", 2023, 62000, 9800, "SUV", false));

            // Mini
            vehicleRepository.save(new Vehicle("Mini", "Cooper SE", 2022, 32000, 19000, "Hatchback", true));
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