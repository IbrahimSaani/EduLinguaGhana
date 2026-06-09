package com.edulinguaghana;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private TextView tvYourNameHero;
    private AvatarView ivYourAvatarHero;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;

    // Podium views
    private View podiumSection;
    private AvatarView avatar1st, avatar2nd, avatar3rd;
    private TextView name1st, name2nd, name3rd;
    private TextView score1st, score2nd, score3rd;

    // Sticky footer views
    private View userRankStickyFooter;
    private TextView tvFooterRank, tvFooterName, tvFooterScore;
    private AvatarView ivFooterAvatar;

    private OfflineManager offlineManager;
    private DatabaseReference leaderboardRef;
    private List<LeaderboardEntry> leaderboardList;
    private LeaderboardAdapter adapter;
    private FirebaseUser currentUser;
    private MediaPlayer sfxPlayer;

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
        tvYourNameHero = findViewById(R.id.tvYourNameHero);
        ivYourAvatarHero = findViewById(R.id.ivYourAvatarHero);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Podium views
        podiumSection = findViewById(R.id.podiumSection);
        avatar1st = findViewById(R.id.avatar1st);
        avatar2nd = findViewById(R.id.avatar2nd);
        avatar3rd = findViewById(R.id.avatar3rd);
        name1st = findViewById(R.id.name1st);
        name2nd = findViewById(R.id.name2nd);
        name3rd = findViewById(R.id.name3rd);
        score1st = findViewById(R.id.score1st);
        score2nd = findViewById(R.id.score2nd);
        score3rd = findViewById(R.id.score3rd);

        // Sticky footer
        userRankStickyFooter = findViewById(R.id.userRankStickyFooter);
        tvFooterRank = findViewById(R.id.tvFooterRank);
        tvFooterName = findViewById(R.id.tvFooterName);
        tvFooterScore = findViewById(R.id.tvFooterScore);
        ivFooterAvatar = findViewById(R.id.ivFooterAvatar);

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
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "📶",
            "Internet Required",
            "Leaderboard requires an internet connection. Please connect and try again.",
            "OK",
            null,
            this::finish,
            null
        );
    }

    private void setupRecyclerView() {
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecyclerView.setHasFixedSize(true);
        adapter = new LeaderboardAdapter(this, leaderboardList);
        leaderboardRecyclerView.setAdapter(adapter);
    }

    private void loadLeaderboard() {
        // Detect whether this load was triggered by swipe-to-refresh
        final boolean swipeTriggered = (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing());

        if (!swipeTriggered) {
            progressBar.setVisibility(View.VISIBLE);
            mainContentLayout.setVisibility(View.GONE);
        }

        // Query top 100 scores ordered by score
        // Use a faster approach: just read the leaderboard data directly.
        // The data should already contain usernames and avatars thanks to CloudSyncManager improvements.
        leaderboardRef.orderByChild("score").limitToLast(100)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    leaderboardList.clear();

                    if (!snapshot.exists()) {
                        progressBar.setVisibility(View.GONE);
                        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        mainContentLayout.setVisibility(View.GONE);
                        return;
                    }

                    // Process entries immediately - much faster than re-fetching every user
                    for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                        LeaderboardEntry entry = entrySnapshot.getValue(LeaderboardEntry.class);
                        if (entry != null) {
                            // Check for legacy "avatar" key if "avatarData" is missing
                            if (entry.getAvatarData() == null && entrySnapshot.hasChild("avatar")) {
                                entry.setAvatarData((java.util.Map<String, Object>) entrySnapshot.child("avatar").getValue());
                            }
                            
                            // Basic sanitization: if username is UID-like, try to make it look better
                            String name = entry.getUserName();
                            if (name == null || name.isEmpty() || name.equals(entry.getUserId())) {
                                entry.setUserName("Learner " + entry.getUserId().substring(0, Math.min(5, entry.getUserId().length())));
                            }
                            
                            leaderboardList.add(entry);
                        }
                    }

                    // Sort and update UI
                    finishLoadingLeaderboard(swipeTriggered);
                    
                    // Optional: Re-fetch ONLY the current user's info to ensure it's up to date in the Hero section
                    if (currentUser != null) {
                        FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    if (userSnapshot.exists()) {
                                        updateCurrentUserInList(userSnapshot);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Toast.makeText(LeaderboardActivity.this, "Load failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void updateCurrentUserInList(DataSnapshot userSnapshot) {
        String uid = userSnapshot.getKey();
        if (uid == null) return;

        for (LeaderboardEntry entry : leaderboardList) {
            if (uid.equals(entry.getUserId())) {
                String displayName = userSnapshot.child("displayName").getValue(String.class);
                if (displayName != null && !displayName.isEmpty()) {
                    entry.setUserName(displayName);
                }
                
                if (userSnapshot.child("avatar").exists()) {
                    entry.setAvatarData((java.util.Map<String, Object>) userSnapshot.child("avatar").getValue());
                } else if (userSnapshot.child("avatarData").exists()) {
                    entry.setAvatarData((java.util.Map<String, Object>) userSnapshot.child("avatarData").getValue());
                }
                
                // Refresh UI
                adapter.notifyDataSetChanged();
                updateUserRank(); // Refresh Hero section
                break;
            }
        }
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
            animateEmptyState();
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

            // Handle Podium
            setupPodium();

            // Create a sublist for the RecyclerView starting from index 3 (rank 4 onwards)
            List<LeaderboardEntry> listForAdapter = new ArrayList<>();
            if (leaderboardList.size() > 3) {
                listForAdapter.addAll(leaderboardList.subList(3, leaderboardList.size()));
            }
            
            adapter.updateList(listForAdapter);

            // Animate the leaderboard content entrance
            animateLeaderboardEntrance();

            // Update user's rank and score with celebration
            updateUserRank();

            if (swipeTriggered) {
                Snackbar.make(findViewById(android.R.id.content), "Leaderboard updated! 🏆", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void setupPodium() {
        if (!leaderboardList.isEmpty()) {
            podiumSection.setVisibility(View.VISIBLE);
            
            // 1st Place
            LeaderboardEntry first = leaderboardList.get(0);
            name1st.setText(first.getUserName());
            score1st.setText(first.getScore() + " pts");
            if (first.getAvatarData() != null) {
                avatar1st.setAvatarConfig(AvatarBuilder.AvatarConfig.fromMap(first.getAvatarData()));
            } else {
                avatar1st.setAvatarConfig(new AvatarBuilder.AvatarConfig());
            }

            // 2nd Place
            if (leaderboardList.size() >= 2) {
                LeaderboardEntry second = leaderboardList.get(1);
                name2nd.setText(second.getUserName());
                score2nd.setText(second.getScore() + " pts");
                if (second.getAvatarData() != null) {
                    avatar2nd.setAvatarConfig(AvatarBuilder.AvatarConfig.fromMap(second.getAvatarData()));
                } else {
                    avatar2nd.setAvatarConfig(new AvatarBuilder.AvatarConfig());
                }
            }

            // 3rd Place
            if (leaderboardList.size() >= 3) {
                LeaderboardEntry third = leaderboardList.get(2);
                name3rd.setText(third.getUserName());
                score3rd.setText(third.getScore() + " pts");
                if (third.getAvatarData() != null) {
                    avatar3rd.setAvatarConfig(AvatarBuilder.AvatarConfig.fromMap(third.getAvatarData()));
                } else {
                    avatar3rd.setAvatarConfig(new AvatarBuilder.AvatarConfig());
                }
            }
        } else {
            podiumSection.setVisibility(View.GONE);
        }
    }


    private void updateUserRank() {
        if (currentUser == null) {
            tvYourRank.setText("#--");
            tvYourScore.setText("0");
            userRankStickyFooter.setVisibility(View.GONE);
            return;
        }

        // Find user's rank and score from leaderboard
        int rank = -1;
        LeaderboardEntry userEntry = null;

        for (int i = 0; i < leaderboardList.size(); i++) {
            LeaderboardEntry entry = leaderboardList.get(i);
            if (entry.getUserId() != null && entry.getUserId().equals(currentUser.getUid())) {
                rank = i + 1;
                userEntry = entry;
                break;
            }
        }

        if (rank > 0) {
            tvYourRank.setText("#" + rank);
            tvYourScore.setText(String.valueOf(userEntry.getScore()));
            tvYourNameHero.setText(userEntry.getUserName());
            if (userEntry.getAvatarData() != null) {
                ivYourAvatarHero.setAvatarConfig(AvatarBuilder.AvatarConfig.fromMap(userEntry.getAvatarData()));
            } else {
                ivYourAvatarHero.setAvatarConfig(new AvatarBuilder.AvatarConfig());
            }

            // Handle Sticky Footer
            // Show footer if user is not in top 3 (already shown in podium)
            if (rank > 3) {
                userRankStickyFooter.setVisibility(View.VISIBLE);
                tvFooterRank.setText("#" + rank);
                tvFooterName.setText(userEntry.getUserName());
                tvFooterScore.setText(userEntry.getScore() + " pts");
                if (userEntry.getAvatarData() != null) {
                    ivFooterAvatar.setAvatarConfig(AvatarBuilder.AvatarConfig.fromMap(userEntry.getAvatarData()));
                } else {
                    ivFooterAvatar.setAvatarConfig(new AvatarBuilder.AvatarConfig());
                }
            } else {
                userRankStickyFooter.setVisibility(View.GONE);
            }

            // Animate rank update with celebration
            animateRankUpdate(rank);

            // Play celebratory sound if top 10
            if (rank <= 10) {
                playRankCelebration();
            }
        } else {
            tvYourRank.setText("#--");
            tvYourScore.setText("0");
            userRankStickyFooter.setVisibility(View.GONE);
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

    // ========== ANIMATION METHODS ==========

    private void animateEmptyState() {
        try {
            LinearLayout emptyLayout = findViewById(R.id.emptyStateLayout);
            if (emptyLayout != null) {
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                emptyLayout.startAnimation(fadeIn);
            }
        } catch (Exception ignored) {}
    }

    private void animateLeaderboardEntrance() {
        try {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
            mainContentLayout.startAnimation(slideUp);
        } catch (Exception ignored) {}
    }

    private void animateRankUpdate(int rank) {
        try {
            // Rainbow shine effect for user's rank card
            Animation shine = AnimationUtils.loadAnimation(this, R.anim.rainbow_shine);
            tvYourRank.startAnimation(shine);

            // Bounce pop for score
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_pop);
            tvYourScore.startAnimation(bounce);
        } catch (Exception ignored) {}
    }

    private void playRankCelebration() {
        try {
            // Screen shake for top 10 celebration
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.screen_shake);
            View root = findViewById(android.R.id.content);
            if (root != null) {
                root.startAnimation(shake);
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sfxPlayer != null) {
            sfxPlayer.release();
            sfxPlayer = null;
        }
    }
}
