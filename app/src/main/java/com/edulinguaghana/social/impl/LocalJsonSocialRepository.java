package com.edulinguaghana.social.impl;

import com.edulinguaghana.social.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class LocalJsonSocialRepository implements SocialRepository {
    private final File file;
    private final Gson gson;

    static class Snapshot {
        List<Friend> friends = new ArrayList<>();
        List<Challenge> challenges = new ArrayList<>();
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        List<AchievementShare> shares = new ArrayList<>();
    }

    public LocalJsonSocialRepository(File file) {
        this.file = file;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private synchronized Snapshot load() {
        try {
            if (!file.exists() || file.length() == 0) return new Snapshot();
            FileReader fr = new FileReader(file);
            Type t = new TypeToken<Snapshot>(){}.getType();
            Snapshot s = gson.fromJson(fr, t);
            fr.close();
            if (s == null) s = new Snapshot();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return new Snapshot();
        }
    }

    private synchronized void save(Snapshot s) {
        try {
            FileWriter fw = new FileWriter(file);
            gson.toJson(s, fw);
            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Friend> getFriends(String userId) {
        Snapshot s = load();
        List<Friend> result = new ArrayList<>();
        for (Friend f : s.friends) {
            if (f.userId != null && f.userId.equals(userId) && f.status == Friend.Status.ACCEPTED) result.add(f);
        }
        return result;
    }

    @Override
    public Friend addFriend(String requesterId, String friendId) {
        if (requesterId.equals(friendId)) throw new IllegalArgumentException("cannot add self");
        Snapshot s = load();
        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        Friend f = new Friend(id, requesterId, friendId, null, Friend.Status.PENDING, now, null);
        s.friends.add(f);
        save(s);
        return f;
    }

    @Override
    public boolean removeFriend(String userId, String friendId) {
        Snapshot s = load();
        List<Friend> remaining = new ArrayList<>();
        boolean removed = false;
        for (Friend f : s.friends) {
            if ((f.userId != null && f.userId.equals(userId) && f.friendUserId != null && f.friendUserId.equals(friendId)) ||
                (f.userId != null && f.userId.equals(friendId) && f.friendUserId != null && f.friendUserId.equals(userId))) {
                removed = true;
            } else {
                remaining.add(f);
            }
        }
        s.friends = remaining;
        save(s);
        return removed;
    }

    @Override
    public Friend acceptFriend(String userId, String requesterId) {
        Snapshot s = load();
        for (Friend f : s.friends) {
            if (f.userId != null && f.userId.equals(requesterId) && f.friendUserId != null && f.friendUserId.equals(userId) && f.status == Friend.Status.PENDING) {
                f.status = Friend.Status.ACCEPTED;
                f.acceptedAt = System.currentTimeMillis();
                // add reciprocal
                String id2 = UUID.randomUUID().toString();
                Friend f2 = new Friend(id2, userId, requesterId, null, Friend.Status.ACCEPTED, f.requestedAt, f.acceptedAt);
                s.friends.add(f2);
                save(s);
                return f;
            }
        }
        save(s);
        return null;
    }

    @Override
    public List<Friend> getFriendRequests(String userId) {
        Snapshot s = load();
        List<Friend> result = new ArrayList<>();
        for (Friend f : s.friends) {
            if (f.friendUserId != null && f.friendUserId.equals(userId) && f.status == Friend.Status.PENDING) result.add(f);
        }
        return result;
    }

    @Override
    public Challenge createChallenge(Challenge challenge) {
        Snapshot s = load();
        String id = UUID.randomUUID().toString();
        challenge.id = id;
        challenge.state = Challenge.State.PENDING;
        challenge.createdAt = System.currentTimeMillis();
        s.challenges.add(challenge);
        save(s);
        return challenge;
    }

    @Override
    public List<Challenge> getChallengesForUser(String userId) {
        Snapshot s = load();
        List<Challenge> result = new ArrayList<>();
        for (Challenge c : s.challenges) {
            if ((c.challengerId != null && c.challengerId.equals(userId)) || (c.challengedId != null && c.challengedId.equals(userId))) result.add(c);
        }
        return result;
    }

    @Override
    public Challenge updateChallenge(Challenge challenge) {
        if (challenge.id == null) throw new IllegalArgumentException("challenge id required");
        Snapshot s = load();
        List<Challenge> updated = new ArrayList<>();
        boolean found = false;
        for (Challenge c : s.challenges) {
            if (challenge.id.equals(c.id)) {
                updated.add(challenge);
                found = true;
            } else {
                updated.add(c);
            }
        }
        if (!found) updated.add(challenge);
        s.challenges = updated;
        save(s);
        return challenge;
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(String quizId, int limit) {
        Snapshot s = load();
        List<LeaderboardEntry> list = new ArrayList<>();
        for (LeaderboardEntry e : s.leaderboard) {
            if (quizId == null || (e.quizId != null && e.quizId.equals(quizId))) list.add(e);
        }
        Collections.sort(list, new Comparator<LeaderboardEntry>() {
            @Override
            public int compare(LeaderboardEntry a, LeaderboardEntry b) {
                return Integer.compare(b.score, a.score);
            }
        });
        if (list.size() > limit) list = new ArrayList<>(list.subList(0, limit));
        int rank = 1;
        for (LeaderboardEntry e : list) e.rank = rank++;
        return list;
    }

    @Override
    public AchievementShare addAchievementShare(AchievementShare share) {
        Snapshot s = load();
        if (share.id == null) share.id = UUID.randomUUID().toString();
        share.timestamp = System.currentTimeMillis();
        s.shares.add(share);
        save(s);
        return share;
    }
}

