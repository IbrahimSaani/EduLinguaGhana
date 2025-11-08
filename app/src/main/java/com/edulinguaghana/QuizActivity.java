package com.edulinguaghana;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class QuizActivity extends AppCompatActivity {

    // Views
    private TextView tvGameTitle, tvGameTimer, tvGameScore, tvGameBest, tvGameFeedback, tvGamePrompt;
    private Button btnPlayAudio, btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6, btnBackQuiz;

    // Game variables
    private int score = 0;
    private int bestScore = 0;
    private String currentCorrectAnswer;
    private CountDownTimer countDownTimer;
    private String languageCode;

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";

    // Timer settings
    private static final long TIME_PER_QUESTION_MS = 30000; // 30 seconds for each question

    // Game data (Example: English Alphabet)
    private String[] questions = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    // TTS instance
    private TextToSpeech textToSpeech;
    private boolean isTtsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_game);

        // Initialize views
        tvGameTitle = findViewById(R.id.tvGameTitle);
        tvGameTimer = findViewById(R.id.tvGameTimer);
        tvGameScore = findViewById(R.id.tvGameScore);
        tvGameBest = findViewById(R.id.tvGameBest);
        tvGameFeedback = findViewById(R.id.tvGameFeedback);
        tvGamePrompt = findViewById(R.id.tvGamePrompt);

        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        btnOption1 = findViewById(R.id.btnGameOpt1);
        btnOption2 = findViewById(R.id.btnGameOpt2);
        btnOption3 = findViewById(R.id.btnGameOpt3);
        btnOption4 = findViewById(R.id.btnGameOpt4);
        btnOption5 = findViewById(R.id.btnGameOpt5);
        btnOption6 = findViewById(R.id.btnGameOpt6);
        btnBackQuiz = findViewById(R.id.btnGameBack);

        // Retrieve the language code passed from the previous activity
        languageCode = getIntent().getStringExtra("LANG_CODE");

        // Initialize Text-to-Speech (TTS)
        initializeTTS();

        // Load the best score
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        tvGameBest.setText("Best: " + bestScore);

        // Start the game
        startGame();

        // Set up button listeners
        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1.getText().toString()));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2.getText().toString()));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3.getText().toString()));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4.getText().toString()));
        btnOption5.setOnClickListener(v -> checkAnswer(btnOption5.getText().toString()));
        btnOption6.setOnClickListener(v -> checkAnswer(btnOption6.getText().toString()));

        btnBackQuiz.setOnClickListener(v -> {
            cancelTimer();
            finish(); // Go back to the previous screen
        });

        // Set up Play Audio button listener (for TTS)
        btnPlayAudio.setOnClickListener(v -> playAudio());
    }

    // Initialize Text-to-Speech (TTS)
    private void initializeTTS() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                setTTSLanguage(languageCode);  // Set the language based on the languageCode
                isTtsInitialized = true;
                // Play audio for the first question now that TTS is ready
                playAudio();
            } else {
                Log.e("TTS", "Initialization failed with status: " + status);
                Toast.makeText(this, "TTS Initialization Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Set the appropriate language for TTS
    private void setTTSLanguage(String langCode) {
        Locale locale;

        // Check which language code was passed and set the appropriate locale for TTS
        switch (langCode) {
            case "fr":
                locale = Locale.FRENCH;  // French TTS
                break;
            case "en":
            default:
                locale = Locale.ENGLISH;  // English TTS (default)
                break;
        }

        // Set the language of the TextToSpeech engine
        int result = textToSpeech.setLanguage(locale);

        // ### START: CORRECTION ###
        // The constant LANG_COUNTRY_NOT_SUPPORTED does not exist.
        // The correct constants to check for errors are LANG_MISSING_DATA and LANG_NOT_SUPPORTED.
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Handle the error if the language is not supported or data is missing
            Log.e("TTS", "The Language is not supported!");
            Toast.makeText(this, "The selected language is not supported.", Toast.LENGTH_SHORT).show();
        }
        // ### END: CORRECTION ###
    }

    // Start a new game
    private void startGame() {
        score = 0;
        tvGameScore.setText("Score: " + score);
        tvGameFeedback.setText("");

        generateNewQuestion();
        startTimer();
    }

    // Generate a new question
    private void generateNewQuestion() {
        Random random = new Random();
        // Pick a random correct answer from the questions array
        currentCorrectAnswer = questions[random.nextInt(questions.length)];

        // Generate 6 unique options, including the correct answer
        List<String> options = new ArrayList<>();
        options.add(currentCorrectAnswer);

        Set<String> usedOptions = new HashSet<>();
        usedOptions.add(currentCorrectAnswer);

        while (options.size() < 6) {
            String randomOption = questions[random.nextInt(questions.length)];
            if (!usedOptions.contains(randomOption)) {
                options.add(randomOption);
                usedOptions.add(randomOption);
            }
        }

        // Shuffle the options so the correct answer isn't always first
        Collections.shuffle(options);

        // Set options to buttons
        btnOption1.setText(options.get(0));
        btnOption2.setText(options.get(1));
        btnOption3.setText(options.get(2));
        btnOption4.setText(options.get(3));
        btnOption5.setText(options.get(4));
        btnOption6.setText(options.get(5));

        tvGamePrompt.setText("Which letter did you hear?");
        resetButtonStyles();

        // Play the audio for the new question if TTS is ready
        if (isTtsInitialized) {
            playAudio();
        }
    }

    // Check the selected answer
    private void checkAnswer(String selectedAnswer) {
        cancelTimer(); // Stop the timer once an answer is submitted

        if (selectedAnswer.equals(currentCorrectAnswer)) {
            score++;
            tvGameFeedback.setText("✅ Correct!");
            playSfx(true);
        } else {
            tvGameFeedback.setText("❌ Incorrect! The correct answer was " + currentCorrectAnswer);
            playSfx(false);
        }
        tvGameScore.setText("Score: " + score);

        // Update the best score if necessary
        if (score > bestScore) {
            bestScore = score;
            tvGameBest.setText("Best: " + bestScore);
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_HIGH_SCORE, bestScore);
            editor.apply();
        }

        // Move to the next question after a short delay to show feedback
        new android.os.Handler().postDelayed(() -> {
            generateNewQuestion();
            startTimer();
        }, 1500); // 1.5-second delay
    }

    // Start the countdown timer
    private void startTimer() {
        cancelTimer(); // Ensure no other timer is running
        countDownTimer = new CountDownTimer(TIME_PER_QUESTION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvGameTimer.setText("Time: " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                tvGameTimer.setText("Time: 0s");
                tvGameFeedback.setText("⏰ Time's up! The correct answer was " + currentCorrectAnswer);
                playSfx(false);
                // Move to the next question after a short delay
                new android.os.Handler().postDelayed(() -> {
                    generateNewQuestion();
                    startTimer();
                }, 1500);
            }
        }.start();
    }

    // Cancel the timer
    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    // Play audio for the current question using TTS
    private void playAudio() {
        if (isTtsInitialized && currentCorrectAnswer != null) {
            textToSpeech.speak(currentCorrectAnswer, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // Play sound effect (correct or wrong answer)
    private void playSfx(boolean isCorrect) {
        int resId = isCorrect ? R.raw.correct : R.raw.wrong;
        MediaPlayer sfxPlayer = MediaPlayer.create(this, resId);
        sfxPlayer.setOnCompletionListener(MediaPlayer::release); // Releases resources automatically
        sfxPlayer.start();
    }

    // Reset button appearances (optional but good practice)
    private void resetButtonStyles() {
        tvGameFeedback.setText("");
        // You can add code here to reset button colors if you change them on correct/incorrect answers
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer(); // Stop the timer to prevent leaks
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
