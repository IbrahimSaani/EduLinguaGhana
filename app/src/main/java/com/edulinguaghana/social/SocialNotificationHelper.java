package com.edulinguaghana.social;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.edulinguaghana.ProfileActivity;
import com.edulinguaghana.R;

/**
 * Helper class for sending social notifications (friend requests, challenges, etc.)
 * This class is ready for integration with Firebase Cloud Messaging (FCM)
 */
@SuppressWarnings("MissingPermission")
public class SocialNotificationHelper {
    private static final String CHANNEL_ID = "social_notifications";
    private static final String CHANNEL_NAME = "Social Notifications";
    private static final String CHANNEL_DESC = "Notifications for friend requests and challenges";

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    public SocialNotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private boolean canShowNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        }
        return true; // No permission needed for older versions
    }

    /**
     * Show notification for new friend request
     */
    @SuppressLint("MissingPermission")
    public void showFriendRequestNotification(String fromUserId, String displayName) {
        if (!canShowNotification()) return;

        String fromName = displayName != null ? displayName : fromUserId;
        
        // Add to in-app notification manager
        com.edulinguaghana.NotificationManager inAppManager = new com.edulinguaghana.NotificationManager(context);
        inAppManager.addNotification(
            "New Friend Request",
            fromName + " wants to be your friend!",
            "👥",
            com.edulinguaghana.Notification.NotificationType.MOTIVATIONAL
        );

        Intent intent = new Intent(context, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("New Friend Request 👥")
            .setContentText((displayName != null ? displayName : fromUserId) + " wants to be your friend!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        try {
            //noinspection MissingPermission
            notificationManager.notify(fromUserId.hashCode(), builder.build());
        } catch (SecurityException e) {
            // Permission not granted, silently fail
        }
    }

    /**
     * Show notification for new challenge
     */
    @SuppressLint("MissingPermission")
    public void showChallengeNotification(String fromUserId, String displayName, String quizType) {
        if (!canShowNotification()) return;

        String fromName = displayName != null ? displayName : fromUserId;

        // Add to in-app notification manager
        com.edulinguaghana.NotificationManager inAppManager = new com.edulinguaghana.NotificationManager(context);
        inAppManager.addNotification(
            "New Challenge ⚔️",
            fromName + " challenged you to " + quizType + "!",
            "⚔️",
            com.edulinguaghana.Notification.NotificationType.MOTIVATIONAL
        );

        Intent intent = new Intent(context, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("New Challenge ⚔️")
            .setContentText((displayName != null ? displayName : fromUserId) + " challenged you to " + quizType + "!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify(("challenge_" + fromUserId).hashCode(), builder.build());
    }

    /**
     * Show notification when friend accepts request
     */
    @SuppressLint("MissingPermission")
    public void showFriendAcceptedNotification(String friendUserId, String displayName) {
        if (!canShowNotification()) return;

        String friendName = displayName != null ? displayName : friendUserId;

        // Add to in-app notification manager
        com.edulinguaghana.NotificationManager inAppManager = new com.edulinguaghana.NotificationManager(context);
        inAppManager.addNotification(
            "Friend Request Accepted ✅",
            friendName + " is now your friend!",
            "✅",
            com.edulinguaghana.Notification.NotificationType.ACHIEVEMENT
        );

        Intent intent = new Intent(context, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Friend Request Accepted ✅")
            .setContentText((displayName != null ? displayName : friendUserId) + " is now your friend!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify(("accepted_" + friendUserId).hashCode(), builder.build());
    }

    /**
     * Show a generic notification
     */
    @SuppressLint("MissingPermission")
    public void showGenericNotification(String title, String message, String tag) {
        if (!canShowNotification()) return;

        Intent intent = new Intent(context, com.edulinguaghana.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            4,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify(tag.hashCode(), builder.build());
    }

    /**
     * Show notification when challenge is completed
     */
    @SuppressLint("MissingPermission")
    public void showChallengeCompletedNotification(String opponentId, String displayName, boolean won) {
        if (!canShowNotification()) return;

        String opponentName = displayName != null ? displayName : opponentId;
        String title = won ? "Challenge Won! 🏆" : "Challenge Completed";
        String message = won ?
            "You beat " + opponentName + "!" :
            "Challenge with " + opponentName + " completed!";

        // Add to in-app notification manager
        com.edulinguaghana.NotificationManager inAppManager = new com.edulinguaghana.NotificationManager(context);
        inAppManager.addNotification(
            title,
            message,
            won ? "🏆" : "🎯",
            won ? com.edulinguaghana.Notification.NotificationType.ACHIEVEMENT : com.edulinguaghana.Notification.NotificationType.MILESTONE
        );

        Intent intent = new Intent(context, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        try {
            //noinspection MissingPermission
            notificationManager.notify(("completed_" + opponentId).hashCode(), builder.build());
        } catch (SecurityException e) {
            // Permission not granted, silently fail
        }
    }
}
