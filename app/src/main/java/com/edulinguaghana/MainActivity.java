package com.edulinguaghana;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button btnEnglish, btnFrench, btnTwi, btnEwe, btnGa;
    private TextView tvBestScoreMain;

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";

    // Startup / exit sounds
    private MediaPlayer startPlayer;
    private MediaPlayer exitPlayer;

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

        // Play app start sound
        try {
            startPlayer = MediaPlayer.create(this, R.raw.app_start);
            if (startPlayer != null) {
                startPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    startPlayer = null;
                });
                startPlayer.start();
            }
        } catch (Exception e) {
            // If sound missing or error, ignore
        }

        // Prepare exit sound (we'll play it in onDestroy)
        try {
            exitPlayer = MediaPlayer.create(this, R.raw.app_exit);
        } catch (Exception e) {
            // ignore if missing
        }

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
                .setIcon(R.mipmap.ic_launcher_round)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showContentTypeDialog(langCode, langName, "recital");
                    } else if (which == 1) {
                        showContentTypeDialog(langCode, langName, "practice");
                    } else if (which == 2) {
                        showQuizTypeDialog(langCode, langName);
                    } else if (which == 3) {
                        openProgressScreen();
                    }
                })
                .show();
    }

    private void showContentTypeDialog(String langCode, String langName, String mode) {
        String[] options = {"Alphabet", "Numbers"};

        String title = (mode.equals("recital") ? "Recital Mode" : "Practice Mode")
                + " - " + langName;

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(R.mipmap.ic_launcher_round)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openAlphabetScreen(langCode, langName, mode);
                    } else if (which == 1) {
                        openNumbersScreen(langCode, langName, mode);
                    }
                })
                .show();
    }

    private void showQuizTypeDialog(String langCode, String langName) {
        String[] quizTypes = {
                "Letter/Number Quiz",
                "Number Sequencing",
                "Matching",
                "Mixed Mode"
        };

        new AlertDialog.Builder(this)
                .setTitle("Quiz Mode - " + langName)
                .setIcon(R.mipmap.ic_launcher_round)
                .setItems(quizTypes, (dialog, which) -> {
                    String quizType = "basic";
                    if (which == 0) {
                        quizType = "basic";
                    } else if (which == 1) {
                        quizType = "sequence";
                    } else if (which == 2) {
                        quizType = "matching";
                    } else if (which == 3) {
                        quizType = "mixed";
                    }
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

    private void openProgressScreen() {
        Intent intent = new Intent(MainActivity.this, ProgressActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Play exit sound when main activity is closing
        try {
            if (exitPlayer != null) {
                exitPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                });
                exitPlayer.start();
            }
        } catch (Exception e) {
            // ignore playback errors
        }

        if (startPlayer != null) {
            startPlayer.release();
            startPlayer = null;
        }
    }
}
