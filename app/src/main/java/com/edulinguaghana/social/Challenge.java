package com.edulinguaghana.social;

import java.util.HashMap;
import java.util.Map;

public class Challenge {
    public enum State { PENDING, ONGOING, COMPLETED, EXPIRED, CANCELLED }

    public String id;
    public String quizId;
    public String challengerId;
    public String challengedId;
    public State state;
    public long createdAt;
    public Long expiresAt;
    public Map<String, Integer> results = new HashMap<>();

    public Challenge() {}

    public Challenge(String id, String quizId, String challengerId, String challengedId, State state, long createdAt, Long expiresAt) {
        this.id = id;
        this.quizId = quizId;
        this.challengerId = challengerId;
        this.challengedId = challengedId;
        this.state = state;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}

