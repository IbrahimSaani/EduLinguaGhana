package com.edulinguaghana.tracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edulinguaghana.R;
import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRelationship;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard for teachers to view all their students' progress
 */
public class TeacherDashboardActivity extends AppCompatActivity {

    private RecyclerView studentsRecyclerView;
    private StudentProgressAdapter adapter;
    private ProgressBar loadingProgress;
    private TextView emptyTextView;
    private FloatingActionButton fabAddStudent;

    private RoleManager roleManager;
    private ProgressTracker progressTracker;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        currentUserId = user.getUid();

        roleManager = new RoleManager();
        progressTracker = new ProgressTracker();

        initViews();
        setupRecyclerView();
        loadStudents();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Students");
        }

        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        emptyTextView = findViewById(R.id.emptyTextView);
        fabAddStudent = findViewById(R.id.fabAddStudent);

        fabAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(this, RelationshipManagementActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        adapter = new StudentProgressAdapter(new ArrayList<>(), studentId -> {
            // Open detailed view for this student
            Intent intent = new Intent(TeacherDashboardActivity.this, StudentDetailActivity.class);
            intent.putExtra("studentId", studentId);
            startActivity(intent);
        });

        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentsRecyclerView.setAdapter(adapter);
    }

    private void loadStudents() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);

        roleManager.getStudents(currentUserId, new RoleManager.RelationshipCallback() {
            @Override
            public void onRelationshipsRetrieved(List<UserRelationship> relationships) {
                loadingProgress.setVisibility(View.GONE);

                if (relationships.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    emptyTextView.setText("No students yet.\n\nTap the + button to add students.");
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    loadStudentProgress(relationships);
                }
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Error loading students: " + error);
                Toast.makeText(TeacherDashboardActivity.this,
                             "Failed to load students", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStudentProgress(List<UserRelationship> relationships) {
        List<StudentProgressItem> progressItems = new ArrayList<>();

        for (UserRelationship relationship : relationships) {
            String studentId = relationship.getStudentId();
            String studentName = relationship.getStudentName();

            // Load progress for each student
            progressTracker.getProgressAggregate(studentId, new ProgressTracker.ProgressAggregateCallback() {
                @Override
                public void onAggregateRetrieved(ProgressAggregate aggregate) {
                    StudentProgressItem item = new StudentProgressItem(
                        studentId,
                        studentName,
                        aggregate
                    );
                    progressItems.add(item);
                    adapter.updateStudents(progressItems);
                }

                @Override
                public void onError(String error) {
                    // Add with empty progress
                    StudentProgressItem item = new StudentProgressItem(
                        studentId,
                        studentName,
                        new ProgressAggregate()
                    );
                    progressItems.add(item);
                    adapter.updateStudents(progressItems);
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh student list when returning to this activity
        loadStudents();
    }
}

