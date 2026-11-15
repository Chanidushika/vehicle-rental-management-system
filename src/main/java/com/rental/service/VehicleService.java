package com.rental.service;

import com.rental.model.*;
import com.rental.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ============================================================
// VehicleService — Business logic for all vehicle operations.
// Member 01: Toggle vehicle status Available <-> Rented
// Member 04: Filter/search vehicles by type, location, price
// Member 05: Price calculation is polymorphic (done in model)
// ============================================================
@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    // Add a new vehicle — generate a UUID if no ID provided
    public Vehicle addVehicle(Vehicle vehicle) {
        if (vehicle.getId() == null || vehicle.getId().isEmpty()) {
            vehicle.setId(UUID.randomUUID().toString());
        }
        vehicleRepository.save(vehicle);
        return vehicle;
    }

    // Get all vehicles
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    // Get a single vehicle by its UUID
    public Vehicle getVehicleById(String id) {
        return vehicleRepository.findById(id).orElse(null);
    }

    // Delete a vehicle by ID (Admin only)
    public void deleteVehicle(String id) {
        vehicleRepository.delete(id);
    }

    // Get only vehicles that are currently available
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findAvailable();
    }

    // Member 04 — Search with filters (type, location, price range)
    public List<Vehicle> searchVehicles(String type, String location, Double minPrice, Double maxPrice) {
        return vehicleRepository.search(type, location, minPrice, maxPrice);
    }

    // Member 01 — Set available status directly (used by BookingService)
    public void updateVehicleStatus(String id, boolean status) {
        Vehicle vehicle = getVehicleById(id);
        if (vehicle != null) {
            vehicle.setAvailableStatus(status);
            vehicleRepository.save(vehicle);
        }
    }
}
