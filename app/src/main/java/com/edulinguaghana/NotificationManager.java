package com.edulinguaghana;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NotificationManager {
    private static final String PREF_NAME = "NotificationsPrefs";
    private static final String KEY_NOTIFICATIONS = "NOTIFICATIONS_LIST";
    private static final String KEY_LAST_CHECK = "LAST_CHECK_TIME";
    private static final String KEY_STREAK_NOTIF_SHOWN = "STREAK_NOTIF_SHOWN";
    private static final String KEY_INACTIVITY_NOTIF_SHOWN = "INACTIVITY_NOTIF_SHOWN";

    private Context context;
    private SharedPreferences prefs;
    private Gson gson;

    public NotificationManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // Get all notifications
    public List<Notification> getAllNotifications() {
        String json = prefs.getString(KEY_NOTIFICATIONS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Notification>>() {}.getType();
        List<Notification> notifications = gson.fromJson(json, type);
        // Sort by timestamp (newest first)
        Collections.sort(notifications, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
        return notifications;
    }

    // Save notifications
    private void saveNotifications(List<Notification> notifications) {
        String json = gson.toJson(notifications);
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply();
    }

    // Add a new notification
    public void addNotification(String title, String message, String emoji, Notification.NotificationType type) {
        List<Notification> notifications = getAllNotifications();

        String id = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        Notification notification = new Notification(id, title, message, emoji, timestamp, type, false);
        notifications.add(notification);

        // Keep only last 50 notifications
        if (notifications.size() > 50) {
            notifications = notifications.subList(0, 50);
        }

        saveNotifications(notifications);
    }

    // Mark notification as read
    public void markAsRead(String notificationId) {
        List<Notification> notifications = getAllNotifications();
        for (Notification n : notifications) {
            if (n.getId().equals(notificationId)) {
                n.setRead(true);
                break;
            }
        }
        saveNotifications(notifications);
    }

    // Mark all as read
    public void markAllAsRead() {
        List<Notification> notifications = getAllNotifications();
        for (Notification n : notifications) {
            n.setRead(true);
        }
        saveNotifications(notifications);
    }

    // Get unread count
    public int getUnreadCount() {
        List<Notification> notifications = getAllNotifications();
        int count = 0;
        for (Notification n : notifications) {
            if (!n.isRead()) {
                count++;
            }
        }
        return count;
    }

    // Clear all notifications
    public void clearAllNotifications() {
        prefs.edit().remove(KEY_NOTIFICATIONS).apply();
    }

    // Delete a specific notification
    public void deleteNotification(String notificationId) {
        List<Notification> notifications = getAllNotifications();
        // Use iterator to support API level 21+
        for (int i = notifications.size() - 1; i >= 0; i--) {
            if (notifications.get(i).getId().equals(notificationId)) {
                notifications.remove(i);
                break;
            }
        }
        saveNotifications(notifications);
    }

    // Generate automatic notifications based on user activity
    public void checkAndGenerateNotifications() {
        long lastCheck = prefs.getLong(KEY_LAST_CHECK, 0);
        long now = System.currentTimeMillis();

        // Check once per day
        if (now - lastCheck < 24 * 60 * 60 * 1000) {
            return;
        }

        prefs.edit().putLong(KEY_LAST_CHECK, now).apply();

        // Generate notifications based on progress
        generateProgressNotifications();
        generateStreakNotifications();
        generateMotivationalNotifications();
        generateInactivityNotifications();
    }

    private void generateInactivityNotifications() {
        SharedPreferences streakPrefs = context.getSharedPreferences("StreakPrefs", Context.MODE_PRIVATE);
        long lastPracticeTime = streakPrefs.getLong("LAST_PRACTICE_TIMESTAMP", 0);
        
        if (lastPracticeTime == 0) return; // Never practiced, handled by welcome

        long now = System.currentTimeMillis();
        long daysInactive = (now - lastPracticeTime) / (24 * 60 * 60 * 1000);

        if (daysInactive >= 3) {
            String lastShownId = prefs.getString(KEY_INACTIVITY_NOTIF_SHOWN, "");
            String currentInactivityId = "inactivity_" + (daysInactive / 3); // Trigger every 3 days

            if (lastShownId == null || !lastShownId.equals(currentInactivityId)) {
                addNotification(
                    "We miss you! 🦉",
                    "It's been " + daysInactive + " days since your last lesson. Kojo is waiting to learn with you!",
                    "🦉",
                    Notification.NotificationType.MOTIVATIONAL
                );
                prefs.edit().putString(KEY_INACTIVITY_NOTIF_SHOWN, currentInactivityId).apply();
            }
        }
    }

    private void generateProgressNotifications() {
        int totalQuizzes = ProgressManager.getTotalQuizzes(context);
        int highScore = ProgressManager.getHighScore(context);
        int accuracy = ProgressManager.getAccuracy(context);

        // Milestone notifications
        if (totalQuizzes == 5) {
            addNotification(
                "First Steps! 🎉",
                "You've completed 5 quizzes! Keep up the great work!",
                "🎉",
                Notification.NotificationType.MILESTONE
            );
        } else if (totalQuizzes == 10) {
            addNotification(
                "Getting Started! 🌟",
                "10 quizzes completed! You're building a strong foundation!",
                "🌟",
                Notification.NotificationType.MILESTONE
            );
        } else if (totalQuizzes == 25) {
            addNotification(
                "Quarter Century! 🏆",
                "25 quizzes completed! You're becoming an expert!",
                "🏆",
                Notification.NotificationType.MILESTONE
            );
        } else if (totalQuizzes == 50) {
            addNotification(
                "Half Century! 🎖️",
                "50 quizzes! You're halfway to mastery!",
                "🎖️",
                Notification.NotificationType.MILESTONE
            );
        } else if (totalQuizzes == 100) {
            addNotification(
                "Century Club! 👑",
                "100 quizzes completed! You're a true language champion!",
                "👑",
                Notification.NotificationType.MILESTONE
            );
        }

        // High score achievements
        if (highScore >= 80 && highScore < 90) {
            addNotification(
                "Great Score! 🌟",
                "You scored " + highScore + " points! Almost perfect!",
                "🌟",
                Notification.NotificationType.ACHIEVEMENT
            );
        } else if (highScore >= 90) {
            addNotification(
                "Perfect Score! ⭐",
                "Amazing! You scored " + highScore + " points! Outstanding!",
                "⭐",
                Notification.NotificationType.ACHIEVEMENT
            );
        }

        // Accuracy achievements
        if (accuracy >= 75 && accuracy < 85) {
            addNotification(
                "Great Accuracy! 🎯",
                "Your accuracy is " + accuracy + "%! Well done!",
                "🎯",
                Notification.NotificationType.ACHIEVEMENT
            );
        } else if (accuracy >= 85) {
            addNotification(
                "Exceptional Accuracy! 🏅",
                "Your accuracy is " + accuracy + "%! Outstanding!",
                "🏅",
                Notification.NotificationType.ACHIEVEMENT
            );
        }
    }

    private void generateStreakNotifications() {
        // Check if user practiced today
        Calendar today = Calendar.getInstance();
        int dayOfYear = today.get(Calendar.DAY_OF_YEAR);

        SharedPreferences mainPrefs = context.getSharedPreferences("EduLinguaPrefs", Context.MODE_PRIVATE);
        int lastPracticeDay = mainPrefs.getInt("LAST_PRACTICE_DAY", -1);

        if (lastPracticeDay != dayOfYear) {
            // User hasn't practiced today
            addNotification(
                "Time to Practice! 📚",
                "Don't break your streak! Complete a lesson today.",
                "📚",
                Notification.NotificationType.REMINDER
            );
        }
    }

    private void generateMotivationalNotifications() {
        // Add random motivational messages
        String[] messages = {
            "Every lesson brings you closer to fluency! Keep going! 💪",
            "Learning a language opens doors to new worlds! 🌍",
            "Practice makes perfect! You're doing great! ⭐",
            "Your dedication is inspiring! Keep learning! 🌟",
            "Small steps every day lead to big results! 🚀",
            "You're building valuable skills! Stay consistent! 📈"
        };

        String[] emojis = {"💪", "🌍", "⭐", "🌟", "🚀", "📈"};

        int random = (int) (Math.random() * messages.length);

        addNotification(
            "Daily Motivation 💡",
            messages[random],
            emojis[random],
            Notification.NotificationType.MOTIVATIONAL
        );
    }

    // Send achievement notification
    public void sendAchievementNotification(String title, String message) {
        addNotification(title, message, "🎉", Notification.NotificationType.ACHIEVEMENT);
    }

    // Send reminder notification
    public void sendReminderNotification(String title, String message) {
        addNotification(title, message, "⏰", Notification.NotificationType.REMINDER);
    }

    // Send streak notification
    public void sendStreakNotification(int streakDays) {
        String emoji = streakDays >= 7 ? "🔥" : "⚡";
        addNotification(
            "Streak Milestone! " + emoji,
            "You're on a " + streakDays + " day streak! Amazing!",
            emoji,
            Notification.NotificationType.STREAK
        );
    }
}

