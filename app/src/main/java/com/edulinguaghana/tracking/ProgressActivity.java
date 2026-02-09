package com.edulinguaghana.tracking;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single learning activity/event
 */
public class ProgressActivity {
    private String id;
    private String userId;
    private ActivityType activityType;
    private long timestamp;
    private int score;
    private int correctAnswers;
    private int totalQuestions;
    private int xpEarned;
    private String mode; // "alphabet", "numbers", "quiz", etc.
    private long durationSeconds;
    private Map<String, Object> metadata;

    public enum ActivityType {
        QUIZ_COMPLETED,
        LESSON_COMPLETED,
        ACHIEVEMENT_UNLOCKED,
        STREAK_MILESTONE,
        XP_EARNED,
        CHALLENGE_COMPLETED,
        BADGE_EARNED
    }

    public ProgressActivity() {
        // Required for Firebase
        this.metadata = new HashMap<>();
    }

    public ProgressActivity(String id, String userId, ActivityType activityType,
                           long timestamp, int score, int correctAnswers,
                           int totalQuestions, int xpEarned, String mode,
                           long durationSeconds) {
        this.id = id;
        this.userId = userId;
        this.activityType = activityType;
        this.timestamp = timestamp;
        this.score = score;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.xpEarned = xpEarned;
        this.mode = mode;
        this.durationSeconds = durationSeconds;
        this.metadata = new HashMap<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getXpEarned() { return xpEarned; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}

