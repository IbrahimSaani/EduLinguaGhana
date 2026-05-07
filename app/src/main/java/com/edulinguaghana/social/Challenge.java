package com.edulinguaghana.social;

import java.util.HashMap;
import java.util.Map;

public class Challenge {
    public enum State { PENDING, ONGOING, COMPLETED, EXPIRED, CANCELLED }
    public enum QuizType { MULTIPLE_CHOICE, TRUE_FALSE, FILL_BLANK, MATCHING }

    public String id;
    public String quizId;
    public String challengerId;
    public String challengedId;
    public State state;
    public long createdAt;
    public Long expiresAt;
    public Long durationMinutes;  // Challenge duration in minutes

    // Challenge configuration
    public String language;       // Language code (tw, ee, ga)
    public String quizType;       // Quiz type (vocabulary, etc.)

    // Challenge results
    public Integer challengerScore;  // Challenger's score
    public Integer challengedScore;  // Challenged player's score
    public String winnerId;       // ID of the winner (null if tie/no result yet)

    // Tracking - not persisted to Firebase
    public Map<String, Integer> results = new HashMap<>();

    // Transient fields for UI display (not saved to Firebase)
    public transient String challengerName;
    public transient String challengedName;

    public Challenge() {}

    public Challenge(String id, String quizId, String challengerId, String challengedId,
                     State state, long createdAt, Long expiresAt) {
        this.id = id;
        this.quizId = quizId;
        this.challengerId = challengerId;
        this.challengedId = challengedId;
        this.state = state;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.durationMinutes = 10L;  // Default 10 minutes
    }

    /**
     * Determines the winner based on scores
     * @return Winner ID, or null if no winner yet or scores are tied
     */
    public String determineWinner() {
        if (challengerScore == null || challengedScore == null) {
            return null;  // Not all scores submitted yet
        }

        if (challengerScore > challengedScore) {
            winnerId = challengerId;
        } else if (challengedScore > challengerScore) {
            winnerId = challengedId;
        } else {
            winnerId = null;  // Tie
        }

        return winnerId;
    }

    /**
     * Checks if both players have completed the challenge
     */
    public boolean isFullyCompleted() {
        return challengerScore != null && challengedScore != null && state == State.COMPLETED;
    }
}



