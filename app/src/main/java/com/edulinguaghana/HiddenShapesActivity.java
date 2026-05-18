package com.edulinguaghana;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edulinguaghana.tts.OfflineGhanaLPTtsService;
import com.edulinguaghana.utils.LanguageConversionUtils;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.Random;

public class HiddenShapesActivity extends AppCompatActivity {

    private ScratchRevealView scratchView;
    private TextView tvPrompt, tvCountdown, tvTimer, tvScore;
    private MaterialButton btnNext, btnRestart, btnResume;
    private View overlayLayout;
    private nl.dionsegijn.konfetti.xml.KonfettiView konfettiView;
    private String languageCode;
    private String currentCharacter;
    private String[] alphabet;
    private int score = 0;
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private long timeLeftMs = 60000;
    private android.os.CountDownTimer gameTimer;
    
    private TextToSpeech tts;
    private OfflineGhanaLPTtsService offlineTts;
    private MediaPlayer gameOverPlayer;
    private int bestScore = 0;
    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE_HIDDEN = "high_score_hidden_shapes";
    private boolean isTtsReady = false;
    private final Handler handler = new Handler(android.os.Looper.getMainLooper());

    // Challenge mode
    private boolean isChallengeMode = false;
    private String challengeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_shapes);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        if (languageCode == null) languageCode = "en";

        // Check for challenge mode
        isChallengeMode = getIntent().getBooleanExtra("CHALLENGE_MODE", false);
        challengeId = getIntent().getStringExtra("CHALLENGE_ID");
        if (isChallengeMode) {
            timeLeftMs = getIntent().getLongExtra("CHALLENGE_DURATION", 60) * 1000;
        }

        // Get language-specific alphabet for pre-warming
        alphabet = LanguageConversionUtils.getAlphabetForLanguage(languageCode);

        // Pre-warm character arrays
        handler.post(() -> {
            if (alphabet != null && alphabet.length > 0) {
                @SuppressWarnings("unused")
                String first = alphabet[0];
            }
        });

        scratchView = findViewById(R.id.scratchView);
        tvPrompt = findViewById(R.id.tvPrompt);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        btnNext = findViewById(R.id.btnNext);
        btnRestart = findViewById(R.id.btnRestart);
        btnResume = findViewById(R.id.btnResume);
        overlayLayout = findViewById(R.id.overlayLayout);
        konfettiView = findViewById(R.id.konfettiView);

        // Standard Back Button in Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        btnNext.setOnClickListener(v -> generateNewCharacter());
        btnRestart.setOnClickListener(v -> startNewGame());
        btnResume.setOnClickListener(v -> togglePause());
        findViewById(R.id.btnPause).setOnClickListener(v -> togglePause());
        findViewById(R.id.btnQuit).setOnClickListener(v -> finish());

        initTts();
        initSounds();
        startNewGame();
    }

    private void initSounds() {
        gameOverPlayer = MediaPlayer.create(this, R.raw.gameover);
        
        android.content.SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_HIGH_SCORE_HIDDEN, 0);
    }

    private void startNewGame() {
        isGameOver = false;
        isPaused = false;
        score = 0;
        if (isChallengeMode) {
            timeLeftMs = getIntent().getLongExtra("CHALLENGE_DURATION", 60) * 1000;
        } else {
            timeLeftMs = 60000;
        }
        overlayLayout.setVisibility(View.GONE);
        updateTimerDisplay();
        updateScoreDisplay();
        showInitialCountdown();
    }

    private void togglePause() {
        if (isGameOver) return;
        isPaused = !isPaused;
        if (isPaused) {
            if (gameTimer != null) gameTimer.cancel();
            showPauseOverlay("Paused");
        } else {
            overlayLayout.setVisibility(View.GONE);
            startTimer(timeLeftMs);
        }
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
            tvTimer.setText((timeLeftMs / 1000) + "s");
        }
    }

    private void updateScoreDisplay() {
        if (tvScore != null) {
            tvScore.setText("⭐ " + score);
        }
    }

    private void endGame() {
        isGameOver = true;
        if (gameTimer != null) gameTimer.cancel();
        if (gameOverPlayer != null) {
            gameOverPlayer.start();
        }

        if (score > bestScore) {
            bestScore = score;
            getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putInt(KEY_HIGH_SCORE_HIDDEN, bestScore).apply();
            celebrate();
        }

        if (isChallengeMode && challengeId != null) {
            saveChallengeResult();
        }

        showPauseOverlay("Time Up!");
    }

    private void saveChallengeResult() {
        com.edulinguaghana.social.ChallengeManager challengeManager = new com.edulinguaghana.social.ChallengeManager();
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        
        if (user == null) return;
        
        challengeManager.recordScore(challengeId, user.getUid(), score, new com.edulinguaghana.social.ChallengeManager.ScoreRecordingCallback() {
            @Override
            public void onSuccess(com.edulinguaghana.social.Challenge challenge) {
                runOnUiThread(() -> {
                    String msg = "Challenge score saved: " + score + " points! ⏳";
                    android.widget.Toast.makeText(HiddenShapesActivity.this, msg, android.widget.Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> android.widget.Toast.makeText(HiddenShapesActivity.this, "Failed to save challenge score: " + error, android.widget.Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showPauseOverlay(String title) {
        overlayLayout.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tvOverlayTitle)).setText(title);
        TextView scoreText = findViewById(R.id.tvOverlayScore);
        
        if (isPaused && !isGameOver) {
            scoreText.setText("Status: Paused");
            btnResume.setVisibility(View.VISIBLE);
            btnRestart.setVisibility(View.GONE);
        } else {
            scoreText.setText("Mission Score: " + score);
            btnResume.setVisibility(View.GONE);
            btnRestart.setVisibility(View.VISIBLE);
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
            }
        });
    }

    private void generateNewCharacter() {
        if (isGameOver || isPaused) return;

        btnNext.setVisibility(View.GONE);
        tvPrompt.setText(R.string.hidden_shapes_prompt);

        boolean useNumber = new Random().nextBoolean();
        
        if (useNumber) {
            currentCharacter = String.valueOf(new Random().nextInt(20) + 1);
        } else if (alphabet != null && alphabet.length > 0) {
            currentCharacter = alphabet[new Random().nextInt(alphabet.length)];
        } else {
            currentCharacter = "A"; // Fallback
        }

        scratchView.setHiddenText(currentCharacter, text -> {
            // Character revealed!
            runOnUiThread(() -> {
                if (isGameOver) return;
                
                // Pause timer while celebrating/showing revealed item
                if (gameTimer != null) gameTimer.cancel();
                
                score += 1; // 1 point per shape
                updateScoreDisplay();
                celebrate();
                speakCharacter(text);
                tvPrompt.setText(getString(R.string.hidden_shapes_found, text));
                
                // Auto-advance after 1.5 seconds
                handler.postDelayed(() -> {
                    if (!isGameOver && !isPaused) {
                        generateNewCharacter();
                        // Continue timer
                        startTimer(timeLeftMs);
                    }
                }, 1500);
            });
        });
    }

    private void showInitialCountdown() {
        if (tvCountdown == null) {
            startTimer(timeLeftMs);
            generateNewCharacter();
            return;
        }

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
                    handler.postDelayed(this, 1000);
                } else if (count[0] == 0) {
                    tvCountdown.setText("GO!");
                    count[0]--;
                    handler.postDelayed(this, 800);
                } else {
                    tvCountdown.setVisibility(View.GONE);
                    startTimer(timeLeftMs);
                    generateNewCharacter();
                }
            }
        };
        handler.post(countdownRunnable);
    }

    private void celebrate() {
        if (konfettiView == null) return;
        konfettiView.start(
            new nl.dionsegijn.konfetti.core.PartyFactory(
                new nl.dionsegijn.konfetti.core.emitter.Emitter(1000L, java.util.concurrent.TimeUnit.MILLISECONDS).max(100)
            )
            .spread(360)
            .colors(java.util.Arrays.asList(0xfce18a, 0xff726d, 0xf48fb1, 0xafdfff))
            .setSpeedBetween(10f, 30f)
            .position(new nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.3))
            .build()
        );
    }

    private void speakCharacter(String text) {
        if (LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
            offlineTts.speak(text, languageCode, new OfflineGhanaLPTtsService.PlaybackCallback() {
                @Override
                public void onStart() {}

                @Override
                public void onComplete() {}

                @Override
                public void onError(String error) {
                    // Fallback to Android TTS if offline audio is missing
                    if (isTtsReady) {
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hidden_shape_fallback");
                    }
                }
            });
        } else if (isTtsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hidden_shape");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) tts.shutdown();
        if (offlineTts != null) offlineTts.stop();
        if (gameOverPlayer != null) {
            gameOverPlayer.release();
            gameOverPlayer = null;
        }
    }
}
