package com.edulinguaghana.games;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edulinguaghana.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BeatMatcherActivity extends AppCompatActivity {

    private TextView tvScore, tvStatus, tvPattern;
    private View btnDrum, overlayLayout;
    private nl.dionsegijn.konfetti.xml.KonfettiView konfettiView;
    
    private int score = 0;
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private boolean isListening = true;
    private String languageCode;

    private List<Long> patternTimings = new ArrayList<>();
    private List<Long> userTimings = new ArrayList<>();
    private List<String> currentPatternStrings = new ArrayList<>();
    private String[] alphabet;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    private MediaPlayer drumPlayer, correctPlayer, wrongPlayer, gameOverPlayer;
    private int bestScore = 0;
    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE_BEAT = "high_score_beat_matcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beat_matcher);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        if (languageCode == null) languageCode = "en";
        alphabet = com.edulinguaghana.utils.LanguageConversionUtils.getAlphabetForLanguage(languageCode);

        initViews();
        initSounds();
        startNewGame();
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvStatus = findViewById(R.id.tvStatus);
        tvPattern = findViewById(R.id.tvPattern);
        btnDrum = findViewById(R.id.btnDrum);
        overlayLayout = findViewById(R.id.overlayLayout);
        konfettiView = findViewById(R.id.konfettiView);

        btnDrum.setOnClickListener(v -> handleDrumTap());
        findViewById(R.id.btnPause).setOnClickListener(v -> togglePause());
        findViewById(R.id.btnResume).setOnClickListener(v -> togglePause());
        findViewById(R.id.btnRestart).setOnClickListener(v -> startNewGame());
        findViewById(R.id.btnQuit).setOnClickListener(v -> finish());
    }

    private void initSounds() {
        drumPlayer = MediaPlayer.create(this, R.raw.bell); // Use bell as drum sound
        correctPlayer = MediaPlayer.create(this, R.raw.correct);
        wrongPlayer = MediaPlayer.create(this, R.raw.wrong);
        gameOverPlayer = MediaPlayer.create(this, R.raw.gameover);

        android.content.SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_HIGH_SCORE_BEAT, 0);
    }

    private void startNewGame() {
        score = 0;
        isGameOver = false;
        isPaused = false;
        overlayLayout.setVisibility(View.GONE);
        updateUI();
        generateNewPattern();
    }

    private void generateNewPattern() {
        isListening = true;
        tvStatus.setText("Listen!");
        btnDrum.setEnabled(false);
        patternTimings.clear();
        userTimings.clear();
        currentPatternStrings.clear();

        // Pattern of 3-5 beats
        int length = 3 + random.nextInt(2);
        StringBuilder sb = new StringBuilder();
        
        // Randomly choose between letters and numbers for the visual pattern
        boolean useNumbers = random.nextBoolean();
        String[] pool = useNumbers ? new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"} : alphabet;

        long lastTime = 0;
        for (int i = 0; i < length; i++) {
            String item = pool[random.nextInt(pool.length)];
            currentPatternStrings.add(item);
            sb.append(item).append(i == length - 1 ? "" : " - ");
            
            // Random rhythm interval between 500ms and 1000ms
            long interval = 500 + random.nextInt(500);
            lastTime += interval;
            patternTimings.add(lastTime);
        }
        
        tvPattern.setText(sb.toString());
        playPattern(0);
    }

    private void playPattern(int index) {
        if (isPaused || isGameOver) return;
        
        if (index < currentPatternStrings.size()) {
            String item = currentPatternStrings.get(index);
            tvPattern.setScaleX(1.2f);
            tvPattern.setScaleY(1.2f);
            tvPattern.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            
            speakItem(item);
            if (drumPlayer != null) drumPlayer.start();
            
            long nextWait = (index == currentPatternStrings.size() - 1) ? 1000 : (patternTimings.get(index + 1) - patternTimings.get(index));
            handler.postDelayed(() -> playPattern(index + 1), nextWait);
        } else {
            startPlayerTurn();
        }
    }

    private void speakItem(String text) {
        if (com.edulinguaghana.utils.LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
            com.edulinguaghana.tts.OfflineGhanaLPTtsService offlineTts = new com.edulinguaghana.tts.OfflineGhanaLPTtsService(this);
            offlineTts.speak(text, languageCode, null);
            // Note: In a real app we'd manage this service better to avoid multiple initializations
        }
    }

    private void startPlayerTurn() {
        isListening = false;
        tvStatus.setText("Your Turn!");
        btnDrum.setEnabled(true);
        userTimings.clear();
    }

    private void handleDrumTap() {
        if (isListening || isPaused || isGameOver) return;

        long now = System.currentTimeMillis();
        if (userTimings.isEmpty()) {
            userTimings.add(0L); // First tap is baseline
        } else {
            // Record time since first tap
            // Note: This is a simplified rhythm matching logic
            userTimings.add(userTimings.size(), (long) userTimings.size() * 600); // placeholder
        }
        
        if (drumPlayer != null) drumPlayer.start();
        btnDrum.animate().scaleX(1.1f).scaleY(1.1f).setDuration(50).withEndAction(() -> 
            btnDrum.animate().scaleX(1.0f).scaleY(1.0f).setDuration(50).start()
        ).start();

        if (userTimings.size() >= patternTimings.size()) {
            checkPattern();
        }
    }

    private void checkPattern() {
        // Since we don't have complex timing analysis yet, 
        // we'll reward completing the sequence for now.
        score += 20;
        if (correctPlayer != null) correctPlayer.start();
        tvStatus.setText("Perfect! ✨");
        updateUI();
        
        handler.postDelayed(this::generateNewPattern, 1500);
    }

    private void endGame() {
        isGameOver = true;
        if (gameOverPlayer != null) {
            gameOverPlayer.start();
        }

        if (score > bestScore) {
            bestScore = score;
            getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putInt(KEY_HIGH_SCORE_BEAT, bestScore).apply();
            celebrate();
        }

        overlayLayout.setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tvOverlayTitle)).setText("Game Over!");
        ((TextView) findViewById(R.id.tvOverlayScore)).setText("Final Score: " + score);
        findViewById(R.id.btnResume).setVisibility(View.GONE);
    }

    private void togglePause() {
        isPaused = !isPaused;
        overlayLayout.setVisibility(isPaused ? View.VISIBLE : View.GONE);
    }

    private void updateUI() {
        tvScore.setText("⭐ " + score);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (drumPlayer != null) drumPlayer.release();
        if (correctPlayer != null) correctPlayer.release();
        if (wrongPlayer != null) wrongPlayer.release();
        if (gameOverPlayer != null) gameOverPlayer.release();
    }
}
