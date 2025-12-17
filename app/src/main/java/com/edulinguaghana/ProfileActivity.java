package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private View notSignedInLayout, signedInLayout;
    private MaterialButton btnGoToLogin, btnManageAccount, btnSignOut;
    private TextView tvUserName, tvUserEmail, tvProfileStreak, tvTotalLessons, tvBestScore, tvFavoriteLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        initViews();
        setupProfile();
        setupListeners();
    }

    private void initViews() {
        notSignedInLayout = findViewById(R.id.notSignedInLayout);
        signedInLayout = findViewById(R.id.signedInLayout);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        btnManageAccount = findViewById(R.id.btnManageAccount);
        btnSignOut = findViewById(R.id.btnSignOut);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvProfileStreak = findViewById(R.id.tvProfileStreak);
        tvTotalLessons = findViewById(R.id.tvTotalLessons);
        tvBestScore = findViewById(R.id.tvBestScore);
        tvFavoriteLanguage = findViewById(R.id.tvFavoriteLanguage);
    }

    private void setupProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in
            notSignedInLayout.setVisibility(View.GONE);
            signedInLayout.setVisibility(View.VISIBLE);

            // Display user info
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            tvUserName.setText(displayName != null ? displayName : "User");
            tvUserEmail.setText(email != null ? email : "");

            // Load user statistics from ProgressManager
            // Display streak (TODO: implement streak tracking)
            tvProfileStreak.setText("0 days");

            // Display total lessons (using total quizzes as proxy)
            int totalLessons = ProgressManager.getTotalQuizzes(this);
            tvTotalLessons.setText(String.valueOf(totalLessons));

            // Display best score
            int bestScore = ProgressManager.getHighScore(this);
            tvBestScore.setText(bestScore + " / 10");

            // Display favorite language (TODO: implement favorite language tracking)
            tvFavoriteLanguage.setText("Not set yet");
        } else {
            // User is not signed in
            notSignedInLayout.setVisibility(View.VISIBLE);
            signedInLayout.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnManageAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Account management coming soon!", Toast.LENGTH_SHORT).show();
            // TODO: Implement account management (change password, update profile, etc.)
        });

        btnSignOut.setOnClickListener(v -> showSignOutDialog());
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(ProfileActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    setupProfile(); // Refresh the UI
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupProfile(); // Refresh profile when returning to this activity
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

