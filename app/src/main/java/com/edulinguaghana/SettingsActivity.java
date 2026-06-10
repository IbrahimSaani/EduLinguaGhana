package com.edulinguaghana;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRole;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchMusic, switchSfx;
    private SwitchMaterial switchAnimations;
    private SwitchMaterial switchLowPowerAnimations;
    private SwitchMaterial switchDailyReminders, switchStreakAlerts;
    private SwitchMaterial switchHighContrast, switchStandardFont;
    private SwitchMaterial switchHapticFeedback, switchAutoVoice, switchFocusMode;
    private SeekBar seekBarQuizMusicVolume, seekBarTextSize;
    private TextView tvQuizMusicVolumeValue, tvTextSizeValue;
    private Button btnResetProgress;
    private Button btnSyncToCloud;
    private Button btnSyncFromCloud;
    private Button btnChangeRole;
    private View btnAppTutorial;
    private View btnPrivacyPolicy;
    private View btnContactUs;
    private TextView tvLastSync;
    private TextView tvCurrentRole;
    private TextView tvAppVersion;

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_MUSIC_ENABLED = "MUSIC_ENABLED";
    private static final String KEY_SFX_ENABLED = "SFX_ENABLED";
    private static final String KEY_ANIMATIONS_ENABLED = "ANIMATIONS_ENABLED";
    private static final String KEY_LOW_POWER_ANIMATIONS = "LOW_POWER_ANIMATIONS";
    private static final String KEY_QUIZ_MUSIC_VOLUME = "QUIZ_MUSIC_VOLUME";
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
        seekBarQuizMusicVolume = findViewById(R.id.seekBarQuizMusicVolume);
        tvQuizMusicVolumeValue = findViewById(R.id.tvQuizMusicVolumeValue);
        btnResetProgress = findViewById(R.id.btnResetProgress);
        btnSyncToCloud = findViewById(R.id.btnSyncToCloud);
        btnSyncFromCloud = findViewById(R.id.btnSyncFromCloud);
        btnChangeRole = findViewById(R.id.btnChangeRole);
          btnAppTutorial = findViewById(R.id.btnAppTutorial);
          btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
          btnContactUs = findViewById(R.id.btnContactUs);
          tvLastSync = findViewById(R.id.tvLastSync);
          tvCurrentRole = findViewById(R.id.tvCurrentRole);
          tvAppVersion = findViewById(R.id.tvAppVersion);
        switchDailyReminders = findViewById(R.id.switchDailyReminders);
        switchStreakAlerts = findViewById(R.id.switchStreakAlerts);

        // Accessibility Views
        switchHighContrast = findViewById(R.id.switchHighContrast);
        switchStandardFont = findViewById(R.id.switchStandardFont);
        switchHapticFeedback = findViewById(R.id.switchHapticFeedback);
        switchAutoVoice = findViewById(R.id.switchAutoVoice);
        switchFocusMode = findViewById(R.id.switchFocusMode);
        seekBarTextSize = findViewById(R.id.seekBarTextSize);
        tvTextSizeValue = findViewById(R.id.tvTextSizeValue);

        // Apply custom font to section headers
        applySectionHeaderFonts();

        // Update App Version
        updateAppVersionText();

        // Update Sync and Role status
        updateLastSyncTime();
        displayCurrentRole();
        setupAccessibilityListeners();

        // Load preferences
        boolean musicEnabled = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getBoolean(KEY_MUSIC_ENABLED, true);
        boolean sfxEnabled = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getBoolean(KEY_SFX_ENABLED, true);
        boolean animationsEnabled = AppPreferences.isAnimationsEnabled(this);
        boolean lowPowerEnabled = AppPreferences.isReducedMotionEnabled(this);
        boolean dailyReminders = AppPreferences.isDailyRemindersEnabled(this);
        boolean streakAlerts = AppPreferences.isStreakAlertsEnabled(this);
        int quizMusicVolume = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getInt(KEY_QUIZ_MUSIC_VOLUME, 50);

        // Set UI states
        switchMusic.setChecked(musicEnabled);
        switchSfx.setChecked(sfxEnabled);
        switchAnimations.setChecked(animationsEnabled);
        switchLowPowerAnimations.setChecked(lowPowerEnabled);
        switchDailyReminders.setChecked(dailyReminders);
        switchStreakAlerts.setChecked(streakAlerts);

        // Set quiz music volume
        if (seekBarQuizMusicVolume != null) {
            seekBarQuizMusicVolume.setProgress(quizMusicVolume);
            updateQuizMusicVolumeDisplay(quizMusicVolume);
        }

        // Save preferences when toggles change
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_MUSIC_ENABLED, isChecked).apply();
        });

        switchSfx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_SFX_ENABLED, isChecked).apply();
        });

        switchAnimations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.setAnimationsEnabled(this, isChecked);
        });

        switchLowPowerAnimations.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.setReducedMotionEnabled(this, isChecked);
        });

        switchDailyReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.setDailyRemindersEnabled(SettingsActivity.this, isChecked);
            if (isChecked) {
                new NotificationManager(SettingsActivity.this).checkAndGenerateNotifications(true);
            }
        });

        switchStreakAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.setStreakAlertsEnabled(SettingsActivity.this, isChecked);
            if (isChecked) {
                new NotificationManager(SettingsActivity.this).checkAndGenerateNotifications(true);
            }
        });

        // Quiz Music Volume SeekBar listener
        if (seekBarQuizMusicVolume != null) {
            seekBarQuizMusicVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        updateQuizMusicVolumeDisplay(progress);
                        prefs.edit().putInt(KEY_QUIZ_MUSIC_VOLUME, progress).apply();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Not used
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Notify QuizActivity of volume change if needed
                    Toast.makeText(SettingsActivity.this, 
                        getString(R.string.settings_quiz_music_volume_updated, seekBar.getProgress()), 
                        Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Cloud Sync buttons
        btnSyncToCloud.setOnClickListener(v -> syncToCloud());
        btnSyncFromCloud.setOnClickListener(v -> syncFromCloud());

        // Change Role button
        if (btnChangeRole != null) {
            btnChangeRole.setOnClickListener(v -> openRoleSelection());
        }

        // Test upload score button
        btnSyncToCloud.setOnLongClickListener(v -> {
            // Long press to test upload a score
            testUploadScore();
            return true;
        });

        // Reset quiz progress when clicked
        btnResetProgress.setOnClickListener(v -> {
            StyledMenuHelper.showStyledConfirmationDialog(
                    this,
                    "⚠️",
                    getString(R.string.settings_reset_dialog_title),
                    getString(R.string.settings_reset_dialog_message),
                    getString(R.string.settings_reset_dialog_positive),
                    getString(R.string.settings_reset_dialog_negative),
                    () -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(KEY_HIGH_SCORE, 0);
                        editor.putInt(KEY_TOTAL_QUIZZES, 0);
                        editor.putInt(KEY_TOTAL_CORRECT, 0);
                        editor.apply();
                        Toast.makeText(this, getString(R.string.settings_reset_toast), Toast.LENGTH_SHORT).show();
                    },
                    null
            );
        });

        // About & Privacy listeners
         if (btnAppTutorial != null) {
             btnAppTutorial.setOnClickListener(v -> {
                 Intent intent = new Intent(this, TutorialActivity.class);
                 startActivity(intent);
             });
         }

         if (btnPrivacyPolicy != null) {
             btnPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicy());
         }

         if (btnContactUs != null) {
             btnContactUs.setOnClickListener(v -> contactDevelopers());
         }

    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }


    private void setupAccessibilityListeners() {
        // High Contrast
        switchHighContrast.setChecked(AppPreferences.isHighContrastEnabled(this));
        switchHighContrast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.setHighContrastEnabled(this, isChecked);
            // In a real app, we might need to recreate the activity or apply theme
            Toast.makeText(this, "High Contrast " + (isChecked ? "Enabled" : "Disabled") + ". Restart app to apply fully.", Toast.LENGTH_SHORT).show();
        });

        // Standard Font
        switchStandardFont.setChecked(AppPreferences.isStandardFontEnabled(this));
        switchStandardFont.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.setStandardFontEnabled(this, isChecked);
            Toast.makeText(this, "Standard Font " + (isChecked ? "Enabled" : "Disabled") + ". Restart app to apply fully.", Toast.LENGTH_SHORT).show();
        });

        // Haptic Feedback
        switchHapticFeedback.setChecked(AppPreferences.isHapticFeedbackEnabled(this));
        switchHapticFeedback.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.setHapticFeedbackEnabled(this, isChecked);
        });

        // Auto Voice
        switchAutoVoice.setChecked(AppPreferences.isAutoVoiceEnabled(this));
        switchAutoVoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.setAutoVoiceEnabled(this, isChecked);
        });

        // Focus Mode
        switchFocusMode.setChecked(AppPreferences.isFocusModeEnabled(this));
        switchFocusMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppPreferences.setFocusModeEnabled(this, isChecked);
            Toast.makeText(this, "Focus Mode " + (isChecked ? "Enabled" : "Disabled") + ". UI will adjust on next screen load.", Toast.LENGTH_SHORT).show();
        });

        // Text Size
        int currentSize = AppPreferences.getTextSize(this);
        seekBarTextSize.setProgress(currentSize);
        updateTextSizeDisplay(currentSize);

        seekBarTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextSizeDisplay(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AppPreferences.setTextSize(SettingsActivity.this, seekBar.getProgress());
                Toast.makeText(SettingsActivity.this, "Text Size updated. Restart app to apply fully.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTextSizeDisplay(int progress) {
        String sizeLabel;
        switch (progress) {
            case 0: sizeLabel = getString(R.string.settings_text_size_small); break;
            case 2: sizeLabel = getString(R.string.settings_text_size_large); break;
            case 3: sizeLabel = getString(R.string.settings_text_size_extra_large); break;
            default: sizeLabel = getString(R.string.settings_text_size_normal); break;
        }
        tvTextSizeValue.setText(sizeLabel);
    }

    private void updateAppVersionText() {
        if (tvAppVersion == null) return;
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText(getString(R.string.settings_app_version, versionName));
        } catch (Exception e) {
            tvAppVersion.setText("App Version: 1.0.0");
        }
    }


    private void applyToolbarFont(Toolbar toolbar) {
        if (AppPreferences.isStandardFontEnabled(this)) return;
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
        } catch (Exception ignored) {
        }
    }

    private void applySectionHeaderFonts() {
        Typeface typeface = null;
        if (!AppPreferences.isStandardFontEnabled(this)) {
            try {
                typeface = ResourcesCompat.getFont(this, R.font.agbalumo);
            } catch (Exception ignored) {
            }
        }

        // Apply font to all section headers
        TextView tvAudioHeader = findViewById(R.id.tvAudioHeader);
        TextView tvVisualHeader = findViewById(R.id.tvVisualHeader);
        TextView tvNotificationsHeader = findViewById(R.id.tvNotificationsHeader);
        TextView tvProgressHeader = findViewById(R.id.tvProgressHeader);
        TextView tvAccountHeader = findViewById(R.id.tvAccountHeader);
        TextView tvCloudHeader = findViewById(R.id.tvCloudHeader);
        TextView tvAboutHeader = findViewById(R.id.tvAboutHeader);
        TextView tvAccessibilityHeader = findViewById(R.id.tvAccessibilityHeader);

        if (tvAudioHeader != null) tvAudioHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
        if (tvVisualHeader != null) tvVisualHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
        if (tvNotificationsHeader != null) tvNotificationsHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
        if (tvProgressHeader != null) tvProgressHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
        if (tvAccountHeader != null) tvAccountHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
        if (tvCloudHeader != null) tvCloudHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
        if (tvAboutHeader != null) tvAboutHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
        if (tvAccessibilityHeader != null) tvAccessibilityHeader.setTypeface(typeface, android.graphics.Typeface.BOLD);
    }

    private void showPrivacyPolicy() {
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "🔒",
            "Privacy Policy",
            "Your privacy is important to us. EduLingua Ghana collects minimal data to sync your progress and provide a personalized experience.\n\nWe do not share your personal information with third parties.\n\nYour learning data is securely stored on Google Firebase.",
            "I Understand",
            null,
            null,
            null
        );
    }

    private void contactDevelopers() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:edulinguaghana4@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "[App Feedback] EduLingua Ghana");
        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Oops! You don't have an email app installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update the volume display value in the settings
     */
    private void updateQuizMusicVolumeDisplay(int volumePercent) {
        if (tvQuizMusicVolumeValue != null) {
            tvQuizMusicVolumeValue.setText(volumePercent + "%");
        }
    }


    private void updateLastSyncTime() {
        if (tvLastSync == null) return;

        CloudSyncManager syncManager = new CloudSyncManager(this);
        String lastSyncTime = syncManager.getLastSyncTimeString();
        tvLastSync.setText(getString(R.string.settings_last_sync_label, lastSyncTime));
    }

    private void displayCurrentRole() {
        if (tvCurrentRole == null) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvCurrentRole.setText(getString(R.string.settings_role_prefix, getString(R.string.settings_role_not_logged_in)));
            if (btnChangeRole != null) {
                btnChangeRole.setEnabled(false);
            }
            return;
        }

        RoleManager roleManager = new RoleManager();
        roleManager.getUserRole(this, user.getUid(), new RoleManager.RoleCallback() {
            @Override
            public void onRoleRetrieved(UserRole role) {
                String roleValue = "";
                switch (role) {
                    case STUDENT:
                        roleValue = getString(R.string.settings_role_student);
                        break;
                    case TEACHER:
                        roleValue = getString(R.string.settings_role_teacher);
                        break;
                    case PARENT:
                        roleValue = getString(R.string.settings_role_parent);
                        break;
                }
                tvCurrentRole.setText(getString(R.string.settings_role_prefix, roleValue));
            }

            @Override
            public void onError(String error) {
                tvCurrentRole.setText(getString(R.string.settings_role_prefix, getString(R.string.settings_role_not_set)));
            }
        });
    }

    private void openRoleSelection() {
        Intent intent = new Intent(this, RoleSelectionActivity.class);
        intent.putExtra("first_time", false); // Not first time, allow back navigation
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh role display when returning from RoleSelectionActivity
        displayCurrentRole();
    }

    private void syncToCloud() {
        CloudSyncManager syncManager = new CloudSyncManager(this);

        if (!syncManager.canSync()) {
            StyledMenuHelper.showStyledConfirmationDialog(
                this,
                "☁️",
                "Sync Unavailable",
                "Cloud sync requires login and internet connection.",
                "OK",
                null,
                null,
                null
            );
            return;
        }

        // Show progress dialog
        AlertDialog progressDialog = StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "☁️",
            "Syncing to Cloud",
            "Uploading your progress...",
            null,
            null,
            null,
            null
        );
        progressDialog.setCancelable(false);
        progressDialog.show();

        syncManager.syncToCloud((success, message) -> {
            runOnUiThread(() -> {
                progressDialog.dismiss();

                StyledMenuHelper.showStyledConfirmationDialog(
                    this,
                    success ? "✅" : "❌",
                    success ? "Sync Complete" : "Sync Failed",
                    message,
                    "OK",
                    null,
                    () -> {
                        if (success) {
                            updateLastSyncTime();
                        }
                    },
                    null
                );
            });
        });
    }

    private void syncFromCloud() {
        CloudSyncManager syncManager = new CloudSyncManager(this);

        if (!syncManager.canSync()) {
            StyledMenuHelper.showStyledConfirmationDialog(
                this,
                "☁️",
                "Sync Unavailable",
                "Cloud sync requires login and internet connection.",
                "OK",
                null,
                null,
                null
            );
            return;
        }

        // Show confirmation
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "⬇️",
            "Download Progress",
            "This will replace your local progress with cloud data. Continue?",
            "Download",
            "Cancel",
            () -> {
                // Show progress dialog
                AlertDialog progressDialog = StyledMenuHelper.showStyledConfirmationDialog(
                    this,
                    "☁️",
                    "Syncing from Cloud",
                    "Downloading your progress...",
                    null,
                    null,
                    null,
                    null
                );
                progressDialog.setCancelable(false);

                syncManager.syncFromCloud((success, message) -> {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();

                        StyledMenuHelper.showStyledConfirmationDialog(
                            this,
                            success ? "✅" : "❌",
                            success ? "Sync Complete" : "Sync Failed",
                            message,
                            "OK",
                            null,
                            () -> {
                                if (success) {
                                    updateLastSyncTime();
                                }
                            },
                            null
                        );
                    });
                });
            },
            null
        );
    }

    /**
     * Test method to upload a score manually
     * Triggered by long-pressing the "Sync to Cloud" button
     */
    private void testUploadScore() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, R.string.error_feature_locked, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show dialog to enter test score
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter score (0-100)");
        input.setText("75");
        
        // Wrap input in a container for padding
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = (int) (20 * getResources().getDisplayMetrics().density);
        params.leftMargin = margin;
        params.rightMargin = margin;
        input.setLayoutParams(params);
        container.addView(input);

        if (isFinishing() || isDestroyed()) return;
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Test Upload Score")
            .setMessage("Upload a test score to the leaderboard:")
            .setView(container)
            .setPositiveButton("Upload", (dialog, which) -> {
                try {
                    int testScore = Integer.parseInt(input.getText().toString());
                    if (testScore < 0 || testScore > 100) {
                        Toast.makeText(this, "The score must be between 0 and 100!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CloudSyncManager.uploadScoreToLeaderboard(user, testScore, this);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Oops! That doesn't look like a valid score.", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
