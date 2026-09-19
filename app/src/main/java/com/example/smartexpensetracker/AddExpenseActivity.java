package com.example.smartexpensetracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartexpensetracker.database.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.*;

public class AddExpenseActivity extends AppCompatActivity {

    EditText amount, note;
    Spinner category;
    Button save, voiceBtn, dateBtn;

    DatabaseHelper db;
    String selectedDate = "";
    String userEmail = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_expense);

        amount = findViewById(R.id.amount);
        note = findViewById(R.id.note);
        category = findViewById(R.id.category);
        save = findViewById(R.id.save);
        voiceBtn = findViewById(R.id.voiceBtn);
        dateBtn = findViewById(R.id.dateBtn);

        db = new DatabaseHelper(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
        }

        // 📅 DATE PICKER
        dateBtn.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate = d + "/" + (m + 1) + "/" + y;
                dateBtn.setText("📅 " + selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 🎤 VOICE INPUT
        voiceBtn.setOnClickListener(v -> {
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            i.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Say: 500 food / 200 travel / 100 movie");

            try {
                startActivityForResult(i, 100);
            } catch (Exception e) {
                Toast.makeText(this, "Voice not supported", Toast.LENGTH_SHORT).show();
            }
        });

        // ✨ LIVE SMART DETECT
        note.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                smartFill(s.toString().toLowerCase());
            }
        });

        // 💾 SAVE
        save.setOnClickListener(v -> {
            String amt = amount.getText().toString().trim();
            String cat = category.getSelectedItem() != null ? category.getSelectedItem().toString() : "General";
            String noteText = note.getText().toString().trim();

            if (amt.isEmpty()) {
                Toast.makeText(this, "Enter amount ❗", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Select date 📅", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double value = Double.parseDouble(amt);
                String title = noteText.isEmpty() ? cat : noteText;

                // 👉 DB addTransaction with userEmail
                db.addTransaction(title, "Expense", cat, value, noteText, selectedDate, userEmail);

                Toast.makeText(this, "Expense Saved ✅", Toast.LENGTH_SHORT).show();
                
                // Close activity and return to Dashboard
                finish();
                
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount ❗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🎤 RESULT
    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);

        if (req == 100 && res == RESULT_OK && data != null) {
            ArrayList<String> list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (list != null && !list.isEmpty()) {
                String text = list.get(0).toLowerCase();
                note.setText(text);
                smartFill(text);
            }
        }
    }

    // 🤖 SMART AI LOGIC
    void smartFill(String text) {
        // 💰 Extract amount
        String num = text.replaceAll("[^0-9]", "");
        if (!num.isEmpty() && amount.getText().toString().isEmpty()) {
            amount.setText(num);
        }

        // 🧠 Category detection
        if (text.contains("food") || text.contains("hotel") || text.contains("eat")) setCategory("Food");
        else if (text.contains("travel") || text.contains("uber") || text.contains("bus") || text.contains("petrol")) setCategory("Travel");
        else if (text.contains("shopping") || text.contains("cloth") || text.contains("mall")) setCategory("Shopping");
        else if (text.contains("bill") || text.contains("electricity") || text.contains("recharge")) setCategory("Bills");
        else if (text.contains("rent") || text.contains("house")) setCategory("Rent");
        else if (text.contains("movie") || text.contains("netflix") || text.contains("game")) setCategory("Entertainment");
    }

    void setCategory(String value) {
        for (int i = 0; i < category.getCount(); i++) {
            if (category.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                category.setSelection(i);
                break;
            }
        }
    }
}
