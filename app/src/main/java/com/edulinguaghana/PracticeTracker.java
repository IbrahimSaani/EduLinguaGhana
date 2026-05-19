package com.edulinguaghana;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

/**
 * Tracks when users last practiced and whether they practiced today
 */
public class PracticeTracker {
    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_LAST_PRACTICE_TIME = "LAST_PRACTICE_TIME";

    private PracticeTracker() {}

    /**
     * Record that the user practiced now
     */
    public static void recordPractice(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_PRACTICE_TIME, System.currentTimeMillis()).apply();
    }

    /**
     * Get the timestamp of the last practice
     */
    public static long getLastPracticeTime(Context context) {
        if (context == null) return 0L;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_PRACTICE_TIME, 0L);
    }

    /**
     * Check if the user has practiced today (in the current calendar day)
     */
    public static boolean hasPracticedToday(Context context) {
        if (context == null) return false;

        long lastPracticeTime = getLastPracticeTime(context);
        if (lastPracticeTime == 0) {
            return false; // Never practiced
        }

        // Check if last practice was today
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar lastPractice = Calendar.getInstance();
        lastPractice.setTimeInMillis(lastPracticeTime);
        lastPractice.set(Calendar.HOUR_OF_DAY, 0);
        lastPractice.set(Calendar.MINUTE, 0);
        lastPractice.set(Calendar.SECOND, 0);
        lastPractice.set(Calendar.MILLISECOND, 0);

        return today.equals(lastPractice);
    }

    /**
     * Get the number of days since last practice (0 if today, 1 if yesterday, etc.)
     */
    public static int getDaysSinceLastPractice(Context context) {
        if (context == null) return Integer.MAX_VALUE;

        long lastPracticeTime = getLastPracticeTime(context);
        if (lastPracticeTime == 0) {
            return Integer.MAX_VALUE; // Never practiced
        }

        long now = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        return (int) ((now - lastPracticeTime) / dayInMillis);
    }
}

