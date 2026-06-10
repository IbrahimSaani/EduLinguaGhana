package com.edulinguaghana.tracking;

import android.content.Context;

import com.edulinguaghana.R;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central service for tracking and syncing student progress to Firebase
 */
public class ProgressTracker {

    private final Context context;
    private final DatabaseReference progressRef;
    private final DatabaseReference aggregatesRef;
    private final DatabaseReference milestonesRef;

    public interface ProgressCallback {
        void onSuccess();
        void onError(String error);
    }

    public ProgressTracker(Context context) {
        this.context = context.getApplicationContext();
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
                if (callback != null) callback.onError(context.getString(R.string.error_feature_locked));
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
                // Update aggregates
                updateAggregates(context, finalUserId);

                // Check for milestones
                checkMilestones(context, finalUserId, score, correctAnswers, totalQuestions);

                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(context.getString(R.string.error_sync_failed));
            });
    }

    /**
     * Log a fun game completion activity
     */
    public void logFunGameCompletion(Context context, String userId, String gameId, int score, long durationSeconds, ProgressCallback callback) {
        if (userId == null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userId = user.getUid();
            } else {
                if (callback != null) callback.onError(context.getString(R.string.error_feature_locked));
                return;
            }
        }

        final String finalUserId = userId;
        String activityId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        // Calculate XP earned (same formula as FunGameProgressManager)
        int xpEarned = Math.max(5, Math.min(50, score / 2 + 8));

        ProgressActivity activity = new ProgressActivity(
            activityId,
            finalUserId,
            ProgressActivity.ActivityType.FUN_GAME_COMPLETED,
            timestamp,
            score,
            0, 0,
            xpEarned,
            gameId,
            durationSeconds
        );

        progressRef.child(finalUserId).child("activities").child(activityId).setValue(activity)
            .addOnSuccessListener(aVoid -> {
                // Update aggregates to push achievements/quests/badges to Firebase
                updateAggregates(context, finalUserId);
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(context.getString(R.string.error_sync_failed));
            });
    }

    public void logFunGameCompletion(Context context, String userId, String gameId, int score, ProgressCallback callback) {
        logFunGameCompletion(context, userId, gameId, score, 0, callback);
    }

    /**
     * Log a lesson completion activity (e.g., finishing an alphabet)
     */
    public void logLessonCompletion(Context context, String userId, String lessonName, String category, long durationSeconds, ProgressCallback callback) {
        if (userId == null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userId = user.getUid();
            } else {
                if (callback != null) callback.onError(context.getString(R.string.error_feature_locked));
                return;
            }
        }

        final String finalUserId = userId;
        String activityId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        // Standard XP for completing a lesson
        int xpEarned = 20;

        ProgressActivity activity = new ProgressActivity(
            activityId,
            finalUserId,
            ProgressActivity.ActivityType.LESSON_COMPLETED,
            timestamp,
            100, // Score as 100% completion
            0, 0,
            xpEarned,
            category + ": " + lessonName,
            durationSeconds
        );

        progressRef.child(finalUserId).child("activities").child(activityId).setValue(activity)
            .addOnSuccessListener(aVoid -> {
                updateAggregates(context, finalUserId);
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(context.getString(R.string.error_sync_failed));
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
                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(context.getString(R.string.error_sync_failed));
            });
    }

    /**
     * Log achievement unlocked
     */
    public void logAchievement(Context context, String userId, String achievementId, String achievementName, ProgressCallback callback) {
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
                // This is a milestone - notify supervisors
                if (context != null) {
                    createMilestone(userId, context.getString(R.string.notification_milestone_achievement, achievementName),
                                  "achievement", achievementId);
                }

                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(context.getString(R.string.error_sync_failed));
            });
    }

    /**
     * Log streak milestone
     */
    public void logStreakMilestone(Context context, String userId, int streakDays, ProgressCallback callback) {
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
                // Create milestone for significant streaks
                if (streakDays >= 7 && streakDays % 7 == 0 && context != null) {
                    createMilestone(userId, context.getString(R.string.notification_milestone_streak, streakDays),
                                  "streak", String.valueOf(streakDays));
                }

                if (callback != null) callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onError(context.getString(R.string.error_sync_failed));
            });
    }

    /**
     * Update aggregated statistics for a user
     */
    public void updateAggregates(Context context, String userId) {
        // First fetch all activities to calculate sums and time-based stats
        progressRef.child(userId).child("activities").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot activitiesSnapshot) {
                long totalTimeSeconds = 0;
                int quizzesThisWeek = 0;
                int xpThisWeek = 0;
                int totalQuizzesCount = 0;
                int totalCorrectCount = 0;
                int totalQuestionsCount = 0;
                int totalFunGamesCount = 0;
                int bestFunGameScore = 0;
                int totalFunGameScore = 0;
                int totalCumulativeScore = 0;
                int maxQuizScore = 0;
                int funGamesThisWeek = 0;
                Map<String, Integer> quizHighScores = new HashMap<>();
                
                long now = System.currentTimeMillis();
                long oneWeekAgo = now - (7L * 24 * 60 * 60 * 1000);

                for (DataSnapshot snapshot : activitiesSnapshot.getChildren()) {
                    ProgressActivity activity = snapshot.getValue(ProgressActivity.class);
                    if (activity != null) {
                        totalTimeSeconds += activity.getDurationSeconds();
                        totalCumulativeScore += activity.getScore();
                        
                        if (activity.getActivityType() == ProgressActivity.ActivityType.QUIZ_COMPLETED) {
                            totalQuizzesCount++;
                            totalCorrectCount += activity.getCorrectAnswers();
                            totalQuestionsCount += activity.getTotalQuestions();
                            
                            if (activity.getScore() > maxQuizScore) {
                                maxQuizScore = activity.getScore();
                            }

                            // Track high scores per mode
                            String mode = activity.getMode();
                            int currentScore = activity.getScore();
                            if (mode != null) {
                                int existingHigh = quizHighScores.getOrDefault(mode, 0);
                                if (currentScore > existingHigh) {
                                    quizHighScores.put(mode, currentScore);
                                }
                            }

                            if (activity.getTimestamp() >= oneWeekAgo) {
                                quizzesThisWeek++;
                                xpThisWeek += activity.getXpEarned();
                            }
                        } else if (activity.getActivityType() == ProgressActivity.ActivityType.FUN_GAME_COMPLETED) {
                            totalFunGamesCount++;
                            totalFunGameScore += activity.getScore();
                            if (activity.getScore() > bestFunGameScore) {
                                bestFunGameScore = activity.getScore();
                            }

                            // Also track individual fun game high scores in the same map
                            String mode = activity.getMode();
                            int currentScore = activity.getScore();
                            if (mode != null) {
                                int existingHigh = quizHighScores.getOrDefault(mode, 0);
                                if (currentScore > existingHigh) {
                                    quizHighScores.put(mode, currentScore);
                                }
                            }
                            
                            if (activity.getTimestamp() >= oneWeekAgo) {
                                funGamesThisWeek++;
                                xpThisWeek += activity.getXpEarned();
                            }
                        }
else if (activity.getActivityType() == ProgressActivity.ActivityType.XP_EARNED) {
                            if (activity.getTimestamp() >= oneWeekAgo) {
                                xpThisWeek += activity.getXpEarned();
                            }
                        }
                    }
                }

                final long finalTotalTime = totalTimeSeconds;
                final int finalQuizzesWeek = quizzesThisWeek;
                final int finalXpWeek = xpThisWeek;
                final int finalQuizzesCount = totalQuizzesCount;
                final int finalCorrectCount = totalCorrectCount;
                final int finalQuestionsCount = totalQuestionsCount;
                final int finalFunGamesCount = totalFunGamesCount;
                final int finalBestFunGameScore = bestFunGameScore;
                final int finalTotalFunGameScore = totalFunGameScore;
                final int finalTotalCumulativeScore = totalCumulativeScore;
                final int finalMaxQuizScore = maxQuizScore;
                final int finalFunGamesWeek = funGamesThisWeek;
                final Map<String, Integer> finalQuizHighScores = quizHighScores;

                aggregatesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        ProgressAggregate aggregate = snapshot.getValue(ProgressAggregate.class);
                        if (aggregate == null) {
                            aggregate = new ProgressAggregate();
                            aggregate.setUserId(userId);
                        }

                        // Get data from local managers for streak and XP
                        try {
                            // XP and level
                            XPState xpState = XPManager.getState(context);
                            if (xpState != null) {
                                aggregate.setTotalXP(xpState.totalXp);
                                aggregate.setCurrentLevel(xpState.level);
                            }

                            // Streak
                            StreakManager streakManager = new StreakManager(context);
                            aggregate.setCurrentStreak(streakManager.getCurrentStreak());
                            aggregate.setLongestStreak(streakManager.getLongestStreak());
                            aggregate.setDaysActive(streakManager.getTotalPracticeDays());

                            // Use calculated values from Firebase activities
                            aggregate.setTotalQuizzes(finalQuizzesCount);
                            aggregate.setTotalCorrectAnswers(finalCorrectCount);
                            aggregate.setTotalQuestions(finalQuestionsCount);
                            aggregate.setTotalTimeSpentSeconds(finalTotalTime);
                            aggregate.setQuizzesThisWeek(finalQuizzesWeek);
                            aggregate.setXpThisWeek(finalXpWeek);
                            aggregate.setHighestScore(Math.max(finalMaxQuizScore, aggregate.getHighestScore()));
                            
                            // Fun Games
                            aggregate.setTotalFunGames(finalFunGamesCount);
                            aggregate.setBestFunGameScore(finalBestFunGameScore);
                            aggregate.setTotalFunGameScore(finalTotalFunGameScore);
                            aggregate.setTotalCumulativeScore(finalTotalCumulativeScore);
                            aggregate.setFunGamesThisWeek(finalFunGamesWeek);
                            aggregate.setQuizHighScores(finalQuizHighScores);

                            // Calculate accuracy
                            if (finalQuestionsCount > 0) {
                                aggregate.setAccuracy((double) finalCorrectCount / finalQuestionsCount * 100);
                            }
                            
                            // High score from local if available
                            int localHighScore = com.edulinguaghana.ProgressManager.getHighScore(context);
                            if (localHighScore > aggregate.getHighestScore()) {
                                aggregate.setHighestScore(localHighScore);
                            }

                            // Badges and Achievements
                            com.edulinguaghana.AchievementManager achievementManager = new com.edulinguaghana.AchievementManager(context);
                            aggregate.setTotalAchievements(achievementManager.getUnlockedCount());
                            
                            java.util.List<com.edulinguaghana.gamification.Badge> badges = com.edulinguaghana.gamification.BadgeManager.getAllBadges(context);
                            int unlockedBadges = 0;
                            for (com.edulinguaghana.gamification.Badge b : badges) if (b.unlocked) unlockedBadges++;
                            aggregate.setTotalBadges(unlockedBadges);

                            aggregate.setLastUpdated(System.currentTimeMillis());

                            // Save back to Firebase
                            aggregatesRef.child(userId).setValue(aggregate);

                            // Also sync other data
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                            userRef.child("badges").setValue(badges);
                            java.util.List<com.edulinguaghana.gamification.Quest> quests = com.edulinguaghana.gamification.QuestManager.getDailyQuests(context);
                            userRef.child("quests").setValue(quests);
                            userRef.child("achievements").setValue(achievementManager.getAllAchievements());

                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }


    /**
     * Check for milestone achievements
     */
    private void checkMilestones(Context context, String userId, int score, int correctAnswers, int totalQuestions) {
        // Perfect score milestone
        if (correctAnswers == totalQuestions && totalQuestions > 0) {
            createMilestone(userId, context.getString(R.string.notification_milestone_perfect_score), "perfect_score", String.valueOf(score));
        }

        // High score milestone (>90%)
        if (totalQuestions > 0) {
            double percentage = (correctAnswers * 100.0) / totalQuestions;
            if (percentage >= 90) {
                createMilestone(userId, context.getString(R.string.notification_milestone_excellent, (int)percentage),
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
            })
            .addOnFailureListener(e -> {
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
                callback.onError(context.getString(R.string.error_data_load_failed));
            }
        });
    }

    public interface ProgressAggregateCallback {
        void onAggregateRetrieved(ProgressAggregate aggregate);
        void onError(String error);
    }
}

