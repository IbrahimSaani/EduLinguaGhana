package com.edulinguaghana.games.bubblepop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.edulinguaghana.DynamicBackgroundView;
import com.edulinguaghana.R;
import com.edulinguaghana.StyledMenuHelper;
import com.edulinguaghana.tts.OfflineGhanaLPTtsService;
import com.edulinguaghana.utils.LanguageConversionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.xml.KonfettiView;

/**
 * Native implementation of Bubble Pop game to replace LibGDX version.
 * This ensures stability across all devices (no native binaries required).
 */
public class BubblePopActivity extends AppCompatActivity {

    private FrameLayout bubbleContainer, ambientParticles;
    private TextView tvScore, tvTarget;
    private View overlayLayout;
    private DynamicBackgroundView dynamicBackground;
    private KonfettiView konfettiView;

    private String languageCode;
    private String languageName;
    private String[] alphabet;
    private String targetLetter;
    private int score = 0;
    private boolean isGameOver = false;
    private int decoysSinceLastTarget = 0;

    private Random random = new Random();
    private Handler spawnHandler = new Handler(Looper.getMainLooper());
    private Runnable spawnRunnable;
    private List<View> activeBubbles = new ArrayList<>();

    private TextToSpeech tts;
    private OfflineGhanaLPTtsService offlineTts;
    private boolean isTtsReady = false;
    private MediaPlayer correctPlayer;
    private MediaPlayer wrongPlayer;

    private float currentSpeed = 3500f; // Duration in ms for a bubble to cross screen (Faster start)
    private final float MIN_SPEED = 1200f; // Lower minimum duration (Higher max speed)
    private final float SPEED_INCREMENT = 350f; // More aggressive speed increase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble_pop_native);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");
        if (languageCode == null) languageCode = "en";
        
        alphabet = LanguageConversionUtils.getAlphabetForLanguage(languageCode);

        initViews();
        initSounds();
        initTts();
        
