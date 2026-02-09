package com.edulinguaghana.tracking;

/**
 * Model for displaying student progress in a list
 */
public class StudentProgressItem {
    private String studentId;
    private String studentName;
    private ProgressAggregate progress;

    public StudentProgressItem(String studentId, String studentName, ProgressAggregate progress) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.progress = progress;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public ProgressAggregate getProgress() {
        return progress;
    }

    public void setProgress(ProgressAggregate progress) {
        this.progress = progress;
    }
}

