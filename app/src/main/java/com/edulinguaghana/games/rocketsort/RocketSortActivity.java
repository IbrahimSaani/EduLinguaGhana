package com.edulinguaghana.games.rocketsort;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
    private TextView tvScore, tvLives, tvCountdown, tvTimer;
    private View overlayLayout;
    private ImageView ivRocketLeft, ivRocketRight;
    private KonfettiView konfettiView;
    private View rocketDock;

    private int score = 0;
    private int lives = 5;
    private boolean isGameOver = false;
    private boolean isPaused = false;

    private String languageCode;
    private String[] alphabet;
    private String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private Random random = new Random();
    private Handler spawnHandler = new Handler(Looper.getMainLooper());
    private Runnable spawnRunnable;
    private List<View> activeAsteroids = new ArrayList<>();
    private java.util.Map<View, ObjectAnimator> asteroidAnimators = new java.util.HashMap<>();

    private MediaPlayer correctPlayer;
    private MediaPlayer wrongPlayer;
    private MediaPlayer gameOverPlayer;
    private MediaPlayer levelUpPlayer;
    private MediaPlayer backgroundMusic;
    private int bestScore = 0;
    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE_ROCKET = "high_score_rocket_sort";

    // Challenge mode
    private boolean isChallengeMode = false;
    private String challengeId;
    private int challengeHearts = 5;
    private long timeLeftMs = 60000;
    private android.os.CountDownTimer gameTimer;

    private float currentSpeed = 3200f; // Faster starting speed
    private float spawnInterval = 2200f; // More frequent starting spawns
    private final float MIN_SPEED = 1200f;
    private final float MIN_SPAWN_INTERVAL = 800f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rocket_sort);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        if (languageCode == null) languageCode = "en";
        alphabet = LanguageConversionUtils.getAlphabetForLanguage(languageCode);

        // Check for challenge mode
        isChallengeMode = getIntent().getBooleanExtra("CHALLENGE_MODE", false);
        challengeId = getIntent().getStringExtra("CHALLENGE_ID");
        challengeHearts = getIntent().getIntExtra("CHALLENGE_HEARTS", 5);
        if (isChallengeMode) {
            timeLeftMs = getIntent().getLongExtra("CHALLENGE_DURATION", 60) * 1000;
        }

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
        tvCountdown = findViewById(R.id.tvCountdown);
        tvTimer = findViewById(R.id.tvTimer);
        overlayLayout = findViewById(R.id.overlayLayout);
        ivRocketLeft = findViewById(R.id.ivRocketLeft);
        ivRocketRight = findViewById(R.id.ivRocketRight);
        konfettiView = findViewById(R.id.konfettiView);
        rocketDock = findViewById(R.id.rocketDock);

        // Pre-warm character arrays for local languages
        new Handler(Looper.getMainLooper()).post(() -> {
            if (alphabet != null && alphabet.length > 0) {
                // Just touching the array to ensure it's fully initialized in memory
                @SuppressWarnings("unused")
                String first = alphabet[0];
            }
        });

        findViewById(R.id.btnPause).setOnClickListener(v -> togglePause());
        findViewById(R.id.btnRestart).setOnClickListener(v -> startNewGame());
        findViewById(R.id.btnResume).setOnClickListener(v -> togglePause());
        findViewById(R.id.btnQuit).setOnClickListener(v -> finish());
    }

    private void initSounds() {
        if (correctPlayer != null) correctPlayer.release();
        correctPlayer = MediaPlayer.create(this, R.raw.correct);
        if (wrongPlayer != null) wrongPlayer.release();
        wrongPlayer = MediaPlayer.create(this, R.raw.shortexplosion);
        if (gameOverPlayer != null) gameOverPlayer.release();
        gameOverPlayer = MediaPlayer.create(this, R.raw.gameover);
        if (levelUpPlayer != null) levelUpPlayer.release();
        levelUpPlayer = MediaPlayer.create(this, R.raw.level);
        
        if (backgroundMusic != null) backgroundMusic.release();
        backgroundMusic = MediaPlayer.create(this, R.raw.rocket);
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f, 0.5f);
        }

        android.content.SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_HIGH_SCORE_ROCKET, 0);
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
        lives = isChallengeMode ? challengeHearts : 5;
        if (isChallengeMode) {
            timeLeftMs = getIntent().getLongExtra("CHALLENGE_DURATION", 60) * 1000;
            if (tvTimer != null) {
                tvTimer.setVisibility(View.VISIBLE);
                updateTimerDisplay();
            }
        } else {
            if (tvTimer != null) tvTimer.setVisibility(View.GONE);
        }
        currentSpeed = 3200f;
        spawnInterval = 2500f;
        
        updateUI();
        overlayLayout.setVisibility(View.GONE);
        
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
        
        for (View a : activeAsteroids) {
            asteroidContainer.removeView(a);
        }
        activeAsteroids.clear();

        showCountdown();
    }

    private void showCountdown() {
        tvCountdown.setVisibility(View.VISIBLE);
        final int[] count = {3};
        
        Runnable countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (count[0] > 0) {
                    tvCountdown.setText(String.valueOf(count[0]));
                    tvCountdown.setScaleX(1.5f);
                    tvCountdown.setScaleY(1.5f);
                    tvCountdown.animate().scaleX(1f).scaleY(1f).setDuration(500).start();
                    count[0]--;
                    spawnHandler.postDelayed(this, 1000);
                } else if (count[0] == 0) {
                    tvCountdown.setText("GO!");
                    count[0]--;
                    spawnHandler.postDelayed(this, 800);
                } else {
                    tvCountdown.setVisibility(View.GONE);
                    startSpawning();
                    if (isChallengeMode) {
                        startTimer(timeLeftMs);
                    }
                }
            }
        };
        spawnHandler.post(countdownRunnable);
    }

    private void startTimer(long duration) {
        if (gameTimer != null) gameTimer.cancel();
        gameTimer = new android.os.CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMs = millisUntilFinished;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                timeLeftMs = 0;
                updateTimerDisplay();
                endGame();
            }
        }.start();
    }

    private void updateTimerDisplay() {
        if (tvTimer != null) {
            tvTimer.setText("⏱️ " + (timeLeftMs / 1000) + "s");
        }
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            if (gameTimer != null) gameTimer.cancel();
            spawnHandler.removeCallbacks(spawnRunnable);
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
            // Properly pause all active ObjectAnimators
            for (ObjectAnimator animator : asteroidAnimators.values()) {
                animator.pause();
            }
            showOverlay("Paused");
        } else {
            overlayLayout.setVisibility(View.GONE);
            if (backgroundMusic != null && !isGameOver) {
                backgroundMusic.start();
            }
            // Properly resume all active ObjectAnimators
            for (ObjectAnimator animator : asteroidAnimators.values()) {
                animator.resume();
            }
            if (isChallengeMode) {
                startTimer(timeLeftMs);
            }
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
        if (isGameOver || isPaused) return;

        boolean isLetter = random.nextBoolean();
        String content = isLetter ? alphabet[random.nextInt(alphabet.length)] : numbers[random.nextInt(numbers.length)];
        
        int size = (int) (getResources().getDisplayMetrics().density * 80);
        final FrameLayout wrapper = new FrameLayout(this);
        wrapper.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        
        // Modern Star Visuals
        wrapper.setBackgroundResource(R.drawable.star_game_object);
        wrapper.setElevation(12f);
        
        TextView tv = new TextView(this);
        tv.setText(content);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(34);
        tv.setGravity(Gravity.CENTER);
        tv.setShadowLayer(6f, 3f, 3f, Color.BLACK);
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
        
        asteroidAnimators.put(wrapper, fallAnimator);

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
                        
                        newX = Math.max(0, Math.min(newX, asteroidContainer.getWidth() - v.getWidth()));
                        
                        v.setX(newX);
                        v.setY(newY);
                        
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
        if (correctPlayer != null) correctPlayer.start();
        updateUI();
        
        ImageView rocket = leftRocket ? ivRocketLeft : ivRocketRight;
        if (rocket != null) {
            rocket.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() -> 
                rocket.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            ).start();
        }
        
        if (score % 5 == 0) {
            if (levelUpPlayer != null) levelUpPlayer.start();
            currentSpeed = Math.max(MIN_SPEED, currentSpeed - 400); // More aggressive speed up
            spawnInterval = Math.max(MIN_SPAWN_INTERVAL, spawnInterval - 300); // Faster spawning
            android.widget.Toast.makeText(this, "Mission Speed Up! 🚀", android.widget.Toast.LENGTH_SHORT).show();
        }
        
        if (score % 10 == 0) celebrate();
    }

    private void handleWrongSort(View asteroid) {
        if (isGameOver || isFinishing() || isDestroyed()) return;
        lives--;
        if (wrongPlayer != null) wrongPlayer.start();
        updateUI();
        
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
        if (wrongPlayer != null) wrongPlayer.start();
        updateUI();
        if (lives <= 0) endGame();
    }

    private void removeAsteroid(View asteroid) {
        asteroidContainer.removeView(asteroid);
        activeAsteroids.remove(asteroid);
        ObjectAnimator animator = asteroidAnimators.remove(asteroid);
        if (animator != null) {
            animator.cancel();
        }
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
        if (gameTimer != null) gameTimer.cancel();
        spawnHandler.removeCallbacks(spawnRunnable);
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
        if (gameOverPlayer != null) {
            gameOverPlayer.start();
        }

        if (score > bestScore) {
            bestScore = score;
            getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putInt(KEY_HIGH_SCORE_ROCKET, bestScore).apply();
            celebrate();
        }

        if (isChallengeMode && challengeId != null) {
            saveChallengeResult();
        }

        showOverlay("Mission Complete!");
    }

    private void saveChallengeResult() {
        com.edulinguaghana.social.ChallengeManager challengeManager = new com.edulinguaghana.social.ChallengeManager();
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        
        if (user == null) return;
        
        challengeManager.recordScore(challengeId, user.getUid(), score, new com.edulinguaghana.social.ChallengeManager.ScoreRecordingCallback() {
            @Override
            public void onSuccess(com.edulinguaghana.social.Challenge challenge) {
                runOnUiThread(() -> {
                    String msg = "Challenge score saved: " + score + " points! 🚀";
                    android.widget.Toast.makeText(RocketSortActivity.this, msg, android.widget.Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> android.widget.Toast.makeText(RocketSortActivity.this, "Failed to save challenge score: " + error, android.widget.Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showOverlay(String title) {
        if (isFinishing() || isDestroyed()) return;
        overlayLayout.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tvOverlayTitle)).setText(title);
        
        TextView scoreText = findViewById(R.id.tvOverlayScore);
        View resumeBtn = findViewById(R.id.btnResume);
        View restartBtn = findViewById(R.id.btnRestart);
        
        if (isPaused && !isGameOver) {
            scoreText.setText("Mission Status: Paused");
            resumeBtn.setVisibility(View.VISIBLE);
            restartBtn.setVisibility(View.GONE);
        } else {
            scoreText.setText("Final Score: " + score);
            resumeBtn.setVisibility(View.GONE);
            restartBtn.setVisibility(View.VISIBLE);
        }
    }

    private void celebrate() {
        if (konfettiView != null) {
            konfettiView.start(new PartyFactory(new Emitter(100L, java.util.concurrent.TimeUnit.MILLISECONDS).max(30))
                    .position(new Position.Relative(0.5, 0.3))
                    .spread(360)
                    .colors(java.util.Arrays.asList(0xfce18a, 0xff726d, 0xf48fb1, 0xafdfff))
                    .build());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        if (gameTimer != null) gameTimer.cancel();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
        spawnHandler.removeCallbacks(spawnRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null && !isPaused && !isGameOver) {
            backgroundMusic.start();
        }
        if (isChallengeMode && !isPaused && !isGameOver) {
            startTimer(timeLeftMs);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) gameTimer.cancel();
        if (correctPlayer != null) correctPlayer.release();
        if (wrongPlayer != null) wrongPlayer.release();
        if (gameOverPlayer != null) gameOverPlayer.release();
        if (levelUpPlayer != null) levelUpPlayer.release();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }
}
