package com.edulinguaghana;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.content.res.ColorStateList;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.edulinguaghana.roles.RoleManager;
import com.edulinguaghana.gamification.FunGameProgressManager;
import com.edulinguaghana.roles.UserRole;
import com.edulinguaghana.tracking.TeacherDashboardActivity;
import com.edulinguaghana.tracking.ParentDashboardActivity;
import com.edulinguaghana.tracking.RelationshipManagementActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout rootCoordinator;
    private ChipGroup languageChipGroup;
    private MaterialCardView btnRecitalMode, btnGameMode, btnQuizMode, btnProgressMode;
    private MaterialCardView heroCard;
    private LottieAnimationView mascotView;
    private NestedScrollView nestedScrollView;
    private ObjectAnimator overlayPulseAnimator;
    private Animator heroGlowAnimator;
    private ImageView starTopLeft, starTopRight;
    private Animator starLeftAnimator;
    private Animator starRightAnimator;
    private ImageView bubbleTop, bubbleMidRight, bubbleBottomLeft;
    private Animator bubbleTopAnimator, bubbleMidAnimator, bubbleBottomAnimator;
    private android.view.ViewGroup floatingElementsContainer;
    private android.view.ViewGroup animatedShapesContainer;
    private android.widget.TextView tvMotivationMessage;
    private android.widget.TextView tvStreakCount;
    private android.widget.TextView tvFunFact;
    private android.widget.TextView tvGreeting;
    private android.widget.TextView tvTotalQuizzes;
    private android.widget.TextView tvTotalGames;
    private android.widget.TextView tvAchievements;
    private View offlineBanner;
    private BottomNavigationView bottomNavigation;
    private com.edulinguaghana.social.NotificationPermissionHelper permissionHelper;
    private FloatingActionButton fabRoleDashboard;
    private MaterialCardView roleDashboardCard;
    private android.widget.TextView tvRoleTitle, tvRoleSubtitle;
    private com.google.android.material.button.MaterialButton btnOpenRoleDashboard;
    private ConnectivityManager.NetworkCallback networkCallback;

    private boolean recitalAnimated = false;
    private boolean gameAnimated = false;
    private boolean quizAnimated = false;
    private boolean progressAnimated = false;

    private static final String KEY_ANIMATIONS_ENABLED = "ANIMATIONS_ENABLED";
    private static final String KEY_LOW_POWER_ANIMATIONS = "LOW_POWER_ANIMATIONS";

    public static final String PREF_NAME = "EduLinguaPrefs";

    private static final String KEY_LAST_LANG_CODE = "LAST_LANG_CODE";
    private static final String KEY_LAST_LANG_NAME = "LAST_LANG_NAME";
    public static final String KEY_SEEN_INTRO = "SEEN_INTRO";

    private String selectedLangCode = null;
    private String selectedLangName = null;

    private String[] langNames = {"English", "French", "Twi", "Ewe", "Ga"};
    private String[] langCodes = {"en", "fr", "ak", "ee", "gaa"};
    private int[] langFlags = {R.drawable.ic_flag_uk, R.drawable.ic_flag_france, R.drawable.ic_flag_ghana, R.drawable.ic_flag_ghana, R.drawable.ic_flag_ghana};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize permission helper before super.onCreate to ensure early registration
        // of the ActivityResultLauncher before the activity is started.
        permissionHelper = new com.edulinguaghana.social.NotificationPermissionHelper(this);

        super.onCreate(savedInstanceState);

        // Re-initialize FCM token in MainActivity to ensure it's logged correctly
        new com.edulinguaghana.social.FCMTokenManager(this).initializeFCMToken();

        // Ensure the app respects system windows (status bar, navigation bar)
        // Remove any fullscreen flags that might be set
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Apply custom font to toolbar title
        applyToolbarFont(toolbar);

        // Animate toolbar entrance
        animateToolbar(toolbar);

        rootCoordinator = findViewById(R.id.rootCoordinator);
        heroCard = findViewById(R.id.heroCard);
        starTopLeft = findViewById(R.id.starTopLeft);
        starTopRight = findViewById(R.id.starTopRight);
        bubbleTop = findViewById(R.id.bubbleTop);
        bubbleMidRight = findViewById(R.id.bubbleMidRight);
        bubbleBottomLeft = findViewById(R.id.bubbleBottomLeft);
        floatingElementsContainer = findViewById(R.id.floatingElementsContainer);
        animatedShapesContainer = findViewById(R.id.animatedShapesContainer);
        tvMotivationMessage = findViewById(R.id.tvMotivationMessage);
        tvStreakCount = findViewById(R.id.tvStreakCount);
        tvFunFact = findViewById(R.id.tvFunFact);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvTotalQuizzes = findViewById(R.id.tvTotalQuizzes);
        tvTotalGames = findViewById(R.id.tvTotalGames);
        tvAchievements = findViewById(R.id.tvAchievements);
        offlineBanner = findViewById(R.id.offlineBanner);
        languageChipGroup = findViewById(R.id.languageChipGroup);
        btnRecitalMode = findViewById(R.id.btnRecitalMode);
        btnGameMode = findViewById(R.id.btnGameMode);
        btnQuizMode = findViewById(R.id.btnQuizMode);
        btnProgressMode = findViewById(R.id.btnProgressMode);
        mascotView = findViewById(R.id.mascotView);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        roleDashboardCard = findViewById(R.id.roleDashboardCard);
        tvRoleTitle = findViewById(R.id.tvRoleTitle);
        tvRoleSubtitle = findViewById(R.id.tvRoleSubtitle);
        btnOpenRoleDashboard = findViewById(R.id.btnOpenRoleDashboard);

        setupMascot();
        setupHeroGlow();
        setupStarAnimations();
        setupBubbleAnimations();
        setupFloatingElements();
        setupAnimatedShapes();
        setupEnhancedFeatures();
        setupQuickStats();
        setupOfflineIndicator();
        setupLanguageChips();
        restoreLastLanguageSelection();
        setupButtons();
        setupBottomNavigation();
        setupScrollAnimations();
        setupBackHandler();
        showIntroIfFirstTime();

        // Initialize notification system
        initializeNotifications();

        // Setup role-based navigation
        setupRoleBasedNavigation();
    }

    private void setupOfflineIndicator() {
        if (offlineBanner == null) return;

        OfflineManager offlineManager = new OfflineManager(this);
        boolean isOnline = offlineManager.isOnline();
        updateOfflineBanner(isOnline);

        // Request notification permission (Android 13+)
        requestNotificationPermission();
    }

    private void updateOfflineBanner(boolean isOnline) {
        if (offlineBanner == null) return;
        runOnUiThread(() -> {
            if (isOnline) {
                if (offlineBanner.getVisibility() != View.GONE) {
                    offlineBanner.setVisibility(View.GONE);
                }
            } else {
                if (offlineBanner.getVisibility() != View.VISIBLE) {
                    offlineBanner.setVisibility(View.VISIBLE);
                    // Use a more modern animation for entrance
                    offlineBanner.setAlpha(0f);
                    offlineBanner.animate().alpha(1f).setDuration(500).start();
                }
            }
        });
    }

    private void registerNetworkCallback() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) return;

            NetworkRequest networkRequest = new NetworkRequest.Builder().build();
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@androidx.annotation.NonNull Network network) {
                    updateOfflineBanner(true);
                }

                @Override
                public void onLost(@androidx.annotation.NonNull Network network) {
                    updateOfflineBanner(false);
                }
            };
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        } catch (Exception ignored) {}
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null) {
            try {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerNetworkCallback();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterNetworkCallback();
    }

    private void requestNotificationPermission() {
        // Use the pre-initialized permissionHelper
        if (permissionHelper == null) return;

        // Request permission after a short delay to not overwhelm user on startup
        new android.os.Handler().postDelayed(() -> {
            permissionHelper.requestPermissionSilently();
        }, 2000); // Wait 2 seconds after app starts
    }

    private void setupQuickStats() {
        // Update Total Quizzes
        if (tvTotalQuizzes != null) {
            int totalQuizzes = ProgressManager.getTotalQuizzes(this);
            tvTotalQuizzes.setText(String.valueOf(totalQuizzes));
        }

        // Update Total Games
        if (tvTotalGames != null) {
            int totalGames = FunGameProgressManager.getTotalFunGamesPlayed(this);
            tvTotalGames.setText(String.valueOf(totalGames));
        }

        // Update Achievements
        if (tvAchievements != null) {
            AchievementManager achievementManager = new AchievementManager(this);
            int unlocked = achievementManager.getUnlockedCount();
            int total = achievementManager.getTotalCount();
            tvAchievements.setText(unlocked + "/" + total);
        }

        // Setup badges click handler
        View badgesClickArea = findViewById(R.id.badgesClickArea);
        if (badgesClickArea != null) {
            badgesClickArea.setOnClickListener(v -> openAchievementsScreen());
        }

        /* Fun Games stats (total, best, per-game counts) - removed from layout
        android.widget.TextView tvFunGamesTotal = findViewById(R.id.tvFunGamesTotal);
        android.widget.TextView tvFunGamesBest = findViewById(R.id.tvFunGamesBest);
        android.widget.TextView tvFunGamesPerGame = findViewById(R.id.tvFunGamesPerGame);

        if (tvFunGamesTotal != null) {
            int totalFun = FunGameProgressManager.getTotalFunGamesPlayed(this);
            tvFunGamesTotal.setText(String.valueOf(totalFun));
        }

        if (tvFunGamesBest != null) {
            int best = FunGameProgressManager.getBestFunGameScore(this);
            tvFunGamesBest.setText(String.valueOf(best));
        }

        if (tvFunGamesPerGame != null) {
            int speed = FunGameProgressManager.getSpeedGamesPlayed(this);
            int puzzle = FunGameProgressManager.getPuzzleGamesPlayed(this);
            tvFunGamesPerGame.setText("S:" + speed + " P:" + puzzle);
        } */
    }

    private void openAchievementsScreen() {
        OfflineManager offlineManager = new OfflineManager(this);

        // Check if user is logged in
        if (!offlineManager.isLoggedIn()) {
            StyledMenuHelper.showStyledConfirmationDialog(
                this,
                "🔒",
                "Achievements Locked",
                "Sign in to unlock amazing badges, earn XP, and track your learning milestones!",
                "Sign In",
                "Later",
                () -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                },
                null
            );
            return;
        }

        // Check email verification
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            showVerificationRequiredDialog();
            return;
        }

        // Check internet connection
        if (!offlineManager.isOnline()) {
            StyledMenuHelper.showStyledConfirmationDialog(
                this,
                "📶",
                "Internet Required",
                "Achievements require an internet connection. Please connect and try again.",
                "OK",
                null,
                null,
                null
            );
            return;
        }

        // Open achievements screen
        Intent intent = new Intent(this, AchievementsActivity.class);
        startActivity(intent);
    }

    private void initializeNotifications() {
        NotificationManager notificationManager = new NotificationManager(this);

        // Check if this is first launch
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isFirstLaunch = !prefs.getBoolean("HAS_LAUNCHED_BEFORE", false);

        if (isFirstLaunch) {
            // Welcome notification
            notificationManager.addNotification(
                "Welcome to EduLingua Ghana! 🎉",
                "Start your journey to mastering Ghanaian languages today!",
                "🎉",
                Notification.NotificationType.MOTIVATIONAL
            );

            // Add some sample notifications to show the feature
            notificationManager.addNotification(
                "Let's Get Started! 🚀",
                "Choose a language and begin your first lesson.",
                "🚀",
                Notification.NotificationType.REMINDER
            );

            notificationManager.addNotification(
                "Daily Learning Tip 💡",
                "Just 10 minutes a day can make a huge difference in language learning!",
                "💡",
                Notification.NotificationType.MOTIVATIONAL
            );

            prefs.edit().putBoolean("HAS_LAUNCHED_BEFORE", true).apply();
        } else {
            // Check and generate automatic notifications
            notificationManager.checkAndGenerateNotifications();
        }
    }

    private void setupMascot() {
        if (mascotView == null) return;

        // Entrance animation: Peek from bottom
        mascotView.setAlpha(0f);
        mascotView.setTranslationY(100f);
        mascotView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1000)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .withEndAction(this::startMascotIdleAnimation)
                .start();

        // Set up interactive click listener
        mascotView.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            onMascotClicked();
        });

        // Set up long click for special animation
        mascotView.setOnLongClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            playMascotCelebration();
            showMascotMessage(getString(R.string.mascot_special_love));
            return true;
        });
    }

    private void startMascotIdleAnimation() {
        if (mascotView == null || !animationsEnabled()) return;

        try {
            android.view.animation.Animation idleAnim = AnimationUtils.loadAnimation(this, R.anim.mascot_idle);
            mascotView.startAnimation(idleAnim);
        } catch (Exception e) {
            // Fail silently
        }
    }

    private void onMascotClicked() {
        if (mascotView == null) return;

        vibrate();

        // Random interaction
        int interaction = (int) (Math.random() * 3);

        switch (interaction) {
            case 0:
                playMascotJump();
                showMascotMessage(getMascotGreeting());
                break;
            case 1:
                playMascotShake();
                showMascotMessage(getMascotEncouragement());
                break;
            case 2:
                playMascotCelebration();
                showMascotMessage(getString(R.string.mascot_yay));
                break;
        }
    }

    private void playMascotJump() {
        if (mascotView == null || !animationsEnabled()) return;

        try {
            android.view.animation.Animation jumpAnim = AnimationUtils.loadAnimation(this, R.anim.mascot_jump);
            jumpAnim.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override
                public void onAnimationStart(android.view.animation.Animation animation) {}

                @Override
                public void onAnimationEnd(android.view.animation.Animation animation) {
                    startMascotIdleAnimation();
                }

                @Override
                public void onAnimationRepeat(android.view.animation.Animation animation) {}
            });
            mascotView.startAnimation(jumpAnim);
        } catch (Exception e) {
            // Fail silently
        }
    }

    private void playMascotShake() {
        if (mascotView == null || !animationsEnabled()) return;

        try {
            android.view.animation.Animation shakeAnim = AnimationUtils.loadAnimation(this, R.anim.mascot_shake);
            shakeAnim.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override
                public void onAnimationStart(android.view.animation.Animation animation) {}

                @Override
                public void onAnimationEnd(android.view.animation.Animation animation) {
                    startMascotIdleAnimation();
                }

                @Override
                public void onAnimationRepeat(android.view.animation.Animation animation) {}
            });
            mascotView.startAnimation(shakeAnim);
        } catch (Exception e) {
            // Fail silently
        }
    }

    private void playMascotCelebration() {
        if (mascotView == null || !animationsEnabled()) return;

        vibrate();

        try {
            android.view.animation.Animation celebrateAnim = AnimationUtils.loadAnimation(this, R.anim.mascot_celebrate);
            celebrateAnim.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override
                public void onAnimationStart(android.view.animation.Animation animation) {}

                @Override
                public void onAnimationEnd(android.view.animation.Animation animation) {
                    startMascotIdleAnimation();
                }

                @Override
                public void onAnimationRepeat(android.view.animation.Animation animation) {}
            });
            mascotView.startAnimation(celebrateAnim);
        } catch (Exception e) {
            // Fail silently
        }
    }

    private String getMascotGreeting() {
        String[] greetings = {
            getString(R.string.mascot_greeting_1),
            getString(R.string.mascot_greeting_2),
            getString(R.string.mascot_greeting_3),
            getString(R.string.mascot_greeting_4)
        };
        return greetings[(int) (Math.random() * greetings.length)];
    }

    private String getMascotEncouragement() {
        String[] encouragements = {
            getString(R.string.mascot_encouragement_1),
            getString(R.string.mascot_encouragement_2),
            getString(R.string.mascot_encouragement_3)
        };
        return encouragements[(int) (Math.random() * encouragements.length)];
    }

    private void showMascotMessage(String message) {
        if (rootCoordinator == null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        Snackbar snackbar = Snackbar.make(rootCoordinator, message, Snackbar.LENGTH_LONG);
        if (heroCard != null) {
            snackbar.setAnchorView(heroCard);
        }
        snackbar.setBackgroundTint(ContextCompat.getColor(this, R.color.colorPrimary));
        snackbar.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        snackbar.setAction("OK", v -> snackbar.dismiss());
        snackbar.setActionTextColor(ContextCompat.getColor(this, android.R.color.white));
        snackbar.show();
    }

    private void setupHeroGlow() {
        try {
            int heroRes = animationsReduced() ? R.animator.hero_glow_reduced : R.animator.hero_glow;
            heroGlowAnimator = AnimatorInflater.loadAnimator(this, heroRes);
            if (heroGlowAnimator != null && heroCard != null) {
                heroGlowAnimator.setTarget(heroCard);
            }
        } catch (Exception e) {
            // fail silently if animator isn't available on older platforms
            heroGlowAnimator = null;
        }
    }

    private void setupStarAnimations() {
        try {
            int twinkleRes = animationsReduced() ? R.animator.star_twinkle_reduced : R.animator.star_twinkle;
            int orbitRes = animationsReduced() ? R.animator.star_slow_orbit_reduced : R.animator.star_slow_orbit;
            starLeftAnimator = AnimatorInflater.loadAnimator(this, twinkleRes);
            starRightAnimator = AnimatorInflater.loadAnimator(this, orbitRes);

            if (starLeftAnimator != null && starTopLeft != null) starLeftAnimator.setTarget(starTopLeft);
            if (starRightAnimator != null && starTopRight != null) starRightAnimator.setTarget(starTopRight);
        } catch (Exception e) {
            // ignore if animators can't be loaded on some devices
            starLeftAnimator = null;
            starRightAnimator = null;
        }
    }

    private void setupBubbleAnimations() {
        try {
            int bubbleRes = animationsReduced() ? R.animator.bubble_float_reduced : R.animator.bubble_float;
            bubbleTopAnimator = AnimatorInflater.loadAnimator(this, bubbleRes);
            bubbleMidAnimator = AnimatorInflater.loadAnimator(this, bubbleRes);
            bubbleBottomAnimator = AnimatorInflater.loadAnimator(this, bubbleRes);

            if (bubbleTopAnimator != null && bubbleTop != null) {
                bubbleTopAnimator.setTarget(bubbleTop);
                bubbleTopAnimator.setStartDelay(0);
            }
            if (bubbleMidAnimator != null && bubbleMidRight != null) {
                bubbleMidAnimator.setTarget(bubbleMidRight);
                bubbleMidAnimator.setStartDelay(300);
            }
            if (bubbleBottomAnimator != null && bubbleBottomLeft != null) {
                bubbleBottomAnimator.setTarget(bubbleBottomLeft);
                bubbleBottomAnimator.setStartDelay(600);
            }
        } catch (Exception e) {
            bubbleTopAnimator = bubbleMidAnimator = bubbleBottomAnimator = null;
        }
    }

    private void setupFloatingElements() {
        if (floatingElementsContainer == null || !animationsEnabled()) return;

        try {
            // Get all child FrameLayouts (each contains a letter or number)
            for (int i = 0; i < floatingElementsContainer.getChildCount(); i++) {
                View floatingElement = floatingElementsContainer.getChildAt(i);

                // Choose animation based on position (vary the animations)
                int animRes;
                switch (i % 3) {
                    case 0:
                        animRes = R.anim.float_up_slow;
                        break;
                    case 1:
                        animRes = R.anim.float_up_medium;
                        break;
                    default:
                        animRes = R.anim.float_up_fast;
                        break;
                }

                android.view.animation.Animation floatAnim = AnimationUtils.loadAnimation(this, animRes);
                // Stagger the start of each animation
                floatAnim.setStartOffset(i * 400L);
                floatingElement.startAnimation(floatAnim);
            }
        } catch (Exception e) {
            // Fail silently if animations can't be loaded
        }
    }

    private void setupAnimatedShapes() {
        if (animatedShapesContainer == null || !animationsEnabled()) return;

        try {
            // Animation resource mapping
            int[] animResources = {
                R.anim.diagonal_drift,      // star1
                R.anim.bounce_rotate,       // circleCyan
                R.anim.circular_orbit,      // triangle1
                R.anim.zigzag_path,         // square1
                R.anim.shimmer_wave,        // circlePink
                R.anim.bounce_rotate,       // star2
                R.anim.diagonal_drift,      // circleGreen
                R.anim.circular_orbit,      // sparkle1
                R.anim.zigzag_path,         // ring1
                R.anim.diagonal_drift,      // square2
                R.anim.shimmer_wave,        // triangle2
                R.anim.circular_orbit,      // circleCyan2
                R.anim.zigzag_path          // star3
            };

            // Apply animations to each shape with varied timing
            for (int i = 0; i < animatedShapesContainer.getChildCount() && i < animResources.length; i++) {
                View shape = animatedShapesContainer.getChildAt(i);
                android.view.animation.Animation shapeAnim = AnimationUtils.loadAnimation(this, animResources[i]);

                // Stagger start times for organic feel
                shapeAnim.setStartOffset(i * 300L);
                shape.startAnimation(shapeAnim);
            }
        } catch (Exception e) {
            // Fail silently if animations can't be loaded
        }
    }

    private void setupEnhancedFeatures() {
        // Setup time-based greeting
        setupGreeting();

        // Setup announcements
        setupAnnouncementsListener();

        // Setup daily motivational message
        setupDailyMotivation();

        // Setup learning streak
        setupLearningStreak();

        // Setup fun facts
        setupFunFacts();

        // Animate cards on entrance
        animateEnhancedCards();
    }

    private void setupGreeting() {
        if (tvGreeting == null) return;

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String timeGreeting;

        if (hour >= 5 && hour < 12) {
            timeGreeting = getString(R.string.main_greeting_morning);
        } else if (hour >= 12 && hour < 17) {
            timeGreeting = getString(R.string.main_greeting_afternoon);
        } else if (hour >= 17 && hour < 21) {
            timeGreeting = getString(R.string.main_greeting_evening);
        } else {
            timeGreeting = getString(R.string.main_greeting_generic);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            String firstName = user.getDisplayName().split(" ")[0];
            tvGreeting.setText(timeGreeting.replace("!", "") + ", " + firstName + "!");
        } else {
            tvGreeting.setText(timeGreeting);
        }
    }

    private void setupAnnouncementsListener() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        final String currentUid = user.getUid();
        
        // We first need the student's class to filter CLASS broadcasts
        RoleManager roleManager = new RoleManager();
        roleManager.getUserStudentClass(currentUid, new RoleManager.StringValueCallback() {
            @Override
            public void onValueRetrieved(String studentClass) {
                listenForAnnouncements(currentUid, studentClass);
            }

            @Override
            public void onError(String error) {
                // If we can't get the class, we just listen for ALL and STUDENT types
                listenForAnnouncements(currentUid, null);
            }
        });
    }

    private void listenForAnnouncements(String currentUid, String myClass) {
        DatabaseReference announceRef = FirebaseDatabase.getInstance().getReference("announcements");
        // Limit to recent announcements
        announceRef.orderByChild("timestamp").limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    long timestamp = postSnapshot.child("timestamp").getValue(Long.class) != null ?
                            postSnapshot.child("timestamp").getValue(Long.class) : 0;

                    // Filter by time: Only show if it's new (in the last 5 minutes)
                    if (System.currentTimeMillis() - timestamp < 5 * 60 * 1000) {
                        String type = postSnapshot.child("type").getValue(String.class);
                        String targetId = postSnapshot.child("targetId").getValue(String.class);
                        String message = postSnapshot.child("message").getValue(String.class);
                        String teacherName = postSnapshot.child("teacherName").getValue(String.class);

                        // Filtering logic
                        boolean isForMe = false;
                        if (type == null || "ALL".equals(type)) {
                            isForMe = true;
                        } else if ("STUDENT".equals(type)) {
                            isForMe = currentUid.equals(targetId);
                        } else if ("CLASS".equals(type) && myClass != null) {
                            isForMe = myClass.equals(targetId);
                        }

                        if (isForMe) {
                            // Check if we already showed this one (using SharedPreferences to avoid duplicates)
                            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                            java.util.Set<String> seenIds = prefs.getStringSet("SEEN_ANNOUNCEMENT_IDS", new java.util.HashSet<>());
                            
                            if (seenIds != null && !seenIds.contains(postSnapshot.getKey())) {
                                showAnnouncementNotification(teacherName, message);
                                
                                // Update seen IDs
                                java.util.Set<String> newSeenIds = new java.util.HashSet<>(seenIds);
                                newSeenIds.add(postSnapshot.getKey());
                                
                                // Clean up if too many IDs tracked (very unlikely to be an issue, but good practice)
                                if (newSeenIds.size() > 50) {
                                    List<String> sorted = new java.util.ArrayList<>(newSeenIds);
                                    newSeenIds = new java.util.HashSet<>(sorted.subList(25, sorted.size()));
                                }
                                
                                prefs.edit().putStringSet("SEEN_ANNOUNCEMENT_IDS", newSeenIds).apply();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void showAnnouncementNotification(String teacher, String message) {
        NotificationManager nm = new NotificationManager(this);
        nm.addNotification(
            "Teacher Announcement 📢",
            teacher + ": " + message,
            "📢",
            Notification.NotificationType.MOTIVATIONAL
        );
    }

    private void setupDailyMotivation() {
        if (tvMotivationMessage == null) return;

        String[] motivationMessages = {
            "You're doing great! 🌟",
            "Keep learning! 💪",
            "Every day is progress! 🚀",
            "You're amazing! ✨",
            "Learning is fun! 🎉",
            "Stay curious! 🤔",
            "You can do it! 💫",
            "Believe in yourself! 🌈"
        };

        // Pick a random motivational message
        int index = (int) (Math.random() * motivationMessages.length);
        tvMotivationMessage.setText(motivationMessages[index]);
    }

    private void setupLearningStreak() {
        if (tvStreakCount == null) return;

        StreakManager streakManager = new StreakManager(this);
        int streak = streakManager.getCurrentStreak();

        if (streak == 0) {
            tvStreakCount.setText(R.string.main_streak_start);
        } else if (streak == 1) {
            tvStreakCount.setText(R.string.main_streak_day);
        } else {
            tvStreakCount.setText(getString(R.string.main_streak_days, streak));
        }
    }

    private void setupFunFacts() {
        if (tvFunFact == null) return;

        String[] funFacts = {
            "Ghana has over 80 languages! 🇬🇭",
            "Twi is spoken by 9 million people! 💬",
            "Learning languages makes your brain stronger! 🧠",
            "French is spoken in 29 countries! 🌍",
            "Ewe has unique tonal sounds! 🎵",
            "The Ga people live near Accra! 🏙️",
            "Bilingual children are better problem solvers! ⚡",
            "Learning a language connects you to cultures! 🤝"
        };

        // Pick a random fun fact
        int index = (int) (Math.random() * funFacts.length);
        tvFunFact.setText(funFacts[index]);
    }

    private void animateEnhancedCards() {
        if (!animationsEnabled()) return;

        try {
            View motivationCard = findViewById(R.id.motivationCard);
            View streakCard = findViewById(R.id.streakCard);
            View funFactCard = findViewById(R.id.funFactCard);

            if (motivationCard != null) {
                android.view.animation.Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_gentle);
                anim.setStartOffset(200);
                motivationCard.startAnimation(anim);
            }

            if (streakCard != null) {
                android.view.animation.Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_gentle);
                anim.setStartOffset(300);
                streakCard.startAnimation(anim);
            }

            if (funFactCard != null) {
                android.view.animation.Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_gentle);
                anim.setStartOffset(400);
                funFactCard.startAnimation(anim);
            }
        } catch (Exception e) {
            // Fail silently
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);


        // Add role-based menu items dynamically
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            RoleManager roleManager = new RoleManager();
            roleManager.getUserRole(this, user.getUid(), new RoleManager.RoleCallback() {
                @Override
                public void onRoleRetrieved(UserRole role) {
                    addRoleMenuItems(menu, role);
                }

                @Override
                public void onError(String error) {
                    // No special menu items
                }
            });
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_my_students) {
            Intent intent = new Intent(this, TeacherDashboardActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_my_children) {
            Intent intent = new Intent(this, ParentDashboardActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_connections) {
            Intent intent = new Intent(this, RelationshipManagementActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void applyToolbarFont(Toolbar toolbar) {
        // Apply custom Agbalumo font to toolbar title
        try {
            android.graphics.Typeface typeface = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.agbalumo);
            // Get the TextView that displays the title
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View view = toolbar.getChildAt(i);
                if (view instanceof android.widget.TextView) {
                    android.widget.TextView textView = (android.widget.TextView) view;
                    if (textView.getText().equals(toolbar.getTitle())) {
                        textView.setTypeface(typeface);
                        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 20);

                        // Add morphing/inflating text animation
                        if (animationsEnabled()) {
                            animateTextMorph(textView);
                        }
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void animateTextMorph(android.widget.TextView textView) {
        // Start with compressed text (small scale and tight letter spacing)
        textView.setScaleX(0.3f);
        textView.setScaleY(0.3f);
        textView.setAlpha(0f);
        textView.setLetterSpacing(0.3f); // Expanded letter spacing

        // Animate to normal size with morphing effect
        textView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(300)
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.5f))
            .withEndAction(() -> {
                // Animate letter spacing to normal
                android.animation.ValueAnimator letterSpacingAnimator = android.animation.ValueAnimator.ofFloat(0.3f, 0.05f);
                letterSpacingAnimator.setDuration(400);
                letterSpacingAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator());
                letterSpacingAnimator.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    textView.setLetterSpacing(value);
                });
                letterSpacingAnimator.start();
            })
            .start();
    }

    private void animateToolbar(Toolbar toolbar) {
        if (!animationsEnabled()) return;

        // Start with toolbar slightly scaled down
        toolbar.setScaleX(0.9f);
        toolbar.setScaleY(0.9f);
        toolbar.setAlpha(0.7f);

        // Animate with a gentle zoom-in effect
        toolbar.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(400)
            .setStartDelay(100)
            .setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f))
            .start();

        // Add playful rotation to logo
        toolbar.postDelayed(() -> {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                View view = toolbar.getChildAt(i);
                if (view instanceof ImageView) {
                    view.setRotation(-15f);
                    view.setScaleX(0.6f);
                    view.setScaleY(0.6f);
                    view.setAlpha(0f);
                    view.animate()
                        .rotation(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(500)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(1.8f))
                        .start();
                    break;
                }
            }
        }, 150);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh quick stats
        setupQuickStats();
        setupLearningStreak();
        setupOfflineIndicator();

        // Check and unlock achievements
        AchievementManager achievementManager = new AchievementManager(this);
        achievementManager.checkAndUnlockAchievements();

        // Setup role-based navigation (refresh in case role changed)
        setupRoleBasedNavigation();

        // previously read high score to show in removed UI; keep prefs access in case other features rely on it

        // Only start the dynamic overlay pulse when animations are enabled, dynamic
        // backgrounds are allowed by user preference and the system is not in dark mode.
        boolean isDark = ThemeUtils.isDarkMode(this);

        if (animationsEnabled() && !isDark) {
            if (heroGlowAnimator != null && !heroGlowAnimator.isStarted()) {
                heroGlowAnimator.start();
            }
            if (starLeftAnimator != null && !starLeftAnimator.isStarted()) starLeftAnimator.start();
            if (starRightAnimator != null && !starRightAnimator.isStarted()) starRightAnimator.start();
            if (bubbleTopAnimator != null && !bubbleTopAnimator.isStarted()) bubbleTopAnimator.start();
            if (bubbleMidAnimator != null && !bubbleMidAnimator.isStarted()) bubbleMidAnimator.start();
            if (bubbleBottomAnimator != null && !bubbleBottomAnimator.isStarted()) bubbleBottomAnimator.start();
        }
    }

    @Override
    protected void onPause() {
        if (heroGlowAnimator != null && heroGlowAnimator.isRunning()) {
            heroGlowAnimator.end();
        }
        if (starLeftAnimator != null && starLeftAnimator.isRunning()) starLeftAnimator.end();
        if (starRightAnimator != null && starRightAnimator.isRunning()) starRightAnimator.end();
        if (bubbleTopAnimator != null && bubbleTopAnimator.isRunning()) bubbleTopAnimator.end();
        if (bubbleMidAnimator != null && bubbleMidAnimator.isRunning()) bubbleMidAnimator.end();
        if (bubbleBottomAnimator != null && bubbleBottomAnimator.isRunning()) bubbleBottomAnimator.end();
        super.onPause();
    }

    private boolean animationsEnabled() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_ANIMATIONS_ENABLED, true);
    }

    private boolean animationsReduced() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_LOW_POWER_ANIMATIONS, false);
    }

    // ---------------- BACK HANDLER ----------------

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });
    }

    private void showExitDialog() {
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "🚪",
            getString(R.string.main_exit_title),
            getString(R.string.main_exit_message),
            "Yes",
            "No",
            this::playAppExitSoundAndExit,
            null
        );
    }

    private void playAppExitSoundAndExit() {

        // Close immediately
        finish();
    }

    // ---------------- INTRO DIALOG ----------------

    private void showIntroIfFirstTime() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean seenIntro = prefs.getBoolean(KEY_SEEN_INTRO, false);
        if (seenIntro) return;

        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
        // We don't finish() here because user can skip and come back, 
        // but TutorialActivity will finish itself and return to a NEW MainActivity instance or just finish.
        // Actually TutorialActivity starts a NEW MainActivity. To avoid duplicates:
        finish();
    }

    // ---------------- LANGUAGE ----------------

    private void setupLanguageChips() {
        for (int i = 0; i < langNames.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(langNames[i]);
            chip.setTag(langCodes[i]);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setChipIcon(AppCompatResources.getDrawable(this, langFlags[i]));

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedLangCode = (String) buttonView.getTag();
                    selectedLangName = buttonView.getText().toString();
                    saveLastLanguageSelection(selectedLangCode, selectedLangName);
                }
            });

            chip.setOnClickListener(v -> {
                if (chip.isChecked()) {
                    showMascotMessage(getString(R.string.mascot_lang_switch, chip.getText().toString()));
                    playMascotJump();
                }
            });
            languageChipGroup.addView(chip);
        }
    }

    private void saveLastLanguageSelection(String code, String name) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString(KEY_LAST_LANG_CODE, code);
        ed.putString(KEY_LAST_LANG_NAME, name);
        ed.apply();
    }

    private void restoreLastLanguageSelection() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String lastCode = prefs.getString(KEY_LAST_LANG_CODE, "en"); // Default to English

        for (int i = 0; i < languageChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) languageChipGroup.getChildAt(i);
            if (chip.getTag().equals(lastCode)) {
                chip.setChecked(true);
                return; // Found and set the chip
            }
        }

        // If no chip was found, default to the first one
        if (languageChipGroup.getChildCount() > 0) {
            Chip firstChip = (Chip) languageChipGroup.getChildAt(0);
            firstChip.setChecked(true);
        }
    }

    private boolean ensureLanguageSelected() {
        if (selectedLangCode == null || selectedLangName == null) {
            Toast.makeText(this, R.string.main_lang_select_prompt, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ---------------- BUTTON ACTIONS ----------------

    private void applyGamingClickEffect(View v, Runnable action) {
        v.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(100)
            .withEndAction(() -> {
                v.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .withEndAction(action)
                    .start();
            })
            .start();
    }

    private void setupButtons() {
        btnRecitalMode.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            applyGamingClickEffect(v, () -> {
                if (!ensureLanguageSelected()) return;
                showContentTypeDialog(selectedLangCode, selectedLangName);
            });
        });

        btnGameMode.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            applyGamingClickEffect(v, () -> {
                if (!ensureLanguageSelected()) return;
                showGameModeDialog(selectedLangCode, selectedLangName);
            });
        });

        btnQuizMode.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            applyGamingClickEffect(v, () -> {
                if (!ensureLanguageSelected()) return;
                showQuizTypeDialog(selectedLangCode, selectedLangName);
            });
        });

        btnProgressMode.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            applyGamingClickEffect(v, this::openProgressScreen);
        });
    }

    private void showContentTypeDialog(String langCode, String langName) {
        String modeLabel = getString(R.string.content_recital);

        List<StyledMenuHelper.MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🔤",
                getString(R.string.content_alphabet),
                getString(R.string.content_alphabet_desc),
                () -> openAlphabetScreen(langCode, langName)
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🔢",
                getString(R.string.content_numbers),
                getString(R.string.content_numbers_desc),
                () -> openNumbersScreen(langCode, langName)
        ));

        StyledMenuHelper.showStyledMenu(
                this,
                "🎤",
                modeLabel + " Mode - " + langName,
                "",
                menuItems,
                null
        );
    }

    // UPDATED: Educational quizzes only (Games moved to Game Mode)
    private void showQuizTypeDialog(String langCode, String langName) {
        List<StyledMenuHelper.MenuItem> menuItems = new ArrayList<>();
        
        // --- CATEGORY: EDUCATIONAL QUIZZES ---
        menuItems.add(new StyledMenuHelper.MenuItem(
                "📚",
                getString(R.string.main_section_quizzes),
                "SECTION_HEADER",
                null
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🔤",
                getString(R.string.quiz_mode_letters),
                getString(R.string.quiz_desc_letters),
                () -> openQuizScreen(langCode, langName, "basic", "beginner", "all")
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🔢",
                getString(R.string.quiz_mode_numbers),
                getString(R.string.quiz_desc_numbers),
                () -> openQuizScreen(langCode, langName, "numbers", "beginner", "all")
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "📊",
                getString(R.string.quiz_mode_sequence),
                getString(R.string.quiz_desc_sequence),
                () -> openQuizScreen(langCode, langName, "sequence", "beginner", "all")
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🎯",
                getString(R.string.quiz_mode_matching),
                getString(R.string.quiz_desc_matching),
                () -> openQuizScreen(langCode, langName, "matching", "beginner", "all")
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🔡",
                getString(R.string.quiz_mode_word_recog),
                getString(R.string.quiz_desc_word_recog),
                () -> openQuizScreen(langCode, langName, "odd_one_out", "beginner", "all")
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🎲",
                getString(R.string.quiz_mode_mixed),
                getString(R.string.quiz_desc_mixed),
                () -> openQuizScreen(langCode, langName, "mixed", "beginner", "all")
        ));

        StyledMenuHelper.showStyledMenu(
                this,
                "📚",
                getString(R.string.main_section_quizzes),
                getString(R.string.quiz_desc_mixed),
                menuItems,
                null
        );
    }

    private void showGameModeDialog(String langCode, String langName) {
        List<StyledMenuHelper.MenuItem> menuItems = new ArrayList<>();

        // --- CATEGORY: FUN MINI-GAMES ---
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🎮",
                getString(R.string.main_section_games),
                "SECTION_HEADER",
                null
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🚀",
                getString(R.string.game_rocket_sort_title),
                getString(R.string.game_rocket_sort_desc),
                () -> openRocketSortScreen(langCode, langName)
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🫧",
                getString(R.string.game_bubble_pop_title),
                getString(R.string.game_bubble_pop_desc),
                () -> openBubblePopScreen(langCode, langName)
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🏃",
                getString(R.string.game_speed_challenge_title),
                getString(R.string.game_speed_challenge_desc),
                () -> openSpeedGameScreen(langCode, langName)
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🏜️",
                getString(R.string.game_hidden_shapes_title),
                getString(R.string.game_hidden_shapes_desc),
                () -> openHiddenShapesScreen(langCode, langName)
        ));
        menuItems.add(new StyledMenuHelper.MenuItem(
                "🧩",
                getString(R.string.game_puzzle_match_title),
                getString(R.string.game_puzzle_match_desc),
                () -> openPuzzleGameScreen(langCode, langName)
        ));

        StyledMenuHelper.showStyledMenu(
                this,
                "🎮",
                getString(R.string.mode_game_title),
                getString(R.string.mode_game_desc),
                menuItems,
                null
        );
    }

    /**
     * Show dialog for selecting difficulty level and word category
     * (DEPRECATED - directly launching with defaults now)
     */
    @Deprecated
    private void showDifficultyAndCategoryDialog(String langCode, String langName, String quizType) {
        // This method is no longer used - quiz launches with default settings
    }

    /**
     * Show dialog for selecting word category
     * (DEPRECATED - directly launching with defaults now)
     */
    @Deprecated
    private void showCategorySelectionDialog(String langCode, String langName, String quizType, String difficulty) {
        // This method is no longer used - quiz launches with default settings
    }

    private void openAlphabetScreen(String langCode, String langName) {
        Intent intent = new Intent(MainActivity.this, AlphabetActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        startActivity(intent);
    }

    private void openNumbersScreen(String langCode, String langName) {
        Intent intent = new Intent(MainActivity.this, NumbersActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        startActivity(intent);
    }

    private void openQuizScreen(String langCode, String langName, String quizType) {
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        intent.putExtra("QUIZ_TYPE", quizType);
        startActivity(intent);
    }

    /**
     * Open quiz screen with difficulty level and category selection
     */
    private void openQuizScreen(String langCode, String langName, String quizType, String difficulty, String category) {
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        intent.putExtra("QUIZ_TYPE", quizType);
        intent.putExtra("DIFFICULTY", difficulty);
        intent.putExtra("CATEGORY", category);
        startActivity(intent);
    }

    private void openSpeedGameScreen(String langCode, String langName) {
        Intent intent = new Intent(MainActivity.this, SpeedGameActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        startActivity(intent);
    }

    private void openHiddenShapesScreen(String langCode, String langName) {
        Intent intent = new Intent(MainActivity.this, HiddenShapesActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        startActivity(intent);
    }

    private void openPuzzleGameScreen(String langCode, String langName) {
        Intent intent = new Intent(MainActivity.this, com.edulinguaghana.games.PuzzleGameActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        startActivity(intent);
    }

    private void openBubblePopScreen(String langCode, String langName) {
        Intent intent = new Intent(MainActivity.this, com.edulinguaghana.games.bubblepop.BubblePopActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        startActivity(intent);
    }

    private void openRocketSortScreen(String langCode, String langName) {
        Intent intent = new Intent(MainActivity.this, com.edulinguaghana.games.rocketsort.RocketSortActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        startActivity(intent);
    }

    private void openProgressScreen() {
        Intent intent = new Intent(MainActivity.this, ProgressActivity.class);
        startActivity(intent);
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26 
            v.vibrate(50);
        }
    }

    private void setupScrollAnimations() {
        if (nestedScrollView == null) return;
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {
                if (!recitalAnimated) { animateCard(btnRecitalMode, true); recitalAnimated = true; }
                if (!gameAnimated) { animateCard(btnGameMode, true); gameAnimated = true; }
                if (!quizAnimated) { animateCard(btnQuizMode, true); quizAnimated = true; }
                if (!progressAnimated) { animateCard(btnProgressMode, true); progressAnimated = true; }
            }
        });
    }

    private void animateCard(View view, boolean scrollingDown) {
        if (scrollingDown) {
            view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;

        // Disable icon tinting to show colorful cartoon icons
        bottomNavigation.setItemIconTintList(null);

        // Set Home as selected by default
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            vibrate();
            
            // Add a small scale animation to the clicked item icon
            View itemView = findViewById(item.getItemId());
            if (itemView != null) {
                itemView.animate().scaleX(1.15f).scaleY(1.15f).setDuration(150).withEndAction(() -> 
                    itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                ).start();
            }

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home, maybe scroll to top
                if (nestedScrollView != null) {
                    nestedScrollView.smoothScrollTo(0, 0);
                }
                return true;
            } else if (itemId == R.id.nav_leaderboard) {
                openLeaderboardScreen();
                return true;
            } else if (itemId == R.id.nav_profile) {
                openProfileScreen();
                return true;
            } else if (itemId == R.id.nav_challenges) {
                openChallengesScreen();
                return true;
            } else if (itemId == R.id.nav_notifications) {
                openNotificationsScreen();
                return true;
            }
            return false;
        });
    }

    private void openLeaderboardScreen() {
        OfflineManager offlineManager = new OfflineManager(this);

        // Check if user is logged in
        if (!offlineManager.isLoggedIn()) {
            StyledMenuHelper.showStyledConfirmationDialog(
                this,
                "🔒",
                "Leaderboard Locked",
                "Sign in to compete on the global leaderboard, see your ranking, and earn top scores!",
                "Sign In",
                "Later",
                () -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                },
                null
            );
            return;
        }

        // Check email verification
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            showVerificationRequiredDialog();
            return;
        }

        // Check internet connection
        if (!offlineManager.isOnline()) {
            StyledMenuHelper.showStyledConfirmationDialog(
                this,
                "📶",
                "Internet Required",
                "Leaderboard requires an active internet connection to show global rankings.",
                "OK",
                null,
                null,
                null
            );
            return;
        }

        Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
        startActivity(intent);
    }

    private void openProfileScreen() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openChallengesScreen() {
        OfflineManager offlineManager = new OfflineManager(this);

        // Check if user is logged in
        if (!offlineManager.isLoggedIn()) {
            StyledMenuHelper.showStyledConfirmationDialog(
                this,
                "🔒",
                "Challenges Locked",
                "Sign in to unlock daily quests, earn exclusive badges, and compete with friends!",
                "Sign In",
                "Later",
                () -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                },
                null
            );
            return;
        }

        // Check email verification
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            showVerificationRequiredDialog();
            return;
        }

        // Check internet connection
        if (!offlineManager.isOnline()) {
            StyledMenuHelper.showStyledConfirmationDialog(
                this,
                "📶",
                "Internet Required",
                "Challenges require an active internet connection to sync quests and leaderboard data.",
                "OK",
                null,
                null,
                null
            );
            return;
        }

        Intent intent = new Intent(MainActivity.this, ChallengesActivity.class);
        startActivity(intent);
    }

    private void openNotificationsScreen() {
        Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
        startActivity(intent);
    }

    private void showVerificationRequiredDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "📧",
            getString(R.string.profile_verify_email_title),
            getString(R.string.profile_verify_email_desc),
            "Verify Now",
            "Later",
            () -> {
                // Go to account management where they can resend/check
                Intent intent = new Intent(this, AccountManagementActivity.class);
                startActivity(intent);
            },
            null
        );
    }

    /**
     * Setup role-based navigation for teachers and parents
     */
    private void setupRoleBasedNavigation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // User not logged in, hide role-based features
            if (roleDashboardCard != null) {
                roleDashboardCard.setVisibility(View.GONE);
            }
            return;
        }

        RoleManager roleManager = new RoleManager();
        roleManager.getUserRole(this, user.getUid(), new RoleManager.RoleCallback() {
            @Override
            public void onRoleRetrieved(UserRole role) {
                setupNavigationForRole(role);
            }

            @Override
            public void onError(String error) {
                // Default to student - no special navigation needed
            }
        });
    }

    /**
     * Setup navigation UI based on user role
     */
    private void setupNavigationForRole(UserRole role) {
        if (role == UserRole.TEACHER) {
            setupTeacherNavigation();
        } else if (role == UserRole.PARENT) {
            setupParentNavigation();
        } else {
            // Student role - add connection management access
            setupStudentNavigation();
        }
    }

    /**
     * Setup navigation for teachers
     */
    private void setupTeacherNavigation() {
        if (roleDashboardCard != null) {
            roleDashboardCard.setVisibility(View.VISIBLE);
            if (tvRoleTitle != null) tvRoleTitle.setText(R.string.settings_role_teacher);
            if (tvRoleSubtitle != null) tvRoleSubtitle.setText(R.string.role_dashboard_teacher_subtitle);
            if (btnOpenRoleDashboard != null) {
                btnOpenRoleDashboard.setOnClickListener(v -> {
                    vibrate();
                    startActivity(new Intent(this, TeacherDashboardActivity.class));
                });
            }
        }

        // Find or create FAB for teacher dashboard
        fabRoleDashboard = findViewById(R.id.fabRoleDashboard);
        if (fabRoleDashboard != null) {
            fabRoleDashboard.setVisibility(View.GONE);
        }

        // Also add menu item if toolbar menu exists
        invalidateOptionsMenu();
    }

    /**
     * Setup navigation for parents
     */
    private void setupParentNavigation() {
        if (roleDashboardCard != null) {
            roleDashboardCard.setVisibility(View.VISIBLE);
            if (tvRoleTitle != null) tvRoleTitle.setText(R.string.settings_role_parent);
            if (tvRoleSubtitle != null) tvRoleSubtitle.setText(R.string.role_dashboard_parent_subtitle);
            if (btnOpenRoleDashboard != null) {
                btnOpenRoleDashboard.setOnClickListener(v -> {
                    vibrate();
                    startActivity(new Intent(this, ParentDashboardActivity.class));
                });
            }
        }

        // Find or create FAB for parent dashboard
        fabRoleDashboard = findViewById(R.id.fabRoleDashboard);
        if (fabRoleDashboard != null) {
            fabRoleDashboard.setVisibility(View.GONE);
        }

        invalidateOptionsMenu();
    }

    /**
     * Setup navigation for students
     */
    private void setupStudentNavigation() {
        if (roleDashboardCard != null) {
            roleDashboardCard.setVisibility(View.GONE);
        }

        // Students get access to connection management in the menu
        invalidateOptionsMenu();
    }

    /**
     * Create a FAB programmatically if it doesn't exist in layout
     */
    private void createRoleFAB() {
        if (rootCoordinator == null) return;

        fabRoleDashboard = new FloatingActionButton(this);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
            CoordinatorLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        params.setMargins(0, 0, 48, 48); // 48dp margin from bottom and end
        fabRoleDashboard.setLayoutParams(params);
        fabRoleDashboard.setId(View.generateViewId());

        rootCoordinator.addView(fabRoleDashboard);
    }

    /**
     * Add menu items based on user role
     */
    private void addRoleMenuItems(Menu menu, UserRole role) {
        if (role == UserRole.TEACHER) {
            // Check if item already exists before adding
            if (menu.findItem(R.id.menu_my_students) == null) {
                menu.add(0, R.id.menu_my_students, 100, getString(R.string.main_menu_my_students))
                    .setIcon(android.R.drawable.ic_menu_agenda)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        } else if (role == UserRole.PARENT) {
            // Check if item already exists before adding
            if (menu.findItem(R.id.menu_my_children) == null) {
                menu.add(0, R.id.menu_my_children, 100, getString(R.string.main_menu_my_children))
                    .setIcon(android.R.drawable.ic_menu_agenda)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        } else {
            // Student - add connection management
            // Check if item already exists before adding
            if (menu.findItem(R.id.menu_connections) == null) {
                menu.add(0, R.id.menu_connections, 100, getString(R.string.main_menu_connections))
                    .setIcon(android.R.drawable.ic_menu_add)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
    }
}
