package com.edulinguaghana.roles;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

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

    public interface StringValueCallback {
        void onValueRetrieved(String value);
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
     * Check if a relationship already exists between supervisor and student
     */
    public void checkRelationshipExists(String supervisorId, String studentId, RelationshipExistsCallback callback) {
        relationshipsRef.orderByChild("supervisorId").equalTo(supervisorId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean exists = false;
                    UserRelationship existingRelationship = null;

                    for (DataSnapshot child : snapshot.getChildren()) {
                        UserRelationship rel = child.getValue(UserRelationship.class);
                        if (rel != null && rel.getStudentId().equals(studentId)) {
                            exists = true;
                            existingRelationship = rel;
                            break;
                        }
                    }
                    callback.onResult(exists, existingRelationship);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }

    public interface RelationshipExistsCallback {
        void onResult(boolean exists, UserRelationship relationship);
        void onError(String error);
    }

    /**
     * Create a relationship request (teacher to student or parent to child)
     */
    public void createRelationship(String supervisorId, String supervisorName,
                                   String studentId, String studentName,
                                   UserRelationship.RelationType type,
                                   RelationshipActionCallback callback) {
        // Fetch supervisor gender first
        usersRef.child(supervisorId).child("gender")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String gender = snapshot.getValue(String.class);
                    if (gender == null) gender = "Not Specified";
                    
                    final String supervisorGender = gender;

                    // First check if relationship already exists
                    checkRelationshipExists(supervisorId, studentId, new RelationshipExistsCallback() {
                        @Override
                        public void onResult(boolean exists, UserRelationship existingRelationship) {
                            String predictableId = supervisorId + "_" + studentId;
                            
                            if (exists) {
                                // If it already exists with the correct predictable ID, we stop and show error
                                if (existingRelationship.getId() != null && existingRelationship.getId().equals(predictableId)) {
                                    String status = existingRelationship.getStatus() == UserRelationship.RelationshipStatus.PENDING
                                        ? "A request is already waiting to be accepted!"
                                        : "You're already connected with this explorer!";
                                    callback.onError(status);
                                    return;
                                }
                                
                                // If it exists but with a different ID (e.g. push key), we'll allow creating the correct one.
                                // We should delete the old one to avoid duplicates and ensure security rules work.
                                Log.i(TAG, "Relationship exists with non-predictable ID: " + existingRelationship.getId() + ". Migrating to: " + predictableId);
                                relationshipsRef.child(existingRelationship.getId()).removeValue();
                                // We continue to create the new one below
                            }

                            // Create new relationship with a predictable ID (supervisorId_studentId)
                            // This allows for high-performance security rules in Firebase
                            long now = System.currentTimeMillis();

                            UserRelationship relationship = new UserRelationship(
                                predictableId,
                                supervisorId,
                                studentId,
                                supervisorName,
                                studentName,
                                supervisorGender,
                                type,
                                UserRelationship.RelationshipStatus.PENDING,
                                now,
                                0
                            );

                            relationshipsRef.child(predictableId).setValue(relationship)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Relationship created: " + predictableId);
                                    callback.onSuccess(relationship);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create relationship", e);
                                    callback.onError(e.getMessage());
                                });
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to check existing relationships: " + error);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError("Failed to fetch user gender: " + error.getMessage());
                }
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
     * Fetch the class assigned to a user profile.
     */
    public void getUserStudentClass(String userId, StringValueCallback callback) {
        if (userId == null) {
            callback.onError("User ID is null");
            return;
        }

        usersRef.child(userId).child("studentClass").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String studentClass = snapshot.getValue(String.class);
                callback.onValueRetrieved(studentClass != null ? studentClass.trim() : "");
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
     * Get pending relationship requests for a student (received requests)
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
     * Get pending relationship requests sent by a supervisor
     */
    public void getSentPendingRequests(String supervisorId, RelationshipCallback callback) {
        relationshipsRef.orderByChild("supervisorId").equalTo(supervisorId)
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
        prefs.edit().clear().commit();
    }
}

