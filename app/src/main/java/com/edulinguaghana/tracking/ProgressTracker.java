package com.edulinguaghana.tracking;

import android.content.Context;
import android.util.Log;

import com.edulinguaghana.StreakManager;
import com.edulinguaghana.gamification.XPState;
import com.edulinguaghana.gamification.XPManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central service for tracking and syncing student progress to Firebase
 */
public class ProgressTracker {
    private static final String TAG = "ProgressTracker";

    private final DatabaseReference progressRef;
    private final DatabaseReference aggregatesRef;
    private final DatabaseReference milestonesRef;

    public interface ProgressCallback {
        void onSuccess();
        void onError(String error);
    }

    public ProgressTracker() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.progressRef = database.getReference("progress");
        this.aggregatesRef = database.getReference("aggregates");
        this.milestonesRef = database.getReference("milestones");
    }

    /**
     * Log a quiz completion activity
     */
    public void logQuizCompletion(Context context, String userId, String mode,
                                  int score, int correctAnswers, int totalQuestions,
                                  long durationSeconds, ProgressCallback callback) {
        if (userId == null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userId = user.getUid();
            } else {
                if (callback != null) callback.onError("User not logged in");
                return;
            }
        }

        final String finalUserId = userId; // Make effectively final for lambda
        String activityId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        // Calculate XP earned (same formula as ProgressManager)
        int xpEarned = Math.max(5, correctAnswers * 2 + score / 5);

        ProgressActivity activity = new ProgressActivity(
            activityId,
            finalUserId,
            ProgressActivity.ActivityType.QUIZ_COMPLETED,
            timestamp,
            score,
            correctAnswers,
            totalQuestions,
            xpEarned,
            mode,
            durationSeconds
        );

        // Save to Firebase
        progressRef.child(finalUserId).child("activities").child(activityId).setValue(activity)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Quiz activity logged: " + activityId);

                // Update aggregates
                updateAggregates(context, finalUserId);

                // Check for milestones
                checkMilestones(finalUserId, score, correctAnswers, totalQuestions);

                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to log quiz activity", e);
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    /**
     * Log XP earned activity
     */
    public void logXPEarned(String userId, int xpAmount, String reason, ProgressCallback callback) {
        if (userId == null) return;

        String activityId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        ProgressActivity activity = new ProgressActivity(
            activityId,
            userId,
            ProgressActivity.ActivityType.XP_EARNED,
            timestamp,
            0, 0, 0,
            xpAmount,
            reason,
            0
        );

        progressRef.child(userId).child("activities").child(activityId).setValue(activity)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "XP activity logged: " + activityId);
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to log XP activity", e);
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    /**
     * Log achievement unlocked
     */
    public void logAchievement(String userId, String achievementId, String achievementName, ProgressCallback callback) {
        if (userId == null) return;

        String activityId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        ProgressActivity activity = new ProgressActivity(
            activityId,
            userId,
            ProgressActivity.ActivityType.ACHIEVEMENT_UNLOCKED,
            timestamp,
            0, 0, 0, 0,
            achievementId,
            0
        );

        activity.getMetadata().put("achievementName", achievementName);

        progressRef.child(userId).child("activities").child(activityId).setValue(activity)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Achievement logged: " + achievementId);

                // This is a milestone - notify supervisors
                createMilestone(userId, "Achievement Unlocked: " + achievementName,
                              "achievement", achievementId);

                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to log achievement", e);
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    /**
     * Log streak milestone
     */
    public void logStreakMilestone(String userId, int streakDays, ProgressCallback callback) {
        if (userId == null) return;

        String activityId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        ProgressActivity activity = new ProgressActivity(
            activityId,
            userId,
            ProgressActivity.ActivityType.STREAK_MILESTONE,
            timestamp,
            streakDays, 0, 0, 0,
            "streak",
            0
        );

        progressRef.child(userId).child("activities").child(activityId).setValue(activity)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Streak milestone logged: " + streakDays);

                // Create milestone for significant streaks
                if (streakDays >= 7 && streakDays % 7 == 0) {
                    createMilestone(userId, streakDays + "-Day Streak!",
                                  "streak", String.valueOf(streakDays));
                }

                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to log streak", e);
                if (callback != null) callback.onError(e.getMessage());
            });
    }

    /**
     * Update aggregated statistics for a user
     */
    private void updateAggregates(Context context, String userId) {
        aggregatesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ProgressAggregate aggregate = snapshot.getValue(ProgressAggregate.class);
                if (aggregate == null) {
                    aggregate = new ProgressAggregate();
                    aggregate.setUserId(userId);
                }

                // Get data from local managers
                try {
                    // XP and level
                    XPState xpState = XPManager.getState(context);
                    if (xpState != null) {
                        aggregate.setTotalXP(xpState.totalXp);
                        aggregate.setCurrentLevel(xpState.level);
                    }

                    // Streak
                    StreakManager streakManager = new StreakManager(context);
                    int currentStreak = streakManager.getCurrentStreak();
                    int longestStreak = streakManager.getLongestStreak();
                    aggregate.setCurrentStreak(currentStreak);
                    aggregate.setLongestStreak(longestStreak);

                    // From ProgressManager (local)
                    int totalQuizzes = com.edulinguaghana.ProgressManager.getTotalQuizzes(context);
                    int totalCorrect = com.edulinguaghana.ProgressManager.getTotalCorrect(context);
                    int highScore = com.edulinguaghana.ProgressManager.getHighScore(context);
                    int accuracy = com.edulinguaghana.ProgressManager.getAccuracy(context);

                    aggregate.setTotalQuizzes(totalQuizzes);
                    aggregate.setTotalCorrectAnswers(totalCorrect);
                    aggregate.setTotalQuestions(totalQuizzes * 10); // Assuming 10 questions per quiz
                    aggregate.setHighestScore(highScore);
                    aggregate.setAccuracy(accuracy);

                    aggregate.setLastUpdated(System.currentTimeMillis());

                    // Save back to Firebase
                    aggregatesRef.child(userId).setValue(aggregate);

                } catch (Exception e) {
                    Log.e(TAG, "Error updating aggregates", e);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to read aggregates", error.toException());
            }
        });
    }

    /**
     * Check for milestone achievements
     */
    private void checkMilestones(String userId, int score, int correctAnswers, int totalQuestions) {
        // Perfect score milestone
        if (correctAnswers == totalQuestions && totalQuestions > 0) {
            createMilestone(userId, "Perfect Score!", "perfect_score", String.valueOf(score));
        }

        // High score milestone (>90%)
        if (totalQuestions > 0) {
            double percentage = (correctAnswers * 100.0) / totalQuestions;
            if (percentage >= 90) {
                createMilestone(userId, "Excellent Performance - " + (int)percentage + "%!",
                              "high_score", String.valueOf(score));
            }
        }
    }

    /**
     * Create a milestone entry for notifications
     */
    private void createMilestone(String userId, String title, String type, String value) {
        String milestoneId = UUID.randomUUID().toString();
        Map<String, Object> milestone = new HashMap<>();
        milestone.put("id", milestoneId);
        milestone.put("userId", userId);
        milestone.put("title", title);
        milestone.put("type", type);
        milestone.put("value", value);
        milestone.put("timestamp", System.currentTimeMillis());
        milestone.put("notified", false);

        milestonesRef.child(userId).child(milestoneId).setValue(milestone)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Milestone created: " + title);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to create milestone", e);
            });
    }

    /**
     * Get progress aggregate for a student
     */
    public void getProgressAggregate(String userId, ProgressAggregateCallback callback) {
        aggregatesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ProgressAggregate aggregate = snapshot.getValue(ProgressAggregate.class);
                if (aggregate == null) {
                    aggregate = new ProgressAggregate();
                    aggregate.setUserId(userId);
                }
                callback.onAggregateRetrieved(aggregate);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public interface ProgressAggregateCallback {
        void onAggregateRetrieved(ProgressAggregate aggregate);
        void onError(String error);
    }
}

