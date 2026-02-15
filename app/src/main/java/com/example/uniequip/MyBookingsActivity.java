package com.example.uniequip;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MyBookingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyBookingsAdapter adapter;
    private ArrayList<BookingModel> bookingsList = new ArrayList<>();

    // Removed btnCreateNew
    private Button btnBackDashboard;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        recyclerView = findViewById(R.id.recyclerMyBookings);

        // Removed findViewById for btnCreateNew
        btnBackDashboard = findViewById(R.id.btnBackDashboard);

        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyBookingsAdapter(this, bookingsList);
        recyclerView.setAdapter(adapter);

        loadMyBookings();

        // Footer Buttons
        // Removed setOnClickListener for btnCreateNew

        btnBackDashboard.setOnClickListener(v -> finish());
    }

    private void loadMyBookings() {
        String studNum = SessionManager.getStudentNumber(this);

        bookingsList.clear();
        adapter.notifyDataSetChanged();

        db.collection("bookings")
                .whereEqualTo("stud_num", studNum)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        BookingModel b = new BookingModel();
                        b.id = doc.getId();
                        b.eventName = doc.getString("event_name");
                        b.clubName = doc.getString("club_name");
                        b.status = doc.getString("status");

                        // Handle Dates safely
                        Timestamp start = doc.getTimestamp("start_date");
                        Timestamp end = doc.getTimestamp("end_date");
                        b.dateRange = formatDateRange(start, end);

                        bookingsList.add(b);
                    }
                    adapter.notifyDataSetChanged();

                    if (bookingsList.isEmpty()) {
                        Toast.makeText(this, "No bookings found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String formatDateRange(Timestamp start, Timestamp end) {
        if (start == null || end == null) return "Date N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        return sdf.format(start.toDate()) + " - " + sdf.format(end.toDate());
    }

    // --- INNER MODEL CLASS ---
    public static class BookingModel {
        String id, eventName, clubName, status, dateRange;
    }

    // --- INNER ADAPTER CLASS ---
    public static class MyBookingsAdapter extends RecyclerView.Adapter<MyBookingsAdapter.ViewHolder> {

        Context context;
        ArrayList<BookingModel> list;

        public MyBookingsAdapter(Context context, ArrayList<BookingModel> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.row_booking_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BookingModel item = list.get(position);

            holder.tvId.setText("# " + item.id.toUpperCase().substring(0, Math.min(item.id.length(), 6)));
            holder.tvEvent.setText(item.eventName);
            holder.tvClub.setText(item.clubName);
            holder.tvDate.setText(item.dateRange);

            // Status Styling
            String status = item.status == null ? "PENDING" : item.status.toUpperCase();
            holder.tvStatus.setText(status);

            if (status.contains("APPROVED")) {
                holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
            } else if (status.contains("REJECTED")) {
                holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336"))); // Red
            } else {
                holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA726"))); // Orange
            }

            // Click listener for details
            holder.btnDetails.setOnClickListener(v -> {
                Intent i = new Intent(context, BookingDetailActivity.class);
                i.putExtra("booking_id", item.id);
                context.startActivity(i);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvId, tvStatus, tvEvent, tvDate, tvClub;
            Button btnDetails;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.tvBookingId);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvEvent = itemView.findViewById(R.id.tvEventName);
                tvDate = itemView.findViewById(R.id.tvDateRange);
                tvClub = itemView.findViewById(R.id.tvClubName);
                btnDetails = itemView.findViewById(R.id.btnViewDetails);
            }
        }
    }
}