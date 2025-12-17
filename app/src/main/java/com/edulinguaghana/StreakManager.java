package com.edulinguaghana;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

public class StreakManager {
    private static final String PREF_NAME = "StreakPrefs";
    private static final String KEY_CURRENT_STREAK = "CURRENT_STREAK";
    private static final String KEY_LONGEST_STREAK = "LONGEST_STREAK";
    private static final String KEY_LAST_PRACTICE_DATE = "LAST_PRACTICE_DATE";
    private static final String KEY_LAST_PRACTICE_DAY = "LAST_PRACTICE_DAY";
    private static final String KEY_TOTAL_PRACTICE_DAYS = "TOTAL_PRACTICE_DAYS";

    private Context context;
    private SharedPreferences prefs;

    public StreakManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Record practice for today
    public void recordPractice() {
        Calendar today = Calendar.getInstance();
        int currentDay = today.get(Calendar.DAY_OF_YEAR);
        int currentYear = today.get(Calendar.YEAR);

        int lastPracticeDay = prefs.getInt(KEY_LAST_PRACTICE_DAY, -1);
        String lastPracticeDate = prefs.getString(KEY_LAST_PRACTICE_DATE, "");

        // Check if already practiced today
        String todayDate = currentYear + "-" + currentDay;
        if (todayDate.equals(lastPracticeDate)) {
            return; // Already recorded today
        }

        int currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0);
        int longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0);
        int totalPracticeDays = prefs.getInt(KEY_TOTAL_PRACTICE_DAYS, 0);

        // Check if practiced yesterday (streak continues)
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        int yesterdayDay = yesterday.get(Calendar.DAY_OF_YEAR);
        int yesterdayYear = yesterday.get(Calendar.YEAR);
        String yesterdayDate = yesterdayYear + "-" + yesterdayDay;

        if (lastPracticeDate.equals(yesterdayDate)) {
            // Streak continues
            currentStreak++;
        } else if (lastPracticeDate.isEmpty()) {
            // First time practicing
            currentStreak = 1;
        } else {
            // Streak broken, restart
            currentStreak = 1;
        }

        // Update longest streak
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }

        // Update total practice days
        totalPracticeDays++;

        // Save to preferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_CURRENT_STREAK, currentStreak);
        editor.putInt(KEY_LONGEST_STREAK, longestStreak);
        editor.putString(KEY_LAST_PRACTICE_DATE, todayDate);
        editor.putInt(KEY_LAST_PRACTICE_DAY, currentDay);
        editor.putInt(KEY_TOTAL_PRACTICE_DAYS, totalPracticeDays);
        editor.apply();

        // Send notification for streak milestones
        if (currentStreak % 7 == 0 && currentStreak > 0) {
            NotificationManager notificationManager = new NotificationManager(context);
            notificationManager.sendStreakNotification(currentStreak);
        }
    }

    // Get current streak
    public int getCurrentStreak() {
        // Check if streak is still valid
        Calendar today = Calendar.getInstance();
        int currentDay = today.get(Calendar.DAY_OF_YEAR);
        int currentYear = today.get(Calendar.YEAR);
        String todayDate = currentYear + "-" + currentDay;

        String lastPracticeDate = prefs.getString(KEY_LAST_PRACTICE_DATE, "");

        if (lastPracticeDate.isEmpty()) {
            return 0;
        }

        // Check if practiced today
        if (todayDate.equals(lastPracticeDate)) {
            return prefs.getInt(KEY_CURRENT_STREAK, 0);
        }

        // Check if practiced yesterday
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        int yesterdayDay = yesterday.get(Calendar.DAY_OF_YEAR);
        int yesterdayYear = yesterday.get(Calendar.YEAR);
        String yesterdayDate = yesterdayYear + "-" + yesterdayDay;

        if (lastPracticeDate.equals(yesterdayDate)) {
            return prefs.getInt(KEY_CURRENT_STREAK, 0);
        }

        // Streak is broken
        return 0;
    }

    // Get longest streak
    public int getLongestStreak() {
        return prefs.getInt(KEY_LONGEST_STREAK, 0);
    }

    // Get total practice days
    public int getTotalPracticeDays() {
        return prefs.getInt(KEY_TOTAL_PRACTICE_DAYS, 0);
    }

    // Check if practiced today
    public boolean isPracticedToday() {
        Calendar today = Calendar.getInstance();
        int currentDay = today.get(Calendar.DAY_OF_YEAR);
        int currentYear = today.get(Calendar.YEAR);
        String todayDate = currentYear + "-" + currentDay;

        String lastPracticeDate = prefs.getString(KEY_LAST_PRACTICE_DATE, "");
        return todayDate.equals(lastPracticeDate);
    }

    // Reset streak (for testing or user request)
    public void resetStreak() {
        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, 0)
            .putString(KEY_LAST_PRACTICE_DATE, "")
            .apply();
    }
}

