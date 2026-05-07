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
    private static final String KEY_LANGUAGES_USED = "LANGUAGES_USED"; // Track distinct languages for language_explorer quest

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

            // Progress other quiz-related quests
            QuestManager.progressQuest(context, "quiz_multiple", 1);         // Quest 5 - Complete 2 quizzes
            QuestManager.progressQuest(context, "marathon_learner", 1);      // Quest 8 - accumulate all activities

            // Check for perfect score achievement
            if (score == 100) {
                com.edulinguaghana.gamification.BadgeManager.unlockBadge(context, "perfect_score");
            }

            // Check for quiz master badge (20 quizzes completed)
            // Note: totalQuizzes was incremented above, so check if it reaches 20
            if (totalQuizzes + 1 >= 20) {
                com.edulinguaghana.gamification.BadgeManager.unlockBadge(context, "quiz_master");
            }
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

    // Update progress with language tracking for language_explorer quest
    public static void updateProgressWithLanguage(Context context, String mode, int score, int correctCount, String languageCode) {
        updateProgress(context, mode, score, correctCount, 10); // Default 10 questions

        // Track language usage for language_explorer quest
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String languagesUsedStr = prefs.getString(KEY_LANGUAGES_USED, "");

            // Parse existing languages
            java.util.Set<String> languagesUsed = new java.util.HashSet<>();
            if (!languagesUsedStr.isEmpty()) {
                languagesUsed.addAll(java.util.Arrays.asList(languagesUsedStr.split(",")));
            }

            // Add current language
            boolean isNewLanguage = languagesUsed.add(languageCode);

            // Save updated languages
            String updatedLanguages = String.join(",", languagesUsed);
            prefs.edit().putString(KEY_LANGUAGES_USED, updatedLanguages).apply();

            // If new language is added, progress the quest
            if (isNewLanguage && languagesUsed.size() <= 2) {
                QuestManager.progressQuest(context, "language_explorer", 1); // Quest 7
            }
        } catch (Exception ignored) { }
    }

    // Track distinct languages used for language_explorer quest
    public static void trackLanguageUsage(Context context, String languageCode) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String languagesUsedStr = prefs.getString(KEY_LANGUAGES_USED, "");
            
            // Parse existing languages
            java.util.Set<String> languagesUsed = new java.util.HashSet<>();
            if (!languagesUsedStr.isEmpty()) {
                languagesUsed.addAll(java.util.Arrays.asList(languagesUsedStr.split(",")));
            }
            
            // Add current language
            boolean isNewLanguage = languagesUsed.add(languageCode);
            
            // Save updated languages
            String updatedLanguages = String.join(",", languagesUsed);
            prefs.edit().putString(KEY_LANGUAGES_USED, updatedLanguages).apply();
            
            // If new language is added, progress the quest
            if (isNewLanguage && languagesUsed.size() <= 2) {
                com.edulinguaghana.gamification.QuestManager.progressQuest(context, "language_explorer", 1); // Quest 7
            }

            // Unlock multilingual badge when 3 languages are practiced
            if (languagesUsed.size() >= 3) {
                com.edulinguaghana.gamification.BadgeManager.unlockBadge(context, "multilingual");
            }
        } catch (Exception ignored) { }
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
