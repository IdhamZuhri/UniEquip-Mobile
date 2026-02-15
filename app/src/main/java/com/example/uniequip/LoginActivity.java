package com.example.uniequip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etUserNumber, etPassword;
    private Button btnLogin, btnReset;
    private TextView tvGoToRegister;
    private RadioGroup rgRole; // ✅ Added RadioGroup

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 1. Bind IDs
        etUserNumber = findViewById(R.id.etUserNumber);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnReset = findViewById(R.id.btnReset);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        rgRole = findViewById(R.id.rgRole); // ✅ Bind RadioGroup

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Reset Logic
        btnReset.setOnClickListener(v -> {
            etUserNumber.setText("");
            etPassword.setText("");
        });

        // 3. Register Logic
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 4. Auto Login
        // We pass 'null' because auto-login trusts the database role directly
        if (mAuth.getCurrentUser() != null) {
            goByRole(mAuth.getCurrentUser(), null);
        }

        // 5. Login Logic
        btnLogin.setOnClickListener(v -> {
            String email = etUserNumber.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Get the selected role from RadioGroup
            int selectedId = rgRole.getCheckedRadioButtonId();
            String selectedRole = (selectedId == R.id.rbAdmin) ? "admin" : "student";

            btnLogin.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        btnLogin.setEnabled(true);
                        if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                            // ✅ Pass the selected role for verification
                            goByRole(mAuth.getCurrentUser(), selectedRole);
                        } else {
                            String msg = (task.getException() != null) ? task.getException().getMessage() : "Login failed";
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    // ✅ Updated method to accept 'requiredRole' for validation
    // Update this method in LoginActivity.java
    // ... inside LoginActivity.java ...

    private void goByRole(FirebaseUser user, String requiredRole) {
        String uid = user.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(LoginActivity.this, "Profile not found", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }

                    String dbRole = doc.getString("role");
                    String dbUserNumber = doc.getString("userNumber"); // Get ID from Firestore

                    if (dbRole == null) dbRole = "student";

                    if (requiredRole != null && !dbRole.equalsIgnoreCase(requiredRole)) {
                        Toast.makeText(LoginActivity.this, "Access Denied", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        return;
                    }

                    // ✅ FIX: Use SessionManager to save (matches your new class)
                    SessionManager.setStudentNumber(LoginActivity.this, dbUserNumber);

                    Intent intent;
                    if ("admin".equalsIgnoreCase(dbRole)) {
                        intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                });
    }
}