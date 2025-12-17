package com.edulinguaghana;

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
            getSupportActionBar().setTitle("Account Management");
        }

        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
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
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountManagementActivity.this,
                                "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AccountManagementActivity.this,
                                "Failed to update profile: " + task.getException().getMessage(),
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
                                    showProgress(false);
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(AccountManagementActivity.this,
                                                "Password changed successfully!", Toast.LENGTH_SHORT).show();
                                        // Clear password fields
                                        etCurrentPassword.setText("");
                                        etNewPassword.setText("");
                                        etConfirmPassword.setText("");
                                    } else {
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

    private void sendVerificationEmail() {
        showProgress(true);

        currentUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Verification Email Sent")
                                .setMessage("A verification email has been sent to " + currentUser.getEmail() +
                                        ". Please check your inbox and verify your email address.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        Toast.makeText(AccountManagementActivity.this,
                                "Failed to send verification email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Delete Account")
                .setMessage("Are you absolutely sure you want to delete your account?\n\n" +
                        "This action is permanent and cannot be undone. All your progress, " +
                        "achievements, and data will be permanently deleted.")
                .setPositiveButton("Delete", (dialog, which) -> confirmDeleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteAccount() {
        // Show a second confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Final Confirmation")
                .setMessage("This is your last chance. Do you really want to delete your account?")
                .setPositiveButton("Yes, Delete Forever", (dialog, which) -> deleteAccount())
                .setNegativeButton("No, Keep My Account", null)
                .show();
    }

    private void deleteAccount() {
        showProgress(true);

        currentUser.delete()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(AccountManagementActivity.this,
                                "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        // Return to main activity
                        finishAffinity(); // Close all activities
                        android.content.Intent intent = new android.content.Intent(AccountManagementActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        // If deletion fails, user might need to re-authenticate
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";

                        if (errorMessage.contains("recent login")) {
                            new AlertDialog.Builder(this)
                                    .setTitle("Re-authentication Required")
                                    .setMessage("For security reasons, you need to sign in again before deleting your account. " +
                                            "Please sign out and sign in again, then try deleting your account.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        } else {
                            Toast.makeText(AccountManagementActivity.this,
                                    "Failed to delete account: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
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

