package com.edulinguaghana;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AchievementManager {
    private static final String PREF_NAME = "AchievementsPrefs";
    private static final String KEY_ACHIEVEMENTS = "ACHIEVEMENTS_LIST";

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private List<Achievement> achievements;

    public AchievementManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        initializeAchievements();
    }

    private void initializeAchievements() {
        // Load saved achievements
        String json = prefs.getString(KEY_ACHIEVEMENTS, null);
        if (json != null) {
            Type type = new TypeToken<List<Achievement>>() {}.getType();
            achievements = gson.fromJson(json, type);

            // Update icons for existing achievements to use new professional icons
            boolean updated = false;
            List<Achievement> defaults = createDefaultAchievements();
            for (Achievement a : achievements) {
                for (Achievement def : defaults) {
                    if (def.getId().equals(a.getId())) {
                        if (a.getIconName() == null || !a.getIconName().equals(def.getIconName())) {
                            a.setIconName(def.getIconName());
                            updated = true;
                        }
                        break;
                    }
                }
            }
            if (updated) {
                saveAchievements();
            }
        } else {
            // Create default achievements
            achievements = createDefaultAchievements();
            saveAchievements();
        }
    }

    private List<Achievement> createDefaultAchievements() {
        List<Achievement> list = new ArrayList<>();

        // Quiz Count Achievements
        list.add(new Achievement("first_quiz", "First Steps", "Complete your first quiz", "🎯", "ic_achievement_star_bronze", Achievement.AchievementType.QUIZ_COUNT, 1));
        list.add(new Achievement("quiz_5", "Getting Started", "Complete 5 quizzes", "⭐", "ic_achievement_book", Achievement.AchievementType.QUIZ_COUNT, 5));
        list.add(new Achievement("quiz_10", "Dedicated Learner", "Complete 10 quizzes", "🌟", "ic_achievement_star_silver", Achievement.AchievementType.QUIZ_COUNT, 10));
        list.add(new Achievement("quiz_25", "Quarter Century", "Complete 25 quizzes", "🏆", "ic_achievement_trophy", Achievement.AchievementType.QUIZ_COUNT, 25));
        list.add(new Achievement("quiz_50", "Half Century", "Complete 50 quizzes", "🎖️", "ic_achievement_medal", Achievement.AchievementType.QUIZ_COUNT, 50));
        list.add(new Achievement("quiz_100", "Century Club", "Complete 100 quizzes", "👑", "ic_achievement_crown", Achievement.AchievementType.QUIZ_COUNT, 100));

        // High Score Achievements
        list.add(new Achievement("score_50", "Half Way There", "Score 50 or more points", "💪", "ic_achievement_bolt_bronze", Achievement.AchievementType.HIGH_SCORE, 50));
        list.add(new Achievement("score_75", "Outstanding", "Score 75 or more points", "⚡", "ic_achievement_bolt_silver", Achievement.AchievementType.HIGH_SCORE, 75));
        list.add(new Achievement("score_90", "Almost Perfect", "Score 90 or more points", "💎", "ic_achievement_diamond", Achievement.AchievementType.HIGH_SCORE, 90));

        // Perfect Score Achievements
        list.add(new Achievement("perfect_1", "Perfectionist", "Get your first perfect score", "✨", "ic_achievement_medal_silver", Achievement.AchievementType.PERFECT_SCORE, 1));
        list.add(new Achievement("perfect_5", "Master", "Get 5 perfect scores", "🏅", "ic_achievement_medal", Achievement.AchievementType.PERFECT_SCORE, 5));

        // Streak Achievements
        list.add(new Achievement("streak_3", "Consistent", "Maintain a 3-day streak", "🔥", "ic_achievement_fire_bronze", Achievement.AchievementType.STREAK, 3));
        list.add(new Achievement("streak_7", "Week Warrior", "Maintain a 7-day streak", "🔥🔥", "ic_achievement_fire_silver", Achievement.AchievementType.STREAK, 7));
        list.add(new Achievement("streak_14", "Two Week Hero", "Maintain a 14-day streak", "🔥🔥🔥", "ic_achievement_fire", Achievement.AchievementType.STREAK, 14));
        list.add(new Achievement("streak_30", "Month Master", "Maintain a 30-day streak", "🏆🔥", "ic_achievement_shield", Achievement.AchievementType.STREAK, 30));

        // Accuracy Achievements
        list.add(new Achievement("accuracy_75", "Accurate", "Reach 75% accuracy", "🎯", "ic_achievement_target_bronze", Achievement.AchievementType.ACCURACY, 75));
        list.add(new Achievement("accuracy_85", "Sharpshooter", "Reach 85% accuracy", "🎯🎯", "ic_achievement_target_silver", Achievement.AchievementType.ACCURACY, 85));
        list.add(new Achievement("accuracy_95", "Sniper", "Reach 95% accuracy", "🎯🎯🎯", "ic_achievement_target", Achievement.AchievementType.ACCURACY, 95));

        return list;
    }

    private void saveAchievements() {
        String json = gson.toJson(achievements);
        prefs.edit().putString(KEY_ACHIEVEMENTS, json).apply();
    }

    public List<Achievement> getAllAchievements() {
        return new ArrayList<>(achievements);
    }

    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> unlocked = new ArrayList<>();
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                unlocked.add(achievement);
            }
        }
        return unlocked;
    }

    public int getUnlockedCount() {
        return getUnlockedAchievements().size();
    }

    public int getTotalCount() {
        return achievements.size();
    }

    // Check and unlock achievements based on progress
    public void checkAndUnlockAchievements() {
        ProgressManager progressManager = new ProgressManager();
        StreakManager streakManager = new StreakManager(context);

        int totalQuizzes = progressManager.getTotalQuizzes(context);
        int highScore = progressManager.getHighScore(context);
        int accuracy = progressManager.getAccuracy(context);
        int currentStreak = streakManager.getCurrentStreak();
        int totalCorrect = progressManager.getTotalCorrect(context);

        boolean newUnlock = false;
        NotificationManager notificationManager = new NotificationManager(context);

        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                continue; // Already unlocked
            }

            boolean shouldUnlock = false;

            switch (achievement.getType()) {
                case QUIZ_COUNT:
                    shouldUnlock = totalQuizzes >= achievement.getRequiredValue();
                    break;
                case HIGH_SCORE:
                    shouldUnlock = highScore >= achievement.getRequiredValue();
                    break;
                case PERFECT_SCORE:
                    // Count perfect scores (would need tracking, simplified here)
                    shouldUnlock = highScore == 100 && achievement.getRequiredValue() == 1;
                    break;
                case STREAK:
                    shouldUnlock = currentStreak >= achievement.getRequiredValue();
                    break;
                case ACCURACY:
                    shouldUnlock = accuracy >= achievement.getRequiredValue();
                    break;
            }

            if (shouldUnlock) {
                achievement.unlock();
                newUnlock = true;

                // Send notification
                notificationManager.addNotification(
                    "Achievement Unlocked! " + achievement.getEmoji(),
                    achievement.getTitle() + ": " + achievement.getDescription(),
                    achievement.getEmoji(),
                    Notification.NotificationType.ACHIEVEMENT
                );
            }
        }

        if (newUnlock) {
            saveAchievements();
        }
    }
}

