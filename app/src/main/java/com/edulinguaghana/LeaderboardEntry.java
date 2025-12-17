package com.edulinguaghana;

public class LeaderboardEntry {
    private String userId;
    private String userName;
    private int score;
    private long timestamp;
    private int rank;

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

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setScore(int score) { this.score = score; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setRank(int rank) { this.rank = rank; }
}

