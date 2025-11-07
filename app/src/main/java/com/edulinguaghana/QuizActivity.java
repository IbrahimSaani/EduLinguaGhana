package com.edulinguaghana;   // <-- your package

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestionTitle, tvFeedback, tvQuestionInfo, tvScore, tvBestScore, tvTimer;
    private Button btnPlayAudio, btnOption1, btnOption2, btnOption3, btnNextQuestion, btnBackQuiz, btnToggleMusic;

    private String[] letters = {"A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};

    private String[] matchLetters = {"A", "B", "C", "D"};
    private String[] matchWords   = {"Apple", "Ball", "Cat", "Dog"};

    private int maxNumber = 20; // for number questions

    private String correctAnswer;
    private String quizType;      // "basic", "sequence", "matching", "mixed"

    private String languageCode;
    private String languageName;

    private TextToSpeech tts;
    private MediaPlayer sfxPlayer;
    private MediaPlayer musicPlayer;
    private boolean isMusicOn = false;

    private Random random = new Random();

    private int score = 0;
    private int currentQuestion = 1;
    private final int totalQuestions = 10;

    // Timer
    private CountDownTimer countDownTimer;
    private static final long TIME_PER_QUESTION_MS = 15000;
    private boolean questionAnswered = false;

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
        tvTimer = findViewById(R.id.tvTimer);

        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        btnBackQuiz = findViewById(R.id.btnBackQuiz);
        btnToggleMusic = findViewById(R.id.btnToggleMusic);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");
        quizType = getIntent().getStringExtra("QUIZ_TYPE");

        if (languageName == null) languageName = "Unknown";
        if (quizType == null) quizType = "basic";

        // TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(getLocale(languageCode));
            }
        });

        // Progress
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        highScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        totalQuizzes = prefs.getInt(KEY_TOTAL_QUIZZES, 0);
        totalCorrectAnswers = prefs.getInt(KEY_TOTAL_CORRECT, 0);

        // Music
        initMusicPlayer();

        updateHeader();
        generateQuestion();

        btnPlayAudio.setOnClickListener(v -> playAudio());

        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3));

        btnNextQuestion.setOnClickListener(v -> {
            currentQuestion++;
            if (currentQuestion > totalQuestions) {
                showResultDialog();
            } else {
                generateQuestion();
            }
        });

        btnBackQuiz.setOnClickListener(v -> finish());

        btnToggleMusic.setOnClickListener(v -> toggleMusic());
    }

    private void initMusicPlayer() {
        try {
            musicPlayer = MediaPlayer.create(this, R.raw.quiz_music);
            if (musicPlayer != null) {
                musicPlayer.setLooping(true);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Background music not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleMusic() {
        if (musicPlayer == null) {
            Toast.makeText(this, "Background music not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isMusicOn) {
            musicPlayer.pause();
            isMusicOn = false;
            btnToggleMusic.setText("Music: Off");
        } else {
            musicPlayer.start();
            isMusicOn = true;
            btnToggleMusic.setText("Music: On");
        }
    }

    private void generateQuestion() {
        // Reset buttons
        btnOption1.setEnabled(true);
        btnOption2.setEnabled(true);
        btnOption3.setEnabled(true);

        btnOption1.setBackgroundResource(R.drawable.bg_quiz_option);
        btnOption2.setBackgroundResource(R.drawable.bg_quiz_option);
        btnOption3.setBackgroundResource(R.drawable.bg_quiz_option);

        tvFeedback.setText("");
        tvFeedback.setTextColor(Color.parseColor("#37474F"));

        cancelTimer();
        questionAnswered = false;

        switch (quizType) {
            case "sequence":
                generateSequenceQuestion();
                break;
            case "matching":
                generateMatchingQuestion();
                break;
            case "mixed":
                int choice = random.nextInt(3);
                if (choice == 0) {
                    generateBasicQuestion();
                } else if (choice == 1) {
                    generateSequenceQuestion();
                } else {
                    generateMatchingQuestion();
                }
                break;
            case "basic":
            default:
                generateBasicQuestion();
                break;
        }

        updateHeader();
        startTimer();
    }

    private void startTimer() {
        tvTimer.setText("Time: 15s");
        countDownTimer = new CountDownTimer(TIME_PER_QUESTION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText("Time: " + seconds + "s");
            }

            @Override
            public void onFinish() {
                if (!questionAnswered) {
                    tvTimer.setText("Time: 0s");
                    btnOption1.setEnabled(false);
                    btnOption2.setEnabled(false);
                    btnOption3.setEnabled(false);

                    tvFeedback.setText("⏰ Time's up! Correct: " + correctAnswer);
                    tvFeedback.setTextColor(Color.parseColor("#B71C1C"));
                    playSfx(false);
                    updateHeader();
                }
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void generateBasicQuestion() {
        boolean isLetterQuestion = random.nextBoolean();
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
    }

    private void generateSequenceQuestion() {
        int start = random.nextInt(15) + 1;
        int[] seq = {start, start + 1, start + 2, start + 3};

        int missingIndex = random.nextInt(2) + 1; // 1 or 2
        int missingValue = seq[missingIndex];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < seq.length; i++) {
            if (i == missingIndex) sb.append("?, ");
            else sb.append(seq[i]).append(", ");
        }
        String seqText = sb.toString();
        if (seqText.endsWith(", ")) {
            seqText = seqText.substring(0, seqText.length() - 2);
        }

        tvQuestionTitle.setText("Complete the sequence: " + seqText);
        correctAnswer = String.valueOf(missingValue);

        String[] options = new String[3];
        options[0] = correctAnswer;
        int wrong1 = missingValue + random.nextInt(3) + 1;
        int wrong2 = missingValue - (random.nextInt(3) + 1);
        if (wrong2 < 1) wrong2 = wrong1 + 2;

        options[1] = String.valueOf(wrong1);
        options[2] = String.valueOf(wrong2);

        shuffleArray(options);
        btnOption1.setText(options[0]);
        btnOption2.setText(options[1]);
        btnOption3.setText(options[2]);
    }

    private void generateMatchingQuestion() {
        int idx = random.nextInt(matchLetters.length);
        String letter = matchLetters[idx];
        String wordCorrect = matchWords[idx];

        tvQuestionTitle.setText("Which word starts with letter " + letter + "?");
        correctAnswer = wordCorrect;

        int wrong1 = random.nextInt(matchLetters.length);
        int wrong2 = random.nextInt(matchLetters.length);
        while (wrong1 == idx) wrong1 = random.nextInt(matchLetters.length);
        while (wrong2 == idx || wrong2 == wrong1) wrong2 = random.nextInt(matchLetters.length);

        String[] options = new String[3];
        options[0] = wordCorrect;
        options[1] = matchWords[wrong1];
        options[2] = matchWords[wrong2];

        shuffleArray(options);
        btnOption1.setText(options[0]);
        btnOption2.setText(options[1]);
        btnOption3.setText(options[2]);
    }

    private void playAudio() {
        if (tts != null && correctAnswer != null) {
            tts.speak(correctAnswer, TextToSpeech.QUEUE_FLUSH, null, "quiz_audio");
        }
    }

    private void checkAnswer(Button selectedButton) {
        questionAnswered = true;
        cancelTimer();

        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);
        btnOption3.setEnabled(false);

        String chosen = selectedButton.getText().toString();

        if (chosen.equals(correctAnswer)) {
            tvFeedback.setText("✅ Correct!");
            tvFeedback.setTextColor(Color.parseColor("#1B5E20"));
            score++;
            playSfx(true);
            animateButtonCorrect(selectedButton);
        } else {
            tvFeedback.setText("❌ Wrong! Correct: " + correctAnswer);
            tvFeedback.setTextColor(Color.parseColor("#B71C1C"));
            playSfx(false);
            animateButtonWrong(selectedButton);
        }
        updateHeader();
    }

    private void animateButtonCorrect(Button button) {
        button.setBackgroundResource(R.drawable.bg_quiz_option_correct);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_correct_pulse);
        button.startAnimation(anim);
    }

    private void animateButtonWrong(Button button) {
        button.setBackgroundResource(R.drawable.bg_quiz_option_wrong);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_wrong_shake);
        button.startAnimation(anim);
    }

    private void playSfx(boolean isCorrect) {
        int resId = isCorrect ? R.raw.correct : R.raw.wrong;

        if (sfxPlayer != null) {
            sfxPlayer.release();
            sfxPlayer = null;
        }

        try {
            sfxPlayer = MediaPlayer.create(this, resId);
            if (sfxPlayer != null) {
                sfxPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    sfxPlayer = null;
                });
                sfxPlayer.start();
            }
        } catch (Exception e) {
            // Ignore if sound missing
        }
    }

    private void updateHeader() {
        tvQuestionInfo.setText("Question " + currentQuestion + " / " + totalQuestions);
        tvScore.setText("Score: " + score);
        tvBestScore.setText("Best: " + highScore + " / " + totalQuestions);
    }

    private void showResultDialog() {
        String message = "You scored " + score + " out of " + totalQuestions + ".";

        totalQuizzes++;
        totalCorrectAnswers += score;

        String achievement = null;
        if (score == totalQuestions) {
            achievement = "⭐ Perfect score!";
        } else if (score >= 8) {
            achievement = "🏅 Excellent performance!";
        } else if (score >= 5) {
            achievement = "🎓 Keep practicing!";
        }

        if (achievement != null) {
            message += "\n\n" + achievement;
            Toast.makeText(this, "Achievement unlocked: " + achievement, Toast.LENGTH_LONG).show();
        }

        boolean isNewHighScore = false;
        if (score > highScore) {
            highScore = score;
            isNewHighScore = true;
            message += "\n\n🎉 New high score!";
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_HIGH_SCORE, highScore);
        editor.putInt(KEY_TOTAL_QUIZZES, totalQuizzes);
        editor.putInt(KEY_TOTAL_CORRECT, totalCorrectAnswers);
        editor.apply();

        updateHeader();

        if (isNewHighScore) {
            animateLevelUp();
        }

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

    private void animateLevelUp() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.level_up_pulse);
        tvBestScore.startAnimation(anim);
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
        cancelTimer();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (sfxPlayer != null) {
            sfxPlayer.stop();
            sfxPlayer.release();
            sfxPlayer = null;
        }
        if (musicPlayer != null) {
            if (musicPlayer.isPlaying()) musicPlayer.stop();
            musicPlayer.release();
            musicPlayer = null;
        }
    }
}
