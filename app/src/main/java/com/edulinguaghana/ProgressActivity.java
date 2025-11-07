package com.edulinguaghana;  // <-- your package

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ProgressActivity extends AppCompatActivity {

    private TextView tvStatHighScore, tvStatTotalQuizzes, tvStatTotalCorrect, tvStatAccuracy;
    private Button btnCloseProgress;

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";
    private static final String KEY_TOTAL_QUIZZES = "TOTAL_QUIZZES";
    private static final String KEY_TOTAL_CORRECT = "TOTAL_CORRECT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        tvStatHighScore = findViewById(R.id.tvStatHighScore);
        tvStatTotalQuizzes = findViewById(R.id.tvStatTotalQuizzes);
        tvStatTotalCorrect = findViewById(R.id.tvStatTotalCorrect);
        tvStatAccuracy = findViewById(R.id.tvStatAccuracy);
        btnCloseProgress = findViewById(R.id.btnCloseProgress);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        int totalQuizzes = prefs.getInt(KEY_TOTAL_QUIZZES, 0);
        int totalCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0);

        tvStatHighScore.setText("Best quiz score: " + highScore + " / 10");
        tvStatTotalQuizzes.setText("Total quizzes taken: " + totalQuizzes);
        tvStatTotalCorrect.setText("Total correct answers: " + totalCorrect);

        String accuracyText;
        if (totalQuizzes == 0 || totalCorrect == 0) {
            accuracyText = "Overall accuracy: 0%";
        } else {
            int totalQuestions = totalQuizzes * 10;
            int percentage = (int) Math.round((totalCorrect * 100.0) / totalQuestions);
            accuracyText = "Overall accuracy: " + percentage + "%";
        }
        tvStatAccuracy.setText(accuracyText);

        btnCloseProgress.setOnClickListener(v -> finish());
    }
}
