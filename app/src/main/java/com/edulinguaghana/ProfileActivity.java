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
    private TextView tvUserName, tvUserEmail, tvUserId, tvProfileStreak, tvTotalLessons, tvBestScore, tvFavoriteLanguage;
    private View userIdSection;
    private MaterialButton btnCopyUserId;
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
        tvUserId = findViewById(R.id.tvUserId);
        userIdSection = findViewById(R.id.userIdSection);
        btnCopyUserId = findViewById(R.id.btnCopyUserId);
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

    /**
     * Ensure the current user's data exists in Firebase Realtime Database
     * This is crucial for friend search and social features to work
     */
    private void ensureUserInDatabase(FirebaseUser user) {
        if (user == null) return;

        com.google.firebase.database.DatabaseReference usersRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users");

        // Check if user exists
        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // User doesn't exist, create entry
                    android.util.Log.d("ProfileActivity", "User not in database, creating entry for: " + user.getUid());

                    java.util.Map<String, Object> userProfile = new java.util.HashMap<>();
                    userProfile.put("uid", user.getUid());
                    userProfile.put("email", user.getEmail());
                    userProfile.put("displayName", user.getDisplayName());
                    userProfile.put("username", user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());
                    userProfile.put("createdAt", System.currentTimeMillis());

                    usersRef.child(user.getUid()).setValue(userProfile)
                        .addOnSuccessListener(aVoid -> {
                            android.util.Log.d("ProfileActivity", "User profile created successfully");
                        })
                        .addOnFailureListener(e -> {
                            android.util.Log.e("ProfileActivity", "Failed to create user profile", e);
                        });
                } else {
                    android.util.Log.d("ProfileActivity", "User already exists in database: " + user.getUid());
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                android.util.Log.e("ProfileActivity", "Failed to check user existence", error.toException());
            }
        });
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
            // Ensure current user's data is in the database
            ensureUserInDatabase(currentUser);

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

            // Display and setup user ID
            if (currentUserId != null && tvUserId != null && userIdSection != null) {
                tvUserId.setText(currentUserId);
                userIdSection.setVisibility(View.VISIBLE);
            }

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

        // Copy User ID button
        if (btnCopyUserId != null) {
            btnCopyUserId.setOnClickListener(v -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("User ID", userId);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "User ID copied! Share it with friends.", Toast.LENGTH_SHORT).show();
                }
            });
        }

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

                if (currentUserId == null) {
                    Toast.makeText(this, "Please sign in to use social features", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show menu with friend options
                showFriendsMenu(currentUserId);
            });
        }

        if (btnChallengeFriend != null) {
            btnChallengeFriend.setOnClickListener(v -> {
                String testOverride = getIntent().getStringExtra("TEST_CURRENT_USER_ID");
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String currentUserId = testOverride != null ? testOverride : (currentUser != null ? currentUser.getUid() : null);

                if (currentUserId == null) {
                    Toast.makeText(this, "Please sign in to use social features", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show menu with challenge options
                showChallengesMenu(currentUserId);
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
        String[] searchOptions = {"Search by Email", "Search by User ID"};

        new AlertDialog.Builder(this)
            .setTitle("Add Friend")
            .setItems(searchOptions, (dialog, which) -> {
                if (which == 0) {
                    showSearchByEmailDialog(currentUserId);
                } else {
                    showSearchByIdDialog(currentUserId);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showSearchByEmailDialog(String currentUserId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Friend by Email");
        final EditText input = new EditText(this);
        input.setHint("Enter email address");
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);
        builder.setPositiveButton("Search", (dialog, which) -> {
            String email = input.getText() != null ? input.getText().toString().trim() : "";
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show();
                return;
            }
            searchUserByEmail(email, currentUserId);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showSearchByIdDialog(String currentUserId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Friend by ID");

        // Add button to show all users for debugging
        builder.setNeutralButton("Show All Users", (dialog, which) -> {
            showAllUsersInDatabase();
        });

        final EditText input = new EditText(this);
        input.setHint("Enter user ID");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Search", (dialog, which) -> {
            String target = input.getText() != null ? input.getText().toString().trim() : "";
            if (target.isEmpty()) {
                Toast.makeText(this, "Please enter a user id", Toast.LENGTH_SHORT).show();
                return;
            }
            if (target.equals(currentUserId)) {
                Toast.makeText(this, "You can't add yourself", Toast.LENGTH_SHORT).show();
                return;
            }
            // Validate user exists before sending request
            validateAndAddFriendById(currentUserId, target);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAllUsersInDatabase() {
        android.util.Log.d("ProfileActivity", "Fetching all users from database...");
        Toast.makeText(this, "Loading users...", Toast.LENGTH_SHORT).show();

        com.google.firebase.database.DatabaseReference usersRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users");

        usersRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                StringBuilder userList = new StringBuilder();
                int count = 0;

                android.util.Log.d("ProfileActivity", "Total users in database: " + snapshot.getChildrenCount());

                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.getKey();
                    String email = child.child("email").getValue(String.class);
                    String displayName = child.child("displayName").getValue(String.class);
                    String username = child.child("username").getValue(String.class);

                    String name = displayName != null ? displayName : (username != null ? username : "No name");

                    userList.append(++count).append(". ").append(name)
                           .append("\n   Email: ").append(email != null ? email : "N/A")
                           .append("\n   UID: ").append(uid).append("\n\n");

                    android.util.Log.d("ProfileActivity", "User #" + count + " - UID: " + uid + ", Email: " + email + ", Name: " + name);
                }

                if (count == 0) {
                    userList.append("No users found in database!\n\nMake sure users are signing up properly.");
                    android.util.Log.e("ProfileActivity", "No users found in database!");
                } else {
                    userList.insert(0, "Users in Database (" + count + "):\n\n");
                }

                // Show dialog with scrollable list
                android.widget.ScrollView scrollView = new android.widget.ScrollView(ProfileActivity.this);
                android.widget.TextView textView = new android.widget.TextView(ProfileActivity.this);
                textView.setText(userList.toString());
                textView.setPadding(40, 40, 40, 40);
                textView.setTextSize(12);
                scrollView.addView(textView);

                new android.app.AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("All Users in Database")
                    .setView(scrollView)
                    .setPositiveButton("OK", null)
                    .show();
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                android.util.Log.e("ProfileActivity", "Failed to fetch users: " + error.getMessage());
                Toast.makeText(ProfileActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void validateAndAddFriendById(String currentUserId, String targetUserId) {
        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();
        android.util.Log.d("ProfileActivity", "Searching for user ID: " + targetUserId);

        // Check if user exists in Firebase
        com.google.firebase.database.DatabaseReference usersRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(targetUserId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                android.util.Log.d("ProfileActivity", "Search result - exists: " + snapshot.exists());

                if (!snapshot.exists()) {
                    android.util.Log.e("ProfileActivity", "User not found in database: " + targetUserId);

                    // Show more helpful error message
                    new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("User Not Found")
                        .setMessage("The user with ID:\n" + targetUserId +
                                  "\n\ndoes not exist in the database.\n\n" +
                                  "Possible reasons:\n" +
                                  "• The user hasn't signed up yet\n" +
                                  "• The user ID is incorrect\n" +
                                  "• The user signed up but their data wasn't saved\n\n" +
                                  "Tip: Use the 'Show All Users' button to see available users.")
                        .setPositiveButton("OK", null)
                        .setNeutralButton("Show All Users", (d, w) -> showAllUsersInDatabase())
                        .show();
                    return;
                }

                // Log all available fields
                android.util.Log.d("ProfileActivity", "User data: " + snapshot.getValue());

                String displayName = snapshot.child("displayName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String username = snapshot.child("username").getValue(String.class);

                android.util.Log.d("ProfileActivity", "displayName: " + displayName + ", email: " + email + ", username: " + username);

                // Use displayName, username, or email as fallback
                String name = displayName != null ? displayName : (username != null ? username : "Unknown");

                // Show confirmation dialog
                showAddFriendConfirmation(currentUserId, targetUserId, name, email != null ? email : "Unknown");
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                android.util.Log.e("ProfileActivity", "Search error: " + error.getMessage());
                Toast.makeText(ProfileActivity.this, "Search failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchUserByEmail(String email, String currentUserId) {
        // Show progress
        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();
        android.util.Log.d("ProfileActivity", "Searching for email: " + email);

        // Search in Firebase users by email
        com.google.firebase.database.DatabaseReference usersRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    android.util.Log.d("ProfileActivity", "Email search result - exists: " + snapshot.exists() + ", count: " + snapshot.getChildrenCount());

                    if (!snapshot.exists()) {
                        android.util.Log.e("ProfileActivity", "No user found with email: " + email);

                        // Show more helpful error message
                        new AlertDialog.Builder(ProfileActivity.this)
                            .setTitle("User Not Found")
                            .setMessage("No user found with email:\n" + email +
                                      "\n\nPossible reasons:\n" +
                                      "• The user hasn't signed up yet\n" +
                                      "• The email address is incorrect\n" +
                                      "• The email is not indexed in database\n\n" +
                                      "Tip: Use the 'Show All Users' button to see available users.")
                            .setPositiveButton("OK", null)
                            .setNeutralButton("Show All Users", (d, w) -> showAllUsersInDatabase())
                            .show();
                        return;
                    }

                    // Get the first matching user
                    for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                        String foundUserId = child.getKey();
                        android.util.Log.d("ProfileActivity", "Found user ID: " + foundUserId);
                        android.util.Log.d("ProfileActivity", "User data: " + child.getValue());

                        String displayName = child.child("displayName").getValue(String.class);
                        String username = child.child("username").getValue(String.class);

                        android.util.Log.d("ProfileActivity", "displayName: " + displayName + ", username: " + username);

                        if (foundUserId != null) {
                            if (foundUserId.equals(currentUserId)) {
                                Toast.makeText(ProfileActivity.this, "That's your own email!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Use displayName, username, or email as fallback
                            String name = displayName != null ? displayName : (username != null ? username : "Unknown");

                            // Show confirmation dialog
                            showAddFriendConfirmation(currentUserId, foundUserId, name, email);
                            return;
                        }
                    }
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    android.util.Log.e("ProfileActivity", "Email search error: " + error.getMessage());
                    Toast.makeText(ProfileActivity.this, "Search failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showAddFriendConfirmation(String currentUserId, String targetUserId, String displayName, String email) {
        String message = "Send friend request to:\n\n" +
                        "Name: " + (displayName != null ? displayName : "Unknown") + "\n" +
                        "Email: " + email;

        new AlertDialog.Builder(this)
            .setTitle("Confirm Friend Request")
            .setMessage(message)
            .setPositiveButton("Send Request", (dialog, which) -> {
                performSendFriendRequest(currentUserId, targetUserId);
            })
            .setNegativeButton("Cancel", null)
            .show();
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
        android.util.Log.d("ProfileActivity", "Sending friend request from " + fromUserId + " to " + toUserId);

        SocialRepository repo = SocialProvider.get();
        if (repo == null) {
            android.util.Log.e("ProfileActivity", "SocialRepository is null!");
            Toast.makeText(this, "Social features unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // First validate that the target user exists in Firebase
        Toast.makeText(this, "Validating user...", Toast.LENGTH_SHORT).show();

        com.google.firebase.database.DatabaseReference usersRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(toUserId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                android.util.Log.d("ProfileActivity", "Validation check - user exists: " + snapshot.exists());

                if (!snapshot.exists()) {
                    android.util.Log.e("ProfileActivity", "User not found: " + toUserId);
                    Toast.makeText(ProfileActivity.this, "User not found: " + toUserId, Toast.LENGTH_LONG).show();
                    return;
                }

                // User exists, proceed with friend request
                try {
                    android.util.Log.d("ProfileActivity", "Creating FriendService and sending request...");
                    FriendService svc = new FriendService(repo);
                    Friend result = svc.sendFriendRequest(fromUserId, toUserId);
                    android.util.Log.d("ProfileActivity", "Friend request created: " + (result != null ? result.id : "null"));
                    Toast.makeText(ProfileActivity.this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    android.util.Log.e("ProfileActivity", "Error sending friend request", ex);
                    Toast.makeText(ProfileActivity.this, "Failed to send request: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                android.util.Log.e("ProfileActivity", "Validation error: " + error.getMessage());
                Toast.makeText(ProfileActivity.this, "Failed to validate user: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performCreateChallenge(String fromUserId, String toUserId) {
        SocialRepository repo = SocialProvider.get();
        if (repo == null) {
            Toast.makeText(this, "Social features unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // First validate that the target user exists in Firebase
        Toast.makeText(this, "Validating user...", Toast.LENGTH_SHORT).show();

        com.google.firebase.database.DatabaseReference usersRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(toUserId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(ProfileActivity.this, "User not found: " + toUserId, Toast.LENGTH_LONG).show();
                    return;
                }

                // User exists, proceed with challenge creation
                com.edulinguaghana.social.Challenge ch = new com.edulinguaghana.social.Challenge();
                ch.quizId = "default_quiz";
                ch.challengerId = fromUserId;
                ch.challengedId = toUserId;
                try {
                    repo.createChallenge(ch);
                    Toast.makeText(ProfileActivity.this, "Challenge sent!", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Toast.makeText(ProfileActivity.this, "Failed to send challenge: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to validate user: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFriendsMenu(String userId) {
        String[] options = {"👥 View Friends", "📬 View Requests", "➕ Add Friend"};

        new AlertDialog.Builder(this)
            .setTitle("Friends")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showFriendsList(userId);
                        break;
                    case 1:
                        showFriendRequests(userId);
                        break;
                    case 2:
                        presentAddFriendDialog(userId);
                        break;
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showFriendsList(String userId) {
        SocialRepository repo = SocialProvider.get();
        if (repo == null) {
            Toast.makeText(this, "Social features unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use Firebase listener to get accepted friends
        com.google.firebase.database.DatabaseReference friendsRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("friends");

        friendsRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    java.util.List<Friend> acceptedFriends = new java.util.ArrayList<>();
                    for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                        Friend friend = child.getValue(Friend.class);
                        if (friend != null && friend.status == Friend.Status.ACCEPTED) {
                            acceptedFriends.add(friend);
                        }
                    }

                    if (acceptedFriends.isEmpty()) {
                        Toast.makeText(ProfileActivity.this, "No friends yet. Add some!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Fetch usernames for each friend
                    fetchUsernamesForFriends(acceptedFriends, userId);
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load friends", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void fetchUsernamesForFriends(java.util.List<Friend> friends, String currentUserId) {
        if (friends.isEmpty()) {
            showFriendsListDialog(friends, currentUserId);
            return;
        }

        com.google.firebase.database.DatabaseReference usersRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users");

        final int[] fetchedCount = {0};
        final int totalCount = friends.size();

        for (Friend friend : friends) {
            usersRef.child(friend.friendUserId).addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String displayName = snapshot.child("displayName").getValue(String.class);
                            if (displayName != null && !displayName.isEmpty()) {
                                friend.displayName = displayName;
                            } else {
                                // Fallback to email or UID
                                String email = snapshot.child("email").getValue(String.class);
                                friend.displayName = email != null ? email : friend.friendUserId;
                            }
                        } else {
                            friend.displayName = friend.friendUserId; // Fallback to UID
                        }

                        fetchedCount[0]++;
                        if (fetchedCount[0] == totalCount) {
                            // All usernames fetched, show the dialog
                            showFriendsListDialog(friends, currentUserId);
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        friend.displayName = friend.friendUserId; // Fallback to UID on error
                        fetchedCount[0]++;
                        if (fetchedCount[0] == totalCount) {
                            showFriendsListDialog(friends, currentUserId);
                        }
                    }
                }
            );
        }
    }

    private void showFriendsListDialog(java.util.List<Friend> friends, String currentUserId) {
        String[] friendItems = new String[friends.size()];
        for (int i = 0; i < friends.size(); i++) {
            Friend friend = friends.get(i);
            String displayName = friend.displayName != null && !friend.displayName.isEmpty()
                ? friend.displayName
                : friend.friendUserId;
            friendItems[i] = "👤 " + displayName;
        }

        new AlertDialog.Builder(this)
            .setTitle("My Friends (" + friends.size() + ")")
            .setItems(friendItems, (dialog, which) -> {
                Friend selectedFriend = friends.get(which);
                showFriendOptionsDialog(selectedFriend, currentUserId);
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void showFriendOptionsDialog(Friend friend, String currentUserId) {
        String[] options = {"👤 View Profile", "⚔️ Challenge", "❌ Remove Friend"};
        String displayName = friend.displayName != null && !friend.displayName.isEmpty()
            ? friend.displayName
            : friend.friendUserId;

        new AlertDialog.Builder(this)
            .setTitle(displayName)
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        // View profile
                        showFriendProfile(friend.friendUserId);
                        break;
                    case 1:
                        // Challenge friend
                        performCreateChallenge(currentUserId, friend.friendUserId);
                        break;
                    case 2:
                        // Remove friend
                        showRemoveFriendConfirmation(friend, currentUserId);
                        break;
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showFriendProfile(String friendUserId) {
        Toast.makeText(this, "Loading profile...", Toast.LENGTH_SHORT).show();

        // Fetch user data from Firebase
        com.google.firebase.database.DatabaseReference userRef =
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("users").child(friendUserId);

        userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(ProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                String displayName = snapshot.child("displayName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                Long createdAt = snapshot.child("createdAt").getValue(Long.class);

                // Get user stats from ProgressManager
                int totalQuizzes = ProgressManager.getTotalQuizzes(ProfileActivity.this);
                int highScore = ProgressManager.getHighScore(ProfileActivity.this);

                // Build profile info
                StringBuilder profileInfo = new StringBuilder();
                profileInfo.append("📧 Email: ").append(email != null ? email : "N/A").append("\n\n");
                profileInfo.append("🆔 User ID: ").append(friendUserId).append("\n\n");

                if (createdAt != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                    profileInfo.append("📅 Joined: ").append(sdf.format(new java.util.Date(createdAt))).append("\n\n");
                }

                profileInfo.append("📊 Stats:\n");
                profileInfo.append("• Total Quizzes: ").append(totalQuizzes).append("\n");
                profileInfo.append("• High Score: ").append(highScore).append("/10");

                new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("👤 " + (displayName != null ? displayName : "User Profile"))
                    .setMessage(profileInfo.toString())
                    .setPositiveButton("Close", null)
                    .setNeutralButton("Challenge", (dialog, which) -> {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            performCreateChallenge(currentUser.getUid(), friendUserId);
                        }
                    })
                    .show();
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRemoveFriendConfirmation(Friend friend, String currentUserId) {
        new AlertDialog.Builder(this)
            .setTitle("Remove Friend?")
            .setMessage("Are you sure you want to remove " + friend.friendUserId + " from your friends?")
            .setPositiveButton("Remove", (dialog, which) -> {
                SocialRepository repo = SocialProvider.get();
                if (repo != null) {
                    try {
                        repo.removeFriend(currentUserId, friend.friendUserId);
                        Toast.makeText(this, "Friend removed", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Toast.makeText(this, "Failed to remove friend", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showFriendRequests(String userId) {
        SocialRepository repo = SocialProvider.get();
        if (repo == null) {
            Toast.makeText(this, "Social features unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use Firebase listener to get friend requests
        com.google.firebase.database.DatabaseReference friendsRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("friends");

        friendsRef.orderByChild("friendUserId").equalTo(userId)
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    java.util.List<Friend> pendingRequests = new java.util.ArrayList<>();
                    for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                        Friend friend = child.getValue(Friend.class);
                        if (friend != null && friend.status == Friend.Status.PENDING) {
                            pendingRequests.add(friend);
                        }
                    }

                    if (pendingRequests.isEmpty()) {
                        Toast.makeText(ProfileActivity.this, "No pending friend requests", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Show dialog with friend requests
                    showFriendRequestsDialog(pendingRequests);
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load friend requests", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showFriendRequestsDialog(java.util.List<Friend> requests) {
        String[] requestItems = new String[requests.size()];
        for (int i = 0; i < requests.size(); i++) {
            Friend friend = requests.get(i);
            requestItems[i] = "Request from: " + friend.userId;
        }

        new AlertDialog.Builder(this)
            .setTitle("Friend Requests (" + requests.size() + ")")
            .setItems(requestItems, (dialog, which) -> {
                Friend selectedRequest = requests.get(which);
                showAcceptRejectDialog(selectedRequest);
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void showAcceptRejectDialog(Friend request) {
        new AlertDialog.Builder(this)
            .setTitle("Friend Request")
            .setMessage("Accept friend request from " + request.userId + "?")
            .setPositiveButton("Accept", (dialog, which) -> {
                SocialRepository repo = SocialProvider.get();
                if (repo != null) {
                    try {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String currentUserId = currentUser != null ? currentUser.getUid() : null;
                        if (currentUserId != null) {
                            repo.acceptFriend(currentUserId, request.userId);
                            Toast.makeText(this, "Friend request accepted!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(this, "Failed to accept request", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Reject", (dialog, which) -> {
                SocialRepository repo = SocialProvider.get();
                if (repo != null) {
                    try {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        String currentUserId = currentUser != null ? currentUser.getUid() : null;
                        if (currentUserId != null) {
                            repo.removeFriend(currentUserId, request.userId);
                            Toast.makeText(this, "Friend request rejected", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(this, "Failed to reject request", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNeutralButton("Cancel", null)
            .show();
    }

    private void showChallengesMenu(String userId) {
        String[] options = {"⚔️ Pending Challenges", "🏆 Completed Challenges", "🎯 Create Challenge"};

        new AlertDialog.Builder(this)
            .setTitle("Challenges")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showChallenges(userId);
                        break;
                    case 1:
                        showCompletedChallenges(userId);
                        break;
                    case 2:
                        presentChallengeDialog(userId);
                        break;
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showChallenges(String userId) {
        SocialRepository repo = SocialProvider.get();
        if (repo == null) {
            Toast.makeText(this, "Social features unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use Firebase listener to get challenges
        com.google.firebase.database.DatabaseReference challengesRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("challenges");

        challengesRef.orderByChild("challengedId").equalTo(userId)
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    java.util.List<com.edulinguaghana.social.Challenge> pendingChallenges = new java.util.ArrayList<>();
                    for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                        com.edulinguaghana.social.Challenge challenge = child.getValue(com.edulinguaghana.social.Challenge.class);
                        if (challenge != null && challenge.state == com.edulinguaghana.social.Challenge.State.PENDING) {
                            pendingChallenges.add(challenge);
                        }
                    }

                    if (pendingChallenges.isEmpty()) {
                        Toast.makeText(ProfileActivity.this, "No pending challenges", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Fetch usernames for challenges
                    fetchUsernamesForChallenges(pendingChallenges);
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load challenges", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void fetchUsernamesForChallenges(java.util.List<com.edulinguaghana.social.Challenge> challenges) {
        if (challenges.isEmpty()) {
            showChallengesDialog(challenges);
            return;
        }

        com.google.firebase.database.DatabaseReference usersRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users");

        final int[] fetchedCount = {0};
        final int totalCount = challenges.size();

        for (com.edulinguaghana.social.Challenge challenge : challenges) {
            usersRef.child(challenge.challengerId).addListenerForSingleValueEvent(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String displayName = snapshot.child("displayName").getValue(String.class);
                            if (displayName != null && !displayName.isEmpty()) {
                                challenge.challengerName = displayName;
                            } else {
                                String email = snapshot.child("email").getValue(String.class);
                                challenge.challengerName = email != null ? email : challenge.challengerId;
                            }
                        } else {
                            challenge.challengerName = challenge.challengerId;
                        }

                        fetchedCount[0]++;
                        if (fetchedCount[0] == totalCount) {
                            showChallengesDialog(challenges);
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        challenge.challengerName = challenge.challengerId;
                        fetchedCount[0]++;
                        if (fetchedCount[0] == totalCount) {
                            showChallengesDialog(challenges);
                        }
                    }
                }
            );
        }
    }

    private void showChallengesDialog(java.util.List<com.edulinguaghana.social.Challenge> challenges) {
        String[] challengeItems = new String[challenges.size()];
        for (int i = 0; i < challenges.size(); i++) {
            com.edulinguaghana.social.Challenge challenge = challenges.get(i);
            String displayName = challenge.challengerName != null && !challenge.challengerName.isEmpty()
                ? challenge.challengerName
                : challenge.challengerId;
            challengeItems[i] = "⚔️ Challenge from: " + displayName;
        }

        new AlertDialog.Builder(this)
            .setTitle("Challenges (" + challenges.size() + ")")
            .setItems(challengeItems, (dialog, which) -> {
                com.edulinguaghana.social.Challenge selectedChallenge = challenges.get(which);
                showChallengeDetailsDialog(selectedChallenge);
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void showCompletedChallenges(String userId) {
        SocialRepository repo = SocialProvider.get();
        if (repo == null) {
            Toast.makeText(this, "Social features unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use Firebase listener to get completed challenges
        com.google.firebase.database.DatabaseReference challengesRef =
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("challenges");

        // Get challenges where user is either challenger or challenged
        challengesRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                java.util.List<com.edulinguaghana.social.Challenge> completedChallenges = new java.util.ArrayList<>();
                for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                    com.edulinguaghana.social.Challenge challenge = child.getValue(com.edulinguaghana.social.Challenge.class);
                    if (challenge != null &&
                        challenge.state == com.edulinguaghana.social.Challenge.State.COMPLETED &&
                        (userId.equals(challenge.challengerId) || userId.equals(challenge.challengedId))) {
                        completedChallenges.add(challenge);
                    }
                }

                if (completedChallenges.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "No completed challenges yet", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show dialog with completed challenges
                showCompletedChallengesDialog(completedChallenges, userId);
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load challenges", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCompletedChallengesDialog(java.util.List<com.edulinguaghana.social.Challenge> challenges, String userId) {
        String[] challengeItems = new String[challenges.size()];
        for (int i = 0; i < challenges.size(); i++) {
            com.edulinguaghana.social.Challenge challenge = challenges.get(i);

            // Get scores
            Integer myScore = challenge.results.get(userId);
            String opponentId = userId.equals(challenge.challengerId) ? challenge.challengedId : challenge.challengerId;
            Integer opponentScore = challenge.results.get(opponentId);

            String result;
            if (myScore != null && opponentScore != null) {
                if (myScore > opponentScore) {
                    result = "🏆 WON";
                } else if (myScore < opponentScore) {
                    result = "❌ LOST";
                } else {
                    result = "🤝 TIE";
                }
                challengeItems[i] = result + " - You: " + myScore + " vs " + opponentScore;
            } else {
                challengeItems[i] = "Waiting for results...";
            }
        }

        new AlertDialog.Builder(this)
            .setTitle("Completed Challenges")
            .setItems(challengeItems, (dialog, which) -> {
                com.edulinguaghana.social.Challenge selectedChallenge = challenges.get(which);
                showChallengeResultDetails(selectedChallenge, userId);
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void showChallengeResultDetails(com.edulinguaghana.social.Challenge challenge, String userId) {
        Integer myScore = challenge.results.get(userId);
        String opponentId = userId.equals(challenge.challengerId) ? challenge.challengedId : challenge.challengerId;
        Integer opponentScore = challenge.results.get(opponentId);

        String result;
        String emoji;
        if (myScore != null && opponentScore != null) {
            if (myScore > opponentScore) {
                result = "You Won! 🎉";
                emoji = "🏆";
            } else if (myScore < opponentScore) {
                result = "You Lost";
                emoji = "💪";
            } else {
                result = "It's a Tie!";
                emoji = "🤝";
            }
        } else {
            result = "Incomplete";
            emoji = "⏳";
        }

        String message = emoji + " " + result + "\n\n" +
                        "Your Score: " + (myScore != null ? myScore : "N/A") + "\n" +
                        "Opponent: " + opponentId + "\n" +
                        "Their Score: " + (opponentScore != null ? opponentScore : "N/A") + "\n" +
                        "Quiz: " + challenge.quizId;

        new AlertDialog.Builder(this)
            .setTitle("Challenge Result")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    private void showChallengeDetailsDialog(com.edulinguaghana.social.Challenge challenge) {
        String message = "Challenger: " + challenge.challengerId + "\n" +
                        "Quiz: " + challenge.quizId + "\n\n" +
                        "Accept this challenge and start the quiz?";

        new AlertDialog.Builder(this)
            .setTitle("⚔️ Challenge Details")
            .setMessage(message)
            .setPositiveButton("Accept & Start", (dialog, which) -> {
                acceptChallengeAndStartQuiz(challenge);
            })
            .setNegativeButton("Decline", (dialog, which) -> {
                declineChallenge(challenge);
            })
            .setNeutralButton("Later", null)
            .show();
    }

    private void acceptChallengeAndStartQuiz(com.edulinguaghana.social.Challenge challenge) {
        SocialRepository repo = SocialProvider.get();
        if (repo != null) {
            try {
                // Update challenge state to ONGOING
                challenge.state = com.edulinguaghana.social.Challenge.State.ONGOING;
                repo.updateChallenge(challenge);

                // Launch quiz with challenge info
                Intent intent = new Intent(ProfileActivity.this, QuizActivity.class);
                intent.putExtra("LANG_CODE", "tw"); // Default to Twi for now
                intent.putExtra("LANG_NAME", "Twi");
                intent.putExtra("QUIZ_TYPE", challenge.quizId);
                intent.putExtra("CHALLENGE_ID", challenge.id);
                intent.putExtra("CHALLENGE_MODE", true);
                startActivity(intent);

                Toast.makeText(this, "Challenge accepted! Good luck! 🎯", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(this, "Failed to accept challenge", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void declineChallenge(com.edulinguaghana.social.Challenge challenge) {
        SocialRepository repo = SocialProvider.get();
        if (repo != null) {
            try {
                challenge.state = com.edulinguaghana.social.Challenge.State.CANCELLED;
                repo.updateChallenge(challenge);
                Toast.makeText(this, "Challenge declined", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(this, "Failed to decline challenge", Toast.LENGTH_SHORT).show();
            }
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
