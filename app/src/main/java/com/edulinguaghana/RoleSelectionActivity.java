package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
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
                    Toast.makeText(RoleSelectionActivity.this, R.string.role_selection_first_time_toast, Toast.LENGTH_SHORT).show();
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
            if (checkedId != -1) {
                group.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                updateRoleDescription(checkedId);
            }
        });

        btnConfirmRole.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            confirmRoleSelection();
        });

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
            description = getString(R.string.role_desc_student);
        } else if (checkedId == R.id.rbTeacher) {
            description = getString(R.string.role_desc_teacher);
        } else if (checkedId == R.id.rbParent) {
            description = getString(R.string.role_desc_parent);
        } else {
            description = getString(R.string.role_desc_default);
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
            Toast.makeText(this, R.string.role_selection_confirm_prompt, Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmRole.setEnabled(false);

        // Save role to Firebase
        roleManager.setUserRole(this, currentUser.getUid(), selectedRole);

        Toast.makeText(this, getString(R.string.role_selection_success_toast, selectedRole.name()), Toast.LENGTH_SHORT).show();

        boolean isFirstTime = getIntent().getBooleanExtra("first_time", false);
        if (isFirstTime) {
            // For Teachers and Parents, skip profile completion (age/class) and go straight to Main
            if (selectedRole == UserRole.TEACHER || selectedRole == UserRole.PARENT) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }

            Intent intent = new Intent(this, CompleteProfileActivity.class);
            intent.putExtra(CompleteProfileActivity.EXTRA_NEXT_STEP, CompleteProfileActivity.NEXT_STEP_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Navigate to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

