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

public class CreateEquipmentActivity extends AppCompatActivity {

    // 1. Removed 'etImageUrl' since it's not in your XML
    private EditText etEquipmentName, etModel, etTotalQty;
    private Spinner spCategory;
    private Button btnSave, btnBack;

    private FirebaseFirestore db;

    private final String[] categories = new String[]{
            "-- Select Category --",
            "Stage Equipment", "Audio Equipment", "Visual Equipment",
            "Lighting Equipment", "Furniture & Seating", "Tents & Canopies",
            "Decor & Draping", "Power & Electrical", "Staging & Structures",
            "Signage & Display", "Catering Equipment", "Climate Control",
            "Event Technology", "Sanitation & Safety", "Transportation & Storage"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_equipment);

        etEquipmentName = findViewById(R.id.etEquipmentName);
        spCategory = findViewById(R.id.spCategory);
        etModel = findViewById(R.id.etModel);
        etTotalQty = findViewById(R.id.etTotalQty);
        btnSave = findViewById(R.id.btnSaveEquipment);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );
        spCategory.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveEquipment());
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveEquipment() {
        String name = etEquipmentName.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        String model = etModel.getText().toString().trim();
        String qtyStr = etTotalQty.getText().toString().trim();

        // 2. Removed 'imageUrl' reading logic

        if (name.isEmpty() || model.isEmpty() || qtyStr.isEmpty()) {
            Toast.makeText(this, "Please fill in name, model, and quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.equals("-- Select Category --")) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
        } catch (Exception e) {
            Toast.makeText(this, "Quantity must be a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (qty <= 0) {
            Toast.makeText(this, "Quantity must be more than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("category", category);
        data.put("model", model);
        data.put("totalQty", qty);
        data.put("imageUrl", ""); // 3. Save empty string or remove this line
        data.put("createdAt", Timestamp.now());

        db.collection("equipment")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Equipment created successfully! ✅", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CreateEquipmentActivity.this, AdminDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Equipment");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}