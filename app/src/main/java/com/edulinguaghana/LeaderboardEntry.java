package com.edulinguaghana;

import java.util.Map;

public class LeaderboardEntry {
    private String userId;
    private String userName;
    private int score;
    private long timestamp;
    private int rank;
    private Map<String, Object> avatarData;

    public LeaderboardEntry() {
        // Required for Firebase
    }

    public LeaderboardEntry(String userId, String userName, int score, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.score = score;
        this.timestamp = timestamp;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getScore() { return score; }
    public long getTimestamp() { return timestamp; }
    public int getRank() { return rank; }
    public Map<String, Object> getAvatarData() { return avatarData; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setScore(int score) { this.score = score; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setRank(int rank) { this.rank = rank; }
    public void setAvatarData(Map<String, Object> avatarData) { this.avatarData = avatarData; }
}

