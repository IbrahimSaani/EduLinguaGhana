package com.edulinguaghana.social;

public class AchievementShare {
    public String id;
    public String userId;
    public String achievementId;
    public String message;
    public String platform;
    public long timestamp;

    public AchievementShare() {}

    public AchievementShare(String id, String userId, String achievementId, String message, String platform, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.achievementId = achievementId;
        this.message = message;
        this.platform = platform;
        this.timestamp = timestamp;
    }
}

