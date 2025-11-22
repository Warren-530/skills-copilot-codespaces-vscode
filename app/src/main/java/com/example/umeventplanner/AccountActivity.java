package com.example.umeventplanner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountActivity extends AppCompatActivity {

    private EditText etName, etYear, etCourse, etMatricNumber;
    private SwitchMaterial swPlannerMode;
    private Button btnUpdate;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        etName = findViewById(R.id.etName);
        etYear = findViewById(R.id.etYear);
        etCourse = findViewById(R.id.etCourse);
        etMatricNumber = findViewById(R.id.etMatricNumber);
        swPlannerMode = findViewById(R.id.swPlannerMode);
        btnUpdate = findViewById(R.id.btnUpdate);

        loadUserProfile();

        btnUpdate.setOnClickListener(v -> updateUserProfile());
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    etName.setText(documentSnapshot.getString("name"));
                    etYear.setText(documentSnapshot.getString("year"));
                    etCourse.setText(documentSnapshot.getString("course"));
                    etMatricNumber.setText(documentSnapshot.getString("matricNumber"));
                    String role = documentSnapshot.getString("role");
                    swPlannerMode.setChecked("Event Planner".equals(role));
                }
            });
        }
    }

    private void updateUserProfile() {
        String name = etName.getText().toString().trim();
        String year = etYear.getText().toString().trim();
        String course = etCourse.getText().toString().trim();
        String matricNumber = etMatricNumber.getText().toString().trim();
        String role = swPlannerMode.isChecked() ? "Event Planner" : "Participant";

        if (currentUser != null) {
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());
            userRef.update("name", name,
                    "year", year,
                    "course", course,
                    "matricNumber", matricNumber,
                    "role", role)
                    .addOnSuccessListener(aVoid -> Toast.makeText(AccountActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
