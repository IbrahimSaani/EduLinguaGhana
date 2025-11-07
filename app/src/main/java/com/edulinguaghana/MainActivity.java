package com.edulinguaghana;  // <-- change to your real package if different

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button btnEnglish, btnFrench, btnTwi, btnEwe, btnGa;
    private TextView tvBestScoreMain;

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEnglish = findViewById(R.id.btnEnglish);
        btnFrench  = findViewById(R.id.btnFrench);
        btnTwi     = findViewById(R.id.btnTwi);
        btnEwe     = findViewById(R.id.btnEwe);
        btnGa      = findViewById(R.id.btnGa);
        tvBestScoreMain = findViewById(R.id.tvBestScoreMain);

        btnEnglish.setOnClickListener(v -> showModeDialog("en", "English"));
        btnFrench.setOnClickListener(v -> showModeDialog("fr", "French"));
        btnTwi.setOnClickListener(v -> showModeDialog("twi", "Twi"));
        btnEwe.setOnClickListener(v -> showModeDialog("ewe", "Ewe"));
        btnGa.setOnClickListener(v -> showModeDialog("ga", "Ga"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        tvBestScoreMain.setText("Best quiz score: " + highScore + " / 10");
    }

    private void showModeDialog(String langCode, String langName) {
        String[] options = {"Recital Mode", "Practice Mode", "Quiz Mode", "Progress Tracker"};

        new AlertDialog.Builder(this)
                .setTitle("Choose mode for " + langName)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Recital
                        showContentTypeDialog(langCode, langName, "recital");
                    } else if (which == 1) {
                        // Practice
                        showContentTypeDialog(langCode, langName, "practice");
                    } else if (which == 2) {
                        // Quiz
                        openQuizScreen(langCode, langName);
                    } else if (which == 3) {
                        // Progress Tracker (global, not language-specific for now)
                        openProgressScreen();
                    }
                })
                .show();
    }

    private void showContentTypeDialog(String langCode, String langName, String mode) {
        String[] options = {"Alphabet", "Numbers"};

        String title = "Choose content for " +
                (mode.equals("recital") ? "Recital" : "Practice") +
                " (" + langName + ")";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openAlphabetScreen(langCode, langName, mode);
                    } else if (which == 1) {
                        openNumbersScreen(langCode, langName, mode);
                    }
                })
                .show();
    }

    private void openAlphabetScreen(String langCode, String langName, String mode) {
        Intent intent = new Intent(MainActivity.this, AlphabetActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        intent.putExtra("MODE", mode); // "recital" or "practice"
        startActivity(intent);
    }

    private void openNumbersScreen(String langCode, String langName, String mode) {
        Intent intent = new Intent(MainActivity.this, NumbersActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        intent.putExtra("MODE", mode); // "recital" or "practice"
        startActivity(intent);
    }

    private void openQuizScreen(String langCode, String langName) {
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        startActivity(intent);
    }

    private void openProgressScreen() {
        Intent intent = new Intent(MainActivity.this, ProgressActivity.class);
        startActivity(intent);
    }
}
