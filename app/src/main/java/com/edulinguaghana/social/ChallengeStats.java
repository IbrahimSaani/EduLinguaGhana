package com.edulinguaghana.social;

/**
 * Tracks challenge statistics for a user
 */
public class ChallengeStats {
    public String userId;
    public int challengesWon;
    public int challengesLost;
    public int challengesTied;
    public int challengesDeclined;
    public int totalChallenges;
    public double winRate;
    public long lastChallengeTime;

    public ChallengeStats() {
        this.challengesWon = 0;
        this.challengesLost = 0;
        this.challengesTied = 0;
        this.challengesDeclined = 0;
        this.totalChallenges = 0;
        this.winRate = 0.0;
        this.lastChallengeTime = 0;
    }

    public ChallengeStats(String userId) {
        this();
        this.userId = userId;
    }

    /**
     * Updates statistics when a challenge is completed
     */
    public void recordChallengeResult(String winnerId) {
        totalChallenges++;
        lastChallengeTime = System.currentTimeMillis();

        if (winnerId == null) {
            // Tie
            challengesTied++;
        } else if (winnerId.equals(userId)) {
            // User won
            challengesWon++;
        } else {
            // User lost
            challengesLost++;
        }

        // Calculate win rate
        if (totalChallenges > 0) {
            winRate = (double) challengesWon / (totalChallenges);
        }
    }

    public void recordDecline() {
        challengesDeclined++;
    }

    public String getFormattedWinRate() {
        return String.format("%.1f%%", winRate * 100);
    }

    public String getStats() {
        return challengesWon + "W - " + challengesLost + "L - " + challengesTied + "T";
    }
}

