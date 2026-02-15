package com.example.uniequip;

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

public class AdminBookingListAdapter extends RecyclerView.Adapter<AdminBookingListAdapter.VH> {

    public interface OnClick {
        void open(String bookingId);
    }

    private final List<AdminBookingRow> list;
    private final OnClick onClick;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public AdminBookingListAdapter(List<AdminBookingRow> list, OnClick onClick) {
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

        h.tvTitle.setText(r.eventName + " (" + r.clubName + ")");
        h.tvStatus.setText("Status: " + r.status);

        String start = formatTs(r.startDate);
        String end = formatTs(r.endDate);
        h.tvDate.setText("Date: " + start + " → " + end);

        h.itemView.setOnClickListener(v -> onClick.open(r.bookingId));
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
        TextView tvTitle, tvStatus, tvDate;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAdminBookingTitle);
            tvStatus = itemView.findViewById(R.id.tvAdminBookingStatus);
            tvDate = itemView.findViewById(R.id.tvAdminBookingDate);
        }
    }
}
