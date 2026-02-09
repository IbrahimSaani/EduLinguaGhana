package com.edulinguaghana.roles;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages user roles and relationships between teachers/parents and students
 */
public class RoleManager {
    private static final String TAG = "RoleManager";
    private static final String PREFS_NAME = "RolePrefs";
    private static final String KEY_USER_ROLE = "user_role";

    private final DatabaseReference usersRef;
    private final DatabaseReference relationshipsRef;

    public interface RoleCallback {
        void onRoleRetrieved(UserRole role);
        void onError(String error);
    }

    public interface RelationshipCallback {
        void onRelationshipsRetrieved(List<UserRelationship> relationships);
        void onError(String error);
    }

    public interface RelationshipActionCallback {
        void onSuccess(UserRelationship relationship);
        void onError(String error);
    }

    public RoleManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.usersRef = database.getReference("users");
        this.relationshipsRef = database.getReference("relationships");
    }

    /**
     * Set user role in Firebase and local cache
     */
    public void setUserRole(Context context, String userId, UserRole role) {
        if (userId == null || role == null) return;

        // Save to Firebase
        usersRef.child(userId).child("role").setValue(role.name())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Role set successfully for user: " + userId + " -> " + role.name());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to set role", e);
            });

        // Save to local cache
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_ROLE, role.name()).apply();
    }

    /**
     * Get user role from cache or Firebase
     */
    public void getUserRole(Context context, String userId, RoleCallback callback) {
        if (userId == null) {
            callback.onError("User ID is null");
            return;
        }

        // Try local cache first
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cachedRole = prefs.getString(KEY_USER_ROLE, null);

        // Also fetch from Firebase to ensure consistency
        usersRef.child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String roleStr = snapshot.getValue(String.class);
                UserRole role = UserRole.fromString(roleStr);

                // Update cache
                prefs.edit().putString(KEY_USER_ROLE, role.name()).apply();

                callback.onRoleRetrieved(role);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Fall back to cached role if available
                if (cachedRole != null) {
                    callback.onRoleRetrieved(UserRole.fromString(cachedRole));
                } else {
                    callback.onError(error.getMessage());
                }
            }
        });
    }

    /**
     * Create a relationship request (teacher to student or parent to child)
     */
    public void createRelationship(String supervisorId, String supervisorName,
                                   String studentId, String studentName,
                                   UserRelationship.RelationType type,
                                   RelationshipActionCallback callback) {
        String relationshipId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        UserRelationship relationship = new UserRelationship(
            relationshipId,
            supervisorId,
            studentId,
            supervisorName,
            studentName,
            type,
            UserRelationship.RelationshipStatus.PENDING,
            now,
            0
        );

        relationshipsRef.child(relationshipId).setValue(relationship)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Relationship created: " + relationshipId);
                callback.onSuccess(relationship);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to create relationship", e);
                callback.onError(e.getMessage());
            });
    }

    /**
     * Accept a relationship request
     */
    public void acceptRelationship(String relationshipId, RelationshipActionCallback callback) {
        relationshipsRef.child(relationshipId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                UserRelationship relationship = snapshot.getValue(UserRelationship.class);
                if (relationship != null) {
                    relationship.setStatus(UserRelationship.RelationshipStatus.ACCEPTED);
                    relationship.setAcceptedAt(System.currentTimeMillis());

                    relationshipsRef.child(relationshipId).setValue(relationship)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(relationship))
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                } else {
                    callback.onError("Relationship not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Get all students for a teacher or parent
     */
    public void getStudents(String supervisorId, RelationshipCallback callback) {
        relationshipsRef.orderByChild("supervisorId").equalTo(supervisorId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<UserRelationship> relationships = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        UserRelationship rel = child.getValue(UserRelationship.class);
                        if (rel != null && rel.getStatus() == UserRelationship.RelationshipStatus.ACCEPTED) {
                            relationships.add(rel);
                        }
                    }
                    callback.onRelationshipsRetrieved(relationships);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }

    /**
     * Get all supervisors (teachers/parents) for a student
     */
    public void getSupervisors(String studentId, RelationshipCallback callback) {
        relationshipsRef.orderByChild("studentId").equalTo(studentId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<UserRelationship> relationships = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        UserRelationship rel = child.getValue(UserRelationship.class);
                        if (rel != null && rel.getStatus() == UserRelationship.RelationshipStatus.ACCEPTED) {
                            relationships.add(rel);
                        }
                    }
                    callback.onRelationshipsRetrieved(relationships);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }

    /**
     * Get pending relationship requests for a student
     */
    public void getPendingRequests(String studentId, RelationshipCallback callback) {
        relationshipsRef.orderByChild("studentId").equalTo(studentId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<UserRelationship> relationships = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        UserRelationship rel = child.getValue(UserRelationship.class);
                        if (rel != null && rel.getStatus() == UserRelationship.RelationshipStatus.PENDING) {
                            relationships.add(rel);
                        }
                    }
                    callback.onRelationshipsRetrieved(relationships);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }

    /**
     * Remove a relationship
     */
    public void removeRelationship(String relationshipId, RelationshipActionCallback callback) {
        relationshipsRef.child(relationshipId).removeValue()
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Clear local cache
     */
    public void clearCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}

