package com.edulinguaghana;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class ProgressManager {

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";
    private static final String KEY_TOTAL_QUIZZES = "TOTAL_QUIZZES";
    private static final String KEY_TOTAL_CORRECT = "TOTAL_CORRECT";

    // Update global progress stats
    public static void updateProgress(Context context, String mode, int score, int correctCount) {
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
