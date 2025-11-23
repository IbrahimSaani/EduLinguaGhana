package com.edulinguaghana;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class SpeedGameActivity extends AppCompatActivity {

    // Views
    private TextView tvGameTitle, tvGameTimer, tvGameScore, tvGameBest, tvGameFeedback, tvGamePrompt;
    private Button btnPlayAudio, btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6, btnBack;

    // Game variables
    private int score = 0;
    private int bestScore = 0;
    private String currentCorrectAnswer;
    private CountDownTimer countDownTimer;
    private long timeLeftMs;

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";
    private static final String KEY_SFX_ENABLED = "SFX_ENABLED";

    // Timer settings (total round time)
    private static final long TOTAL_TIME_MS = 30000;  // 30s round

    // Letters to use in the game
    private final String[] questions = {
            "A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"
    };

    // TTS
    private TextToSpeech tts;
    private boolean isTtsReady = false;
    private String languageCode;   // "en", "fr", etc.

    // SFX
    private boolean isSfxOn = true;
    private MediaPlayer sfxPlayer;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_game);

        // --- Get language code from intent (same as other modes) ---
        languageCode = getIntent().getStringExtra("LANG_CODE");
        if (languageCode == null) {
            languageCode = "en";
        }

        // --- Init views ---
        tvGameTitle    = findViewById(R.id.tvGameTitle);
        tvGameTimer    = findViewById(R.id.tvGameTimer);
        tvGameScore    = findViewById(R.id.tvGameScore);
        tvGameBest     = findViewById(R.id.tvGameBest);
        tvGameFeedback = findViewById(R.id.tvGameFeedback);
        tvGamePrompt   = findViewById(R.id.tvGamePrompt);

        btnOption1 = findViewById(R.id.btnGameOpt1);
        btnOption2 = findViewById(R.id.btnGameOpt2);
        btnOption3 = findViewById(R.id.btnGameOpt3);
        btnOption4 = findViewById(R.id.btnGameOpt4);
        btnOption5 = findViewById(R.id.btnGameOpt5);
        btnOption6 = findViewById(R.id.btnGameOpt6);
        btnBack    = findViewById(R.id.btnGameBack);

        // OPTIONAL: if you added a Play Audio button in XML, otherwise comment out
        btnPlayAudio = findViewById(R.id.btnPlayAudio);

        // --- SharedPreferences ---
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        isSfxOn   = prefs.getBoolean(KEY_SFX_ENABLED, true);

        tvGameBest.setText("Best: " + bestScore);
        tvGameScore.setText("Score: 0");
        tvGameFeedback.setText("");

        // --- Init TTS ---
        initTts();

        // --- Button listeners ---
        btnOption1.setOnClickListener(v -> handleAnswerClick((Button) v));
        btnOption2.setOnClickListener(v -> handleAnswerClick((Button) v));
        btnOption3.setOnClickListener(v -> handleAnswerClick((Button) v));
        btnOption4.setOnClickListener(v -> handleAnswerClick((Button) v));
        btnOption5.setOnClickListener(v -> handleAnswerClick((Button) v));
        btnOption6.setOnClickListener(v -> handleAnswerClick((Button) v));

        btnBack.setOnClickListener(v -> {
            cancelTimer();
            finish();
        });

        if (btnPlayAudio != null) {
            btnPlayAudio.setOnClickListener(v -> playAudio());
        }

        // --- Start game ---
        startNewRound();
    }

    // -------------------------
    // TTS
    // -------------------------
    private void initTts() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale locale;
                if ("fr".equalsIgnoreCase(languageCode)) {
                    locale = Locale.FRENCH;
                } else {
                    locale = Locale.ENGLISH;
                }
                int result = tts.setLanguage(locale);
                if (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true;
                }
            }
        });
    }

    private void playAudio() {
        if (!isTtsReady || currentCorrectAnswer == null) return;
        // Stop any current speech then speak
        tts.stop();
        tts.speak(currentCorrectAnswer, TextToSpeech.QUEUE_FLUSH, null, "speed_game_letter");
    }

    // -------------------------
    // GAME FLOW
    // -------------------------
    private void startNewRound() {
        score = 0;
        tvGameScore.setText("Score: " + score);
        tvGameFeedback.setText("");
        timeLeftMs = TOTAL_TIME_MS;
        tvGameTimer.setText("Time: " + (timeLeftMs / 1000) + "s");

        generateNewQuestion();
        startTimer();
    }

    private void generateNewQuestion() {
        // pick correct letter
        Random rnd = new Random();
        currentCorrectAnswer = questions[rnd.nextInt(questions.length)];

        // build 6 unique options including correct
        List<String> options = new ArrayList<>();
        options.add(currentCorrectAnswer);
        Set<String> used = new HashSet<>();
        used.add(currentCorrectAnswer);

        while (options.size() < 6) {
            String candidate = questions[rnd.nextInt(questions.length)];
            if (!used.contains(candidate)) {
                options.add(candidate);
                used.add(candidate);
            }
        }

        Collections.shuffle(options);

        btnOption1.setText(options.get(0));
        btnOption2.setText(options.get(1));
        btnOption3.setText(options.get(2));
        btnOption4.setText(options.get(3));
        btnOption5.setText(options.get(4));
        btnOption6.setText(options.get(5));

        tvGamePrompt.setText("Which letter did you hear?");
        tvGameFeedback.setText("");
    }

    private void handleAnswerClick(Button clickedButton) {
        String chosen = clickedButton.getText().toString();
        boolean correct = chosen.equals(currentCorrectAnswer);

        if (correct) {
            score++;
            tvGameFeedback.setText("✅ Correct!");
            tvGameScore.setText("Score: " + score);

            playSfx(true);
            playCorrectAnimation(clickedButton);

            if (score > bestScore) {
                bestScore = score;
                tvGameBest.setText("Best: " + bestScore);
                saveHighScore(bestScore);
                playHighScoreCelebration();
            }
        } else {
            tvGameFeedback.setText("❌ Wrong! Correct: " + currentCorrectAnswer);
            playSfx(false);
            playWrongAnimation(clickedButton);
        }

        // Next question immediately
        generateNewQuestion();
    }

    // -------------------------
    // TIMER
    // -------------------------
    private void startTimer() {
        cancelTimer();
        countDownTimer = new CountDownTimer(timeLeftMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMs = millisUntilFinished;
                tvGameTimer.setText("Time: " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                timeLeftMs = 0;
                tvGameTimer.setText("Time: 0s");
                tvGameFeedback.setText("⏰ Time's up! Final score: " + score);

                // Disable buttons
                setOptionsEnabled(false);
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void setOptionsEnabled(boolean enabled) {
        btnOption1.setEnabled(enabled);
        btnOption2.setEnabled(enabled);
        btnOption3.setEnabled(enabled);
        btnOption4.setEnabled(enabled);
        btnOption5.setEnabled(enabled);
        btnOption6.setEnabled(enabled);
    }

    // -------------------------
    // SFX
    // -------------------------
    private void playSfx(boolean isCorrect) {
        if (!isSfxOn) return;

        int resId = isCorrect ? R.raw.correct : R.raw.wrong;

        try {
            if (sfxPlayer != null) {
                sfxPlayer.release();
                sfxPlayer = null;
            }
            sfxPlayer = MediaPlayer.create(this, resId);
            if (sfxPlayer != null) {
                sfxPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    sfxPlayer = null;
                });
                sfxPlayer.start();
            }
        } catch (Exception ignored) {}
    }

    private void saveHighScore(int value) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt(KEY_HIGH_SCORE, value);
        ed.apply();
    }

    // -------------------------
    // ANIMATIONS
    // -------------------------
    private void playHighScoreCelebration() {
        // Glow pulse on best score
        try {
            Animation glow = AnimationUtils.loadAnimation(this, R.anim.glow_pulse);
            tvGameBest.startAnimation(glow);
        } catch (Exception ignored) {}

        // Bounce on score
        try {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_pop);
            tvGameScore.startAnimation(bounce);
        } catch (Exception ignored) {}

        // Rainbow shine on title
        try {
            Animation shine = AnimationUtils.loadAnimation(this, R.anim.rainbow_shine);
            tvGameTitle.startAnimation(shine);
        } catch (Exception ignored) {}

        // Confetti on feedback
        try {
            Animation confetti = AnimationUtils.loadAnimation(this, R.anim.confetti_fall);
            tvGameFeedback.startAnimation(confetti);
        } catch (Exception ignored) {}

        // Screen shake for entire screen
        try {
            View root = findViewById(android.R.id.content);
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.screen_shake);
            root.startAnimation(shake);
        } catch (Exception ignored) {}
    }

    private void playCorrectAnimation(View button) {
        try {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_pop);
            button.startAnimation(bounce);

            Animation glow = AnimationUtils.loadAnimation(this, R.anim.glow_pulse);
            tvGameFeedback.startAnimation(glow);
        } catch (Exception ignored) {}
    }

    private void playWrongAnimation(View button) {
        try {
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.screen_shake);
            button.startAnimation(shake);

            Animation confetti = AnimationUtils.loadAnimation(this, R.anim.confetti_fall);
            tvGameFeedback.startAnimation(confetti);
        } catch (Exception ignored) {}
    }

    // -------------------------
    // LIFECYCLE
    // -------------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (sfxPlayer != null) {
            sfxPlayer.release();
            sfxPlayer = null;
        }
    }
}
