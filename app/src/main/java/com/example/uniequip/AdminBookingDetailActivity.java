package com.example.uniequip;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminBookingDetailActivity extends AppCompatActivity {

    private TextView tvAdminBookingInfo;
    private RecyclerView recyclerAdminBookingItems;
    private Button btnAdminApprove, btnAdminReject, btnAdminBorrowed, btnAdminReturned, btnBack;

    private FirebaseFirestore db;
    private AdminBookingItemAdapter itemAdapter;
    private final ArrayList<EquipmentSelection> items = new ArrayList<>();

    private String bookingId;
    private Timestamp startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_booking_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Bind Views
        tvAdminBookingInfo = findViewById(R.id.tvAdminBookingInfo);
        recyclerAdminBookingItems = findViewById(R.id.recyclerAdminBookingItems);

        btnAdminApprove = findViewById(R.id.btnAdminApprove);
        btnAdminReject = findViewById(R.id.btnAdminReject);
        btnAdminBorrowed = findViewById(R.id.btnAdminBorrowed);
        btnAdminReturned = findViewById(R.id.btnAdminReturned);
        btnBack = findViewById(R.id.btnBack);

        // Get ID
        bookingId = getIntent().getStringExtra("booking_id");
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Error: Invalid Booking ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        recyclerAdminBookingItems.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new AdminBookingItemAdapter(items);
        recyclerAdminBookingItems.setAdapter(itemAdapter);

        loadBookingInfo();
        loadBookingItems();

        // Button Logic
        btnAdminApprove.setOnClickListener(v -> approveBookingWithStockCheck());
        btnAdminReject.setOnClickListener(v -> updateStatus("rejected"));
        btnAdminBorrowed.setOnClickListener(v -> updateStatus("borrowed"));
        btnAdminReturned.setOnClickListener(v -> updateStatus("returned"));
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadBookingInfo() {
        db.collection("bookings").document(bookingId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        tvAdminBookingInfo.setText("Error: Booking not found in database.");
                        return;
                    }
                    startDate = doc.getTimestamp("start_date");
                    endDate = doc.getTimestamp("end_date");

                    String info =
                            "Event: " + safe(doc.getString("event_name")) + "\n" +
                                    "Club: " + safe(doc.getString("club_name")) + "\n" +
                                    "Student ID: " + safe(doc.getString("stud_num")) + "\n\n" +
                                    "Date: " + DateUtils.formatYMD(startDate) + " → " + DateUtils.formatYMD(endDate) + "\n" +
                                    "Current Status: " + safe(doc.getString("status")).toUpperCase();

                    tvAdminBookingInfo.setText(info);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load Info Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadBookingItems() {
        db.collection("bookings").document(bookingId)
                .collection("items")
                .get()
                .addOnSuccessListener(snapshot -> {
                    items.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        items.add(new EquipmentSelection(
                                doc.getString("equipment_id"),
                                doc.getString("name"),
                                doc.getString("category"),
                                doc.getString("model"),
                                doc.getLong("qty") == null ? 0 : doc.getLong("qty").intValue()
                        ));
                    }
                    itemAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load Items Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void updateStatus(String status) {
        // Disable buttons prevents double-clicking
        setButtonsEnabled(false);
        Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();

        db.collection("bookings").document(bookingId)
                .update("status", status)
                .addOnSuccessListener(v -> {
                    setButtonsEnabled(true);
                    Toast.makeText(this, "Updated to " + status.toUpperCase(), Toast.LENGTH_SHORT).show();
                    loadBookingInfo(); // Refresh UI
                })
                // ✅ ADDED FAILURE LISTENER - This will tell you WHY it failed
                .addOnFailureListener(e -> {
                    setButtonsEnabled(true);
                    Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("BookingUpdate", "Error updating status", e);
                });
    }

    private void approveBookingWithStockCheck() {
        btnAdminApprove.setEnabled(false);
        btnAdminApprove.setText("Checking...");

        if (startDate == null || endDate == null) {
            Toast.makeText(this, "Error: Dates not loaded yet", Toast.LENGTH_SHORT).show();
            btnAdminApprove.setEnabled(true);
            return;
        }

        StockLogic.computeReservedMapForRange(
                db,
                startDate,
                endDate,
                bookingId,
                new StockLogic.ReservedMapCallback() {
                    @Override
                    public void onSuccess(HashMap<String, Integer> reservedMap) {
                        db.collection("equipment").get()
                                .addOnSuccessListener(eqSnap -> {
                                    Map<String, Integer> totalMap = new HashMap<>();
                                    for (var doc : eqSnap.getDocuments()) {
                                        Long tq = doc.getLong("totalQty");
                                        totalMap.put(doc.getId(), tq == null ? 0 : tq.intValue());
                                    }

                                    for (EquipmentSelection sel : items) {
                                        int totalQty = totalMap.containsKey(sel.equipmentId) ? totalMap.get(sel.equipmentId) : 0;
                                        int reserved = StockLogic.getReservedFor(reservedMap, sel.equipmentId);
                                        int available = totalQty - reserved;

                                        if (available < sel.qty) {
                                            btnAdminApprove.setEnabled(true);
                                            btnAdminApprove.setText("Approve Request");
                                            Toast.makeText(AdminBookingDetailActivity.this,
                                                    "Stock shortage: " + safe(sel.name) + " (Need " + sel.qty + ", Has " + available + ")",
                                                    Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                    }

                                    // If stock is okay, proceed to approve
                                    db.collection("bookings").document(bookingId)
                                            .update("status", "approved", "approvedAt", Timestamp.now())
                                            .addOnSuccessListener(v -> {
                                                btnAdminApprove.setEnabled(true);
                                                btnAdminApprove.setText("Approve Request");
                                                Toast.makeText(AdminBookingDetailActivity.this, "Approved ✅", Toast.LENGTH_SHORT).show();
                                                loadBookingInfo();
                                            })
                                            .addOnFailureListener(e -> {
                                                btnAdminApprove.setEnabled(true);
                                                btnAdminApprove.setText("Approve Request");
                                                Toast.makeText(AdminBookingDetailActivity.this, "Approve Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            });
                                });
                    }

                    @Override
                    public void onError(String message) {
                        btnAdminApprove.setEnabled(true);
                        btnAdminApprove.setText("Approve Request");
                        Toast.makeText(AdminBookingDetailActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void setButtonsEnabled(boolean enabled) {
        btnAdminApprove.setEnabled(enabled);
        btnAdminReject.setEnabled(enabled);
        btnAdminBorrowed.setEnabled(enabled);
        btnAdminReturned.setEnabled(enabled);
    }

    private String safe(String s) {
        return s == null ? "-" : s;
    }
}