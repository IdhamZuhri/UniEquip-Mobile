package com.example.uniequip;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminBookingItemAdapter extends RecyclerView.Adapter<AdminBookingItemAdapter.VH> {

    // ✅ FIX: Changed from BookingEquipmentItem to EquipmentSelection
    private final List<EquipmentSelection> list;

    public AdminBookingItemAdapter(List<EquipmentSelection> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse the detail row layout
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_booking_item_detail, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EquipmentSelection item = list.get(position);

        h.tvName.setText(item.name);
        // Ensure EquipmentSelection has category/model fields. If not, remove them from here.
        h.tvDetails.setText("Category: " + item.category + " | Model: " + item.model);
        h.tvQty.setText(item.qty + " units");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvQty;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBorrowItemName);
            tvDetails = itemView.findViewById(R.id.tvBorrowItemDetails);
            tvQty = itemView.findViewById(R.id.tvBorrowItemQty);
        }
    }
}