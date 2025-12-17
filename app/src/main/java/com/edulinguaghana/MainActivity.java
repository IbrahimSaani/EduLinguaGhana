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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.NestedScrollView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout rootCoordinator;
    private ImageView dynamicBackgroundOverlay;
    private ChipGroup languageChipGroup;
    private MaterialCardView btnRecitalMode, btnPracticeMode, btnQuizMode, btnProgressMode;
    private MaterialCardView heroCard;
    private ImageView mascotView;
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
    private android.widget.TextView tvTotalQuizzes;
    private android.widget.TextView tvAccuracy;
    private android.widget.TextView tvAchievements;
    private android.widget.TextView notificationBadge;
    private android.view.View offlineBanner;
    private BottomNavigationView bottomNavigation;
    private static final String KEY_ANIMATIONS_ENABLED = "ANIMATIONS_ENABLED";
    private static final String KEY_LOW_POWER_ANIMATIONS = "LOW_POWER_ANIMATIONS";

    private static final String PREF_NAME = "EduLinguaPrefs";

    private static final String KEY_LAST_LANG_CODE = "LAST_LANG_CODE";
    private static final String KEY_LAST_LANG_NAME = "LAST_LANG_NAME";
    private static final String KEY_SEEN_INTRO = "SEEN_INTRO";

    private String selectedLangCode = null;
    private String selectedLangName = null;

    private String[] langNames = {"English", "French", "Twi", "Ewe", "Ga"};
    private String[] langCodes = {"en", "fr", "ak", "ee", "gaa"};
    private int[] langFlags = {R.drawable.ic_flag_uk, R.drawable.ic_flag_france, R.drawable.ic_flag_ghana, R.drawable.ic_flag_ghana, R.drawable.ic_flag_ghana};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Apply custom font to toolbar title
        applyToolbarFont(toolbar);

        // Animate toolbar entrance
        animateToolbar(toolbar);

        rootCoordinator = findViewById(R.id.rootCoordinator);
        dynamicBackgroundOverlay = findViewById(R.id.dynamicBackgroundOverlay);
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
        tvTotalQuizzes = findViewById(R.id.tvTotalQuizzes);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvAchievements = findViewById(R.id.tvAchievements);
        offlineBanner = findViewById(R.id.offlineBanner);
        languageChipGroup = findViewById(R.id.languageChipGroup);
        btnRecitalMode = findViewById(R.id.btnRecitalMode);
        btnPracticeMode = findViewById(R.id.btnPracticeMode);
        btnQuizMode = findViewById(R.id.btnQuizMode);
        btnProgressMode = findViewById(R.id.btnProgressMode);
        mascotView = findViewById(R.id.mascotView);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        setupDynamicBackground();
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
    }

    private void setupOfflineIndicator() {
        if (offlineBanner == null) return;

        OfflineManager offlineManager = new OfflineManager(this);
        if (offlineManager.isOnline()) {
            offlineBanner.setVisibility(View.GONE);
        } else {
            offlineBanner.setVisibility(View.VISIBLE);
        }
    }

    private void setupQuickStats() {
        // Update Total Quizzes
        if (tvTotalQuizzes != null) {
            int totalQuizzes = ProgressManager.getTotalQuizzes(this);
            tvTotalQuizzes.setText(String.valueOf(totalQuizzes));
        }

        // Update Accuracy
        if (tvAccuracy != null) {
            int accuracy = ProgressManager.getAccuracy(this);
            tvAccuracy.setText(accuracy + "%");
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
    }

    private void openAchievementsScreen() {
        OfflineManager offlineManager = new OfflineManager(this);

        // Check if user is logged in
        if (!offlineManager.isLoggedIn()) {
            new AlertDialog.Builder(this)
                .setTitle("Login Required 🔒")
                .setMessage(offlineManager.getLoginRequiredMessage("Achievements"))
                .setPositiveButton("Sign In", (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
            return;
        }

        // Check internet connection
        if (!offlineManager.isOnline()) {
            new AlertDialog.Builder(this)
                .setTitle("Internet Required 📶")
                .setMessage("Achievements require an internet connection. Please connect and try again.")
                .setPositiveButton("OK", null)
                .show();
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
                "Daily Practice Tip 💡",
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

        // Start idle animation
        startMascotIdleAnimation();

        // Set up interactive click listener
        mascotView.setOnClickListener(v -> {
            onMascotClicked();
        });

        // Set up long click for special animation
        mascotView.setOnLongClickListener(v -> {
            playMascotCelebration();
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
                showMascotMessage("Yay! 🎉");
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
        // Setup daily motivational message
        setupDailyMotivation();

        // Setup learning streak
        setupLearningStreak();

        // Setup fun facts
        setupFunFacts();

        // Animate cards on entrance
        animateEnhancedCards();
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
            tvStreakCount.setText("🔥 Start today!");
        } else if (streak == 1) {
            tvStreakCount.setText("🔥 1 day");
        } else {
            tvStreakCount.setText("🔥 " + streak + " days");
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

        // Setup notification badge
        MenuItem notificationItem = menu.findItem(R.id.action_notifications);
        if (notificationItem != null) {
            View actionView = notificationItem.getActionView();
            if (actionView != null) {
                notificationBadge = actionView.findViewById(R.id.notification_badge);
                updateNotificationBadge();

                // Set click listener on the action view
                actionView.setOnClickListener(v -> openNotificationsScreen());
            }
        }

        return true;
    }

    private void updateNotificationBadge() {
        if (notificationBadge == null) return;

        NotificationManager notificationManager = new NotificationManager(this);
        int unreadCount = notificationManager.getUnreadCount();

        if (unreadCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(unreadCount > 9 ? "9+" : String.valueOf(unreadCount));
        } else {
            notificationBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        } catch (Exception e) {
            e.printStackTrace();
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
        updateNotificationBadge();

        // Check and unlock achievements
        AchievementManager achievementManager = new AchievementManager(this);
        achievementManager.checkAndUnlockAchievements();

        // previously read high score to show in removed UI; keep prefs access in case other features rely on it
        applyDynamicBackground();
        if (animationsEnabled()) {
            startOverlayPulse();
            if (heroGlowAnimator != null && !heroGlowAnimator.isStarted()) {
                heroGlowAnimator.start();
            }
            if (starLeftAnimator != null && !starLeftAnimator.isStarted()) starLeftAnimator.start();
            if (starRightAnimator != null && !starRightAnimator.isStarted()) starRightAnimator.start();
            if (bubbleTopAnimator != null && !bubbleTopAnimator.isStarted()) bubbleTopAnimator.start();
            if (bubbleMidAnimator != null && !bubbleMidAnimator.isStarted()) bubbleMidAnimator.start();
            if (bubbleBottomAnimator != null && !bubbleBottomAnimator.isStarted()) bubbleBottomAnimator.start();
        } else {
            // ensure overlay alpha is set to default when animations disabled
            if (dynamicBackgroundOverlay != null) dynamicBackgroundOverlay.setAlpha(0.45f);
        }
    }

    @Override
    protected void onPause() {
        stopOverlayPulse();
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
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Exit EduLingua Ghana?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    playAppExitSoundAndExit();
                })
                .setNegativeButton("No", null)
                .show();
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

        new AlertDialog.Builder(this)
                .setTitle("Welcome to EduLingua Ghana")
                .setMessage(
                        "• Recital Mode – listen to letters and numbers in your chosen language.\n\n" +
                                "• Practice Mode – repeat after the app and practice pronunciation.\n\n" +
                                "• Quiz Mode – answer fun questions and play Speed Challenge.\n\n" +
                                "• Progress Tracker – see your best score and learning stats."
                )
                .setPositiveButton("Got it", (d, w) -> {
                    SharedPreferences.Editor ed = prefs.edit();
                    ed.putBoolean(KEY_SEEN_INTRO, true);
                    ed.apply();
                })
                .show();
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
            Toast.makeText(this, "Please select a language first.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ---------------- BUTTON ACTIONS ----------------

    private void setupButtons() {
        AnimatorSet cardClickAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.card_click_animation);

        btnRecitalMode.setOnClickListener(v -> {
            cardClickAnimation.setTarget(v);
            cardClickAnimation.start();
            vibrate();
            if (!ensureLanguageSelected()) return;
            showContentTypeDialog(selectedLangCode, selectedLangName, "recital");
        });

        btnPracticeMode.setOnClickListener(v -> {
            cardClickAnimation.setTarget(v);
            cardClickAnimation.start();
            vibrate();
            if (!ensureLanguageSelected()) return;
            showContentTypeDialog(selectedLangCode, selectedLangName, "practice");
        });

        btnQuizMode.setOnClickListener(v -> {
            cardClickAnimation.setTarget(v);
            cardClickAnimation.start();
            vibrate();
            if (!ensureLanguageSelected()) return;
            showQuizTypeDialog(selectedLangCode, selectedLangName);
        });

        btnProgressMode.setOnClickListener(v -> {
            cardClickAnimation.setTarget(v);
            cardClickAnimation.start();
            vibrate();
            openProgressScreen();
        });
    }

    private void showContentTypeDialog(String langCode, String langName, String mode) {
        String[] options = {"Alphabet", "Numbers"};
        String title = (mode.equals("recital") ? "Recital Mode" : "Practice Mode") + " - " + langName;

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(R.mipmap.ic_launcher_round)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openAlphabetScreen(langCode, langName, mode);
                    } else {
                        openNumbersScreen(langCode, langName, mode);
                    }
                })
                .show();
    }

    // UPDATED: now includes "Speed Challenge (Game)"
    private void showQuizTypeDialog(String langCode, String langName) {
        String[] quizTypes = {
                "Letter/Number Quiz",
                "Number Sequencing",
                "Matching",
                "Mixed Mode",
                "Speed Challenge (Game)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Quiz Mode - " + langName)
                .setIcon(R.mipmap.ic_launcher_round)
                .setItems(quizTypes, (dialog, which) -> {
                    if (which == 4) {
                        // Open separate game screen
                        openSpeedGameScreen(langCode, langName);
                        return;
                    }

                    String quizType;
                    if (which == 0)      quizType = "basic";
                    else if (which == 1) quizType = "sequence";
                    else if (which == 2) quizType = "matching";
                    else                 quizType = "mixed";

                    openQuizScreen(langCode, langName, quizType);
                })
                .show();
    }

    private void openAlphabetScreen(String langCode, String langName, String mode) {
        Intent intent = new Intent(MainActivity.this, AlphabetActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        intent.putExtra("MODE", mode);
        startActivity(intent);
    }

    private void openNumbersScreen(String langCode, String langName, String mode) {
        Intent intent = new Intent(MainActivity.this, NumbersActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        intent.putExtra("MODE", mode);
        startActivity(intent);
    }

    private void openQuizScreen(String langCode, String langName, String quizType) {
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra("LANG_CODE", langCode);
        intent.putExtra("LANG_NAME", langName);
        intent.putExtra("QUIZ_TYPE", quizType);
        startActivity(intent);
    }

    private void openSpeedGameScreen(String langCode, String langName) {
        Intent intent = new Intent(MainActivity.this, SpeedGameActivity.class);
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
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {

                animateCard(btnRecitalMode, true);
                animateCard(btnPracticeMode, true);
                animateCard(btnQuizMode, true);
                animateCard(btnProgressMode, true);
            }
        });
    }

    private void setupDynamicBackground() {
        applyDynamicBackground();
        // overlay pulse is started from onResume() when animations are enabled
    }

    private void applyDynamicBackground() {
        if (rootCoordinator == null || dynamicBackgroundOverlay == null) return;

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int backgroundRes;
        int overlayTintRes;
        float targetAlpha;

        if (hour >= 5 && hour < 11) {
            backgroundRes = R.drawable.bg_main_morning;
            overlayTintRes = R.color.sparkleHalo;
            targetAlpha = 0.45f;
        } else if (hour >= 11 && hour < 17) {
            backgroundRes = R.drawable.bg_main_day;
            overlayTintRes = R.color.sparkleHaloSecondary;
            targetAlpha = 0.4f;
        } else {
            backgroundRes = R.drawable.bg_main_night;
            overlayTintRes = R.color.sparkleCore;
            targetAlpha = 0.6f;
        }

        rootCoordinator.setBackgroundResource(backgroundRes);
        dynamicBackgroundOverlay.setImageResource(R.drawable.bg_dynamic_sparkle);
        ImageViewCompat.setImageTintList(dynamicBackgroundOverlay,
                ColorStateList.valueOf(ContextCompat.getColor(this, overlayTintRes)));
        dynamicBackgroundOverlay.setAlpha(targetAlpha);
    }

    private void startOverlayPulse() {
        if (dynamicBackgroundOverlay == null) return;
        if (overlayPulseAnimator == null) {
            overlayPulseAnimator = ObjectAnimator.ofFloat(dynamicBackgroundOverlay, View.ALPHA, 0.35f, 0.65f);
            overlayPulseAnimator.setDuration(6000);
            overlayPulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
            overlayPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        }
        if (!overlayPulseAnimator.isStarted()) {
            overlayPulseAnimator.start();
        }
    }

    private void stopOverlayPulse() {
        if (overlayPulseAnimator != null && overlayPulseAnimator.isStarted()) {
            overlayPulseAnimator.cancel();
        }
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
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home, maybe scroll to top
                if (nestedScrollView != null) {
                    nestedScrollView.smoothScrollTo(0, 0);
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                openProfileScreen();
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_notifications) {
                openNotificationsScreen();
                return true;
            }
            return false;
        });
    }

    private void openProfileScreen() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openNotificationsScreen() {
        Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
        startActivity(intent);
    }
}
