package com.example.smartexpensetracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartexpensetracker.database.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.*;

public class AddIncomeActivity extends AppCompatActivity {

    EditText amount, source;
    Button save, voiceBtn, dateBtn;
    TextView aiText;

    DatabaseHelper db;
    String selectedDate = "";
    String userEmail = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_income);

        amount = findViewById(R.id.incomeAmount);
        source = findViewById(R.id.incomeSource);
        save = findViewById(R.id.saveIncome);
        voiceBtn = findViewById(R.id.voiceBtn);
        dateBtn = findViewById(R.id.dateBtn);
        aiText = findViewById(R.id.aiIncomeText);

        db = new DatabaseHelper(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
        }

        // 📅 DATE
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
                    "Say: 20000 salary / 5000 freelance");

            try {
                startActivityForResult(i, 200);
            } catch (Exception e) {
                Toast.makeText(this, "Voice not supported", Toast.LENGTH_SHORT).show();
            }
        });

        // ✨ SMART TEXT DETECT
        source.addTextChangedListener(new TextWatcher() {
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
            String src = source.getText().toString().trim();

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
                String title = src.isEmpty() ? "Income" : src;

                // Fix: Call addTransaction with userEmail
                db.addTransaction(title, "Income", "Income", value, "", selectedDate, userEmail);

                Toast.makeText(this, "Income Added 💰", Toast.LENGTH_SHORT).show();

                generateInsight(value);

                // Reset fields
                amount.setText("");
                source.setText("");
                dateBtn.setText("📅 Select Date");
                selectedDate = "";

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount ❗", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Quick Fill Methods
    public void quickFillSalary(View v) {
        source.setText("Salary");
    }

    public void quickFillFreelance(View v) {
        source.setText("Freelance");
    }

    public void quickFillBusiness(View v) {
        source.setText("Business");
    }

    // 🎤 RESULT
    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);

        if (req == 200 && res == RESULT_OK && data != null) {

            ArrayList<String> list =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (list != null && !list.isEmpty()) {

                String text = list.get(0).toLowerCase();

                source.setText(text);
                // No need to call smartFill(text) here because source.setText triggers the TextWatcher
            }
        }
    }

    // 🤖 SMART LOGIC
    void smartFill(String text) {

        // Extract amount
        String num = text.replaceAll("[^0-9]", "");
        if (!num.isEmpty() && amount.getText().toString().isEmpty()) {
            amount.setText(num);
        }

        // Detect source (prevent recursion by checking if text is already set)
        String detected = null;
        if (text.contains("salary")) detected = "Salary";
        else if (text.contains("freelance")) detected = "Freelance";
        else if (text.contains("business")) detected = "Business";
        else if (text.contains("bonus")) detected = "Bonus";

        if (detected != null && !source.getText().toString().equals(detected)) {
            source.setText(detected);
            source.setSelection(source.getText().length()); // cursor to end
        }
    }

    // 💡 AI INSIGHT
    void generateInsight(double income) {

        String msg;

        if (income > 50000)
            msg = "🔥 Great income! You're earning strong!";
        else if (income > 20000)
            msg = "💼 Good job! Stable earning!";
        else if (income > 5000)
            msg = "🙂 Decent income, keep growing!";
        else
            msg = "⚠️ Try increasing your income sources!";

        aiText.setText(msg);
    }
}
