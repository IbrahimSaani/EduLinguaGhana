package com.edulinguaghana.social.impl;

import com.edulinguaghana.social.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lightweight Firebase-backed SocialRepository using Realtime Database.
 * This implementation performs simple synchronous-ish operations by writing and returning the object;
 * callers should listen for updates via Firebase listeners in the UI when real-time updates are required.
 */
public class FirebaseSocialRepository implements SocialRepository {
    private final DatabaseReference rootRef;

    public FirebaseSocialRepository() {
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public List<Friend> getFriends(String userId) {
        // For simplicity return an empty list — recommend using direct Firebase listeners in UI for real time
        return new ArrayList<>();
    }

    @Override
    public Friend addFriend(String requesterId, String friendId) {
        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        Friend f = new Friend(id, requesterId, friendId, null, Friend.Status.PENDING, now, null);
        rootRef.child("friends").child(id).setValue(f);
        return f;
    }

    @Override
    public boolean removeFriend(String userId, String friendId) {
        // simple remove by query: scan children and remove matching entries (not efficient but simple)
        rootRef.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Friend f = child.getValue(Friend.class);
                    if (f != null) {
                        if ((userId.equals(f.userId) && friendId.equals(f.friendUserId)) || (userId.equals(f.friendUserId) && friendId.equals(f.userId))) {
                            child.getRef().removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        return true;
    }

    @Override
    public Friend acceptFriend(String userId, String requesterId) {
        // Mark the request as accepted and add reciprocal entry
        // Find the pending request and update
        rootRef.child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Friend f = child.getValue(Friend.class);
                    if (f != null && requesterId.equals(f.userId) && userId.equals(f.friendUserId) && f.status == Friend.Status.PENDING) {
                        f.status = Friend.Status.ACCEPTED;
                        f.acceptedAt = System.currentTimeMillis();
                        child.getRef().setValue(f);
                        // create reciprocal
                        String id2 = UUID.randomUUID().toString();
                        Friend f2 = new Friend(id2, userId, requesterId, null, Friend.Status.ACCEPTED, f.requestedAt, f.acceptedAt);
                        rootRef.child("friends").child(id2).setValue(f2);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        return null;
    }

    @Override
    public List<Friend> getFriendRequests(String userId) {
        return new ArrayList<>();
    }

    @Override
    public Challenge createChallenge(Challenge challenge) {
        String id = UUID.randomUUID().toString();
        challenge.id = id;
        challenge.state = Challenge.State.PENDING;
        challenge.createdAt = System.currentTimeMillis();
        rootRef.child("challenges").child(id).setValue(challenge);
        return challenge;
    }

    @Override
    public List<Challenge> getChallengesForUser(String userId) {
        return new ArrayList<>();
    }

    @Override
    public Challenge updateChallenge(Challenge challenge) {
        if (challenge.id == null) throw new IllegalArgumentException("challenge id required");
        rootRef.child("challenges").child(challenge.id).setValue(challenge);
        return challenge;
    }

    @Override
    public List<LeaderboardEntry> getLeaderboard(String quizId, int limit) {
        return new ArrayList<>();
    }

    @Override
    public AchievementShare addAchievementShare(AchievementShare share) {
        if (share.id == null) share.id = UUID.randomUUID().toString();
        share.timestamp = System.currentTimeMillis();
        rootRef.child("achievement_shares").child(share.id).setValue(share);
        return share;
    }
}
