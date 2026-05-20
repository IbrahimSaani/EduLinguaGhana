package com.edulinguaghana.social;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashSet;
import java.util.Set;

/**
 * Listens for new social activities (friend requests, challenges) in real-time
 * and triggers local notifications.
 */
public class SocialActivityListener {
    private static final String TAG = "SocialActivityListener";
    private static SocialActivityListener instance;

    private final DatabaseReference dbRef;
    private final SocialNotificationHelper notificationHelper;
    
    private Query friendRequestQuery;
    private Query challengeQuery;
    private Query relationshipQuery;
    
    private ChildEventListener friendListener;
    private ChildEventListener challengeListener;
    private ChildEventListener relationshipListener;

    private String currentUserId;
    private final Set<String> processedIds = new HashSet<>();
    private final SharedPreferences prefs;
    private static final String PREF_NAME = "SocialActivityListenerPrefs";
    private static final String KEY_PROCESSED_IDS = "PROCESSED_IDS";

    private SocialActivityListener(Context context) {
        this.dbRef = FirebaseDatabase.getInstance().getReference();
        this.notificationHelper = new SocialNotificationHelper(context);
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        loadProcessedIds();
        setupAuthListener();
    }

    private void loadProcessedIds() {
        Set<String> saved = prefs.getStringSet(KEY_PROCESSED_IDS, null);
        if (saved != null) {
            processedIds.addAll(saved);
        }
    }

    private void saveProcessedIds() {
        // Keep set size manageable
        if (processedIds.size() > 500) {
            // Very simple cleanup - not perfect but prevents infinite growth
            processedIds.clear();
        }
        prefs.edit().putStringSet(KEY_PROCESSED_IDS, processedIds).apply();
    }

    public static synchronized SocialActivityListener getInstance(Context context) {
        if (instance == null) {
            instance = new SocialActivityListener(context);
        }
        return instance;
    }

    private void setupAuthListener() {
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                if (!java.util.Objects.equals(user.getUid(), currentUserId)) {
                    stopListening();
                    currentUserId = user.getUid();
                    startListening();
                }
            } else {
                stopListening();
                currentUserId = null;
            }
        });
    }

    public void startListening() {
        if (currentUserId == null) return;
        Log.d(TAG, "Starting social activity listeners for user: " + currentUserId);

        // 1. Listen for Friend Requests
        friendRequestQuery = dbRef.child("friends")
                .orderByChild("friendUserId")
                .equalTo(currentUserId);
        
        friendListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String status = snapshot.child("status").getValue(String.class);
                if ("PENDING".equalsIgnoreCase(status)) {
                    String requestId = snapshot.getKey();
                    if (requestId != null && !processedIds.contains(requestId)) {
                        processedIds.add(requestId);
                        saveProcessedIds();
                        
                        String fromUserId = snapshot.child("userId").getValue(String.class);
                        fetchUserNameAndNotifyFriend(fromUserId);
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        friendRequestQuery.addChildEventListener(friendListener);

        // 2. Listen for Challenges
        challengeQuery = dbRef.child("challenges")
                .orderByChild("challengedId")
                .equalTo(currentUserId);
        
        challengeListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String state = snapshot.child("state").getValue(String.class);
                if ("PENDING".equalsIgnoreCase(state)) {
                    String challengeId = snapshot.getKey();
                    if (challengeId != null && !processedIds.contains(challengeId)) {
                        processedIds.add(challengeId);
                        saveProcessedIds();
                        
                        String fromUserId = snapshot.child("challengerId").getValue(String.class);
                        String quizType = snapshot.child("quizType").getValue(String.class);
                        fetchUserNameAndNotifyChallenge(fromUserId, quizType);
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        challengeQuery.addChildEventListener(challengeListener);

        // 3. Listen for Relationship Requests (Teacher/Parent)
        relationshipQuery = dbRef.child("relationships")
                .orderByChild("studentId")
                .equalTo(currentUserId);
        
        relationshipListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String status = snapshot.child("status").getValue(String.class);
                if ("PENDING".equalsIgnoreCase(status)) {
                    String relId = snapshot.getKey();
                    if (relId != null && !processedIds.contains(relId)) {
                        processedIds.add(relId);
                        saveProcessedIds();
                        
                        String fromUserId = snapshot.child("supervisorId").getValue(String.class);
                        String supervisorName = snapshot.child("supervisorName").getValue(String.class);
                        String gender = snapshot.child("supervisorGender").getValue(String.class);
                        String type = snapshot.child("type").getValue(String.class);
                        
                        notificationHelper.showRelationshipRequestNotification(fromUserId, supervisorName, gender, type);
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        relationshipQuery.addChildEventListener(relationshipListener);
    }

    private void stopListening() {
        if (friendRequestQuery != null && friendListener != null) {
            friendRequestQuery.removeEventListener(friendListener);
        }
        if (challengeQuery != null && challengeListener != null) {
            challengeQuery.removeEventListener(challengeListener);
        }
        if (relationshipQuery != null && relationshipListener != null) {
            relationshipQuery.removeEventListener(relationshipListener);
        }
        processedIds.clear();
    }

    private void fetchUserNameAndNotifyFriend(String userId) {
        dbRef.child("users").child(userId).child("displayName").get().addOnCompleteListener(task -> {
            String name = task.isSuccessful() ? task.getResult().getValue(String.class) : userId;
            notificationHelper.showFriendRequestNotification(userId, name);
        });
    }

    private void fetchUserNameAndNotifyChallenge(String userId, String quizType) {
        dbRef.child("users").child(userId).child("displayName").get().addOnCompleteListener(task -> {
            String name = task.isSuccessful() ? task.getResult().getValue(String.class) : userId;
            notificationHelper.showChallengeNotification(userId, name, quizType);
        });
    }
}
