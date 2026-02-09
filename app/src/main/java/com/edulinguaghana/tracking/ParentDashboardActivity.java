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
 * Dashboard for parents to view their children's progress
 */
public class ParentDashboardActivity extends AppCompatActivity {

    private RecyclerView childrenRecyclerView;
    private StudentProgressAdapter adapter;
    private ProgressBar loadingProgress;
    private TextView emptyTextView;
    private FloatingActionButton fabAddChild;

    private RoleManager roleManager;
    private ProgressTracker progressTracker;
    private String currentUserId;

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
        fabAddChild = findViewById(R.id.fabAddChild);

        fabAddChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, RelationshipManagementActivity.class);
            startActivity(intent);
        });
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
        emptyTextView.setVisibility(View.GONE);

        roleManager.getStudents(currentUserId, new RoleManager.RelationshipCallback() {
            @Override
            public void onRelationshipsRetrieved(List<UserRelationship> relationships) {
                loadingProgress.setVisibility(View.GONE);

                if (relationships.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                    emptyTextView.setText("No children linked yet.\n\nTap the + button to link your child's account.");
                } else {
                    emptyTextView.setVisibility(View.GONE);
                    loadChildrenProgress(relationships);
                }
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText("Error loading children: " + error);
                Toast.makeText(ParentDashboardActivity.this,
                             "Failed to load children", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChildrenProgress(List<UserRelationship> relationships) {
        List<StudentProgressItem> progressItems = new ArrayList<>();

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
        // Refresh children list when returning to this activity
        loadChildren();
    }
}

