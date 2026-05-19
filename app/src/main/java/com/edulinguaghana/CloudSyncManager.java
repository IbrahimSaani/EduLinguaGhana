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
import java.util.Set;

public class CloudSyncManager {
    private static final String TAG = "CloudSyncManager";
    private static final String PREF_NAME = "CloudSyncPrefs";
    private static final String KEY_LAST_SYNC = "LAST_SYNC_TIME";
    private static final String EDU_PREFS = "EduLinguaPrefs";
    private static final String GAMIFICATION_PREFS = "gamification_prefs";
    private static final String ACHIEVEMENT_PREFS = "AchievementsPrefs";

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

            // Full gamification snapshots to preserve quests/badges/achievements across devices
            SharedPreferences gamificationPrefs = context.getSharedPreferences(GAMIFICATION_PREFS, Context.MODE_PRIVATE);
            SharedPreferences achievementsPrefs = context.getSharedPreferences(ACHIEVEMENT_PREFS, Context.MODE_PRIVATE);
            SharedPreferences eduPrefs = context.getSharedPreferences(EDU_PREFS, Context.MODE_PRIVATE);

            userData.put("questsJson", gamificationPrefs.getString("quests", null));
            userData.put("badgesJson", gamificationPrefs.getString("badges", null));
            userData.put("achievementsJson", achievementsPrefs.getString("ACHIEVEMENTS_LIST", null));

            userData.put("totalFunGames", eduPrefs.getInt("TOTAL_FUN_GAMES", 0));
            userData.put("speedGamesPlayed", eduPrefs.getInt("SPEED_GAMES_PLAYED", 0));
            userData.put("puzzleGamesPlayed", eduPrefs.getInt("PUZZLE_GAMES_PLAYED", 0));
            userData.put("beatGamesPlayed", eduPrefs.getInt("BEAT_GAMES_PLAYED", 0));
            userData.put("funGameBestScore", eduPrefs.getInt("FUN_GAME_BEST_SCORE", 0));
            Set<String> playedGames = eduPrefs.getStringSet("FUN_GAMES_PLAYED_SET", null);
            if (playedGames != null) {
                userData.put("funGamesPlayedSet", playedGames);
            }

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

                            Integer cloudTotalFunGames = snapshot.child("totalFunGames").getValue(Integer.class);
                            Integer cloudSpeedGames = snapshot.child("speedGamesPlayed").getValue(Integer.class);
                            Integer cloudPuzzleGames = snapshot.child("puzzleGamesPlayed").getValue(Integer.class);
                            Integer cloudBeatGames = snapshot.child("beatGamesPlayed").getValue(Integer.class);
                            Integer cloudFunBest = snapshot.child("funGameBestScore").getValue(Integer.class);

                            int localTotalFunGames = prefs.getInt("TOTAL_FUN_GAMES", 0);
                            int localSpeedGames = prefs.getInt("SPEED_GAMES_PLAYED", 0);
                            int localPuzzleGames = prefs.getInt("PUZZLE_GAMES_PLAYED", 0);
                            int localBeatGames = prefs.getInt("BEAT_GAMES_PLAYED", 0);
                            int localFunBest = prefs.getInt("FUN_GAME_BEST_SCORE", 0);

                            if (cloudTotalFunGames != null) {
                                editor.putInt("TOTAL_FUN_GAMES", Math.max(localTotalFunGames, cloudTotalFunGames));
                            }
                            if (cloudSpeedGames != null) {
                                editor.putInt("SPEED_GAMES_PLAYED", Math.max(localSpeedGames, cloudSpeedGames));
                            }
                            if (cloudPuzzleGames != null) {
                                editor.putInt("PUZZLE_GAMES_PLAYED", Math.max(localPuzzleGames, cloudPuzzleGames));
                            }
                            if (cloudBeatGames != null) {
                                editor.putInt("BEAT_GAMES_PLAYED", Math.max(localBeatGames, cloudBeatGames));
                            }
                            if (cloudFunBest != null) {
                                editor.putInt("FUN_GAME_BEST_SCORE", Math.max(localFunBest, cloudFunBest));
                            }

                            DataSnapshot playedGamesSnapshot = snapshot.child("funGamesPlayedSet");
                            if (playedGamesSnapshot.exists()) {
                                java.util.HashSet<String> restoredSet = new java.util.HashSet<>();
                                for (DataSnapshot child : playedGamesSnapshot.getChildren()) {
                                    String value = child.getValue(String.class);
                                    if (value != null && !value.trim().isEmpty()) {
                                        restoredSet.add(value);
                                    }
                                }
                                if (!restoredSet.isEmpty()) {
                                    editor.putStringSet("FUN_GAMES_PLAYED_SET", restoredSet);
                                }
                            }

