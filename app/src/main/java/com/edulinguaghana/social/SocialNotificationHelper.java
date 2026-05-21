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
                context.getString(R.string.notification_channel_social_name),
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(context.getString(R.string.notification_channel_social_desc));
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
        return true;
    }

    /**
     * Show notification for new friend request
     */
    @SuppressLint("MissingPermission")
    public void showFriendRequestNotification(String fromUserId, String displayName) {
        if (!canShowNotification()) return;

        String fromName = displayName != null ? displayName : fromUserId;
        String title = context.getString(R.string.notification_friend_request_title);
        String message = context.getString(R.string.notification_friend_request_message, fromName);
        
        // Add to in-app notification manager
        com.edulinguaghana.NotificationManager inAppManager = new com.edulinguaghana.NotificationManager(context);
        inAppManager.addNotification(
            context.getString(R.string.notification_friend_request_title_no_emoji), // Helper needed? or just use same
            message,
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
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        try {
            notificationManager.notify(fromUserId.hashCode(), builder.build());
        } catch (SecurityException e) {
            // Silently fail
        }
    }

    /**
     * Show notification for new relationship request (Teacher/Parent)
     */
    @SuppressLint("MissingPermission")
    public void showRelationshipRequestNotification(String fromUserId, String displayName, String gender, String type) {
        if (!canShowNotification()) return;

        String prefix = "";
        if ("Male".equalsIgnoreCase(gender)) {
            prefix = context.getString(R.string.notification_prefix_mr);
        } else if ("Female".equalsIgnoreCase(gender)) {
            prefix = context.getString(R.string.notification_prefix_mrs);
        }
        
        String fromName = prefix + (displayName != null ? displayName : fromUserId);
        String roleLabel = "Teacher".equalsIgnoreCase(type) ? 
                context.getString(R.string.notification_rel_teacher) : 
                context.getString(R.string.notification_rel_parent);

        // Add to in-app notification manager
        com.edulinguaghana.NotificationManager inAppManager = new com.edulinguaghana.NotificationManager(context);
        inAppManager.addNotification(
            context.getString(R.string.notification_rel_request_inapp_title),
            context.getString(R.string.notification_rel_request_inapp_message, fromName, roleLabel),
            "👨‍🏫",
            com.edulinguaghana.Notification.NotificationType.MOTIVATIONAL
        );

        Intent intent = new Intent(context, com.edulinguaghana.tracking.RelationshipManagementActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            5,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(context.getString(R.string.notification_rel_request_title))
            .setContentText(context.getString(R.string.notification_rel_request_message, fromName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify(("rel_" + fromUserId).hashCode(), builder.build());
    }

    /**
     * Show notification for new challenge
     */
    @SuppressLint("MissingPermission")
    public void showChallengeNotification(String fromUserId, String displayName, String quizType) {
        if (!canShowNotification()) return;

        String fromName = displayName != null ? displayName : fromUserId;
        String title = context.getString(R.string.notification_challenge_title);
        String message = context.getString(R.string.notification_challenge_message, fromName, quizType);

        // Add to in-app notification manager
        com.edulinguaghana.NotificationManager inAppManager = new com.edulinguaghana.NotificationManager(context);
        inAppManager.addNotification(
            title,
            message,
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
            .setContentTitle(title)
            .setContentText(message)
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
        String title = context.getString(R.string.notification_friend_accepted_title);
        String message = context.getString(R.string.notification_friend_accepted_message, friendName);

        // Add to in-app notification manager
        com.edulinguaghana.NotificationManager inAppManager = new com.edulinguaghana.NotificationManager(context);
        inAppManager.addNotification(
            title,
            message,
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
            .setContentTitle(title)
            .setContentText(message)
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
        String title = won ? context.getString(R.string.notification_challenge_won_title) : 
                context.getString(R.string.notification_challenge_completed_title);
        String message = won ?
            context.getString(R.string.notification_challenge_won_message, opponentName) :
            context.getString(R.string.notification_challenge_completed_message, opponentName);

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
            notificationManager.notify(("completed_" + opponentId).hashCode(), builder.build());
        } catch (SecurityException e) {
            // Silently fail
        }
    }
}
