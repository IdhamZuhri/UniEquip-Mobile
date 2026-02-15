package com.example.uniequip;

import com.google.firebase.Timestamp;

public class AdminBookingRow {
    public String bookingId;
    public String clubName;
    public String eventName;
    public String status;
    public Timestamp startDate;
    public Timestamp endDate;
    public String studNum; // Useful for admin to see who booked it

    // Empty constructor for Firebase
    public AdminBookingRow() {}

    public AdminBookingRow(String bookingId, String clubName, String eventName,
                           String status, Timestamp startDate, Timestamp endDate, String studNum) {
        this.bookingId = bookingId;
        this.clubName = clubName;
        this.eventName = eventName;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.studNum = studNum;
    }
}