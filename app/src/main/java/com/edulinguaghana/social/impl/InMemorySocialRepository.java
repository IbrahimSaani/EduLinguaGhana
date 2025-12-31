package com.edulinguaghana.social.impl;

import com.edulinguaghana.social.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySocialRepository implements SocialRepository {
    private final Map<String, Friend> friends = new ConcurrentHashMap<>(); // key: friend.id
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final Map<String, LeaderboardEntry> leaderboard = new ConcurrentHashMap<>();
    private final Map<String, AchievementShare> shares = new ConcurrentHashMap<>();

    @Override
    public List<Friend> getFriends(String userId) {
        List<Friend> result = new ArrayList<>();
        for (Friend f : friends.values()) {
            if (f.userId != null && f.userId.equals(userId) && f.status == Friend.Status.ACCEPTED) {
                result.add(f);
            }
        }
        return result;
    }

    @Override
    public Friend addFriend(String requesterId, String friendId) {
        if (requesterId.equals(friendId)) throw new IllegalArgumentException("cannot add self");
        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        Friend f = new Friend(id, requesterId, friendId, null, Friend.Status.PENDING, now, null);
        friends.put(id, f);
        return f;
    }

    @Override
    public boolean removeFriend(String userId, String friendId) {
        List<String> toRemove = new ArrayList<>();
        for (Friend f : friends.values()) {
            if ((f.userId != null && f.userId.equals(userId) && f.friendUserId != null && f.friendUserId.equals(friendId)) ||
                (f.userId != null && f.userId.equals(friendId) && f.friendUserId != null && f.friendUserId.equals(userId))) {
                toRemove.add(f.id);
            }
        }
        for (String id : toRemove) friends.remove(id);
        return !toRemove.isEmpty();
    }

    @Override
    public Friend acceptFriend(String userId, String requesterId) {
        for (Friend f : friends.values()) {
            if (f.userId != null && f.userId.equals(requesterId) && f.friendUserId != null && f.friendUserId.equals(userId) && f.status == Friend.Status.PENDING) {
                f.status = Friend.Status.ACCEPTED;
                f.acceptedAt = System.currentTimeMillis();
                // Also create reciprocal accepted friend entry
                String id2 = UUID.randomUUID().toString();
                Friend f2 = new Friend(id2, userId, requesterId, null, Friend.Status.ACCEPTED, f.requestedAt, f.acceptedAt);
                friends.put(id2, f2);
                return f;
            }
        }
        return null;
    }

    @Override
    public List<Friend> getFriendRequests(String userId) {
        List<Friend> result = new ArrayList<>();
        for (Friend f : friends.values()) {
            if (f.friendUserId != null && f.friendUserId.equals(userId) && f.status == Friend.Status.PENDING) {
                result.add(f);
            }
        }
        return result;
    }

    @Override
    public Challenge createChallenge(Challenge challenge) {
        String id = UUID.randomUUID().toString();
        challenge.id = id;
        challenge.state = Challenge.State.PENDING;
        challenge.createdAt = System.currentTimeMillis();
        challenges.put(id, challenge);
        return challenge;
    }

    @Override
    public List<Challenge> getChallengesForUser(String userId) {
        List<Challenge> result = new ArrayList<>();
        for (Challenge c : challenges.values()) {
            if ((c.challengerId != null && c.challengerId.equals(userId)) || (c.challengedId != null && c.challengedId.equals(userId))) {
                result.add(c);
            }
        }
        return result;
    }

    @Override
    public Challenge updateChallenge(Challenge challenge) {
        if (challenge.id == null) throw new IllegalArgumentException("challenge id required");
        challenges.put(challenge.id, challenge);
        return challenge;
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(String quizId, int limit) {
        List<LeaderboardEntry> list = new ArrayList<>();
        for (LeaderboardEntry e : leaderboard.values()) {
            if (quizId == null || (e.quizId != null && e.quizId.equals(quizId))) {
                list.add(e);
            }
        }
        // sort descending by score
        Collections.sort(list, new Comparator<LeaderboardEntry>() {
            @Override
            public int compare(LeaderboardEntry a, LeaderboardEntry b) {
                return Integer.compare(b.score, a.score);
            }
        });
        if (list.size() > limit) {
            list = new ArrayList<>(list.subList(0, limit));
        }
        int rank = 1;
        for (LeaderboardEntry e : list) {
            e.rank = rank++;
        }
        return list;
    }

    @Override
    public AchievementShare addAchievementShare(AchievementShare share) {
        if (share.id == null) share.id = UUID.randomUUID().toString();
        share.timestamp = System.currentTimeMillis();
        shares.put(share.id, share);
        return share;
    }
}
