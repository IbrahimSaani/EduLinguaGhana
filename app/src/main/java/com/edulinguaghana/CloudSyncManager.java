package com.edulinguaghana;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CloudSyncManager {
    private static final String TAG = "CloudSyncManager";
    private static final String PREF_NAME = "CloudSyncPrefs";
    private static final String KEY_LAST_SYNC = "LAST_SYNC_TIME";

    private Context context;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    public interface SyncCallback {
        void onSyncComplete(boolean success, String message);
    }

    public CloudSyncManager(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Check if user can sync (logged in and online)
     */
    public boolean canSync() {
        OfflineManager offlineManager = new OfflineManager(context);
        FirebaseUser user = auth.getCurrentUser();
        return user != null && offlineManager.isOnline();
    }

    /**
     * Get current user ID
     */
    private String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Sync all user data to cloud
     */
    public void syncToCloud(SyncCallback callback) {
        if (!canSync()) {
            callback.onSyncComplete(false, "Login and internet required for sync");
            return;
        }

        String userId = getUserId();
        if (userId == null) {
            callback.onSyncComplete(false, "User not authenticated");
            return;
        }

        try {
            // Gather all local data
            Map<String, Object> userData = new HashMap<>();

            // Progress data
            int totalQuizzes = ProgressManager.getTotalQuizzes(context);
            int highScore = ProgressManager.getHighScore(context);
            int totalCorrect = ProgressManager.getTotalCorrect(context);
            int accuracy = ProgressManager.getAccuracy(context);

            userData.put("totalQuizzes", totalQuizzes);
            userData.put("highScore", highScore);
            userData.put("totalCorrect", totalCorrect);
            userData.put("accuracy", accuracy);

            // Streak data
            StreakManager streakManager = new StreakManager(context);
            userData.put("currentStreak", streakManager.getCurrentStreak());
            userData.put("longestStreak", streakManager.getLongestStreak());
            userData.put("totalPracticeDays", streakManager.getTotalPracticeDays());

            // Achievement data
            AchievementManager achievementManager = new AchievementManager(context);
            userData.put("unlockedAchievements", achievementManager.getUnlockedCount());
            userData.put("totalAchievements", achievementManager.getTotalCount());

            // Timestamp
            userData.put("lastSyncTime", System.currentTimeMillis());
            userData.put("deviceType", "Android");

            // Upload to Firebase
            databaseRef.child("users").child(userId).child("progress")
                .setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    saveLastSyncTime();
                    Log.d(TAG, "Data synced to cloud successfully");
                    callback.onSyncComplete(true, "Data synced successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to sync data", e);
                    callback.onSyncComplete(false, "Sync failed: " + e.getMessage());
                });

        } catch (Exception e) {
            Log.e(TAG, "Error syncing data", e);
            callback.onSyncComplete(false, "Error: " + e.getMessage());
        }
    }

    /**
     * Sync data from cloud to local
     */
    public void syncFromCloud(SyncCallback callback) {
        if (!canSync()) {
            callback.onSyncComplete(false, "Login and internet required for sync");
            return;
        }

        String userId = getUserId();
        if (userId == null) {
            callback.onSyncComplete(false, "User not authenticated");
            return;
        }

        databaseRef.child("users").child(userId).child("progress")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {
                            // Get cloud data
                            Integer cloudQuizzes = snapshot.child("totalQuizzes").getValue(Integer.class);
                            Integer cloudHighScore = snapshot.child("highScore").getValue(Integer.class);
                            Integer cloudStreak = snapshot.child("currentStreak").getValue(Integer.class);

                            // Get local data
                            int localQuizzes = ProgressManager.getTotalQuizzes(context);
                            int localHighScore = ProgressManager.getHighScore(context);

                            // Merge logic: Take the maximum values
                            SharedPreferences prefs = context.getSharedPreferences("EduLinguaPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();

                            if (cloudQuizzes != null && cloudQuizzes > localQuizzes) {
                                editor.putInt("TOTAL_QUIZZES", cloudQuizzes);
                            }

                            if (cloudHighScore != null && cloudHighScore > localHighScore) {
                                editor.putInt("HIGH_SCORE", cloudHighScore);
                            }

                            editor.apply();

                            saveLastSyncTime();
                            Log.d(TAG, "Data synced from cloud successfully");
                            callback.onSyncComplete(true, "Data restored from cloud!");

                        } catch (Exception e) {
                            Log.e(TAG, "Error processing cloud data", e);
                            callback.onSyncComplete(false, "Error processing data");
                        }
                    } else {
                        callback.onSyncComplete(true, "No cloud data found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to read cloud data", error.toException());
                    callback.onSyncComplete(false, "Failed to read cloud data");
                }
            });
    }

    /**
     * Upload user score to leaderboard
     */
    public void uploadToLeaderboard(String userName, int score, SyncCallback callback) {
        if (!canSync()) {
            callback.onSyncComplete(false, "Login and internet required");
            return;
        }

        String userId = getUserId();
        if (userId == null) {
            callback.onSyncComplete(false, "User not authenticated");
            return;
        }

        Map<String, Object> leaderboardEntry = new HashMap<>();
        leaderboardEntry.put("userId", userId);
        leaderboardEntry.put("userName", userName);
        leaderboardEntry.put("score", score);
        leaderboardEntry.put("timestamp", System.currentTimeMillis());

        // Upload to leaderboard
        databaseRef.child("leaderboard").child(userId)
            .setValue(leaderboardEntry)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Score uploaded to leaderboard");
                callback.onSyncComplete(true, "Score submitted!");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to upload score", e);
                callback.onSyncComplete(false, "Upload failed");
            });
    }

    /**
     * Get last sync time
     */
    public long getLastSyncTime() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }

    /**
     * Save last sync time
     */
    private void saveLastSyncTime() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply();
    }

    /**
     * Get last sync time as readable string
     */
    public String getLastSyncTimeString() {
        long lastSync = getLastSyncTime();
        if (lastSync == 0) {
            return "Never synced";
        }

        long diff = System.currentTimeMillis() - lastSync;
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            return days + " days ago";
        }
    }
}

