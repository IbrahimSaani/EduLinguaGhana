package com.edulinguaghana.gamification;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class XPManager {
    private static final String PREFS = "gamification_prefs";
    private static final String KEY_XP = "xp_state";

    private static final Set<XPListener> listeners = new HashSet<>();

    public interface XPListener {
        void onXpChanged(XPState state);
        void onLevelUp(int newLevel);
    }

    public static XPState getState(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String j = p.getString(KEY_XP, null);
        if (j == null) return new XPState();
        try {
            return XPState.fromJson(new JSONObject(j));
        } catch (JSONException e) {
            return new XPState();
        }
    }

    public static void saveState(Context ctx, XPState s) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        p.edit().putString(KEY_XP, s.toJson().toString()).apply();
    }

    public static void awardXP(Context ctx, int amount, String reason) {
        if (amount <= 0) return;
        XPState s = getState(ctx);
        s.totalXp += amount;
        // compute level and xpIntoLevel
        int newLevel = getLevelForXp(s.totalXp);
        int xpForCurrentLevel = xpRequiredForLevel(newLevel);
        int xpForPrevLevels = totalXpForLevelsBelow(newLevel);
        s.level = newLevel;
        s.xpIntoLevel = s.totalXp - xpForPrevLevels;
        s.lastUpdated = System.currentTimeMillis();
        saveState(ctx, s);

        // notify listeners on main thread
        Handler h = new Handler(Looper.getMainLooper());
        h.post(() -> {
            for (XPListener l : listeners) {
                try { l.onXpChanged(s); } catch (Exception ignored) {}
            }
            // If leveled up, inform listeners (approximate check)
            // For simplicity, trigger level up when xpIntoLevel < amount (means crossed boundary)
            if (s.xpIntoLevel < amount) {
                for (XPListener l : listeners) {
                    try { l.onLevelUp(s.level); } catch (Exception ignored) {}
                }
            }
        });
    }

    // Simple formula: xp required for next level = round(100 * level^1.5)
    public static int xpRequiredForLevel(int level) {
        // returns XP required to reach this level from previous level (level-1 -> level)
        double v = 100.0 * Math.pow(level, 1.5);
        return (int) Math.round(v);
    }

    public static int totalXpForLevelsBelow(int level) {
        // total XP required to reach the start of `level` (i.e., sum for levels 1..level-1)
        int total = 0;
        for (int l = 1; l < level; l++) total += xpRequiredForLevel(l);
        return total;
    }

    public static int getLevelForXp(int totalXp) {
        int level = 1;
        while (true) {
            int threshold = totalXpForLevelsBelow(level + 1);
            if (totalXp >= threshold) {
                level++;
                if (level > 2000) break; // safety
            } else break;
        }
        return Math.max(1, level);
    }

    public static void addListener(XPListener l) {
        listeners.add(l);
    }

    public static void removeListener(XPListener l) {
        listeners.remove(l);
    }
}

