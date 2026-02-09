package com.edulinguaghana.tracking;

import java.util.Map;

/**
 * Aggregated progress statistics for a student
 */
public class ProgressAggregate {
    private String userId;
    private long lastUpdated;

    // Quiz statistics
    private int totalQuizzes;
    private int totalCorrectAnswers;
    private int totalQuestions;
    private int highestScore;
    private double averageScore;
    private double accuracy;

    // XP and Level
    private int totalXP;
    private int currentLevel;

    // Time tracking
    private long totalTimeSpentSeconds;
    private int daysActive;
    private int currentStreak;
    private int longestStreak;

    // Achievements
    private int totalAchievements;
    private int totalBadges;

    // Recent activity (last 7 days)
    private int quizzesThisWeek;
    private int xpThisWeek;
    private double accuracyThisWeek;

    // Recent activity (last 30 days)
    private int quizzesThisMonth;
    private int xpThisMonth;

    public ProgressAggregate() {
        // Required for Firebase
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    public int getTotalQuizzes() { return totalQuizzes; }
    public void setTotalQuizzes(int totalQuizzes) { this.totalQuizzes = totalQuizzes; }

    public int getTotalCorrectAnswers() { return totalCorrectAnswers; }
    public void setTotalCorrectAnswers(int totalCorrectAnswers) { this.totalCorrectAnswers = totalCorrectAnswers; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getHighestScore() { return highestScore; }
    public void setHighestScore(int highestScore) { this.highestScore = highestScore; }

    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public int getTotalXP() { return totalXP; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }

    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }

    public long getTotalTimeSpentSeconds() { return totalTimeSpentSeconds; }
    public void setTotalTimeSpentSeconds(long totalTimeSpentSeconds) { this.totalTimeSpentSeconds = totalTimeSpentSeconds; }

    public int getDaysActive() { return daysActive; }
    public void setDaysActive(int daysActive) { this.daysActive = daysActive; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public int getTotalAchievements() { return totalAchievements; }
    public void setTotalAchievements(int totalAchievements) { this.totalAchievements = totalAchievements; }

    public int getTotalBadges() { return totalBadges; }
    public void setTotalBadges(int totalBadges) { this.totalBadges = totalBadges; }

    public int getQuizzesThisWeek() { return quizzesThisWeek; }
    public void setQuizzesThisWeek(int quizzesThisWeek) { this.quizzesThisWeek = quizzesThisWeek; }

    public int getXpThisWeek() { return xpThisWeek; }
    public void setXpThisWeek(int xpThisWeek) { this.xpThisWeek = xpThisWeek; }

    public double getAccuracyThisWeek() { return accuracyThisWeek; }
    public void setAccuracyThisWeek(double accuracyThisWeek) { this.accuracyThisWeek = accuracyThisWeek; }

    public int getQuizzesThisMonth() { return quizzesThisMonth; }
    public void setQuizzesThisMonth(int quizzesThisMonth) { this.quizzesThisMonth = quizzesThisMonth; }

    public int getXpThisMonth() { return xpThisMonth; }
    public void setXpThisMonth(int xpThisMonth) { this.xpThisMonth = xpThisMonth; }
}

