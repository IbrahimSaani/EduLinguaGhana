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

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains data payload
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage);
        }

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage);
        }
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {
        String type = remoteMessage.getData().get("type");
        String fromUserId = remoteMessage.getData().get("fromUserId");
        String displayName = remoteMessage.getData().get("displayName");

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

                default:
                    Log.w(TAG, "Unknown notification type: " + type);
            }
        }
    }

    private void handleNotification(RemoteMessage remoteMessage) {
        // Notification payload is automatically displayed by Firebase
        // We can add custom handling here if needed
        Log.d(TAG, "Notification handled automatically by Firebase");
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

