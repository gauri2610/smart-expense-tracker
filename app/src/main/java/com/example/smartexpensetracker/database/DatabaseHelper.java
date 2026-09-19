package com.example.smartexpensetracker.database;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import com.example.smartexpensetracker.model.TransactionModel;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseDB";
    private static final int DATABASE_VERSION = 2; // Incremented version

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT, password TEXT)");
        db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, type TEXT, category TEXT, amount REAL, note TEXT, date TEXT, user_email TEXT)");
        db.execSQL("CREATE TABLE budget(id INTEGER PRIMARY KEY AUTOINCREMENT, amount REAL, user_email TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS budget");
        onCreate(db);
    }

    // USER
    public boolean register(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("password", password);
        return db.insert("users", null, cv) > 0;
    }

    public boolean login(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE email=? AND password=?", new String[]{email, password});
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    // TRANSACTIONS
    public void addTransaction(String title, String type, String category, double amount, String note, String date, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("type", type);
        cv.put("category", category);
        cv.put("amount", amount);
        cv.put("note", note);
        cv.put("date", date);
        cv.put("user_email", userEmail);
        db.insert("transactions", null, cv);
    }

    public List<TransactionModel> getAllTransactions(String userEmail) {
        List<TransactionModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions WHERE user_email=? ORDER BY id DESC", new String[]{userEmail});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String typeStr = cursor.getString(2);
                String category = cursor.getString(3);
                double amount = cursor.getDouble(4);
                String note = cursor.getString(5);
                String date = cursor.getString(6);

                TransactionModel.TransactionType type = typeStr.equalsIgnoreCase("Income") ? 
                        TransactionModel.TransactionType.INCOME : TransactionModel.TransactionType.EXPENSE;

                list.add(new TransactionModel(id, title, type, category, amount, note, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public double getTotal(String type, String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(amount) FROM transactions WHERE type=? AND user_email=?", new String[]{type, userEmail});
        double total = 0;
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        return total;
    }

    // BUDGET
    public void setBudget(double amount, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("budget", "user_email=?", new String[]{userEmail});
        ContentValues cv = new ContentValues();
        cv.put("amount", amount);
        cv.put("user_email", userEmail);
        db.insert("budget", null, cv);
    }

    public double getBudget(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT amount FROM budget WHERE user_email=?", new String[]{userEmail});
        double budget = 0;
        if (c.moveToFirst()) budget = c.getDouble(0);
        c.close();
        return budget;
    }
}
