package com.edulinguaghana;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchMusic, switchSfx;
    private Button btnResetProgress;

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_MUSIC_ENABLED = "MUSIC_ENABLED";
    private static final String KEY_SFX_ENABLED = "SFX_ENABLED";

    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";
    private static final String KEY_TOTAL_QUIZZES = "TOTAL_QUIZZES";
    private static final String KEY_TOTAL_CORRECT = "TOTAL_CORRECT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Find views
        switchMusic = findViewById(R.id.switchMusic);
        switchSfx = findViewById(R.id.switchSfx);
        btnResetProgress = findViewById(R.id.btnResetProgress);

        // Load preferences for Music and SFX
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean musicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true);
        boolean sfxEnabled = prefs.getBoolean(KEY_SFX_ENABLED, true);

        // Set switches based on saved preferences
        switchMusic.setChecked(musicEnabled);
        switchSfx.setChecked(sfxEnabled);

        // Save preferences when toggles change
        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_MUSIC_ENABLED, isChecked);
            editor.apply();
        });

        switchSfx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_SFX_ENABLED, isChecked);
            editor.apply();
        });

        // Reset quiz progress when clicked
        btnResetProgress.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset Progress")
                    .setMessage("Are you sure you want to reset quiz progress? This will clear your best score and quiz statistics.")
                    .setPositiveButton("Yes, reset", (dialog, which) -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(KEY_HIGH_SCORE, 0);
                        editor.putInt(KEY_TOTAL_QUIZZES, 0);
                        editor.putInt(KEY_TOTAL_CORRECT, 0);
                        editor.apply();
                        Toast.makeText(this, "Progress has been reset.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
