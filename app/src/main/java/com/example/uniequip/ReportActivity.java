package com.example.uniequip;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private Spinner spReportType, spMonth, spYear;
    private Button btnGenerate, btnBack; // Added btnBack
    private TextView tvHint;
    private RecyclerView recycler;

    private FirebaseFirestore db;
    private final ArrayList<ReportRow> rows = new ArrayList<>();
    private ReportAdapter adapter;

    private final String[] reportTypes = new String[] {
            "Most Booked Equipment",
            "Most Active Clubs"
    };

    private final String[] months = new String[] {
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Hide default Header
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // Bind Views
        spReportType = findViewById(R.id.spReportType);
        spMonth = findViewById(R.id.spMonth);
        spYear = findViewById(R.id.spYear);
        btnGenerate = findViewById(R.id.btnGenerateReport);
        btnBack = findViewById(R.id.btnBack); // Bind Back Button
        tvHint = findViewById(R.id.tvReportHint);
        recycler = findViewById(R.id.recyclerReport);

        db = FirebaseFirestore.getInstance();

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportAdapter(rows);
        recycler.setAdapter(adapter);

        setupSpinners();

        btnGenerate.setOnClickListener(v -> generate());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        spReportType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reportTypes));
        spMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<String> years = new ArrayList<>();
        for (int y = currentYear; y >= currentYear - 5; y--) years.add(String.valueOf(y));

        spYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));

        // Default to current month/year
        spMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));
        spYear.setSelection(0);
    }

    private void generate() {
        int reportIndex = spReportType.getSelectedItemPosition();
        int monthIndex = spMonth.getSelectedItemPosition();
        int year = Integer.parseInt(spYear.getSelectedItem().toString());

        Timestamp[] range = getMonthRange(year, monthIndex);
        Timestamp start = range[0];
        Timestamp endExclusive = range[1];

        rows.clear();
        adapter.notifyDataSetChanged();

        if (reportIndex == 0) {
            tvHint.setText("Generating: Most Booked Equipment (" + months[monthIndex] + " " + year + ")");
            reportMostBookedEquipment(start, endExclusive);
        } else {
            tvHint.setText("Generating: Most Active Clubs (" + months[monthIndex] + " " + year + ")");
            reportMostBookingsByClub(start, endExclusive);
        }
    }

    private void reportMostBookedEquipment(Timestamp start, Timestamp endExclusive) {
        ArrayList<String> statuses = new ArrayList<>();
        statuses.add("approved");
        statuses.add("borrowed");
        statuses.add("returned");
        statuses.add("late");

        db.collection("bookings")
                .whereGreaterThanOrEqualTo("start_date", start)
                .whereLessThan("start_date", endExclusive)
                .orderBy("start_date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    HashMap<String, Integer> sumMap = new HashMap<>();
                    ArrayList<DocumentSnapshot> bookings = new ArrayList<>(snap.getDocuments());

                    if (bookings.isEmpty()) {
                        tvHint.setText("No bookings found for this period.");
                        return;
                    }
                    loadItemsForEquipmentReport(bookings, 0, statuses, sumMap);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadItemsForEquipmentReport(ArrayList<DocumentSnapshot> bookings, int idx, ArrayList<String> statuses, HashMap<String, Integer> sumMap) {
        if (idx >= bookings.size()) {
            showSortedMap(sumMap);
            return;
        }

        DocumentSnapshot b = bookings.get(idx);
        String status = b.getString("status");

        if (status == null || !statuses.contains(status)) {
            loadItemsForEquipmentReport(bookings, idx + 1, statuses, sumMap);
            return;
        }

        db.collection("bookings").document(b.getId()).collection("items").get()
                .addOnSuccessListener(itemSnap -> {
                    for (DocumentSnapshot it : itemSnap.getDocuments()) {
                        String eqName = it.getString("name");
                        Long q = it.getLong("qty");
                        int qty = q == null ? 0 : q.intValue();
                        String key = (eqName != null) ? eqName : "Unknown";
                        sumMap.put(key, sumMap.getOrDefault(key, 0) + qty);
                    }
                    loadItemsForEquipmentReport(bookings, idx + 1, statuses, sumMap);
                });
    }

    private void reportMostBookingsByClub(Timestamp start, Timestamp endExclusive) {
        db.collection("bookings")
                .whereGreaterThanOrEqualTo("start_date", start)
                .whereLessThan("start_date", endExclusive)
                .orderBy("start_date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    HashMap<String, Integer> countMap = new HashMap<>();
                    for (DocumentSnapshot b : snap.getDocuments()) {
                        String status = b.getString("status");
                        if ("rejected".equals(status)) continue;

                        String club = b.getString("club_name");
                        String key = (club == null) ? "Unknown Club" : club;
                        countMap.put(key, countMap.getOrDefault(key, 0) + 1);
                    }
                    showSortedMap(countMap);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showSortedMap(HashMap<String, Integer> map) {
        rows.clear();
        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, (a, b) -> Integer.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Integer> e : list) {
            rows.add(new ReportRow(e.getKey(), e.getValue()));
        }

        if (rows.isEmpty()) tvHint.setText("No data found for this period.");
        else tvHint.setText("Report Generated Successfully");

        adapter.notifyDataSetChanged();
    }

    private Timestamp[] getMonthRange(int year, int monthZeroBased) {
        Calendar c1 = Calendar.getInstance();
        c1.set(year, monthZeroBased, 1, 0, 0, 0);
        c1.set(Calendar.MILLISECOND, 0);

        Calendar c2 = (Calendar) c1.clone();
        c2.add(Calendar.MONTH, 1);

        return new Timestamp[]{new Timestamp(c1.getTime()), new Timestamp(c2.getTime())};
    }
}