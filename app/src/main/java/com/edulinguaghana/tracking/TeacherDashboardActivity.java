package com.edulinguaghana.tracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edulinguaghana.R;
import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRelationship;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Dashboard for teachers to view all their students' progress
 */
public class TeacherDashboardActivity extends AppCompatActivity {

    private RecyclerView studentsRecyclerView;
    private StudentProgressAdapter adapter;
    private ProgressBar loadingProgress;
    private TextView emptyTextView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyStateLayout;
    private MaterialButton btnSort;
    private MaterialButton btnAddFirstStudent;
    private MaterialButton btnAddStudentBottom;
    private MaterialButton btnRemoveStudent;

    // Statistics views
    private TextView tvTotalStudents;
    private TextView tvAvgLevel;
    private TextView tvActiveToday;

    private RoleManager roleManager;
    private ProgressTracker progressTracker;
    private String currentUserId;
    private List<StudentProgressItem> allStudents = new ArrayList<>();
    private int currentSortOption = 0; // 0=Name, 1=Level, 2=Activity

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
        setupSwipeRefresh();
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
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnSort = findViewById(R.id.btnSort);
        btnAddFirstStudent = findViewById(R.id.btnAddFirstStudent);
        btnAddStudentBottom = findViewById(R.id.btnAddStudentBottom);
        btnRemoveStudent = findViewById(R.id.btnRemoveStudent);

        // Statistics views
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvAvgLevel = findViewById(R.id.tvAvgLevel);
        tvActiveToday = findViewById(R.id.tvActiveToday);


        if (btnAddFirstStudent != null) {
            btnAddFirstStudent.setOnClickListener(v -> openRelationshipManagement());
        }

        if (btnAddStudentBottom != null) {
            btnAddStudentBottom.setOnClickListener(v -> openRelationshipManagement());
        }

        if (btnRemoveStudent != null) {
            btnRemoveStudent.setOnClickListener(v -> showRemoveStudentDialog());
        }