        startNewGame();
    }

    private void initViews() {
        bubbleContainer = findViewById(R.id.bubbleContainer);
        ambientParticles = findViewById(R.id.ambientParticles);
        tvScore = findViewById(R.id.tvScore);
        tvTarget = findViewById(R.id.tvTarget);
        overlayLayout = findViewById(R.id.overlayLayout);
        dynamicBackground = findViewById(R.id.dynamicBackground);
        konfettiView = findViewById(R.id.konfettiView);

        findViewById(R.id.btnRestart).setOnClickListener(v -> startNewGame());
        findViewById(R.id.btnQuit).setOnClickListener(v -> finish());

        if (dynamicBackground != null) {
            dynamicBackground.setColors(
                ContextCompat.getColor(this, R.color.bgDayStart),
                ContextCompat.getColor(this, R.color.bgDayMid),
                ContextCompat.getColor(this, R.color.bgDayEnd)
            );
        }

        setupAmbientBackground();
    }

    private void setupAmbientBackground() {
        if (ambientParticles == null) return;
        // Add some fixed decorative elements
        for (int i = 0; i < 8; i++) {
            View particle = new View(this);
            int size = (int) (40 * getResources().getDisplayMetrics().density);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.leftMargin = random.nextInt(800);
            params.topMargin = random.nextInt(1200);
            particle.setLayoutParams(params);
            
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(Color.WHITE);
            gd.setAlpha(30);
            particle.setBackground(gd);
            
            ambientParticles.addView(particle);
            
            animateParticle(particle);
        }
    }

    private void animateParticle(View p) {
        p.animate()
            .translationYBy(random.nextBoolean() ? 200 : -200)
            .translationXBy(random.nextBoolean() ? 200 : -200)
            .setDuration(6000 + random.nextInt(4000))
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isGameOver) animateParticle(p);
                }
            }).start();
    }

    private void initTts() {
        if (LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
            offlineTts = new OfflineGhanaLPTtsService(this);
        }
        
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(LanguageConversionUtils.getLocaleForLanguage(languageCode));
                isTtsReady = true;
                // Speak first target once ready
                speakTarget(targetLetter);
            }
        });
    }

    private void initSounds() {
        correctPlayer = MediaPlayer.create(this, R.raw.correct);
        wrongPlayer = MediaPlayer.create(this, R.raw.wrong);
    }

    private void startNewGame() {
        isGameOver = false;
        score = 0;
        currentSpeed = 4000f;
        updateScoreDisplay();
        overlayLayout.setVisibility(View.GONE);
        
        // Clear existing bubbles
        for (View b : activeBubbles) {
            bubbleContainer.removeView(b);
        }
        activeBubbles.clear();

        generateNewTarget();
        startSpawning();
    }

    @Override
    public void onBackPressed() {
        if (!isGameOver) {
            isGameOver = true;
            overlayLayout.setVisibility(View.VISIBLE);
            TextView title = findViewById(R.id.tvOverlayTitle);
            title.setText("Paused");
            TextView scoreText = findViewById(R.id.tvOverlayScore);
            scoreText.setText("Current Score: " + score);
        } else {
            super.onBackPressed();
        }
    }

    private void generateNewTarget() {
        targetLetter = alphabet[random.nextInt(alphabet.length)];
        tvTarget.setText(targetLetter);

        // Scale animation for new target
        tvTarget.setScaleX(0.5f);
        tvTarget.setScaleY(0.5f);
        tvTarget.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start();

        speakTarget(targetLetter);
    }

    private void speakTarget(String text) {
        if (LanguageConversionUtils.isGhanaianLanguage(languageCode) && offlineTts != null) {
            offlineTts.speak(text, languageCode, null);
        } else if (isTtsReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bubble_pop");
        }
    }

    private void startSpawning() {
        if (spawnRunnable != null) spawnHandler.removeCallbacks(spawnRunnable);
        
        spawnRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isGameOver) {
                    spawnBubble();
                    // Spawn interval gets shorter as speed increases
                    long interval = (long) (currentSpeed / 2.5f);
                    spawnHandler.postDelayed(this, Math.max(800, interval));
                }
            }
        };
        
        // Use a simpler approach: delay by 1 second to ensure layout
        spawnHandler.postDelayed(spawnRunnable, 1000);
    }

    private void spawnBubble() {
        if (bubbleContainer.getWidth() == 0 || bubbleContainer.getHeight() == 0) return;

        // Force a target if too many decoys have passed
        float targetChance = 0.35f + (decoysSinceLastTarget * 0.1f);
        final boolean isTarget = random.nextFloat() < targetChance;
        
        String letter;
        if (isTarget) {
            letter = targetLetter;
            decoysSinceLastTarget = 0;
        } else {
            decoysSinceLastTarget++;
            // Ensure decoy is NOT the target letter
            int attempts = 0;
            do {
                letter = alphabet[random.nextInt(alphabet.length)];
                attempts++;
            } while (letter.equals(targetLetter) && attempts < 10);
        }
        
        // Random bubble size
        int size = (int) (getResources().getDisplayMetrics().density * (75 + random.nextInt(25)));
        if (isTarget) size = (int) (size * 1.15f); // Make target bubbles 15% larger to help visibility
        
        final TextView bubble = new TextView(this);
        bubble.setText(letter);
        bubble.setTextSize(34); // Slightly larger
        bubble.setTextColor(Color.WHITE);
        bubble.setGravity(Gravity.CENTER);
        bubble.setTypeface(null, android.graphics.Typeface.BOLD);
        bubble.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        
        // Bubble colors
        int[] bubbleColors = {0xFF4FC3F7, 0xFFF06292, 0xFFBA68C8, 0xFF4DB6AC, 0xFFFFD54F};
        
        // Gradient background for bubble
        int bubbleColor = bubbleColors[random.nextInt(bubbleColors.length)];
        GradientDrawable shape = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[] { bubbleColor, 0xCCFFFFFF }
        );
        shape.setShape(GradientDrawable.OVAL);
        shape.setCornerRadius(size / 2f);
        
        // HIGHLIGHT TARGET: Thicker, gold border for targets to help user "see" them better
        if (isTarget) {
            shape.setStroke(10, 0xFFFFD700); // Thicker Gold stroke
        } else {
            shape.setStroke(4, Color.WHITE);
        }
        bubble.setBackground(shape);

        // Add a "shimmer" effect
        View highlight = new View(this);
        int hlSize = size / 3;
        FrameLayout.LayoutParams hlParams = new FrameLayout.LayoutParams(hlSize, hlSize);
        hlParams.leftMargin = size / 5;
        hlParams.topMargin = size / 5;
        highlight.setLayoutParams(hlParams);
        
        GradientDrawable hlShape = new GradientDrawable();
        hlShape.setShape(GradientDrawable.OVAL);
        hlShape.setColor(Color.WHITE);
        hlShape.setAlpha(120);
        highlight.setBackground(hlShape);
        
        // Wrapper for bubble + highlight
        FrameLayout wrapper = new FrameLayout(this);
        wrapper.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        wrapper.addView(bubble);
        wrapper.addView(highlight);

        bubble.setOnClickListener(v -> handleBubbleClick(bubble, isTarget));

        bubbleContainer.addView(wrapper);
        activeBubbles.add(wrapper);

        // SAFE ZONE CALCULATION
        float density = getResources().getDisplayMetrics().density;
        int swayRange = (int) (20 * density); // Reduced sway slightly for easier visibility/hitting
        int containerWidth = bubbleContainer.getWidth();
        
        int maxX = containerWidth - size - (swayRange * 2);
        
        float startX;
        if (maxX > 0) {
            startX = swayRange + random.nextInt(maxX);
        } else {
            startX = (containerWidth - size) / 2f;
        }
        
        float startY = bubbleContainer.getHeight();
        float endY = -size - 150; // Go further up to ensure it clears the screen completely
        
        wrapper.setTranslationX(startX);
        wrapper.setTranslationY(startY);
        
        // Rising animation
        ObjectAnimator animator = ObjectAnimator.ofFloat(wrapper, View.TRANSLATION_Y, startY, endY);
        animator.setDuration((long) (currentSpeed + random.nextInt(1000)));
        animator.setInterpolator(new LinearInterpolator());
        
        // FIXED SWAY: Animate around startX smoothly without jumping at start
        ObjectAnimator sway = ObjectAnimator.ofFloat(wrapper, View.TRANSLATION_X, startX, startX + swayRange, startX - swayRange, startX);
        sway.setDuration(2000 + random.nextInt(1000));
        sway.setRepeatCount(ValueAnimator.INFINITE);
        sway.setInterpolator(new AccelerateDecelerateInterpolator());
        sway.start();

        final String finalLetter = letter;
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                sway.cancel();
                if (bubbleContainer.indexOfChild(wrapper) != -1) {
                    // Only show "escaped" if it's the CURRENT target to avoid confusing the user
                    // with old targets that were replaced after a successful pop.
                    if (isTarget && !isGameOver && finalLetter.equals(targetLetter)) {
                        Toast.makeText(BubblePopActivity.this, R.string.bubble_pop_escaped, Toast.LENGTH_SHORT).show();
                    }
                    removeBubble(wrapper);
                }
            }
        });
        animator.start();
    }

    private void handleBubbleClick(TextView bubble, boolean isTarget) {
        if (isGameOver || bubble.getAlpha() < 1.0f) return;

        if (isTarget) {
            popBubble(bubble, true);
            score++;
            updateScoreDisplay();
            
            if (score % 5 == 0) {
                currentSpeed = Math.max(MIN_SPEED, currentSpeed - SPEED_INCREMENT);
                Toast.makeText(this, R.string.bubble_pop_speed_up, Toast.LENGTH_SHORT).show();
            }
            
            generateNewTarget();
            celebrate();
        } else {
            // Wrong bubble: Shake and sound
            wrongPlayer.start();
            ObjectAnimator shake = ObjectAnimator.ofFloat(bubble, View.TRANSLATION_X, bubble.getTranslationX(), bubble.getTranslationX() + 25f);
            shake.setDuration(100);
            shake.setRepeatCount(3);
            shake.setRepeatMode(ValueAnimator.REVERSE);
            shake.start();
        }
    }

    private void popBubble(TextView bubble, boolean isCorrect) {
        if (isCorrect) correctPlayer.start();
        
        // Animation: Scale up and fade out
        bubble.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .alpha(0f)
            .setDuration(200)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    removeBubble(bubble);
                }
            });
    }

    private void removeBubble(View wrapper) {
        bubbleContainer.removeView(wrapper);
        activeBubbles.remove(wrapper);
    }

    private void updateScoreDisplay() {
        tvScore.setText("⭐ Score: " + score);
    }

    private void celebrate() {
        if (konfettiView == null) return;
        konfettiView.start(new PartyFactory(new Emitter(100L, java.util.concurrent.TimeUnit.MILLISECONDS).max(30))
                .position(new Position.Relative(0.5, 0.3))
                .spread(360)
                .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf48fb1, 0xafdfff))
                .build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        isGameOver = true;
        spawnHandler.removeCallbacks(spawnRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (offlineTts != null) {
            offlineTts.release();
        }
        if (correctPlayer != null) correctPlayer.release();
        if (wrongPlayer != null) wrongPlayer.release();
    }
}
