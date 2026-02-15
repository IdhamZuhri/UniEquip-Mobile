package com.example.uniequip;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditClubActivity extends AppCompatActivity {

    private EditText etClubName, etAdvisorName, etAdvisorTel, etAdvisorEmail, etAdvisorStaffNo;
    private Spinner spClubType;
    private Button btnUpdate, btnBack;
    private FirebaseFirestore db;
    private String clubId;

    private final String[] clubTypes = new String[]{ "-- Select Club Type --", "Open", "Closed" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_club); // Reusing Register Layout

        // Update Headers
        ((TextView) findViewById(R.id.tvHeaderTitle)).setText("Edit Club Details");
        ((TextView) findViewById(R.id.tvHeaderSubtitle)).setText("Update existing organization information");

        // Bind Views
        etClubName = findViewById(R.id.etClubName);
        spClubType = findViewById(R.id.spClubType);
        etAdvisorName = findViewById(R.id.etAdvisorName);
        etAdvisorStaffNo = findViewById(R.id.etAdvisorStaffNo);
        etAdvisorTel = findViewById(R.id.etAdvisorTel);
        etAdvisorEmail = findViewById(R.id.etAdvisorEmail);
        btnUpdate = findViewById(R.id.btnSaveClub);
        btnBack = findViewById(R.id.btnBack);

        btnUpdate.setText("Update Club");
        db = FirebaseFirestore.getInstance();

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, clubTypes);
        spClubType.setAdapter(adapter);

        // Populate Data
        clubId = getIntent().getStringExtra("id");
        etClubName.setText(getIntent().getStringExtra("name"));
        etAdvisorName.setText(getIntent().getStringExtra("advName"));
        etAdvisorStaffNo.setText(getIntent().getStringExtra("advNum"));
        etAdvisorTel.setText(getIntent().getStringExtra("advTel"));
        etAdvisorEmail.setText(getIntent().getStringExtra("advEmail"));

        // Set Spinner
        String type = getIntent().getStringExtra("type");
        if (type != null) {
            int pos = adapter.getPosition(type);
            spClubType.setSelection(pos);
        }

        btnUpdate.setOnClickListener(v -> updateClub());
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateClub() {
        String clubName = etClubName.getText().toString().trim();
        String type = spClubType.getSelectedItem().toString();
        String advName = etAdvisorName.getText().toString().trim();
        String advNum = etAdvisorStaffNo.getText().toString().trim();
        String advTel = etAdvisorTel.getText().toString().trim();
        String advEmail = etAdvisorEmail.getText().toString().trim();

        if (clubName.isEmpty() || type.equals("-- Select Club Type --")) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("club_name", clubName);
        data.put("type", type);
        data.put("adv_name", advName);
        data.put("adv_num", advNum);
        data.put("adv_tel", advTel);
        data.put("adv_email", advEmail);

        db.collection("clubs").document(clubId)
                .update(data)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Club updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}