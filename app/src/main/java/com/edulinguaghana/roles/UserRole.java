package com.edulinguaghana.roles;

/**
 * User roles in the EduLinguaGhana system
 */
public enum UserRole {
    STUDENT("Student"),
    TEACHER("Teacher"),
    PARENT("Parent");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromString(String role) {
        if (role == null) return STUDENT; // Default to student
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STUDENT;
        }
    }
}

