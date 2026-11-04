package com.evs.electricvehiclestore.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.evs.electricvehiclestore.entity.Vehicle;
import com.evs.electricvehiclestore.entity.VehicleImage;
import com.evs.electricvehiclestore.dto.VehicleRequest;
import com.evs.electricvehiclestore.repository.VehicleImageRepository;
import com.evs.electricvehiclestore.repository.VehicleRepository;

@Service
public class CatalogService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final VehicleRepository vehicleRepository;
    private final VehicleImageRepository vehicleImageRepository;

    public CatalogService(
            VehicleRepository vehicleRepository,
            VehicleImageRepository vehicleImageRepository
    ) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleImageRepository = vehicleImageRepository;
    }

    public List<Vehicle> listVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicle(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vehicle not found with ID: " + vehicleId
                ));
    }

    public List<Vehicle> filterByBrand(String brand) {
        return vehicleRepository.findByBrandIgnoreCase(brand);
    }

    public List<Vehicle> filterByShape(String shape) {
        return vehicleRepository.findByShapeIgnoreCase(shape);
    }

    public List<Vehicle> filterByYear(int year) {
        return vehicleRepository.findByModelYear(year);
    }

    public List<Vehicle> searchVehicles(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A search keyword is required"
            );
        }

        String cleanedKeyword = keyword.trim();

        return vehicleRepository
                .findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                        cleanedKeyword,
                        cleanedKeyword
                );
    }

    public List<Vehicle> filterByPriceRange(
            double minimumPrice,
            double maximumPrice
    ) {
        if (minimumPrice < 0 || maximumPrice < minimumPrice) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The price range is invalid"
            );
        }

        return vehicleRepository.findByPriceBetween(
                minimumPrice,
                maximumPrice
        );
    }

    public List<Vehicle> sortByPrice() {
        return vehicleRepository.findAll()
                .stream()
                .sorted(Comparator.comparingDouble(Vehicle::getPrice))
                .toList();
    }

    public List<Vehicle> sortByMileage() {
        return vehicleRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(Vehicle::getMileage))
                .toList();
    }

    public List<Vehicle> getHotDeals() {
        return vehicleRepository.findByHotDealTrue();
    }

    public Vehicle addVehicle(VehicleRequest request) {
        Vehicle vehicle = new Vehicle(
                request.brand().trim(),
                request.model().trim(),
                request.modelYear(),
                request.price(),
                request.mileage(),
                request.shape().trim(),
                request.hotDeal()
        );
        vehicle.setAvailable(request.available());
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle saveVehicleImage(Long vehicleId, MultipartFile image) {
        Vehicle vehicle = getVehicle(vehicleId);
        validateImage(image);

        try {
            byte[] data = image.getBytes();
            String contentType = image.getContentType();
            if (!hasValidImageSignature(contentType, data)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "The selected file does not contain a valid JPEG, PNG, or WebP image"
                );
            }

            vehicleImageRepository.save(new VehicleImage(vehicleId, contentType, data));
            vehicle.setImageAvailable(true);
            return vehicleRepository.save(vehicle);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "The vehicle image could not be read",
                    exception
            );
        }
    }

    public VehicleImage getVehicleImage(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vehicle not found with ID: " + vehicleId
            );
        }
        return vehicleImageRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No image has been uploaded for vehicle ID: " + vehicleId
                ));
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select an image");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Vehicle images must be 5 MB or smaller"
            );
        }
        if (!ALLOWED_IMAGE_TYPES.contains(image.getContentType())) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only JPEG, PNG, and WebP images are supported"
            );
        }
    }

    private boolean hasValidImageSignature(String contentType, byte[] data) {
        return switch (contentType) {
            case "image/jpeg" -> data.length >= 3
                    && (data[0] & 0xff) == 0xff
                    && (data[1] & 0xff) == 0xd8
                    && (data[2] & 0xff) == 0xff;
            case "image/png" -> data.length >= 8
                    && (data[0] & 0xff) == 0x89
                    && data[1] == 0x50
                    && data[2] == 0x4e
                    && data[3] == 0x47
                    && data[4] == 0x0d
                    && data[5] == 0x0a
                    && data[6] == 0x1a
                    && data[7] == 0x0a;
            case "image/webp" -> data.length >= 12
                    && data[0] == 'R'
                    && data[1] == 'I'
                    && data[2] == 'F'
                    && data[3] == 'F'
                    && data[8] == 'W'
                    && data[9] == 'E'
                    && data[10] == 'B'
                    && data[11] == 'P';
            default -> false;
        };
    }
}
