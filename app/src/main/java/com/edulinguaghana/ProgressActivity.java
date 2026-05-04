package com.edulinguaghana;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class ProgressActivity extends AppCompatActivity {

    private static final String TAG = "ProgressActivity";
    private TextView tvStatHighScore, tvStatTotalQuizzes, tvStatTotalCorrect, tvStatAccuracy, tvAchievements;
    private MaterialButton btnCloseProgress, btnShareProgress;
    private CircularProgressIndicator progressAccuracy;
    private MaterialToolbar toolbar;
    private MaterialCardView cardStats, cardAccuracy, cardAchievements;
    private MediaPlayer sfxPlayer;
    private TextView tvAccuracyPercentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Find views
        tvStatHighScore    = findViewById(R.id.tvStatHighScore);
        tvStatTotalQuizzes = findViewById(R.id.tvStatTotalQuizzes);
        tvStatTotalCorrect = findViewById(R.id.tvStatTotalCorrect);
        tvStatAccuracy     = findViewById(R.id.tvStatAccuracy);
        tvAchievements     = findViewById(R.id.tvAchievements);
        btnCloseProgress   = findViewById(R.id.btnCloseProgress);
        btnShareProgress   = findViewById(R.id.btnShareProgress);
        progressAccuracy   = findViewById(R.id.progressAccuracy);
        tvAccuracyPercentage = findViewById(R.id.tvAccuracyPercentage);
        cardStats          = findViewById(R.id.cardStats);
        cardAccuracy       = findViewById(R.id.cardAccuracy);
        cardAchievements   = findViewById(R.id.cardAchievements);

        // Show cards immediately (no animation)
        cardStats.setVisibility(View.VISIBLE);
        cardAccuracy.setVisibility(View.VISIBLE);
        cardAchievements.setVisibility(View.VISIBLE);

        // Set initial data
        final int highScore     = ProgressManager.getHighScore(this);
        final int totalQuizzes  = ProgressManager.getTotalQuizzes(this);
        final int totalCorrect  = ProgressManager.getTotalCorrect(this);
        final int percentage    = ProgressManager.getAccuracy(this);

        // Basic stats
        // Cap the high score display at 10 (quiz can have scores > 10 in time-limited mode)
        int displayHighScore = Math.min(highScore, 10);
        tvStatHighScore.setText("Best quiz score: " + displayHighScore + " / 10");
        tvStatTotalQuizzes.setText("Total quizzes taken: " + totalQuizzes);
        tvStatTotalCorrect.setText("Total correct answers: " + totalCorrect);
        tvStatAccuracy.setText("Overall accuracy: " + percentage + "%");
        progressAccuracy.setMax(100);

        // --- Set progress immediately (no animation) ---
        Log.d(TAG, "onCreate: Skipping animations.");
        progressAccuracy.setProgressCompat(percentage, true);
        tvAccuracyPercentage.setText(percentage + "%");

        // --- Set achievement text ---
        setupAchievements(totalCorrect, totalQuizzes, percentage);

        // --- Button listeners ---
        btnCloseProgress.setOnClickListener(v -> finish());
        btnShareProgress.setOnClickListener(v -> {
            shareProgress(highScore, totalQuizzes, totalCorrect, percentage, tvAchievements.getText().toString());
        });
    }

    private void animateProgress(int targetProgress) {
        Log.d(TAG, "animateProgress: Animating to " + targetProgress);
        new Handler().postDelayed(() -> {
            Log.d(TAG, "animateProgress: Handler running for progress bar.");
            // Animate the progress bar with a smooth progression
            animateProgressValue(targetProgress);
            
            // Add pulse effect to the progress bar
            try {
                Animation pulse = AnimationUtils.loadAnimation(this, R.anim.level_up_pulse);
                progressAccuracy.startAnimation(pulse);
            } catch (Exception ignored) {}
        }, 300);
    }

    private void animateProgressValue(int targetProgress) {
        final int animationDuration = 1000; // 1 second animation
        final long startTime = System.currentTimeMillis();
        final Handler handler = new Handler();
        
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                int progress = (int) ((targetProgress * elapsed) / animationDuration);
                
                if (progress < targetProgress) {
                    progressAccuracy.setProgressCompat(progress, true);
                    tvAccuracyPercentage.setText(progress + "%");
                    handler.postDelayed(this, 16); // 60fps
                } else {
                    progressAccuracy.setProgressCompat(targetProgress, true);
                    tvAccuracyPercentage.setText(targetProgress + "%");
                }
            }
        });
    }

    private void animateCards() {
        Log.d(TAG, "animateCards: Preparing card animations.");
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        Animation levelUp = AnimationUtils.loadAnimation(this, R.anim.level_up_pulse);
        Animation sparkle = AnimationUtils.loadAnimation(this, R.anim.sparkle_bounce);

        Handler handler = new Handler();

        // Stats card - slide in and glow
        startAnimation(handler, cardStats, slideIn, 0);
        handler.postDelayed(() -> {
            try {
                cardStats.startAnimation(levelUp);
            } catch (Exception ignored) {}
        }, 300);

        // Accuracy card - slide in and pulse
        startAnimation(handler, cardAccuracy, slideIn, 150);
        handler.postDelayed(() -> {
            try {
                Animation glow = AnimationUtils.loadAnimation(this, R.anim.glow_pulse);
                cardAccuracy.startAnimation(glow);
            } catch (Exception ignored) {}
        }, 450);

        // Achievements card - slide in with sparkle for celebration
        startAnimation(handler, cardAchievements, slideIn, 300);
        handler.postDelayed(() -> {
            try {
                cardAchievements.startAnimation(sparkle);
            } catch (Exception ignored) {}
        }, 600);
    }

    private void startAnimation(Handler handler, final View view, final Animation animation, int delay) {
        handler.postDelayed(() -> {
            Log.d(TAG, "startAnimation: Animating view with ID: " + view.getResources().getResourceEntryName(view.getId()) + " after " + delay + "ms");
            view.setVisibility(View.VISIBLE);
            view.startAnimation(animation);
        }, delay);
    }

    private void setupAchievements(int totalCorrect, int totalQuizzes, int percentage) {
        int level = 1;
        if (totalCorrect >= 100)      level = 5;
        else if (totalCorrect >= 70)  level = 4;
        else if (totalCorrect >= 40)  level = 3;
        else if (totalCorrect >= 20)  level = 2;

        String levelName;
        switch (level) {
            case 1: levelName = "Beginner Linguist"; break;
            case 2: levelName = "Rising Speaker"; break;
            case 3: levelName = "Confident Learner"; break;
            case 4: levelName = "Fluent Explorer"; break;
            case 5: default: levelName = "EduLingua Champion"; break;
        }

        int nextLevelTarget;
        if (level >= 5) { nextLevelTarget = -1; }
        else if (level == 4) { nextLevelTarget = 100; }
        else if (level == 3) { nextLevelTarget = 70; }
        else if (level == 2) { nextLevelTarget = 40; }
        else { nextLevelTarget = 20; }

        String achievementText;
        if (totalQuizzes == 0) {
            achievementText = "Level 1 – Beginner Linguist\n\n" +
                    "Start your first quiz to begin your EduLingua Ghana journey!";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Level ").append(level).append(" – ").append(levelName).append("\n\n");

            if (percentage == 100) {
                sb.append("⭐ Perfect accuracy! You\'re mastering the content.\n");
            } else if (percentage >= 80) {
                sb.append("🏅 Excellent accuracy (80%+). Great job!\n");
            } else if (percentage >= 50) {
                sb.append("🎓 Good effort. Keep practicing to improve.\n");
            } else {
                sb.append("🌱 You\'re just getting started. Practice regularly to grow.\n");
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
    }

    private void shareProgress(int highScore, int totalQuizzes, int totalCorrect, int percentage, String achievementText) {
        String shareText = "My EduLingua Ghana Progress:\n\n"
                + "Best quiz score: " + highScore + " / 10\n"
                + "Total quizzes taken: " + totalQuizzes + "\n"
                + "Total correct answers: " + totalCorrect + "\n"
                + "Overall accuracy: " + percentage + "%\n\n"
                + achievementText;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        Intent shareIntent = Intent.createChooser(sendIntent, "Share my EduLingua progress");
        startActivity(shareIntent);
    }

    private void playShareAnimation() {
        try {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_pop);
            btnShareProgress.startAnimation(bounce);
            Toast.makeText(this, "Progress shared! 🎉", Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {}
    }

    private void playSfx(boolean isCorrect) {
        try {
            if (sfxPlayer != null) {
                sfxPlayer.release();
                sfxPlayer = null;
            }
            int resId = isCorrect ? R.raw.correct : R.raw.wrong;
            sfxPlayer = MediaPlayer.create(this, resId);
            if (sfxPlayer != null) {
                sfxPlayer.setVolume(0.5f, 0.5f);
                sfxPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    sfxPlayer = null;
                });
                sfxPlayer.start();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sfxPlayer != null) {
            sfxPlayer.release();
            sfxPlayer = null;
        }
    }
}
