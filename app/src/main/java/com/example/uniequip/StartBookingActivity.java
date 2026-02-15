package com.example.uniequip;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StartBookingActivity extends AppCompatActivity {

    private Spinner spClub, spClubType;
    private EditText etEventName, etStartDate, etEndDate;
    private Button btnNext, btnBack;

    private FirebaseFirestore db;
    private final ArrayList<String> clubNames = new ArrayList<>();
    private ArrayAdapter<String> clubAdapter;

    // Date formatter to convert UI string to Data
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_booking);

        // Bind Views
        spClub = findViewById(R.id.spClub);
        spClubType = findViewById(R.id.spClubType);
        etEventName = findViewById(R.id.etEventName);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        btnNext = findViewById(R.id.btnNextSelectEquipment);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();

        // 1. Setup Spinners
        String[] types = {"-- Select Club Type --", "Open", "Closed"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spClubType.setAdapter(typeAdapter);

        clubNames.clear();
        clubNames.add("-- Select Club Name --");
        clubAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clubNames);
        spClub.setAdapter(clubAdapter);

        loadClubsFromFirestore();

        // 2. Setup Date Pickers
        setupDatePicker(etStartDate, true);
        setupDatePicker(etEndDate, false);

        // 3. Button Logic
        btnNext.setOnClickListener(v -> goSelectEquipment());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadClubsFromFirestore() {
        db.collection("clubs")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snapshot -> {
                    clubNames.clear();
                    clubNames.add("-- Select Club Name --"); // Keep default at top
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String clubName = doc.getString("club_name");
                        if (clubName != null && !clubName.trim().isEmpty()) {
                            clubNames.add(clubName.trim());
                        }
                    }
                    clubAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load clubs", Toast.LENGTH_SHORT).show()
                );
    }

    private void setupDatePicker(EditText et, boolean isStart) {
        et.setFocusable(false);
        et.setClickable(true);

        et.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    StartBookingActivity.this,
                    (view, y, m, d) -> {
                        // Format: 2026-01-25
                        String val = String.format(Locale.US, "%04d-%02d-%02d", y, (m + 1), d);
                        et.setText(val);

                        // Auto-adjust end date if needed
                        if (isStart) {
                            String end = etEndDate.getText().toString().trim();
                            if (!end.isEmpty() && end.compareTo(val) < 0) {
                                etEndDate.setText(val);
                            }
                        }
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });
    }

    private void goSelectEquipment() {
        String club = spClub.getSelectedItem().toString();
        String eventName = etEventName.getText().toString().trim();
        String startDateStr = etStartDate.getText().toString().trim();
        String endDateStr = etEndDate.getText().toString().trim();

        // Validation
        if (club.contains("-- Select")) {
            Toast.makeText(this, "Please select a club", Toast.LENGTH_SHORT).show();
            return;
        }
        if (eventName.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
            Toast.makeText(this, "Please fill event name and dates", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endDateStr.compareTo(startDateStr) < 0) {
            Toast.makeText(this, "End date cannot be earlier than start date", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // ✅ CONVERT STRING DATES TO LONG (MILLISECONDS)
            Date dateStart = sdf.parse(startDateStr);
            Date dateEnd = sdf.parse(endDateStr);

            if (dateStart == null || dateEnd == null) throw new ParseException("", 0);

            Intent i = new Intent(this, SelectEquipmentActivity.class);
            i.putExtra("club_name", club);
            i.putExtra("event_name", eventName);

            // ✅ Pass as Long (fixes 1970 issue)
            i.putExtra("start_date", dateStart.getTime());
            i.putExtra("end_date", dateEnd.getTime());

            startActivity(i);

        } catch (ParseException e) {
            Toast.makeText(this, "Error parsing dates", Toast.LENGTH_SHORT).show();
        }
    }
}