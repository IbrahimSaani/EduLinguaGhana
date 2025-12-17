package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private RecyclerView achievementsRecyclerView;
    private LinearLayout emptyStateLayout;
    private LinearLayout loginRequiredLayout;
    private TextView tvUnlockedCount;
    private TextView tvTotalCount;
    private OfflineManager offlineManager;
    private AchievementManager achievementManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize views
        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loginRequiredLayout = findViewById(R.id.loginRequiredLayout);
        tvUnlockedCount = findViewById(R.id.tvUnlockedCount);
        tvTotalCount = findViewById(R.id.tvTotalCount);

        // Initialize managers
        offlineManager = new OfflineManager(this);
        achievementManager = new AchievementManager(this);

        // Check if user is logged in
        if (!offlineManager.isLoggedIn()) {
            showLoginRequired();
            return;
        }

        // Check internet connection
        if (!offlineManager.isOnline()) {
            showOfflineMessage();
            return;
        }

        // Setup RecyclerView
        setupRecyclerView();

        // Load achievements
        loadAchievements();
    }

    private void showLoginRequired() {
        achievementsRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        loginRequiredLayout.setVisibility(View.VISIBLE);

        findViewById(R.id.btnGoToLogin).setOnClickListener(v -> {
            // Navigate to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showOfflineMessage() {
        new AlertDialog.Builder(this)
            .setTitle("Internet Required")
            .setMessage("Achievements require an internet connection. Please connect and try again.")
            .setPositiveButton("OK", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    private void setupRecyclerView() {
        achievementsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        achievementsRecyclerView.setHasFixedSize(true);
    }

    private void loadAchievements() {
        List<Achievement> achievements = achievementManager.getAllAchievements();
        int unlockedCount = achievementManager.getUnlockedCount();
        int totalCount = achievementManager.getTotalCount();

        // Update counts
        if (tvUnlockedCount != null) {
            tvUnlockedCount.setText(String.valueOf(unlockedCount));
        }
        if (tvTotalCount != null) {
            tvTotalCount.setText(String.valueOf(totalCount));
        }

        if (achievements.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            achievementsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            achievementsRecyclerView.setVisibility(View.VISIBLE);

            AchievementsAdapter adapter = new AchievementsAdapter(this, achievements);
            achievementsRecyclerView.setAdapter(adapter);
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

