package com.edulinguaghana.games;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edulinguaghana.R;
import com.edulinguaghana.gamification.FunGameProgressManager;
import com.edulinguaghana.utils.LanguageConversionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PuzzleGameActivity extends AppCompatActivity {

    private LinearLayout slotContainer, pieceContainer;
    private TextView tvScore, tvTimer, tvCountdown, tvPrompt;
    private View overlayLayout;
    private nl.dionsegijn.konfetti.xml.KonfettiView konfettiView;

    private String languageCode;
    private String[] alphabet;
    private int score = 0;
    private long timeLeftMs = 60000;
    private boolean isGameOver = false, isPaused = false;
    private CountDownTimer gameTimer;
    private int piecesMatched = 0;
    private final int PUZZLE_SIZE = 3;

    private MediaPlayer correctPlayer, wrongPlayer, gameOverPlayer;
    private int bestScore = 0;
    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE_PUZZLE = "high_score_puzzle_game";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean funSessionRecorded = false;

    // Challenge mode
    private boolean isChallengeMode = false;
    private String challengeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_game);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        if (languageCode == null) languageCode = "en";
        alphabet = LanguageConversionUtils.getAlphabetForLanguage(languageCode);

        // Check for challenge mode
        isChallengeMode = getIntent().getBooleanExtra("CHALLENGE_MODE", false);
        challengeId = getIntent().getStringExtra("CHALLENGE_ID");
        if (isChallengeMode) {
            timeLeftMs = getIntent().getLongExtra("CHALLENGE_DURATION", 60) * 1000;
        }

        initViews();
        
        // Pre-warm character arrays
        handler.post(() -> {
            if (alphabet != null && alphabet.length > 0) {
                @SuppressWarnings("unused")
                String first = alphabet[0];
            }
        });

        initSounds();
        startNewGame();
    }

    private void initViews() {
        slotContainer = findViewById(R.id.slotContainer);
        pieceContainer = findViewById(R.id.pieceContainer);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvPrompt = findViewById(R.id.tvPrompt);
        overlayLayout = findViewById(R.id.overlayLayout);
        konfettiView = findViewById(R.id.konfettiView);

        findViewById(R.id.btnPause).setOnClickListener(v -> togglePause());
        findViewById(R.id.btnResume).setOnClickListener(v -> togglePause());
        findViewById(R.id.btnRestart).setOnClickListener(v -> startNewGame());
        findViewById(R.id.btnQuit).setOnClickListener(v -> finish());
    }

    private void initSounds() {
        correctPlayer = MediaPlayer.create(this, R.raw.correct);
        wrongPlayer = MediaPlayer.create(this, R.raw.wrong);
        gameOverPlayer = MediaPlayer.create(this, R.raw.gameover);

        android.content.SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_HIGH_SCORE_PUZZLE, 0);
    }

    private void startNewGame() {
        funSessionRecorded = false;
        score = 0;
        if (isChallengeMode) {
            timeLeftMs = getIntent().getLongExtra("CHALLENGE_DURATION", 60) * 1000;
        } else {
            timeLeftMs = 60000;
        }
        isGameOver = false;
        isPaused = false;
        overlayLayout.setVisibility(View.GONE);
        updateUI();
        showCountdown();
    }

    private void showCountdown() {
        tvCountdown.setVisibility(View.VISIBLE);
        final int[] count = {3};
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (count[0] > 0) {
                    tvCountdown.setText(String.valueOf(count[0]));
                    count[0]--;
                    handler.postDelayed(this, 1000);
                } else if (count[0] == 0) {
                    tvCountdown.setText("GO!");
                    count[0]--;
                    handler.postDelayed(this, 800);
                } else {
                    tvCountdown.setVisibility(View.GONE);
                    startTimer(timeLeftMs);
                    generateNewPuzzle();
                }
            }
        };
        handler.post(r);
    }

    private void generateNewPuzzle() {
        slotContainer.removeAllViews();
        pieceContainer.removeAllViews();
        piecesMatched = 0;

        List<String> selected = new ArrayList<>();
        Random r = new Random();
        
        // Randomly decide between letters and numbers for this set
        boolean useNumbers = r.nextBoolean();
        String[] pool = useNumbers ? new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"} : alphabet;

        tvPrompt.setText(useNumbers ? "Match the numbers!" : "Match the letters!");

        while (selected.size() < PUZZLE_SIZE) {
            String item = pool[r.nextInt(pool.length)];
            if (!selected.contains(item)) selected.add(item);
        }

        List<String> shuffled = new ArrayList<>(selected);
        Collections.shuffle(shuffled);

        int size = (int) (80 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(10, 0, 10, 0);

        for (String letter : selected) {
            FrameLayout slot = new FrameLayout(this);
            slot.setLayoutParams(params);
            slot.setBackgroundResource(R.drawable.puzzle_slot_modern);
            slot.setTag(letter);
            
            TextView tv = new TextView(this);
            tv.setText(letter);
            tv.setTextColor(Color.parseColor("#44FFFFFF"));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(28);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            slot.addView(tv);

            slot.setOnDragListener(new PuzzleDragListener());
            slotContainer.addView(slot);
            
            // Add a pulse animation to slots
            ObjectAnimator pulse = ObjectAnimator.ofFloat(slot, View.ALPHA, 0.5f, 1.0f);
            pulse.setDuration(1500);
            pulse.setRepeatCount(ValueAnimator.INFINITE);
            pulse.setRepeatMode(ValueAnimator.REVERSE);
            pulse.start();
        }

        for (String letter : shuffled) {
            TextView piece = new TextView(this);
            piece.setLayoutParams(params);
            piece.setText(letter);
            piece.setGravity(Gravity.CENTER);
            piece.setTextSize(32);
            piece.setTextColor(Color.WHITE);
            piece.setBackgroundResource(R.drawable.puzzle_piece_glossy);
            piece.setTag(letter);
            piece.setElevation(12f);

            // Add a gentle hover animation
            piece.animate().translationYBy(-10).setDuration(1000).setInterpolator(new android.view.animation.CycleInterpolator(0.5f)).start();

            piece.setOnLongClickListener(v -> {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                v.startDrag(data, shadow, v, 0);
                v.setVisibility(View.INVISIBLE);
                return true;
            });

            pieceContainer.addView(piece);
            
            // Piece Entrance Animation
            piece.setAlpha(0f);
            piece.setTranslationY(100f);
            long delay = (long) shuffled.indexOf(letter) * 100;
            piece.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(delay).setInterpolator(new android.view.animation.OvershootInterpolator()).start();
        }
    }

    private class PuzzleDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    String draggedLetter = (String) draggedView.getTag();
                    String targetLetter = (String) v.getTag();

                    if (draggedLetter.equals(targetLetter)) {
                        // Correct Match
                        score += 1;
                        piecesMatched++;
                        updateUI();
                        if (correctPlayer != null) correctPlayer.start();
                        
                        ((ViewGroup) draggedView.getParent()).removeView(draggedView);
                        ((FrameLayout) v).removeAllViews();
                        ((FrameLayout) v).addView(draggedView);
                        draggedView.setVisibility(View.VISIBLE);
                        draggedView.setOnLongClickListener(null); // Lock it
                        draggedView.setBackgroundResource(R.drawable.puzzle_piece_glossy); // Ensure style
                        
                        // Success Animation
                        draggedView.setScaleX(1.2f);
                        draggedView.setScaleY(1.2f);
                        draggedView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new android.view.animation.OvershootInterpolator()).start();

                        v.setBackgroundResource(0); // Remove slot border
                        
                        if (piecesMatched >= PUZZLE_SIZE) {
                            celebrate();
                            handler.postDelayed(() -> generateNewPuzzle(), 1000);
                        }
                    } else {
                        // Wrong Match
                        if (wrongPlayer != null) wrongPlayer.start();
                        draggedView.setVisibility(View.VISIBLE);
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        View view = (View) event.getLocalState();
                        view.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            return true;
        }
    }

    private void startTimer(long duration) {
        if (gameTimer != null) gameTimer.cancel();
        gameTimer = new CountDownTimer(duration, 1000) {
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

    private void togglePause() {
        if (isGameOver) return;
        isPaused = !isPaused;
        overlayLayout.setVisibility(isPaused ? View.VISIBLE : View.GONE);
        TextView tvTitle = findViewById(R.id.tvOverlayTitle);
        tvTitle.setText(isPaused ? "Paused" : "Time Up!");
        
        TextView tvScoreText = findViewById(R.id.tvOverlayScore);
        tvScoreText.setText("Current Score: " + score);

        findViewById(R.id.btnResume).setVisibility(isPaused ? View.VISIBLE : View.GONE);
        findViewById(R.id.btnRestart).setVisibility(isPaused ? View.GONE : View.VISIBLE);

        if (isPaused) {
            if (gameTimer != null) gameTimer.cancel();
        } else {
            startTimer(timeLeftMs);
        }
    }

    private void updateUI() {
        tvScore.setText("⭐ " + score);
    }

    private void updateTimerDisplay() {
        tvTimer.setText((timeLeftMs / 1000) + "s");
    }

    private void endGame() {
        isGameOver = true;
        if (gameOverPlayer != null) {
            gameOverPlayer.start();
        }

        if (score > bestScore) {
            bestScore = score;
            getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putInt(KEY_HIGH_SCORE_PUZZLE, bestScore).apply();
            celebrate();
        }

        if (isChallengeMode && challengeId != null) {
            saveChallengeResult();
        }

        if (!funSessionRecorded) {
            funSessionRecorded = true;
            try {
                FunGameProgressManager.recordGameCompleted(this, "puzzle_game", score, languageCode);
            } catch (Exception ignored) {
            }
        }

        overlayLayout.setVisibility(View.VISIBLE);
        TextView tvTitle = findViewById(R.id.tvOverlayTitle);
        tvTitle.setText("Mission Complete");
        TextView tvScoreText = findViewById(R.id.tvOverlayScore);
        tvScoreText.setText("Final Score: " + score);
        findViewById(R.id.btnResume).setVisibility(View.GONE);
        findViewById(R.id.btnRestart).setVisibility(View.VISIBLE);
    }

    private void saveChallengeResult() {
        com.edulinguaghana.social.ChallengeManager challengeManager = new com.edulinguaghana.social.ChallengeManager();
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        
        if (user == null) return;
        
        challengeManager.recordScore(challengeId, user.getUid(), score, new com.edulinguaghana.social.ChallengeManager.ScoreRecordingCallback() {
            @Override
            public void onSuccess(com.edulinguaghana.social.Challenge challenge) {
                runOnUiThread(() -> {
                    String msg = "Challenge score saved: " + score + " points! 🧩";
                    android.widget.Toast.makeText(PuzzleGameActivity.this, msg, android.widget.Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> android.widget.Toast.makeText(PuzzleGameActivity.this, "Failed to save challenge score: " + error, android.widget.Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void celebrate() {
        konfettiView.start(new nl.dionsegijn.konfetti.core.PartyFactory(new nl.dionsegijn.konfetti.core.emitter.Emitter(100L, java.util.concurrent.TimeUnit.MILLISECONDS).max(30))
                .position(new nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.3))
                .spread(360)
                .colors(java.util.Arrays.asList(0xfce18a, 0xff726d, 0xf48fb1, 0xafdfff))
                .build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) gameTimer.cancel();
        if (correctPlayer != null) correctPlayer.release();
        if (wrongPlayer != null) wrongPlayer.release();
        if (gameOverPlayer != null) gameOverPlayer.release();
    }
}
