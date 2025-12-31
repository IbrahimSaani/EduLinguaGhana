package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.core.content.ContextCompat;
import java.util.Calendar;

import com.edulinguaghana.gamification.XPManager;
import com.edulinguaghana.gamification.XPState;
import com.edulinguaghana.social.SocialProvider;
import com.edulinguaghana.social.SocialRepository;
import com.edulinguaghana.social.service.FriendService;
import com.edulinguaghana.social.Friend;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private View notSignedInLayout, signedInLayout;
    private MaterialButton btnGoToLogin, btnManageAccount, btnSignOut, btnEditAvatar;
    private TextView tvUserName, tvUserEmail, tvProfileStreak, tvTotalLessons, tvBestScore, tvFavoriteLanguage;
    // Gamification views
    private TextView tvLevel, tvXpText;
    private ProgressBar pbXp;
    private ImageView ivBadgesPreview;
    private AvatarView profileImage, avatarNotSignedIn;
    private DynamicBackgroundView dynamicBackground;
    private MaterialButton btnAddFriend, btnChallengeFriend;

    private final XPManager.XPListener xpListener = new XPManager.XPListener() {
        @Override
        public void onXpChanged(XPState state) {
            runOnUiThread(() -> updateXpUi(state));
        }

        @Override
        public void onLevelUp(int newLevel) {
            // simple toast for now
            runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Level up! " + newLevel, Toast.LENGTH_SHORT).show());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // Ensure back button is visible: tint with on-primary (use white from colors for contrast)
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(ContextCompat.getColor(this, R.color.white));
            }
        }

        // Settings button opens SettingsActivity
        androidx.appcompat.widget.AppCompatImageButton settingsBtn = findViewById(R.id.btn_profile_settings);
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }

        initViews();
        setupDynamicBackground();
        setupProfile();
        setupListeners();
    }

    private void setupDynamicBackground() {
        if (dynamicBackground == null) return;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int colorStart, colorMid, colorEnd;

        if (hour >= 5 && hour < 11) {
            colorStart = ContextCompat.getColor(this, R.color.bgMorningStart);
            colorMid = ContextCompat.getColor(this, R.color.bgMorningMid);
            colorEnd = ContextCompat.getColor(this, R.color.bgMorningEnd);
        } else if (hour >= 11 && hour < 17) {
            colorStart = ContextCompat.getColor(this, R.color.bgDayStart);
            colorMid = ContextCompat.getColor(this, R.color.bgDayMid);
            colorEnd = ContextCompat.getColor(this, R.color.bgDayEnd);
        } else {
            colorStart = ContextCompat.getColor(this, R.color.bgNightStart);
            colorMid = ContextCompat.getColor(this, R.color.bgNightMid);
            colorEnd = ContextCompat.getColor(this, R.color.bgNightEnd);
        }

        dynamicBackground.setColors(colorStart, colorMid, colorEnd);
    }

    private void initViews() {
        notSignedInLayout = findViewById(R.id.notSignedInLayout);
        signedInLayout = findViewById(R.id.signedInLayout);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        btnManageAccount = findViewById(R.id.btnManageAccount);
        btnSignOut = findViewById(R.id.btnSignOut);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnChallengeFriend = findViewById(R.id.btnChallengeFriend);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvProfileStreak = findViewById(R.id.tvProfileStreak);
        tvTotalLessons = findViewById(R.id.tvTotalLessons);
        tvBestScore = findViewById(R.id.tvBestScore);
        tvFavoriteLanguage = findViewById(R.id.tvFavoriteLanguage);
        profileImage = findViewById(R.id.profileImage);
        avatarNotSignedIn = findViewById(R.id.avatarNotSignedIn);
        dynamicBackground = findViewById(R.id.dynamicBackground);

        // Gamification views
        tvLevel = findViewById(R.id.tv_level);
        pbXp = findViewById(R.id.pb_xp);
        tvXpText = findViewById(R.id.tv_xp_text);
        ivBadgesPreview = findViewById(R.id.iv_badges_preview);
    }

    private void setupProfile() {
        // Determine current user id (respect test override for androidTest)
        String testOverride = getIntent().getStringExtra("TEST_CURRENT_USER_ID");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = testOverride != null ? testOverride : (currentUser != null ? currentUser.getUid() : null);

        String viewedUserId = getIntent().getStringExtra("PROFILE_USER_ID");

        boolean signedIn = currentUserId != null;
        boolean viewingOther = viewedUserId != null && !viewedUserId.equals(currentUserId);

        if (signedIn) {
            // User is signed in
            notSignedInLayout.setVisibility(View.GONE);
            signedInLayout.setVisibility(View.VISIBLE);

            // Load saved avatar config for signed-in user
            AvatarBuilder.AvatarConfig config = AvatarBuilder.loadConfig(this);
            profileImage.setAvatarConfig(config);

            // Display user info
            String displayName = currentUser != null ? currentUser.getDisplayName() : null;
            String email = currentUser != null ? currentUser.getEmail() : null;

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

            // Populate gamification panel
            XPState s = XPManager.getState(this);
            updateXpUi(s);

        } else {
            // User is not signed in
            notSignedInLayout.setVisibility(View.VISIBLE);
            signedInLayout.setVisibility(View.GONE);

            // Use placeholder icon for signed-out state
            avatarNotSignedIn.setImageResource(R.drawable.ic_graduation_cap);
            avatarNotSignedIn.setAvatarConfig(null); // Clear any config if needed
        }

        // Configure visibility of friend/challenge actions:
        // - show when signed in (so user can search for others even on their own profile)
        // - hide when not signed in
        if (btnAddFriend != null) {
            btnAddFriend.setVisibility(signedIn ? View.VISIBLE : View.GONE);
        }
        if (btnChallengeFriend != null) {
            btnChallengeFriend.setVisibility(signedIn ? View.VISIBLE : View.GONE);
        }
    }

    private void updateXpUi(XPState s) {
        if (s == null) return;
        try {
            tvLevel.setText(String.format(getString(R.string.xp_label_level), s.level));
            int levelRequirement = XPManager.xpRequiredForLevel(s.level);
            pbXp.setMax(Math.max(1, levelRequirement));
            pbXp.setProgress(Math.max(0, s.xpIntoLevel));
            tvXpText.setText(String.format(getString(R.string.xp_text_pattern), s.xpIntoLevel, levelRequirement));
        } catch (Exception ignored) {}
    }

    private void setupListeners() {
        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnManageAccount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AccountManagementActivity.class);
            startActivity(intent);
        });

        btnSignOut.setOnClickListener(v -> showSignOutDialog());

        if (btnEditAvatar != null) {
            btnEditAvatar.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, AvatarEditorActivity.class);
                startActivity(intent);
            });
        }

        if (btnAddFriend != null) {
            btnAddFriend.setOnClickListener(v -> {
                String testOverride = getIntent().getStringExtra("TEST_CURRENT_USER_ID");
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String currentUserId = testOverride != null ? testOverride : (currentUser != null ? currentUser.getUid() : null);
                String viewedUserId = getIntent().getStringExtra("PROFILE_USER_ID");

                if (currentUserId == null) {
                    Toast.makeText(this, "Please sign in to use social features", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If viewing another user, keep existing direct flow
                if (viewedUserId != null && !viewedUserId.equals(currentUserId)) {
                    performSendFriendRequest(currentUserId, viewedUserId);
                    return;
                }

                // Otherwise open a small search dialog to enter a user id
                presentAddFriendDialog(currentUserId);
            });
        }

        if (btnChallengeFriend != null) {
            btnChallengeFriend.setOnClickListener(v -> {
                String testOverride = getIntent().getStringExtra("TEST_CURRENT_USER_ID");
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String currentUserId = testOverride != null ? testOverride : (currentUser != null ? currentUser.getUid() : null);
                String viewedUserId = getIntent().getStringExtra("PROFILE_USER_ID");

                if (currentUserId == null) {
                    Toast.makeText(this, "Please sign in to use social features", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (viewedUserId != null && !viewedUserId.equals(currentUserId)) {
                    performCreateChallenge(currentUserId, viewedUserId);
                    return;
                }

                // Otherwise open dialog to choose who to challenge
                presentChallengeDialog(currentUserId);
            });
        }

        // Allow clicking the profile image to edit avatar
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AvatarEditorActivity.class);
            startActivity(intent);
        });
        avatarNotSignedIn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AvatarEditorActivity.class);
            startActivity(intent);
        });
    }

    // Helper: show dialog to find a user id to add as friend
    private void presentAddFriendDialog(String currentUserId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Friend by ID");
        final EditText input = new EditText(this);
        input.setHint("Enter user id (e.g. user123)");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Send Request", (dialog, which) -> {
            String target = input.getText() != null ? input.getText().toString().trim() : "";
            if (target.isEmpty()) {
                Toast.makeText(this, "Please enter a user id", Toast.LENGTH_SHORT).show();
                return;
            }
            if (target.equals(currentUserId)) {
                Toast.makeText(this, "You can't add yourself", Toast.LENGTH_SHORT).show();
                return;
            }
            performSendFriendRequest(currentUserId, target);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Helper: show dialog to choose a user to challenge
    private void presentChallengeDialog(String currentUserId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Challenge a Friend");
        final EditText input = new EditText(this);
        input.setHint("Enter user id to challenge");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Send Challenge", (dialog, which) -> {
            String target = input.getText() != null ? input.getText().toString().trim() : "";
            if (target.isEmpty()) {
                Toast.makeText(this, "Please enter a user id", Toast.LENGTH_SHORT).show();
                return;
            }
            if (target.equals(currentUserId)) {
                Toast.makeText(this, "You can't challenge yourself", Toast.LENGTH_SHORT).show();
                return;
            }
            performCreateChallenge(currentUserId, target);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void performSendFriendRequest(String fromUserId, String toUserId) {
        SocialRepository repo = SocialProvider.get();
        if (repo == null) {
            Toast.makeText(this, "Social features unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            FriendService svc = new FriendService(repo);
            svc.sendFriendRequest(fromUserId, toUserId);
            Toast.makeText(this, "Friend request sent to " + toUserId, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show();
        }
    }

    private void performCreateChallenge(String fromUserId, String toUserId) {
        SocialRepository repo = SocialProvider.get();
        if (repo == null) {
            Toast.makeText(this, "Social features unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        com.edulinguaghana.social.Challenge ch = new com.edulinguaghana.social.Challenge();
        ch.quizId = "default_quiz";
        ch.challengerId = fromUserId;
        ch.challengedId = toUserId;
        try {
            repo.createChallenge(ch);
            Toast.makeText(this, "Challenge sent to " + toUserId, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(this, "Failed to send challenge", Toast.LENGTH_SHORT).show();
        }
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
        XPManager.addListener(xpListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        XPManager.removeListener(xpListener);
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
