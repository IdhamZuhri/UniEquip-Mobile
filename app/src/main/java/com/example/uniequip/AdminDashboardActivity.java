package com.example.uniequip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnAddEquipment, btnEquipmentList;
    private Button btnRegisterClub, btnViewClubs;
    private Button btnManageBookings, btnReport;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Bind Views
        btnAddEquipment = findViewById(R.id.btnAddEquipment);
        btnEquipmentList = findViewById(R.id.btnEquipmentList);

        btnRegisterClub = findViewById(R.id.btnRegisterClub);
        btnViewClubs = findViewById(R.id.btnViewClubs);

        btnManageBookings = findViewById(R.id.btnManageBookings);
        btnReport = findViewById(R.id.btnReport);

        btnLogout = findViewById(R.id.btnLogout);

        // --- Equipment Actions ---
        btnAddEquipment.setOnClickListener(v ->
                startActivity(new Intent(this, CreateEquipmentActivity.class)));

        btnEquipmentList.setOnClickListener(v ->
                startActivity(new Intent(this, EquipmentListActivity.class)));

        // --- Club Actions ---
        btnRegisterClub.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterClubActivity.class)));

        btnViewClubs.setOnClickListener(v ->
                startActivity(new Intent(this, ClubListActivity.class)));

        // --- Booking & Report Actions ---
        btnManageBookings.setOnClickListener(v ->
                startActivity(new Intent(this, AdminBookingListActivity.class)));

        btnReport.setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class))
        );

        // --- Logout Action ---
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class);
            // Clear back stack to prevent going back to dashboard
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}