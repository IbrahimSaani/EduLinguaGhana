package com.edulinguaghana.challenge;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ChallengeDatabase - Manages language-specific challenges with Firebase integration
 *
 * Firebase Structure:
 * challenges/
 *   {challengeId}/
 *     challengerId: "{userId}"
 *     challengedId: "{userId}"
 *     language: "en"
 *     quizMode: "letters"
 *     createdAt: 1681000000000
 *     expiresAt: 1681086400000
 *     state: "pending" (pending, accepted, completed)
 *     challengerScore: 0
 *     challengedScore: 0
 *     completedAt: null
 *     winner: null
 */
public class ChallengeDatabase {
    private static final String TAG = "ChallengeDB";

    private DatabaseReference challengesRef;

    // Challenge states
    public enum ChallengeState {
        PENDING("pending"),
        ACCEPTED("accepted"),
        IN_PROGRESS("in_progress"),
        COMPLETED("completed");

        public final String value;

        ChallengeState(String value) {
            this.value = value;
        }

        public static ChallengeState fromValue(String value) {
            for (ChallengeState state : values()) {
                if (state.value.equals(value)) return state;
            }
            return PENDING;
        }
    }

    /**
     * Constructor
     */
    public ChallengeDatabase() {
        this.challengesRef = FirebaseDatabase.getInstance().getReference("challenges");
    }

    /**
     * Create a new challenge
     */
    public void createChallenge(String challengerId, String challengedId, String language,
                               String quizMode, OnCompleteListener listener) {
        if (challengerId == null || challengedId == null || language == null) {
            if (listener != null) listener.onError("Missing required fields");
            return;
        }

        String challengeId = challengesRef.push().getKey();
        if (challengeId == null) {
            if (listener != null) listener.onError("Failed to generate challenge ID");
            return;
        }

        Map<String, Object> challengeData = new HashMap<>();
        challengeData.put("challengerId", challengerId);
        challengeData.put("challengedId", challengedId);
        challengeData.put("language", language);
        challengeData.put("quizMode", quizMode != null ? quizMode : "letters");
        challengeData.put("state", ChallengeState.PENDING.value);
        challengeData.put("createdAt", System.currentTimeMillis());
        challengeData.put("expiresAt", System.currentTimeMillis() + (24 * 60 * 60 * 1000)); // 24 hours
        challengeData.put("challengerScore", 0);
        challengeData.put("challengedScore", 0);

        challengesRef.child(challengeId).setValue(challengeData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Challenge created: " + challengeId);
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating challenge", e);
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    /**
     * Accept a challenge
     */
    public void acceptChallenge(String challengeId, OnCompleteListener listener) {
        challengesRef.child(challengeId).child("state")
                .setValue(ChallengeState.IN_PROGRESS.value)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Challenge accepted: " + challengeId);
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error accepting challenge", e);
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    /**
     * Submit challenge score
     */
    public void submitChallengeScore(String challengeId, String userId, int score,
                                    OnCompleteListener listener) {
        challengesRef.child(challengeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (listener != null) listener.onError("Challenge not found");
                    return;
                }

                String challengerId = snapshot.child("challengerId").getValue(String.class);
                String challengedId = snapshot.child("challengedId").getValue(String.class);

                // Determine which field to update
                String scoreField = userId.equals(challengerId) ? "challengerScore" : "challengedScore";

                // Update score and mark as completed if both submitted
                challengesRef.child(challengeId).child(scoreField).setValue(score)
                        .addOnSuccessListener(aVoid -> {
                            // Check if both players submitted
                            Integer challengerScore = snapshot.child("challengerScore").getValue(Integer.class);
                            Integer challengedScore = snapshot.child("challengedScore").getValue(Integer.class);

                            if (challengerScore != null && challengedScore != null &&
                                (challengerScore > 0 || userId.equals(challengerId)) &&
                                (challengedScore > 0 || userId.equals(challengedId))) {
                                // Both submitted, complete challenge
                                completeChallenge(challengeId, listener);
                            } else if (listener != null) {
                                listener.onSuccess();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error submitting score", e);
                            if (listener != null) listener.onError(e.getMessage());
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error reading challenge", error.toException());
                if (listener != null) listener.onError(error.getMessage());
            }
        });
    }

    /**
     * Complete a challenge
     */
    private void completeChallenge(String challengeId, OnCompleteListener listener) {
        challengesRef.child(challengeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer challengerScore = snapshot.child("challengerScore").getValue(Integer.class);
                Integer challengedScore = snapshot.child("challengedScore").getValue(Integer.class);
                String challengerId = snapshot.child("challengerId").getValue(String.class);
                String challengedId = snapshot.child("challengedId").getValue(String.class);

                String winner = null;
                if (challengerScore != null && challengedScore != null) {
                    if (challengerScore > challengedScore) {
                        winner = challengerId;
                    } else if (challengedScore > challengerScore) {
                        winner = challengedId;
                    }
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("state", ChallengeState.COMPLETED.value);
                updates.put("completedAt", System.currentTimeMillis());
                if (winner != null) {
                    updates.put("winner", winner);
                }

                challengesRef.child(challengeId).updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Challenge completed: " + challengeId);
                            if (listener != null) listener.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error completing challenge", e);
                            if (listener != null) listener.onError(e.getMessage());
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error reading challenge", error.toException());
                if (listener != null) listener.onError(error.getMessage());
            }
        });
    }

    /**
     * Get pending challenges for a user
     */
    public void getPendingChallenges(String userId, OnChallengesLoadedListener listener) {
        Query query = challengesRef.orderByChild("challengedId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Map<String, Object>> challenges = new ArrayList<>();

                for (DataSnapshot challengeSnap : snapshot.getChildren()) {
                    String state = challengeSnap.child("state").getValue(String.class);
                    if ("pending".equals(state)) {
                        Map<String, Object> challenge = new HashMap<>();
                        challenge.put("id", challengeSnap.getKey());
                        challenge.put("challengerId", challengeSnap.child("challengerId").getValue(String.class));
                        challenge.put("language", challengeSnap.child("language").getValue(String.class));
                        challenge.put("quizMode", challengeSnap.child("quizMode").getValue(String.class));
                        challenge.put("createdAt", challengeSnap.child("createdAt").getValue(Long.class));
                        challenges.add(challenge);
                    }
                }

                Log.d(TAG, "Loaded " + challenges.size() + " pending challenges");
                if (listener != null) listener.onChallengesLoaded(challenges);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading challenges", error.toException());
                if (listener != null) listener.onError(error.getMessage());
            }
        });
    }

