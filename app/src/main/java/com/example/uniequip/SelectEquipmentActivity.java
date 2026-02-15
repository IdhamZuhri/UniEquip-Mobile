package com.example.uniequip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SelectEquipmentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvSummary;
    private Button btnConfirmBooking, btnBack;

    private FirebaseFirestore db;
    private EquipmentSelectAdapter adapter;
    private final ArrayList<EquipmentItem> items = new ArrayList<>();

    private String eventName, clubName;
    private long startDateMillis, endDateMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_equipment);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 1. Get Data
        eventName = getIntent().getStringExtra("event_name");
        clubName = getIntent().getStringExtra("club_name");
        startDateMillis = getIntent().getLongExtra("start_date", 0);
        endDateMillis = getIntent().getLongExtra("end_date", 0);

        // Validation
        if (startDateMillis == 0 || endDateMillis == 0) {
            Toast.makeText(this, "Error: Invalid Dates.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2. Bind Views
        recyclerView = findViewById(R.id.recyclerSelectEquipment);
        tvSummary = findViewById(R.id.tvSelectionSummary);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();

        // 3. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EquipmentSelectAdapter(items, () -> {
            int count = adapter.getSelectedItems().size();
            tvSummary.setText(count + " items selected");
        });
        recyclerView.setAdapter(adapter);

        // 4. Load Data with Stock Check
        loadEquipmentWithAvailability();

        // 5. Buttons
        btnConfirmBooking.setOnClickListener(v -> submitBooking());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadEquipmentWithAvailability() {
        // ✅ CONVERT long to Timestamp for StockLogic
        Timestamp startTs = new Timestamp(new Date(startDateMillis));
        Timestamp endTs = new Timestamp(new Date(endDateMillis));

        // 1. Calculate Reserved Items first
        // We pass 'null' for ignoreBookingId because this is a NEW booking
        StockLogic.computeReservedMapForRange(db, startTs, endTs, null, new StockLogic.ReservedMapCallback() {
            @Override
            public void onSuccess(HashMap<String, Integer> reservedMap) {

                // 2. Fetch All Equipment
                db.collection("equipment").get().addOnSuccessListener(snap -> {
                    items.clear();

                    for (QueryDocumentSnapshot doc : snap) {
                        String status = doc.getString("status");
                        // Skip inactive items
                        if (status != null && status.equalsIgnoreCase("inactive")) continue;

                        Long totalQtyLong = doc.getLong("totalQty");
                        int totalQty = (totalQtyLong == null) ? 0 : totalQtyLong.intValue();

                        // 3. MATH: Available = Total - Reserved
                        String eqId = doc.getId();
                        int reservedQty = StockLogic.getReservedFor(reservedMap, eqId);
                        int realAvailable = totalQty - reservedQty;

                        if (realAvailable < 0) realAvailable = 0;

                        EquipmentItem item = new EquipmentItem(
                                eqId,
                                doc.getString("name"),
                                doc.getString("category"),
                                doc.getString("model"),
                                totalQty,
                                null
                        );

                        // ✅ Set the calculated availability
                        item.availableQty = realAvailable;

                        items.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SelectEquipmentActivity.this, "Stock Check Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitBooking() {
        ArrayList<EquipmentSelection> selectedItems = adapter.getSelectedItems();

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Please select at least 1 item", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmBooking.setEnabled(false);
        String currentStudentId = SessionManager.getStudentNumber(this);

        if (currentStudentId.equals("000000")) {
            Toast.makeText(this, "Session Invalid. Please Login Again.", Toast.LENGTH_SHORT).show();
            btnConfirmBooking.setEnabled(true);
            return;
        }

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("event_name", eventName);
        bookingData.put("club_name", clubName);
        bookingData.put("start_date", new Timestamp(new Date(startDateMillis)));
        bookingData.put("end_date", new Timestamp(new Date(endDateMillis)));
        bookingData.put("status", "pending");
        bookingData.put("createdAt", Timestamp.now());
        bookingData.put("stud_num", currentStudentId);

        db.collection("bookings")
                .add(bookingData)
                .addOnSuccessListener(docRef -> saveBookingItems(docRef.getId(), selectedItems))
                .addOnFailureListener(e -> {
                    btnConfirmBooking.setEnabled(true);
                    Toast.makeText(this, "Booking Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveBookingItems(String bookingId, ArrayList<EquipmentSelection> selectedItems) {
        var batch = db.batch();

        for (EquipmentSelection sel : selectedItems) {
            var ref = db.collection("bookings").document(bookingId).collection("items").document();
            batch.set(ref, sel.toMap());
        }

        batch.commit()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Booking Submitted Successfully! ✅", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SelectEquipmentActivity.this, StudentDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnConfirmBooking.setEnabled(true);
                    Toast.makeText(this, "Error saving items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}