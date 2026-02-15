package com.example.uniequip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class ClubListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnBack;
    private final ArrayList<ClubItem> clubs = new ArrayList<>();
    private ClubAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_list);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recyclerView = findViewById(R.id.recyclerClubs);
        btnBack = findViewById(R.id.btnBackClubList);
        db = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Initialize Adapter with Edit and Delete Logic
        adapter = new ClubAdapter(clubs, new ClubAdapter.OnClubActionListener() {
            @Override
            public void onDelete(String clubId, int position) {
                confirmDelete(clubId, position);
            }

            @Override
            public void onEdit(ClubItem item) {
                Intent i = new Intent(ClubListActivity.this, EditClubActivity.class);
                i.putExtra("id", item.id);
                i.putExtra("name", item.clubName);
                i.putExtra("type", item.type);
                i.putExtra("advName", item.advName);
                i.putExtra("advNum", item.advNum);
                i.putExtra("advTel", item.advTel);
                i.putExtra("advEmail", item.advEmail);
                startActivity(i);
            }
        });

        recyclerView.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        loadClubs();
    }

    // ✅ ADD ON RESUME TO REFRESH LIST AUTOMATICALLY
    @Override
    protected void onResume() {
        super.onResume();
        loadClubs();
    }

    private void confirmDelete(String clubId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Club")
                .setMessage("Are you sure you want to delete this club? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteClub(clubId, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteClub(String clubId, int position) {
        db.collection("clubs").document(clubId)
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Club deleted", Toast.LENGTH_SHORT).show();
                    if (position >= 0 && position < clubs.size()) {
                        clubs.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadClubs() {
        db.collection("clubs")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snapshot -> {
                    clubs.clear(); // Clear old list before adding new
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String id = doc.getId();
                        String clubName = doc.getString("club_name");
                        String type = doc.getString("type");
                        String advName = doc.getString("adv_name");
                        String advNum = doc.getString("adv_num");
                        String advTel = doc.getString("adv_tel");
                        String advEmail = doc.getString("adv_email");
                        String status = doc.getString("status");

                        clubs.add(new ClubItem(id, clubName, type, advName, advNum, advTel, advEmail, status));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}