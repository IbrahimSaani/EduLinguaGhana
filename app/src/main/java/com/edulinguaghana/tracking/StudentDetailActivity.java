package com.edulinguaghana.tracking;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;

import com.edulinguaghana.R;
import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.roles.UserRole;
import com.edulinguaghana.social.ChallengeManager;
import com.edulinguaghana.social.ChallengeStats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Detailed view of a student's progress with charts and statistics
 */
public class StudentDetailActivity extends AppCompatActivity {

    private static final String TAG = "StudentDetailActivity";

    private String studentId;
    private String studentName;
    private UserRole currentUserRole = UserRole.STUDENT;

    private ProgressBar loadingProgress;
    private CardView statsCard;
    private TextView tvStudentName;
    private TextView tvStudentAge;
    private TextView tvStudentClass;
    private TextView tvLevel;
    private TextView tvTotalXP;
    private TextView tvCurrentStreak;
    private TextView tvLongestStreak;
    private TextView tvTotalQuizzes;
    private TextView tvAccuracy;
    private TextView tvHighScore;
    private TextView tvAverageScore;
    private TextView tvTotalQuestions;
    private TextView tvQuizzesThisWeek;
    private TextView tvXPThisWeek;
    private TextView tvLastActive;
    private TextView tvTotalAchievements;
    private TextView tvTotalBadges;
    private TextView tvDaysActive;
    private TextView tvTimeSpent;
    private TextView tvChallengesWon;
    private TextView tvChallengesLost;

    // Section views for role-based customization
    private View dividerStreaks;
    private View tvLabelStreaks;
    private View layoutStreaks;
    private View dividerQuizStats;
    private View tvLabelQuizPerformance;
    private View layoutQuizStats;
    private View dividerAchievements;
    private View tvLabelAchievements;
    private View layoutAchievements;
    private View dividerEngagement;
    private View tvLabelEngagement;
    private View layoutEngagement;
    private View dividerChallenges;
    private View tvLabelChallenges;
    private View layoutChallenges;

    private ProgressTracker progressTracker;
    private DatabaseReference progressRef;
    private RoleManager roleManager;
    private ChallengeManager challengeManager;
    private boolean initialLoadComplete = false;
    private long lastObservedActivityTimestamp = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressTracker = new ProgressTracker();
        roleManager = new RoleManager();
        challengeManager = new ChallengeManager();
        progressRef = FirebaseDatabase.getInstance().getReference("progress").child(studentId);

