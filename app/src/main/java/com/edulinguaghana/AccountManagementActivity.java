package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRole;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AccountManagementActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private AvatarView avatarView;
    private MaterialButton btnEditAvatar;
    private TextInputEditText etDisplayName, etLearnerAge, etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialAutoCompleteTextView etLearnerClass;
    private TextView tvEmail;
    private MaterialButton btnUpdateProfile, btnChangePassword, btnSendVerificationEmail, btnDeleteAccount;
    private MaterialCardView changePasswordCard, emailVerificationCard;
    private View progressOverlay;
    private View learnerDetailsSection;
    private String[] classOptions;
    private RoleManager roleManager;
    private UserRole currentUserRole = UserRole.STUDENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        roleManager = new RoleManager();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        initViews();
        setupClassDropdown();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        avatarView = findViewById(R.id.avatarView);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        etDisplayName = findViewById(R.id.etDisplayName);
        etLearnerAge = findViewById(R.id.etLearnerAge);
        etLearnerClass = findViewById(R.id.etLearnerClass);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvEmail = findViewById(R.id.tvEmail);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnSendVerificationEmail = findViewById(R.id.btnSendVerificationEmail);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        changePasswordCard = findViewById(R.id.changePasswordCard);
        emailVerificationCard = findViewById(R.id.emailVerificationCard);
        progressOverlay = findViewById(R.id.progressOverlay);
        learnerDetailsSection = findViewById(R.id.learnerDetailsSection);
        classOptions = getResources().getStringArray(R.array.basic_class_options);
    }

    private void setupClassDropdown() {
        if (etLearnerClass == null) return;

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                classOptions
        );
        etLearnerClass.setAdapter(adapter);
        etLearnerClass.setOnClickListener(v -> etLearnerClass.showDropDown());
    }

    private void loadUserData() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            boolean isEmailVerified = currentUser.isEmailVerified();

            etDisplayName.setText(displayName != null ? displayName : "");
            tvEmail.setText(email != null ? email : "");

            // Load saved avatar config
            if (avatarView != null) {
                AvatarBuilder.AvatarConfig config = AvatarBuilder.loadConfig(this);
                if (config != null) {
                    avatarView.setAvatarConfig(config);
                } else if (currentUser != null) {
                    // Not in local cache, show a default and sync from Firebase
                    avatarView.setAvatarConfig(new AvatarBuilder.AvatarConfig());

                    AvatarBuilder.syncWithFirebase(this, currentUser.getUid(), () -> {
                        AvatarBuilder.AvatarConfig syncedConfig = AvatarBuilder.loadConfig(this);
                        if (syncedConfig != null) {
                            runOnUiThread(() -> avatarView.setAvatarConfig(syncedConfig));
                        }
                    });
                }
            }

            boolean hasPasswordProvider = false;
            if (currentUser.getProviderData() != null) {
                for (com.google.firebase.auth.UserInfo profile : currentUser.getProviderData()) {
                    if (profile.getProviderId().equals("password")) {
                        hasPasswordProvider = true;
                        break;
                    }
                }
            }

            if (!hasPasswordProvider) {
                changePasswordCard.setVisibility(View.GONE);
            }

            if (!isEmailVerified && hasPasswordProvider) {
                emailVerificationCard.setVisibility(View.VISIBLE);
            }

            loadRoleAndProfileData();
        }
    }

    private void loadRoleAndProfileData() {
        if (currentUser == null) return;

        roleManager.getUserRole(this, currentUser.getUid(), new RoleManager.RoleCallback() {
            @Override
            public void onRoleRetrieved(UserRole role) {
                currentUserRole = role;
                boolean isStudent = role == UserRole.STUDENT;

                if (learnerDetailsSection != null) {
                    learnerDetailsSection.setVisibility(isStudent ? View.VISIBLE : View.GONE);
                }

                if (isStudent) {
                    loadLearnerProfileValues();
                } else {
                    clearLearnerFields();
                }
            }

            @Override
            public void onError(String error) {
                currentUserRole = UserRole.STUDENT;
                if (learnerDetailsSection != null) {
                    learnerDetailsSection.setVisibility(View.VISIBLE);
                }
                loadLearnerProfileValues();
            }
        });
    }

    private void clearLearnerFields() {
        if (etLearnerAge != null) etLearnerAge.setText("");
        if (etLearnerClass != null) etLearnerClass.setText("", false);
    }

    private void setupListeners() {
        btnEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(AccountManagementActivity.this, AvatarEditorActivity.class);
            startActivity(intent);
        });

        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnSendVerificationEmail.setOnClickListener(v -> sendVerificationEmail());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void loadLearnerProfileValues() {
        if (currentUser == null || etLearnerAge == null || etLearnerClass == null) {
            return;
        }

        com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid())
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        String age = snapshot.child("age").getValue(String.class);
                        String studentClass = snapshot.child("studentClass").getValue(String.class);

                        etLearnerAge.setText(TextUtils.isEmpty(age) ? "" : age);
                        etLearnerClass.setText(TextUtils.isEmpty(studentClass) ? "" : studentClass, false);
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        android.util.Log.e("AccountManagement", "Failed to load learner profile values", error.toException());
                    }
                });
    }

    private void updateProfile() {
        String newDisplayName = etDisplayName.getText().toString().trim();
        String learnerAge = etLearnerAge != null ? etLearnerAge.getText().toString().trim() : "";
        String learnerClass = etLearnerClass != null ? etLearnerClass.getText().toString().trim() : "";

        if (TextUtils.isEmpty(newDisplayName)) {
            etDisplayName.setError(getString(R.string.account_mgmt_name_empty));
            etDisplayName.requestFocus();
            return;
        }

        boolean isStudent = currentUserRole == UserRole.STUDENT;
        if (isStudent) {
            if (TextUtils.isEmpty(learnerAge) && !TextUtils.isEmpty(learnerClass)) {
                etLearnerAge.setError(getString(R.string.complete_profile_required_age));
                etLearnerAge.requestFocus();
                return;
            }

            if (!TextUtils.isEmpty(learnerAge) && TextUtils.isEmpty(learnerClass)) {
                etLearnerClass.setError(getString(R.string.complete_profile_required_class));
                etLearnerClass.requestFocus();
                return;
            }

            if (!TextUtils.isEmpty(learnerAge)) {
                try {
                    int parsedAge = Integer.parseInt(learnerAge);
                    if (parsedAge <= 0 || parsedAge > 25) {
                        etLearnerAge.setError(getString(R.string.complete_profile_required_age));
                        etLearnerAge.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    etLearnerAge.setError(getString(R.string.complete_profile_required_age));
                    etLearnerAge.requestFocus();
                    return;
                }
            }
        } else {
            learnerAge = "";
            learnerClass = "";
        }

        final String learnerAgeToSave = learnerAge;
        final String learnerClassToSave = learnerClass;

        showProgress(true);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateProfileInDatabase(newDisplayName, learnerAgeToSave, learnerClassToSave);
                    } else {
                        showProgress(false);
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.error_profile_save_failed),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateProfileInDatabase(String displayName, String age, String studentClass) {
        com.google.firebase.database.DatabaseReference usersRef =
                com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(currentUser.getUid());

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("displayName", displayName);
        updates.put("username", displayName);
        updates.put("age", age);
        updates.put("studentClass", studentClass);
        updates.put("updatedAt", System.currentTimeMillis());

        usersRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.account_mgmt_profile_updated), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.error_sync_failed),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError(getString(R.string.account_mgmt_enter_current_password));
            etCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError(getString(R.string.account_mgmt_enter_new_password));
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError(getString(R.string.account_mgmt_password_too_short));
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.account_mgmt_passwords_mismatch));
            etConfirmPassword.requestFocus();
            return;
        }

        showProgress(true);

        String email = currentUser.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        updatePasswordChangeInDatabase();
                                    } else {
                                        showProgress(false);
                                        Toast.makeText(AccountManagementActivity.this,
                                                getString(R.string.error_sync_failed),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        showProgress(false);
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.account_mgmt_reauth_failed),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updatePasswordChangeInDatabase() {
        com.google.firebase.database.DatabaseReference usersRef =
                com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(currentUser.getUid());

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("lastPasswordChangeAt", System.currentTimeMillis());

        usersRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.account_mgmt_password_changed), Toast.LENGTH_SHORT).show();
                        etCurrentPassword.setText("");
                        etNewPassword.setText("");
                        etConfirmPassword.setText("");
                    } else {
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.error_sync_failed),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendVerificationEmail() {
        showProgress(true);

        currentUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        StyledMenuHelper.showStyledConfirmationDialog(
                            this,
                            "📧",
                            getString(R.string.account_mgmt_verification_sent_title),
                            getString(R.string.account_mgmt_verification_sent_message, currentUser.getEmail()),
                            getString(R.string.account_mgmt_verified_my_email),
                            "OK",
                            this::refreshEmailVerificationStatus,
                            null
                        );
                    } else {
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.error_generic_try_again),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void refreshEmailVerificationStatus() {
        showProgress(true);

        currentUser.reload()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        currentUser = mAuth.getCurrentUser();
                        
                        if (currentUser != null && currentUser.isEmailVerified()) {
                            emailVerificationCard.setVisibility(View.GONE);
                            Toast.makeText(AccountManagementActivity.this,
                                    getString(R.string.account_mgmt_email_verified_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountManagementActivity.this,
                                    getString(R.string.account_mgmt_email_not_verified),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.error_verify_failed),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showDeleteAccountDialog() {
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "⚠️",
            getString(R.string.account_mgmt_delete_confirm_title),
            getString(R.string.account_mgmt_delete_confirm_message),
            getString(R.string.teacher_dashboard_remove_confirm), // Using existing "Remove/Delete"
            getString(R.string.teacher_dashboard_remove_cancel),
            this::confirmDeleteAccount,
            null
        );
    }

    private void confirmDeleteAccount() {
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "🛑",
            getString(R.string.account_mgmt_delete_final_title),
            getString(R.string.account_mgmt_delete_final_message),
            getString(R.string.account_mgmt_delete_forever),
            getString(R.string.account_mgmt_keep_account),
            this::deleteAccount,
            null
        );
    }

    private void deleteAccount() {
        if (currentUser == null) return;

        boolean hasEmailProvider = false;
        for (UserInfo profile : currentUser.getProviderData()) {
            if (EmailAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                hasEmailProvider = true;
                break;
            }
        }

        if (hasEmailProvider) {
            showReAuthenticationDialog();
        } else {
            // Social provider users (Google/Facebook)
            deleteUserDataFromDatabase(currentUser.getUid());
        }
    }

    private void showReAuthenticationDialog() {
        final TextInputEditText passwordInput = new TextInputEditText(this);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint(getString(R.string.account_mgmt_password_hint));

        StyledMenuHelper.showStyledCustomDialog(
            this,
            "🔑",
            getString(R.string.account_mgmt_reauth_title),
            getString(R.string.account_mgmt_reauth_message),
            passwordInput,
            getString(R.string.account_mgmt_delete_button),
            "Cancel",
            () -> {
                String password = passwordInput.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(AccountManagementActivity.this,
                            getString(R.string.account_mgmt_password_hint), Toast.LENGTH_SHORT).show();
                    return;
                }
                performReAuthenticationAndDelete(password);
            },
            null
        );
    }

    private void performReAuthenticationAndDelete(String password) {
        showProgress(true);

        String email = currentUser.getEmail();
        if (TextUtils.isEmpty(email)) {
            showProgress(false);
            Toast.makeText(this, "Email required for re-authentication", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(reAuthTask -> {
                    if (reAuthTask.isSuccessful()) {
                        deleteUserDataFromDatabase(currentUser.getUid());
                    } else {
                        showProgress(false);
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.account_mgmt_reauth_failed_delete),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteUserDataFromDatabase(String userId) {
        com.google.firebase.database.DatabaseReference dbRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference();

        dbRef.child("users").child(userId).removeValue()
                .addOnCompleteListener(usersTask -> {
                    if (usersTask.isSuccessful()) {
                        android.util.Log.d("AccountManagement", "User data deleted from users node");
                    } else {
                        android.util.Log.e("AccountManagement", "Failed to delete user data", usersTask.getException());
                    }
                    deleteFromLeaderboard(userId);
                });
    }

    private void deleteFromLeaderboard(String userId) {
        com.google.firebase.database.DatabaseReference leaderboardRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("leaderboard").child(userId);

        leaderboardRef.removeValue()
                .addOnCompleteListener(leaderboardTask -> {
                    if (leaderboardTask.isSuccessful()) {
                        android.util.Log.d("AccountManagement", "Leaderboard entry deleted");
                    } else {
                        android.util.Log.e("AccountManagement", "Failed to delete leaderboard entry", leaderboardTask.getException());
                    }
                    deleteUserProgress(currentUser.getUid());
                });
    }

    private void deleteUserProgress(String userId) {
        com.google.firebase.database.DatabaseReference progressRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("progress").child(userId);

        progressRef.removeValue()
                .addOnCompleteListener(progressTask -> {
                    if (progressTask.isSuccessful()) {
                        android.util.Log.d("AccountManagement", "Progress data deleted");
                    } else {
                        android.util.Log.e("AccountManagement", "Failed to delete progress", progressTask.getException());
                    }
                    deleteUserStats(userId);
                });
    }

    private void deleteUserStats(String userId) {
        com.google.firebase.database.DatabaseReference statsRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("userStats").child(userId);

        statsRef.removeValue()
                .addOnCompleteListener(statsTask -> {
                    if (statsTask.isSuccessful()) {
                        android.util.Log.d("AccountManagement", "User stats deleted");
                    } else {
                        android.util.Log.e("AccountManagement", "Failed to delete user stats", statsTask.getException());
                    }
                    deleteUserAggregates(userId);
                });
    }

    private void deleteUserAggregates(String userId) {
        com.google.firebase.database.DatabaseReference aggregatesRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("aggregates").child(userId);

        aggregatesRef.removeValue()
                .addOnCompleteListener(aggregatesTask -> {
                    if (aggregatesTask.isSuccessful()) {
                        android.util.Log.d("AccountManagement", "User aggregates deleted");
                    } else {
                        android.util.Log.e("AccountManagement", "Failed to delete aggregates", aggregatesTask.getException());
                    }
                    deleteMilestones(userId);
                });
    }

    private void deleteMilestones(String userId) {
        com.google.firebase.database.DatabaseReference milestonesRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("milestones").child(userId);

        milestonesRef.removeValue()
                .addOnCompleteListener(milestonesTask -> {
                    if (milestonesTask.isSuccessful()) {
                        android.util.Log.d("AccountManagement", "User milestones deleted");
                    } else {
                        android.util.Log.e("AccountManagement", "Failed to delete milestones", milestonesTask.getException());
                    }
                    deleteRelationships(userId);
                });
    }

    private void deleteRelationships(String userId) {
        com.google.firebase.database.DatabaseReference relationshipsRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("relationships");

        relationshipsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                for (com.google.firebase.database.DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String relationshipId = childSnapshot.getKey();
                    if (relationshipId != null && relationshipId.contains(userId)) {
                        childSnapshot.getRef().removeValue();
                    }
                }
                android.util.Log.d("AccountManagement", "User relationships deleted");
                deleteUserChallenges(userId);
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                android.util.Log.e("AccountManagement", "Failed to delete relationships", error.toException());
                deleteUserChallenges(userId);
            }
        });
    }

    private void deleteUserChallenges(String userId) {
        com.google.firebase.database.DatabaseReference challengesRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("challenges");

        challengesRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                for (com.google.firebase.database.DataSnapshot childSnapshot : snapshot.getChildren()) {
                    com.google.firebase.database.DataSnapshot initiatorSnapshot = childSnapshot.child("initiatorId");
                    com.google.firebase.database.DataSnapshot participantSnapshot = childSnapshot.child("participantId");

                    boolean isInvolved = false;
                    if (initiatorSnapshot.exists() && userId.equals(initiatorSnapshot.getValue(String.class))) {
                        isInvolved = true;
                    }
                    if (participantSnapshot.exists() && userId.equals(participantSnapshot.getValue(String.class))) {
                        isInvolved = true;
                    }

                    if (isInvolved) {
                        childSnapshot.getRef().removeValue();
                    }
                }
                android.util.Log.d("AccountManagement", "User challenges deleted");
                deleteFirebaseAuthUser();
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                android.util.Log.e("AccountManagement", "Failed to delete challenges", error.toException());
                deleteFirebaseAuthUser();
            }
        });
    }

    private void deleteFirebaseAuthUser() {
        currentUser.delete()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.account_mgmt_delete_success), Toast.LENGTH_SHORT).show();
                        
                        ProgressManager.resetProgress(this);
                        com.edulinguaghana.gamification.FunGameProgressManager.resetProgress(this);
                        com.edulinguaghana.gamification.XPManager.resetXP(this);
                        new StreakManager(this).resetAllData();
                        com.edulinguaghana.gamification.BadgeManager.resetBadges(this);
                        com.edulinguaghana.gamification.QuestManager.resetQuests(this);
                        new com.edulinguaghana.AchievementManager(this).resetAchievements();
                        AvatarBuilder.clearCache(this);

                        mAuth.signOut();
                        finishAffinity();
                        android.content.Intent intent = new android.content.Intent(AccountManagementActivity.this, MainActivity.class);
                        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        showProgress(false);
                        Toast.makeText(AccountManagementActivity.this,
                                getString(R.string.error_delete_failed),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (avatarView != null) {
            AvatarBuilder.AvatarConfig config = AvatarBuilder.loadConfig(this);
            avatarView.setAvatarConfig(config);
        }

        if (currentUser != null) {
            currentUser.reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                boolean isEmailVerified = currentUser.isEmailVerified();
                                boolean hasPasswordProvider = false;

                                if (currentUser.getProviderData() != null) {
                                    for (com.google.firebase.auth.UserInfo profile : currentUser.getProviderData()) {
                                        if (profile.getProviderId().equals("password")) {
                                            hasPasswordProvider = true;
                                            break;
                                        }
                                    }
                                }

                                if (!isEmailVerified && hasPasswordProvider) {
                                    emailVerificationCard.setVisibility(View.VISIBLE);
                                } else {
                                    emailVerificationCard.setVisibility(View.GONE);
                                }

                                loadRoleAndProfileData();
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
