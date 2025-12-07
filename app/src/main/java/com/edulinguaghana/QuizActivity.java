package com.edulinguaghana;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class QuizActivity extends AppCompatActivity {

    // Views
    private TextView tvGameTimer, tvGameScore, tvGameBest, tvGameFeedback, tvGamePrompt, tvStartTitle, tvStartDescription, tvEndTitle, tvFinalScore, tvEndBestScore;
    private MaterialButton btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6, btnStartQuiz, btnPlayAgain, btnEndQuit;
    private View btnPlayAudio;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private View startQuizContainer, quizContentContainer, endQuizContainer;
    private ImageView ivQuizIcon;

    // Game state
    private int score = 0;
    private int bestScore = 0;
    private String currentCorrectAnswer;
    private String currentPromptTtsText;   // what TTS should actually say

    private CountDownTimer countDownTimer;
    private long remainingTime = 30000L;             // total quiz time: 30s
    private static final long PENALTY_TIME = 1000L;  // -1s penalty for wrong answer

    // SharedPreferences keys
    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE_PREFIX = "QUIZ_HIGH_SCORE_";
    private static final String KEY_SFX_ENABLED = "SFX_ENABLED";

    // Alphabet (letters-only)
    private final String[] alphabet = {
            "A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"
    };

    // Matching data (letter → word so kids can relate)
    private final String[] matchLetters = {"A","B","C","D","E","F"};
    private final String[] matchWords   = {"Apple","Ball","Cat","Dog","Egg","Fish"};

    // Number settings
    private static final int MAX_NUMBER = 30;

    // TTS
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private String languageCode;   // "en", "fr", etc.
    private String languageName;   // "English", "French", etc.

    // SFX
    private boolean isSfxOn = true;

    // Quiz mode: "letters", "numbers", "sequence", "matching", "mixed"
    private String quizType = "letters";

    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // --- Get language & mode from intent ---
        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");

        String rawType = getIntent().getStringExtra("QUIZ_TYPE");
        if (rawType == null) rawType = getIntent().getStringExtra("QUIZ_MODE");

        if (languageCode == null) languageCode = "en";
        if (languageName == null) languageName = "Unknown";
        if (rawType == null) rawType = "letters";

        quizType = normalizeQuizType(rawType);

        // --- Bind views ---
        bindViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setupStartScreen();

        // Load prefs (high score & SFX setting)
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(getHighScoreKey(), 0);
        isSfxOn   = prefs.getBoolean(KEY_SFX_ENABLED, true);
        tvGameBest.setText("Best: " + bestScore);

        initTTS();

        btnStartQuiz.setOnClickListener(v -> showQuizContent());

        // Answer buttons
        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4));
        btnOption5.setOnClickListener(v -> checkAnswer(btnOption5));
        btnOption6.setOnClickListener(v -> checkAnswer(btnOption6));

        // Replay audio
        if (btnPlayAudio != null) {
            btnPlayAudio.setOnClickListener(v -> speakPrompt());
        }

        // End screen buttons
        btnPlayAgain.setOnClickListener(v -> {
            endQuizContainer.setVisibility(View.GONE);
            showQuizContent();
        });
        btnEndQuit.setOnClickListener(v -> finish());
    }

    private void bindViews() {
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        startQuizContainer = findViewById(R.id.startQuizContainer);
        quizContentContainer = findViewById(R.id.quizContentContainer);
        endQuizContainer = findViewById(R.id.endQuizContainer);

        tvStartTitle = findViewById(R.id.tvStartTitle);
        tvStartDescription = findViewById(R.id.tvStartDescription);
        ivQuizIcon = findViewById(R.id.ivQuizIcon);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);

        tvGameTimer    = findViewById(R.id.tvTimer);
        tvGameScore    = findViewById(R.id.tvScore);
        tvGameBest     = findViewById(R.id.tvBest);
        tvGameFeedback = findViewById(R.id.tvFeedback);
        tvGamePrompt   = findViewById(R.id.tvGamePrompt);

        btnPlayAudio   = findViewById(R.id.btnPlayAudio);

        btnOption1     = findViewById(R.id.btnOption1);
        btnOption2     = findViewById(R.id.btnOption2);
        btnOption3     = findViewById(R.id.btnOption3);
        btnOption4     = findViewById(R.id.btnOption4);
        btnOption5     = findViewById(R.id.btnOption5);
        btnOption6     = findViewById(R.id.btnOption6);

        tvEndTitle = findViewById(R.id.tvEndTitle);
        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvEndBestScore = findViewById(R.id.tvEndBestScore);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);
        btnEndQuit = findViewById(R.id.btnEndQuit);
    }

    private void setupStartScreen() {
        String modeLabel, description;
        int iconRes;

        switch (quizType) {
            case "numbers":
                modeLabel = "Numbers Quiz";
                description = "Listen to the number and choose the correct option.";
                iconRes = R.drawable.ic_quiz_numbers;
                break;
            case "sequence":
                modeLabel = "Number Sequence Quiz";
                description = "Complete the sequence by finding the missing number.";
                iconRes = R.drawable.ic_quiz_sequence;
                break;
            case "matching":
                modeLabel = "Matching Quiz";
                description = "Match the letter to the word that starts with it.";
                iconRes = R.drawable.ic_quiz_matching;
                break;
            case "mixed":
                modeLabel = "Mixed Quiz";
                description = "A mix of letter and number questions.";
                iconRes = R.drawable.ic_quiz_mixed;
                break;
            case "letters":
            default:
                modeLabel = "Letters Quiz";
                description = "Listen to the letter and choose the correct option.";
                iconRes = R.drawable.ic_quiz_letters;
                break;
        }
        getSupportActionBar().setTitle(modeLabel + " – " + languageName);
        tvStartTitle.setText(modeLabel);
        tvStartDescription.setText(description);
        ivQuizIcon.setImageResource(iconRes);

        Animation startButtonAnim = AnimationUtils.loadAnimation(this, R.anim.bounce_pop);
        btnStartQuiz.startAnimation(startButtonAnim);
    }

    private void showQuizContent() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        startQuizContainer.setVisibility(View.GONE);
        quizContentContainer.setVisibility(View.VISIBLE);
        appBarLayout.setVisibility(View.VISIBLE);
        quizContentContainer.startAnimation(fadeIn);
        appBarLayout.startAnimation(fadeIn);
        startGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        isSfxOn = prefs.getBoolean(KEY_SFX_ENABLED, true);
    }

    private String normalizeQuizType(String raw) {
        String t = raw.toLowerCase(Locale.ROOT);
        if (t.contains("letter")) return "letters";
        if (t.contains("sequ"))   return "sequence";
        if (t.contains("match"))  return "matching";
        if (t.contains("mix"))    return "mixed";
        if (t.contains("num"))    return "numbers";
        return t;
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                switch (languageCode.toLowerCase()) {
                    case "fr":
                        tts.setLanguage(Locale.FRENCH);
                        break;
                    case "en":
                    default:
                        tts.setLanguage(Locale.US);
                        break;
                }
                ttsReady = true;
                if (currentPromptTtsText != null) {
                    speakPrompt();
                }
            }
        });
    }

    private void speakPrompt() {
        if (!ttsReady || currentPromptTtsText == null) return;
        tts.stop();
        tts.speak(currentPromptTtsText, TextToSpeech.QUEUE_FLUSH, null, "quiz_tts");
    }

    private void startGame() {
        score = 0;
        remainingTime = 30000L;
        tvGameScore.setText("Score: 0");
        tvGameFeedback.setText("");
        generateNewQuestion();
        startTimer();
    }

    private void generateNewQuestion() {
        resetButtons();
        switch (quizType) {
            case "numbers": generateNumberQuestion(); break;
            case "sequence": generateSequenceQuestion(); break;
            case "matching": generateMatchingQuestion(); break;
            case "mixed": generateMixedQuestion(); break;
            default: generateLetterQuestion(); break;
        }
        speakPrompt();
    }

    private void generateLetterQuestion() {
        currentCorrectAnswer = alphabet[random.nextInt(alphabet.length)];
        List<String> options = new ArrayList<>();
        Set<String> used = new HashSet<>();
        options.add(currentCorrectAnswer);
        used.add(currentCorrectAnswer);
        while (options.size() < 6) {
            String pick = alphabet[random.nextInt(alphabet.length)];
            if (!used.contains(pick)) {
                options.add(pick);
                used.add(pick);
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
        currentPromptTtsText = currentCorrectAnswer;
    }

    private void generateNumberQuestion() {
        int correctNumber = random.nextInt(MAX_NUMBER) + 1;
        currentCorrectAnswer = String.valueOf(correctNumber);
        List<String> options = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        options.add(currentCorrectAnswer);
        used.add(correctNumber);
        while (options.size() < 6) {
            int pick = random.nextInt(MAX_NUMBER) + 1;
            if (!used.contains(pick)) {
                options.add(String.valueOf(pick));
                used.add(pick);
            }
        }
        Collections.shuffle(options);
        btnOption1.setText(options.get(0));
        btnOption2.setText(options.get(1));
        btnOption3.setText(options.get(2));
        btnOption4.setText(options.get(3));
        btnOption5.setText(options.get(4));
        btnOption6.setText(options.get(5));
        tvGamePrompt.setText("Which number did you hear?");
        currentPromptTtsText = currentCorrectAnswer;
    }

    private void generateSequenceQuestion() {
        int start = random.nextInt(20) + 1;
        int step = 1;
        int[] seq = new int[4];
        for (int i = 0; i < 4; i++) {
            seq[i] = start + i * step;
        }
        int missingIndex = random.nextInt(2) + 1;
        int missingValue = seq[missingIndex];
        currentCorrectAnswer = String.valueOf(missingValue);
        StringBuilder sb = new StringBuilder("Complete the sequence: ");
        for (int i = 0; i < 4; i++) {
            if (i == missingIndex) {
                sb.append("?, ");
            } else {
                sb.append(seq[i]).append(", ");
            }
        }
        String prompt = sb.toString();
        if (prompt.endsWith(", ")) {
            prompt = prompt.substring(0, prompt.length() - 2);
        }
        tvGamePrompt.setText(prompt);
        List<String> options = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        options.add(currentCorrectAnswer);
        used.add(missingValue);
        while (options.size() < 6) {
            int delta = random.nextInt(3) + 1;
            int candidate = random.nextBoolean() ? missingValue + delta : missingValue - delta;
            if (candidate < 1) candidate = missingValue + delta + 1;
            if (!used.contains(candidate)) {
                options.add(String.valueOf(candidate));
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
        currentPromptTtsText = "Find the missing number";
    }

    private void generateMatchingQuestion() {
        int idx = random.nextInt(matchLetters.length);
        String letter = matchLetters[idx];
        String correctWord = matchWords[idx];
        currentCorrectAnswer = correctWord;
        tvGamePrompt.setText("Which word starts with letter " + letter + "?");
        List<String> options = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        options.add(correctWord);
        used.add(idx);
        while (options.size() < 6 && used.size() < matchWords.length) {
            int pick = random.nextInt(matchWords.length);
            if (!used.contains(pick)) {
                options.add(matchWords[pick]);
                used.add(pick);
            }
        }
        while (options.size() < 6) {
            options.add(matchWords[random.nextInt(matchWords.length)]);
        }
        Collections.shuffle(options);
        btnOption1.setText(options.get(0));
        btnOption2.setText(options.get(1));
        btnOption3.setText(options.get(2));
        btnOption4.setText(options.get(3));
        btnOption5.setText(options.get(4));
        btnOption6.setText(options.get(5));
        currentPromptTtsText = letter;
    }

    private void generateMixedQuestion() {
        if (random.nextBoolean()) {
            generateLetterQuestion();
        } else {
            generateNumberQuestion();
        }
    }

    private void checkAnswer(MaterialButton clickedButton) {
        cancelTimer();
        String answer = clickedButton.getText().toString();
        boolean isCorrect = answer.equals(currentCorrectAnswer);
        Animation correctAnim = AnimationUtils.loadAnimation(this, R.anim.correct_answer);
        Animation wrongAnim = AnimationUtils.loadAnimation(this, R.anim.wrong_answer);

        if (isCorrect) {
            score++;
            tvGameFeedback.setText("✅ Correct!");
            tvGameFeedback.setTextColor(ContextCompat.getColor(this, R.color.correctAnswer));
            clickedButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.correctAnswer)));
            clickedButton.startAnimation(correctAnim);
            playSfx(true);
            if (score > bestScore) {
                bestScore = score;
                saveHighScore();
                animateHighScore();
            }
        } else {
            tvGameFeedback.setText("❌ Wrong! Correct: " + currentCorrectAnswer);
            tvGameFeedback.setTextColor(ContextCompat.getColor(this, R.color.wrongAnswer));
            clickedButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.wrongAnswer)));
            clickedButton.startAnimation(wrongAnim);
            playSfx(false);
            remainingTime -= PENALTY_TIME;
            if (remainingTime < 0) remainingTime = 0;
        }

        tvGameScore.setText("Score: " + score);
        setButtonsEnabled(false);

        new Handler().postDelayed(() -> {
            if (remainingTime <= 0) {
                endQuiz();
            } else {
                generateNewQuestion();
                startTimer();
            }
        }, 1200);
    }

    private void startTimer() {
        cancelTimer();
        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                long s = millisUntilFinished / 1000;
                tvGameTimer.setText("Time: " + s + "s");
            }

            @Override
            public void onFinish() {
                remainingTime = 0;
                tvGameTimer.setText("Time: 0s");
                endQuiz();
            }
        }.start();
    }

    private void endQuiz() {
        cancelTimer();
        setButtonsEnabled(false);

        // Update overall progress
        ProgressManager.updateProgress(this, quizType, score, score);

        // Show end screen
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        quizContentContainer.setVisibility(View.GONE);
        appBarLayout.setVisibility(View.GONE);
        endQuizContainer.setVisibility(View.VISIBLE);
        endQuizContainer.startAnimation(fadeIn);

        tvFinalScore.setText("Your Score: " + score);
        tvEndBestScore.setText("Best Score: " + bestScore);
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void resetButtons() {
        tvGameFeedback.setText("");
        setButtonsEnabled(true);
        MaterialButton[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6};
        for (MaterialButton button : buttons) {
            button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.buttonSecondary)));
            button.clearAnimation();
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnOption1.setEnabled(enabled);
        btnOption2.setEnabled(enabled);
        btnOption3.setEnabled(enabled);
        btnOption4.setEnabled(enabled);
        btnOption5.setEnabled(enabled);
        btnOption6.setEnabled(enabled);
    }

    private String getHighScoreKey() {
        return KEY_HIGH_SCORE_PREFIX + quizType;
    }

    private void saveHighScore() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putInt(getHighScoreKey(), bestScore).apply();
        tvGameBest.setText("Best: " + bestScore);
    }

    private void animateHighScore() {
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.glow_pulse);
        tvGameBest.startAnimation(pulse);
    }

    private void playSfx(boolean correct) {
        if (!isSfxOn) return;
        int res = correct ? R.raw.correct : R.raw.wrong;
        MediaPlayer mp = MediaPlayer.create(this, res);
        if (mp != null) {
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
