package com.example.smartexpensetracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartexpensetracker.database.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    ImageView profileImage;
    TextView userName, userEmail, tvProfileIncome, tvProfileExpense, tvProfileRemaining, aiProfileText;
    Button logoutBtn;

    FirebaseAuth auth;
    StorageReference storageRef;
    DatabaseHelper db;

    ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        tvProfileIncome = findViewById(R.id.tvProfileIncome);
        tvProfileExpense = findViewById(R.id.tvProfileExpense);
        tvProfileRemaining = findViewById(R.id.tvProfileRemaining);
        aiProfileText = findViewById(R.id.aiProfileText);
        logoutBtn = findViewById(R.id.logoutBtn);

        auth = FirebaseAuth.getInstance();
        db = new DatabaseHelper(this);

        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            userName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User");
            userEmail.setText(user.getEmail());

            // 🔥 Load Image
            StorageReference imgRef = storageRef.child(user.getUid() + ".jpg");
            imgRef.getDownloadUrl()
                    .addOnSuccessListener(uri ->
                            Glide.with(this).load(uri).circleCrop().into(profileImage))
                    .addOnFailureListener(e ->
                            profileImage.setImageResource(android.R.drawable.ic_menu_myplaces));
        }

        // 🔥 Image Picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) uploadProfileImage(imageUri);
                    }
                });

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Logout
        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        updateStats();
    }

    // 🔥 Upload Image
    private void uploadProfileImage(Uri uri) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        StorageReference ref = storageRef.child(user.getUid() + ".jpg");

        ref.putFile(uri)
                .addOnSuccessListener(task -> {
                    Toast.makeText(this, "Image Uploaded ✅", Toast.LENGTH_SHORT).show();

                    ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        Glide.with(this)
                                .load(downloadUri)
                                .circleCrop()
                                .into(profileImage);
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload Failed ❌", Toast.LENGTH_SHORT).show());
    }

    // 🔥 Stats
    private void updateStats() {

        FirebaseUser user = auth.getCurrentUser();
        String email = (user != null) ? user.getEmail() : "";

        double income = db.getTotal("Income", email);
        double expense = db.getTotal("Expense", email);
        double budget = db.getBudget(email);

        double remaining = budget - expense;

        tvProfileIncome.setText("₹" + (int) income);
        tvProfileExpense.setText("₹" + (int) expense);
        tvProfileRemaining.setText("₹" + (int) remaining);

        String insight;

        if (budget == 0) insight = "⚠️ Set budget!";
        else if (expense > budget) insight = "🚨 Overspending!";
        else insight = "✅ Good financial control!";

        aiProfileText.setText(insight);

        Button settingsBtn = findViewById(R.id.settingsBtn);
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, SettingsActivity.class));
            });
        }
    }
}
