package com.edulinguaghana;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Background worker for scheduling and sending learning reminders and streak alerts
 * even when the app is closed. Runs on a periodic schedule (e.g., daily).
 */
public class LearningNotificationWorker extends Worker {

    public LearningNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();

            // Check if reminders and alerts are enabled
            boolean dailyRemindersEnabled = AppPreferences.isDailyRemindersEnabled(context);
            boolean streakAlertsEnabled = AppPreferences.isStreakAlertsEnabled(context);

            if (!dailyRemindersEnabled && !streakAlertsEnabled) {
                // Both disabled, no work needed
                return Result.success();
            }

            // Perform the same checks as in-app notifications but post system notifications
            long lastCheck = NotificationManager.getLastCheckTime(context);
            long now = System.currentTimeMillis();
            long dayInMillis = 24 * 60 * 60 * 1000;

            // Only run once per day to avoid spam
            if (now - lastCheck < dayInMillis) {
                return Result.success();
            }

            // Mark this check time
            NotificationManager.markChecked(context, now);

            // Generate and post system notifications based on user preferences
            if (dailyRemindersEnabled) {
                sendPracticeReminder(context);
            }

            if (streakAlertsEnabled) {
                sendStreakAlert(context);
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            // Retry if there's an error
            return Result.retry();
        }
    }

    /**
     * Send a practice reminder notification if user hasn't practiced today
     */
    private void sendPracticeReminder(Context context) {
        try {
            // Check if user has practiced today
            boolean practicedToday = PracticeTracker.hasPracticedToday(context);

            if (!practicedToday) {
                LearningNotificationHelper.showReminder(
                        context,
                        "Time to Practice! ⏰",
                        "Don't break your learning streak! Come practice a few minutes today.",
                        1001
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a streak loss alert if practice has been missed for too long
     */
    private void sendStreakAlert(Context context) {
        try {
            // Calculate days of inactivity
            long lastPracticeTime = PracticeTracker.getLastPracticeTime(context);
            long now = System.currentTimeMillis();
            long dayInMillis = 24 * 60 * 60 * 1000;
            long daysInactive = (now - lastPracticeTime) / dayInMillis;

            // Send alert if user hasn't practiced for 1+ days
            if (daysInactive >= 1) {
                LearningNotificationHelper.showStreakAlert(
                        context,
                        (int) daysInactive,
                        1002
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

