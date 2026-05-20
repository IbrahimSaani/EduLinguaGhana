package com.edulinguaghana.social.impl;

import com.edulinguaghana.social.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        android.util.Log.d("FirebaseSocialRepository", "addFriend called - requesterId: " + requesterId + ", friendId: " + friendId);

        // Check for existing request to prevent duplicates
        rootRef.child("friends").orderByChild("userId").equalTo(requesterId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String targetId = child.child("friendUserId").getValue(String.class);
                        if (friendId.equals(targetId)) {
                            android.util.Log.d("FirebaseSocialRepository", "Friend request already exists");
                            return; 
                        }
                    }
                    
                    // No existing request, create new one
                    String id = UUID.randomUUID().toString();
                    long now = System.currentTimeMillis();
                    
                    Map<String, Object> f = new HashMap<>();
                    f.put("id", id);
                    f.put("userId", requesterId);
                    f.put("friendUserId", friendId);
                    f.put("status", Friend.Status.PENDING.name());
                    f.put("requestedAt", now);

                    rootRef.child("friends").child(id).setValue(f);
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });

        return null;
    }



    @Override
    public boolean removeFriend(String userId, String friendId) {
        // Use a query to find the specific friend records to remove
        rootRef.child("friends").orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Friend f = child.getValue(Friend.class);
                        if (f != null && friendId.equals(f.friendUserId)) {
                            child.getRef().removeValue();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });

        // Remove the reciprocal entry as well
        rootRef.child("friends").orderByChild("userId").equalTo(friendId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Friend f = child.getValue(Friend.class);
                        if (f != null && userId.equals(f.friendUserId)) {
                            child.getRef().removeValue();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });

        return true;
    }

    @Override
    public Friend acceptFriend(String currentUserId, String requesterId) {
        android.util.Log.d("FirebaseSocialRepository", "acceptFriend: " + currentUserId + " accepting " + requesterId);
        
        // Find ALL pending requests WHERE the current user is the recipient (friendUserId)
        // This matches the security rule: query.orderByChild == 'friendUserId' && query.equalTo == auth.uid
        rootRef.child("friends")
            .orderByChild("friendUserId").equalTo(currentUserId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean updated = false;
                    long now = System.currentTimeMillis();
                    
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Object senderIdObj = child.child("userId").getValue();
                        String senderId = senderIdObj != null ? String.valueOf(senderIdObj) : "";
                        
                        Object statusObj = child.child("status").getValue();
                        String statusStr = statusObj != null ? String.valueOf(statusObj) : "";
                        
                        // Verify this is the specific request from requesterId
                        if (requesterId.equals(senderId) && "PENDING".equalsIgnoreCase(statusStr)) {
                            android.util.Log.d("FirebaseSocialRepository", "Updating request " + child.getKey() + " to ACCEPTED");
                            
                            Map<String, Object> update = new HashMap<>();
                            update.put("status", "ACCEPTED");
                            update.put("acceptedAt", now);
                            
                            child.getRef().updateChildren(update);
                            updated = true;
                        }
                    }
                    
                    if (updated) {
                        // Create reciprocal entry so both users see each other
                        // (Similar check for existing reciprocal record...)
                        rootRef.child("friends").orderByChild("userId").equalTo(currentUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot recSnapshot) {
                                    boolean reciprocalExists = false;
                                    for (DataSnapshot recChild : recSnapshot.getChildren()) {
                                        Object fIdObj = recChild.child("friendUserId").getValue();
                                        if (requesterId.equals(String.valueOf(fIdObj))) {
                                            reciprocalExists = true;
                                            // Ensure existing reciprocal is also ACCEPTED
                                            recChild.getRef().child("status").setValue("ACCEPTED");
                                            recChild.getRef().child("acceptedAt").setValue(now);
                                            break;
                                        }
                                    }
                                    
                                    if (!reciprocalExists) {
                                        android.util.Log.d("FirebaseSocialRepository", "Creating reciprocal friend record");
                                        String reciprocalId = UUID.randomUUID().toString();
                                        Map<String, Object> reciprocal = new HashMap<>();
                                        reciprocal.put("id", reciprocalId);
                                        reciprocal.put("userId", currentUserId);
                                        reciprocal.put("friendUserId", requesterId);
                                        reciprocal.put("status", "ACCEPTED");
                                        reciprocal.put("requestedAt", now);
                                        reciprocal.put("acceptedAt", now);
                                        
                                        rootRef.child("friends").child(reciprocalId).setValue(reciprocal);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError error) {}
                            });
                    } else {
                        android.util.Log.w("FirebaseSocialRepository", "No pending request found to accept from " + requesterId);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    android.util.Log.e("FirebaseSocialRepository", "acceptFriend failed: " + error.getMessage());
                }
            });
        return null;
    }

    @Override
    public List<Friend> getFriendRequests(String userId) {
        // This method is synchronous but Firebase is async. 
        // Returning empty list; recommended to use direct listeners in the UI.
        return new ArrayList<>();
    }

    @Override
    public Challenge createChallenge(Challenge challenge) {
        // Simple validation - just create the challenge
        // The UI layer should validate user existence before calling this
        String id = UUID.randomUUID().toString();
        challenge.id = id;
        challenge.state = Challenge.State.PENDING;
        challenge.createdAt = System.currentTimeMillis();
        rootRef.child("challenges").child(id).setValue(challenge);
        return challenge;
    }

    @Override
    public List<Challenge> getChallengesForUser(String userId) {
        // Note: This method is synchronous but Firebase operations are async
        // For real-time updates, use Firebase listeners directly in UI layer
        // This returns empty list immediately; consider using callback pattern for async operations
        List<Challenge> challenges = new ArrayList<>();

        // To properly implement this, you should use the FirebaseDatabase listeners
        // in the UI rather than relying on this synchronous method.
        // Example in UI:
        // DatabaseReference ref = FirebaseDatabase.getInstance().getReference("challenges");
        // ref.orderByChild("challengedId").equalTo(userId).addListenerForSingleValueEvent(...)

        return challenges;
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
