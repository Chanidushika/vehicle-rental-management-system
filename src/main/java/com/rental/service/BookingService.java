package com.rental.service;

import com.rental.model.*;
import com.rental.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import com.rental.service.FileService;

// ============================================================
// BookingService — Business logic for rentals.
// Member 03: Calculate delay/late charges when returning late.
// Member 05: Uses polymorphic calculateRentalPrice() and
//            calculateLateFee() from the vehicle model.
// ============================================================
@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private FileService fileService;

    // Create a new booking (rent a vehicle)
    public Booking bookVehicle(User user, String vehicleId, LocalDate startDate, LocalDate endDate) {
        Vehicle vehicle = vehicleService.getVehicleById(vehicleId);

        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found.");
        }
        if (!vehicle.isAvailableStatus()) {
            throw new IllegalArgumentException("Sorry, this vehicle is already rented.");
        }

        // Calculate number of rental days (minimum 1 day)
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) days = 1;

        // Member 05 — Polymorphic price calculation
        // Each vehicle type (Car/Bike/Van/Truck) calculates differently
        double totalCost = vehicle.calculateRentalPrice((int) days);

        // Create the booking record
        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                vehicleId,
                user.getId(),
                startDate,
                endDate,
                totalCost
        );

        bookingRepository.save(booking);

        // Append to payments.txt (Member 05)
        String paymentId = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().toString();
        String paymentRecord = paymentId + "," + booking.getId() + "," + user.getId() + "," + totalCost + "," + timestamp;
        fileService.appendLine("payments.txt", paymentRecord);

        // Member 01 — Mark vehicle as NOT available (it's now rented)
        vehicleService.updateVehicleStatus(vehicleId, false);

        return booking;
    }

    // Process a vehicle return — calculate late fees if returned late
    public Booking returnVehicle(String bookingId, LocalDate returnDate) {
        Optional<Booking> opt = bookingRepository.findById(bookingId);

        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Booking not found.");
        }

        Booking booking = opt.get();

        if (booking.getReturnDate() != null) {
            throw new IllegalArgumentException("This vehicle has already been returned.");
        }

        booking.setReturnDate(returnDate);

        // Member 03 — Delay charge calculation
        long lateDays = ChronoUnit.DAYS.between(booking.getEndDate(), returnDate);
        if (lateDays > 0) {
            // Get the vehicle to call its polymorphic late fee method
            Vehicle vehicle = vehicleService.getVehicleById(booking.getVehicleId());
            if (vehicle != null) {
                // Member 05 — calculateLateFee() is polymorphic per vehicle type
                double lateFee = vehicle.calculateLateFee((int) lateDays);
                booking.setLateFee(lateFee);
            }
        }

        booking.setPaymentStatus(PaymentStatus.PAID);
        bookingRepository.save(booking); // update the booking record in file

        // Append late fee to payments.txt if applicable (Member 05)
        if (booking.getLateFee() > 0) {
            String paymentId = UUID.randomUUID().toString();
            String timestamp = LocalDateTime.now().toString();
            String paymentRecord = paymentId + "," + booking.getId() + "," + booking.getUserId() + "," + booking.getLateFee() + "," + timestamp;
            fileService.appendLine("payments.txt", paymentRecord);
        }

        // Member 01 — Mark vehicle as available again after return
        vehicleService.updateVehicleStatus(booking.getVehicleId(), true);

        return booking;
    }

    // Get all bookings (for Admin dashboard — Member 06)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Get bookings for a specific customer
    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserId(user.getId());
    }

    // Get a single booking by ID
    public Booking getBookingById(String id) {
        return bookingRepository.findById(id).orElse(null);
    }
}
