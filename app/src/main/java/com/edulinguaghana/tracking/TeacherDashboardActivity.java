package com.edulinguaghana.tracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edulinguaghana.R;
import com.edulinguaghana.StyledMenuHelper;
import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRelationship;
import com.edulinguaghana.roles.UserRole;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private MaterialAutoCompleteTextView classFilterDropdown;
    private MaterialButton btnAddFirstStudent;
    private MaterialButton btnAddStudentBottom;

    // Statistics views
    private TextView tvTotalStudents;
    private TextView tvAvgLevel;
    private TextView tvActiveToday;
    private TextView tvClassAccuracy;
    private TextView tvTotalWeeklyQuizzes;

    private RoleManager roleManager;
    private ProgressTracker progressTracker;
    private String currentUserId;
    private final List<StudentProgressItem> allStudents = new ArrayList<>();
    private int currentSortOption = 0; // 0=Name, 1=Level, 2=Activity
    private int activeLoadToken = 0;

    private String ALL_CLASSES_FILTER;
    private String UNASSIGNED_CLASSES_FILTER;
    private String[] classFilterOptions;
    private String selectedClassFilter;

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

        ALL_CLASSES_FILTER = getString(R.string.teacher_dashboard_all_classes);
        UNASSIGNED_CLASSES_FILTER = getString(R.string.teacher_dashboard_unassigned);
        classFilterOptions = new String[]{
                ALL_CLASSES_FILTER,
                "Class 1",
                "Class 2",
                "Class 3",
                "Class 4",
                "Class 5",
                "Class 6",
                UNASSIGNED_CLASSES_FILTER
        };
        selectedClassFilter = ALL_CLASSES_FILTER;

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
            getSupportActionBar().setTitle(R.string.teacher_dashboard_title);
        }

        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        emptyTextView = findViewById(R.id.emptyTextView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        MaterialButton btnSort = findViewById(R.id.btnSort);
        classFilterDropdown = findViewById(R.id.classFilterDropdown);
        btnAddFirstStudent = findViewById(R.id.btnAddFirstStudent);
        btnAddStudentBottom = findViewById(R.id.btnAddStudentBottom);
        MaterialButton btnRemoveStudent = findViewById(R.id.btnRemoveStudent);

        // Statistics views
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvAvgLevel = findViewById(R.id.tvAvgLevel);
        tvActiveToday = findViewById(R.id.tvActiveToday);
        tvClassAccuracy = findViewById(R.id.tvClassAccuracy);
        tvTotalWeeklyQuizzes = findViewById(R.id.tvTotalWeeklyQuizzes);


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

        setupClassFilter();
    }

    private void setupClassFilter() {
        if (classFilterDropdown == null) return;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                classFilterOptions
        );
        classFilterDropdown.setAdapter(adapter);
        classFilterDropdown.setText(selectedClassFilter, false);
        classFilterDropdown.setOnClickListener(v -> classFilterDropdown.showDropDown());
        classFilterDropdown.setOnItemClickListener((parent, view, position, id) -> {
            selectedClassFilter = classFilterOptions[position];
            renderStudents();
        });
    }

    private void openRelationshipManagement() {
        Intent intent = new Intent(this, RelationshipManagementActivity.class);
        startActivity(intent);
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::loadStudents);
            swipeRefresh.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.correctAnswer
            );
        }
    }

    private void showSortDialog() {
        String[] options = {
                getString(R.string.sort_by_name),
                getString(R.string.sort_by_level),
                getString(R.string.sort_by_activity)
        };

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
            getString(R.string.teacher_dashboard_sort_title),
            menuItems
        );
    }

    private void sortStudents() {
        renderStudents();
    }

    private void setupRecyclerView() {
        adapter = new StudentProgressAdapter(new ArrayList<>(), UserRole.TEACHER, new StudentProgressAdapter.OnStudentClickListener() {
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
        final int loadToken = ++activeLoadToken;

        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        studentsRecyclerView.setVisibility(View.GONE);
        allStudents.clear();
        adapter.updateStudents(new ArrayList<>());

        roleManager.getStudents(currentUserId, new RoleManager.RelationshipCallback() {
            @Override
            public void onRelationshipsRetrieved(List<UserRelationship> relationships) {
                if (loadToken != activeLoadToken) return;

                loadingProgress.setVisibility(View.GONE);
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }

                if (relationships.isEmpty()) {
                    allStudents.clear();
                    renderStudents();
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    studentsRecyclerView.setVisibility(View.VISIBLE);
                    loadStudentProgress(relationships, loadToken);
                }
            }

            @Override
            public void onError(String error) {
                if (loadToken != activeLoadToken) return;

                loadingProgress.setVisibility(View.GONE);
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                emptyStateLayout.setVisibility(View.VISIBLE);
                studentsRecyclerView.setVisibility(View.GONE);
                if (emptyTextView != null) {
                    emptyTextView.setText(getString(R.string.teacher_dashboard_error_loading_students, error));
                }
                Toast.makeText(TeacherDashboardActivity.this,
                             R.string.teacher_dashboard_load_failed, Toast.LENGTH_SHORT).show();
                updateStatistics(new ArrayList<>());
            }
        });
    }

    private void loadStudentProgress(List<UserRelationship> relationships, int loadToken) {
        final int totalStudents = relationships.size();
        final int[] loadedCount = {0};
        final LinkedHashMap<String, StudentProgressItem> loadedStudents = new LinkedHashMap<>();

        for (UserRelationship relationship : relationships) {
            String studentId = relationship.getStudentId();
            String studentName = relationship.getStudentName();

            roleManager.getUserStudentClass(studentId, new RoleManager.StringValueCallback() {
                @Override
                public void onValueRetrieved(String studentClass) {
                    loadStudentProgressAggregate(studentId, studentName, studentClass, loadedStudents, loadedCount, totalStudents, loadToken);
                }

                @Override
                public void onError(String error) {
                    loadStudentProgressAggregate(studentId, studentName, "", loadedStudents, loadedCount, totalStudents, loadToken);
                }
            });
        }
    }

    private void loadStudentProgressAggregate(String studentId, String studentName, String studentClass,
                                              LinkedHashMap<String, StudentProgressItem> loadedStudents,
                                              int[] loadedCount, int totalStudents, int loadToken) {
        // Load progress for each student after their class is known
        progressTracker.getProgressAggregate(studentId, new ProgressTracker.ProgressAggregateCallback() {
            @Override
            public void onAggregateRetrieved(ProgressAggregate aggregate) {
                if (loadToken != activeLoadToken) return;

                StudentProgressItem item = new StudentProgressItem(
                        studentId,
                        studentName,
                        aggregate,
                        studentClass
                );
                loadedStudents.put(studentId, item);
                loadedCount[0]++;

                if (loadedCount[0] == totalStudents) {
                    if (loadToken != activeLoadToken) return;
                    allStudents.clear();
                    allStudents.addAll(loadedStudents.values());
                    renderStudents();
                }
            }

            @Override
            public void onError(String error) {
                if (loadToken != activeLoadToken) return;

                StudentProgressItem item = new StudentProgressItem(
                        studentId,
                        studentName,
                        new ProgressAggregate(),
                        studentClass
                );
                loadedStudents.put(studentId, item);
                loadedCount[0]++;

                if (loadedCount[0] == totalStudents) {
                    if (loadToken != activeLoadToken) return;
                    allStudents.clear();
                    allStudents.addAll(loadedStudents.values());
                    renderStudents();
                }
            }
        });
    }

    private void renderStudents() {
        List<StudentProgressItem> visibleStudents = new ArrayList<>();
        for (StudentProgressItem student : allStudents) {
            if (matchesSelectedClass(student.getStudentClass())) {
                visibleStudents.add(student);
            }
        }

        sortVisibleStudents(visibleStudents);
        adapter.updateStudents(visibleStudents);
        updateStatistics(visibleStudents);

        if (visibleStudents.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            studentsRecyclerView.setVisibility(View.GONE);
            if (emptyTextView != null) {
                emptyTextView.setText(getEmptyStateMessage());
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            studentsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private boolean matchesSelectedClass(String studentClass) {
        String normalizedClass = normalizeClassLabel(studentClass);

        if (Objects.equals(ALL_CLASSES_FILTER, selectedClassFilter)) {
            return true;
        }

        if (Objects.equals(UNASSIGNED_CLASSES_FILTER, selectedClassFilter)) {
            return normalizedClass.isEmpty();
        }

        return selectedClassFilter.equalsIgnoreCase(normalizedClass);
    }

    private void sortVisibleStudents(List<StudentProgressItem> students) {
        if (students.isEmpty()) return;

        Collections.sort(students, (a, b) -> {
            if (Objects.equals(ALL_CLASSES_FILTER, selectedClassFilter)) {
                int classCompare = Integer.compare(
                        getClassSortOrder(a.getStudentClass()),
                        getClassSortOrder(b.getStudentClass())
                );
                if (classCompare != 0) {
                    return classCompare;
                }
            }

            switch (currentSortOption) {
                case 0:
                    String aName = a.getStudentName() != null ? a.getStudentName() : "";
                    String bName = b.getStudentName() != null ? b.getStudentName() : "";
                    return aName.compareToIgnoreCase(bName);
                case 1:
                    return Integer.compare(
                            b.getProgress().getCurrentLevel(),
                            a.getProgress().getCurrentLevel());
                case 2:
                    return Long.compare(
                            b.getProgress().getLastUpdated(),
                            a.getProgress().getLastUpdated());
                default:
                    return 0;
            }
        });
    }

    private int getClassSortOrder(String studentClass) {
        String normalizedClass = normalizeClassLabel(studentClass);
        switch (normalizedClass) {
            case "Class 1": return 1;
            case "Class 2": return 2;
            case "Class 3": return 3;
            case "Class 4": return 4;
            case "Class 5": return 5;
            case "Class 6": return 6;
            case "": return 999;
            default: return 998;
        }
    }

    private String normalizeClassLabel(String studentClass) {
        return studentClass == null ? "" : studentClass.trim();
    }

    private String getEmptyStateMessage() {
        if (Objects.equals(ALL_CLASSES_FILTER, selectedClassFilter)) {
            return getString(R.string.teacher_dashboard_empty_message);
        }

        if (Objects.equals(UNASSIGNED_CLASSES_FILTER, selectedClassFilter)) {
            return getString(R.string.teacher_dashboard_empty_unassigned);
        }

        return getString(R.string.teacher_dashboard_empty_filter, selectedClassFilter);
    }

    private void updateStatistics(List<StudentProgressItem> students) {
        int totalStudents = students.size();
        int totalLevels = 0;
        int activeToday = 0;
        int totalWeeklyQuizzes = 0;
        double totalAccuracy = 0;
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        for (StudentProgressItem student : students) {
            ProgressAggregate progress = student.getProgress();
            totalLevels += progress.getCurrentLevel();
            totalWeeklyQuizzes += progress.getQuizzesThisWeek();
            totalAccuracy += progress.getAccuracy();

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

        if (tvClassAccuracy != null) {
            double avgAccuracy = totalStudents > 0 ? totalAccuracy / totalStudents : 0;
            tvClassAccuracy.setText(String.format(Locale.getDefault(), "%.1f%%", avgAccuracy));
        }

        if (tvTotalWeeklyQuizzes != null) {
            tvTotalWeeklyQuizzes.setText(String.valueOf(totalWeeklyQuizzes));
        }
    }


    private void showRemoveStudentDialog() {
        if (allStudents.isEmpty()) {
            Toast.makeText(this, R.string.teacher_dashboard_no_students_to_remove, Toast.LENGTH_SHORT).show();
            return;
        }

        List<StyledMenuHelper.MenuItem> menuItems = new ArrayList<>();
        for (StudentProgressItem student : allStudents) {
            menuItems.add(new StyledMenuHelper.MenuItem(
                "👤",
                student.getStudentName(),
                () -> confirmRemoveStudent(student)
            ));
        }

        StyledMenuHelper.showStyledMenu(
            this,
            "🗑️",
            getString(R.string.teacher_dashboard_remove_title),
            getString(R.string.teacher_dashboard_remove_prompt),
            menuItems,
            null
        );
    }

    private void confirmRemoveStudent(StudentProgressItem student) {
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "❓",
            getString(R.string.teacher_dashboard_remove_dialog_title),
            getString(R.string.teacher_dashboard_remove_dialog_message, student.getStudentName()),
            getString(R.string.teacher_dashboard_remove_confirm),
            getString(R.string.teacher_dashboard_remove_cancel),
            () -> removeStudentFromClass(student.getStudentId(), student.getStudentName()),
            null
        );
    }

    private void removeStudentFromClass(String studentId, String studentName) {
        // Find the relationship ID for this student
        roleManager.getStudents(currentUserId, new RoleManager.RelationshipCallback() {
            @Override
            public void onRelationshipsRetrieved(List<UserRelationship> relationships) {
                for (UserRelationship rel : relationships) {
                    if (Objects.equals(rel.getStudentId(), studentId)) {
                        // Found the relationship, now remove it
                        roleManager.removeRelationship(rel.getId(),
                            new RoleManager.RelationshipActionCallback() {
                                @Override
                                public void onSuccess(UserRelationship relationship) {
                                    Toast.makeText(TeacherDashboardActivity.this,
                                        getString(R.string.teacher_dashboard_remove_success, studentName), Toast.LENGTH_SHORT).show();
                                    loadStudents(); // Refresh the list
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(TeacherDashboardActivity.this,
                                        getString(R.string.teacher_dashboard_remove_failed, error), Toast.LENGTH_SHORT).show();
                                }
                            });
                        return;
                    }
                }
                Toast.makeText(TeacherDashboardActivity.this,
                    R.string.teacher_dashboard_remove_not_found, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(TeacherDashboardActivity.this,
                    getString(R.string.teacher_dashboard_remove_failed, error), Toast.LENGTH_SHORT).show();
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

