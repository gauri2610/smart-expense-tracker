package com.example.smartexpensetracker;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartexpensetracker.adapter.TransactionAdapter;
import com.example.smartexpensetracker.database.DatabaseHelper;
import com.example.smartexpensetracker.model.TransactionModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class TransactionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private DatabaseHelper db;
    private TextView tvEmpty; // optional: show "No transactions" if list is empty
    private String userEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        recyclerView = findViewById(R.id.recyclerTransactions);
        tvEmpty = findViewById(R.id.tvEmptyTransactions); // optional TextView in layout

        db = new DatabaseHelper(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
        }

        List<TransactionModel> list = db.getAllTransactions(userEmail);
        if (list == null) list = new ArrayList<>();

        if (list.isEmpty()) {
            recyclerView.setVisibility(RecyclerView.GONE);
            if (tvEmpty != null) tvEmpty.setVisibility(TextView.VISIBLE);
        } else {
            recyclerView.setVisibility(RecyclerView.VISIBLE);
            if (tvEmpty != null) tvEmpty.setVisibility(TextView.GONE);
        }

        adapter = new TransactionAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
