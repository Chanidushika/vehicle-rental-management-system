package com.rental.model;

import java.time.LocalDate;

// ============================================================
// Booking — Stores a single rental transaction.
// We do NOT store full Vehicle/User objects here —
// we only store their IDs (vehicleId, userId) as Strings.
// This is how file-based storage works (no JPA joins).
//
// CSV format in bookings.txt:
//   id,vehicleId,userId,startDate,endDate,returnDate,totalCost,lateFee,paymentStatus
// ============================================================
public class Booking {

    private String id;
    private String vehicleId;
    private String userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate returnDate;  // null until the vehicle is returned
    private double totalCost;
    private double lateFee;
    private PaymentStatus paymentStatus;

    // Default constructor
    public Booking() {}

    // Constructor used when creating a new booking
    public Booking(String id, String vehicleId, String userId,
                   LocalDate startDate, LocalDate endDate, double totalCost) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
        this.lateFee = 0.0;
        this.paymentStatus = PaymentStatus.PENDING; // new bookings start as PENDING
    }

    // ---- CSV Serialization ----

    // Convert booking to one CSV line for the text file
    public String toCsv() {
        // Use "NULL" string when returnDate is not set yet
        String returnDateStr = (returnDate != null) ? returnDate.toString() : "NULL";
        return id + "," + vehicleId + "," + userId + "," + startDate + ","
                + endDate + "," + returnDateStr + "," + totalCost + ","
                + lateFee + "," + paymentStatus.name();
    }

    // Build a Booking object from a CSV line
    public static Booking fromCsv(String csvLine) {
        String[] p = csvLine.split(",");
        if (p.length < 9) return null; // skip bad/incomplete lines

        Booking b = new Booking();
        b.id            = p[0];
        b.vehicleId     = p[1];
        b.userId        = p[2];
        b.startDate     = LocalDate.parse(p[3]);
        b.endDate       = LocalDate.parse(p[4]);
        b.returnDate    = p[5].equals("NULL") ? null : LocalDate.parse(p[5]);
        b.totalCost     = Double.parseDouble(p[6]);
        b.lateFee       = Double.parseDouble(p[7]);
        b.paymentStatus = PaymentStatus.valueOf(p[8]);
        return b;
    }

    // ---------- Getters & Setters ----------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getLateFee() { return lateFee; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
}
