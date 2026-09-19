package com.example.smartexpensetracker.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionModel {

    public enum TransactionType { INCOME, EXPENSE }

    private int id;
    private String title;
    private TransactionType type;
    private String category;
    private double amount;
    private String note;
    private String date;       // original String for database
    private Date dateObj;      // internal Date object for sorting/filtering

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    // Empty constructor
    public TransactionModel() {
    }

    // Constructor without ID (for new transactions before insert)
    public TransactionModel(String title, TransactionType type, String category, double amount, String note, String date) {
        this.title = title;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        setDate(date);
    }

    // Full constructor
    public TransactionModel(int id, String title, TransactionType type, String category, double amount, String note, String date) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        setDate(date);
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public void setType(String typeStr) {
        if (typeStr.equalsIgnoreCase("Income")) this.type = TransactionType.INCOME;
        else this.type = TransactionType.EXPENSE;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDate() { return date; }

    public void setDate(String date) {
        this.date = date;
        try {
            this.dateObj = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            this.dateObj = new Date();
            this.date = sdf.format(this.dateObj);
        }
    }

    public Date getDateObj() { return dateObj; }

    // Convenience methods
    public boolean isIncome() { return TransactionType.INCOME.equals(type); }
    public boolean isExpense() { return TransactionType.EXPENSE.equals(type); }

    public String getFormattedAmount() {
        return "₹" + String.format("%.2f", amount);
    }

}