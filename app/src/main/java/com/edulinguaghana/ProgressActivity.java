package com.edulinguaghana;

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

        tvStatHighScore    = findViewById(R.id.tvStatHighScore);
        tvStatTotalQuizzes = findViewById(R.id.tvStatTotalQuizzes);
        tvStatTotalCorrect = findViewById(R.id.tvStatTotalCorrect);
        tvStatAccuracy     = findViewById(R.id.tvStatAccuracy);
        tvAchievements     = findViewById(R.id.tvAchievements);
        btnCloseProgress   = findViewById(R.id.btnCloseProgress);
        btnShareProgress   = findViewById(R.id.btnShareProgress);
        progressAccuracy   = findViewById(R.id.progressAccuracy);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        final int highScore     = prefs.getInt(KEY_HIGH_SCORE, 0);
        final int totalQuizzes  = prefs.getInt(KEY_TOTAL_QUIZZES, 0);
        final int totalCorrect  = prefs.getInt(KEY_TOTAL_CORRECT, 0);

        // Basic stats
        tvStatHighScore.setText("Best quiz score: " + highScore + " / 10");
        tvStatTotalQuizzes.setText("Total quizzes taken: " + totalQuizzes);
        tvStatTotalCorrect.setText("Total correct answers: " + totalCorrect);

        // Accuracy calculation
        int percentage = 0;
        int totalQuestionsAttempted = totalQuizzes * 10;
        if (totalQuestionsAttempted > 0 && totalCorrect > 0) {
            percentage = (int) Math.round((totalCorrect * 100.0) / totalQuestionsAttempted);
        }

        tvStatAccuracy.setText("Overall accuracy: " + percentage + "%");
        progressAccuracy.setMax(100);
        progressAccuracy.setProgress(percentage);

        // --- SIMPLE LEVEL SYSTEM ---
        // Level 1:  0–19 correct
        // Level 2: 20–39 correct
        // Level 3: 40–69 correct
        // Level 4: 70–99 correct
        // Level 5: 100+ correct
        int level = 1;
        if (totalCorrect >= 100)      level = 5;
        else if (totalCorrect >= 70)  level = 4;
        else if (totalCorrect >= 40)  level = 3;
        else if (totalCorrect >= 20)  level = 2;

        String levelName;
        switch (level) {
            case 1:
                levelName = "Beginner Linguist";
                break;
            case 2:
                levelName = "Rising Speaker";
                break;
            case 3:
                levelName = "Confident Learner";
                break;
            case 4:
                levelName = "Fluent Explorer";
                break;
            case 5:
            default:
                levelName = "EduLingua Champion";
                break;
        }

        // Next level hint
        int nextLevelTarget;
        if (level >= 5) {
            nextLevelTarget = -1; // maxed
        } else if (level == 4) {
            nextLevelTarget = 100;
        } else if (level == 3) {
            nextLevelTarget = 70;
        } else if (level == 2) {
            nextLevelTarget = 40;
        } else {
            nextLevelTarget = 20;
        }

        String achievementText;
        if (totalQuizzes == 0) {
            achievementText = "Level 1 – Beginner Linguist\n\n" +
                    "Start your first quiz to begin your EduLingua Ghana journey!";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Level ").append(level).append(" – ").append(levelName).append("\n\n");

            if (percentage == 100) {
                sb.append("⭐ Perfect accuracy! You're mastering the content.\n");
            } else if (percentage >= 80) {
                sb.append("🏅 Excellent accuracy (80%+). Great job!\n");
            } else if (percentage >= 50) {
                sb.append("🎓 Good effort. Keep practicing to improve.\n");
            } else {
                sb.append("🌱 You're just getting started. Practice regularly to grow.\n");
            }

            sb.append("\nTotal correct answers so far: ").append(totalCorrect);

            if (nextLevelTarget > 0) {
                int remaining = nextLevelTarget - totalCorrect;
                if (remaining > 0) {
                    sb.append("\n\nNext level at ").append(nextLevelTarget)
                            .append(" correct answers.\nOnly ")
                            .append(remaining).append(" more to go!");
                }
            } else {
                sb.append("\n\nYou’ve reached the highest level – keep revising to stay sharp!");
            }

            achievementText = sb.toString();
        }

        tvAchievements.setText(achievementText);

        btnCloseProgress.setOnClickListener(v -> finish());

        // Make a final copy of achievementText for use inside the lambda
        final String shareAchievementText = achievementText;
        final int finalPercentage = percentage;

        btnShareProgress.setOnClickListener(v -> {
            String shareText = "My EduLingua Ghana Progress:\n\n"
                    + "Best quiz score: " + highScore + " / 10\n"
                    + "Total quizzes taken: " + totalQuizzes + "\n"
                    + "Total correct answers: " + totalCorrect + "\n"
                    + "Overall accuracy: " + finalPercentage + "%\n\n"
                    + shareAchievementText;

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            Intent shareIntent = Intent.createChooser(sendIntent, "Share my EduLingua progress");
            startActivity(shareIntent);
        });
    }
}
