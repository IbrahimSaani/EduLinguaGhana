package com.edulinguaghana;

public class Notification {
    private String id;
    private String title;
    private String message;
    private String emoji;
    private long timestamp;
    private NotificationType type;
    private boolean isRead;

    public enum NotificationType {
        ACHIEVEMENT,
        REMINDER,
        STREAK,
        NEW_CONTENT,
        MOTIVATIONAL,
        MILESTONE
    }

    public Notification() {
        // Required for Firebase
    }

    public Notification(String id, String title, String message, String emoji,
                       long timestamp, NotificationType type, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.emoji = emoji;
        this.timestamp = timestamp;
        this.type = type;
        this.isRead = isRead;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getEmoji() { return emoji; }
    public long getTimestamp() { return timestamp; }
    public NotificationType getType() { return type; }
    public boolean isRead() { return isRead; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setType(NotificationType type) { this.type = type; }
    public void setRead(boolean read) { isRead = read; }
}

