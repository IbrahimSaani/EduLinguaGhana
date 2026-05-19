package com.edulinguaghana;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Centralized accessor for small app-level preferences used by multiple activities.
 */
public final class AppPreferences {

    private static final String PREFS_NAME = "EduLinguaPrefs"; // keep consistent with app
    private static final String KEY_DYNAMIC_BG = "dynamic_backgrounds_enabled";
    private static final String KEY_DAILY_REMINDERS = "DAILY_REMINDERS";
    private static final String KEY_STREAK_ALERTS = "STREAK_ALERTS";

    private AppPreferences() {}

    public static boolean isDynamicBackgroundEnabled(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DYNAMIC_BG, true);
    }

    public static void setDynamicBackgroundEnabled(Context context, boolean enabled) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DYNAMIC_BG, enabled).apply();
    }

    public static boolean isDailyRemindersEnabled(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DAILY_REMINDERS, true);
    }

    public static void setDailyRemindersEnabled(Context context, boolean enabled) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DAILY_REMINDERS, enabled).apply();
    }

    public static boolean isStreakAlertsEnabled(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_STREAK_ALERTS, true);
    }

    public static void setStreakAlertsEnabled(Context context, boolean enabled) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_STREAK_ALERTS, enabled).apply();
    }
}

