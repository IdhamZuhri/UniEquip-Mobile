package com.example.uniequip;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;

public class AdminEditEquipmentActivity extends AppCompatActivity {

    private EditText etName, etModel, etTotalQty;
    private Spinner spCategory;
    private Button btnUpdate, btnDelete, btnCancel;

    private FirebaseFirestore db;
    private String equipmentId;

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
        setContentView(R.layout.activity_admin_edit_equipment);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etName = findViewById(R.id.etEditEqName);
        spCategory = findViewById(R.id.spEditEqCategory);
        etModel = findViewById(R.id.etEditEqModel);
        etTotalQty = findViewById(R.id.etEditEqTotalQty);

        btnUpdate = findViewById(R.id.btnUpdateEquipment);
        btnDelete = findViewById(R.id.btnDeleteEquipment);
        btnCancel = findViewById(R.id.btnCancel);

        equipmentId = getIntent().getStringExtra("equipment_id");
        if (equipmentId == null) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        setupCategorySpinner();
        loadEquipment();

        btnUpdate.setOnClickListener(v -> updateEquipment());
        btnDelete.setOnClickListener(v -> softDeleteEquipment());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList(categories)
        );
        spCategory.setAdapter(adapter);
    }

    private void loadEquipment() {
        db.collection("equipment").document(equipmentId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        finish();
                        return;
                    }
                    String name = doc.getString("name");
                    String category = doc.getString("category");
                    String model = doc.getString("model");
                    Long tq = doc.getLong("totalQty");

                    etName.setText(name == null ? "" : name);
                    etModel.setText(model == null ? "" : model);
                    etTotalQty.setText(String.valueOf(tq == null ? 0 : tq.intValue()));

                    if (category != null) {
                        int idx = indexOf(categories, category);
                        if (idx >= 0) spCategory.setSelection(idx);
                    }
                });
    }

    private int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null && arr[i].equals(val)) return i;
        }
        return -1;
    }

    private void updateEquipment() {
        String name = etName.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        String qtyStr = etTotalQty.getText().toString().trim();

        if (name.isEmpty() || qtyStr.isEmpty()) {
            Toast.makeText(this, "Please fill in Name and Quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.equals("-- Select Category --")) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        int newQty;
        try {
            newQty = Integer.parseInt(qtyStr);
        } catch (Exception e) {
            Toast.makeText(this, "Quantity must be a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newQty < 0) {
            Toast.makeText(this, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUpdate.setEnabled(false);
        btnUpdate.setText("Checking Stock...");

        // 1. Check stock usage before updating
        StockLogic.computeOutMapNow(db, new StockLogic.ReservedMapCallback() {
            @Override
            public void onSuccess(HashMap<String, Integer> outMap) {
                int currentlyOut = outMap.containsKey(equipmentId) ? outMap.get(equipmentId) : 0;

                if (newQty < currentlyOut) {
                    btnUpdate.setEnabled(true);
                    btnUpdate.setText("Update Changes");
                    Toast.makeText(AdminEditEquipmentActivity.this,
                            "Cannot reduce to " + newQty + ". " + currentlyOut + " items are currently borrowed!",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                saveToFirestore(name, category, model, newQty);
            }

            @Override
            public void onError(String message) {
                btnUpdate.setEnabled(true);
                btnUpdate.setText("Update Changes");
                Toast.makeText(AdminEditEquipmentActivity.this, "Stock check failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFirestore(String name, String category, String model, int newQty) {
        db.collection("equipment").document(equipmentId)
                .update(
                        "name", name,
                        "category", category,
                        "model", model,
                        "totalQty", newQty
                )
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Equipment updated successfully! ✅", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnUpdate.setEnabled(true);
                    btnUpdate.setText("Update Changes");
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ✅ UPDATED DELETE LOGIC
    private void softDeleteEquipment() {
        btnDelete.setEnabled(false);
        btnDelete.setText("Checking Stock...");

        // 1. Check stock usage before deleting
        StockLogic.computeOutMapNow(db, new StockLogic.ReservedMapCallback() {
            @Override
            public void onSuccess(HashMap<String, Integer> outMap) {
                int currentlyOut = outMap.containsKey(equipmentId) ? outMap.get(equipmentId) : 0;

                // 2. If item is currently borrowed/late, BLOCK DELETE
                if (currentlyOut > 0) {
                    btnDelete.setEnabled(true);
                    btnDelete.setText("Delete Equipment");
                    Toast.makeText(AdminEditEquipmentActivity.this,
                            "Cannot delete! " + currentlyOut + " items are still borrowed.",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // 3. If safe, proceed to soft delete
                performSoftDelete();
            }

            @Override
            public void onError(String message) {
                btnDelete.setEnabled(true);
                btnDelete.setText("Delete Equipment");
                Toast.makeText(AdminEditEquipmentActivity.this, "Stock check failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSoftDelete() {
        db.collection("equipment").document(equipmentId)
                .update("status", "inactive")
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Equipment removed (inactive) ✅", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnDelete.setEnabled(true);
                    btnDelete.setText("Delete Equipment");
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}