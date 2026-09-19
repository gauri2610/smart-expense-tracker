package com.example.smartexpensetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    Switch darkMode, notification;
    Spinner currency;
    SeekBar alertSeek;
    TextView alertValue;
    Button saveBtn, clearBtn;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_settings);

        // Bind
        darkMode = findViewById(R.id.switchDarkMode);
        notification = findViewById(R.id.switchNotification);
        currency = findViewById(R.id.spinnerCurrency);
        alertSeek = findViewById(R.id.seekAlert);
        alertValue = findViewById(R.id.tvAlertValue);
        saveBtn = findViewById(R.id.btnSaveSettings);
        clearBtn = findViewById(R.id.btnClear);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // Currency options
        String[] curr = {"₹ INR", "$ USD", "€ EURO"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, curr);
        currency.setAdapter(adapter);

        loadSettings();

        // SeekBar change
        alertSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                alertValue.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Save
        saveBtn.setOnClickListener(v -> {

            SharedPreferences.Editor editor = prefs.edit();

            editor.putBoolean("dark", darkMode.isChecked());
            editor.putBoolean("notify", notification.isChecked());
            editor.putString("currency", currency.getSelectedItem().toString());
            editor.putInt("alert", alertSeek.getProgress());

            editor.apply();

            // Apply Dark Mode
            if (darkMode.isChecked()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            Toast.makeText(this, "Settings Saved ✅", Toast.LENGTH_SHORT).show();
        });

        // Clear Data
        clearBtn.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Toast.makeText(this, "All Data Cleared ❗", Toast.LENGTH_SHORT).show();
        });
    }

    void loadSettings() {

        darkMode.setChecked(prefs.getBoolean("dark", false));
        notification.setChecked(prefs.getBoolean("notify", true));
        alertSeek.setProgress(prefs.getInt("alert", 80));

        alertValue.setText(alertSeek.getProgress() + "%");
    }
}