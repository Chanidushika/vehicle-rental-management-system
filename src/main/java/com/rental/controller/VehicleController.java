package com.rental.controller;

import com.rental.model.*;
import com.rental.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// ============================================================
// VehicleController — Manage vehicles (Admin CRUD + Customer browse).
// Member 01: Admin can toggle vehicle status Available <-> Rented
// Member 04: Customer can search/filter vehicles
// ============================================================
@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    // List ALL vehicles (Admin sees all; Customer sees available ones)
    @GetMapping
    public String listVehicles(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() == Role.ADMIN) {
            model.addAttribute("vehicles", vehicleService.getAllVehicles());
        } else {
            model.addAttribute("vehicles", vehicleService.getAvailableVehicles());
        }
        return "vehicles/list";
    }

    // Member 04 — Search vehicles by type, location, and price range
    @GetMapping("/search")
    public String searchVehicles(@RequestParam(required = false) String type,
                                 @RequestParam(required = false) String location,
                                 @RequestParam(required = false) Double minPrice,
                                 @RequestParam(required = false) Double maxPrice,
                                 Model model) {
        List<Vehicle> results = vehicleService.searchVehicles(type, location, minPrice, maxPrice);
        model.addAttribute("vehicles", results);
        model.addAttribute("type",     type);
        model.addAttribute("location", location);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        return "vehicles/search";
    }

    // Show the Add Vehicle form (Admin only)
    @GetMapping("/add")
    public String showAddForm(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/";
        }
        return "vehicles/form";
    }

    // Process the Add Vehicle form (Admin only)
    @PostMapping("/add")
    public String addVehicle(HttpSession session,
                             @RequestParam String type,
                             @RequestParam String brand,
                             @RequestParam String model,
                             @RequestParam double pricePerDay,
                             @RequestParam String location,
                             @RequestParam(required = false, defaultValue = "0")     int    numberOfSeats,
                             @RequestParam(required = false, defaultValue = "false") boolean hasCarrier,
                             @RequestParam(required = false, defaultValue = "0.0")  double cargoCapacity,
                             @RequestParam(required = false, defaultValue = "0")    int    payloadCapacity) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/";
        }

        String id = UUID.randomUUID().toString(); // generate UUID for new vehicle

        // Build the correct subclass based on type (Polymorphism)
        Vehicle vehicle = switch (type.toUpperCase()) {
            case "CAR"   -> new Car  (id, brand, model, pricePerDay, location, numberOfSeats);
            case "BIKE"  -> new Bike (id, brand, model, pricePerDay, location, hasCarrier);
            case "VAN"   -> new Van  (id, brand, model, pricePerDay, location, cargoCapacity);
            case "TRUCK" -> new Truck(id, brand, model, pricePerDay, location, payloadCapacity);
            default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
        };

        vehicleService.addVehicle(vehicle);
        return "redirect:/vehicles?success";
    }

    // Member 01 — Toggle vehicle availability (Admin only)
    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() == Role.ADMIN) {
            Vehicle vehicle = vehicleService.getVehicleById(id);
            if (vehicle != null) {
                vehicleService.updateVehicleStatus(id, !vehicle.isAvailableStatus());
            }
        }
        return "redirect:/vehicles";
    }

    // Delete a vehicle (Admin only)
    @GetMapping("/delete/{id}")
    public String deleteVehicle(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null && user.getRole() == Role.ADMIN) {
            vehicleService.deleteVehicle(id);
        }
        return "redirect:/vehicles";
    }
}
