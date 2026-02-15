package com.example.uniequip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class EquipmentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnBack;
    private EquipmentAdapter adapter;
    private final ArrayList<EquipmentItem> items = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.recyclerEquipment);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup Adapter with Click Listener to open Edit Page
        adapter = new EquipmentAdapter(this, items);

        // Optional: If you want clicking an item to open the Edit page, ensure your adapter handles it.
        // If your adapter doesn't support clicks yet, we can add that later.
        // For now, this just lists them.

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEquipmentWithInStoreQty();
    }

    private void loadEquipmentWithInStoreQty() {
        db.collection("equipment")
                .get()
                .addOnSuccessListener(eqSnap -> {
                    items.clear();

                    for (QueryDocumentSnapshot doc : eqSnap) {
                        // ✅ FIX: Check if item is "inactive" and skip it
                        String status = doc.getString("status");
                        if ("inactive".equals(status)) {
                            continue; // Skip this item, don't add to list
                        }

                        String id = doc.getId();
                        String name = doc.getString("name");
                        String category = doc.getString("category");
                        String model = doc.getString("model");
                        String imageUrl = doc.getString("imageUrl");

                        Long tq = doc.getLong("totalQty");
                        int totalQty = (tq == null) ? 0 : tq.intValue();

                        items.add(new EquipmentItem(id, name, category, model, totalQty, imageUrl));
                    }

                    // Compute available stock (Total - Borrowed)
                    StockLogic.computeOutMapNow(db, new StockLogic.ReservedMapCallback() {
                        @Override
                        public void onSuccess(HashMap<String, Integer> outMap) {
                            for (EquipmentItem it : items) {
                                int out = outMap.containsKey(it.id) ? outMap.get(it.id) : 0;
                                it.inStoreQty = Math.max(it.totalQty - out, 0);
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(String message) {
                            for (EquipmentItem it : items) it.inStoreQty = it.totalQty;
                            adapter.notifyDataSetChanged();
                        }
                    });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load equipment: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}