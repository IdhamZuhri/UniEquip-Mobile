package com.example.uniequip;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.VH> {

    private Context context;
    private ArrayList<EquipmentItem> list;

    public EquipmentAdapter(Context context, ArrayList<EquipmentItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the new CardView layout
        View v = LayoutInflater.from(context).inflate(R.layout.row_equipment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        EquipmentItem item = list.get(position);

        holder.tvName.setText(item.name);
        holder.tvCategory.setText(item.category);
        holder.tvModel.setText(item.model);

        // Format Qty nicely
        holder.tvQty.setText(item.inStoreQty + " / " + item.totalQty + " Avail");

        // CLICK TO EDIT
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminEditEquipmentActivity.class);
            intent.putExtra("equipment_id", item.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvModel, tvQty;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvEqName);
            tvCategory = itemView.findViewById(R.id.tvEqCategory);
            tvModel = itemView.findViewById(R.id.tvEqModel);
            tvQty = itemView.findViewById(R.id.tvEqQty);
        }
    }
}