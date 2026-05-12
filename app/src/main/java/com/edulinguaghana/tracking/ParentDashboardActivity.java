package com.edulinguaghana.tracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.edulinguaghana.StyledMenuHelper;
import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRelationship;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Dashboard for parents to view their children's progress
 */
public class ParentDashboardActivity extends AppCompatActivity {

    private RecyclerView childrenRecyclerView;
    private StudentProgressAdapter adapter;
    private ProgressBar loadingProgress;
    private TextView emptyTextView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyStateLayout;
    private MaterialButton btnSort;
    private MaterialAutoCompleteTextView classFilterDropdown;
    private MaterialButton btnAddFirstChild;
    private MaterialButton btnAddChildBottom;
    private MaterialButton btnRemoveChild;

    // Statistics views
    private TextView tvTotalChildren;
    private TextView tvAvgLevel;
    private TextView tvActiveToday;

    private RoleManager roleManager;
    private ProgressTracker progressTracker;
    private String currentUserId;
    private List<StudentProgressItem> allChildren = new ArrayList<>();
    private int currentSortOption = 0; // 0=Name, 1=Level, 2=Activity
    private int activeLoadToken = 0;

    private static final String ALL_CLASSES_FILTER = "All Classes";
    private static final String UNASSIGNED_CLASSES_FILTER = "Unassigned";
    private final String[] classFilterOptions = {
            ALL_CLASSES_FILTER,
            "Class 1",
            "Class 2",
            "Class 3",
            "Class 4",
            "Class 5",
            "Class 6",
            UNASSIGNED_CLASSES_FILTER
    };
    private String selectedClassFilter = ALL_CLASSES_FILTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

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
        loadChildren();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Children");
        }

        childrenRecyclerView = findViewById(R.id.childrenRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        emptyTextView = findViewById(R.id.emptyTextView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnSort = findViewById(R.id.btnSort);
        classFilterDropdown = findViewById(R.id.classFilterDropdown);
        btnAddFirstChild = findViewById(R.id.btnAddFirstChild);
        btnAddChildBottom = findViewById(R.id.btnAddChildBottom);
        btnRemoveChild = findViewById(R.id.btnRemoveChild);

        // Statistics views
        tvTotalChildren = findViewById(R.id.tvTotalChildren);
        tvAvgLevel = findViewById(R.id.tvAvgLevel);
        tvActiveToday = findViewById(R.id.tvActiveToday);


        if (btnAddFirstChild != null) {
            btnAddFirstChild.setOnClickListener(v -> openRelationshipManagement());
        }

        if (btnAddChildBottom != null) {
            btnAddChildBottom.setOnClickListener(v -> openRelationshipManagement());
        }

        if (btnRemoveChild != null) {
            btnRemoveChild.setOnClickListener(v -> showRemoveChildDialog());
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
            renderChildren();
        });
    }

    private void openRelationshipManagement() {
        Intent intent = new Intent(this, RelationshipManagementActivity.class);
        startActivity(intent);
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                loadChildren();
            });
            swipeRefresh.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.correctAnswer
            );
        }
    }

    private void showSortDialog() {
        String[] options = {"Sort by Name", "Most Weekly Quizzes", "Needs Attention"};

        // Create menu items for styled dialog
        java.util.List<com.edulinguaghana.StyledMenuHelper.MenuItem> menuItems = new java.util.ArrayList<>();
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            String icon = index == 0 ? "🔤" : (index == 1 ? "📝" : "🚨");
            menuItems.add(new com.edulinguaghana.StyledMenuHelper.MenuItem(
                icon,
                options[i],
                () -> {
                    currentSortOption = index;
                    sortChildren();
                }
            ));
        }

        com.edulinguaghana.StyledMenuHelper.showStyledMenu(
            this,
            "🔀",
            "Sort Family View",
            menuItems
        );
    }

    private void sortChildren() {
        renderChildren();
    }

    private void setupRecyclerView() {
        adapter = new StudentProgressAdapter(new ArrayList<>(), new StudentProgressAdapter.OnStudentClickListener() {
            @Override
            public void onStudentClick(String studentId) {
                // Open detailed view for this child
                Intent intent = new Intent(ParentDashboardActivity.this, StudentDetailActivity.class);
                intent.putExtra("studentId", studentId);
                startActivity(intent);
            }

            @Override
            public void onRemoveStudent(StudentProgressItem student) {
                confirmRemoveChild(student);
            }
        });

        childrenRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        childrenRecyclerView.setAdapter(adapter);
    }

    private void loadChildren() {
        final int loadToken = ++activeLoadToken;

        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        childrenRecyclerView.setVisibility(View.GONE);
        allChildren.clear();
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
                    allChildren.clear();
                    renderChildren();
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    childrenRecyclerView.setVisibility(View.VISIBLE);
                    loadChildrenProgress(relationships, loadToken);
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
                childrenRecyclerView.setVisibility(View.GONE);
                if (emptyTextView != null) {
                    emptyTextView.setText(getString(R.string.parent_dashboard_error_loading_children, error));
                }
                Toast.makeText(ParentDashboardActivity.this,
                             "Failed to load children", Toast.LENGTH_SHORT).show();
                updateStatistics(new ArrayList<>());
            }
        });
    }

    private void loadChildrenProgress(List<UserRelationship> relationships, int loadToken) {
        final int totalChildren = relationships.size();
        final int[] loadedCount = {0};
        final LinkedHashMap<String, StudentProgressItem> loadedChildren = new LinkedHashMap<>();

        for (UserRelationship relationship : relationships) {
            String studentId = relationship.getStudentId();
            String studentName = relationship.getStudentName();

            roleManager.getUserStudentClass(studentId, new RoleManager.StringValueCallback() {
                @Override
                public void onValueRetrieved(String studentClass) {
                    loadChildProgressAggregate(studentId, studentName, studentClass, loadedChildren, loadedCount, totalChildren, loadToken);
                }

                @Override
                public void onError(String error) {
                    loadChildProgressAggregate(studentId, studentName, "", loadedChildren, loadedCount, totalChildren, loadToken);
                }
            });
        }
    }

    private void loadChildProgressAggregate(String studentId, String studentName, String studentClass,
                                            LinkedHashMap<String, StudentProgressItem> loadedChildren,
                                            int[] loadedCount, int totalChildren, int loadToken) {
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
                loadedChildren.put(studentId, item);
                loadedCount[0]++;

                if (loadedCount[0] == totalChildren) {
                    if (loadToken != activeLoadToken) return;
                    allChildren.clear();
                    allChildren.addAll(loadedChildren.values());
                    renderChildren();
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
                loadedChildren.put(studentId, item);
                loadedCount[0]++;

                if (loadedCount[0] == totalChildren) {
                    if (loadToken != activeLoadToken) return;
                    allChildren.clear();
                    allChildren.addAll(loadedChildren.values());
                    renderChildren();
                }
            }
        });
    }

    private void renderChildren() {
        List<StudentProgressItem> visibleChildren = new ArrayList<>();
        for (StudentProgressItem child : allChildren) {
            if (matchesSelectedClass(child.getStudentClass())) {
                visibleChildren.add(child);
            }
        }

        sortVisibleChildren(visibleChildren);
        adapter.updateStudents(visibleChildren);
        updateStatistics(visibleChildren);

        if (visibleChildren.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            childrenRecyclerView.setVisibility(View.GONE);
            if (emptyTextView != null) {
                emptyTextView.setText(getEmptyStateMessage());
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            childrenRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private boolean matchesSelectedClass(String studentClass) {
        String normalizedClass = normalizeClassLabel(studentClass);

        if (ALL_CLASSES_FILTER.equals(selectedClassFilter)) {
            return true;
        }

        if (UNASSIGNED_CLASSES_FILTER.equals(selectedClassFilter)) {
            return normalizedClass.isEmpty();
        }

        return selectedClassFilter.equalsIgnoreCase(normalizedClass);
    }

    private void sortVisibleChildren(List<StudentProgressItem> children) {
        if (children.isEmpty()) return;

        Collections.sort(children, (a, b) -> {
            if (ALL_CLASSES_FILTER.equals(selectedClassFilter)) {
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
                            b.getProgress().getQuizzesThisWeek(),
                            a.getProgress().getQuizzesThisWeek());
                case 2:
                    return Long.compare(
                            a.getProgress().getLastUpdated(),
                            b.getProgress().getLastUpdated());
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
        if (ALL_CLASSES_FILTER.equals(selectedClassFilter)) {
            return "Connect with your children to track their learning progress.\n\nTap the + button below to get started.";
        }

        if (UNASSIGNED_CLASSES_FILTER.equals(selectedClassFilter)) {
            return "No children without a class assignment were found.";
        }

        return "No children found in " + selectedClassFilter + ".";
    }

    private void updateStatistics(List<StudentProgressItem> children) {
        int totalChildren = children.size();
        int totalQuizzesThisWeek = 0;
        int activeToday = 0;
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        for (StudentProgressItem child : children) {
            ProgressAggregate progress = child.getProgress();
            totalQuizzesThisWeek += progress.getQuizzesThisWeek();

            if (progress.getLastUpdated() > oneDayAgo) {
                activeToday++;
            }
        }

        if (tvTotalChildren != null) {
            tvTotalChildren.setText(String.valueOf(totalChildren));
        }

        if (tvAvgLevel != null) {
            tvAvgLevel.setText(String.valueOf(totalQuizzesThisWeek));
        }

        if (tvActiveToday != null) {
            tvActiveToday.setText(String.valueOf(activeToday));
        }
    }


    private void showRemoveChildDialog() {
        if (allChildren.isEmpty()) {
            Toast.makeText(this, "No children to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        List<StyledMenuHelper.MenuItem> menuItems = new ArrayList<>();
        for (StudentProgressItem child : allChildren) {
            menuItems.add(new StyledMenuHelper.MenuItem(
                "👤",
                child.getStudentName(),
                () -> confirmRemoveChild(child)
            ));
        }

        StyledMenuHelper.showStyledMenu(
            this,
            "🗑️",
            "Remove Child",
            "Select a child to stop tracking",
            menuItems,
            null
        );
    }

    private void confirmRemoveChild(StudentProgressItem child) {
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "❓",
            "Remove Child?",
            "Are you sure you want to stop tracking " + child.getStudentName() + "'s progress?",
            "Remove",
            "Cancel",
            () -> removeChildConnection(child.getStudentId(), child.getStudentName()),
            null
        );
    }

    private void removeChildConnection(String studentId, String studentName) {
        roleManager.getStudents(currentUserId, new RoleManager.RelationshipCallback() {
            @Override
            public void onRelationshipsRetrieved(List<UserRelationship> relationships) {
                for (UserRelationship rel : relationships) {
                    if (rel.getStudentId().equals(studentId)) {
                        roleManager.removeRelationship(rel.getId(),
                            new RoleManager.RelationshipActionCallback() {
                                @Override
                                public void onSuccess(UserRelationship relationship) {
                                    Toast.makeText(ParentDashboardActivity.this,
                                        studentName + " removed from your dashboard", Toast.LENGTH_SHORT).show();
                                    loadChildren();
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(ParentDashboardActivity.this,
                                        "Failed to remove: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        return;
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ParentDashboardActivity.this,
                    "Failed to remove: " + error, Toast.LENGTH_SHORT).show();
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
        // Refresh children list when returning to this activity
        loadChildren();
    }
}

