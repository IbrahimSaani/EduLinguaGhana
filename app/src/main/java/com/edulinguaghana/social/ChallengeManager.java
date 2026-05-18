package com.edulinguaghana.social;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

/**
 * Manager for challenge operations including creation, scoring, and statistics
 */
public class ChallengeManager {
    private static final String TAG = "ChallengeManager";
    private static final String CHALLENGES_PATH = "challenges";
    private static final String CHALLENGE_STATS_PATH = "challengeStats";

    private DatabaseReference dbRef;

    public ChallengeManager() {
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Creates a new challenge with specified parameters
     */
    public void createChallenge(String challengerId, String challengedId, String language,
                               String quizType, Long durationMinutes, Integer hearts,
                               ChallengeCreationCallback callback) {
        try {
            Challenge challenge = new Challenge();
            challenge.id = UUID.randomUUID().toString();
            challenge.challengerId = challengerId;
            challenge.challengedId = challengedId;
            challenge.language = language;
            challenge.quizType = quizType;
            challenge.durationMinutes = durationMinutes != null ? durationMinutes : 60L; // Use as seconds internally now
            challenge.hearts = hearts;
            challenge.state = Challenge.State.PENDING;
            challenge.createdAt = System.currentTimeMillis();
            challenge.expiresAt = challenge.createdAt + (challenge.durationMinutes * 1000);
            challenge.quizId = generateQuizId(quizType);

            // Save to Firebase
            dbRef.child(CHALLENGES_PATH).child(challenge.id).setValue(challenge)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Challenge created: " + challenge.id);
                    if (callback != null) {
                        callback.onSuccess(challenge);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create challenge", e);
                    if (callback != null) {
                        callback.onFailure(e.getMessage());
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "Error creating challenge", e);
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        }
    }

    /**
     * Records a player's score for a challenge
     */
    public void recordScore(String challengeId, String playerId, int score,
                          ScoreRecordingCallback callback) {
        dbRef.child(CHALLENGES_PATH).child(challengeId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        if (callback != null) {
                            callback.onFailure("Challenge not found");
                        }
                        return;
                    }

                    Challenge challenge = snapshot.getValue(Challenge.class);
                    if (challenge == null) {
                        if (callback != null) {
                            callback.onFailure("Invalid challenge data");
                        }
                        return;
                    }

                    // Record score based on which player submitted
                    if (playerId.equals(challenge.challengerId)) {
                        challenge.challengerScore = score;
                    } else if (playerId.equals(challenge.challengedId)) {
                        challenge.challengedScore = score;
                    } else {
                        if (callback != null) {
                            callback.onFailure("Player not part of this challenge");
                        }
                        return;
                    }

                    // If both scores are recorded, determine winner and update state
                    if (challenge.challengerScore != null && challenge.challengedScore != null) {
                        challenge.state = Challenge.State.COMPLETED;
                        challenge.determineWinner();

                        // Update challenge stats for both players
                        updateChallengeStats(challenge, () -> {
                            // Save updated challenge
                            dbRef.child(CHALLENGES_PATH).child(challengeId).setValue(challenge)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Challenge completed with winner: " + challenge.winnerId);
                                    if (callback != null) {
                                        callback.onSuccess(challenge);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) {
                                        callback.onFailure(e.getMessage());
                                    }
                                });
                        });
                    } else {
                        // Just one player's score recorded, keep state as ONGOING
                        challenge.state = Challenge.State.ONGOING;
                        dbRef.child(CHALLENGES_PATH).child(challengeId).setValue(challenge)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Score recorded for player: " + playerId);
                                if (callback != null) {
                                    callback.onSuccess(challenge);
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) {
                                    callback.onFailure(e.getMessage());
                                }
                            });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (callback != null) {
                        callback.onFailure(error.getMessage());
                    }
                }
            });
    }

    /**
     * Updates challenge statistics for both players
     */
    private void updateChallengeStats(Challenge challenge, Runnable onComplete) {
        // Update challenger stats
        updatePlayerStats(challenge.challengerId, challenge.winnerId, () -> {
            // Update challenged player stats
            updatePlayerStats(challenge.challengedId, challenge.winnerId, onComplete);
        });
    }

    /**
     * Updates challenge stats for a single player
     */
    private void updatePlayerStats(String userId, String winnerId, Runnable onComplete) {
        dbRef.child(CHALLENGE_STATS_PATH).child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    ChallengeStats stats;
                    if (snapshot.exists()) {
                        stats = snapshot.getValue(ChallengeStats.class);
                        if (stats == null) {
                            stats = new ChallengeStats(userId);
                        }
                    } else {
                        stats = new ChallengeStats(userId);
                    }

                    // Record result
                    stats.recordChallengeResult(winnerId);

                    // Save updated stats
                    dbRef.child(CHALLENGE_STATS_PATH).child(userId).setValue(stats)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Updated stats for user: " + userId);
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to update stats for user: " + userId, e);
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Database error updating stats", error.toException());
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
    }

    /**
     * Fetches challenge stats for a user
     */
    public void getChallengeStats(String userId, StatsCallback callback) {
        dbRef.child(CHALLENGE_STATS_PATH).child(userId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    ChallengeStats stats;
                    if (snapshot.exists()) {
                        stats = snapshot.getValue(ChallengeStats.class);
                        if (stats == null) {
                            stats = new ChallengeStats(userId);
                        }
                    } else {
                        stats = new ChallengeStats(userId);
                    }

                    if (callback != null) {
                        callback.onSuccess(stats);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (callback != null) {
                        callback.onFailure(error.getMessage());
                    }
                }
            });
    }

    /**
     * Generates a quiz ID based on quiz type
     */
    private String generateQuizId(String quizType) {
        if (quizType == null) {
            return "vocabulary";
        }

        switch (quizType.toLowerCase()) {
            case "MULTIPLE_CHOICE":
            case "multiple_choice":
                return "vocab_quiz";
            case "TRUE_FALSE":
            case "true_false":
                return "true_false_quiz";
            case "FILL_BLANK":
            case "fill_blank":
                return "fill_quiz";
            case "MATCHING":
            case "matching":
                return "match_quiz";
            default:
                return "vocabulary";
        }
    }

    /**
     * Declines a challenge
     */
    public void declineChallenge(String challengeId, Runnable onComplete) {
        dbRef.child(CHALLENGES_PATH).child(challengeId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Challenge challenge = snapshot.getValue(Challenge.class);
                    if (challenge != null) {
                        challenge.state = Challenge.State.CANCELLED;
                        dbRef.child(CHALLENGES_PATH).child(challengeId).setValue(challenge);
                        
                        // Increment decline count for the challenged player
                        incrementDeclineCount(challenge.challengedId, onComplete);
                    } else if (onComplete != null) {
                        onComplete.run();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (onComplete != null) onComplete.run();
                }
            });
    }

    private void incrementDeclineCount(String userId, Runnable onComplete) {
        dbRef.child(CHALLENGE_STATS_PATH).child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    ChallengeStats stats = snapshot.exists() ? snapshot.getValue(ChallengeStats.class) : new ChallengeStats(userId);
                    if (stats != null) {
                        stats.recordDecline();
                        dbRef.child(CHALLENGE_STATS_PATH).child(userId).setValue(stats);
                    }
                    if (onComplete != null) onComplete.run();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (onComplete != null) onComplete.run();
                }
            });
    }
    public void getCompletedChallenges(String userId, ChallengesCallback callback) {
        dbRef.child(CHALLENGES_PATH)
            .orderByChild("state")
            .equalTo(Challenge.State.COMPLETED.toString())
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    java.util.List<Challenge> challenges = new java.util.ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Challenge challenge = child.getValue(Challenge.class);
                        if (challenge != null &&
                            (challenge.challengerId.equals(userId) || challenge.challengedId.equals(userId))) {
                            challenges.add(challenge);
                        }
                    }

                    if (callback != null) {
                        callback.onSuccess(challenges);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (callback != null) {
                        callback.onFailure(error.getMessage());
                    }
                }
            });
    }

    // Callbacks
    public interface ChallengeCreationCallback {
        void onSuccess(Challenge challenge);
        void onFailure(String error);
    }

    public interface ScoreRecordingCallback {
        void onSuccess(Challenge challenge);
        void onFailure(String error);
    }

    public interface StatsCallback {
        void onSuccess(ChallengeStats stats);
        void onFailure(String error);
    }

    public interface ChallengesCallback {
        void onSuccess(java.util.List<Challenge> challenges);
        void onFailure(String error);
    }
}

