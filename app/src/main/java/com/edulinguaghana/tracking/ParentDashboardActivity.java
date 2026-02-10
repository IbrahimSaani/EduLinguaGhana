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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dashboard for parents to view their children's progress
 */
public class ParentDashboardActivity extends AppCompatActivity {

    private RecyclerView childrenRecyclerView;
    private StudentProgressAdapter adapter;
    private ProgressBar loadingProgress;
    private TextView emptyTextView;
    private ExtendedFloatingActionButton fabAddChild;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyStateLayout;
    private MaterialButton btnSort;
    private MaterialButton btnAddFirstChild;

    // Statistics views
    private TextView tvTotalChildren;
    private TextView tvAvgLevel;
    private TextView tvActiveToday;

    private RoleManager roleManager;
    private ProgressTracker progressTracker;
    private String currentUserId;
    private List<StudentProgressItem> allChildren = new ArrayList<>();
    private int currentSortOption = 0; // 0=Name, 1=Level, 2=Activity

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
        fabAddChild = findViewById(R.id.fabAddChild);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnSort = findViewById(R.id.btnSort);
        btnAddFirstChild = findViewById(R.id.btnAddFirstChild);

        // Statistics views
        tvTotalChildren = findViewById(R.id.tvTotalChildren);
        tvAvgLevel = findViewById(R.id.tvAvgLevel);
        tvActiveToday = findViewById(R.id.tvActiveToday);

        fabAddChild.setOnClickListener(v -> openRelationshipManagement());

        if (btnAddFirstChild != null) {
            btnAddFirstChild.setOnClickListener(v -> openRelationshipManagement());
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
        String[] options = {"Sort by Name", "Sort by Level", "Sort by Recent Activity"};

        new AlertDialog.Builder(this)
            .setTitle("Sort Children")
            .setSingleChoiceItems(options, currentSortOption, (dialog, which) -> {
                currentSortOption = which;
                sortChildren();
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void sortChildren() {
        if (allChildren.isEmpty()) return;

        switch (currentSortOption) {
            case 0: // By Name
                Collections.sort(allChildren, (a, b) ->
                    a.getStudentName().compareToIgnoreCase(b.getStudentName()));
                break;
            case 1: // By Level
                Collections.sort(allChildren, (a, b) ->
                    Integer.compare(b.getProgress().getCurrentLevel(),
                                  a.getProgress().getCurrentLevel()));
                break;
            case 2: // By Recent Activity
                Collections.sort(allChildren, (a, b) ->
                    Long.compare(b.getProgress().getLastUpdated(),
                               a.getProgress().getLastUpdated()));
                break;
        }

        adapter.updateStudents(allChildren);
    }

    private void setupRecyclerView() {
        adapter = new StudentProgressAdapter(new ArrayList<>(), studentId -> {
            // Open detailed view for this child
            Intent intent = new Intent(ParentDashboardActivity.this, StudentDetailActivity.class);
            intent.putExtra("studentId", studentId);
            startActivity(intent);
        });

        childrenRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        childrenRecyclerView.setAdapter(adapter);
    }

    private void loadChildren() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        childrenRecyclerView.setVisibility(View.GONE);

        roleManager.getStudents(currentUserId, new RoleManager.RelationshipCallback() {
            @Override
            public void onRelationshipsRetrieved(List<UserRelationship> relationships) {
                loadingProgress.setVisibility(View.GONE);
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }

                if (relationships.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    childrenRecyclerView.setVisibility(View.GONE);
                    updateStatistics(new ArrayList<>());
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    childrenRecyclerView.setVisibility(View.VISIBLE);
                    loadChildrenProgress(relationships);
                }
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                emptyStateLayout.setVisibility(View.VISIBLE);
                childrenRecyclerView.setVisibility(View.GONE);
                if (emptyTextView != null) {
                    emptyTextView.setText("Error loading children: " + error);
                }
                Toast.makeText(ParentDashboardActivity.this,
                             "Failed to load children", Toast.LENGTH_SHORT).show();
                updateStatistics(new ArrayList<>());
            }
        });
    }

    private void loadChildrenProgress(List<UserRelationship> relationships) {
        allChildren.clear();
        final int totalChildren = relationships.size();
        final int[] loadedCount = {0};

        for (UserRelationship relationship : relationships) {
            String studentId = relationship.getStudentId();
            String studentName = relationship.getStudentName();

            // Load progress for each child
            progressTracker.getProgressAggregate(studentId, new ProgressTracker.ProgressAggregateCallback() {
                @Override
                public void onAggregateRetrieved(ProgressAggregate aggregate) {
                    StudentProgressItem item = new StudentProgressItem(
                        studentId,
                        studentName,
                        aggregate
                    );
                    allChildren.add(item);
                    loadedCount[0]++;

                    if (loadedCount[0] == totalChildren) {
                        sortChildren();
                        updateStatistics(allChildren);
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
                    allChildren.add(item);
                    loadedCount[0]++;

                    if (loadedCount[0] == totalChildren) {
                        sortChildren();
                        updateStatistics(allChildren);
                    }
                }
            });
        }
    }

    private void updateStatistics(List<StudentProgressItem> children) {
        int totalChildren = children.size();
        int totalLevels = 0;
        int activeToday = 0;
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);

        for (StudentProgressItem child : children) {
            ProgressAggregate progress = child.getProgress();
            totalLevels += progress.getCurrentLevel();

            if (progress.getLastUpdated() > oneDayAgo) {
                activeToday++;
            }
        }

        if (tvTotalChildren != null) {
            tvTotalChildren.setText(String.valueOf(totalChildren));
        }

        if (tvAvgLevel != null) {
            int avgLevel = totalChildren > 0 ? totalLevels / totalChildren : 0;
            tvAvgLevel.setText(String.valueOf(avgLevel));
        }

        if (tvActiveToday != null) {
            tvActiveToday.setText(String.valueOf(activeToday));
        }
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

