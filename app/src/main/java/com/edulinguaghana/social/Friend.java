package com.edulinguaghana.social;

public class Friend {
    public enum Status { PENDING, ACCEPTED, BLOCKED }

    public String id;
    public String userId; // owner
    public String friendUserId; // the other user's id
    public String displayName;
    public String avatarUrl;
    public Status status;
    public long requestedAt;
    public Long acceptedAt; // nullable

    public Friend() {}

    public Friend(String id, String userId, String friendUserId, String displayName, Status status, long requestedAt, Long acceptedAt) {
        this.id = id;
        this.userId = userId;
        this.friendUserId = friendUserId;
        this.displayName = displayName;
        this.status = status;
        this.requestedAt = requestedAt;
        this.acceptedAt = acceptedAt;
    }
}

