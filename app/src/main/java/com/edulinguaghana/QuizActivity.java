package com.edulinguaghana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

// removed Lottie dependency for end screen; using a lightweight TextView emoji instead
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.edulinguaghana.tts.OfflineGhanaLPTtsService;
import com.edulinguaghana.audio.AudioCacheManager;
import com.edulinguaghana.utils.LanguageConversionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class QuizActivity extends AppCompatActivity {
    public static final String PREF_NAME = "EduLinguaPrefs";
    public static final String KEY_HIGH_SCORE_PREFIX = "high_score_";
    public static final String KEY_SFX_ENABLED = "sfx_enabled";
    public static final String KEY_QUIZ_MUSIC_VOLUME = "QUIZ_MUSIC_VOLUME";

    private static final long PENALTY_TIME = 5000L;
    private static final int MAX_NUMBER = 50;

    private final Random random = new Random();

    private MaterialToolbar toolbar;
    private AppBarLayout appBarLayout;
    private View startQuizContainer, quizContentContainer, endQuizContainer;

    // Start screen
    private TextView tvStartTitle, tvStartDescription;
    private ImageView ivWelcomeIcon;
    private MaterialButton btnStartQuiz;

    // Game screen
    private TextView tvGameTimer, tvGameScore, tvGameBest, tvGameFeedback, tvGamePrompt;
    private FloatingActionButton btnPlayAudio;
    private MaterialButton btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6;

    // End screen
    private TextView tvFinalScore, tvEndBestScore, tvNewHighScore;
    private MaterialButton btnPlayAgain, btnEndQuit;
    private TextView tvEndCelebrationEmoji; // lightweight replacement for Lottie

    private String quizType, languageCode, languageName;
    private String difficulty = "beginner";  // Default difficulty level
    private String category = "all";  // Default category
    private int score = 0;
    private int bestScore = 0;
    private CountDownTimer countDownTimer;
    private long remainingTime;

    // Challenge mode
    private boolean isChallengeMode = false;
    private String challengeId;

    private TextToSpeech tts;
    private boolean ttsReady = false;
    private String currentCorrectAnswer;
    private String currentPromptTtsText;
    private boolean isSfxOn = true;

    // Offline TTS for native Ghanaian languages (loads pre-recorded audio from res/raw)
    private OfflineGhanaLPTtsService offlineTts;
    private boolean isOfflineTtsPlaying = false;

    // Phase 3: TTS Audio Caching
    private AudioCacheManager audioCacheManager;

    // Background music
    private MediaPlayer backgroundMusicPlayer;
    private static final String KEY_BACKGROUND_MUSIC_ENABLED = "background_music_enabled";
    // Audio focus management
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;

    private String[] alphabet; // Will be set based on language
    private final String[] matchLetters = {"A", "B", "C"};
    private final String[] matchWords = {"Apple", "Ball", "Cat"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        languageName = getIntent().getStringExtra("LANG_NAME");
        languageCode = getIntent().getStringExtra("LANG_CODE");
        String rawType = getIntent().getStringExtra("QUIZ_TYPE");
        difficulty = getIntent().getStringExtra("DIFFICULTY");
        category = getIntent().getStringExtra("CATEGORY");

        // Check if this is challenge mode
        isChallengeMode = getIntent().getBooleanExtra("CHALLENGE_MODE", false);
        challengeId = getIntent().getStringExtra("CHALLENGE_ID");

        if (languageCode == null) languageCode = "en";
        if (languageName == null) languageName = "Unknown";
        if (rawType == null) rawType = "letters";
        if (difficulty == null) difficulty = "beginner";
        if (category == null) category = "all";

        quizType = normalizeQuizType(rawType);

        // --- Bind views ---
        bindViews();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setupStartScreen();

        // Load prefs (high score & SFX setting)
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(getHighScoreKey(), 0);
        isSfxOn = prefs.getBoolean(KEY_SFX_ENABLED, true);
        // Cap best score display at 10 (quiz can have scores > 10 in time-limited mode)
        int displayBestScore = Math.min(bestScore, 10);
        tvGameBest.setText(String.format(Locale.getDefault(), getString(R.string.quiz_best_score), displayBestScore));

        // Initialize language-specific alphabet
        alphabet = LanguageConversionUtils.getAlphabetForLanguage(languageCode);

        // Phase 3: Initialize Audio Cache Manager for TTS
        audioCacheManager = new AudioCacheManager(this);


        initTTS();

        // Setup accessibility features
        setupAccessibility();

        // Initialize background music
        initializeBackgroundMusic();

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
            if (tvEndCelebrationEmoji != null) {
                tvEndCelebrationEmoji.animate().cancel();
                tvEndCelebrationEmoji.setVisibility(View.GONE);
                tvEndCelebrationEmoji.setRotation(0f);
            }
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
        ivWelcomeIcon = findViewById(R.id.ivWelcomeIcon);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);

        tvGameTimer = findViewById(R.id.tvTimer);
        tvGameScore = findViewById(R.id.tvScore);
        tvGameBest = findViewById(R.id.tvBest);
        tvGameFeedback = findViewById(R.id.tvFeedback);
        tvGamePrompt = findViewById(R.id.tvGamePrompt);

        // Accessibility: Make feedback announcements readable by screen readers
        tvGameFeedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        tvGamePrompt.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);

        btnPlayAudio = findViewById(R.id.btnPlayAudio);

        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnOption5 = findViewById(R.id.btnOption5);
        btnOption6 = findViewById(R.id.btnOption6);

        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvEndBestScore = findViewById(R.id.tvEndBestScore);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);
        btnEndQuit = findViewById(R.id.btnEndQuit);
        tvNewHighScore = findViewById(R.id.tvNewHighScore);
        tvEndCelebrationEmoji = findViewById(R.id.tvEndCelebrationEmoji);
    }

    private void setupAccessibility() {
        // Set content descriptions for answer buttons
        btnOption1.setContentDescription(getString(R.string.accessibility_option, "1"));
        btnOption2.setContentDescription(getString(R.string.accessibility_option, "2"));
        btnOption3.setContentDescription(getString(R.string.accessibility_option, "3"));
        btnOption4.setContentDescription(getString(R.string.accessibility_option, "4"));
        btnOption5.setContentDescription(getString(R.string.accessibility_option, "5"));
        btnOption6.setContentDescription(getString(R.string.accessibility_option, "6"));

        // Set content description for play audio button
        if (btnPlayAudio != null) {
            btnPlayAudio.setContentDescription(getString(R.string.accessibility_play_audio));
        }

        // Make game prompt region live for accessibility
        tvGamePrompt.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        tvGameFeedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);

        // Set content descriptions for navigation buttons
        if (btnStartQuiz != null) {
            btnStartQuiz.setContentDescription("Start Quiz");
        }
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

        // Add difficulty and category information to description
        String fullDescription = description;
        if (!difficulty.equalsIgnoreCase("beginner") || !category.equalsIgnoreCase("all")) {
            fullDescription += "\n\nDifficulty: " + capitalizeWord(difficulty);
            if (!category.equalsIgnoreCase("all")) {
                fullDescription += " | Category: " + capitalizeWord(category);
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(modeLabel + " – " + languageName);
        }
        if (tvStartTitle != null) {
            tvStartTitle.setText(modeLabel);
        }
        if (tvStartDescription != null) {
            tvStartDescription.setText(fullDescription);
        }
        if (ivWelcomeIcon != null) {
            ivWelcomeIcon.setImageResource(iconRes);
        }

        if (btnStartQuiz != null) {
            Animation startButtonAnim = AnimationUtils.loadAnimation(this, R.anim.bounce_pop);
            btnStartQuiz.startAnimation(startButtonAnim);
        }
    }

    /**
     * Capitalize first letter of a word
     */
    private String capitalizeWord(String word) {
        if (word == null || word.length() == 0) return "";
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    private void showQuizContent() {
        try {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            if (startQuizContainer != null) {
                startQuizContainer.setVisibility(View.GONE);
            }
            if (quizContentContainer != null) {
                quizContentContainer.setVisibility(View.VISIBLE);
                quizContentContainer.startAnimation(fadeIn);
            }
            if (appBarLayout != null) {
                appBarLayout.setVisibility(View.VISIBLE);
                appBarLayout.startAnimation(fadeIn);
            }
            startBackgroundMusic();
            startGame();
        } catch (Exception e) {
            Log.e("QuizActivity", "Error showing quiz content", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        isSfxOn = prefs.getBoolean(KEY_SFX_ENABLED, true);
        startBackgroundMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseBackgroundMusic();
    }

    private String normalizeQuizType(String raw) {
        String t = raw.toLowerCase(Locale.ROOT);
        if (t.contains("letter")) return "letters";
        if (t.contains("sequ")) return "sequence";
        if (t.contains("match")) return "matching";
        if (t.contains("mix")) return "mixed";
        if (t.contains("num")) return "numbers";
        return t;
    }

    private void initTTS() {
        // Initialize Offline TTS for Ghanaian languages (loads from res/raw)
        if (LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
            offlineTts = new OfflineGhanaLPTtsService(this);
        }

        // Initialize Android TTS for all languages
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale locale = LanguageConversionUtils.getLocaleForLanguage(languageCode);
                tts.setLanguage(locale);
                ttsReady = true;
                if (currentPromptTtsText != null) {
                    speakPrompt();
                }
            }
        });
    }

    private void speakPrompt() {
        if (currentPromptTtsText == null || currentPromptTtsText.trim().isEmpty()) {
            Log.w("QuizActivity", "Cannot speak: currentPromptTtsText is null or empty");
            return;
        }

        // Use GhanaLP TTS for Ghanaian languages (Twi, Ewe, Ga)
        if (isGhanaianLanguage(languageCode)) {
            speakWithGhanaLP(currentPromptTtsText);
        } else {
            // Use Android TTS with caching for English/French
            if (!ttsReady) {
                Log.w("QuizActivity", "TTS not ready yet, cannot speak: " + currentPromptTtsText);
                return;
            }

            // Phase 3: Check cache first
            android.net.Uri cachedAudio = null;
            if (audioCacheManager != null) {
                cachedAudio = audioCacheManager.getCachedTtsAudio(currentPromptTtsText, languageCode);
            }
            if (cachedAudio != null) {
                Log.d("QuizActivity", "Playing cached audio: " + currentPromptTtsText);
                playAudioFile(cachedAudio);
                return;
            }

            Log.d("QuizActivity", "Speaking text (not cached): '" + currentPromptTtsText + "' in language: " + languageCode);
            if (tts != null) {
                tts.stop();
                tts.setSpeechRate(0.9f); // Slightly slower for clarity

                int result = tts.speak(currentPromptTtsText, TextToSpeech.QUEUE_FLUSH, null, "quiz_tts");

                if (result != TextToSpeech.SUCCESS) {
                    Log.e("QuizActivity", "TTS.speak() failed with result: " + result + " for text: " + currentPromptTtsText);
                }
            }
        }
    }

    private void playAudioFile(android.net.Uri audioUri) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, audioUri);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> mp.start());
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        } catch (Exception e) {
            Log.e("QuizActivity", "Error playing audio file", e);
        }
    }

    private boolean isGhanaianLanguage(String code) {
        return LanguageConversionUtils.isGhanaianLanguage(code);
    }

    private void speakWithGhanaLP(String text) {
        if (isOfflineTtsPlaying) {
            offlineTts.stop();
        }

        // Play pre-recorded audio from res/raw
        // OfflineGhanaLPTtsService will handle language code normalization internally
        offlineTts.speak(
            text,
            languageCode,
            new OfflineGhanaLPTtsService.PlaybackCallback() {
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
                    runOnUiThread(() -> {
                        // Fallback to Android TTS on error
                        Log.w("QuizActivity", "Offline TTS error: " + error + ", falling back to Android TTS");
                        if (tts != null && ttsReady) {
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "quiz_tts");
                        }
                    });
                }
            }
        );
    }

    private String normalizeLanguageCode(String code) {
        return LanguageConversionUtils.normalizeLanguageCode(code);
    }

    private void startGame() {
        score = 0;
        remainingTime = 30000L;
        if (tvGameScore != null) {
            tvGameScore.setText(String.format(Locale.getDefault(), getString(R.string.quiz_score), score));
        }
        if (tvGameFeedback != null) {
            tvGameFeedback.setText("");
        }
        generateNewQuestion();
        startTimer();
    }

    private void generateNewQuestion() {
        resetButtons();
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
                break;
        }
        speakPrompt();
    }

    private void generateLetterQuestion() {
        if (alphabet == null || alphabet.length == 0) {
            Log.e("QuizActivity", "Alphabet is null or empty for language: " + languageCode);
            // Fallback to English alphabet
            alphabet = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        }

        currentCorrectAnswer = alphabet[random.nextInt(alphabet.length)];
        android.util.Log.d("QuizActivity", "Letter quiz for " + languageCode + ": " + currentCorrectAnswer);

        // Use the letter directly as the TTS text (for audio prompts)
        currentPromptTtsText = currentCorrectAnswer;

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
        if (btnOption1 != null) btnOption1.setText(options.get(0));
        if (btnOption2 != null) btnOption2.setText(options.get(1));
        if (btnOption3 != null) btnOption3.setText(options.get(2));
        if (btnOption4 != null) btnOption4.setText(options.get(3));
        if (btnOption5 != null) btnOption5.setText(options.get(4));
        if (btnOption6 != null) btnOption6.setText(options.get(5));

        // Add accessibility descriptions for each button with the letter
        if (btnOption1 != null) btnOption1.setContentDescription("Letter " + options.get(0) + ", Option 1");
        if (btnOption2 != null) btnOption2.setContentDescription("Letter " + options.get(1) + ", Option 2");
        if (btnOption3 != null) btnOption3.setContentDescription("Letter " + options.get(2) + ", Option 3");
        if (btnOption4 != null) btnOption4.setContentDescription("Letter " + options.get(3) + ", Option 4");
        if (btnOption5 != null) btnOption5.setContentDescription("Letter " + options.get(4) + ", Option 5");
        if (btnOption6 != null) btnOption6.setContentDescription("Letter " + options.get(5) + ", Option 6");

        if (tvGamePrompt != null) {
            tvGamePrompt.setText(R.string.quiz_prompt_letter);
        }
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
        if (btnOption1 != null) btnOption1.setText(options.get(0));
        if (btnOption2 != null) btnOption2.setText(options.get(1));
        if (btnOption3 != null) btnOption3.setText(options.get(2));
        if (btnOption4 != null) btnOption4.setText(options.get(3));
        if (btnOption5 != null) btnOption5.setText(options.get(4));
        if (btnOption6 != null) btnOption6.setText(options.get(5));

        // Add accessibility descriptions for each button with the number
        if (btnOption1 != null) btnOption1.setContentDescription("Number " + options.get(0) + ", Option 1");
        if (btnOption2 != null) btnOption2.setContentDescription("Number " + options.get(1) + ", Option 2");
        if (btnOption3 != null) btnOption3.setContentDescription("Number " + options.get(2) + ", Option 3");
        if (btnOption4 != null) btnOption4.setContentDescription("Number " + options.get(3) + ", Option 4");
        if (btnOption5 != null) btnOption5.setContentDescription("Number " + options.get(4) + ", Option 5");
        if (btnOption6 != null) btnOption6.setContentDescription("Number " + options.get(5) + ", Option 6");

        // Show the number word spelling if available
        String numberWord = LanguageConversionUtils.convertNumberToWord(correctNumber, languageCode);
        if (tvGamePrompt != null) {
            if (!numberWord.isEmpty()) {
                tvGamePrompt.setText(getString(R.string.quiz_prompt_number) + "\n(" + numberWord + ")");
            } else {
                tvGamePrompt.setText(R.string.quiz_prompt_number);
            }
        }

        // Use word form for TTS for non-Ghanaian languages (like French)
        // This ensures French/English TTS can pronounce the number word instead of the digit
        if (!isGhanaianLanguage(languageCode)) {
            currentPromptTtsText = numberWord.isEmpty() ? currentCorrectAnswer : numberWord;
        } else {
            // For Ghanaian languages, use the numeric string to lookup audio files
            currentPromptTtsText = currentCorrectAnswer;
        }
    }

    private void generateSequenceQuestion() {
        int start = random.nextInt(20) + 1;
        int step = 1;
        int[] seq = new int[4];
        for (int i = 0; i < 4; i++) {
            seq[i] = start + i * step;
        }
        // Always use index 2 as the missing position (shown as ?)
        int missingIndex = 2;
        int missingValue = seq[missingIndex];
        currentCorrectAnswer = String.valueOf(missingValue);

        // Display sequence prompt without localization
        if (tvGamePrompt != null) {
            tvGamePrompt.setText("Find the missing number: " + seq[0] + ", " + seq[1] + ", ?, " + seq[3]);
        }

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
        if (btnOption1 != null) btnOption1.setText(options.get(0));
        if (btnOption2 != null) btnOption2.setText(options.get(1));
        if (btnOption3 != null) btnOption3.setText(options.get(2));
        if (btnOption4 != null) btnOption4.setText(options.get(3));
        if (btnOption5 != null) btnOption5.setText(options.get(4));
        if (btnOption6 != null) btnOption6.setText(options.get(5));
        currentPromptTtsText = "Find the missing number";
    }

    private void generateMatchingQuestion() {
        // Use fallback matching with hardcoded words
        int idx = random.nextInt(matchLetters.length);
        String letter = matchLetters[idx];
        String correctWord = matchWords[idx];
        currentCorrectAnswer = correctWord;
        if (tvGamePrompt != null) {
            tvGamePrompt.setText(getString(R.string.quiz_prompt_matching, letter));
        }

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
        if (btnOption1 != null) btnOption1.setText(options.get(0));
        if (btnOption2 != null) btnOption2.setText(options.get(1));
        if (btnOption3 != null) btnOption3.setText(options.get(2));
        if (btnOption4 != null) btnOption4.setText(options.get(3));
        if (btnOption5 != null) btnOption5.setText(options.get(4));
        if (btnOption6 != null) btnOption6.setText(options.get(5));
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
            tvGameFeedback.setText(R.string.quiz_feedback_correct);
            tvGameFeedback.setTextColor(ContextCompat.getColor(this, R.color.correctAnswer));
            tvGameFeedback.announceForAccessibility(getString(R.string.accessibility_correct_answer, score));
            clickedButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.correctAnswer)));
            clickedButton.startAnimation(correctAnim);
            playSfx(true);
            if (score > bestScore) {
                bestScore = score;
                saveHighScore();
                animateHighScore();
            }
        } else {
            tvGameFeedback.setText(getString(R.string.quiz_feedback_wrong, currentCorrectAnswer));
            tvGameFeedback.setTextColor(ContextCompat.getColor(this, R.color.wrongAnswer));
            tvGameFeedback.announceForAccessibility(getString(R.string.accessibility_wrong_answer, currentCorrectAnswer));
            clickedButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.wrongAnswer)));
            clickedButton.startAnimation(wrongAnim);
            playSfx(false);
            remainingTime -= PENALTY_TIME;
            if (remainingTime < 0) remainingTime = 0;
        }


        tvGameScore.setText(String.format(Locale.getDefault(), getString(R.string.quiz_score), score));
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
                if (tvGameTimer != null) {
                    tvGameTimer.setText(String.format(Locale.getDefault(), getString(R.string.quiz_timer), s));
                }
            }

            @Override
            public void onFinish() {
                remainingTime = 0;
                if (tvGameTimer != null) {
                    tvGameTimer.setText(R.string.quiz_timer_done);
                }
                endQuiz();
            }
        }.start();
    }

    private void endQuiz() {
        cancelTimer();
        setButtonsEnabled(false);

        boolean newHighScore = score > 0 && score >= bestScore;

        // Update overall progress with language tracking
        ProgressManager.updateProgressWithLanguage(this, quizType, score, score, languageCode);

        // Record practice for streak
        StreakManager streakManager = new StreakManager(this);
        streakManager.recordPractice();

        // Check and unlock achievements
        AchievementManager achievementManager = new AchievementManager(this);
        achievementManager.checkAndUnlockAchievements();

        // Upload to leaderboard if score is good and user is online
        if (score >= 10) {
            CloudSyncManager cloudSync = new CloudSyncManager(this);
            if (cloudSync.canSync()) {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String userName = prefs.getString("USER_NAME", "Anonymous");
                cloudSync.uploadToLeaderboard(userName, score, (success, message) -> {
                    // Silent upload, no need to notify user
                });
            }
        }

        // Trigger notifications for achievements
        NotificationManager notificationManager = new NotificationManager(this);

        if (newHighScore && score >= 80) {
            notificationManager.sendAchievementNotification(
                "New High Score! 🏆",
                "Amazing! You scored " + score + " points in " + quizType + " mode!"
            );
        }

        // Save challenge result if in challenge mode
        if (isChallengeMode && challengeId != null) {
            saveChallengeResult();
            
            // Progress challenge quest when challenge is attempted
            try {
                com.edulinguaghana.gamification.QuestManager.progressQuest(this, "daily_challenge", 1); // Quest 3
            } catch (Exception ignored) { }
        }

        if (score == 100) {
            notificationManager.sendAchievementNotification(
                "Perfect Score! ⭐",
                "Outstanding! You got a perfect score in " + quizType + " mode!"
            );
        }

        // --- Social: post leaderboard and resolve challenges ---
        try {
            com.edulinguaghana.social.SocialRepository social = com.edulinguaghana.social.SocialProvider.get();
            if (social != null) {
                // Post to Firebase leaderboard (Realtime DB path "leaderboard")
                String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "anonymous";

                // Fetch user's displayName from /users node before saving to leaderboard
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                            String displayName = uid; // fallback to uid
                            if (snapshot.exists() && snapshot.child("displayName").getValue() != null) {
                                displayName = snapshot.child("displayName").getValue(String.class);
                            }

                            // Create and save leaderboard entry with actual displayName
                            com.edulinguaghana.LeaderboardEntry entry = new com.edulinguaghana.LeaderboardEntry();
                            entry.setUserId(uid);
                            entry.setUserName(displayName);
                            entry.setScore(score);
                            entry.setTimestamp(System.currentTimeMillis());

                            com.google.firebase.database.FirebaseDatabase.getInstance()
                                .getReference("leaderboard")
                                .child(entry.getUserId())
                                .setValue(entry);
                        }

                        @Override
                        public void onCancelled(com.google.firebase.database.DatabaseError error) {
                            // Fallback: save with uid as username if fetch fails
                            com.edulinguaghana.LeaderboardEntry entry = new com.edulinguaghana.LeaderboardEntry();
                            entry.setUserId(uid);
                            entry.setUserName(uid);
                            entry.setScore(score);
                            entry.setTimestamp(System.currentTimeMillis());

                            com.google.firebase.database.FirebaseDatabase.getInstance()
                                .getReference("leaderboard")
                                .child(entry.getUserId())
                                .setValue(entry);
                        }
                    });

                // Resolve pending challenges for this user: fetch challenges where challengedId==uid
                // FirebaseSocialRepository does not provide a synchronous get, so we update via DB query
                com.google.firebase.database.DatabaseReference challengesRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("challenges");
                challengesRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            com.edulinguaghana.social.Challenge c = child.getValue(com.edulinguaghana.social.Challenge.class);
                            if (c != null && c.challengedId != null && c.challengedId.equals(uid) && (c.state == com.edulinguaghana.social.Challenge.State.PENDING || c.state == com.edulinguaghana.social.Challenge.State.ONGOING)) {
                                // record result
                                if (c.results == null) c.results = new java.util.HashMap<>();
                                c.results.put(uid, score);
                                // Mark completed if challenger also has a result (simple two-player model)
                                if (c.results.containsKey(c.challengerId)) {
                                    c.state = com.edulinguaghana.social.Challenge.State.COMPLETED;
                                } else {
                                    c.state = com.edulinguaghana.social.Challenge.State.ONGOING;
                                }
                                child.getRef().setValue(c);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) { }
                });
            }
        } catch (Exception ignored) {}

        // Show end screen
        try {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            if (quizContentContainer != null) {
                quizContentContainer.setVisibility(View.GONE);
            }
            if (appBarLayout != null) {
                appBarLayout.setVisibility(View.GONE);
            }
            if (endQuizContainer != null) {
                endQuizContainer.setVisibility(View.VISIBLE);
                endQuizContainer.startAnimation(fadeIn);
            }

            if (tvFinalScore != null) {
                tvFinalScore.setText(String.format(Locale.getDefault(), getString(R.string.quiz_final_score), score));
            }
            // Cap best score display at 10 (quiz can have scores > 10 in time-limited mode)
            int displayBestScore = Math.min(bestScore, 10);
            if (tvEndBestScore != null) {
                tvEndBestScore.setText(String.format(Locale.getDefault(), getString(R.string.quiz_best_score), displayBestScore));
            }

            if (newHighScore) {
                if (tvNewHighScore != null) {
                    tvNewHighScore.setVisibility(View.VISIBLE);
                    Animation bouncePop = AnimationUtils.loadAnimation(this, R.anim.bounce_pop);
                    tvNewHighScore.startAnimation(bouncePop);
                }
                if (tvEndCelebrationEmoji != null) {
                    tvEndCelebrationEmoji.setVisibility(View.VISIBLE);
                    tvEndCelebrationEmoji.setScaleX(0.8f);
                    tvEndCelebrationEmoji.setScaleY(0.8f);
                    tvEndCelebrationEmoji.animate()
                        .scaleX(1.3f)
                        .scaleY(1.3f)
                        .rotationBy(360f)
                        .setDuration(700)
                        .withEndAction(() -> {
                            // Reset rotation to keep view stable
                            tvEndCelebrationEmoji.setRotation(0f);
                        })
                        .start();
                }
            } else {
                if (tvNewHighScore != null) {
                    tvNewHighScore.setVisibility(View.GONE);
                }
                if (tvEndCelebrationEmoji != null) {
                    tvEndCelebrationEmoji.animate().cancel();
                    tvEndCelebrationEmoji.setVisibility(View.GONE);
                    tvEndCelebrationEmoji.setRotation(0f);
                }
            }
        } catch (Exception e) {
            Log.e("QuizActivity", "Error showing end screen", e);
        }
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void resetButtons() {
        if (tvGameFeedback != null) {
            tvGameFeedback.setText("");
        }
        setButtonsEnabled(true);
        MaterialButton[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6};
        for (MaterialButton button : buttons) {
            if (button != null) {
                button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.buttonSecondary)));
                button.clearAnimation();
            }
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        if (btnOption1 != null) btnOption1.setEnabled(enabled);
        if (btnOption2 != null) btnOption2.setEnabled(enabled);
        if (btnOption3 != null) btnOption3.setEnabled(enabled);
        if (btnOption4 != null) btnOption4.setEnabled(enabled);
        if (btnOption5 != null) btnOption5.setEnabled(enabled);
        if (btnOption6 != null) btnOption6.setEnabled(enabled);
    }

    private String getHighScoreKey() {
        return KEY_HIGH_SCORE_PREFIX + quizType;
    }

    private void saveHighScore() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putInt(getHighScoreKey(), bestScore).apply();
        if (tvGameBest != null) {
            tvGameBest.setText(String.format(Locale.getDefault(), getString(R.string.quiz_best_score), bestScore));
        }
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

    private void saveChallengeResult() {
        com.edulinguaghana.social.SocialRepository repo = com.edulinguaghana.social.SocialProvider.get();
        if (repo == null) return;

        try {
            // Get current user ID
            com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) return;

            String userId = currentUser.getUid();

            // Get the challenge from Firebase
            com.google.firebase.database.DatabaseReference challengeRef =
                com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("challenges").child(challengeId);

            challengeRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                    com.edulinguaghana.social.Challenge challenge = snapshot.getValue(com.edulinguaghana.social.Challenge.class);
                    if (challenge != null) {
                        // Save the score
                        if (challenge.results == null) {
                            challenge.results = new java.util.HashMap<>();
                        }
                        challenge.results.put(userId, score);

                        // Check if both players have completed
                        if (challenge.results.size() >= 2) {
                            challenge.state = com.edulinguaghana.social.Challenge.State.COMPLETED;
                        }

                        // Update challenge
                        challengeRef.setValue(challenge);

                        // Show result
                        runOnUiThread(() -> {
                            Toast.makeText(QuizActivity.this,
                                "Challenge score saved: " + score + " points! ⚔️",
                                Toast.LENGTH_LONG).show();
                        });
                    }
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    runOnUiThread(() -> {
                        Toast.makeText(QuizActivity.this,
                            "Failed to save challenge result",
                            Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (Exception ex) {
            Toast.makeText(this, "Error saving challenge result", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initialize and prepare background music for quiz
     */
    private void initializeBackgroundMusic() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            boolean backgroundMusicEnabled = prefs.getBoolean(KEY_BACKGROUND_MUSIC_ENABLED, true);
            // The settings screen saves QUIZ_MUSIC_VOLUME as an int percentage (0-100).
            // Read int if present and convert to 0.0-1.0 range; fall back to float for older installs.
            float _prefTmp;
            if (prefs.contains(KEY_QUIZ_MUSIC_VOLUME)) {
                try {
                    int percent = prefs.getInt(KEY_QUIZ_MUSIC_VOLUME, -1);
                    if (percent >= 0) {
                        _prefTmp = Math.max(0f, Math.min(1f, percent / 100f));
                    } else {
                        _prefTmp = prefs.getFloat(KEY_QUIZ_MUSIC_VOLUME, 0.3f);
                    }
                } catch (ClassCastException e) {
                    _prefTmp = prefs.getFloat(KEY_QUIZ_MUSIC_VOLUME, 0.3f);
                }
            } else {
                _prefTmp = 0.3f;
            }
            final float prefVolume = _prefTmp;

            // Prepare AudioManager and a simple focus change listener
            audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            afChangeListener = focusChange -> {
                try {
                    if (backgroundMusicPlayer == null) return;
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            if (!backgroundMusicPlayer.isPlaying()) backgroundMusicPlayer.start();
                            backgroundMusicPlayer.setVolume(prefVolume, prefVolume);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            if (backgroundMusicPlayer.isPlaying()) backgroundMusicPlayer.pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            if (backgroundMusicPlayer.isPlaying()) backgroundMusicPlayer.pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            backgroundMusicPlayer.setVolume(prefVolume * 0.2f, prefVolume * 0.2f);
                            break;
                    }
                } catch (Exception e) {
                    Log.w("QuizActivity", "Audio focus change handling failed", e);
                }
            };

            if (backgroundMusicEnabled && prefVolume > 0f) {
                backgroundMusicPlayer = MediaPlayer.create(this, R.raw.quiz_music);
                if (backgroundMusicPlayer != null) {
                    // Ensure playback uses the music stream and is looped
                    backgroundMusicPlayer.setLooping(true);
                    // Respect saved preference volume (0.0 - 1.0)
                    backgroundMusicPlayer.setVolume(prefVolume, prefVolume);
                }
            }
        } catch (Exception e) {
            Log.e("QuizActivity", "Error initializing background music: " + e.getMessage());
        }
    }

    /**
     * Start playing background music during quiz
     */
    private void startBackgroundMusic() {
        try {
            if (backgroundMusicPlayer == null) return;

            // Respect system media volume: if the device media stream is muted (0) do not start music
            if (audioManager == null) audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                int vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (vol == 0) {
                    Log.d("QuizActivity", "Not starting background music because STREAM_MUSIC volume is 0");
                    return;
                }
            }

            // Check preference volume and request audio focus before starting
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            boolean backgroundMusicEnabled = prefs.getBoolean(KEY_BACKGROUND_MUSIC_ENABLED, true);
            // Read preference volume (stored as int percent 0-100 in SettingsActivity) and convert to 0.0-1.0
            float prefVolume;
            if (prefs.contains(KEY_QUIZ_MUSIC_VOLUME)) {
                try {
                    int percent = prefs.getInt(KEY_QUIZ_MUSIC_VOLUME, -1);
                    if (percent >= 0) {
                        prefVolume = Math.max(0f, Math.min(1f, percent / 100f));
                    } else {
                        prefVolume = prefs.getFloat(KEY_QUIZ_MUSIC_VOLUME, 0.3f);
                    }
                } catch (ClassCastException e) {
                    prefVolume = prefs.getFloat(KEY_QUIZ_MUSIC_VOLUME, 0.3f);
                }
            } else {
                prefVolume = 0.3f;
            }
            if (!backgroundMusicEnabled || prefVolume <= 0f) {
                Log.d("QuizActivity", "Background music disabled or volume set to 0 in preferences");
                return;
            }

            // Request audio focus
            int focusResult = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
            try {
                if (audioManager != null) {
                    focusResult = audioManager.requestAudioFocus(afChangeListener,
                            AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN);
                }
            } catch (Exception e) {
                Log.w("QuizActivity", "Audio focus request failed", e);
            }

            if (focusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                try {
                    backgroundMusicPlayer.setVolume(prefVolume, prefVolume);
                    if (!backgroundMusicPlayer.isPlaying()) backgroundMusicPlayer.start();
                } catch (Exception e) {
                    Log.e("QuizActivity", "Error starting background music: " + e.getMessage());
                }
            } else {
                Log.d("QuizActivity", "Audio focus not granted, not starting background music");
            }
        } catch (Exception e) {
            Log.e("QuizActivity", "Error starting background music: " + e.getMessage());
        }
    }

    /**
     * Pause background music
     */
    private void pauseBackgroundMusic() {
        try {
            if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
                backgroundMusicPlayer.pause();
            }
        } catch (Exception e) {
            Log.e("QuizActivity", "Error pausing background music: " + e.getMessage());
        }
    }

    /**
     * Stop and release background music resources
     */
    private void stopBackgroundMusic() {
        try {
            if (backgroundMusicPlayer != null) {
                if (backgroundMusicPlayer.isPlaying()) {
                    backgroundMusicPlayer.stop();
                }
                backgroundMusicPlayer.release();
                backgroundMusicPlayer = null;
            }
            if (audioManager != null && afChangeListener != null) {
                try {
                    audioManager.abandonAudioFocus(afChangeListener);
                } catch (Exception e) {
                    Log.w("QuizActivity", "Failed to abandon audio focus", e);
                }
            }
        } catch (Exception e) {
            Log.e("QuizActivity", "Error stopping background music: " + e.getMessage());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        stopBackgroundMusic();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (offlineTts != null) {
            offlineTts.stop();
        }
    }
}
