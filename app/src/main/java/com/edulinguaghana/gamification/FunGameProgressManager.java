package com.edulinguaghana.gamification;

import android.content.Context;
import android.content.SharedPreferences;

import com.edulinguaghana.AchievementManager;
import com.edulinguaghana.CloudSyncManager;
import com.edulinguaghana.ProgressManager;
import com.edulinguaghana.StreakManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tracks fun game progress (speed/puzzle/beat matcher) and feeds quests, badges, XP, and achievements.
 */
public final class FunGameProgressManager {
    private static final String PREF_NAME = "EduLinguaPrefs";

    private static final String KEY_TOTAL_FUN_GAMES = "TOTAL_FUN_GAMES";
    private static final String KEY_SPEED_GAMES_PLAYED = "SPEED_GAMES_PLAYED";
    private static final String KEY_PUZZLE_GAMES_PLAYED = "PUZZLE_GAMES_PLAYED";
    private static final String KEY_BEAT_GAMES_PLAYED = "BEAT_GAMES_PLAYED";
    private static final String KEY_FUN_GAME_BEST_SCORE = "FUN_GAME_BEST_SCORE";
    private static final String KEY_FUN_GAMES_PLAYED_SET = "FUN_GAMES_PLAYED_SET";

    private FunGameProgressManager() {
    }

    public static void recordGameCompleted(Context context, String gameId, int score, String languageCode) {
        if (context == null || gameId == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int total = prefs.getInt(KEY_TOTAL_FUN_GAMES, 0) + 1;
        int bestScore = Math.max(prefs.getInt(KEY_FUN_GAME_BEST_SCORE, 0), Math.max(0, score));

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_TOTAL_FUN_GAMES, total);
        editor.putInt(KEY_FUN_GAME_BEST_SCORE, bestScore);

        switch (gameId) {
            case "speed_game":
                editor.putInt(KEY_SPEED_GAMES_PLAYED, prefs.getInt(KEY_SPEED_GAMES_PLAYED, 0) + 1);
                break;
            case "puzzle_game":
                editor.putInt(KEY_PUZZLE_GAMES_PLAYED, prefs.getInt(KEY_PUZZLE_GAMES_PLAYED, 0) + 1);
                break;
            case "beat_matcher":
                editor.putInt(KEY_BEAT_GAMES_PLAYED, prefs.getInt(KEY_BEAT_GAMES_PLAYED, 0) + 1);
                break;
            default:
                break;
        }

        Set<String> existingSet = prefs.getStringSet(KEY_FUN_GAMES_PLAYED_SET, null);
        Set<String> gamesPlayed = existingSet == null ? new HashSet<>() : new HashSet<>(existingSet);
        gamesPlayed.add(gameId);
        editor.putStringSet(KEY_FUN_GAMES_PLAYED_SET, gamesPlayed);
        editor.apply();

        persistFunGameProgressToCloud(context, gameId, score, languageCode, total, bestScore, gamesPlayed);

        if (total >= 1) {
            unlockAchievementIfNeeded(context, "fun_game_1");
        }

        // Quests for fun game ecosystem
        QuestManager.progressQuest(context, "fun_game_daily", 1);
        QuestManager.progressQuest(context, "fun_game_explorer", gamesPlayed.size() >= 3 ? 1 : 0);
        QuestManager.progressQuest(context, "marathon_learner", 1);

        if ("speed_game".equals(gameId)) {
            QuestManager.progressQuest(context, "speed_game", 1);
            BadgeManager.unlockBadge(context, "speed_champion");
        } else if ("puzzle_game".equals(gameId)) {
            QuestManager.progressQuest(context, "puzzle_solver", 1);
        } else if ("beat_matcher".equals(gameId)) {
            QuestManager.progressQuest(context, "beat_master", 1);
        }

        // Badges based on cumulative fun game progress
        int puzzleCount = prefs.getInt(KEY_PUZZLE_GAMES_PLAYED, 0);
        int beatCount = prefs.getInt(KEY_BEAT_GAMES_PLAYED, 0);

        if (total >= 1) BadgeManager.unlockBadge(context, "fun_starter");
        if (total >= 10) BadgeManager.unlockBadge(context, "fun_legend");
        if (puzzleCount >= 5) BadgeManager.unlockBadge(context, "puzzle_pro");
        if (beatCount >= 5) BadgeManager.unlockBadge(context, "beat_expert");
        if (gamesPlayed.size() >= 3) BadgeManager.unlockBadge(context, "game_explorer");

        // XP, streak and language usage
        XPManager.awardXP(context, Math.max(5, Math.min(50, score / 2 + 8)), "fun_game:" + gameId);
        new StreakManager(context).recordPractice();
        if (languageCode != null && !languageCode.trim().isEmpty()) {
            ProgressManager.trackLanguageUsage(context, languageCode);
        }

        // Re-evaluate achievements after updating counters.
        new AchievementManager(context).checkAndUnlockAchievements();

        // Best-effort cloud persistence for cross-device continuity.
        try {
            CloudSyncManager cloudSyncManager = new CloudSyncManager(context);
            if (cloudSyncManager.canSync()) {
                cloudSyncManager.syncToCloud((success, message) -> {
                    // Silent: this runs in gameplay context, no UI interruption.
                });
            }
        } catch (Exception ignored) {
        }
    }