        if (btnSort != null) {
            btnSort.setOnClickListener(v -> showSortDialog());
        }
    }

    private void openRelationshipManagement() {
        Intent intent = new Intent(this, RelationshipManagementActivity.class);
        startActivity(intent);
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                loadStudents();
            });
            swipeRefresh.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.correctAnswer
            );
        }
    }

    private void showSortDialog() {
        String[] options = {"Sort by Name", "Sort by Level", "Sort by Recent Activity"};

        // Create menu items for styled dialog
        java.util.List<com.edulinguaghana.StyledMenuHelper.MenuItem> menuItems = new java.util.ArrayList<>();
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            String icon = index == 0 ? "🔤" : (index == 1 ? "📊" : "🕐");
            menuItems.add(new com.edulinguaghana.StyledMenuHelper.MenuItem(
                icon,
                options[i],
                () -> {
                    currentSortOption = index;
                    sortStudents();
                }
            ));
        }

        com.edulinguaghana.StyledMenuHelper.showStyledMenu(
            this,
            "🔀",
            "Sort Students",
            menuItems
        );
    }

    private void sortStudents() {
        if (allStudents.isEmpty()) return;

        switch (currentSortOption) {
            case 0: // By Name
                Collections.sort(allStudents, (a, b) ->
                    a.getStudentName().compareToIgnoreCase(b.getStudentName()));
                break;
            case 1: // By Level
                Collections.sort(allStudents, (a, b) ->
                    Integer.compare(b.getProgress().getCurrentLevel(),
                                  a.getProgress().getCurrentLevel()));
                break;
            case 2: // By Recent Activity
                Collections.sort(allStudents, (a, b) ->
                    Long.compare(b.getProgress().getLastUpdated(),
                               a.getProgress().getLastUpdated()));
                break;
        }

        adapter.updateStudents(allStudents);
    }

    private void setupRecyclerView() {
        adapter = new StudentProgressAdapter(new ArrayList<>(), new StudentProgressAdapter.OnStudentClickListener() {
            @Override
            public void onStudentClick(String studentId) {
                // Open detailed view for this student
                Intent intent = new Intent(TeacherDashboardActivity.this, StudentDetailActivity.class);
                intent.putExtra("studentId", studentId);
                startActivity(intent);
            }

            @Override
            public void onRemoveStudent(StudentProgressItem student) {
                confirmRemoveStudent(student);
            }
        });

        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentsRecyclerView.setAdapter(adapter);
    }

    private void loadStudents() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        studentsRecyclerView.setVisibility(View.GONE);

        roleManager.getStudents(currentUserId, new RoleManager.RelationshipCallback() {
            @Override
            public void onRelationshipsRetrieved(List<UserRelationship> relationships) {
                loadingProgress.setVisibility(View.GONE);
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }

                if (relationships.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    studentsRecyclerView.setVisibility(View.GONE);
                    updateStatistics(new ArrayList<>());
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    studentsRecyclerView.setVisibility(View.VISIBLE);
                    loadStudentProgress(relationships);
                }
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                emptyStateLayout.setVisibility(View.VISIBLE);
                studentsRecyclerView.setVisibility(View.GONE);
                if (emptyTextView != null) {
                    emptyTextView.setText("Error loading students: " + error);
                }
                Toast.makeText(TeacherDashboardActivity.this,
                             "Failed to load students", Toast.LENGTH_SHORT).show();
                updateStatistics(new ArrayList<>());
            }
        });
    }

    private void loadStudentProgress(List<UserRelationship> relationships) {
        allStudents.clear();
        final int totalStudents = relationships.size();
        final int[] loadedCount = {0};

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
                    allStudents.add(item);
                    loadedCount[0]++;

                    if (loadedCount[0] == totalStudents) {
                        sortStudents();
                        updateStatistics(allStudents);
                    }
                }

                @Override
                public void onError(String error) {
                    // Add with empty progress
                    StudentProgressItem item = new StudentProgressItem(
                        studentId,
                        studentName,
                        new ProgressAggregate()
                    );
                    allStudents.add(item);
                    loadedCount[0]++;

                    if (loadedCount[0] == totalStudents) {
                        sortStudents();
                        updateStatistics(allStudents);
                    }
                }
            });
        }
    }

    private void updateStatistics(List<StudentProgressItem> students) {
        int totalStudents = students.size();
        int totalLevels = 0;
        int activeToday = 0;
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        for (StudentProgressItem student : students) {
            ProgressAggregate progress = student.getProgress();
            totalLevels += progress.getCurrentLevel();

            if (progress.getLastUpdated() > oneDayAgo) {
                activeToday++;
            }
        }

        if (tvTotalStudents != null) {
            tvTotalStudents.setText(String.valueOf(totalStudents));
        }

        if (tvAvgLevel != null) {
            int avgLevel = totalStudents > 0 ? totalLevels / totalStudents : 0;
            tvAvgLevel.setText(String.valueOf(avgLevel));
        }

        if (tvActiveToday != null) {
            tvActiveToday.setText(String.valueOf(activeToday));
        }
    }

    private void showRemoveStudentDialog() {
        if (allStudents.isEmpty()) {
            Toast.makeText(this, "No students to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] studentNames = new String[allStudents.size()];
        for (int i = 0; i < allStudents.size(); i++) {
            studentNames[i] = allStudents.get(i).getStudentName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Student")
            .setItems(studentNames, (dialog, which) -> {
                StudentProgressItem selectedStudent = allStudents.get(which);
                confirmRemoveStudent(selectedStudent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void confirmRemoveStudent(StudentProgressItem student) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Student?")
            .setMessage("Are you sure you want to remove " + student.getStudentName() + " from your class?")
            .setPositiveButton("Remove", (dialog, which) -> {
                removeStudentFromClass(student.getStudentId(), student.getStudentName());
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void removeStudentFromClass(String studentId, String studentName) {
        // Find the relationship ID for this student
        roleManager.getStudents(currentUserId, new RoleManager.RelationshipCallback() {
            @Override
            public void onRelationshipsRetrieved(List<UserRelationship> relationships) {
                for (UserRelationship rel : relationships) {
                    if (rel.getStudentId().equals(studentId)) {
                        // Found the relationship, now remove it
                        roleManager.removeRelationship(rel.getId(),
                            new RoleManager.RelationshipActionCallback() {
                                @Override
                                public void onSuccess(UserRelationship relationship) {
                                    Toast.makeText(TeacherDashboardActivity.this,
                                        studentName + " removed from your class", Toast.LENGTH_SHORT).show();
                                    loadStudents(); // Refresh the list
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(TeacherDashboardActivity.this,
                                        "Failed to remove student: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        return;
                    }
                }
                Toast.makeText(TeacherDashboardActivity.this,
                    "Could not find student relationship", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(TeacherDashboardActivity.this,
                    "Failed to remove student: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh student list when returning to this activity
        loadStudents();
    }
}

