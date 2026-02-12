package com.edulinguaghana.tracking;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edulinguaghana.R;
import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRelationship;
import com.edulinguaghana.roles.UserRole;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Activity for managing teacher-student and parent-child relationships
 */
public class RelationshipManagementActivity extends AppCompatActivity {

    private LinearLayout addStudentSection;
    private LinearLayout myCodeSection;
    private TextView tvMyCode;
    private Button btnCopyCode;
    private TextInputEditText etStudentEmail;
    private Button btnAddStudent;
    private RecyclerView pendingRequestsRecyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyTextView;
    private LinearLayout emptyStateLayout;

    private RoleManager roleManager;
    private String currentUserId;
    private String currentUserName;
    private UserRole currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relationship_management);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        currentUserId = user.getUid();
        currentUserName = user.getDisplayName() != null ? user.getDisplayName() : user.getEmail();

        roleManager = new RoleManager();

        initViews();
        loadUserRole();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Connections");
        }

        addStudentSection = findViewById(R.id.addStudentSection);
        myCodeSection = findViewById(R.id.myCodeSection);
        tvMyCode = findViewById(R.id.tvMyCode);
        btnCopyCode = findViewById(R.id.btnCopyCode);
        etStudentEmail = findViewById(R.id.etStudentEmail);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        pendingRequestsRecyclerView = findViewById(R.id.pendingRequestsRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        emptyTextView = findViewById(R.id.emptyTextView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        btnCopyCode.setOnClickListener(v -> copyCodeToClipboard());
        btnAddStudent.setOnClickListener(v -> addStudent());

        pendingRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadUserRole() {
        roleManager.getUserRole(this, currentUserId, new RoleManager.RoleCallback() {
            @Override
            public void onRoleRetrieved(UserRole role) {
                currentUserRole = role;
                setupUIForRole(role);
                loadPendingRequests();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RelationshipManagementActivity.this,
                             "Error loading role: " + error, Toast.LENGTH_SHORT).show();
                // Default to student
                currentUserRole = UserRole.STUDENT;
                setupUIForRole(UserRole.STUDENT);
            }
        });
    }

    private void setupUIForRole(UserRole role) {
        if (role == UserRole.TEACHER || role == UserRole.PARENT) {
            // Teachers and parents can add students/children
            addStudentSection.setVisibility(View.VISIBLE);
            myCodeSection.setVisibility(View.GONE);

            String label = role == UserRole.TEACHER ? "Add Student" : "Add Child";
            btnAddStudent.setText(label);
            etStudentEmail.setHint(role == UserRole.TEACHER ?
                "Enter student's email or code" : "Enter child's email or code");
        } else {
            // Students show their code for teachers/parents to use
            addStudentSection.setVisibility(View.GONE);
            myCodeSection.setVisibility(View.VISIBLE);
            tvMyCode.setText(currentUserId.substring(0, Math.min(8, currentUserId.length())));
        }
    }

    private void copyCodeToClipboard() {
        String code = currentUserId.substring(0, Math.min(8, currentUserId.length()));
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Student Code", code);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Code copied to clipboard!", Toast.LENGTH_SHORT).show();
    }

    private void addStudent() {
        String emailOrCode = etStudentEmail.getText().toString().trim();

        if (TextUtils.isEmpty(emailOrCode)) {
            etStudentEmail.setError("Please enter student's email or code");
            return;
        }

        btnAddStudent.setEnabled(false);
        loadingProgress.setVisibility(View.VISIBLE);

        // Search for student by email or UID
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Try to find by email first
        usersRef.orderByChild("email").equalTo(emailOrCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Found by email
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String studentId = child.getKey();
                                String studentName = child.child("displayName").getValue(String.class);
                                createRelationship(studentId, studentName);
                                return;
                            }
                        } else {
                            // Try to find by UID prefix (code)
                            searchByCode(emailOrCode);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        btnAddStudent.setEnabled(true);
                        loadingProgress.setVisibility(View.GONE);
                        Toast.makeText(RelationshipManagementActivity.this,
                                     "Search failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchByCode(String code) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.getKey();
                    if (uid != null && uid.startsWith(code)) {
                        String studentName = child.child("displayName").getValue(String.class);
                        createRelationship(uid, studentName);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    btnAddStudent.setEnabled(true);
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(RelationshipManagementActivity.this,
                                 "Student not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                btnAddStudent.setEnabled(true);
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(RelationshipManagementActivity.this,
                             "Search failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createRelationship(String studentId, String studentName) {
        UserRelationship.RelationType type = currentUserRole == UserRole.TEACHER ?
                UserRelationship.RelationType.TEACHER_STUDENT :
                UserRelationship.RelationType.PARENT_CHILD;

        roleManager.createRelationship(currentUserId, currentUserName,
                                      studentId, studentName, type,
                                      new RoleManager.RelationshipActionCallback() {
            @Override
            public void onSuccess(UserRelationship relationship) {
                btnAddStudent.setEnabled(true);
                loadingProgress.setVisibility(View.GONE);
                etStudentEmail.setText("");

                String message = currentUserRole == UserRole.TEACHER ?
                        "Student connection request sent!" : "Child connection request sent!";
                Toast.makeText(RelationshipManagementActivity.this,
                             message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                btnAddStudent.setEnabled(true);
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(RelationshipManagementActivity.this,
                             "Failed to create connection: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadPendingRequests() {
        // Students see pending requests from teachers/parents
        // Teachers/Parents see their sent requests

        if (currentUserRole == UserRole.STUDENT) {
            roleManager.getPendingRequests(currentUserId, new RoleManager.RelationshipCallback() {
                @Override
                public void onRelationshipsRetrieved(List<UserRelationship> relationships) {
                    loadingProgress.setVisibility(View.GONE);
                    if (relationships.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        pendingRequestsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyStateLayout.setVisibility(View.GONE);
                        pendingRequestsRecyclerView.setVisibility(View.VISIBLE);
                        setupPendingRequestsAdapter(relationships);
                    }
                }

                @Override
                public void onError(String error) {
                    loadingProgress.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    emptyTextView.setText("Error loading requests");
                }
            });
        }
    }

    private void setupPendingRequestsAdapter(List<UserRelationship> relationships) {
        PendingRequestsAdapter adapter = new PendingRequestsAdapter(
            relationships,
            relationship -> acceptRequest(relationship),
            relationship -> rejectRequest(relationship)
        );
        pendingRequestsRecyclerView.setAdapter(adapter);
    }

    private void acceptRequest(UserRelationship relationship) {
        roleManager.acceptRelationship(relationship.getId(),
                                      new RoleManager.RelationshipActionCallback() {
            @Override
            public void onSuccess(UserRelationship rel) {
                Toast.makeText(RelationshipManagementActivity.this,
                             "Connection accepted!", Toast.LENGTH_SHORT).show();
                loadPendingRequests();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RelationshipManagementActivity.this,
                             "Failed to accept: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectRequest(UserRelationship relationship) {
        new AlertDialog.Builder(this)
            .setTitle("Reject Request")
            .setMessage("Are you sure you want to reject this connection request?")
            .setPositiveButton("Reject", (dialog, which) -> {
                roleManager.removeRelationship(relationship.getId(),
                                             new RoleManager.RelationshipActionCallback() {
                    @Override
                    public void onSuccess(UserRelationship rel) {
                        Toast.makeText(RelationshipManagementActivity.this,
                                     "Request rejected", Toast.LENGTH_SHORT).show();
                        loadPendingRequests();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(RelationshipManagementActivity.this,
                                     "Failed to reject: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}

