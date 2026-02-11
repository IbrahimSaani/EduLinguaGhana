package com.edulinguaghana.tracking;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.edulinguaghana.R;
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

    private String studentId;
    private String studentName;

    private ProgressBar loadingProgress;
    private CardView statsCard;
    private TextView tvStudentName;
    private TextView tvLevel;
    private TextView tvTotalXP;
    private TextView tvCurrentStreak;
    private TextView tvLongestStreak;
    private TextView tvTotalQuizzes;
    private TextView tvAccuracy;
    private TextView tvHighScore;
    private TextView tvQuizzesThisWeek;
    private TextView tvXPThisWeek;
    private TextView tvLastActive;
    private TextView tvTotalAchievements;
    private TextView tvTotalBadges;

    private ProgressTracker progressTracker;
    private DatabaseReference progressRef;

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
        progressRef = FirebaseDatabase.getInstance().getReference("progress").child(studentId);

        initViews();
        loadStudentInfo();
        loadProgressData();
        setupRealtimeListener();
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
        tvLevel = findViewById(R.id.tvLevel);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak);
        tvLongestStreak = findViewById(R.id.tvLongestStreak);
        tvTotalQuizzes = findViewById(R.id.tvTotalQuizzes);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvHighScore = findViewById(R.id.tvHighScore);
        tvQuizzesThisWeek = findViewById(R.id.tvQuizzesThisWeek);
        tvXPThisWeek = findViewById(R.id.tvXPThisWeek);
        tvLastActive = findViewById(R.id.tvLastActive);
        tvTotalAchievements = findViewById(R.id.tvTotalAchievements);
        tvTotalBadges = findViewById(R.id.tvTotalBadges);
    }

    private void loadStudentInfo() {
        // Load student name from users table
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    studentName = snapshot.child("displayName").getValue(String.class);
                    if (studentName == null) {
                        studentName = snapshot.child("username").getValue(String.class);
                    }
                    if (studentName == null) {
                        studentName = snapshot.child("email").getValue(String.class);
                    }

                    if (tvStudentName != null) {
                        tvStudentName.setText(studentName != null ? studentName : "Student");
                    }

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(studentName != null ? studentName : "Student Progress");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Use default name
                tvStudentName.setText("Student");
            }
        });
    }

    private void loadProgressData() {
        loadingProgress.setVisibility(View.VISIBLE);
        statsCard.setVisibility(View.GONE);

        progressTracker.getProgressAggregate(studentId, new ProgressTracker.ProgressAggregateCallback() {
            @Override
            public void onAggregateRetrieved(ProgressAggregate aggregate) {
                loadingProgress.setVisibility(View.GONE);
                statsCard.setVisibility(View.VISIBLE);
                displayProgress(aggregate);
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(StudentDetailActivity.this,
                             "Error loading progress: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayProgress(ProgressAggregate aggregate) {
        tvLevel.setText("Level " + aggregate.getCurrentLevel());
        tvTotalXP.setText(aggregate.getTotalXP() + " XP");
        tvCurrentStreak.setText(aggregate.getCurrentStreak() + " days");
        tvLongestStreak.setText(aggregate.getLongestStreak() + " days");
        tvTotalQuizzes.setText(String.valueOf(aggregate.getTotalQuizzes()));
        tvAccuracy.setText(String.format("%.1f%%", aggregate.getAccuracy()));
        tvHighScore.setText(String.valueOf(aggregate.getHighestScore()));
        tvQuizzesThisWeek.setText(String.valueOf(aggregate.getQuizzesThisWeek()));
        tvXPThisWeek.setText(String.valueOf(aggregate.getXpThisWeek()));
        tvTotalAchievements.setText(String.valueOf(aggregate.getTotalAchievements()));
        tvTotalBadges.setText(String.valueOf(aggregate.getTotalBadges()));

        // Format last active time
        if (aggregate.getLastUpdated() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(new Date(aggregate.getLastUpdated()));
            tvLastActive.setText(formattedDate);
        } else {
            tvLastActive.setText("Not active yet");
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
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Reload progress when new activity is detected
                            loadProgressData();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Silently fail - not critical
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}

