package com.edulinguaghana;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

    public static final String PREFS_NAME = "EduLinguaPrefs";
    public static final String KEY_DAILY_REMINDERS = "DAILY_REMINDERS";
    public static final String KEY_STREAK_ALERTS = "STREAK_ALERTS";
    public static final String KEY_HIGH_CONTRAST = "HIGH_CONTRAST";
    public static final String KEY_STANDARD_FONT = "STANDARD_FONT";
    public static final String KEY_TEXT_SIZE = "TEXT_SIZE"; // 0: Small, 1: Normal, 2: Large, 3: Extra Large
    public static final String KEY_ANIMATIONS_ENABLED = "ANIMATIONS_ENABLED";
    public static final String KEY_LOW_POWER_ANIMATIONS = "LOW_POWER_ANIMATIONS";
    
    // New Accessibility Features
    public static final String KEY_HAPTIC_FEEDBACK = "HAPTIC_FEEDBACK";
    public static final String KEY_AUTO_VOICE = "AUTO_VOICE";
    public static final String KEY_FOCUS_MODE = "FOCUS_MODE";

    private AppPreferences() {}

    public static boolean isDailyRemindersEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DAILY_REMINDERS, true);
    }

    public static void setDailyRemindersEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_DAILY_REMINDERS, enabled).apply();
    }

    public static boolean isStreakAlertsEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_STREAK_ALERTS, true);
    }

    public static void setStreakAlertsEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_STREAK_ALERTS, enabled).apply();
    }

    public static boolean isHighContrastEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_HIGH_CONTRAST, false);
    }

    public static void setHighContrastEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply();
    }

    public static boolean isStandardFontEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_STANDARD_FONT, false);
    }

    public static void setStandardFontEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_STANDARD_FONT, enabled).apply();
    }

    public static int getTextSize(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_TEXT_SIZE, 1);
    }

    public static void setTextSize(Context context, int size) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_TEXT_SIZE, size).apply();
    }

    public static boolean isAnimationsEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ANIMATIONS_ENABLED, true);
    }

    public static void setAnimationsEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_ANIMATIONS_ENABLED, enabled).apply();
    }

    public static boolean isReducedMotionEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_LOW_POWER_ANIMATIONS, false);
    }

    public static void setReducedMotionEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_LOW_POWER_ANIMATIONS, enabled).apply();
    }

    public static boolean isHapticFeedbackEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_HAPTIC_FEEDBACK, true);
    }

    public static void setHapticFeedbackEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply();
    }

    public static boolean isAutoVoiceEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_AUTO_VOICE, false);
    }

    public static void setAutoVoiceEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_AUTO_VOICE, enabled).apply();
    }

    public static boolean isFocusModeEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_FOCUS_MODE, false);
    }

    public static void setFocusModeEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_FOCUS_MODE, enabled).apply();
    }
}
