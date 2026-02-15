package com.example.uniequip;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ClubAdapter extends RecyclerView.Adapter<ClubAdapter.ViewHolder> {

    // 1. Updated Interface
    public interface OnClubActionListener {
        void onDelete(String clubId, int position);
        void onEdit(ClubItem item); // New Edit Method
    }

    private final ArrayList<ClubItem> list;
    private final OnClubActionListener listener;

    public ClubAdapter(ArrayList<ClubItem> list, OnClubActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_club, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ClubItem item = list.get(position);

        h.tvName.setText(item.clubName);
        h.tvType.setText("Type: " + item.type);
        h.tvAdvisor.setText("Advisor: " + item.advName + " (" + item.advNum + ")");
        h.tvTel.setText("Tel: " + item.advTel);
        h.tvEmail.setText("Email: " + item.advEmail);

        // Delete Click
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item.id, h.getAdapterPosition()));

        // Edit Click
        h.btnEdit.setOnClickListener(v -> listener.onEdit(item));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvAdvisor, tvTel, tvEmail;
        ImageView btnDelete, btnEdit;

        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvClubName);
            tvType = v.findViewById(R.id.tvClubType);
            tvAdvisor = v.findViewById(R.id.tvClubAdvisor);
            tvTel = v.findViewById(R.id.tvClubTel);
            tvEmail = v.findViewById(R.id.tvClubEmail);
            btnDelete = v.findViewById(R.id.btnDeleteClub);
            btnEdit = v.findViewById(R.id.btnEditClub);
        }
    }
}