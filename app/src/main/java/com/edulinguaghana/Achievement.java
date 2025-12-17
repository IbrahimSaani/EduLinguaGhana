package com.edulinguaghana;

public class Achievement {
    private String id;
    private String title;
    private String description;
    private String emoji;
    private AchievementType type;
    private int requiredValue;
    private boolean isUnlocked;
    private long unlockedTimestamp;

    public enum AchievementType {
        QUIZ_COUNT,
        HIGH_SCORE,
        PERFECT_SCORE,
        STREAK,
        ACCURACY,
        SPEED
    }

    public Achievement() {
        // Required for Firebase
    }

    public Achievement(String id, String title, String description, String emoji,
                      AchievementType type, int requiredValue) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.emoji = emoji;
        this.type = type;
        this.requiredValue = requiredValue;
        this.isUnlocked = false;
        this.unlockedTimestamp = 0;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getEmoji() { return emoji; }
    public AchievementType getType() { return type; }
    public int getRequiredValue() { return requiredValue; }
    public boolean isUnlocked() { return isUnlocked; }
    public long getUnlockedTimestamp() { return unlockedTimestamp; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public void setType(AchievementType type) { this.type = type; }
    public void setRequiredValue(int requiredValue) { this.requiredValue = requiredValue; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
    public void setUnlockedTimestamp(long unlockedTimestamp) { this.unlockedTimestamp = unlockedTimestamp; }

    public void unlock() {
        this.isUnlocked = true;
        this.unlockedTimestamp = System.currentTimeMillis();
    }
}

