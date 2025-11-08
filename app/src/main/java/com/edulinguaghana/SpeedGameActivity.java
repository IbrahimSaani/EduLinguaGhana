package com.edulinguaghana;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class SpeedGameActivity extends AppCompatActivity {

    private TextView tvGameTitle, tvGameTimer, tvGameScore, tvGameBest, tvGamePrompt;
    private Button btnGameOpt1, btnGameOpt2, btnGameOpt3, btnGameOpt4, btnGameOpt5, btnGameOpt6, btnGameBack;

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_SPEED_HIGH_SCORE = "SPEED_HIGH_SCORE";

    private final String[] letters = {
            "A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"
    };

    private int gameScore = 0;
    private int bestScore = 0;
    private String currentCorrect;
    private CountDownTimer gameTimer;
    private static final long GAME_DURATION_MS = 30000; // 30s
    private boolean isGameOver = false;

    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_game);

        tvGameTitle  = findViewById(R.id.tvGameTitle);
        tvGameTimer  = findViewById(R.id.tvGameTimer);
        tvGameScore  = findViewById(R.id.tvGameScore);
        tvGameBest   = findViewById(R.id.tvGameBest);
        tvGamePrompt = findViewById(R.id.tvGamePrompt);

        btnGameOpt1 = findViewById(R.id.btnGameOpt1);
        btnGameOpt2 = findViewById(R.id.btnGameOpt2);
        btnGameOpt3 = findViewById(R.id.btnGameOpt3);
        btnGameOpt4 = findViewById(R.id.btnGameOpt4);
        btnGameOpt5 = findViewById(R.id.btnGameOpt5);
        btnGameOpt6 = findViewById(R.id.btnGameOpt6);
        btnGameBack = findViewById(R.id.btnGameBack);

        String langName = getIntent().getStringExtra("LANG_NAME");
        if (langName == null) langName = "Language";
        tvGameTitle.setText("Speed Challenge - " + langName);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_SPEED_HIGH_SCORE, 0);
        tvGameBest.setText("Best: " + bestScore);

        btnGameOpt1.setOnClickListener(v -> handleAnswer(btnGameOpt1));
        btnGameOpt2.setOnClickListener(v -> handleAnswer(btnGameOpt2));
        btnGameOpt3.setOnClickListener(v -> handleAnswer(btnGameOpt3));
        btnGameOpt4.setOnClickListener(v -> handleAnswer(btnGameOpt4));
        btnGameOpt5.setOnClickListener(v -> handleAnswer(btnGameOpt5));
        btnGameOpt6.setOnClickListener(v -> handleAnswer(btnGameOpt6));

        btnGameBack.setOnClickListener(v -> finish());

        startGame();
    }

    private void startGame() {
        isGameOver = false;
        gameScore = 0;
        tvGameScore.setText("Score: " + gameScore);
        enableOptionButtons(true);
        startTimer();
        generateNewRound();
    }

    private void startTimer() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        tvGameTimer.setText("Time: 30s");

        gameTimer = new CountDownTimer(GAME_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long s = millisUntilFinished / 1000;
                tvGameTimer.setText("Time: " + s + "s");
            }

            @Override
            public void onFinish() {
                tvGameTimer.setText("Time: 0s");
                endGame();
            }
        }.start();
    }

    private void generateNewRound() {
        // Mix of letters and numbers for fun
        boolean isLetterRound = random.nextBoolean();
        String[] options = new String[6];

        if (isLetterRound) {
            int correctIndex = random.nextInt(letters.length);
            currentCorrect = letters[correctIndex];
            tvGamePrompt.setText("Tap the letter: " + currentCorrect);

            options[0] = currentCorrect;
            for (int i = 1; i < 6; i++) {
                String candidate;
                do {
                    candidate = letters[random.nextInt(letters.length)];
                } while (candidate.equals(currentCorrect) || contains(options, candidate, i));
                options[i] = candidate;
            }
        } else {
            int correctNum = random.nextInt(30) + 1;
            currentCorrect = String.valueOf(correctNum);
            tvGamePrompt.setText("Tap the number: " + currentCorrect);

            options[0] = currentCorrect;
            for (int i = 1; i < 6; i++) {
                String candidate;
                do {
                    candidate = String.valueOf(random.nextInt(30) + 1);
                } while (candidate.equals(currentCorrect) || contains(options, candidate, i));
                options[i] = candidate;
            }
        }

        // Shuffle options
        for (int i = options.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String tmp = options[i];
            options[i] = options[j];
            options[j] = tmp;
        }

        btnGameOpt1.setText(options[0]);
        btnGameOpt2.setText(options[1]);
        btnGameOpt3.setText(options[2]);
        btnGameOpt4.setText(options[3]);
        btnGameOpt5.setText(options[4]);
        btnGameOpt6.setText(options[5]);
    }

    private boolean contains(String[] arr, String value, int upToIndex) {
        for (int i = 0; i < upToIndex; i++) {
            if (value.equals(arr[i])) return true;
        }
        return false;
    }

    private void handleAnswer(Button btn) {
        if (isGameOver) return;

        String chosen = btn.getText().toString();
        if (chosen.equals(currentCorrect)) {
            gameScore++;
            tvGameScore.setText("Score: " + gameScore);
        }
        // Immediately go to next round, whether right or wrong
        generateNewRound();
    }

    private void endGame() {
        isGameOver = true;
        enableOptionButtons(false);

        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (gameScore > bestScore) {
            bestScore = gameScore;
            prefs.edit().putInt(KEY_SPEED_HIGH_SCORE, bestScore).apply();
        }
        tvGameBest.setText("Best: " + bestScore);

        new AlertDialog.Builder(this)
                .setTitle("Time's up!")
                .setMessage("You scored " + gameScore + " in 30 seconds.")
                .setCancelable(false)
                .setPositiveButton("Play again", (d, w) -> startGame())
                .setNegativeButton("Close", (d, w) -> finish())
                .show();
    }

    private void enableOptionButtons(boolean enabled) {
        btnGameOpt1.setEnabled(enabled);
        btnGameOpt2.setEnabled(enabled);
        btnGameOpt3.setEnabled(enabled);
        btnGameOpt4.setEnabled(enabled);
        btnGameOpt5.setEnabled(enabled);
        btnGameOpt6.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
    }
}
