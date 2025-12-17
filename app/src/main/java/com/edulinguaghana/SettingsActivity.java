package com.edulinguaghana;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchMusic, switchSfx;
    private SwitchMaterial switchAnimations;
    private SwitchMaterial switchLowPowerAnimations;
    private Button btnResetProgress;
    private Button btnSyncToCloud;
    private Button btnSyncFromCloud;
    private TextView tvLastSync;
    private RadioGroup rgTheme;
    private RadioButton rbLight, rbDark, rbSystem;

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_MUSIC_ENABLED = "MUSIC_ENABLED";
    private static final String KEY_SFX_ENABLED = "SFX_ENABLED";
    private static final String KEY_ANIMATIONS_ENABLED = "ANIMATIONS_ENABLED";
    private static final String KEY_LOW_POWER_ANIMATIONS = "LOW_POWER_ANIMATIONS";
    private static final String KEY_THEME = "THEME";

    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";
    private static final String KEY_TOTAL_QUIZZES = "TOTAL_QUIZZES";
    private static final String KEY_TOTAL_CORRECT = "TOTAL_CORRECT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Apply custom font to toolbar
        applyToolbarFont(toolbar);

        // Find views
        switchMusic = findViewById(R.id.switchMusic);
        switchSfx = findViewById(R.id.switchSfx);
        // Direct lookups (layout contains these ids)
        switchAnimations = findViewById(R.id.switchAnimations);
        switchLowPowerAnimations = findViewById(R.id.switchLowPowerAnimations);
        btnResetProgress = findViewById(R.id.btnResetProgress);
        btnSyncToCloud = findViewById(R.id.btnSyncToCloud);
        btnSyncFromCloud = findViewById(R.id.btnSyncFromCloud);
        tvLastSync = findViewById(R.id.tvLastSync);
        rgTheme = findViewById(R.id.rgTheme);
        rbLight = findViewById(R.id.rbLight);
        rbDark = findViewById(R.id.rbDark);
        rbSystem = findViewById(R.id.rbSystem);

        // Apply custom font to section headers
        applySectionHeaderFonts();

        // Update last sync time
        updateLastSyncTime();

        // Load preferences
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean musicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true);
        boolean sfxEnabled = prefs.getBoolean(KEY_SFX_ENABLED, true);
        boolean animationsEnabled = prefs.getBoolean(KEY_ANIMATIONS_ENABLED, true);
        boolean lowPowerEnabled = prefs.getBoolean(KEY_LOW_POWER_ANIMATIONS, false);
        int currentTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Set UI states
        switchMusic.setChecked(musicEnabled);
        switchSfx.setChecked(sfxEnabled);
        switchAnimations.setChecked(animationsEnabled);
        switchLowPowerAnimations.setChecked(lowPowerEnabled);
        updateThemeRadioButtons(currentTheme);

        // Save preferences when toggles change
        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_MUSIC_ENABLED, isChecked).apply();
        });

        switchSfx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_SFX_ENABLED, isChecked).apply();
        });

        switchAnimations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_ANIMATIONS_ENABLED, isChecked).apply();
        });

        switchLowPowerAnimations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_LOW_POWER_ANIMATIONS, isChecked).apply();
        });

        // Change theme when radio button is selected
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int newTheme = currentTheme;
            if (checkedId == R.id.rbLight) {
                newTheme = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rbDark) {
                newTheme = AppCompatDelegate.MODE_NIGHT_YES;
            } else if (checkedId == R.id.rbSystem) {
                newTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            prefs.edit().putInt(KEY_THEME, newTheme).apply();
            AppCompatDelegate.setDefaultNightMode(newTheme);
        });

        // Cloud Sync buttons
        btnSyncToCloud.setOnClickListener(v -> syncToCloud());
        btnSyncFromCloud.setOnClickListener(v -> syncFromCloud());

        // Reset quiz progress when clicked
        btnResetProgress.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.settings_reset_dialog_title)
                    .setMessage(R.string.settings_reset_dialog_message)
                    .setPositiveButton(R.string.settings_reset_dialog_positive, (dialog, which) -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(KEY_HIGH_SCORE, 0);
                        editor.putInt(KEY_TOTAL_QUIZZES, 0);
                        editor.putInt(KEY_TOTAL_CORRECT, 0);
                        editor.apply();
                        Toast.makeText(this, getString(R.string.settings_reset_toast), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.settings_reset_dialog_negative, null)
                    .show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void updateThemeRadioButtons(int currentTheme) {
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            rbLight.setChecked(true);
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            rbDark.setChecked(true);
        } else {
            rbSystem.setChecked(true);
        }
    }

    private void applyToolbarFont(Toolbar toolbar) {
        try {
            Typeface typeface = ResourcesCompat.getFont(this, R.font.agbalumo);
            // Find and apply font to toolbar title
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View view = toolbar.getChildAt(i);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    if (textView.getText().equals(toolbar.getTitle())) {
                        textView.setTypeface(typeface);
                        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 20);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applySectionHeaderFonts() {
        try {
            Typeface typeface = ResourcesCompat.getFont(this, R.font.agbalumo);

            // Apply font to all section headers
            TextView tvAudioHeader = findViewById(R.id.tvAudioHeader);
            TextView tvVisualHeader = findViewById(R.id.tvVisualHeader);
            TextView tvThemeHeader = findViewById(R.id.tvThemeHeader);
            TextView tvProgressHeader = findViewById(R.id.tvProgressHeader);

            if (tvAudioHeader != null) {
                tvAudioHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
            }
            if (tvVisualHeader != null) {
                tvVisualHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
            }
            if (tvThemeHeader != null) {
                tvThemeHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
            }
            if (tvProgressHeader != null) {
                tvProgressHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLastSyncTime() {
        if (tvLastSync == null) return;

        CloudSyncManager syncManager = new CloudSyncManager(this);
        String lastSyncTime = syncManager.getLastSyncTimeString();
        tvLastSync.setText("Last sync: " + lastSyncTime);
    }

    private void syncToCloud() {
        CloudSyncManager syncManager = new CloudSyncManager(this);

        if (!syncManager.canSync()) {
            new AlertDialog.Builder(this)
                .setTitle("Sync Unavailable")
                .setMessage("Cloud sync requires login and internet connection.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        // Show progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(this)
            .setTitle("Syncing to Cloud")
            .setMessage("Uploading your progress...")
            .setCancelable(false)
            .create();
        progressDialog.show();

        syncManager.syncToCloud((success, message) -> {
            runOnUiThread(() -> {
                progressDialog.dismiss();

                new AlertDialog.Builder(this)
                    .setTitle(success ? "Sync Complete" : "Sync Failed")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();

                if (success) {
                    updateLastSyncTime();
                }
            });
        });
    }

    private void syncFromCloud() {
        CloudSyncManager syncManager = new CloudSyncManager(this);

        if (!syncManager.canSync()) {
            new AlertDialog.Builder(this)
                .setTitle("Sync Unavailable")
                .setMessage("Cloud sync requires login and internet connection.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        // Show confirmation
        new AlertDialog.Builder(this)
            .setTitle("Download Progress")
            .setMessage("This will replace your local progress with cloud data. Continue?")
            .setPositiveButton("Download", (dialog, which) -> {
                // Show progress dialog
                AlertDialog progressDialog = new AlertDialog.Builder(this)
                    .setTitle("Syncing from Cloud")
                    .setMessage("Downloading your progress...")
                    .setCancelable(false)
                    .create();
                progressDialog.show();

                syncManager.syncFromCloud((success, message) -> {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();

                        new AlertDialog.Builder(this)
                            .setTitle(success ? "Sync Complete" : "Sync Failed")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show();

                        if (success) {
                            updateLastSyncTime();
                        }
                    });
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
