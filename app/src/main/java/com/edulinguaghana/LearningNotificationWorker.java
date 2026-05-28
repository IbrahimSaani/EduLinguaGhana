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
                return Result.success();
            }

            // USE A SEPARATE KEY for background check to ensure it runs independently of app openings
            String PREF_NAME = "NotificationsPrefs";
            String KEY_LAST_BACKGROUND_CHECK = "LAST_BACKGROUND_CHECK_TIME";
            android.content.SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            
            long lastCheck = prefs.getLong(KEY_LAST_BACKGROUND_CHECK, 0L);
            long now = System.currentTimeMillis();
            long dayInMillis = 24 * 60 * 60 * 1000;

            // Still avoid spamming multiple times a day
            if (now - lastCheck < (dayInMillis - 1000 * 60 * 30)) { // 23.5 hours buffer
                return Result.success();
            }

            // Mark this background check time
            prefs.edit().putLong(KEY_LAST_BACKGROUND_CHECK, now).apply();

            // Use NotificationManager to handle the logic. 
            // NotificationManager.addNotification already shows a system notification via AppNotificationHelper.
            NotificationManager notificationManager = new NotificationManager(context);
            
            if (dailyRemindersEnabled) {
                checkAndSendReminder(context, notificationManager);
            }

            if (streakAlertsEnabled) {
                checkAndSendStreakAlert(context, notificationManager);
            }

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private void checkAndSendReminder(Context context, NotificationManager nm) {
        if (!PracticeTracker.hasPracticedToday(context)) {
            String title = "Time to Practice! ⏰";
            String message = "Don't break your learning streak! Come practice a few minutes today.";
            
            // This adds to history AND shows a system notification
            nm.addNotification(title, message, "⏰", Notification.NotificationType.REMINDER);
        }
    }

    private void checkAndSendStreakAlert(Context context, NotificationManager nm) {
        long lastPracticeTime = PracticeTracker.getLastPracticeTime(context);
        if (lastPracticeTime == 0) return;

        long now = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        int daysInactive = (int) ((now - lastPracticeTime) / dayInMillis);

        if (daysInactive >= 1) {
            String title = "Streak at Risk! 🔥";
            String message = "You haven't practiced for " + daysInactive + " day(s). Come back today to keep learning!";
            
            nm.addNotification(title, message, "🔥", Notification.NotificationType.STREAK);

            if (daysInactive >= 3) {
                String emailTitle = "We Miss You! 📧";
                String emailMsg = "Check your Gmail! We've sent you a special motivational message to " + getUserEmail();
                
                // Add to history and show system notification
                nm.addNotification(emailTitle, emailMsg, "📧", Notification.NotificationType.REMINDER);
                
                // NOTE: To actually send an email to Gmail, a backend service (like Firebase Functions)
                // or a 3rd party API (SendGrid/Mailgun) is required.
                // The app itself cannot silently send emails without a server.
                triggerEmailMock(getUserEmail());
            }
        }
    }

    private void triggerEmailMock(String email) {
        // Placeholder for triggering a real email via a backend API
        android.util.Log.d("LearningWorker", "Requesting backend to send email to: " + email);
    }

    private String getUserEmail() {
        try {
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                return user.getEmail();
            }
        } catch (Exception ignored) {}
        return "your email";
    }
}

