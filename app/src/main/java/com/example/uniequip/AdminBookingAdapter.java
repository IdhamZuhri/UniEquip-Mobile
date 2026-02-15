package com.example.uniequip;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.VH> {

    public interface OnClick {
        void open(String bookingId);
    }

    private final List<AdminBookingRow> list;
    private final OnClick onClick;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public AdminBookingAdapter(List<AdminBookingRow> list, OnClick onClick) {
        this.list = list;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_admin_booking, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminBookingRow r = list.get(position);

        // Basic Info
        h.tvId.setText("# " + r.bookingId.substring(0, Math.min(r.bookingId.length(), 6)).toUpperCase());
        h.tvTitle.setText(r.eventName);
        h.tvClub.setText(r.clubName);

        // Dates
        String start = formatTs(r.startDate);
        String end = formatTs(r.endDate);
        h.tvDate.setText(start + " - " + end);

        // ✅ DYNAMIC STATUS COLOR LOGIC
        String status = (r.status == null) ? "pending" : r.status.toLowerCase();
        h.tvStatus.setText(status.toUpperCase());
        setStatusColor(h.tvStatus, status);

        // Click Listener
        h.itemView.setOnClickListener(v -> onClick.open(r.bookingId));
    }

    // ✅ Helper to change color based on status text
    private void setStatusColor(TextView tv, String status) {
        String colorCode;
        switch (status) {
            case "approved":
            case "returned":
                colorCode = "#4CAF50"; // Green
                break;
            case "rejected":
                colorCode = "#D32F2F"; // Red
                break;
            case "borrowed":
                colorCode = "#2196F3"; // Blue
                break;
            case "late":
                colorCode = "#C62828"; // Dark Red
                break;
            case "pending":
            default:
                colorCode = "#FFA726"; // Orange
                break;
        }
        // Apply tint to the background shape
        tv.getBackground().setTint(Color.parseColor(colorCode));
    }

    private String formatTs(Timestamp ts) {
        if (ts == null) return "-";
        return df.format(ts.toDate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvId, tvTitle, tvClub, tvStatus, tvDate;

        VH(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvAdminBookingId);
            tvTitle = itemView.findViewById(R.id.tvAdminBookingTitle);
            tvClub = itemView.findViewById(R.id.tvAdminClubName);
            tvStatus = itemView.findViewById(R.id.tvAdminBookingStatus);
            tvDate = itemView.findViewById(R.id.tvAdminBookingDate);
        }
    }
}