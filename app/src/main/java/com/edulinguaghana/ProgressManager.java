package com.edulinguaghana;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.edulinguaghana.gamification.XPManager;
import com.edulinguaghana.gamification.QuestManager;
import com.edulinguaghana.tracking.ProgressTracker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProgressManager {

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";
    private static final String KEY_TOTAL_QUIZZES = "TOTAL_QUIZZES";
    private static final String KEY_TOTAL_CORRECT = "TOTAL_CORRECT";

    // Update global progress stats
    public static void updateProgress(Context context, String mode, int score, int correctCount) {
        updateProgress(context, mode, score, correctCount, 10); // Default 10 questions
    }

    // Update global progress stats with total questions parameter
    public static void updateProgress(Context context, String mode, int score, int correctCount, int totalQuestions) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int prevHighScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        int totalQuizzes = prefs.getInt(KEY_TOTAL_QUIZZES, 0);
        int totalCorrect = prefs.getInt(KEY_TOTAL_CORRECT, 0);

        // Update stats
        if (score > prevHighScore) {
            editor.putInt(KEY_HIGH_SCORE, score);
            Toast.makeText(context, "🎉 New High Score in " + mode + "!", Toast.LENGTH_SHORT).show();
        }

        editor.putInt(KEY_TOTAL_QUIZZES, totalQuizzes + 1);
        editor.putInt(KEY_TOTAL_CORRECT, totalCorrect + correctCount);
        editor.apply();

        // --- Gamification: award XP and progress quests ---
        try {
            int xpAward = Math.max(5, correctCount * 2 + score / 5); // conservative formula
            XPManager.awardXP(context, xpAward, "quiz_complete");
            // progress the daily_quiz quest by 1
            QuestManager.progressQuest(context, "daily_quiz", 1);
        } catch (Exception ignored) { }

        // --- Real-time Progress Tracking: Log to Firebase ---
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                ProgressTracker tracker = new ProgressTracker();
                tracker.logQuizCompletion(context, user.getUid(), mode, score,
                                        correctCount, totalQuestions, 0, null);
            }
        } catch (Exception e) {
            // Silently fail if Firebase is not available
            android.util.Log.e("ProgressManager", "Failed to log to Firebase", e);
        }
    }

    // Get total quizzes taken
    public static int getTotalQuizzes(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_TOTAL_QUIZZES, 0);
    }

    // Get total correct answers
    public static int getTotalCorrect(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_TOTAL_CORRECT, 0);
    }

    // Get highest score
    public static int getHighScore(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_HIGH_SCORE, 0);
    }

    // Get accuracy percentage
    public static int getAccuracy(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int correct = prefs.getInt(KEY_TOTAL_CORRECT, 0);
        int quizzes = prefs.getInt(KEY_TOTAL_QUIZZES, 0);
        int totalQuestions = quizzes * 10; // assuming 10 per quiz

        if (totalQuestions == 0) return 0;
        return (int) Math.round((correct * 100.0) / totalQuestions);
    }

    // Clear progress (optional)
    public static void resetProgress(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
