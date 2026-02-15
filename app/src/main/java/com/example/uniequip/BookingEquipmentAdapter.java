package com.example.uniequip;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookingEquipmentAdapter extends RecyclerView.Adapter<BookingEquipmentAdapter.ViewHolder> {

    private final List<BookingEquipmentItem> list;

    public BookingEquipmentAdapter(List<BookingEquipmentItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Uses the detail row layout created in the previous step
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_booking_item_detail, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        BookingEquipmentItem item = list.get(position);
        h.tvName.setText(item.name);
        h.tvDetails.setText("Category: " + item.category + " | Model: " + item.model);
        h.tvQty.setText(item.qty + " units");
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails, tvQty;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBorrowItemName);
            tvDetails = itemView.findViewById(R.id.tvBorrowItemDetails);
            tvQty = itemView.findViewById(R.id.tvBorrowItemQty);
        }
    }
}