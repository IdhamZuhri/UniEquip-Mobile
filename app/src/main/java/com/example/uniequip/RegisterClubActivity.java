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

import java.util.HashMap;
import java.util.Map;

public class RegisterClubActivity extends AppCompatActivity {

    private EditText etClubName, etAdvisorName, etAdvisorTel, etAdvisorEmail, etAdvisorStaffNo;
    private Spinner spClubType;
    private Button btnSaveClub, btnBack; // Added btnBack

    private FirebaseFirestore db;

    private final String[] clubTypes = new String[]{
            "-- Select Club Type --",
            "Open",
            "Closed" // Changed "Close" to "Closed" for better grammar
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_club);

        // Bind Views
        etClubName = findViewById(R.id.etClubName);
        spClubType = findViewById(R.id.spClubType);
        etAdvisorName = findViewById(R.id.etAdvisorName);
        etAdvisorStaffNo = findViewById(R.id.etAdvisorStaffNo);
        etAdvisorTel = findViewById(R.id.etAdvisorTel);
        etAdvisorEmail = findViewById(R.id.etAdvisorEmail);
        btnSaveClub = findViewById(R.id.btnSaveClub);
        btnBack = findViewById(R.id.btnBack); // Bind Back Button

        db = FirebaseFirestore.getInstance();

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                clubTypes
        );
        spClubType.setAdapter(adapter);

        // Button Listeners
        btnSaveClub.setOnClickListener(v -> saveClub());

        btnBack.setOnClickListener(v -> finish()); // Cancel action
    }

    private void saveClub() {
        String clubName = etClubName.getText().toString().trim();
        String type = spClubType.getSelectedItem().toString();
        String advName = etAdvisorName.getText().toString().trim();
        String advNum = etAdvisorStaffNo.getText().toString().trim();
        String advTel = etAdvisorTel.getText().toString().trim();
        String advEmail = etAdvisorEmail.getText().toString().trim();

        if (clubName.isEmpty() || advName.isEmpty() || advNum.isEmpty()
                || advTel.isEmpty() || advEmail.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type.equals("-- Select Club Type --")) {
            Toast.makeText(this, "Please select a club type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double clicks
        btnSaveClub.setEnabled(false);
        btnSaveClub.setText("Saving...");

        Map<String, Object> data = new HashMap<>();
        data.put("club_name", clubName);
        data.put("type", type);
        data.put("adv_name", advName);
        data.put("adv_num", advNum);
        data.put("adv_tel", advTel);
        data.put("adv_email", advEmail);
        data.put("status", "active");
        data.put("createdAt", Timestamp.now());

        db.collection("clubs")
                .add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Club registered successfully! ✅", Toast.LENGTH_LONG).show();

                    // Redirect to dashboard
                    Intent intent = new Intent(this, AdminDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveClub.setEnabled(true);
                    btnSaveClub.setText("Save Club");
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}