        initViews();
        loadCurrentUserRole();
        loadStudentInfo();
        loadProgressData();
        loadChallengeStats();
        setupRealtimeListener();
    }

    private void loadChallengeStats() {
        challengeManager.getChallengeStats(studentId, new ChallengeManager.StatsCallback() {
            @Override
            public void onSuccess(ChallengeStats stats) {
                if (tvChallengesWon != null) {
                    tvChallengesWon.setText(String.valueOf(stats.challengesWon));
                }
                if (tvChallengesLost != null) {
                    tvChallengesLost.setText(String.valueOf(stats.challengesLost));
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to load challenge stats: " + error);
            }
        });
    }

    private void loadCurrentUserRole() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            roleManager.getUserRole(this, uid, new RoleManager.RoleCallback() {
                @Override
                public void onRoleRetrieved(UserRole role) {
                    currentUserRole = role;
                    applyRoleBasedUI();
                    updateTitle();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error loading current user role: " + error);
                }
            });
        }
    }

    private void applyRoleBasedUI() {
        // Differentiate view based on role
        if (currentUserRole == UserRole.PARENT) {
            // Parents focus on engagement and rewards
            if (tvLabelQuizPerformance != null) tvLabelQuizPerformance.setVisibility(View.GONE);
            if (layoutQuizStats != null) layoutQuizStats.setVisibility(View.GONE);
            if (dividerQuizStats != null) dividerQuizStats.setVisibility(View.GONE);

            if (tvLabelEngagement != null) tvLabelEngagement.setVisibility(View.GONE);
            if (layoutEngagement != null) layoutEngagement.setVisibility(View.GONE);
            if (dividerEngagement != null) dividerEngagement.setVisibility(View.GONE);

            if (tvLabelStreaks != null) tvLabelStreaks.setVisibility(View.VISIBLE);
            if (layoutStreaks != null) layoutStreaks.setVisibility(View.VISIBLE);
            if (dividerStreaks != null) dividerStreaks.setVisibility(View.VISIBLE);
            
            if (tvLabelAchievements != null) tvLabelAchievements.setVisibility(View.VISIBLE);
            if (layoutAchievements != null) layoutAchievements.setVisibility(View.VISIBLE);
            if (dividerAchievements != null) dividerAchievements.setVisibility(View.VISIBLE);

            // Challenges visible to both
            if (tvLabelChallenges != null) tvLabelChallenges.setVisibility(View.VISIBLE);
            if (layoutChallenges != null) layoutChallenges.setVisibility(View.VISIBLE);
            if (dividerChallenges != null) dividerChallenges.setVisibility(View.VISIBLE);
        } else if (currentUserRole == UserRole.TEACHER) {
            // Teachers focus on academic performance
            if (tvLabelStreaks != null) tvLabelStreaks.setVisibility(View.GONE);
            if (layoutStreaks != null) layoutStreaks.setVisibility(View.GONE);
            if (dividerStreaks != null) dividerStreaks.setVisibility(View.GONE);

            if (tvLabelAchievements != null) tvLabelAchievements.setVisibility(View.GONE);
            if (layoutAchievements != null) layoutAchievements.setVisibility(View.GONE);
            if (dividerAchievements != null) dividerAchievements.setVisibility(View.GONE);

            if (tvLabelQuizPerformance != null) tvLabelQuizPerformance.setVisibility(View.VISIBLE);
            if (layoutQuizStats != null) layoutQuizStats.setVisibility(View.VISIBLE);
            if (dividerQuizStats != null) dividerQuizStats.setVisibility(View.VISIBLE);

            if (tvLabelEngagement != null) tvLabelEngagement.setVisibility(View.VISIBLE);
            if (layoutEngagement != null) layoutEngagement.setVisibility(View.VISIBLE);
            if (dividerEngagement != null) dividerEngagement.setVisibility(View.VISIBLE);

            // Challenges visible to both, but we explicitly set it here for clarity
            if (tvLabelChallenges != null) tvLabelChallenges.setVisibility(View.VISIBLE);
            if (layoutChallenges != null) layoutChallenges.setVisibility(View.VISIBLE);
            if (dividerChallenges != null) dividerChallenges.setVisibility(View.VISIBLE);
        }
    }

    private void updateTitle() {
        if (getSupportActionBar() != null) {
            String prefix = (currentUserRole == UserRole.PARENT) ? "Child: " : "Student: ";
            getSupportActionBar().setTitle(studentName != null ? prefix + studentName : 
                    (currentUserRole == UserRole.PARENT ? "Child Progress" : "Student Progress"));
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Student Progress");
        }

        loadingProgress = findViewById(R.id.loadingProgress);
        statsCard = findViewById(R.id.statsCard);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentAge = findViewById(R.id.tvStudentAge);
        tvStudentClass = findViewById(R.id.tvStudentClass);
        tvLevel = findViewById(R.id.tvLevel);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak);
        tvLongestStreak = findViewById(R.id.tvLongestStreak);
        tvTotalQuizzes = findViewById(R.id.tvTotalQuizzes);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvHighScore = findViewById(R.id.tvHighScore);
        tvAverageScore = findViewById(R.id.tvAverageScore);
        tvTotalQuestions = findViewById(R.id.tvTotalQuestions);
        tvQuizzesThisWeek = findViewById(R.id.tvQuizzesThisWeek);
        tvXPThisWeek = findViewById(R.id.tvXPThisWeek);
        tvLastActive = findViewById(R.id.tvLastActive);
        tvTotalAchievements = findViewById(R.id.tvTotalAchievements);
        tvTotalBadges = findViewById(R.id.tvTotalBadges);
        tvDaysActive = findViewById(R.id.tvDaysActive);
        tvTimeSpent = findViewById(R.id.tvTimeSpent);
        tvChallengesWon = findViewById(R.id.tvChallengesWon);
        tvChallengesLost = findViewById(R.id.tvChallengesLost);

        dividerStreaks = findViewById(R.id.dividerStreaks);
        tvLabelStreaks = findViewById(R.id.tvLabelStreaks);
        layoutStreaks = findViewById(R.id.layoutStreaks);
        dividerQuizStats = findViewById(R.id.dividerQuizStats);
        tvLabelQuizPerformance = findViewById(R.id.tvLabelQuizPerformance);
        layoutQuizStats = findViewById(R.id.layoutQuizStats);
        dividerAchievements = findViewById(R.id.dividerAchievements);
        tvLabelAchievements = findViewById(R.id.tvLabelAchievements);
        layoutAchievements = findViewById(R.id.layoutAchievements);
        dividerEngagement = findViewById(R.id.dividerEngagement);
        tvLabelEngagement = findViewById(R.id.tvLabelEngagement);
        layoutEngagement = findViewById(R.id.layoutEngagement);
        dividerChallenges = findViewById(R.id.dividerChallenges);
        tvLabelChallenges = findViewById(R.id.tvLabelChallenges);
        layoutChallenges = findViewById(R.id.layoutChallenges);
    }

    private void loadStudentInfo() {
        // Load student name from users table
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    studentName = snapshot.child("displayName").getValue(String.class);
                    if (studentName == null) {
                        studentName = snapshot.child("username").getValue(String.class);
                    }
                    if (studentName == null) {
                        studentName = snapshot.child("email").getValue(String.class);
                    }

                    String age = snapshot.child("age").getValue(String.class);
                    String studentClass = snapshot.child("studentClass").getValue(String.class);

                    if (tvStudentName != null) {
                        tvStudentName.setText(studentName != null ? studentName : "Student");
                    }

                    if (tvStudentAge != null) {
                        tvStudentAge.setText(!isEmptyValue(age) ? age : "Not set");
                    }

                    if (tvStudentClass != null) {
                        tvStudentClass.setText(!isEmptyValue(studentClass) ? studentClass : "Not set");
                    }

                    updateTitle();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Use default name
                tvStudentName.setText("Student");
            }
        });
    }

    private boolean isEmptyValue(String value) {
        return value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim());
    }

    private void loadProgressData() {
        initialLoadComplete = false;
        loadingProgress.setVisibility(View.VISIBLE);
        statsCard.setVisibility(View.GONE);

        Log.d(TAG, "Loading progress for student: " + studentId);

        progressTracker.getProgressAggregate(studentId, new ProgressTracker.ProgressAggregateCallback() {
            @Override
            public void onAggregateRetrieved(ProgressAggregate aggregate) {
                loadingProgress.setVisibility(View.GONE);
                initialLoadComplete = true;
                statsCard.setVisibility(View.VISIBLE);
                Log.d(TAG, "Progress loaded successfully");
                displayProgress(aggregate);
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                initialLoadComplete = true;
                Log.e(TAG, "Failed to load progress: " + error);

                String errorMessage = "Error loading progress";
                if (error != null && error.contains("Permission denied")) {
                    errorMessage = getString(R.string.student_detail_permission_denied);
                } else if (error != null) {
                    errorMessage = getString(R.string.student_detail_error_loading_progress, error);
                }

                Toast.makeText(StudentDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayProgress(ProgressAggregate aggregate) {
        tvLevel.setText("Level " + aggregate.getCurrentLevel());
        tvTotalXP.setText(aggregate.getTotalXP() + " XP");
        tvCurrentStreak.setText(aggregate.getCurrentStreak() + " days");
        tvLongestStreak.setText(aggregate.getLongestStreak() + " days");
        tvTotalQuizzes.setText(String.valueOf(aggregate.getTotalQuizzes()));
        tvAccuracy.setText(String.format(Locale.getDefault(), "%.1f%%", aggregate.getAccuracy()));
        tvHighScore.setText(String.valueOf(aggregate.getHighestScore()));
        tvAverageScore.setText(String.format(Locale.getDefault(), "%.1f", aggregate.getAverageScore()));
        tvTotalQuestions.setText(String.valueOf(aggregate.getTotalQuestions()));
        tvQuizzesThisWeek.setText(String.valueOf(aggregate.getQuizzesThisWeek()));
        tvXPThisWeek.setText(String.valueOf(aggregate.getXpThisWeek()));
        tvTotalAchievements.setText(String.valueOf(aggregate.getTotalAchievements()));
        tvTotalBadges.setText(String.valueOf(aggregate.getTotalBadges()));
        tvDaysActive.setText(String.valueOf(aggregate.getDaysActive()));

        // Format time spent (convert seconds to minutes)
        long minutes = aggregate.getTotalTimeSpentSeconds() / 60;
        if (minutes < 60) {
            tvTimeSpent.setText(minutes + "m");
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            tvTimeSpent.setText(hours + "h " + remainingMinutes + "m");
        }

        // Format last active time
        if (aggregate.getLastUpdated() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
            String formattedDate = sdf.format(new Date(aggregate.getLastUpdated()));
            tvLastActive.setText(formattedDate);
        } else {
            tvLastActive.setText("Not active yet");
        }

        if (statsCard != null) {
            statsCard.setAlpha(0f);
            statsCard.setScaleX(0.98f);
            statsCard.setScaleY(0.98f);
            statsCard.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(220)
                    .start();
        }
    }

    /**
     * Setup real-time listener for live updates
     */
    private void setupRealtimeListener() {
        progressRef.child("activities").orderByChild("timestamp")
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) return;

                        long latestTimestamp = extractLatestTimestamp(snapshot);
                        if (latestTimestamp <= 0 || latestTimestamp == lastObservedActivityTimestamp) {
                            return;
                        }

                        lastObservedActivityTimestamp = latestTimestamp;

                        if (initialLoadComplete) {
                            // Reload progress only when a newer activity appears
                            loadProgressData();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Silently fail - not critical
                    }
                });
    }

    private long extractLatestTimestamp(DataSnapshot snapshot) {
        long latestTimestamp = -1L;
        for (DataSnapshot child : snapshot.getChildren()) {
            Object value = child.child("timestamp").getValue();
            if (value instanceof Number) {
                latestTimestamp = Math.max(latestTimestamp, ((Number) value).longValue());
            }
        }
        return latestTimestamp;
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}

