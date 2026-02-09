package com.edulinguaghana.roles;

/**
 * Represents a relationship between users (teacher-student or parent-child)
 */
public class UserRelationship {
    private String id;
    private String supervisorId; // Teacher or Parent UID
    private String studentId; // Student UID
    private String supervisorName;
    private String studentName;
    private RelationType type;
    private RelationshipStatus status;
    private long requestedAt;
    private long acceptedAt;

    public enum RelationType {
        TEACHER_STUDENT,
        PARENT_CHILD
    }

    public enum RelationshipStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    public UserRelationship() {
        // Required for Firebase
    }

    public UserRelationship(String id, String supervisorId, String studentId,
                           String supervisorName, String studentName,
                           RelationType type, RelationshipStatus status,
                           long requestedAt, long acceptedAt) {
        this.id = id;
        this.supervisorId = supervisorId;
        this.studentId = studentId;
        this.supervisorName = supervisorName;
        this.studentName = studentName;
        this.type = type;
        this.status = status;
        this.requestedAt = requestedAt;
        this.acceptedAt = acceptedAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public RelationType getType() { return type; }
    public void setType(RelationType type) { this.type = type; }

    public RelationshipStatus getStatus() { return status; }
    public void setStatus(RelationshipStatus status) { this.status = status; }

    public long getRequestedAt() { return requestedAt; }
    public void setRequestedAt(long requestedAt) { this.requestedAt = requestedAt; }

    public long getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(long acceptedAt) { this.acceptedAt = acceptedAt; }
}

