package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AccountManagementActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private AvatarView avatarView;
    private MaterialButton btnEditAvatar;
    private TextInputEditText etDisplayName, etCurrentPassword, etNewPassword, etConfirmPassword;
    private TextView tvEmail;
    private MaterialButton btnUpdateProfile, btnChangePassword, btnSendVerificationEmail, btnDeleteAccount;
    private MaterialCardView changePasswordCard, emailVerificationCard;
    private View progressOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_management);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Title is handled by the CollapsingToolbarLayout header
            getSupportActionBar().setTitle("");
        }

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        avatarView = findViewById(R.id.avatarView);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        etDisplayName = findViewById(R.id.etDisplayName);
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
    }

    private void loadUserData() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            boolean isEmailVerified = currentUser.isEmailVerified();

            etDisplayName.setText(displayName != null ? displayName : "");
            tvEmail.setText(email != null ? email : "");

            // Check if user signed in with email/password provider
            boolean hasPasswordProvider = false;
            if (currentUser.getProviderData() != null) {
                for (com.google.firebase.auth.UserInfo profile : currentUser.getProviderData()) {
                    if (profile.getProviderId().equals("password")) {
                        hasPasswordProvider = true;
                        break;
                    }
                }
            }

            // Hide password change card if user signed in with Google/Facebook
            if (!hasPasswordProvider) {
                changePasswordCard.setVisibility(View.GONE);
            }

            // Show email verification card if email is not verified
            if (!isEmailVerified && hasPasswordProvider) {
                emailVerificationCard.setVisibility(View.VISIBLE);
            }
        }
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

    private void updateProfile() {
        String newDisplayName = etDisplayName.getText().toString().trim();

        if (TextUtils.isEmpty(newDisplayName)) {
            etDisplayName.setError("Display name cannot be empty");
            etDisplayName.requestFocus();
            return;
        }

        showProgress(true);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Also update the profile in Firebase Realtime Database
                        updateProfileInDatabase(newDisplayName);
                    } else {
                        showProgress(false);
                        Toast.makeText(AccountManagementActivity.this,
                                "Failed to update profile: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateProfileInDatabase(String displayName) {
        com.google.firebase.database.DatabaseReference usersRef =
                com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(currentUser.getUid());

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("displayName", displayName);
        updates.put("username", displayName);
        updates.put("updatedAt", System.currentTimeMillis());

        usersRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountManagementActivity.this,
                                "✓ Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AccountManagementActivity.this,
                                "Profile updated on device, but database sync failed: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Enter your current password");
            etCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Enter new password");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        showProgress(true);

        // Re-authenticate user before changing password
        String email = currentUser.getEmail();
        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Now update the password
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        // Log password change to database for security tracking
                                        updatePasswordChangeInDatabase();
                                    } else {
                                        showProgress(false);
                                        Toast.makeText(AccountManagementActivity.this,
                                                "Failed to change password: " + updateTask.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        showProgress(false);
                        Toast.makeText(AccountManagementActivity.this,
                                "Authentication failed. Please check your current password.",
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
                                "🔒 Password changed successfully!", Toast.LENGTH_SHORT).show();
                        // Clear password fields
                        etCurrentPassword.setText("");
                        etNewPassword.setText("");
                        etConfirmPassword.setText("");
                    } else {
                        Toast.makeText(AccountManagementActivity.this,
                                "Password changed on device, but database sync failed: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
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
                            "Verification Email Sent",
                            "A verification email has been sent to " + currentUser.getEmail() +
                                ". Please check your inbox and verify your email address.",
                            "Verified My Email",
                            "OK",
                            this::refreshEmailVerificationStatus,
                            null
                        );
                    } else {
                        Toast.makeText(AccountManagementActivity.this,
                                "Failed to send verification email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void refreshEmailVerificationStatus() {
        showProgress(true);

        // Reload the user to get the latest email verification status
        currentUser.reload()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        // Update the current user reference
                        currentUser = mAuth.getCurrentUser();
                        
                        if (currentUser != null && currentUser.isEmailVerified()) {
                            // Email is now verified, hide the verification card
                            emailVerificationCard.setVisibility(View.GONE);
                            Toast.makeText(AccountManagementActivity.this,
                                    "Email verified successfully! ✓", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountManagementActivity.this,
                                    "Email not verified yet. Please check your email and verify.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AccountManagementActivity.this,
                                "Failed to check email verification status: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showDeleteAccountDialog() {
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "⚠️",
            "Delete Account",
            "Are you absolutely sure you want to delete your account?\n\n" +
                        "This action is permanent and cannot be undone. All your progress, " +
                        "achievements, and data will be permanently deleted.",
            "Delete",
            "Cancel",
            this::confirmDeleteAccount,
            null
        );
    }

    private void confirmDeleteAccount() {
        // Show a second confirmation dialog
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "🛑",
            "Final Confirmation",
            "This is your last chance. Do you really want to delete your account?",
            "Yes, Delete Forever",
            "No, Keep My Account",
            this::deleteAccount,
            null
        );
    }

    private void deleteAccount() {
        // First, prompt for password to re-authenticate
        showReAuthenticationDialog();
    }

    private void showReAuthenticationDialog() {
        final TextInputEditText passwordInput = new TextInputEditText(this);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Enter your password");

        StyledMenuHelper.showStyledCustomDialog(
            this,
            "🔑",
            "Confirm Password",
            "For security reasons, please enter your password to delete your account.",
            passwordInput,
            "Delete Account",
            "Cancel",
            () -> {
                String password = passwordInput.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(AccountManagementActivity.this,
                            "Password cannot be empty", Toast.LENGTH_SHORT).show();
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
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        // Re-authenticate first
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(reAuthTask -> {
                    if (reAuthTask.isSuccessful()) {
                        // Now delete user data from database before deleting auth account
                        deleteUserDataFromDatabase(currentUser.getUid());
                    } else {
                        showProgress(false);
                        Toast.makeText(AccountManagementActivity.this,
                                "Authentication failed. Please check your password.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteUserDataFromDatabase(String userId) {
        com.google.firebase.database.DatabaseReference dbRef =
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference();

        // Delete from users node
        dbRef.child("users").child(userId).removeValue()
                .addOnCompleteListener(usersTask -> {
                    if (usersTask.isSuccessful()) {
                        android.util.Log.d("AccountManagement", "User data deleted from users node");
                    } else {
                        android.util.Log.e("AccountManagement", "Failed to delete user data", usersTask.getException());
                    }
                    // Continue with other deletions
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
                    // Continue with other deletions
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
                    // Continue with other deletions
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
                    // Continue with other deletions
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
                    // Continue with other deletions
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
                    // Continue with other deletions
                    deleteRelationships(userId);
                });
    }

    private void deleteRelationships(String userId) {
        // Delete all relationships involving this user
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
        // Delete all challenges involving this user
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
                // Finally delete the Firebase Auth account
                deleteFirebaseAuthUser();
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                android.util.Log.e("AccountManagement", "Failed to delete challenges", error.toException());
                // Continue anyway
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
                                "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        // Sign out and return to main activity
                        mAuth.signOut();
                        finishAffinity();
                        android.content.Intent intent = new android.content.Intent(AccountManagementActivity.this, MainActivity.class);
                        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(AccountManagementActivity.this,
                                "Failed to delete account: " + errorMessage,
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
        // Reload avatar when returning from editor
        if (avatarView != null) {
            avatarView.updateAvatar();
        }

        // Refresh email verification status and load latest avatar from database
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

                                // Update email verification card visibility
                                if (!isEmailVerified && hasPasswordProvider) {
                                    emailVerificationCard.setVisibility(View.VISIBLE);
                                } else {
                                    emailVerificationCard.setVisibility(View.GONE);
                                }
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

