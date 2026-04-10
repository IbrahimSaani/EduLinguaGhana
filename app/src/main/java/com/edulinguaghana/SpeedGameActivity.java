package com.edulinguaghana;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edulinguaghana.tts.OfflineGhanaLPTtsService;
import com.edulinguaghana.utils.LanguageConversionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private String quizType = "letters";  // NEW: Support different quiz modes
    private String[] alphabet;  // NEW: Language-specific alphabet

    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_HIGH_SCORE = "HIGH_SCORE";
    private static final String KEY_SFX_ENABLED = "SFX_ENABLED";

    // Timer settings (total round time)
    private static final long TOTAL_TIME_MS = 30000;  // 30s round
    private static final int MAX_NUMBER = 50;  // NEW: For number questions

    // Letters to use in the game - KEEP for compatibility
    private final String[] questions = {
            "A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"
    };

    // Hardcoded matching pairs for matching quiz mode
    private final String[] matchLetters = {"A", "B", "C"};
    private final String[] matchWords = {"Apple", "Ball", "Cat"};

    // TTS
    private TextToSpeech tts;
    private boolean isTtsReady = false;
    private String languageCode;   // "en", "fr", etc.
    private OfflineGhanaLPTtsService offlineTts;  // NEW: For Ghanaian languages
    private boolean isOfflineTtsPlaying = false;  // NEW: Track offline TTS state

    // SFX
    private boolean isSfxOn = true;
    private MediaPlayer sfxPlayer;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_game);

        // --- Get language code and quiz type from intent ---
        languageCode = getIntent().getStringExtra("LANG_CODE");
        if (languageCode == null) {
            languageCode = "en";
        }

        // NEW: Get quiz type from intent (letters, numbers, sequence, matching, mixed)
        String rawType = getIntent().getStringExtra("QUIZ_TYPE");
        if (rawType == null) rawType = "letters";
        quizType = normalizeQuizType(rawType);

        // NEW: Get language-specific alphabet
        alphabet = LanguageConversionUtils.getAlphabetForLanguage(languageCode);

        // ...existing code...
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
        // NEW: Initialize offline TTS for Ghanaian languages
        if (LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
            offlineTts = new OfflineGhanaLPTtsService(this);
        }

        // Initialize Android TTS for all languages
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale locale = LanguageConversionUtils.getLocaleForLanguage(languageCode);
                int result = tts.setLanguage(locale);
                if (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true;
                }
            }
        });
    }

    private void playAudio() {
        if (currentCorrectAnswer == null) return;

        // NEW: Use offline TTS for Ghanaian languages
        if (LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
            speakWithOfflineTts(currentCorrectAnswer);
        } else {
            // Use Android TTS for English/French
            if (!isTtsReady) return;
            tts.stop();
            tts.speak(currentCorrectAnswer, TextToSpeech.QUEUE_FLUSH, null, "speed_game_letter");
        }
    }

    // NEW: Speak using offline TTS
    private void speakWithOfflineTts(String text) {
        if (isOfflineTtsPlaying) {
            offlineTts.stop();
        }

        // Pass raw languageCode; OfflineGhanaLPTtsService will handle normalization internally
        offlineTts.speak(text, languageCode, new OfflineGhanaLPTtsService.PlaybackCallback() {
            @Override
            public void onStart() {
                isOfflineTtsPlaying = true;
            }

            @Override
            public void onComplete() {
                isOfflineTtsPlaying = false;
            }

            @Override
            public void onError(String error) {
                isOfflineTtsPlaying = false;
                Log.w("SpeedGameActivity", "Offline TTS error: " + error + ", falling back to Android TTS");
                if (tts != null && isTtsReady) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "speed_game");
                }
            }
        });
    }

    // -------------------------
    // GAME FLOW
    // -------------------------

    // NEW: Normalize quiz type
    private String normalizeQuizType(String raw) {
        String t = raw.toLowerCase(Locale.ROOT);
        if (t.contains("letter")) return "letters";
        if (t.contains("sequ")) return "sequence";
        if (t.contains("match")) return "matching";
        if (t.contains("mix")) return "mixed";
        if (t.contains("num")) return "numbers";
        return t;
    }

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
        // NEW: Support different quiz modes
        switch (quizType) {
            case "numbers":
                generateNumberQuestion();
                break;
            case "sequence":
                generateSequenceQuestion();
                break;
            case "matching":
                generateMatchingQuestion();
                break;
            case "mixed":
                generateMixedQuestion();
                break;
            default:
                generateLetterQuestion();
        }
    }

    // NEW: Generate letter question
    private void generateLetterQuestion() {
        Random rnd = new Random();
        currentCorrectAnswer = alphabet[rnd.nextInt(alphabet.length)];

        // Build 6 unique options
        List<String> options = new ArrayList<>();
        options.add(currentCorrectAnswer);
        Set<String> used = new HashSet<>();
        used.add(currentCorrectAnswer);

        while (options.size() < 6) {
            String candidate = alphabet[rnd.nextInt(alphabet.length)];
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

        tvGamePrompt.setText(R.string.quiz_prompt_letter);
        tvGameFeedback.setText("");

        speakWithTts(currentCorrectAnswer);
    }

    // NEW: Generate number question
    private void generateNumberQuestion() {
        Random rnd = new Random();
        int correctNumber = rnd.nextInt(MAX_NUMBER) + 1;
        currentCorrectAnswer = String.valueOf(correctNumber);

        List<String> options = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        options.add(currentCorrectAnswer);
        used.add(correctNumber);

        while (options.size() < 6) {
            int pick = rnd.nextInt(MAX_NUMBER) + 1;
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

        String numberWord = LanguageConversionUtils.convertNumberToWord(correctNumber, languageCode);
        if (!numberWord.isEmpty()) {
            tvGamePrompt.setText(R.string.quiz_prompt_number + "\n(" + numberWord + ")");
        } else {
            tvGamePrompt.setText(R.string.quiz_prompt_number);
        }
        tvGameFeedback.setText("");

        speakWithTts(currentCorrectAnswer);
    }

    // NEW: Generate sequence question
    private void generateSequenceQuestion() {
        Random rnd = new Random();
        int start = rnd.nextInt(20) + 1;
        int[] seq = new int[4];
        for (int i = 0; i < 4; i++) {
            seq[i] = start + i;
        }
        int missingIndex = rnd.nextInt(2) + 1;
        int missingValue = seq[missingIndex];
        currentCorrectAnswer = String.valueOf(missingValue);

        // Display sequence prompt without localization
        tvGamePrompt.setText("Find the missing number: " + seq[0] + ", " + seq[1] + ", ?, " + seq[3]);
        tvGameFeedback.setText("");

        List<String> options = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        options.add(currentCorrectAnswer);
        used.add(missingValue);

        while (options.size() < 6) {
            int delta = rnd.nextInt(3) + 1;
            int candidate = rnd.nextBoolean() ? missingValue + delta : missingValue - delta;
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

        speakWithTts("Find the missing number");
    }

    // NEW: Generate matching question
    private void generateMatchingQuestion() {
        Random rnd = new Random();

        // Use fallback matching with hardcoded words
        int idx = rnd.nextInt(matchLetters.length);
        String letter = matchLetters[idx];
        String correctWord = matchWords[idx];
        currentCorrectAnswer = correctWord;

        // Use localized string resource for matching prompt
        tvGamePrompt.setText(getString(R.string.quiz_prompt_matching, letter));
        tvGameFeedback.setText("");

        List<String> options = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        options.add(correctWord);
        used.add(idx);

        while (options.size() < 6 && used.size() < matchWords.length) {
            int pick = rnd.nextInt(matchWords.length);
            if (!used.contains(pick)) {
                options.add(matchWords[pick]);
                used.add(pick);
            }
        }

        while (options.size() < 6) {
            options.add(matchWords[rnd.nextInt(matchWords.length)]);
        }

        Collections.shuffle(options);
        btnOption1.setText(options.get(0));
        btnOption2.setText(options.get(1));
        btnOption3.setText(options.get(2));
        btnOption4.setText(options.get(3));
        btnOption5.setText(options.get(4));
        btnOption6.setText(options.get(5));

        speakWithTts(letter);
    }

    // NEW: Generate mixed question
    private void generateMixedQuestion() {
        Random rnd = new Random();
        if (rnd.nextBoolean()) {
            generateLetterQuestion();
        } else {
            generateNumberQuestion();
        }
    }

    // Helper method to speak with appropriate TTS
    private void speakWithTts(String text) {
        if (LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
            speakWithOfflineTts(text);
        } else {
            if (!isTtsReady) return;
            tts.stop();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "speed_game_tts");
        }
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
        // NEW: Clean up offline TTS
        if (offlineTts != null) {
            offlineTts.stop();
        }
        if (sfxPlayer != null) {
            sfxPlayer.release();
            sfxPlayer = null;
        }
    }
}
