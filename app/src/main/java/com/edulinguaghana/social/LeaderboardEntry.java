package com.edulinguaghana.social;

public class LeaderboardEntry {
    public String userId;
    public String displayName;
    public int score;
    public Integer rank;
    public String quizId;
    public long lastUpdated;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String userId, String displayName, int score, Integer rank, String quizId, long lastUpdated) {
        this.userId = userId;
        this.displayName = displayName;
        this.score = score;
        this.rank = rank;
        this.quizId = quizId;
        this.lastUpdated = lastUpdated;
    }
}

