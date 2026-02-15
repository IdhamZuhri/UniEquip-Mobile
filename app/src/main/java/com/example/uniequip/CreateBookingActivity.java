package com.example.uniequip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CreateBookingActivity extends AppCompatActivity {

    private EditText etEventName, etStartDate, etEndDate;
    private Spinner spClubName;
    private Button btnNext;

    private FirebaseFirestore db;

    private final ArrayList<String> clubNames = new ArrayList<>();
    private ArrayAdapter<String> clubAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_booking);

        etEventName = findViewById(R.id.etEventName);
        etStartDate = findViewById(R.id.etStartDate); // you can keep as text or use DatePicker
        etEndDate = findViewById(R.id.etEndDate);
        spClubName = findViewById(R.id.spClubName);
        btnNext = findViewById(R.id.btnNext);

        db = FirebaseFirestore.getInstance();

        clubNames.clear();
        clubNames.add("-- Select Club --");
        clubAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clubNames);
        spClubName.setAdapter(clubAdapter);

        loadClubs();

        btnNext.setOnClickListener(v -> goNext());
    }

    private void loadClubs() {
        // same as your PHP: only Active clubs, and type Open/Close (optional)
        db.collection("clubs")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String name = doc.getString("club_name");
                        if (name != null) clubNames.add(name);
                    }
                    clubAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed load clubs: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void goNext() {
        String eventName = etEventName.getText().toString().trim();
        String startStr = etStartDate.getText().toString().trim(); // format: YYYY-MM-DD
        String endStr = etEndDate.getText().toString().trim();
        String clubName = spClubName.getSelectedItem().toString();

        if (eventName.isEmpty() || startStr.isEmpty() || endStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (clubName.equals("-- Select Club --")) {
            Toast.makeText(this, "Please select club", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simple date conversion (YYYY-MM-DD) -> Timestamp
        // For course project this is okay. If you want DatePicker later, tell me.
        Timestamp startTs = DateUtils.parseDateToTimestamp(startStr);

        Timestamp endTs = DateUtils.parseDateToTimestamp(endStr);

        if (startTs == null || endTs == null) {
            Toast.makeText(this, "Date format must be YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(CreateBookingActivity.this, SelectEquipmentActivity.class);
        i.putExtra("event_name", eventName);
        i.putExtra("club_name", clubName);
        i.putExtra("start_date_ms", startTs.toDate().getTime());
        i.putExtra("end_date_ms", endTs.toDate().getTime());
        startActivity(i);
    }
}
