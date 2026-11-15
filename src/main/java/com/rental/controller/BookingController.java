package com.rental.controller;

import com.rental.model.*;
import com.rental.service.BookingService;
import com.rental.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// ============================================================
// BookingController — Create bookings and process returns.
// Member 03: Return vehicle page shows delay charges if late.
// ============================================================
@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleService vehicleService;

    // Show the booking form for a specific vehicle
    @GetMapping("/new/{vehicleId}")
    public String showBookingForm(@PathVariable String vehicleId,
                                  HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
        if (vehicle == null || !vehicle.isAvailableStatus()) {
            return "redirect:/vehicles?unavailable";
        }

        model.addAttribute("vehicle", vehicle);
        return "bookings/new";
    }

    // Process the booking form — create the rental
    @PostMapping("/new")
    public String createBooking(@RequestParam String vehicleId,
                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        try {
            bookingService.bookVehicle(user, vehicleId, startDate, endDate);
            return "redirect:/bookings/history?success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("vehicle", vehicleService.getVehicleById(vehicleId));
            return "bookings/new";
        }
    }

    // Show booking history — Admin sees all, Customer sees their own
    @GetMapping("/history")
    public String viewHistory(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/users/login";

        List<Booking> bookings;
        if (user.getRole() == Role.ADMIN) {
            bookings = bookingService.getAllBookings(); // Admin sees everyone's bookings
        } else {
            bookings = bookingService.getUserBookings(user); // Customer sees only theirs
        }

        // Enrich each booking with its Vehicle object for the template to display
        model.addAttribute("bookings", bookings);
        model.addAttribute("vehicleService", vehicleService); // template uses this to look up vehicle
        return "bookings/history";
    }

    // Member 03 — Admin processes a vehicle return and sees delay charges
    @GetMapping("/return/{id}")
    public String returnVehicle(@PathVariable String id,
                                HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.ADMIN) {
            return "redirect:/";
        }

        try {
            // Use today's date as the actual return date
            Booking booking = bookingService.returnVehicle(id, LocalDate.now());

            // Get the vehicle object to display details on the summary page
            Vehicle vehicle = vehicleService.getVehicleById(booking.getVehicleId());

            model.addAttribute("booking", booking);
            model.addAttribute("vehicle", vehicle);
            return "bookings/summary"; // Show summary with any late fees
        } catch (IllegalArgumentException e) {
            return "redirect:/bookings/history?error=" + e.getMessage();
        }
    }
}
