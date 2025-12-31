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

    public static void saveBadges(Context ctx, List<Badge> badges) {
        JSONArray arr = new JSONArray();
        for (Badge b : badges) arr.put(b.toJson());
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        p.edit().putString(KEY_BADGES, arr.toString()).apply();
    }

    private static List<Badge> generateDefaultBadges() {
        List<Badge> list = new ArrayList<>();
        Badge b1 = new Badge();
        b1.id = "first_practice";
        b1.title = "First Practice";
        b1.description = "Complete your first practice session.";
        b1.iconName = "ic_badge_first";
        b1.unlocked = false;
        b1.unlockedAt = 0;
        list.add(b1);

        Badge b2 = new Badge();
        b2.id = "seven_days";
        b2.title = "7 Day Streak";
        b2.description = "Keep practicing 7 days in a row.";
        b2.iconName = "ic_badge_seven";
        b2.unlocked = false;
        b2.unlockedAt = 0;
        list.add(b2);

        return list;
    }

    public static void unlockBadge(Context ctx, String badgeId) {
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

    public static boolean isUnlocked(Context ctx, String badgeId) {
        List<Badge> list = getAllBadges(ctx);
        for (Badge b : list) if (b.id.equals(badgeId)) return b.unlocked;
        return false;
    }
}

