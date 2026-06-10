package com.edulinguaghana;

import android.app.Application;
import android.app.Activity;
import android.os.Bundle;
import android.net.Uri;
import java.lang.reflect.Field;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.edulinguaghana.social.impl.FirebaseSocialRepository;
import com.edulinguaghana.social.SocialRepository;
import com.edulinguaghana.social.SocialProvider;
import com.edulinguaghana.social.FCMTokenManager;
import com.google.firebase.appdistribution.FirebaseAppDistribution;
import com.google.firebase.appdistribution.InterruptionLevel;

import java.util.concurrent.TimeUnit;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

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
        }

        // Initialize FCM token
        try {
            FCMTokenManager fcmTokenManager = new FCMTokenManager(this);
            fcmTokenManager.initializeFCMToken();
        } catch (Exception ignored) {
        }

        // Initialize background notification worker
        try {
            scheduleLearningNotificationWorker();
        } catch (Exception ignored) {
        }

        // Initialize social activity listener for real-time notifications
        try {
            com.edulinguaghana.social.SocialActivityListener.getInstance(this).startListening();
        } catch (Exception ignored) {
        }

        // Enable App Distribution Tester Feedback notification only in debug builds
        if (BuildConfig.DEBUG) {
            try {
                FirebaseAppDistribution.getInstance().showFeedbackNotification(
                        "Submit feedback to the developers",
                        InterruptionLevel.DEFAULT
                );
            } catch (Exception ignored) {
            }
        }

        // Apply global hotfix for Firebase App Distribution FeedbackActivity crash
        // Fixes NullPointerException in onSaveInstanceState when screenshotUri is null
        applyFeedbackActivityCrashFix();
    }

    /**
     * Applies accessibility settings to the given activity.
     */
    private void applyAccessibilitySettings(Activity activity) {
        try {
            // 1. Apply Font Scale
            int size = AppPreferences.getTextSize(activity);
            float scale = 1.0f;
            switch (size) {
                case 0: scale = 0.85f; break;
                case 1: scale = 1.0f; break;
                case 2: scale = 1.15f; break;
                case 3: scale = 1.30f; break;
            }

            android.content.res.Configuration config = new android.content.res.Configuration(activity.getResources().getConfiguration());
            config.fontScale = scale;
            activity.applyOverrideConfiguration(config);

            // 2. High Contrast (Optional: could set a high contrast theme if defined)
            if (AppPreferences.isHighContrastEnabled(activity)) {
                // activity.setTheme(R.style.Theme_HighContrast);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Hotfix for a known crash in Firebase App Distribution SDK (16.0.0-beta19)
     * The FeedbackActivity crashes in onSaveInstanceState when screenshotUri is null.
     */
    private void applyFeedbackActivityCrashFix() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@androidx.annotation.NonNull Activity activity, @androidx.annotation.Nullable Bundle savedInstanceState) {
                applyAccessibilitySettings(activity);
                if (activity.getClass().getName().endsWith(".FeedbackActivity")) {
                    try {
                        // Use reflection to ensure screenshotUri is never null before onSaveInstanceState is called
                        Field field = activity.getClass().getDeclaredField("screenshotUri");
                        field.setAccessible(true);
                        if (field.get(activity) == null) {
                            field.set(activity, Uri.EMPTY);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            @Override public void onActivityStarted(@androidx.annotation.NonNull Activity activity) {}
            @Override public void onActivityResumed(@androidx.annotation.NonNull Activity activity) {}
            @Override public void onActivityPaused(@androidx.annotation.NonNull Activity activity) {}
            @Override public void onActivityStopped(@androidx.annotation.NonNull Activity activity) {}
            @Override public void onActivitySaveInstanceState(@androidx.annotation.NonNull Activity activity, @androidx.annotation.NonNull Bundle outState) {
                // Double protection: Ensure the bundle key doesn't have a null value that causes issues later
                if (activity.getClass().getName().endsWith(".FeedbackActivity")) {
                    String screenshotKey = "com.google.firebase.appdistribution.FeedbackActivity.SCREENSHOT_URI";
                    if (outState.containsKey(screenshotKey) && outState.getString(screenshotKey) == null) {
                        outState.putString(screenshotKey, "");
                    }
                }
            }
            @Override public void onActivityDestroyed(@androidx.annotation.NonNull Activity activity) {}
        });
    }

    /**
     * Schedule the background notification worker to run periodically
     */
    private void scheduleLearningNotificationWorker() {
        // Create constraints: we want it to run when device is idle if possible, but not strictly required
        androidx.work.Constraints constraints = new androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .build();

        // Create a periodic work request that runs daily
        // We use a flex interval to allow the system to batch it
        PeriodicWorkRequest notificationWorkRequest =
                new PeriodicWorkRequest.Builder(
                        LearningNotificationWorker.class,
                        24, TimeUnit.HOURS,
                        6, TimeUnit.HOURS // Flex interval
                )
                .setConstraints(constraints)
                .addTag("learning_notifications_task")
                .build();

        // Schedule the work, replacing any existing work to ensure new logic is applied
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "learning_notifications",
                ExistingPeriodicWorkPolicy.REPLACE,
                notificationWorkRequest
        );
    }
}

