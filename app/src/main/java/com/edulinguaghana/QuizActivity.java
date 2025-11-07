package com.edulinguaghana;   // <-- your package

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestionTitle, tvFeedback, tvQuestionInfo, tvScore, tvBestScore;
    private Button btnPlayAudio, btnOption1, btnOption2, btnOption3, btnNextQuestion, btnBackQuiz;

    private String[] letters = {"A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    private int maxNumber = 20; // numbers 1-20 for quiz

    private String correctAnswer;
    private boolean isLetterQuestion;

    private String languageCode;
    private String languageName;
    private boolean useLocalAudio = false; // placeholder if you add local audio later

    private MediaPlayer mediaPlayer;   // main audio (if you add local audio later)
    private MediaPlayer sfxPlayer;     // correct/wrong sound effects (optional)
    private TextToSpeech tts;
    private Random random = new Random();

    private int score = 0;
    private int currentQuestion = 1;
    private final int totalQuestions = 10;

    // Progress tracking
    private SharedPreferences prefs;
    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";
    private static final String KEY_TOTAL_QUIZZES = "TOTAL_QUIZZES";
    private static final String KEY_TOTAL_CORRECT = "TOTAL_CORRECT";

    private int highScore = 0;
    private int totalQuizzes = 0;
    private int totalCorrectAnswers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestionTitle = findViewById(R.id.tvQuestionTitle);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvQuestionInfo = findViewById(R.id.tvQuestionInfo);
        tvScore = findViewById(R.id.tvScore);
        tvBestScore = findViewById(R.id.tvBestScore);

        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        btnBackQuiz = findViewById(R.id.btnBackQuiz);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");
        if (languageName == null) languageName = "Unknown";

        // For now, use TTS for all languages (local audio to be plugged in later)
        useLocalAudio = false;

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(getLocale(languageCode));
            }
        });

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        totalQuizzes = prefs.getInt(KEY_TOTAL_QUIZZES, 0);
        totalCorrectAnswers = prefs.getInt(KEY_TOTAL_CORRECT, 0);

        updateHeader();
        generateQuestion();

        btnPlayAudio.setOnClickListener(v -> playAudio());

        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1.getText().toString()));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2.getText().toString()));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3.getText().toString()));

        btnNextQuestion.setOnClickListener(v -> {
            currentQuestion++;
            if (currentQuestion > totalQuestions) {
                showResultDialog();
            } else {
                generateQuestion();
            }
        });

        btnBackQuiz.setOnClickListener(v -> finish());
    }

    private void generateQuestion() {
        btnOption1.setEnabled(true);
        btnOption2.setEnabled(true);
        btnOption3.setEnabled(true);
        tvFeedback.setText("");
        tvFeedback.setTextColor(Color.parseColor("#37474F"));

        isLetterQuestion = random.nextBoolean();

        String[] options = new String[3];

        if (isLetterQuestion) {
            tvQuestionTitle.setText("Which letter did you hear?");
            int correctIndex = random.nextInt(letters.length);
            correctAnswer = letters[correctIndex];

            int wrong1 = random.nextInt(letters.length);
            int wrong2 = random.nextInt(letters.length);

            while (wrong1 == correctIndex) wrong1 = random.nextInt(letters.length);
            while (wrong2 == correctIndex || wrong2 == wrong1) wrong2 = random.nextInt(letters.length);

            options[0] = letters[correctIndex];
            options[1] = letters[wrong1];
            options[2] = letters[wrong2];
        } else {
            tvQuestionTitle.setText("Which number did you hear?");
            int correctNum = random.nextInt(maxNumber) + 1;
            correctAnswer = String.valueOf(correctNum);

            int wrong1 = random.nextInt(maxNumber) + 1;
            int wrong2 = random.nextInt(maxNumber) + 1;

            while (wrong1 == correctNum) wrong1 = random.nextInt(maxNumber) + 1;
            while (wrong2 == correctNum || wrong2 == wrong1) wrong2 = random.nextInt(maxNumber) + 1;

            options[0] = String.valueOf(correctNum);
            options[1] = String.valueOf(wrong1);
            options[2] = String.valueOf(wrong2);
        }

        shuffleArray(options);

        btnOption1.setText(options[0]);
        btnOption2.setText(options[1]);
        btnOption3.setText(options[2]);

        updateHeader();
    }

    private void playAudio() {
        // For now just TTS; later you can plug in local audio here if desired
        if (tts != null) {
            tts.speak(correctAnswer, TextToSpeech.QUEUE_FLUSH, null, "quiz_audio");
        }
    }

    private void checkAnswer(String chosen) {
        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);
        btnOption3.setEnabled(false);

        if (chosen.equals(correctAnswer)) {
            tvFeedback.setText("✅ Correct!");
            tvFeedback.setTextColor(Color.parseColor("#1B5E20"));
            score++;
            playSfx(true);
        } else {
            tvFeedback.setText("❌ Wrong! Correct: " + correctAnswer);
            tvFeedback.setTextColor(Color.parseColor("#B71C1C"));
            playSfx(false);
        }
        updateHeader();
    }

    private void playSfx(boolean correct) {
        // Optional: add res/raw/correct.mp3 and wrong.mp3
        int resId = getResources().getIdentifier(correct ? "correct" : "wrong", "raw", getPackageName());
        if (resId == 0) {
            return; // no sfx yet
        }

        if (sfxPlayer != null) {
            sfxPlayer.release();
        }

        sfxPlayer = MediaPlayer.create(this, resId);
        sfxPlayer.setOnCompletionListener(mp -> {
            mp.release();
            sfxPlayer = null;
        });
        sfxPlayer.start();
    }

    private void updateHeader() {
        tvQuestionInfo.setText("Question " + currentQuestion + " / " + totalQuestions);
        tvScore.setText("Score: " + score);
        tvBestScore.setText("Best: " + highScore + " / " + totalQuestions);
    }

    private void showResultDialog() {
        String message = "You scored " + score + " out of " + totalQuestions + ".";

        // Update totals
        totalQuizzes++;
        totalCorrectAnswers += score;

        // Update high score
        if (score > highScore) {
            highScore = score;
            message += "\n\n🎉 New high score!";
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_HIGH_SCORE, highScore);
        editor.putInt(KEY_TOTAL_QUIZZES, totalQuizzes);
        editor.putInt(KEY_TOTAL_CORRECT, totalCorrectAnswers);
        editor.apply();

        updateHeader();

        new AlertDialog.Builder(this)
                .setTitle("Quiz Finished")
                .setMessage(message)
                .setPositiveButton("Restart", (dialog, which) -> {
                    score = 0;
                    currentQuestion = 1;
                    generateQuestion();
                })
                .setNegativeButton("Close", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private Locale getLocale(String code) {
        if (code == null) return Locale.ENGLISH;
        switch (code) {
            case "fr":
                return Locale.FRENCH;
            default:
                return Locale.ENGLISH;
        }
    }

    private void shuffleArray(String[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (sfxPlayer != null) {
            sfxPlayer.stop();
            sfxPlayer.release();
            sfxPlayer = null;
        }
    }
}
