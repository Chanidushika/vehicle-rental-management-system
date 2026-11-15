package com.rental.service;

import com.rental.model.Booking;
import com.rental.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ============================================================
// AdminDashboardService — Summarises platform stats for Admin.
// Member 06: Total revenue and total bookings from files.
// ============================================================
@Service
public class AdminDashboardService {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    // Build the stats map that the Admin Dashboard page displays
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Vehicle> allVehicles  = vehicleService.getAllVehicles();
        List<Booking> allBookings  = bookingService.getAllBookings();

        // Count totals
        long totalVehicles    = allVehicles.size();
        long availableVehicles = allVehicles.stream().filter(Vehicle::isAvailableStatus).count();
        long activeBookings   = allBookings.stream().filter(b -> b.getReturnDate() == null).count();
        long totalUsers       = userService.getAllUsers().size();

        // Member 06 — Total revenue = all rental costs + all late fees
        double totalRevenue = allBookings.stream()
                .mapToDouble(b -> b.getTotalCost() + b.getLateFee())
                .sum();

        // Find which vehicle type is rented the most
        String mostRentedType = "N/A";
        if (!allBookings.isEmpty()) {
            Map<String, Long> typeCounts = allBookings.stream()
                    .collect(Collectors.groupingBy(Booking::getVehicleId, Collectors.counting()));

            // Count by vehicle type — map vehicleId to type first
            Map<String, Long> typeCountByType = new HashMap<>();
            for (Booking b : allBookings) {
                Vehicle v = vehicleService.getVehicleById(b.getVehicleId());
                if (v != null) {
                    String type = v.getType();
                    typeCountByType.put(type, typeCountByType.getOrDefault(type, 0L) + 1);
                }
            }

            mostRentedType = typeCountByType.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
        }

        // Put all data into the stats map for Thymeleaf to display
        stats.put("totalVehicles",     totalVehicles);
        stats.put("availableVehicles", availableVehicles);
        stats.put("activeBookings",    activeBookings);
        stats.put("totalUsers",        totalUsers);
        stats.put("totalRevenue",      totalRevenue);
        stats.put("mostRentedType",    mostRentedType);
        stats.put("totalBookings",     allBookings.size()); // Member 06

        return stats;
    }
}
