package com.edulinguaghana.social;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Helper class to request notification permission on Android 13+
 */
public class NotificationPermissionHelper {
    private static final String TAG = "NotificationPermission";

    private final AppCompatActivity activity;
    private final ActivityResultLauncher<String> requestPermissionLauncher;
    private PermissionCallback callback;

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    public NotificationPermissionHelper(AppCompatActivity activity) {
        this.activity = activity;
        // Register the launcher directly in the constructor.
        // This must be called when the activity is being initialized (before it is started).
        this.requestPermissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted");
                    if (callback != null) {
                        callback.onPermissionGranted();
                    }
                } else {
                    Log.d(TAG, "Notification permission denied");
                    if (callback != null) {
                        callback.onPermissionDenied();
                    }
                }
            }
        );
    }

    /**
     * Check if notification permission is needed (Android 13+)
     */
    public boolean isPermissionNeeded() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    /**
     * Check if notification permission is granted
     */
    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permission not needed on older versions
    }

    /**
     * Request notification permission with explanation
     */
    public void requestPermission(PermissionCallback callback) {
        if (activity.isFinishing() || activity.isDestroyed()) return;

        this.callback = callback;

        if (!isPermissionNeeded()) {
            // No permission needed on Android 12 and below
            if (callback != null) {
                callback.onPermissionGranted();
            }
            return;
        }

        if (isPermissionGranted()) {
            // Already granted
            if (callback != null) {
                callback.onPermissionGranted();
            }
            return;
        }

        // Show rationale if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showRationaleDialog();
            } else {
                // Request permission directly
                try {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to launch permission request: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Show rationale dialog explaining why permission is needed
     */
    private void showRationaleDialog() {
        com.edulinguaghana.StyledMenuHelper.showStyledConfirmationDialog(
            activity,
            "🔔",
            "Enable Notifications",
            "Get notified when friends send you requests or challenges! You can manage notification settings anytime in your device settings.",
            "Enable",
            "Not Now",
            () -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Failed to launch permission request from dialog: " + e.getMessage());
                    }
                }
            },
            () -> {
                if (callback != null) {
                    callback.onPermissionDenied();
                }
            }
        );
    }

    /**
     * Request permission silently without callback
     */
    public void requestPermissionSilently() {
        requestPermission(new PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Log.d(TAG, "Permission granted silently");
            }

            @Override
            public void onPermissionDenied() {
                Log.d(TAG, "Permission denied silently");
            }
        });
    }
}
