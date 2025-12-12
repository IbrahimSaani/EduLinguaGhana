package com.edulinguaghana;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
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
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.NestedScrollView;

import com.airbnb.lottie.LottieAnimationView;
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
    private LottieAnimationView lottieAnimationView;
    private NestedScrollView nestedScrollView;
    private ObjectAnimator overlayPulseAnimator;
    private Animator heroGlowAnimator;
    private ImageView starTopLeft, starTopRight;
    private Animator starLeftAnimator;
    private Animator starRightAnimator;
    private ImageView bubbleTop, bubbleMidRight, bubbleBottomLeft;
    private Animator bubbleTopAnimator, bubbleMidAnimator, bubbleBottomAnimator;
    private static final String KEY_ANIMATIONS_ENABLED = "ANIMATIONS_ENABLED";

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_SFX_ENABLED = "SFX_ENABLED";

    private static final String KEY_LAST_LANG_CODE = "LAST_LANG_CODE";
    private static final String KEY_LAST_LANG_NAME = "LAST_LANG_NAME";
    private static final String KEY_SEEN_INTRO = "SEEN_INTRO";

    private String selectedLangCode = null;
    private String selectedLangName = null;

    private String[] langNames = {"English", "French", "Twi", "Ewe", "Ga"};
    private String[] langCodes = {"en", "fr", "ak", "ee", "gaa"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
        }

        rootCoordinator = findViewById(R.id.rootCoordinator);
        dynamicBackgroundOverlay = findViewById(R.id.dynamicBackgroundOverlay);
        heroCard = findViewById(R.id.heroCard);
        starTopLeft = findViewById(R.id.starTopLeft);
        starTopRight = findViewById(R.id.starTopRight);
        bubbleTop = findViewById(R.id.bubbleTop);
        bubbleMidRight = findViewById(R.id.bubbleMidRight);
        bubbleBottomLeft = findViewById(R.id.bubbleBottomLeft);
        languageChipGroup = findViewById(R.id.languageChipGroup);
        btnRecitalMode = findViewById(R.id.btnRecitalMode);
        btnPracticeMode = findViewById(R.id.btnPracticeMode);
        btnQuizMode = findViewById(R.id.btnQuizMode);
        btnProgressMode = findViewById(R.id.btnProgressMode);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);
        nestedScrollView = findViewById(R.id.nestedScrollView);

        setupDynamicBackground();
        setupAnimation();
        setupHeroGlow();
        setupStarAnimations();
        setupBubbleAnimations();
        setupLanguageChips();
        restoreLastLanguageSelection();
        setupButtons();
        setupScrollAnimations();
        setupBackHandler();
        showIntroIfFirstTime();
    }

    private void setupAnimation() {
        AnimatorSet pulse = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.pulse);
        pulse.setTarget(lottieAnimationView);
        if (animationsEnabled()) {
            pulse.start();
        }

        lottieAnimationView.setOnClickListener(v -> {
            vibrate();
            if (lottieAnimationView.isAnimating()) {
                lottieAnimationView.pauseAnimation();
            } else {
                lottieAnimationView.resumeAnimation();
            }
        });
    }

    private void setupHeroGlow() {
        try {
            heroGlowAnimator = AnimatorInflater.loadAnimator(this, R.animator.hero_glow);
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
            starLeftAnimator = AnimatorInflater.loadAnimator(this, R.animator.star_twinkle);
            starRightAnimator = AnimatorInflater.loadAnimator(this, R.animator.star_slow_orbit);

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
            bubbleTopAnimator = AnimatorInflater.loadAnimator(this, R.animator.bubble_float);
            bubbleMidAnimator = AnimatorInflater.loadAnimator(this, R.animator.bubble_float);
            bubbleBottomAnimator = AnimatorInflater.loadAnimator(this, R.animator.bubble_float);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        // previously read high score to show in removed UI; keep prefs access in case other features rely on it
        applyDynamicBackground();
        if (animationsEnabled()) {
            startOverlayPulse();
            if (heroGlowAnimator != null && !((Animator)heroGlowAnimator).isStarted()) {
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
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean sfxOn = prefs.getBoolean(KEY_SFX_ENABLED, true);

        if (sfxOn) {
            try {
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.app_exit);
                if (mp != null) {
                    mp.setOnCompletionListener(MediaPlayer::release);
                    mp.start();
                }
            } catch (Exception e) {
                // ignore sound errors
            }
        }

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
            chip.setChipIcon(getDrawable(android.R.drawable.ic_btn_speak_now));

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
}
