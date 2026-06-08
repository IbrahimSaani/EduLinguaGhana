package com.edulinguaghana;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private TextView tvStatHighScore, tvStatTotalQuizzes, tvStatTotalCorrect, tvStatTotalGames, tvStatAccuracy, tvAchievements, tvStatPerfectScores;
    private TextView tvCurrentLevelBadge, tvLevelName, tvXpToNextLevel, tvCurrentXP, tvTargetXP;
    private com.google.android.material.progressindicator.LinearProgressIndicator progressLevel;
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
        tvStatPerfectScores = findViewById(R.id.tvStatPerfectScores);
        tvStatTotalGames   = findViewById(R.id.tvStatTotalGames);
        tvStatAccuracy     = findViewById(R.id.tvStatAccuracy);
        tvAchievements     = findViewById(R.id.tvAchievements);
        btnCloseProgress   = findViewById(R.id.btnCloseProgress);
        btnShareProgress   = findViewById(R.id.btnShareProgress);
        progressAccuracy   = findViewById(R.id.progressAccuracy);
        tvAccuracyPercentage = findViewById(R.id.tvAccuracyPercentage);
        cardStats          = findViewById(R.id.cardStats);
        cardAccuracy       = findViewById(R.id.cardAccuracy);
        cardAchievements   = findViewById(R.id.cardAchievements);

        // Level views
        tvCurrentLevelBadge = findViewById(R.id.tvCurrentLevelBadge);
        tvLevelName         = findViewById(R.id.tvLevelName);
        tvXpToNextLevel     = findViewById(R.id.tvXpToNextLevel);
        tvCurrentXP         = findViewById(R.id.tvCurrentXP);
        tvTargetXP          = findViewById(R.id.tvTargetXP);
        progressLevel       = findViewById(R.id.progressLevel);

        // Show cards immediately (no animation)
        cardStats.setVisibility(View.VISIBLE);
        cardAccuracy.setVisibility(View.VISIBLE);
        cardAchievements.setVisibility(View.VISIBLE);

        // Set initial data
        final int highScore     = ProgressManager.getHighScore(this);
        final int totalQuizzes  = ProgressManager.getTotalQuizzes(this);
        final int totalGames    = com.edulinguaghana.gamification.FunGameProgressManager.getTotalFunGamesPlayed(this);
        final int totalCorrect  = ProgressManager.getTotalCorrect(this);
        final int perfectScores = ProgressManager.getPerfectScoresCount(this);
        final int percentage    = ProgressManager.getAccuracy(this);

        // Basic stats
        // Cap the high score display at 10 (quiz can have scores > 10 in time-limited mode)
        int displayHighScore = Math.min(highScore, 10);
        tvStatHighScore.setText(getString(R.string.stat_high_score, displayHighScore));
        tvStatTotalQuizzes.setText(getString(R.string.stat_total_quizzes, totalQuizzes));
        tvStatTotalCorrect.setText(getString(R.string.stat_total_correct, totalCorrect));
        if (tvStatPerfectScores != null) {
            tvStatPerfectScores.setText(getString(R.string.stat_perfect_scores, perfectScores));
        }
        if (tvStatTotalGames != null) {
            tvStatTotalGames.setText(getString(R.string.stat_total_games, totalGames));
        }
        tvStatAccuracy.setText(getString(R.string.stat_accuracy, percentage));
        progressAccuracy.setMax(100);

        // --- Set Level info ---
        com.edulinguaghana.gamification.XPState xpState = com.edulinguaghana.gamification.XPManager.getState(this);
        if (tvCurrentLevelBadge != null) tvCurrentLevelBadge.setText(String.valueOf(xpState.level));
        if (tvLevelName != null) tvLevelName.setText(com.edulinguaghana.gamification.XPManager.getLevelName(xpState.level));
        
        int xpRequired = com.edulinguaghana.gamification.XPManager.xpRequiredForLevel(xpState.level);
        if (progressLevel != null) {
            progressLevel.setMax(xpRequired);
            progressLevel.setProgressCompat(xpState.xpIntoLevel, true);
        }
        if (tvCurrentXP != null) tvCurrentXP.setText(xpState.xpIntoLevel + " XP");
        if (tvTargetXP != null) tvTargetXP.setText(xpRequired + " XP");
        if (tvXpToNextLevel != null) {
            int remaining = xpRequired - xpState.xpIntoLevel;
            tvXpToNextLevel.setText(getString(R.string.xp_to_next_level, remaining, xpState.level + 1));
        }

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
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
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
        final Handler handler = new Handler(Looper.getMainLooper());
        
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

        Handler handler = new Handler(Looper.getMainLooper());

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
        com.edulinguaghana.gamification.XPState xpState = com.edulinguaghana.gamification.XPManager.getState(this);
        int level = xpState.level;
        String levelName = com.edulinguaghana.gamification.XPManager.getLevelName(level);

        String achievementText;
        if (totalQuizzes == 0) {
            achievementText = "Level 1 – Beginner Explorer\n\n" +
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
                sb.append("🌱 You\'re just getting started. Study regularly to grow.\n");
            }

            sb.append("\nTotal correct answers so far: ").append(totalCorrect);

            int xpRequired = com.edulinguaghana.gamification.XPManager.xpRequiredForLevel(level);
            int remaining = xpRequired - xpState.xpIntoLevel;
            sb.append("\n\nNext level at ").append(xpRequired).append(" XP.\n");
            sb.append("Only ").append(remaining).append(" more to go!");

            achievementText = sb.toString();
        }

        if (tvAchievements != null) {
            tvAchievements.setText(achievementText);
        }
    }

    private void shareProgress(int highScore, int totalQuizzes, int totalCorrect, int percentage, String achievementText) {
        // Cap the high score at 10 for sharing (same as display)
        int displayHighScore = Math.min(highScore, 10);
        String shareText = "My EduLingua Ghana Progress:\n\n"
                + "Best quiz score: " + displayHighScore + " / 10\n"
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
