package com.example.uniequip;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BookingDetailActivity extends AppCompatActivity {

    private TextView tvClub, tvEvent, tvDate, tvStatus;
    private RecyclerView recyclerEquipment;
    private Button btnBack;

    private FirebaseFirestore db;

    private BookingEquipmentAdapter adapter;
    private final ArrayList<BookingEquipmentItem> equipmentList = new ArrayList<>();

    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details); // ✅ MUST MATCH YOUR FILE NAME

        tvClub = findViewById(R.id.tvBookingClub);
        tvEvent = findViewById(R.id.tvBookingEvent);
        tvDate = findViewById(R.id.tvBookingDate);
        tvStatus = findViewById(R.id.tvBookingStatus);

        recyclerEquipment = findViewById(R.id.recyclerBookingEquipment);
        btnBack = findViewById(R.id.btnBackBookingDetail);

        bookingId = getIntent().getStringExtra("booking_id");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            Toast.makeText(this, "Missing booking_id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        recyclerEquipment.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingEquipmentAdapter(equipmentList);
        recyclerEquipment.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadBookingHeader();
        loadBookingItems();  // ✅ this is the important part
    }

    private void loadBookingHeader() {
        db.collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String club = doc.getString("club_name");
                    String event = doc.getString("event_name");
                    String status = doc.getString("status");

                    Timestamp start = doc.getTimestamp("start_date");
                    Timestamp end = doc.getTimestamp("end_date");

                    tvClub.setText("Club: " + (club == null ? "-" : club));
                    tvEvent.setText("Event: " + (event == null ? "-" : event));
                    tvStatus.setText("Status: " + (status == null ? "-" : status));

                    String dateText = formatTs(start) + " → " + formatTs(end);
                    tvDate.setText("Date: " + dateText);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load booking", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadBookingItems() {
        // ✅ MUST MATCH ADMIN: bookings/{id}/items
        db.collection("bookings")
                .document(bookingId)
                .collection("items")
                .get()
                .addOnSuccessListener(snapshot -> {
                    equipmentList.clear();

                    snapshot.forEach(doc -> {
                        // ✅ MUST MATCH YOUR FIRESTORE FIELD NAMES
                        String name = doc.getString("name");
                        String category = doc.getString("category");
                        String model = doc.getString("model");

                        Long qtyLong = doc.getLong("qty");
                        int qty = qtyLong == null ? 0 : qtyLong.intValue();

                        equipmentList.add(new BookingEquipmentItem(
                                name == null ? "-" : name,
                                category == null ? "-" : category,
                                model == null ? "-" : model,
                                qty
                        ));
                    });

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load equipment list", Toast.LENGTH_SHORT).show()
                );
    }

    private String formatTs(Timestamp ts) {
        if (ts == null) return "-";
        Date d = ts.toDate();
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d);
    }
}
