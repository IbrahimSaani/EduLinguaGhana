package com.edulinguaghana.social;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Helper class to manage FCM tokens
 */
public class FCMTokenManager {
    private static final String PREF_NAME = "FCM";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private final Context context;

    public FCMTokenManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Get the current FCM token and save it to Firebase
     */
    public void initializeFCMToken() {
        // Check Google Play Services
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            return;
        }

        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();

                // Save token locally
                saveTokenLocally(token);

                // Save token to Firebase
                saveTokenToFirebase(token);
            });
    }

    /**
     * Save token to SharedPreferences
     */
    private void saveTokenLocally(String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
    }

    /**
     * Get locally saved token
     */
    public String getLocalToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_FCM_TOKEN, null);
    }

    /**
     * Save token to Firebase Realtime Database
     */
    private void saveTokenToFirebase(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference tokenRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .child("fcmToken");

            tokenRef.setValue(token)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });
        }
    }

    /**
     * Subscribe to a topic
     */
    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }

    /**
     * Unsubscribe from a topic
     */
    public void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
    }

    /**
     * Delete the FCM token
     */
    public void deleteToken() {
        FirebaseMessaging.getInstance().deleteToken()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    saveTokenLocally(null);
                }
            });
    }
}
