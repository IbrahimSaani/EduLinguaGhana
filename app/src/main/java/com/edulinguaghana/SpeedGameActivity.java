package com.edulinguaghana;

import android.content.res.ColorStateList;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.DragEvent;
import android.content.ClipData;
import android.content.ClipDescription;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.edulinguaghana.tts.OfflineGhanaLPTtsService;
import com.edulinguaghana.utils.LanguageConversionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class SpeedGameActivity extends AppCompatActivity {

    // Views
    private TextView tvGameTitle, tvGameTimer, tvGameScore, tvGameBest, tvGameFeedback, tvGamePrompt;
    private FloatingActionButton btnPlayAudio;
    private MaterialButton btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6;
    private MaterialButton btnBack;
    private ProgressBar speedProgressBar;
    private ShadowView shadowView;
    private nl.dionsegijn.konfetti.xml.KonfettiView konfettiView;

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
    private static final long PENALTY_TIME_MS = 1000;  // 1s penalty
    private static final int MAX_NUMBER = 50;  // NEW: For number questions

    // Letters to use in the game - KEEP for compatibility
    private final String[] questions = {
            "A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"
    };

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

        // Pre-warm character arrays
        new Handler(Looper.getMainLooper()).post(() -> {
            if (alphabet != null && alphabet.length > 0) {
                String first = alphabet[0];
            }
        });

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
        tvGameTimer    = findViewById(R.id.tvGameTimer);
        tvGameScore    = findViewById(R.id.tvGameScore);
        tvGameBest     = findViewById(R.id.tvGameBest);
        tvGameFeedback = findViewById(R.id.tvGameFeedback);
        tvGamePrompt   = findViewById(R.id.tvGamePrompt);
        speedProgressBar = findViewById(R.id.speedProgressBar);

        btnOption1 = findViewById(R.id.btnGameOpt1);
        btnOption2 = findViewById(R.id.btnGameOpt2);
        btnOption3 = findViewById(R.id.btnGameOpt3);
        btnOption4 = findViewById(R.id.btnGameOpt4);
        btnOption5 = findViewById(R.id.btnGameOpt5);
        btnOption6 = findViewById(R.id.btnGameOpt6);
        btnBack    = findViewById(R.id.btnGameBack);
        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        shadowView = findViewById(R.id.shadowView);
        konfettiView = findViewById(R.id.konfettiView);

        // OPTIONAL: if you added a Play Audio button in XML, otherwise comment out
        btnPlayAudio = findViewById(R.id.btnPlayAudio);

        // --- SharedPreferences ---
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_HIGH_SCORE, 0);
        isSfxOn   = prefs.getBoolean(KEY_SFX_ENABLED, true);

        tvGameBest.setText(getString(R.string.quiz_best_score, bestScore));
        tvGameScore.setText(getString(R.string.quiz_score, 0));
        tvGameFeedback.setText("");

        // --- Init TTS ---
        initTts();

        // --- Button listeners ---
        btnOption1.setOnClickListener(v -> handleAnswerClick((MaterialButton) v));
        btnOption2.setOnClickListener(v -> handleAnswerClick((MaterialButton) v));
        btnOption3.setOnClickListener(v -> handleAnswerClick((MaterialButton) v));
        btnOption4.setOnClickListener(v -> handleAnswerClick((MaterialButton) v));
        btnOption5.setOnClickListener(v -> handleAnswerClick((MaterialButton) v));
        btnOption6.setOnClickListener(v -> handleAnswerClick((MaterialButton) v));

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
        if (t.contains("shadow")) return "shadow_match";
        if (t.contains("letter")) return "letters";
        if (t.contains("sequ")) return "sequence";
        if (t.contains("match")) return "matching";
        if (t.contains("miss")) return "shadow_match";
        if (t.contains("mix")) return "mixed";
        if (t.contains("num")) return "numbers";
        return t;
    }

    private void startNewRound() {
        score = 0;
        tvGameScore.setText(getString(R.string.quiz_score, score));
        tvGameFeedback.setText("");
        timeLeftMs = TOTAL_TIME_MS;
        tvGameTimer.setText(getString(R.string.quiz_timer, (timeLeftMs / 1000)));
        tvGameTimer.setTextColor(Color.parseColor("#0D47A1")); // Dark blue for visibility

        generateNewQuestion();
        startTimer();
    }

    private void generateNewQuestion() {
        // Reset shadow match feedback styling
        if (tvGameFeedback != null) {
            tvGameFeedback.setText("");
            tvGameFeedback.setAlpha(1.0f);
            tvGameFeedback.setTextSize(16);
            tvGameFeedback.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        }
        if (shadowView != null) shadowView.setVisibility(View.GONE);

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
            case "shadow_match":
                generateShadowMatchQuestion();
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
        // Show localized prompt; append the spoken number word in parentheses when available
        if (!numberWord.isEmpty()) {
            tvGamePrompt.setText(getString(R.string.quiz_prompt_number) + "\n(" + numberWord + ")");
        } else {
            tvGamePrompt.setText(getString(R.string.quiz_prompt_number));
        }
        tvGameFeedback.setText("");

        speakWithTts(currentCorrectAnswer);
    }

    // NEW: Generate sequence question
    private void generateSequenceQuestion() {
        Random rnd = new Random();
        int start = rnd.nextInt(Math.max(1, MAX_NUMBER - 5)) + 1;
        int[] seq = new int[4];
        for (int i = 0; i < 4; i++) {
            seq[i] = start + i;
        }
        int missingIndex = 2; // Fixed missing index for simplicity
        int missingValue = seq[missingIndex];
        currentCorrectAnswer = String.valueOf(missingValue);

        // Build sequence prompt with words for non-English languages
        String s0 = String.valueOf(seq[0]);
        String s1 = String.valueOf(seq[1]);
        String s3 = String.valueOf(seq[3]);

        if (!languageCode.equals("en")) {
            s0 = LanguageConversionUtils.convertNumberToWord(seq[0], languageCode);
            s1 = LanguageConversionUtils.convertNumberToWord(seq[1], languageCode);
            s3 = LanguageConversionUtils.convertNumberToWord(seq[3], languageCode);
        }

        tvGamePrompt.setText(getString(R.string.quiz_prompt_sequence_find) + "\n" + s0 + ", " + s1 + ", ?, " + s3);
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

    private void generateMatchingQuestion() {
        String[] currentAlphabet = LanguageConversionUtils.getAlphabetForLanguage(languageCode);
        String letter = currentAlphabet[new Random().nextInt(currentAlphabet.length)];
        String correctWord = LanguageConversionUtils.getMatchingWordForLetter(letter, languageCode);
        currentCorrectAnswer = correctWord;

        tvGamePrompt.setText(getString(R.string.quiz_prompt_matching, letter));
        tvGameFeedback.setText("");

        List<String> options = new ArrayList<>();
        options.add(correctWord);

        while (options.size() < 6) {
            String randomLetter = currentAlphabet[new Random().nextInt(currentAlphabet.length)];
            String randomWord = LanguageConversionUtils.getMatchingWordForLetter(randomLetter, languageCode);
            if (!options.contains(randomWord)) {
                options.add(randomWord);
            }
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

    private void generateShadowMatchQuestion() {
        if (tvGamePrompt != null) {
            tvGamePrompt.setText(R.string.quiz_prompt_shadow_match);
        }

        String[] currentAlphabet = LanguageConversionUtils.getAlphabetForLanguage(languageCode);
        boolean useNumber = new Random().nextBoolean();
        String target;

        if (useNumber) {
            target = String.valueOf(new Random().nextInt(MAX_NUMBER) + 1);
        } else {
            target = currentAlphabet[new Random().nextInt(currentAlphabet.length)];
        }

        currentCorrectAnswer = target;

        if (shadowView != null) {
            shadowView.setVisibility(View.VISIBLE);
            shadowView.setCharacter(target);

            // Speed game version of drag listener
            shadowView.setOnDragListener((v, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED: return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).start();
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                        return true;
                    case DragEvent.ACTION_DROP:
                        ClipData.Item item = event.getClipData().getItemAt(0);
                        String dragData = item.getText().toString();
                        if (dragData.equals(currentCorrectAnswer)) {
                            MaterialButton[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6};
                            for (MaterialButton btn : buttons) {
                                if (btn != null && btn.getText().equals(dragData)) {
                                    btn.setTag(R.id.shadowView, "drag_success");
                                    shadowView.reveal(ContextCompat.getColor(this, R.color.correctAnswer));
                                    celebrate();
                                    handleAnswerClick(btn);
                                    btn.setTag(R.id.shadowView, null);
                                    break;
                                }
                            }
                        } else {
                            ObjectAnimator shake = ObjectAnimator.ofFloat(v, View.TRANSLATION_X, 0, 20);
                            shake.setDuration(40);
                            shake.setRepeatCount(3);
                            shake.setRepeatMode(ValueAnimator.REVERSE);
                            shake.start();
                            playSfx(false);
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                        return true;
                }
                return false;
            });
        }

        List<String> options = new ArrayList<>();
        options.add(target);

        while (options.size() < 6) {
            String pick = useNumber ? String.valueOf(new Random().nextInt(MAX_NUMBER) + 1) : currentAlphabet[new Random().nextInt(currentAlphabet.length)];
            if (!options.contains(pick)) {
                options.add(pick);
            }
        }

        Collections.shuffle(options);
        MaterialButton[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6};
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null) {
                buttons[i].setText(options.get(i));
                buttons[i].setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));

                final MaterialButton currentBtn = buttons[i];
                currentBtn.setOnLongClickListener(v -> {
                    ClipData.Item item = new ClipData.Item(currentBtn.getText());
                    ClipData dragData = new ClipData(currentBtn.getText(), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(currentBtn);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        v.startDragAndDrop(dragData, myShadow, null, 0);
                    } else {
                        v.startDrag(dragData, myShadow, null, 0);
                    }
                    return true;
                });
            }
        }

        speakWithTts(target);
    }

    // NEW: Generate mixed question
    private void generateMixedQuestion() {
        Random rnd = new Random();
        int r = rnd.nextInt(5);
        if (r == 0) generateLetterQuestion();
        else if (r == 1) generateNumberQuestion();
        else if (r == 2) generateSequenceQuestion();
        else if (r == 3) generateMatchingQuestion();
        else generateShadowMatchQuestion();
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

    private void handleAnswerClick(MaterialButton clickedButton) {
        if ("shadow_match".equals(quizType) && !"drag_success".equals(clickedButton.getTag(R.id.shadowView))) {
            // Speed game version: Only drag and drop allowed
            tvGameFeedback.setText(R.string.quiz_hint_drag_to_match);
            return;
        }
        String chosen = clickedButton.getText().toString();
        boolean correct = chosen.equals(currentCorrectAnswer);

        if (correct) {
            score++;
            tvGameFeedback.setText(getString(R.string.quiz_feedback_correct));
            tvGameFeedback.setTextColor(ContextCompat.getColor(this, R.color.correctAnswer));
            tvGameScore.setText(getString(R.string.quiz_score, score));

            playSfx(true);
            playCorrectAnimation(clickedButton);

            if (score > bestScore) {
                bestScore = score;
                tvGameBest.setText(getString(R.string.quiz_best_score, bestScore));
                saveHighScore(bestScore);
                celebrate();
            }
        } else {
            tvGameFeedback.setText(getString(R.string.quiz_feedback_wrong, currentCorrectAnswer));
            tvGameFeedback.setTextColor(ContextCompat.getColor(this, R.color.wrongAnswer));
            playSfx(false);
            playWrongAnimation(clickedButton);
            
            // Decrease time by 1 second on wrong answer
            timeLeftMs -= PENALTY_TIME_MS;
            if (timeLeftMs < 0) timeLeftMs = 0;
            tvGameTimer.setText(getString(R.string.quiz_timer, (int)(timeLeftMs / 1000)));
        }

        // Next question immediately
        generateNewQuestion();
    }

    // -------------------------
    // TIMER
    // -------------------------
    private void startTimer() {
        cancelTimer();
        countDownTimer = new CountDownTimer(timeLeftMs, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMs = millisUntilFinished;
                long s = millisUntilFinished / 1000;
                tvGameTimer.setText(getString(R.string.quiz_timer, (int)s));

                if (speedProgressBar != null) {
                    int progress = (int) ((millisUntilFinished * 100) / TOTAL_TIME_MS);
                    speedProgressBar.setProgress(progress);
                }
                
                // Change to red if 10 seconds or below
                if (s <= 10) {
                    tvGameTimer.setTextColor(ContextCompat.getColor(SpeedGameActivity.this, R.color.wrongAnswer));
                    if (speedProgressBar != null) {
                        speedProgressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(SpeedGameActivity.this, R.color.wrongAnswer)));
                    }
                } else {
                    tvGameTimer.setTextColor(ContextCompat.getColor(SpeedGameActivity.this, R.color.colorPrimary));
                    if (speedProgressBar != null) {
                        speedProgressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(SpeedGameActivity.this, R.color.colorAccent)));
                    }
                }
            }

            @Override
            public void onFinish() {
                timeLeftMs = 0;
                tvGameTimer.setText(getString(R.string.quiz_timer_done));
                tvGameFeedback.setText(getString(R.string.quiz_final_score, score));

                // Disable buttons
                setOptionsEnabled(false);

                // --- Gamification: Progress quests for speed game completion ---
                try {
                    com.edulinguaghana.gamification.QuestManager.progressQuest(SpeedGameActivity.this, "speed_game", 1);        // Quest 6
                    com.edulinguaghana.gamification.QuestManager.progressQuest(SpeedGameActivity.this, "marathon_learner", 1);  // Quest 8

                    // Unlock speed champion badge when speed game is won
                    com.edulinguaghana.gamification.BadgeManager.unlockBadge(SpeedGameActivity.this, "speed_champion");

                    // Award XP for completing speed game
                    int xpAward = score + (score > 20 ? 10 : 0); // Bonus XP for good performance
                    com.edulinguaghana.gamification.XPManager.awardXP(SpeedGameActivity.this, xpAward, "speed_game_complete");

                    // Record practice for streak and language tracking
                    StreakManager streakManager = new StreakManager(SpeedGameActivity.this);
                    streakManager.recordPractice();

                    // Track language usage for language_explorer quest
                    ProgressManager.trackLanguageUsage(SpeedGameActivity.this, languageCode);
                } catch (Exception ignored) { }
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
        MaterialButton[] options = {btnOption1, btnOption2, btnOption3, btnOption4, btnOption5, btnOption6};
        for (MaterialButton btn : options) {
            if (btn != null) btn.setEnabled(enabled);
        }
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
