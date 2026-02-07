package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView leaderboardRecyclerView;
    private LinearLayout emptyStateLayout;
    private LinearLayout loginRequiredLayout;
    private LinearLayout mainContentLayout;
    private ProgressBar progressBar;
    private TextView tvYourRank;
    private TextView tvYourScore;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;

    private OfflineManager offlineManager;
    private DatabaseReference leaderboardRef;
    private List<LeaderboardEntry> leaderboardList;
    private LeaderboardAdapter adapter;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize views
        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loginRequiredLayout = findViewById(R.id.loginRequiredLayout);
        mainContentLayout = findViewById(R.id.mainContentLayout);
        progressBar = findViewById(R.id.progressBar);
        tvYourRank = findViewById(R.id.tvYourRank);
        tvYourScore = findViewById(R.id.tvYourScore);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Initialize managers
        offlineManager = new OfflineManager(this);
        leaderboardRef = FirebaseDatabase.getInstance().getReference("leaderboard");
        leaderboardList = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if user is logged in
        if (!offlineManager.isLoggedIn() || currentUser == null) {
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

        // Setup Swipe to Refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // When user pulls to refresh, reload leaderboard
                loadLeaderboard();
            });
            // Use same color as accent for the refresh spinner
            swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        }

        // Load leaderboard
        loadLeaderboard();
    }

    private void showLoginRequired() {
        mainContentLayout.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        loginRequiredLayout.setVisibility(View.VISIBLE);

        findViewById(R.id.btnGoToLogin).setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showOfflineMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Internet Required")
                .setMessage("Leaderboard requires an internet connection. Please connect and try again.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void setupRecyclerView() {
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecyclerView.setHasFixedSize(true);
        adapter = new LeaderboardAdapter(this, leaderboardList);
        leaderboardRecyclerView.setAdapter(adapter);
    }

    private void loadLeaderboard() {
        // Detect whether this load was triggered by swipe-to-refresh so we can show a small indicator
        final boolean swipeTriggered = (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing());

        // If this was not triggered by swipe-refresh, show the central progress indicator;
        // otherwise keep the current content visible and let the swipe spinner show progress.
        if (!swipeTriggered) {
            progressBar.setVisibility(View.VISIBLE);
            mainContentLayout.setVisibility(View.GONE);
        }

        // Query top 100 scores ordered by score
        Query leaderboardQuery = leaderboardRef.orderByChild("score").limitToLast(100);

        leaderboardQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                leaderboardList.clear();

                if (!snapshot.exists()) {
                    // No data yet - show empty state
                    progressBar.setVisibility(View.GONE);
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    mainContentLayout.setVisibility(View.GONE);
                    tvYourRank.setText("#--");
                    tvYourScore.setText("0");
                    if (swipeTriggered) {
                        Snackbar.make(findViewById(android.R.id.content), "No rankings yet", Snackbar.LENGTH_SHORT).show();
                    }
                    return;
                }

                // First, collect all entries
                List<LeaderboardEntry> tempList = new ArrayList<>();
                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    LeaderboardEntry entry = entrySnapshot.getValue(LeaderboardEntry.class);
                    if (entry != null) {
                        tempList.add(entry);
                    }
                }

                // Now fetch usernames from users node for each entry
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                final int totalEntries = tempList.size();
                final int[] processedCount = {0};

                if (totalEntries == 0) {
                    progressBar.setVisibility(View.GONE);
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    mainContentLayout.setVisibility(View.GONE);
                    return;
                }

                for (LeaderboardEntry entry : tempList) {
                    // Check if userName is missing or looks like a UID
                    String userName = entry.getUserName();
                    boolean isLikelyUID = userName == null || userName.isEmpty() ||
                                          userName.length() > 20 ||
                                          userName.equals(entry.getUserId()) ||
                                          (userName.length() >= 20 && !userName.contains(" "));

                    if (isLikelyUID) {
                        // Fetch username from users node
                        usersRef.child(entry.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (userSnapshot.exists()) {
                                    String displayName = userSnapshot.child("displayName").getValue(String.class);
                                    String email = userSnapshot.child("email").getValue(String.class);

                                    // Check if displayName is valid (not null, not empty, not a UID)
                                    if (displayName != null && !displayName.isEmpty() &&
                                        displayName.length() <= 30 &&
                                        !displayName.equals(entry.getUserId())) {
                                        entry.setUserName(displayName);
                                    } else if (email != null && email.contains("@")) {
                                        // Use email prefix if displayName is invalid
                                        entry.setUserName(email.split("@")[0]);
                                    } else {
                                        // Last resort: generate a username from UID
                                        entry.setUserName("User" + entry.getUserId().substring(0, Math.min(6, entry.getUserId().length())));
                                    }
                                } else {
                                    // User not found in database, use fallback
                                    entry.setUserName("User" + entry.getUserId().substring(0, Math.min(6, entry.getUserId().length())));
                                }
                                leaderboardList.add(entry);
                                processedCount[0]++;

                                if (processedCount[0] == totalEntries) {
                                    finishLoadingLeaderboard(swipeTriggered);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Use fallback name
                                if (entry.getUserName() == null || entry.getUserName().isEmpty()) {
                                    entry.setUserName("User" + entry.getUserId().substring(0, Math.min(6, entry.getUserId().length())));
                                }
                                leaderboardList.add(entry);
                                processedCount[0]++;

                                if (processedCount[0] == totalEntries) {
                                    finishLoadingLeaderboard(swipeTriggered);
                                }
                            }
                        });
                    } else {
                        // Username is fine, just add it
                        leaderboardList.add(entry);
                        processedCount[0]++;

                        if (processedCount[0] == totalEntries) {
                            finishLoadingLeaderboard(swipeTriggered);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (swipeTriggered) {
                    Snackbar.make(findViewById(android.R.id.content), "Failed to refresh: " + error.getMessage(), Snackbar.LENGTH_LONG).show();
                }
                Toast.makeText(LeaderboardActivity.this,
                        "Failed to load leaderboard: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finishLoadingLeaderboard(boolean swipeTriggered) {
        // Sort by score descending
        Collections.sort(leaderboardList, (e1, e2) -> Integer.compare(e2.getScore(), e1.getScore()));

        // Assign ranks
        for (int i = 0; i < leaderboardList.size(); i++) {
            leaderboardList.get(i).setRank(i + 1);
        }

        progressBar.setVisibility(View.GONE);
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (leaderboardList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            mainContentLayout.setVisibility(View.GONE);
            tvYourRank.setText("#--");
            tvYourScore.setText("0");
            if (swipeTriggered) {
                Snackbar.make(findViewById(android.R.id.content), "No rankings yet", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            mainContentLayout.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();

            // Update user's rank and score
            updateUserRank();

            if (swipeTriggered) {
                Snackbar.make(findViewById(android.R.id.content), "Leaderboard updated", Snackbar.LENGTH_SHORT).show();
            }
        }
    }


    private void updateUserRank() {
        if (currentUser == null) {
            tvYourRank.setText("#--");
            tvYourScore.setText("0");
            return;
        }

        // Find user's rank and score from leaderboard
        int rank = -1;
        int userScore = 0;

        for (int i = 0; i < leaderboardList.size(); i++) {
            LeaderboardEntry entry = leaderboardList.get(i);
            if (entry.getUserId() != null && entry.getUserId().equals(currentUser.getUid())) {
                rank = i + 1;
                userScore = entry.getScore();
                break;
            }
        }

        if (rank > 0) {
            tvYourRank.setText("#" + rank);
            tvYourScore.setText(String.valueOf(userScore));
        } else {
            tvYourRank.setText("#--");
            tvYourScore.setText("0");
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
