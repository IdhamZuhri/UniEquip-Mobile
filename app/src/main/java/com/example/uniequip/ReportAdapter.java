package com.example.uniequip;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.VH> {
    private final List<ReportRow> list;

    public ReportAdapter(List<ReportRow> list) {
        this.list = list;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_report_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ReportRow item = list.get(position);
        h.tvName.setText(item.name);
        h.tvLabel.setText("Total Count");
        h.tvCount.setText(String.valueOf(item.count));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvLabel, tvCount;
        VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvReportName);
            tvLabel = v.findViewById(R.id.tvReportLabel);
            tvCount = v.findViewById(R.id.tvReportCount);
        }
    }
}