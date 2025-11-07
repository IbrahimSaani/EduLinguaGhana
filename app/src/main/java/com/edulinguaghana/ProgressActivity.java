package com.edulinguaghana;  // <-- your package

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressActivity extends AppCompatActivity {

    private TextView tvStatHighScore, tvStatTotalQuizzes, tvStatTotalCorrect, tvStatAccuracy, tvAchievements;
    private Button btnCloseProgress, btnShareProgress;
    private ProgressBar progressAccuracy;

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
        tvAchievements = findViewById(R.id.tvAchievements);
        btnCloseProgress = findViewById(R.id.btnCloseProgress);
        btnShareProgress = findViewById(R.id.btnShareProgress);
        progressAccuracy = findViewById(R.id.progressAccuracy);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        final int highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        final int totalQuizzes = prefs.getInt(KEY_TOTAL_QUIZZES, 0);
        final int totalCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0);

        tvStatHighScore.setText("Best quiz score: " + highScore + " / 10");
        tvStatTotalQuizzes.setText("Total quizzes taken: " + totalQuizzes);
        tvStatTotalCorrect.setText("Total correct answers: " + totalCorrect);

        // Calculate percentage
        int percentage = 0;
        if (totalQuizzes > 0 && totalCorrect > 0) {
            int totalQuestions = totalQuizzes * 10;
            percentage = (int) Math.round((totalCorrect * 100.0) / totalQuestions);
        }

        // Make a final copy so it can be used in the lambda
        final int finalPercentage = percentage;

        tvStatAccuracy.setText("Overall accuracy: " + finalPercentage + "%");
        progressAccuracy.setMax(100);
        progressAccuracy.setProgress(finalPercentage);

        String achievementText;
        if (totalQuizzes == 0) {
            achievementText = "Achievements: Start a quiz to unlock badges!";
        } else if (finalPercentage == 100) {
            achievementText = "Achievements: ⭐ Perfect learner (100% accuracy)!";
        } else if (finalPercentage >= 80) {
            achievementText = "Achievements: 🏅 Excellent accuracy (80%+).";
        } else if (finalPercentage >= 50) {
            achievementText = "Achievements: 🎓 Good effort. Keep practicing!";
        } else {
            achievementText = "Achievements: 🌱 Beginner. Practice more to unlock badges.";
        }
        tvAchievements.setText(achievementText);

        btnCloseProgress.setOnClickListener(v -> finish());

        btnShareProgress.setOnClickListener(v -> {
            String shareText = "EduLingua Ghana Progress:\n\n"
                    + "Best quiz score: " + highScore + " / 10\n"
                    + "Total quizzes taken: " + totalQuizzes + "\n"
                    + "Total correct answers: " + totalCorrect + "\n"
                    + "Overall accuracy: " + finalPercentage + "%\n\n"
                    + achievementText;

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            Intent shareIntent = Intent.createChooser(sendIntent, "Share progress via");
            startActivity(shareIntent);
        });
    }
}
