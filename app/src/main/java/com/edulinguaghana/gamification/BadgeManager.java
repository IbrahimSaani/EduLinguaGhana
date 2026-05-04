package com.edulinguaghana.gamification;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BadgeManager {
    private static final String PREFS = "gamification_prefs";
    private static final String KEY_BADGES = "badges";

    public static List<Badge> getAllBadges(Context ctx) {
        synchronized (BadgeManager.class) {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String j = p.getString(KEY_BADGES, null);
            List<Badge> out = new ArrayList<>();
            if (j == null) {
                out = generateDefaultBadges();
                saveBadges(ctx, out);
                return out;
            }
            try {
                JSONArray arr = new JSONArray(j);
                for (int i = 0; i < arr.length(); i++) out.add(Badge.fromJson(arr.getJSONObject(i)));
            } catch (JSONException e) {
                out = generateDefaultBadges();
                saveBadges(ctx, out);
            }
            return out;
        }
    }

    public static void saveBadges(Context ctx, List<Badge> badges) {
        synchronized (BadgeManager.class) {
            JSONArray arr = new JSONArray();
            for (Badge b : badges) arr.put(b.toJson());
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            // Use commit() instead of apply() for critical badge data to ensure synchronous write
            p.edit().putString(KEY_BADGES, arr.toString()).commit();
        }
    }

    private static List<Badge> generateDefaultBadges() {
        List<Badge> list = new ArrayList<>();

        // First practice badge
        Badge b1 = new Badge();
        b1.id = "first_practice";
        b1.title = "First Practice";
        b1.description = "Complete your first practice session.";
        b1.iconName = "ic_badge_first";
        b1.unlocked = false;
        b1.unlockedAt = 0;
        list.add(b1);

        // 7 Day Streak badge
        Badge b2 = new Badge();
        b2.id = "seven_days";
        b2.title = "7 Day Streak";
        b2.description = "Keep practicing 7 days in a row.";
        b2.iconName = "ic_badge_seven";
        b2.unlocked = false;
        b2.unlockedAt = 0;
        list.add(b2);

        // 30 Day Streak badge
        Badge b3 = new Badge();
        b3.id = "thirty_days";
        b3.title = "30 Day Streak";
        b3.description = "Practice for 30 consecutive days.";
        b3.iconName = "ic_badge_thirty";
        b3.unlocked = false;
        b3.unlockedAt = 0;
        list.add(b3);

        // Quiz Master badge
        Badge b4 = new Badge();
        b4.id = "quiz_master";
        b4.title = "Quiz Master";
        b4.description = "Complete 20 quizzes.";
        b4.iconName = "ic_badge_quiz";
        b4.unlocked = false;
        b4.unlockedAt = 0;
        list.add(b4);

        // Perfect Score badge
        Badge b5 = new Badge();
        b5.id = "perfect_score";
        b5.title = "Perfect Score";
        b5.description = "Get a perfect score in a quiz.";
        b5.iconName = "ic_badge_perfect";
        b5.unlocked = false;
        b5.unlockedAt = 0;
        list.add(b5);

        // Multilingual badge
        Badge b6 = new Badge();
        b6.id = "multilingual";
        b6.title = "Multilingual";
        b6.description = "Complete practice in 3 different languages.";
        b6.iconName = "ic_badge_languages";
        b6.unlocked = false;
        b6.unlockedAt = 0;
        list.add(b6);

        // Speed Champion badge
        Badge b7 = new Badge();
        b7.id = "speed_champion";
        b7.title = "Speed Champion";
        b7.description = "Win a speed challenge game.";
        b7.iconName = "ic_badge_speed";
        b7.unlocked = false;
        b7.unlockedAt = 0;
        list.add(b7);

        // Achievement Collector badge
        Badge b8 = new Badge();
        b8.id = "achievement_collector";
        b8.title = "Achievement Collector";
        b8.description = "Unlock 5 achievements.";
        b8.iconName = "ic_badge_achievements";
        b8.unlocked = false;
        b8.unlockedAt = 0;
        list.add(b8);

        return list;
    }

    public static void unlockBadge(Context ctx, String badgeId) {
        synchronized (BadgeManager.class) {
            List<Badge> list = getAllBadges(ctx);
            boolean changed = false;
            for (Badge b : list) {
                if (b.id.equals(badgeId) && !b.unlocked) {
                    b.unlocked = true;
                    b.unlockedAt = System.currentTimeMillis();
                    changed = true;
                    // grant xp for unlocking badge
                    XPManager.awardXP(ctx, 25, "badge:" + badgeId);
                }
            }
            if (changed) saveBadges(ctx, list);
        }
    }

    public static boolean isUnlocked(Context ctx, String badgeId) {
        synchronized (BadgeManager.class) {
            List<Badge> list = getAllBadges(ctx);
            for (Badge b : list) if (b.id.equals(badgeId)) return b.unlocked;
            return false;
        }
    }
}

