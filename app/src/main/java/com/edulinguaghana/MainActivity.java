package com.edulinguaghana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerLanguage;
    private Button btnRecitalMode, btnPracticeMode, btnQuizMode, btnProgressMode;
    private TextView tvBestScoreMain;

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";
    private static final String KEY_SFX_ENABLED = "SFX_ENABLED";

    private static final String KEY_LAST_LANG_CODE = "LAST_LANG_CODE";
    private static final String KEY_LAST_LANG_NAME = "LAST_LANG_NAME";
    private static final String KEY_SEEN_INTRO = "SEEN_INTRO";

    private String selectedLangCode = null;
    private String selectedLangName = null;

    private String[] langNames = {"Select language", "English", "French", "Twi", "Ewe", "Ga"};
    private String[] langCodes = {"", "en", "fr", "ak", "ee", "gaa"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        btnRecitalMode = findViewById(R.id.btnRecitalMode);
        btnPracticeMode = findViewById(R.id.btnPracticeMode);
        btnQuizMode = findViewById(R.id.btnQuizMode);
        btnProgressMode = findViewById(R.id.btnProgressMode);
        tvBestScoreMain = findViewById(R.id.tvBestScoreMain);

        setupLanguageSpinner();
        restoreLastLanguageSelection();
        setupButtons();
        setupLongPressHints();
        setupBackHandler();
        showIntroIfFirstTime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        tvBestScoreMain.setText("Best quiz score: " + highScore + " / 10");
    }

    // ---------------- BACK HANDLER ----------------

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Exit EduLingua Ghana?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    playAppExitSoundAndExit();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void playAppExitSoundAndExit() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean sfxOn = prefs.getBoolean(KEY_SFX_ENABLED, true);

        if (sfxOn) {
            try {
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.app_exit);
                if (mp != null) {
                    mp.setOnCompletionListener(MediaPlayer::release);
                    mp.start();
                }
            } catch (Exception e) {
                // ignore sound errors
            }
        }

        // Close immediately
        finish();
    }

    // ---------------- INTRO DIALOG ----------------

    private void showIntroIfFirstTime() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean seenIntro = prefs.getBoolean(KEY_SEEN_INTRO, false);
        if (seenIntro) return;

        new AlertDialog.Builder(this)
                .setTitle("Welcome to EduLingua Ghana")
                .setMessage(
                        "• Recital Mode – listen to letters and numbers in your chosen language.\n\n" +
                                "• Practice Mode – repeat after the app and practice pronunciation.\n\n" +
                                "• Quiz Mode – answer fun questions and play Speed Challenge.\n\n" +
                                "• Progress Tracker – see your best score and learning stats."
                )
                .setPositiveButton("Got it", (d, w) -> {
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.putBoolean(KEY_SEEN_INTRO, true);
                    ed.apply();
                })
                .show();
    }

    // ---------------- LANGUAGE ----------------

    private void setupLanguageSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                langNames
        );
        spinnerLanguage.setAdapter(adapter);

        spinnerLanguage.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedLangCode = null;
                selectedLangName = null;
            } else {
                selectedLangCode = langCodes[position];
                selectedLangName = langNames[position];
                saveLastLanguageSelection(selectedLangCode, selectedLangName);
            }
        });
    }

    private void saveLastLanguageSelection(String code, String name) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString(KEY_LAST_LANG_CODE, code);
        ed.putString(KEY_LAST_LANG_NAME, name);
        ed.apply();
    }

    private void restoreLastLanguageSelection() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String lastCode = prefs.getString(KEY_LAST_LANG_CODE, null);

        if (lastCode == null || lastCode.isEmpty()) {
            return; // keep default "Select language"
        }

        int indexToSelect = 0;
        for (int i = 0; i < langCodes.length; i++) {
            if (lastCode.equals(langCodes[i])) {
                indexToSelect = i;
                break;
            }
        }

        spinnerLanguage.setText(langNames[indexToSelect], false);
        selectedLangCode = langCodes[indexToSelect];
        selectedLangName = langNames[indexToSelect];
    }

    private boolean ensureLanguageSelected() {
        if (selectedLangCode == null || selectedLangName == null) {
            Toast.makeText(this, "Please select a language first.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ---------------- BUTTON ACTIONS ----------------

    private void setupButtons() {
        btnRecitalMode.setOnClickListener(v -> {
            if (!ensureLanguageSelected()) return;
            showContentTypeDialog(selectedLangCode, selectedLangName, "recital");
        });

        btnPracticeMode.setOnClickListener(v -> {
            if (!ensureLanguageSelected()) return;
            showContentTypeDialog(selectedLangCode, selectedLangName, "practice");
        });

        btnQuizMode.setOnClickListener(v -> {
            if (!ensureLanguageSelected()) return;
            showQuizTypeDialog(selectedLangCode, selectedLangName);
        });

        btnProgressMode.setOnClickListener(v -> openProgressScreen());
    }

    // Long press explanations
    private void setupLongPressHints() {
        btnRecitalMode.setOnLongClickListener(v -> {
            Toast.makeText(this, "Recital: listen to alphabet and numbers in " + (selectedLangName != null ? selectedLangName : "your language"), Toast.LENGTH_LONG).show();
            return true;
        });

        btnPracticeMode.setOnLongClickListener(v -> {
            Toast.makeText(this, "Practice: repeat after the app and improve pronunciation.", Toast.LENGTH_LONG).show();
            return true;
        });

        btnQuizMode.setOnLongClickListener(v -> {
            Toast.makeText(this, "Quiz: answer questions or play Speed Challenge.", Toast.LENGTH_LONG).show();
            return true;
        });

        btnProgressMode.setOnLongClickListener(v -> {
            Toast.makeText(this, "Progress: view your best scores and learning stats.", Toast.LENGTH_LONG).show();
            return true;
        });
    }

    private void showContentTypeDialog(String langCode, String langName, String mode) {
        String[] options = {"Alphabet", "Numbers"};
        String title = (mode.equals("recital") ? "Recital Mode" : "Practice Mode") + " - " + langName;

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(R.mipmap.ic_launcher_round)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openAlphabetScreen(langCode, langName, mode);
                    } else {
                        openNumbersScreen(langCode, langName, mode);
                    }
                })
                .show();
    }

    // UPDATED: now includes "Speed Challenge (Game)"
    private void showQuizTypeDialog(String langCode, String langName) {
        String[] quizTypes = {
                "Letter/Number Quiz",
                "Number Sequencing",
                "Matching",
                "Mixed Mode",
                "Speed Challenge (Game)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Quiz Mode - " + langName)
                .setIcon(R.mipmap.ic_launcher_round)
                .setItems(quizTypes, (dialog, which) -> {
                    if (which == 4) {
                        // Open separate game screen
                        openSpeedGameScreen(langCode, langName);
                        return;
                    }

                    String quizType;
                    if (which == 0)      quizType = "basic";
                    else if (which == 1) quizType = "sequence";
                    else if (which == 2) quizType = "matching";
                    else                 quizType = "mixed";

                    openQuizScreen(langCode, langName, quizType);
                })
                .show();
    }

    private void openAlphabetScreen(String langCode, String langName, String mode) {
        Intent intent = new Intent(MainActivity.this, AlphabetActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        intent.putExtra("MODE", mode);
        startActivity(intent);
    }

    private void openNumbersScreen(String langCode, String langName, String mode) {
        Intent intent = new Intent(MainActivity.this, NumbersActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        intent.putExtra("MODE", mode);
        startActivity(intent);
    }

    private void openQuizScreen(String langCode, String langName, String quizType) {
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        intent.putExtra("QUIZ_TYPE", quizType);
        startActivity(intent);
    }

    private void openSpeedGameScreen(String langCode, String langName) {
        Intent intent = new Intent(MainActivity.this, SpeedGameActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        startActivity(intent);
    }

    private void openProgressScreen() {
        Intent intent = new Intent(MainActivity.this, ProgressActivity.class);
        startActivity(intent);
    }
}
