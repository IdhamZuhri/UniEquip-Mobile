package com.example.uniequip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etUserNumber, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etName = findViewById(R.id.etName);
        etUserNumber = findViewById(R.id.etUserNumber);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Back to Login Button Logic
        tvBackToLogin.setOnClickListener(v -> {
            finish(); // Closes Register and goes back to Login
        });

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String userNumber = etUserNumber.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = "student"; // Hardcoded as per requirement

            if (name.isEmpty() || userNumber.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);

            // 1) Create user in Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            btnRegister.setEnabled(true);
                            String msg = (task.getException() != null) ? task.getException().getMessage() : "Register failed";
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            return;
                        }

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            btnRegister.setEnabled(true);
                            Toast.makeText(this, "Register failed (no user)", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 2) Save profile in Firestore using UID
                        String uid = user.getUid();

                        Map<String, Object> data = new HashMap<>();
                        data.put("name", name);
                        data.put("userNumber", userNumber); // Important: Student ID
                        data.put("email", email);
                        data.put("role", role);
                        data.put("createdAt", Timestamp.now());

                        db.collection("users").document(uid)
                                .set(data)
                                .addOnSuccessListener(unused -> {
                                    btnRegister.setEnabled(true);
                                    Toast.makeText(this, "Account created! Please Login.", Toast.LENGTH_SHORT).show();

                                    // Logout immediately so they have to login with new creds
                                    mAuth.signOut();

                                    // Go back to Login Activity
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnRegister.setEnabled(true);
                                    Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    });
        });
    }
}