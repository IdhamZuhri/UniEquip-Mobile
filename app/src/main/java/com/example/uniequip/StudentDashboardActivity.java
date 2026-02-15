package com.example.uniequip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class StudentDashboardActivity extends AppCompatActivity {

    private Button btnMyBookingsCard, btnAddBookingCard, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Hide the default action bar to match your design
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Bind IDs
        btnMyBookingsCard = findViewById(R.id.btnMyBookingsCard);
        btnAddBookingCard = findViewById(R.id.btnAddBookingCard);
        btnLogout = findViewById(R.id.btnLogout);

        // 1. My Bookings Button
        btnMyBookingsCard.setOnClickListener(v ->
                startActivity(new Intent(this, MyBookingsActivity.class))
        );

        // 2. Add Booking Button
        // Make sure you have a file named 'StartBookingActivity.java'
        // If your file is named 'CreateBookingActivity', change the class name below.
        btnAddBookingCard.setOnClickListener(v ->
                startActivity(new Intent(this, StartBookingActivity.class))
        );

        // 3. Logout Button
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class);
            // Clear back stack so user can't press back to return to dashboard
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }
}