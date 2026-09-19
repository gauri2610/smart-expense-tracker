package com.example.smartexpensetracker;

import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.smartexpensetracker.database.DatabaseHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    TextView balance, tvExpense, tvRemaining, tvIncome, aiText;
    PieChart pieChart;
    DatabaseHelper db;
    Switch themeSwitch;
    String userEmail = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_dashboard);

        db = new DatabaseHelper(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
        }

        // UI Bind
        balance = findViewById(R.id.balance);
        tvExpense = findViewById(R.id.tvExpense);
        tvRemaining = findViewById(R.id.tvRemaining);
        tvIncome = findViewById(R.id.tvIncome);
        pieChart = findViewById(R.id.pieChart);
        themeSwitch = findViewById(R.id.themeSwitch);
        aiText = findViewById(R.id.aiText);

        // Profile click
        findViewById(R.id.profileIcon).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Theme toggle
        themeSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Buttons
        findViewById(R.id.addExpense).setOnClickListener(v ->
                startActivity(new Intent(this, AddExpenseActivity.class)));

        findViewById(R.id.addIncome).setOnClickListener(v ->
                startActivity(new Intent(this, AddIncomeActivity.class)));

        findViewById(R.id.report).setOnClickListener(v ->
                startActivity(new Intent(this, ReportsActivity.class)));

        findViewById(R.id.budgetBtn).setOnClickListener(v ->
                startActivity(new Intent(this, BudgetActivity.class)));

        // 🔔 Notification schedule (ONLY ONCE)
        scheduleDailyReminder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    // 🔔 Notification function (clean)
    private void scheduleDailyReminder() {

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent i = new Intent(this, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                this, 0, i, PendingIntent.FLAG_IMMUTABLE
        );

        am.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                AlarmManager.INTERVAL_DAY,
                pi
        );
    }

    void updateUI() {

        double income = db.getTotal("Income", userEmail);
        double expense = db.getTotal("Expense", userEmail);
        double budget = db.getBudget(userEmail);

        double balanceValue = income - expense;
        double saving = income - expense;

        animateBalance(balanceValue);

        tvIncome.setText("Income ₹" + (int) income);
        tvExpense.setText("Expense ₹" + (int) expense);
        tvRemaining.setText("Remaining ₹" + (int) (budget - expense));

        // 🤖 AI
        String insight = "";

        double expenseRatio = (budget > 0) ? (expense / budget) : 0;

        if (budget == 0) {
            insight = "⚠️ Set a budget!";
        }
        else if (expense > budget) {
            insight = "🚨 Overspending!";
        }
        else if (expenseRatio > 0.8) {
            insight = "⚠️ 80% budget used!";
        }
        else if (expenseRatio > 0.5) {
            insight = "⚡ Half budget used!";
        }
        else if (expenseRatio < 0.3) {
            insight = "💰 Great saving!";
        }
        else {
            insight = "✅ Good control!";
        }

        if (income > 50000) {
            insight += "\n🔥 Strong income!";
        } else if (income < 5000) {
            insight += "\n⚠️ Low income!";
        }

        if (saving > 10000) {
            insight += "\n💎 Excellent saving!";
        } else if (saving < 0) {
            insight += "\n❗ Losing money!";
        }

        aiText.setText(insight);

        // 📊 PIE CHART
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) income, "Income"));
        entries.add(new PieEntry((float) expense, "Expense"));

        PieDataSet dataSet = new PieDataSet(entries, "Finance");
        dataSet.setColors(new int[]{0xFF4CAF50, 0xFFFF5252});
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        pieChart.animateY(1200);
        pieChart.setCenterText("Overview");
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    void animateBalance(double value) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, (float) value);
        animator.setDuration(1200);

        animator.addUpdateListener(a -> {
            float val = (float) a.getAnimatedValue();
            balance.setText("₹ " + (int) val);
        });

        animator.start();
    }
}
