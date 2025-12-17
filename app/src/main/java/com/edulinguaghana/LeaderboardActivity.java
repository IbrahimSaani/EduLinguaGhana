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
    private ProgressBar progressBar;
    private TextView tvYourRank;
    private TextView tvYourScore;

    private OfflineManager offlineManager;
    private DatabaseReference leaderboardRef;
    private List<LeaderboardEntry> leaderboardList;
    private LeaderboardAdapter adapter;

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
        progressBar = findViewById(R.id.progressBar);
        tvYourRank = findViewById(R.id.tvYourRank);
        tvYourScore = findViewById(R.id.tvYourScore);

        // Initialize managers
        offlineManager = new OfflineManager(this);
        leaderboardRef = FirebaseDatabase.getInstance().getReference("leaderboard");
        leaderboardList = new ArrayList<>();

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

        // Load leaderboard
        loadLeaderboard();
    }

    private void showLoginRequired() {
        leaderboardRecyclerView.setVisibility(View.GONE);
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
    }

    private void loadLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);

        // Query top 100 scores ordered by score
        Query leaderboardQuery = leaderboardRef.orderByChild("score").limitToLast(100);

        leaderboardQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                leaderboardList.clear();

                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    LeaderboardEntry entry = entrySnapshot.getValue(LeaderboardEntry.class);
                    if (entry != null) {
                        leaderboardList.add(entry);
                    }
                }

                // Sort by score descending
                Collections.sort(leaderboardList, (e1, e2) -> Integer.compare(e2.getScore(), e1.getScore()));

                // Assign ranks
                for (int i = 0; i < leaderboardList.size(); i++) {
                    leaderboardList.get(i).setRank(i + 1);
                }

                progressBar.setVisibility(View.GONE);

                if (leaderboardList.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    leaderboardRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    leaderboardRecyclerView.setVisibility(View.VISIBLE);

                    adapter = new LeaderboardAdapter(LeaderboardActivity.this, leaderboardList);
                    leaderboardRecyclerView.setAdapter(adapter);

                    // Update user's rank and score
                    updateUserRank();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LeaderboardActivity.this,
                    "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserRank() {
        int userScore = ProgressManager.getHighScore(this);
        tvYourScore.setText(String.valueOf(userScore));

        // Find user's rank
        int rank = -1;
        for (int i = 0; i < leaderboardList.size(); i++) {
            if (leaderboardList.get(i).getScore() <= userScore) {
                rank = i + 1;
                break;
            }
        }

        if (rank == -1) {
            rank = leaderboardList.size() + 1;
        }

        tvYourRank.setText(String.valueOf(rank));
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

