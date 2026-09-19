package com.example.smartexpensetracker;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartexpensetracker.database.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BudgetActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private TextView tvMonthlyBudget, tvExpensesThisMonth, tvRemainingBudget;
    private EditText etBudgetAmount;
    private String userEmail = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_budget);

        db = new DatabaseHelper(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
        }

        tvMonthlyBudget = findViewById(R.id.tvMonthlyBudget);
        tvExpensesThisMonth = findViewById(R.id.tvExpensesThisMonth);
        tvRemainingBudget = findViewById(R.id.tvRemainingBudget);
        etBudgetAmount = findViewById(R.id.etBudgetAmount);

        updateUI();

        findViewById(R.id.btnSaveBudget).setOnClickListener(v -> {
            String val = etBudgetAmount.getText().toString();
            if (!val.isEmpty()) {
                db.setBudget(Double.parseDouble(val), userEmail);
                Toast.makeText(this, "Budget Updated", Toast.LENGTH_SHORT).show();
                updateUI();
                etBudgetAmount.setText("");
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            }
        });

        if (findViewById(R.id.toolbarBudget) != null) {
            setSupportActionBar(findViewById(R.id.toolbarBudget));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Budget");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void updateUI() {
        double budget = db.getBudget(userEmail);
        double expenses = db.getTotal("Expense", userEmail);
        double remaining = budget - expenses;

        tvMonthlyBudget.setText("Monthly Budget: ₹" + budget);
        tvExpensesThisMonth.setText("Expenses This Month: ₹" + expenses);
        tvRemainingBudget.setText("Remaining Budget: ₹" + remaining);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
