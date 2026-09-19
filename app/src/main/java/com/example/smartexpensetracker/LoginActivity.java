package com.example.smartexpensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartexpensetracker.database.DatabaseHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText email, pass;
    Button loginBtn;
    TextView goRegister;
    DatabaseHelper db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        db = new DatabaseHelper(this);
        auth = FirebaseAuth.getInstance();

        // Bind UI
        email = findViewById(R.id.emailEditText);
        pass = findViewById(R.id.passwordEditText);
        loginBtn = findViewById(R.id.loginBtn);
        goRegister = findViewById(R.id.registerText);

        // LOGIN BUTTON
        loginBtn.setOnClickListener(v -> loginUser());

        // REGISTER
        goRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    // 🔥 PREMIUM LOGIN METHOD
    private void loginUser() {

        String userEmail = email.getText().toString().trim();
        String userPass = pass.getText().toString().trim();

        // ✅ Validation
        if (userEmail.isEmpty()) {
            email.setError("Enter email");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            email.setError("Invalid email format");
            email.requestFocus();
            return;
        }

        if (userPass.isEmpty()) {
            pass.setError("Enter password");
            pass.requestFocus();
            return;
        }

        if (userPass.length() < 6) {
            pass.setError("Password must be 6+ chars");
            pass.requestFocus();
            return;
        }

        // 🔥 Disable button (prevent double click)
        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");

        // 🔥 Firebase Login
        auth.signInWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener(task -> {

                    loginBtn.setEnabled(true);
                    loginBtn.setText("Login");

                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Welcome 👋", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, DashboardActivity.class));
                        finish(); // 🔥 back press won't return to login

                    } else {

                        // 🔥 SQLite fallback
                        if (db.login(userEmail, userPass)) {

                            startActivity(new Intent(this, DashboardActivity.class));
                            finish();

                        } else {
                            Toast.makeText(this, "Invalid Login ❌", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            if (user != null) {
                                startActivity(new Intent(this, DashboardActivity.class));
                                finish();
                            }
                        }
                    }
                });
    }
}
