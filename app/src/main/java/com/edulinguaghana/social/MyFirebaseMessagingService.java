package com.edulinguaghana.social;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * FCM Service to handle push notifications
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "FCM Message Received!");
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message ID: " + remoteMessage.getMessageId());

        // Check if message contains notification payload
        // If it does, we save it to our local NotificationManager so it appears in the UI
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Title: " + title);
            Log.d(TAG, "Message Notification Body: " + body);

            // Save to in-app notification screen
            com.edulinguaghana.NotificationManager inAppManager = new com.edulinguaghana.NotificationManager(this);
            inAppManager.addNotification(
                title != null ? title : "New Message",
                body != null ? body : "",
                "📢", // Default emoji for broadcast messages
                com.edulinguaghana.Notification.NotificationType.SYSTEM
            );
            
            handleNotification(remoteMessage);
        } else {
            Log.d(TAG, "No notification payload in message");
        }

        // Check if message contains data payload
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            
            // Handle as data message. We've updated the logic to be smarter about duplicates
            handleDataMessage(remoteMessage);
        } else {
            Log.d(TAG, "No data payload in message");
        }
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {
        String type = remoteMessage.getData().get("type");
        String fromUserId = remoteMessage.getData().get("fromUserId");
        String displayName = remoteMessage.getData().get("displayName");

        // If this is a developer broadcast and we already handled the notification payload, skip
        if (remoteMessage.getNotification() != null && "broadcast".equals(type)) {
            Log.d(TAG, "Skipping duplicate broadcast data handling");
            return;
        }

        Log.d(TAG, "Handling data message of type: " + type);

        SocialNotificationHelper notificationHelper = new SocialNotificationHelper(this);

        if (type != null) {
            switch (type) {
                case "friend_request":
                    notificationHelper.showFriendRequestNotification(fromUserId, displayName);
                    break;

                case "friend_accepted":
                    notificationHelper.showFriendAcceptedNotification(fromUserId, displayName);
                    break;

                case "challenge":
                    String quizType = remoteMessage.getData().get("quizType");
                    notificationHelper.showChallengeNotification(fromUserId, displayName, quizType);
                    break;

                case "challenge_completed":
                    boolean won = "true".equals(remoteMessage.getData().get("won"));
                    notificationHelper.showChallengeCompletedNotification(fromUserId, displayName, won);
                    break;

                case "relationship_request":
                    String gender = remoteMessage.getData().get("gender");
                    String relType = remoteMessage.getData().get("relationType");
                    notificationHelper.showRelationshipRequestNotification(fromUserId, displayName, gender, relType);
                    break;

                case "update":
                    String updateTitle = remoteMessage.getData().get("title");
                    String updateMessage = remoteMessage.getData().get("message");
                    showGeneralNotification(updateTitle, updateMessage, "🚀", com.edulinguaghana.Notification.NotificationType.MILESTONE);
                    break;

                case "broadcast":
                    String bTitle = remoteMessage.getData().get("title");
                    String bMessage = remoteMessage.getData().get("message");
                    String bEmoji = remoteMessage.getData().get("emoji");
                    showGeneralNotification(bTitle, bMessage, bEmoji != null ? bEmoji : "📢", com.edulinguaghana.Notification.NotificationType.MOTIVATIONAL);
                    break;

                default:
                    Log.w(TAG, "Unknown notification type: " + type);
            }
        }
    }

    private void showGeneralNotification(String title, String message, String emoji, com.edulinguaghana.Notification.NotificationType type) {
        // Add to in-app notifications
        com.edulinguaghana.NotificationManager inAppManager = new com.edulinguaghana.NotificationManager(this);
        inAppManager.addNotification(title, message, emoji, type);

        // Show system notification
        SocialNotificationHelper helper = new SocialNotificationHelper(this);
        helper.showGenericNotification(title, message, "general_" + System.currentTimeMillis());
    }

    private void handleNotification(RemoteMessage remoteMessage) {
        // Notification payload is automatically displayed by Firebase
        // We can add custom handling here if needed
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification title: " + remoteMessage.getNotification().getTitle());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Send token to your server or save it
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your server
        // For now, we'll save it locally
        getSharedPreferences("FCM", MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply();

        Log.d(TAG, "Token saved locally: " + token);

        // Save token to Firebase for the current user
        saveFCMTokenToFirebase(token);
    }

    private void saveFCMTokenToFirebase(String token) {
        com.google.firebase.auth.FirebaseUser currentUser =
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            com.google.firebase.database.DatabaseReference tokenRef =
                com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("fcmToken");

            tokenRef.setValue(token)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved to Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save FCM token", e));
        }
    }
}

