package com.example.uniequip;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingItemAdapter extends RecyclerView.Adapter<BookingItemAdapter.VH> {

    private final List<EquipmentSelection> list;

    public BookingItemAdapter(List<EquipmentSelection> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // FIX: Use the NEW layout file created above
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_booking_details, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        EquipmentSelection item = list.get(position);

        h.tvName.setText(item.name);

        // Show Category in the badge if you have binding for it, otherwise just leave as is
        // h.tvCategory.setText(item.category);

        h.tvDetails.setText("#" + item.equipmentId + " | Model: " + item.model);
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
            // These IDs now exist in row_booking_item_detail.xml
            tvName = itemView.findViewById(R.id.tvBorrowItemName);
            tvDetails = itemView.findViewById(R.id.tvBorrowItemDetails);
            tvQty = itemView.findViewById(R.id.tvBorrowItemQty);
        }
    }
}