package com.example.uniequip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;

public class AdminBookingListActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private Spinner spFilterStatus;
    private Button btnBack;

    private FirebaseFirestore db;
    private AdminBookingAdapter adapter;

    private final ArrayList<AdminBookingRow> allList = new ArrayList<>();
    private final ArrayList<AdminBookingRow> shownList = new ArrayList<>();

    private final String[] filterOptions = new String[]{
            "All", "pending", "approved", "borrowed", "returned", "rejected", "late"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_booking_list);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recycler = findViewById(R.id.recyclerAdminBookings);
        spFilterStatus = findViewById(R.id.spFilterStatus);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();

        ArrayAdapter<String> spAd = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, filterOptions
        );
        spFilterStatus.setAdapter(spAd);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter with shownList
        adapter = new AdminBookingAdapter(shownList, bookingId -> {
            Intent i = new Intent(this, AdminBookingDetailActivity.class);
            i.putExtra("booking_id", bookingId);
            startActivity(i);
        });
        recycler.setAdapter(adapter);

        loadBookings();

        spFilterStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> p, android.view.View v, int pos, long id) {
                applyFilter(filterOptions[pos]);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadBookings() {
        db.collection("bookings")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    allList.clear();

                    for (var doc : snap.getDocuments()) {
                        String id = doc.getId();
                        String club = doc.getString("club_name");
                        String event = doc.getString("event_name");
                        String status = doc.getString("status");

                        // ✅ FIX: Get the Student Number
                        String studNum = doc.getString("stud_num");

                        Timestamp start = doc.getTimestamp("start_date");
                        Timestamp end = doc.getTimestamp("end_date");

                        // ✅ FIX: Pass all 7 arguments to match AdminBookingRow constructor
                        allList.add(new AdminBookingRow(
                                id,
                                club,
                                event,
                                status,
                                start,
                                end,
                                studNum
                        ));
                    }

                    String selected = spFilterStatus.getSelectedItem() == null ? "All" : spFilterStatus.getSelectedItem().toString();
                    applyFilter(selected);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed load: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void applyFilter(String filter) {
        shownList.clear();
        for (AdminBookingRow b : allList) {
            String displayStatus = getDisplayStatus(b);

            boolean isMatch = false;

            // 1. Show everything if 'All' is selected
            if ("All".equalsIgnoreCase(filter)) {
                isMatch = true;
            }
            // 2. Exact match (e.g. Pending == Pending)
            else if (displayStatus.equalsIgnoreCase(filter)) {
                isMatch = true;
            }
            // 3. ✅ FIX: If filtering 'Borrowed', also show 'Late' items
            // (Because 'Late' items are still technically borrowed/out)
            else if ("borrowed".equalsIgnoreCase(filter) && "late".equalsIgnoreCase(displayStatus)) {
                isMatch = true;
            }

            if (isMatch) {
                shownList.add(b);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private String getDisplayStatus(AdminBookingRow b) {
        if (b == null) return "pending";
        String s = (b.status == null) ? "pending" : b.status;
        if ("borrowed".equalsIgnoreCase(s) && b.endDate != null) {
            Timestamp now = new Timestamp(Calendar.getInstance().getTime());
            if (now.compareTo(b.endDate) > 0) return "late";
        }
        return s;
    }
}