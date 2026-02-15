package com.example.uniequip;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EquipmentSelectAdapter extends RecyclerView.Adapter<EquipmentSelectAdapter.ViewHolder> {

    private final List<EquipmentItem> list;
    private final OnSelectionChangedListener listener; // 1. Added Listener Field

    // 2. Defined Interface for the Activity to listen to
    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    // 3. Updated Constructor to accept the listener
    public EquipmentSelectAdapter(List<EquipmentItem> list, OnSelectionChangedListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public ArrayList<EquipmentSelection> getSelectedItems() {
        ArrayList<EquipmentSelection> selected = new ArrayList<>();
        for (EquipmentItem it : list) {
            if (it.selectedQty > 0) {
                selected.add(new EquipmentSelection(it.id, it.name, it.category, it.model, it.selectedQty));
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure you have a layout file named 'row_equipment_select.xml'
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_equipment_select, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        EquipmentItem item = list.get(position);

        h.tvName.setText(item.name == null ? "-" : item.name);
        h.tvCategory.setText(item.category == null ? "-" : item.category);
        h.tvModel.setText(item.model == null ? "-" : item.model);

        // Show Available Stock
        h.tvStock.setText("Available: " + item.availableQty);

        // Set current quantity text (prevent infinite loops with watcher)
        if (h.watcher != null) h.etQty.removeTextChangedListener(h.watcher);
        h.etQty.setText(item.selectedQty > 0 ? String.valueOf(item.selectedQty) : "");

        // Create new watcher
        h.watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String str = s.toString().trim();
                    int val = str.isEmpty() ? 0 : Integer.parseInt(str);

                    if (val < 0) val = 0;

                    if (val > item.availableQty) {
                        h.etQty.setError("Max available: " + item.availableQty);
                        item.selectedQty = item.availableQty; // Optional: Cap it or set to 0
                    } else {
                        item.selectedQty = val;
                    }
                } catch (Exception e) {
                    item.selectedQty = 0;
                }

                // 4. TRIGGER LISTENER to update Activity Summary
                if (listener != null) {
                    listener.onSelectionChanged();
                }
            }
        };

        h.etQty.addTextChangedListener(h.watcher);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvModel, tvStock;
        EditText etQty;
        TextWatcher watcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Make sure these IDs exist in your row_equipment_select.xml
            tvName = itemView.findViewById(R.id.tvEqName);
            tvCategory = itemView.findViewById(R.id.tvEqCategory);
            tvModel = itemView.findViewById(R.id.tvEqModel);
            tvStock = itemView.findViewById(R.id.tvEqStock);
            etQty = itemView.findViewById(R.id.etQty);
        }
    }
}