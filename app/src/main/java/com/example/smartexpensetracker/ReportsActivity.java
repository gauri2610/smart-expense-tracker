package com.example.smartexpensetracker;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartexpensetracker.database.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.data.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.util.ArrayList;

public class ReportsActivity extends AppCompatActivity {

    TextView income, expense, totalBalance, aiReport;
    Button pdfBtn;
    ProgressBar progressBar;

    PieChart pieChart;
    BarChart barChart;

    DatabaseHelper db;
    String userEmail = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_reports);

        db = new DatabaseHelper(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) userEmail = user.getEmail();

        // Bind UI
        income = findViewById(R.id.income);
        expense = findViewById(R.id.expense);
        totalBalance = findViewById(R.id.totalBalance);
        aiReport = findViewById(R.id.aiReport);
        progressBar = findViewById(R.id.progressBar);
        pdfBtn = findViewById(R.id.exportPdf);

        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);

        loadData();

        pdfBtn.setOnClickListener(v -> generatePDF());
    }

    void loadData() {

        double totalIncome = db.getTotal("Income", userEmail);
        double totalExpense = db.getTotal("Expense", userEmail);
        double balance = totalIncome - totalExpense;

        income.setText("💰 Income ₹ " + (int) totalIncome);
        expense.setText("💸 Expense ₹ " + (int) totalExpense);
        totalBalance.setText("₹ " + (int) balance);

        // 📊 Progress
        int progress = (totalIncome == 0) ? 0 :
                (int)((totalExpense / totalIncome) * 100);

        progressBar.setProgress(progress);

        // 🤖 ADVANCED AI INSIGHT
        String insight;

        if (totalIncome == 0) {
            insight = "⚠️ No income added!";
        }
        else if (totalExpense > totalIncome) {
            insight = "🚨 Overspending! Expenses > Income";
        }
        else if (progress > 80) {
            insight = "⚠️ 80% income used!";
        }
        else if (progress > 50) {
            insight = "⚡ Half income spent!";
        }
        else if (progress < 30) {
            insight = "💰 Excellent saving!";
        }
        else {
            insight = "✅ Good balance!";
        }

        // 🆕 Extra insight
        double saving = totalIncome - totalExpense;
        if (saving > 10000) {
            insight += "\n💎 You saved a lot this month!";
        } else if (saving < 0) {
            insight += "\n❗ You're in loss!";
        }

        aiReport.setText(insight);

        setupPieChart(totalIncome, totalExpense);
        setupBarChart();
    }

    // 📊 PIE CHART
    void setupPieChart(double income, double expense) {

        ArrayList<PieEntry> list = new ArrayList<>();
        list.add(new PieEntry((float) income, "Income"));
        list.add(new PieEntry((float) expense, "Expense"));

        PieDataSet dataSet = new PieDataSet(list, "Finance");
        dataSet.setColors(new int[]{0xFF4CAF50, 0xFFFF5252});
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        pieChart.setCenterText("Overview");
        pieChart.setCenterTextSize(16f);
        pieChart.animateY(1200);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }

    // 📊 BAR CHART (Monthly Trend - Demo)
    void setupBarChart() {

        ArrayList<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(1, 2000));
        entries.add(new BarEntry(2, 3000));
        entries.add(new BarEntry(3, 4000));
        entries.add(new BarEntry(4, 6000));
        entries.add(new BarEntry(5, 8000));

        BarDataSet dataSet = new BarDataSet(entries, "Monthly Expense");
        dataSet.setColor(0xFF2196F3);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        barChart.animateY(1000);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    // 📄 PDF
    void generatePDF() {

        try {
            double totalIncome = db.getTotal("Income", userEmail);
            double totalExpense = db.getTotal("Expense", userEmail);
            double balance = totalIncome - totalExpense;

            File file = new File(getExternalFilesDir(null), "SmartExpenseReport.pdf");

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            doc.add(new Paragraph("📊 Smart Expense Report\n"));
            doc.add(new Paragraph("----------------------------"));
            doc.add(new Paragraph("Total Income: ₹ " + totalIncome));
            doc.add(new Paragraph("Total Expense: ₹ " + totalExpense));
            doc.add(new Paragraph("Remaining Balance: ₹ " + balance));
            doc.add(new Paragraph("----------------------------"));
            doc.add(new Paragraph("Generated by SmartExpenseTracker"));

            doc.close();

            Toast.makeText(this,
                    "PDF Saved 📄\n" + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error generating PDF ❌",
                    Toast.LENGTH_SHORT).show();
        }
    }
}