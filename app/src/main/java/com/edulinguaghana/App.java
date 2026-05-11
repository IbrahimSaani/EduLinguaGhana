package com.edulinguaghana;

import android.app.Application;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.edulinguaghana.social.impl.FirebaseSocialRepository;
import com.edulinguaghana.social.SocialRepository;
import com.edulinguaghana.social.SocialProvider;
import com.edulinguaghana.social.FCMTokenManager;

public class App extends Application {
    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        TimeBasedBackgroundManager.register(this);

        // Initialize Firebase (no-op if already initialized by google-services)
        try {
            FirebaseApp.initializeApp(this);
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        } catch (Exception ignored) {
        }

        // Initialize Facebook SDK
        try {
            FacebookSdk.sdkInitialize(getApplicationContext());
            AppEventsLogger.activateApp(this);
        } catch (Exception ignored) {
        }

        // Initialize SocialProvider with Firebase-backed repository
        try {
            SocialRepository repo = new FirebaseSocialRepository();
            SocialProvider.init(repo);
        } catch (Exception e) {
            // Fall back to null provider; existing code should handle null
            Log.e(TAG, "Failed to initialize SocialProvider", e);
        }

        // Initialize FCM token
        try {
            FCMTokenManager fcmTokenManager = new FCMTokenManager(this);
            fcmTokenManager.initializeFCMToken();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize FCM token", e);
        }
    }
}
