package com.edulinguaghana;

import android.app.Activity;
import android.app.Application;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.Calendar;

/**
 * Applies the app's morning/day/night background consistently across screens.
 */
public final class TimeBasedBackgroundManager {

    private TimeBasedBackgroundManager() {
        // No instances.
    }

    public static void register(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, android.os.Bundle savedInstanceState) {
                applyTo(activity);
            }

            @Override public void onActivityStarted(@NonNull Activity activity) {}

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                applyTo(activity);
            }

            @Override public void onActivityPaused(@NonNull Activity activity) {}
            @Override public void onActivityStopped(@NonNull Activity activity) {}
            @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull android.os.Bundle outState) {}
            @Override public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }

    public static void applyTo(@NonNull Activity activity) {
        if (!shouldApply(activity)) return;

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) return;

        if (content.getChildCount() == 0) {
            content.post(() -> applyTo(activity));
            return;
        }

        View root = content.getChildAt(0);
        if (root == null) return;

        root.setBackgroundResource(getBackgroundResId(activity));
    }

    private static boolean shouldApply(Activity activity) {
        if (activity == null) return false;
        if (activity instanceof SplashActivity) return false;
        // Keep this scoped to the app's own activities; skip third-party screens.
        String packageName = activity.getPackageName();
        String activityPackage = activity.getClass().getPackage() != null
                ? activity.getClass().getPackage().getName()
                : "";
        return activityPackage.startsWith(packageName);
    }

    private static int getBackgroundResId(Activity activity) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 11) {
            return R.drawable.bg_main_morning;
        } else if (hour >= 11 && hour < 17) {
            return R.drawable.bg_main_day;
        } else {
            return R.drawable.bg_main_night;
        }
    }
}


