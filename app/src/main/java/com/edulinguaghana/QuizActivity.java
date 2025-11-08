package com.edulinguaghana;

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

    private final String[] letters = {"A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};

    private final String[] matchLetters = {"A","B","C","D"};
    private final String[] matchWords   = {"Apple","Ball","Cat","Dog"};

    private int maxNumber = 20;

    private String correctAnswer;
    private String quizType;
    private String languageCode;

    private TextToSpeech tts;
    private MediaPlayer sfxPlayer;
    private MediaPlayer musicPlayer;
    private boolean isMusicOn = false;

    private final Random random = new Random();

    private int score = 0;
    private int currentQuestion = 1;
    private final int totalQuestions = 10;

    private CountDownTimer countDownTimer;
    private static final long TIME_PER_QUESTION_MS = 15000;
    private boolean questionAnswered = false;

    private SharedPreferences prefs;
    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";
    private static final String KEY_TOTAL_QUIZZES = "TOTAL_QUIZZES";
    private static final String KEY_TOTAL_CORRECT = "TOTAL_CORRECT";
    private static final String KEY_SFX_ENABLED = "SFX_ENABLED";

    private int highScore = 0;
    private int totalQuizzes = 0;
    private int totalCorrectAnswers = 0;
    private boolean isSfxOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestionTitle = findViewById(R.id.tvQuestionTitle);
        tvFeedback      = findViewById(R.id.tvFeedback);
        tvQuestionInfo  = findViewById(R.id.tvQuestionInfo);
        tvScore         = findViewById(R.id.tvScore);
        tvBestScore     = findViewById(R.id.tvBestScore);
        tvTimer         = findViewById(R.id.tvTimer);

        btnPlayAudio    = findViewById(R.id.btnPlayAudio);
        btnOption1      = findViewById(R.id.btnOption1);
        btnOption2      = findViewById(R.id.btnOption2);
        btnOption3      = findViewById(R.id.btnOption3);
        btnNextQuestion = findViewById(R.id.btnNextQuestion);
        btnBackQuiz     = findViewById(R.id.btnBackQuiz);
        btnToggleMusic  = findViewById(R.id.btnToggleMusic);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        quizType     = getIntent().getStringExtra("QUIZ_TYPE");
        if (quizType == null) quizType = "basic";

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(getLocale(languageCode));
            }
        });

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        highScore          = prefs.getInt(KEY_HIGH_SCORE, 0);
        totalQuizzes       = prefs.getInt(KEY_TOTAL_QUIZZES, 0);
        totalCorrectAnswers= prefs.getInt(KEY_TOTAL_CORRECT, 0);
        isSfxOn            = prefs.getBoolean(KEY_SFX_ENABLED, true);

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

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs != null) {
            isSfxOn = prefs.getBoolean(KEY_SFX_ENABLED, true);
        }
    }

    // ---------- MUSIC ----------

    private void initMusicPlayer() {
        try {
            musicPlayer = MediaPlayer.create(this, R.raw.quiz_music);
            if (musicPlayer != null) musicPlayer.setLooping(true);
        } catch (Exception e) {
            Toast.makeText(this, "Background music not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleMusic() {
        if (musicPlayer == null) {
            Toast.makeText(this, "No background music.", Toast.LENGTH_SHORT).show();
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

    private void playAudio() {
        if (tts != null && correctAnswer != null) {
            tts.speak(correctAnswer, TextToSpeech.QUEUE_FLUSH, null, "quiz_audio");
        }
    }

    // ---------- QUESTION FLOW ----------

    private void generateQuestion() {
        resetOptions();
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
                int choice = new Random().nextInt(3);
                if (choice == 0) generateBasicQuestion();
                else if (choice == 1) generateSequenceQuestion();
                else generateMatchingQuestion();
                break;
            default:
                generateBasicQuestion();
        }

        updateHeader();
        startTimer();
    }

    private void generateBasicQuestion() {
        boolean isLetterQuestion = new Random().nextBoolean();
        String[] options = new String[3];

        if (isLetterQuestion) {
            tvQuestionTitle.setText("Which letter did you hear?");
            int correctIndex = new Random().nextInt(letters.length);
            correctAnswer = letters[correctIndex];

            int wrong1 = new Random().nextInt(letters.length);
            int wrong2 = new Random().nextInt(letters.length);
            while (wrong1 == correctIndex) wrong1 = new Random().nextInt(letters.length);
            while (wrong2 == correctIndex || wrong2 == wrong1) wrong2 = new Random().nextInt(letters.length);

            options[0] = letters[correctIndex];
            options[1] = letters[wrong1];
            options[2] = letters[wrong2];
        } else {
            tvQuestionTitle.setText("Which number did you hear?");
            int correctNum = new Random().nextInt(maxNumber) + 1;
            correctAnswer = String.valueOf(correctNum);

            int wrong1 = new Random().nextInt(maxNumber) + 1;
            int wrong2 = new Random().nextInt(maxNumber) + 1;
            while (wrong1 == correctNum) wrong1 = new Random().nextInt(maxNumber) + 1;
            while (wrong2 == correctNum || wrong2 == wrong1) wrong2 = new Random().nextInt(maxNumber) + 1;

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
        int start = new Random().nextInt(10) + 1;
        int[] seq = {start, start + 1, start + 2, start + 3};
        int missing = new Random().nextInt(seq.length);
        correctAnswer = String.valueOf(seq[missing]);
        seq[missing] = -1;

        StringBuilder q = new StringBuilder("Complete the sequence: ");
        for (int n : seq) q.append(n == -1 ? "? " : n + " ");
        tvQuestionTitle.setText(q.toString());

        String[] options = {
                correctAnswer,
                String.valueOf(new Random().nextInt(20) + 1),
                String.valueOf(new Random().nextInt(20) + 1)
        };
        shuffleArray(options);
        btnOption1.setText(options[0]);
        btnOption2.setText(options[1]);
        btnOption3.setText(options[2]);
    }

    private void generateMatchingQuestion() {
        int idx = new Random().nextInt(matchLetters.length);
        correctAnswer = matchWords[idx];
        tvQuestionTitle.setText("Which word starts with " + matchLetters[idx] + "?");

        String[] options = {
                matchWords[idx],
                matchWords[new Random().nextInt(matchWords.length)],
                matchWords[new Random().nextInt(matchWords.length)]
        };
        shuffleArray(options);
        btnOption1.setText(options[0]);
        btnOption2.setText(options[1]);
        btnOption3.setText(options[2]);
    }

    private void resetOptions() {
        btnOption1.setEnabled(true);
        btnOption2.setEnabled(true);
        btnOption3.setEnabled(true);

        btnOption1.setBackgroundResource(R.drawable.bg_quiz_option);
        btnOption2.setBackgroundResource(R.drawable.bg_quiz_option);
        btnOption3.setBackgroundResource(R.drawable.bg_quiz_option);

        tvFeedback.setText("");
        tvFeedback.setTextColor(Color.parseColor("#37474F"));

        btnOption1.clearAnimation();
        btnOption2.clearAnimation();
        btnOption3.clearAnimation();
    }

    private void checkAnswer(Button selectedButton) {
        questionAnswered = true;
        cancelTimer();

        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);
        btnOption3.setEnabled(false);

        String choice = selectedButton.getText().toString();
        boolean correct = choice.equals(correctAnswer);

        if (correct) {
            score++;
            tvFeedback.setText("✅ Correct!");
            tvFeedback.setTextColor(Color.parseColor("#1B5E20"));
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

    private void animateButtonCorrect(Button b) {
        b.setBackgroundResource(R.drawable.bg_quiz_option_correct);
        Animation a = AnimationUtils.loadAnimation(this, R.anim.button_correct_pulse);
        b.startAnimation(a);
    }

    private void animateButtonWrong(Button b) {
        b.setBackgroundResource(R.drawable.bg_quiz_option_wrong);
        Animation a = AnimationUtils.loadAnimation(this, R.anim.button_wrong_shake);
        b.startAnimation(a);
    }

    private void playSfx(boolean correct) {
        if (!isSfxOn) return;

        int resId = correct ? R.raw.correct : R.raw.wrong;
        if (sfxPlayer != null) {
            sfxPlayer.release();
            sfxPlayer = null;
        }
        sfxPlayer = MediaPlayer.create(this, resId);
        if (sfxPlayer != null) sfxPlayer.start();
    }

    // ---------- TIMER & RESULTS ----------

    private void startTimer() {
        tvTimer.setText("Time: 15s");
        countDownTimer = new CountDownTimer(TIME_PER_QUESTION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long s = millisUntilFinished / 1000;
                tvTimer.setText("Time: " + s + "s");
            }

            @Override
            public void onFinish() {
                if (!questionAnswered) {
                    tvTimer.setText("Time: 0s");
                    tvFeedback.setText("⏰ Time's up! Correct: " + correctAnswer);
                    tvFeedback.setTextColor(Color.parseColor("#B71C1C"));
                    playSfx(false);
                    btnOption1.setEnabled(false);
                    btnOption2.setEnabled(false);
                    btnOption3.setEnabled(false);
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

    private void showResultDialog() {
        String message = "You scored " + score + " out of " + totalQuestions + ".";

        totalQuizzes++;
        totalCorrectAnswers += score;

        if (score > highScore) {
            highScore = score;
            message += "\n\n🎉 New high score!";
        }

        SharedPreferences.Editor ed = prefs.edit();
        ed.putInt(KEY_HIGH_SCORE, highScore);
        ed.putInt(KEY_TOTAL_QUIZZES, totalQuizzes);
        ed.putInt(KEY_TOTAL_CORRECT, totalCorrectAnswers);
        ed.apply();

        updateHeader();

        new AlertDialog.Builder(this)
                .setTitle("Quiz Finished")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Restart", (d, w) -> {
                    score = 0;
                    currentQuestion = 1;
                    generateQuestion();
                })
                .setNegativeButton("Close", (d, w) -> finish())
                .show();
    }

    private void updateHeader() {
        tvQuestionInfo.setText("Question " + currentQuestion + " / " + totalQuestions);
        tvScore.setText("Score: " + score);
        tvBestScore.setText("Best: " + highScore + " / " + totalQuestions);
    }

    private Locale getLocale(String code) {
        if (code == null) return Locale.ENGLISH;
        switch (code) {
            case "fr":  return Locale.FRENCH;
            case "ak":  return new Locale("ak");
            case "ee":  return new Locale("ee");
            case "gaa": return new Locale("gaa");
            default:    return Locale.ENGLISH;
        }
    }

    private void shuffleArray(String[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = new Random().nextInt(i + 1);
            String tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (sfxPlayer != null) { sfxPlayer.release(); sfxPlayer = null; }
        if (musicPlayer != null) {
            if (musicPlayer.isPlaying()) musicPlayer.stop();
            musicPlayer.release();
            musicPlayer = null;
        }
    }
}