    public static int getTotalFunGamesPlayed(Context context) {
        if (context == null) return 0;
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_TOTAL_FUN_GAMES, 0);
    }

    public static int getBestFunGameScore(Context context) {
        if (context == null) return 0;
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_FUN_GAME_BEST_SCORE, 0);
    }

    // New helper getters for per-game counts so UI can display breakdowns.
    public static int getSpeedGamesPlayed(Context context) {
        if (context == null) return 0;
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_SPEED_GAMES_PLAYED, 0);
    }

    public static int getPuzzleGamesPlayed(Context context) {
        if (context == null) return 0;
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_PUZZLE_GAMES_PLAYED, 0);
    }

    public static int getBeatGamesPlayed(Context context) {
        if (context == null) return 0;
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_BEAT_GAMES_PLAYED, 0);
    }

    public static int getDistinctFunGamesPlayedCount(Context context) {
        if (context == null) return 0;
        Set<String> set = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getStringSet(KEY_FUN_GAMES_PLAYED_SET, null);
        return set == null ? 0 : set.size();
    }

    private static void persistFunGameProgressToCloud(Context context, String gameId, int score, String languageCode,
                                                      int totalFunGames, int bestScore, Set<String> gamesPlayed) {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

            Map<String, Object> funGamesUpdate = new HashMap<>();
            funGamesUpdate.put("userId", user.getUid());
            funGamesUpdate.put("totalFunGames", totalFunGames);
            funGamesUpdate.put("bestScore", bestScore);
            funGamesUpdate.put("funGameBestScore", bestScore);
            funGamesUpdate.put("speedGamesPlayed", getSpeedGamesPlayed(context));
            funGamesUpdate.put("puzzleGamesPlayed", getPuzzleGamesPlayed(context));
            funGamesUpdate.put("beatGamesPlayed", getBeatGamesPlayed(context));
            funGamesUpdate.put("distinctFunGamesPlayed", gamesPlayed.size());
            funGamesUpdate.put("funGamesPlayedSet", new ArrayList<>(gamesPlayed));
            funGamesUpdate.put("lastGameId", gameId);
            funGamesUpdate.put("lastScore", Math.max(0, score));
            funGamesUpdate.put("lastLanguageCode", languageCode != null ? languageCode : "");
            funGamesUpdate.put("updatedAt", System.currentTimeMillis());

            userRef.child("funGames").updateChildren(funGamesUpdate);

            Map<String, Object> progressUpdate = new HashMap<>();
            progressUpdate.put("userId", user.getUid());
            progressUpdate.put("totalFunGames", totalFunGames);
            progressUpdate.put("bestScore", bestScore);
            progressUpdate.put("funGameBestScore", bestScore);
            progressUpdate.put("speedGamesPlayed", getSpeedGamesPlayed(context));
            progressUpdate.put("puzzleGamesPlayed", getPuzzleGamesPlayed(context));
            progressUpdate.put("beatGamesPlayed", getBeatGamesPlayed(context));
            progressUpdate.put("distinctFunGamesPlayed", gamesPlayed.size());
            progressUpdate.put("funGamesPlayedSet", new ArrayList<>(gamesPlayed));
            progressUpdate.put("lastFunGameId", gameId);
            progressUpdate.put("lastFunGameScore", Math.max(0, score));
            progressUpdate.put("lastFunGameLanguage", languageCode != null ? languageCode : "");
            progressUpdate.put("lastFunGameAt", System.currentTimeMillis());

            userRef.child("progress").updateChildren(progressUpdate);
        } catch (Exception ignored) {
        }
    }

    private static void unlockAchievementIfNeeded(Context context, String achievementId) {
        try {
            AchievementManager achievementManager = new AchievementManager(context);
            java.util.List<com.edulinguaghana.Achievement> achievements = achievementManager.getAllAchievements();
            boolean changed = false;

            for (com.edulinguaghana.Achievement achievement : achievements) {
                if (achievement != null && achievementId.equals(achievement.getId()) && !achievement.isUnlocked()) {
                    achievement.unlock();
                    changed = true;
                    break;
                }
            }

            if (changed) {
                achievementManager.saveAllAchievements(achievements);
            }
        } catch (Exception ignored) {
        }
    }
}

