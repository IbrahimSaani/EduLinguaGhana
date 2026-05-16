package com.edulinguaghana.games.rocketsort;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.edulinguaghana.R;
import com.edulinguaghana.utils.LanguageConversionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class RocketSortActivity extends AppCompatActivity {

    private FrameLayout starFieldContainer, asteroidContainer;
    private TextView tvScore, tvLives;
    private View overlayLayout;
    private ImageView ivRocketLeft, ivRocketRight;
    private KonfettiView konfettiView;

    private int score = 0;
    private int lives = 3;
    private boolean isGameOver = false;
    private boolean isPaused = false;

    private String languageCode;
    private String[] alphabet;
    private String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private Random random = new Random();
    private Handler spawnHandler = new Handler(Looper.getMainLooper());
    private Runnable spawnRunnable;
    private List<View> activeAsteroids = new ArrayList<>();

    private MediaPlayer correctPlayer;
    private MediaPlayer wrongPlayer;

    private float currentSpeed = 4000f; 
    private float spawnInterval = 2500f;
    private final float MIN_SPEED = 1500f;
    private final float MIN_SPAWN_INTERVAL = 800f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rocket_sort);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        if (languageCode == null) languageCode = "en";
        alphabet = LanguageConversionUtils.getAlphabetForLanguage(languageCode);

        initViews();
        initSounds();
        setupStarField();
        
        startNewGame();
    }

    private void initViews() {
        starFieldContainer = findViewById(R.id.starFieldContainer);
        asteroidContainer = findViewById(R.id.asteroidContainer);
        tvScore = findViewById(R.id.tvScore);
        tvLives = findViewById(R.id.tvLives);
        overlayLayout = findViewById(R.id.overlayLayout);
        ivRocketLeft = findViewById(R.id.ivRocketLeft);
        ivRocketRight = findViewById(R.id.ivRocketRight);
        konfettiView = findViewById(R.id.konfettiView);

        findViewById(R.id.btnPause).setOnClickListener(v -> togglePause());
        findViewById(R.id.btnRestart).setOnClickListener(v -> startNewGame());
        findViewById(R.id.btnQuit).setOnClickListener(v -> finish());
    }

    private void initSounds() {
        correctPlayer = MediaPlayer.create(this, R.raw.correct);
        wrongPlayer = MediaPlayer.create(this, R.raw.wrong);
    }

    private void setupStarField() {
        for (int i = 0; i < 50; i++) {
            View star = new View(this);
            int size = random.nextInt(5) + 2;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            star.setLayoutParams(params);
            star.setBackgroundColor(Color.WHITE);
            star.setAlpha(random.nextFloat() * 0.8f + 0.2f);
            
            starFieldContainer.addView(star);
            animateStar(star, true);
        }
    }

    private void animateStar(View star, boolean initial) {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        
        float startY = initial ? random.nextInt(height) : -20;
        float endY = height + 20;
        
        star.setTranslationX(random.nextInt(width));
        star.setTranslationY(startY);
        
        long duration = 3000 + random.nextInt(7000);
        ObjectAnimator animator = ObjectAnimator.ofFloat(star, View.TRANSLATION_Y, startY, endY);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isGameOver) animateStar(star, false);
            }
        });
        animator.start();
    }

    private void startNewGame() {
        isGameOver = false;
        isPaused = false;
        score = 0;
        lives = 3;
        currentSpeed = 4000f;
        spawnInterval = 2500f;
        
        updateUI();
        overlayLayout.setVisibility(View.GONE);
        
        for (View a : activeAsteroids) {
            asteroidContainer.removeView(a);
        }
        activeAsteroids.clear();

        startSpawning();
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            spawnHandler.removeCallbacks(spawnRunnable);
            // In a real game, we'd pause all animations too
            showOverlay("Paused");
        } else {
            overlayLayout.setVisibility(View.GONE);
            startSpawning();
        }
    }

    private void startSpawning() {
        if (spawnRunnable != null) spawnHandler.removeCallbacks(spawnRunnable);
        
        spawnRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isGameOver && !isPaused) {
                    spawnAsteroid();
                    spawnHandler.postDelayed(this, (long) spawnInterval);
                }
            }
        };
        spawnHandler.postDelayed(spawnRunnable, 1000);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void spawnAsteroid() {
        boolean isLetter = random.nextBoolean();
        String content = isLetter ? alphabet[random.nextInt(alphabet.length)] : numbers[random.nextInt(numbers.length)];
        
        int size = (int) (getResources().getDisplayMetrics().density * 80);
        final FrameLayout wrapper = new FrameLayout(this);
        wrapper.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        
        // Asteroid Shape
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.parseColor("#8D6E63"));
        shape.setStroke(4, Color.parseColor("#5D4037"));
        wrapper.setBackground(shape);
        
        TextView tv = new TextView(this);
        tv.setText(content);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(24);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        wrapper.addView(tv);
        
        asteroidContainer.addView(wrapper);
        activeAsteroids.add(wrapper);
        
        int width = asteroidContainer.getWidth();
        if (width == 0) width = getResources().getDisplayMetrics().widthPixels;
        
        wrapper.setTranslationX(random.nextInt(width - size));
        wrapper.setTranslationY(-size);
        
        ObjectAnimator fallAnimator = ObjectAnimator.ofFloat(wrapper, View.TRANSLATION_Y, -size, asteroidContainer.getHeight());
        fallAnimator.setDuration((long) currentSpeed);
        fallAnimator.setInterpolator(new LinearInterpolator());
        
        final boolean[] isHandled = {false};

        fallAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isHandled[0] && asteroidContainer.indexOfChild(wrapper) != -1) {
                    handleMiss();
                    removeAsteroid(wrapper);
                }
            }
        });
        fallAnimator.start();
        
        // Interaction Logic: Drag to Sort
        wrapper.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isGameOver || isPaused || isHandled[0]) return false;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        fallAnimator.pause();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;
                        
                        // Keep within horizontal bounds
                        newX = Math.max(0, Math.min(newX, asteroidContainer.getWidth() - v.getWidth()));
                        
                        v.setX(newX);
                        v.setY(newY);
                        
                        // Check if dragged into rocket zones (bottom corners)
                        checkRocketCollision(v, isLetter, isHandled, fallAnimator);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (!isHandled[0]) {
                            fallAnimator.resume();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void checkRocketCollision(View v, boolean isLetter, boolean[] isHandled, ObjectAnimator fallAnimator) {
        float centerX = v.getX() + v.getWidth() / 2f;
        float bottomY = v.getY() + v.getHeight();
        float containerWidth = asteroidContainer.getWidth();
        float containerHeight = asteroidContainer.getHeight();
        
        // Rocket trigger zone: Bottom 15% of screen
        if (bottomY > containerHeight * 0.8f) {
            boolean inLeftZone = centerX < containerWidth * 0.4f;
            boolean inRightZone = centerX > containerWidth * 0.6f;
            
            if (inLeftZone || inRightZone) {
                isHandled[0] = true;
                fallAnimator.cancel();
                
                boolean correct = (inLeftZone == isLetter);
                sortAsteroid(v, correct, inLeftZone);
            }
        }
    }

    private void sortAsteroid(View asteroid, boolean correct, boolean leftRocket) {
        float targetX = leftRocket ? 0 : asteroidContainer.getWidth() - asteroid.getWidth();
        float targetY = asteroidContainer.getHeight() + 100;
        
        asteroid.animate()
                .translationX(targetX)
                .translationY(targetY)
                .scaleX(0.2f)
                .scaleY(0.2f)
                .alpha(0.5f)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (correct) {
                            handleCorrectSort(leftRocket);
                        } else {
                            handleWrongSort(asteroid);
                        }
                        removeAsteroid(asteroid);
                    }
                });
    }

    private void handleCorrectSort(boolean leftRocket) {
        if (isGameOver || isFinishing() || isDestroyed()) return;
        score++;
        correctPlayer.start();
        updateUI();
        
        ImageView rocket = leftRocket ? ivRocketLeft : ivRocketRight;
        rocket.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() -> 
            rocket.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
        ).start();
        
        if (score % 5 == 0) {
            currentSpeed = Math.max(MIN_SPEED, currentSpeed - 200);
            spawnInterval = Math.max(MIN_SPAWN_INTERVAL, spawnInterval - 150);
        }
        
        if (score % 10 == 0) celebrate();
    }

    private void handleWrongSort(View asteroid) {
        if (isGameOver || isFinishing() || isDestroyed()) return;
        lives--;
        wrongPlayer.start();
        updateUI();
        
        // Shake the whole screen or something
        asteroidContainer.animate().translationX(20).setDuration(50).withEndAction(() ->
            asteroidContainer.animate().translationX(-20).setDuration(50).withEndAction(() ->
                asteroidContainer.animate().translationX(0).setDuration(50).start()
            ).start()
        ).start();
        
        if (lives <= 0) endGame();
    }

    private void handleMiss() {
        if (isGameOver || isFinishing() || isDestroyed()) return;
        lives--;
        wrongPlayer.start();
        updateUI();
        if (lives <= 0) endGame();
    }

    private void removeAsteroid(View asteroid) {
        asteroidContainer.removeView(asteroid);
        activeAsteroids.remove(asteroid);
    }

    private void updateUI() {
        if (isFinishing() || isDestroyed()) return;
        tvScore.setText("⭐ " + score);
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < lives; i++) hearts.append("❤️ ");
        tvLives.setText(hearts.toString().trim());
    }

    private void endGame() {
        isGameOver = true;
        spawnHandler.removeCallbacks(spawnRunnable);
        showOverlay("Mission Complete!");
    }

    private void showOverlay(String title) {
        if (isFinishing() || isDestroyed()) return;
        overlayLayout.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tvOverlayTitle)).setText(title);
        ((TextView) findViewById(R.id.tvOverlayScore)).setText("Final Score: " + score);
    }

    private void celebrate() {
        konfettiView.start(new PartyFactory(new Emitter(100L, java.util.concurrent.TimeUnit.MILLISECONDS).max(30))
                .position(new Position.Relative(0.5, 0.3))
                .spread(360)
                .colors(java.util.Arrays.asList(0xfce18a, 0xff726d, 0xf48fb1, 0xafdfff))
                .build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        spawnHandler.removeCallbacks(spawnRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (correctPlayer != null) correctPlayer.release();
        if (wrongPlayer != null) wrongPlayer.release();
    }
}
