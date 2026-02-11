package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRole;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity for selecting user role (Student, Teacher, or Parent)
 * Shown on first login or when changing role
 */
public class RoleSelectionActivity extends AppCompatActivity {

    private RadioGroup roleRadioGroup;
    private RadioButton rbStudent, rbTeacher, rbParent;
    private MaterialButton btnConfirmRole;
    private TextView tvRoleDescription;
    private View descriptionCard;

    private RoleManager roleManager;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Not logged in, go to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        roleManager = new RoleManager();

        initViews();
        setupListeners();
        checkExistingRole();
        setupBackPressHandler();
    }

    private void setupBackPressHandler() {
        // Handle back button press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if this is first time setup
                boolean isFirstTime = getIntent().getBooleanExtra("first_time", false);
                if (isFirstTime) {
                    // Don't allow back on first time setup
                    Toast.makeText(RoleSelectionActivity.this, "Please select your role to continue", Toast.LENGTH_SHORT).show();
                } else {
                    // Allow back navigation
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void initViews() {
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        rbStudent = findViewById(R.id.rbStudent);
        rbTeacher = findViewById(R.id.rbTeacher);
        rbParent = findViewById(R.id.rbParent);
        btnConfirmRole = findViewById(R.id.btnConfirmRole);
        tvRoleDescription = findViewById(R.id.tvRoleDescription);
        descriptionCard = findViewById(R.id.descriptionCard);
    }

    private void setupListeners() {
        // Ensure only one radio button can be selected at a time
        roleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Clear all other selections
            if (checkedId != -1) {
                updateRoleDescription(checkedId);
            }
        });

        btnConfirmRole.setOnClickListener(v -> confirmRoleSelection());

        // Set default selection to Student
        roleRadioGroup.check(R.id.rbStudent);
        updateRoleDescription(R.id.rbStudent);
    }

    private void checkExistingRole() {
        // Check if user already has a role set
        roleManager.getUserRole(this, currentUser.getUid(), new RoleManager.RoleCallback() {
            @Override
            public void onRoleRetrieved(UserRole role) {
                if (role != UserRole.STUDENT) {
                    // Pre-select existing role
                    switch (role) {
                        case TEACHER:
                            rbTeacher.setChecked(true);
                            break;
                        case PARENT:
                            rbParent.setChecked(true);
                            break;
                        default:
                            rbStudent.setChecked(true);
                    }
                }
            }

            @Override
            public void onError(String error) {
                // Default to student, no action needed
            }
        });
    }

    private void updateRoleDescription(int checkedId) {
        String description;
        if (checkedId == R.id.rbStudent) {
            description = "📚 Learn languages with interactive lessons\n" +
                    "✨ Complete quizzes and track your progress\n" +
                    "🏆 Compete with friends on leaderboards\n" +
                    "🎮 Earn badges and achievements";
        } else if (checkedId == R.id.rbTeacher) {
            description = "👨‍🏫 Monitor your students' learning progress\n" +
                    "📊 View detailed performance analytics\n" +
                    "📝 Track quiz scores and completion rates\n" +
                    "👥 Connect with students using invite codes";
        } else if (checkedId == R.id.rbParent) {
            description = "👨‍👩‍👧‍👦 Track your children's learning journey\n" +
                    "📈 View progress reports and achievements\n" +
                    "🎯 Monitor quiz performance and activity\n" +
                    "🔗 Connect with children using invite codes";
        } else {
            description = "Select a role to see details";
        }

        tvRoleDescription.setText(description);

        // Animate description update
        descriptionCard.animate()
                .alpha(0f)
                .setDuration(100)
                .withEndAction(() -> {
                    descriptionCard.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    private void confirmRoleSelection() {
        int selectedId = roleRadioGroup.getCheckedRadioButtonId();
        UserRole selectedRole;

        if (selectedId == R.id.rbStudent) {
            selectedRole = UserRole.STUDENT;
        } else if (selectedId == R.id.rbTeacher) {
            selectedRole = UserRole.TEACHER;
        } else if (selectedId == R.id.rbParent) {
            selectedRole = UserRole.PARENT;
        } else {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmRole.setEnabled(false);

        // Save role to Firebase
        roleManager.setUserRole(this, currentUser.getUid(), selectedRole);

        Toast.makeText(this, "Role set to " + selectedRole.name(), Toast.LENGTH_SHORT).show();

        // Navigate to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