    /**
     * Get active challenges for a user
     */
    public void getActiveChallenges(String userId, OnChallengesLoadedListener listener) {
        Query query = challengesRef.orderByChild("state").equalTo(ChallengeState.IN_PROGRESS.value);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Map<String, Object>> challenges = new ArrayList<>();

                for (DataSnapshot challengeSnap : snapshot.getChildren()) {
                    String challengerId = challengeSnap.child("challengerId").getValue(String.class);
                    String challengedId = challengeSnap.child("challengedId").getValue(String.class);

                    if (challengerId.equals(userId) || challengedId.equals(userId)) {
                        Map<String, Object> challenge = new HashMap<>();
                        challenge.put("id", challengeSnap.getKey());
                        challenge.put("opponent", challengerId.equals(userId) ? challengedId : challengerId);
                        challenge.put("language", challengeSnap.child("language").getValue(String.class));
                        challenge.put("quizMode", challengeSnap.child("quizMode").getValue(String.class));
                        challenge.put("challengerScore", challengeSnap.child("challengerScore").getValue(Integer.class));
                        challenge.put("challengedScore", challengeSnap.child("challengedScore").getValue(Integer.class));
                        challenges.add(challenge);
                    }
                }

                Log.d(TAG, "Loaded " + challenges.size() + " active challenges");
                if (listener != null) listener.onChallengesLoaded(challenges);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading challenges", error.toException());
                if (listener != null) listener.onError(error.getMessage());
            }
        });
    }

    /**
     * Get completed challenges for a user
     */
    public void getCompletedChallenges(String userId, int limit, OnChallengesLoadedListener listener) {
        Query query = challengesRef.orderByChild("completedAt");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Map<String, Object>> challenges = new ArrayList<>();

                for (DataSnapshot challengeSnap : snapshot.getChildren()) {
                    String state = challengeSnap.child("state").getValue(String.class);
                    if (ChallengeState.COMPLETED.value.equals(state)) {
                        String challengerId = challengeSnap.child("challengerId").getValue(String.class);
                        String challengedId = challengeSnap.child("challengedId").getValue(String.class);

                        if (challengerId.equals(userId) || challengedId.equals(userId)) {
                            Map<String, Object> challenge = new HashMap<>();
                            challenge.put("id", challengeSnap.getKey());
                            challenge.put("opponent", challengerId.equals(userId) ? challengedId : challengerId);
                            challenge.put("language", challengeSnap.child("language").getValue(String.class));
                            challenge.put("result", challengeSnap.child("winner").getValue(String.class).equals(userId) ? "Won" : "Lost");
                            challenge.put("completedAt", challengeSnap.child("completedAt").getValue(Long.class));
                            challenges.add(challenge);
                        }
                    }
                }

                // Sort by completed date (newest first) and limit
                challenges.sort((a, b) -> {
                    Long dateA = (Long) a.get("completedAt");
                    Long dateB = (Long) b.get("completedAt");
                    return dateB.compareTo(dateA);
                });

                List<Map<String, Object>> limited = new ArrayList<>();
                for (int i = 0; i < Math.min(limit, challenges.size()); i++) {
                    limited.add(challenges.get(i));
                }

                Log.d(TAG, "Loaded " + limited.size() + " completed challenges");
                if (listener != null) listener.onChallengesLoaded(limited);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading challenges", error.toException());
                if (listener != null) listener.onError(error.getMessage());
            }
        });
    }

    // ===== Callback Interfaces =====

    public interface OnCompleteListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnChallengesLoadedListener {
        void onChallengesLoaded(List<Map<String, Object>> challenges);
        void onError(String error);
    }
}