                            editor.apply();

                            String questsJson = snapshot.child("questsJson").getValue(String.class);
                            String badgesJson = snapshot.child("badgesJson").getValue(String.class);
                            String achievementsJson = snapshot.child("achievementsJson").getValue(String.class);

                            if (questsJson != null || badgesJson != null) {
                                SharedPreferences.Editor gamificationEditor =
                                        context.getSharedPreferences(GAMIFICATION_PREFS, Context.MODE_PRIVATE).edit();
                                if (questsJson != null) gamificationEditor.putString("quests", questsJson);
                                if (badgesJson != null) gamificationEditor.putString("badges", badgesJson);
                                gamificationEditor.apply();
                            }

                            if (achievementsJson != null) {
                                context.getSharedPreferences(ACHIEVEMENT_PREFS, Context.MODE_PRIVATE)
                                        .edit()
                                        .putString("ACHIEVEMENTS_LIST", achievementsJson)
                                        .apply();
                            }

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
        FirebaseUser user = auth.getCurrentUser();
        if (userId == null || user == null) {
            callback.onSyncComplete(false, "User not authenticated");
            return;
        }

        // Check user role - only students should be on the leaderboard
        com.edulinguaghana.roles.RoleManager roleManager = new com.edulinguaghana.roles.RoleManager();
        roleManager.getUserRole(context, userId, new com.edulinguaghana.roles.RoleManager.RoleCallback() {
            @Override
            public void onRoleRetrieved(com.edulinguaghana.roles.UserRole role) {
                if (role != com.edulinguaghana.roles.UserRole.STUDENT) {
                    Log.d(TAG, "Non-student user (" + role + ") tried to upload to leaderboard. Skipping.");
                    // Return success but with a message indicating it's for students
                    callback.onSyncComplete(true, "Great job! (Leaderboard is for students)");
                    return;
                }

                // Get username - prioritize parameter, then Firebase display name, then email, then Anonymous
                String finalUserName = userName;
                if (finalUserName == null || finalUserName.isEmpty() || finalUserName.equals("Anonymous")) {
                    if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                        finalUserName = user.getDisplayName();
                    } else if (user.getEmail() != null) {
                        // Use email prefix (before @)
                        finalUserName = user.getEmail().split("@")[0];
                    } else {
                        finalUserName = "User" + userId.substring(0, Math.min(6, userId.length()));
                    }
                }

                Map<String, Object> leaderboardEntry = new HashMap<>();
                leaderboardEntry.put("userId", userId);
                leaderboardEntry.put("userName", finalUserName);
                leaderboardEntry.put("score", score);
                leaderboardEntry.put("timestamp", System.currentTimeMillis());

                Log.d(TAG, "Uploading score to leaderboard - User: " + finalUserName + ", Score: " + score);

                // Upload to leaderboard
                databaseRef.child("leaderboard").child(userId)
                    .setValue(leaderboardEntry)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Score uploaded to leaderboard successfully");
                        callback.onSyncComplete(true, "Score submitted!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to upload score: " + e.getMessage(), e);
                        callback.onSyncComplete(false, "Upload failed: " + e.getMessage());
                    });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to verify user role for leaderboard: " + error);
                callback.onSyncComplete(false, "Verification failed: " + error);
            }
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

    /**
     * Static helper method to upload score from anywhere in the app
     * @param user Firebase user (must be logged in)
     * @param score Quiz score to upload
     * @param context Application context
     */
    public static void uploadScoreToLeaderboard(FirebaseUser user, int score, Context context) {
        if (user == null) {
            android.widget.Toast.makeText(context, "Please log in to upload scores", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        CloudSyncManager manager = new CloudSyncManager(context);
        if (!manager.canSync()) {
            android.widget.Toast.makeText(context, "Internet connection required", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Get username
        String userName = user.getDisplayName();
        if (userName == null || userName.isEmpty()) {
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            userName = prefs.getString("USER_NAME", "Anonymous");
        }

        final String finalUserName = userName;
        manager.uploadToLeaderboard(userName, score, (success, message) -> {
            if (success) {
                android.widget.Toast.makeText(context, "Score " + score + " uploaded to leaderboard!", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                android.widget.Toast.makeText(context, "Upload failed: " + message, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}

