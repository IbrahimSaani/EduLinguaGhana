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

    private FrameLayout bubbleContainer;
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

    private Random random = new Random();
    private Handler spawnHandler = new Handler(Looper.getMainLooper());
    private Runnable spawnRunnable;
    private List<View> activeBubbles = new ArrayList<>();

    private TextToSpeech tts;
    private OfflineGhanaLPTtsService offlineTts;
    private boolean isTtsReady = false;
    private MediaPlayer correctPlayer;
    private MediaPlayer wrongPlayer;

    private float currentSpeed = 4000f; // Duration in ms for a bubble to cross screen
    private final float MIN_SPEED = 1500f;
    private final float SPEED_INCREMENT = 150f;

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

    private void generateNewTarget() {
        targetLetter = alphabet[random.nextInt(alphabet.length)];
        tvTarget.setText(getString(R.string.bubble_pop_target, targetLetter));
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
        spawnHandler.post(spawnRunnable);
    }

    private void spawnBubble() {
        final boolean isTarget = random.nextFloat() < 0.3f;
        String letter = isTarget ? targetLetter : alphabet[random.nextInt(alphabet.length)];
        
        final TextView bubble = new TextView(this);
        bubble.setText(letter);
        bubble.setTextSize(28);
        bubble.setTextColor(Color.WHITE);
        bubble.setGravity(Gravity.CENTER);
        bubble.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // Random bubble size
        int size = (int) (getResources().getDisplayMetrics().density * (60 + random.nextInt(30)));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        
        // Random horizontal position
        int maxX = bubbleContainer.getWidth() - size;
        if (maxX <= 0) maxX = 500; // Fallback for early calls
        params.leftMargin = random.nextInt(maxX);
        params.topMargin = bubbleContainer.getHeight(); // Start at bottom
        
        bubble.setLayoutParams(params);
        
        // Circle background
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        int[] bubbleColors = {0xFF4FC3F7, 0xFFF06292, 0xFFBA68C8, 0xFF4DB6AC, 0xFFFFD54F};
        shape.setColor(bubbleColors[random.nextInt(bubbleColors.length)]);
        shape.setStroke(4, Color.WHITE);
        shape.setAlpha(180);
        bubble.setBackground(shape);

        bubble.setOnClickListener(v -> handleBubbleClick(bubble, isTarget));

        bubbleContainer.addView(bubble);
        activeBubbles.add(bubble);

        // Animate bubble rising
        ObjectAnimator animator = ObjectAnimator.ofFloat(bubble, View.TRANSLATION_Y, 0, -bubbleContainer.getHeight() - size);
        animator.setDuration((long) (currentSpeed + random.nextInt(1000)));
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (bubbleContainer.indexOfChild(bubble) != -1) {
                    if (isTarget && !isGameOver) {
                        // Target escaped!
                        Toast.makeText(BubblePopActivity.this, R.string.bubble_pop_escaped, Toast.LENGTH_SHORT).show();
                    }
                    removeBubble(bubble);
                }
            }
        });
        animator.start();
    }

    private void handleBubbleClick(TextView bubble, boolean isTarget) {
        if (isGameOver) return;

        if (isTarget) {
            popBubble(bubble, true);
            score++;
            updateScoreDisplay();
            
            if (score % 5 == 0) {
                currentSpeed = Math.max(MIN_SPEED, currentSpeed - SPEED_INCREMENT);
            }
            
            generateNewTarget();
            celebrate();
        } else {
            // Wrong bubble: Shake and sound
            wrongPlayer.start();
            ObjectAnimator shake = ObjectAnimator.ofFloat(bubble, View.TRANSLATION_X, bubble.getTranslationX(), bubble.getTranslationX() + 20f);
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

    private void removeBubble(View bubble) {
        bubbleContainer.removeView(bubble);
        activeBubbles.remove(bubble);
    }

    private void updateScoreDisplay() {
        tvScore.setText(getString(R.string.bubble_pop_score, score));
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
