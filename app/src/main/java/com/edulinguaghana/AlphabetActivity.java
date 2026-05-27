package com.edulinguaghana;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.edulinguaghana.tracking.ProgressTracker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.SeekBar;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.edulinguaghana.tts.OfflineGhanaLPTtsService;

public class AlphabetActivity extends AppCompatActivity {

    private TextView tvLanguageTitle, tvLetter, tvLetterWord;
    private TextView tvProgressCounter;
    private MaterialButton btnPrev, btnNext, btnSpeak;
    private FloatingActionButton btnBack;
    private LinearProgressIndicator progressBar;
    private MaterialCardView letterCard, languageCard;
    private MaterialCardView modeBadgeCard;
    private TextView modeBadgeIcon, modeBadgeText, modeBadgeDescription;
    private ImageView decorativeShape1, decorativeShape2, decorativeShape3, decorativeShape4;
    private ImageView progressIcon;
    private TextView modeIcon;  // Changed to TextView for emoji
    private TextView celebrationEmoji;  // For celebration animations
    private SeekBar seekBarNavigation;  // For smooth navigation
    private MaterialButton btnShowGrid;  // For quick access grid
    private MaterialButton btnSpeakQuick;  // For quick speak button

    private Vibrator vibrator;  // For haptic feedback

    private String languageCode;
    private String languageName;
    private String mode;           // "recital" or "practice"
    private boolean isRecitalMode;

    private String[] letters;
    private String[] words;
    private int currentIndex = 0;
    private int practiceRetryCount = 0;
    private static final int MAX_RETRIES_BEFORE_TIP = 2;


    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private final AudioManager.OnAudioFocusChangeListener focusChangeListener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
            if (tts != null) tts.stop();
        }
    };

    // Progress tracking
    private ProgressTracker progressTracker;
    private long startTime;
    private boolean lessonCompletedLogSent = false;

    // Offline TTS for native Ghanaian languages (loads from res/raw)
    private OfflineGhanaLPTtsService offlineTts;
    private boolean isGhanaLpPlaying = false;

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int REQ_CODE_RECORD_AUDIO = 200;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CURRENT_INDEX", currentIndex);
        outState.putBoolean("LESSON_LOGGED", lessonCompletedLogSent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alphabet);

        // Initialize text views
        tvLanguageTitle = findViewById(R.id.tvLanguageTitle);
        tvLetter = findViewById(R.id.tvLetter);

        // Set up toolbar back button
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvLetterWord = findViewById(R.id.tvLetterWord);
        tvProgressCounter = findViewById(R.id.tvProgressCounter);
        celebrationEmoji = findViewById(R.id.celebrationEmoji);

        // Initialize buttons
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnSpeak = findViewById(R.id.btnSpeak);

        // Initialize other views
        progressBar = findViewById(R.id.progressBar);
        letterCard = findViewById(R.id.letterCard);
        languageCard = findViewById(R.id.languageCard);
        modeBadgeCard = findViewById(R.id.modeBadgeCard);
        modeBadgeIcon = findViewById(R.id.modeBadgeIcon);
        modeBadgeText = findViewById(R.id.modeBadgeText);
        modeBadgeDescription = findViewById(R.id.modeBadgeDescription);

        // Initialize decorative elements
        decorativeShape1 = findViewById(R.id.decorativeShape1);
        decorativeShape2 = findViewById(R.id.decorativeShape2);
        decorativeShape3 = findViewById(R.id.decorativeShape3);
        decorativeShape4 = findViewById(R.id.decorativeShape4);
        progressIcon = findViewById(R.id.progressIcon);
        modeIcon = findViewById(R.id.modeIcon);
        
        // Initialize navigation controls
        seekBarNavigation = findViewById(R.id.seekBarNavigation);
        btnShowGrid = findViewById(R.id.btnShowGrid);
        btnSpeakQuick = findViewById(R.id.btnSpeakQuick);

        // Initialize vibrator for haptic feedback
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        progressTracker = new ProgressTracker();
        startTime = System.currentTimeMillis();


        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");
        mode = getIntent().getStringExtra("MODE");

        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt("CURRENT_INDEX", 0);
            lessonCompletedLogSent = savedInstanceState.getBoolean("LESSON_LOGGED", false);
        }

        if (languageName == null) languageName = "Unknown";
        if (mode == null) mode = "practice";

        isRecitalMode = mode.equals("recital");

        tvLanguageTitle.setText(getString(R.string.language_prefix) + " " + languageName);
        btnSpeak.setText(isRecitalMode ? getString(R.string.alphabet_mode_recital) : getString(R.string.alphabet_mode_practice));
        updateModeBadge();

        // --- LOAD ALPHABET & WORDS FROM RESOURCES ---
        switch (languageCode) {
            case "ak":
                letters = getResources().getStringArray(R.array.letters_ak);
                words = getResources().getStringArray(R.array.words_ak);
                break;
            case "gaa":
                letters = getResources().getStringArray(R.array.letters_gaa);
                words = getResources().getStringArray(R.array.words_gaa);
                break;
            case "twi":
                letters = getResources().getStringArray(R.array.letters_ak); // Twi and Akan share alphabet
                words = getResources().getStringArray(R.array.words_twi);
                break;
            case "ee":
                letters = getResources().getStringArray(R.array.letters_ee);
                words = getResources().getStringArray(R.array.words_ee);
                break;
            case "fr":
                letters = getResources().getStringArray(R.array.letters_en_fr);
                words = getResources().getStringArray(R.array.words_fr);
                break;
            default: // "en"
                letters = getResources().getStringArray(R.array.letters_en_fr);
                words = getResources().getStringArray(R.array.words_en);
                break;
        }

        progressBar.setMax(letters.length);
        if (seekBarNavigation != null) {
            seekBarNavigation.setMax(letters.length - 1);
            seekBarNavigation.setProgress(currentIndex);
        }

        // Initialize Offline TTS for native languages (loads from res/raw)
        offlineTts = new OfflineGhanaLPTtsService(this);

        // Start background animations after views are ready
        try {
            startBackgroundAnimations();
        } catch (Exception ignored) {
            // Animations failed, but continue without them
        }

        tts = new TextToSpeech(this, status -> {
            try {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(com.edulinguaghana.utils.LanguageConversionUtils.getLocaleForLanguage(languageCode));
                    updateLetter();
                    if (isRecitalMode) {
                        speakCurrentLetter();
                    }
                } else {
                    updateLetter();
                }
            } catch (Exception e) {
                try {
                    updateLetter();
                } catch (Exception ignored) {
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                animateButtonPress(btnNext);
            } catch (Exception e) {
                android.util.Log.w("AlphabetActivity", "Haptic feedback failed", e);
            }
            try {
                currentIndex++;
                if (currentIndex >= letters.length) {
                    currentIndex = 0;
                    checkAndLogCompletion();
                }
                updateLetterWithAnimation();
                if (seekBarNavigation != null) seekBarNavigation.setProgress(currentIndex);
                if (isRecitalMode) speakCurrentLetter();
            } catch (Exception ignored) {
            }
        });

        btnPrev.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                animateButtonPress(btnPrev);
            } catch (Exception e) {
                android.util.Log.w("AlphabetActivity", "Haptic feedback failed", e);
            }
            try {
                currentIndex--;
                if (currentIndex < 0) currentIndex = letters.length - 1;
                updateLetterWithAnimation();
                if (seekBarNavigation != null) seekBarNavigation.setProgress(currentIndex);
                if (isRecitalMode) speakCurrentLetter();
            } catch (Exception ignored) {
            }
        });

        btnBack.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                animateButtonPress(btnBack);
            } catch (Exception e) {
                android.util.Log.w("AlphabetActivity", "Haptic feedback failed", e);
            }
            finish();
        });

        btnSpeak.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                animateButtonPress(btnSpeak);
                celebrateAction();  // Celebration animation
            } catch (Exception e) {
                android.util.Log.w("AlphabetActivity", "Haptic feedback or celebration failed", e);
            }
            try {
                if (isRecitalMode) {
                    speakCurrentLetter();
                } else {
                    startPracticePronunciation();
                }
            } catch (Exception ignored) {
            }
        });

        // Make letter card tappable to speak
        letterCard.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                speakCurrentLetter();
                celebrateAction();  // Show celebration
            } catch (Exception ignored) {
            }
        });

        // SeekBar navigation for smooth traversal
        seekBarNavigation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean isUserChanging = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    isUserChanging = true;
                    currentIndex = progress;
                    updateLetterWithAnimation();
                    if (isRecitalMode) speakCurrentLetter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserChanging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserChanging = false;
            }
        });

        // Quick access grid button
        btnShowGrid.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showLetterPickerDialog();
            } catch (Exception ignored) {
            }
        });

        // Quick speak button
        btnSpeakQuick.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                animateButtonPress(btnSpeakQuick);
                speakCurrentLetter();
                celebrateAction();
            } catch (Exception ignored) {
            }
        });
    }

    private void updateLetter() {
        practiceRetryCount = 0; // Reset retry count when changing letters
        try {
            // Update text with letter
            String letter = letters[currentIndex];
            tvLetter.setText(letter);
            tvLetterWord.setText(words[currentIndex]);

            // Update progress bar and counter
            progressBar.setProgress(currentIndex + 1);
            updateProgressCounter();
        } catch (Exception ignored) {
        }
    }

    private void updateLetterWithAnimation() {
        // Animate letter change with smooth fade and scale
        try {
            animateLetterChange();
        } catch (Exception ignored) {
        }

        // Update text with letter
        String letter = letters[currentIndex];
        tvLetter.setText(letter);
        tvLetterWord.setText(words[currentIndex]);

        // Update progress bar and counter
        progressBar.setProgress(currentIndex + 1);
        updateProgressCounter();

        try {
            animateProgressIcon();
        } catch (Exception ignored) {
        }
    }

    private void updateProgressCounter() {
        if (tvProgressCounter != null) {
            String progressText = getString(R.string.alphabet_progress_counter, (currentIndex + 1), letters.length);
            tvProgressCounter.setText(progressText);

            // Animate counter update
            tvProgressCounter.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction(() -> {
                    tvProgressCounter.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start();
                })
                .start();
        }
    }

    private void animateLetter() {
        try {
            Animation letterAnim = AnimationUtils.loadAnimation(this, R.anim.letter_bounce);
            tvLetter.startAnimation(letterAnim);

            // Animate word text
            Animation wordAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            tvLetterWord.startAnimation(wordAnim);
        } catch (Exception ignored) {
        }
    }

    private void startBackgroundAnimations() {
        try {
            // Floating animations for decorative shapes
            if (decorativeShape1 != null && decorativeShape2 != null &&
                decorativeShape3 != null && decorativeShape4 != null) {
                Animation float1 = AnimationUtils.loadAnimation(this, R.anim.floating_element);
                Animation float2 = AnimationUtils.loadAnimation(this, R.anim.diagonal_drift);
                Animation float3 = AnimationUtils.loadAnimation(this, R.anim.circular_orbit);
                Animation float4 = AnimationUtils.loadAnimation(this, R.anim.zigzag_path);

                float1.setStartOffset(0);
                float2.setStartOffset(500);
                float3.setStartOffset(1000);
                float4.setStartOffset(1500);

                decorativeShape1.startAnimation(float1);
                decorativeShape2.startAnimation(float2);
                decorativeShape3.startAnimation(float3);
                decorativeShape4.startAnimation(float4);
            }

            // Animate language card icon
            if (modeIcon != null) {
                Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
                modeIcon.startAnimation(pulse);
            }

            // Animate progress icon
            if (progressIcon != null) {
                Animation sparkle = AnimationUtils.loadAnimation(this, R.anim.star_twinkle);
                progressIcon.startAnimation(sparkle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateButtonPress(android.view.View button) {
        try {
            if (button != null) {
                Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_pop);
                button.startAnimation(bounce);
            }
        } catch (Exception ignored) {
        }
    }

    private void animateLetterChange() {
        try {
            if (tvLetter != null && tvLetterWord != null) {
                // Create smooth scale and fade animation
                tvLetter.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .alpha(0.5f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        tvLetter.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .alpha(1.0f)
                            .setDuration(150)
                            .start();
                    })
                    .start();

                // Fade word
                tvLetterWord.animate()
                    .alpha(0f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        tvLetterWord.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                    })
                    .start();
            }
        } catch (Exception ignored) {
        }
    }

    private void animateProgressIcon() {
        try {
            if (progressIcon != null) {
                Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_pop);
                progressIcon.startAnimation(bounce);
            }
        } catch (Exception ignored) {
        }
    }

    private void speakCurrentLetter() {
        if (!requestAudioFocus()) return;
        try {
            String letter = letters[currentIndex];

            // Try to load audio from recorded files first
            int resId = getLetterAudioResId(languageCode, letter);
            if (resId != 0) {
                playAudioResource(resId);
            } else if (com.edulinguaghana.utils.LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
                // Fallback to GhanaLP TTS if no recorded audio exists
                speakWithGhanaLP(letter);
            } else if (tts != null) {
                // Use language-specific pronunciation for letters
                String textToSpeak = getLetterPronunciation(letter);
                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "LETTER_ID");
            }

            animateLetter();
        } catch (Exception ignored) {
        }
    }

    private boolean isGhanaianLanguage(String code) {
        if (code == null) return false;
        String lower = code.toLowerCase();
        return lower.equals("ak") || lower.equals("twi") ||
               lower.equals("ee") || lower.equals("ewe") ||
               lower.equals("gaa") || lower.equals("ga");
    }

    private void speakWithGhanaLP(String text) {
        try {
            if (isGhanaLpPlaying) {
                offlineTts.stop();
            }

            // Normalize language code for audio file lookup
            final String apiLangCode = com.edulinguaghana.utils.LanguageConversionUtils.normalizeLanguageCode(languageCode);

            // Disable speak button during playback
            if (btnSpeak != null) {
                btnSpeak.setEnabled(false);
            }

            // Speak the LETTER for recital mode (not the word)
            // For recital, we want to speak the individual letter
            String letterToSpeak = text;

            offlineTts.speakLetter(
                letterToSpeak,
                apiLangCode,
                new OfflineGhanaLPTtsService.PlaybackCallback() {
                    @Override
                    public void onStart() {
                        isGhanaLpPlaying = true;
                    }

                    @Override
                    public void onComplete() {
                        isGhanaLpPlaying = false;
                        runOnUiThread(() -> {
                            if (btnSpeak != null) {
                                btnSpeak.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // Fallback: Try letter pronunciation
                        try {
                            offlineTts.speakLetter(text, apiLangCode, new OfflineGhanaLPTtsService.PlaybackCallback() {
                                @Override
                                public void onStart() {
                                    isGhanaLpPlaying = true;
                                }

                                @Override
                                public void onComplete() {
                                    isGhanaLpPlaying = false;
                                    runOnUiThread(() -> {
                                        if (btnSpeak != null) {
                                            btnSpeak.setEnabled(true);
                                        }
                                    });
                                }

                                @Override
                                public void onError(String letterError) {
                                    // Final fallback to Android TTS
                                    isGhanaLpPlaying = false;
                                    runOnUiThread(() -> {
                                        if (btnSpeak != null) {
                                            btnSpeak.setEnabled(true);
                                        }
                                        android.util.Log.w("AlphabetActivity", "No offline audio found for: " + text);
                                        try {
                                            if (tts != null) {
                                                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LETTER_ID");
                                            }
                                        } catch (Exception e) {
                                            android.util.Log.e("AlphabetActivity", "TTS fallback failed", e);
                                        }
                                    });
                                }
                            });
                        } catch (Exception e) {
                            android.util.Log.e("AlphabetActivity", "Error in fallback letter speak", e);
                            isGhanaLpPlaying = false;
                            try {
                                if (btnSpeak != null) {
                                    btnSpeak.setEnabled(true);
                                }
                                if (tts != null) {
                                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LETTER_ID");
                                }
                            } catch (Exception ttsError) {
                                android.util.Log.e("AlphabetActivity", "Final TTS fallback also failed", ttsError);
                                if (btnSpeak != null) {
                                    btnSpeak.setEnabled(true);
                                }
                            }
                        }
                    }
                }
            );
        } catch (Exception e) {
            android.util.Log.e("AlphabetActivity", "Error in speakWithGhanaLP", e);
            isGhanaLpPlaying = false;
            try {
                if (btnSpeak != null) {
                    btnSpeak.setEnabled(true);
                }
                if (tts != null && tts.isSpeaking()) {
                    tts.stop();
                }
                if (tts != null) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LETTER_ID");
                }
            } catch (Exception ttsError) {
                android.util.Log.e("AlphabetActivity", "TTS ultimate fallback failed", ttsError);
                if (btnSpeak != null) {
                    btnSpeak.setEnabled(true);
                }
            }
        }
    }

    private String normalizeLanguageCodeLegacy(String code) {
        if (code == null) return "twi";
        switch (code.toLowerCase()) {
            case "ak":
            case "twi":
                return "twi";
            case "ee":
            case "ewe":
                return "ewe";
            case "gaa":
            case "ga":
                return "ga";
            default:
                return code.toLowerCase();
        }
    }

    private int getLetterAudioResId(String lang, String letter) {
        if (lang == null || letter == null) return 0;

        String langLower = lang.toLowerCase(Locale.ROOT);
        String letterLower = letter.toLowerCase(Locale.ROOT);

        // For Ewe language, map special characters to safe filenames
        if ("ee".equals(langLower) || "ewe".equals(langLower)) {
            String sanitizedLetter = letterLower;
            switch (letterLower) {
                case "ɛ":
                    sanitizedLetter = "e_open";
                    break;
                case "ɔ":
                    sanitizedLetter = "o_open";
                    break;
                case "ŋ":
                    sanitizedLetter = "ng";
                    break;
                case "ɖ":
                    sanitizedLetter = "d_caron";
                    break;
                case "ƒ":
                    sanitizedLetter = "f_hook";
                    break;
                case "ɣ":
                    sanitizedLetter = "g_hook";
                    break;
                case "ʋ":
                    sanitizedLetter = "v_hook";
                    break;
            }
            String fileName = "ewe_letter_" + sanitizedLetter;
            return getResources().getIdentifier(fileName, "raw", getPackageName());
        }

        // For Gaa language, try direct character match first, then fallback to safe names
        if ("gaa".equals(langLower) || "ga".equals(langLower)) {
            String sanitizedLetter = letterLower;
            switch (letterLower) {
                case "ɛ":
                    sanitizedLetter = "e_open";
                    break;
                case "ɔ":
                    sanitizedLetter = "o_open";
                    break;
                case "ŋ":
                    sanitizedLetter = "ng";
                    break;
            }
            String fileName = "gaa_letter_" + sanitizedLetter;
            return getResources().getIdentifier(fileName, "raw", getPackageName());
        }

        // For other languages (Akan, French, English)
        String sanitizedLetter = letterLower;
        switch (letterLower) {
            case "ɛ":
                sanitizedLetter = "e_open";
                break;
            case "ɔ":
                sanitizedLetter = "o_open";
                break;
            case "ŋ":
                sanitizedLetter = "ng";
                break;
        }

        String fileName = langLower + "_letter_" + sanitizedLetter;
        return getResources().getIdentifier(fileName, "raw", getPackageName());
    }

    /**
     * Get pronunciation for a letter based on language
     * Returns appropriate pronunciation for English, French, etc.
     */
    private String getLetterPronunciation(String letter) {
        if (letter == null || letter.isEmpty()) {
            return letter;
        }

        if ("fr".equals(languageCode)) {
            return getFrenchLetterPronunciation(letter);
        } else {
            // Default to English pronunciation
            return getEnglishLetterPronunciation(letter);
        }
    }

    /**
     * Get English pronunciation for a letter
     */
    private String getEnglishLetterPronunciation(String letter) {
        if (letter == null || letter.isEmpty()) {
            return letter;
        }

        switch (letter.toUpperCase()) {
            // Vowels
            case "E": return "ee";
            case "I": return "eye";
            case "O": return "oh";
            case "U": return "you";
            case "Y": return "why";

            // Consonants that need special pronunciation
            case "H": return "aitch";
            case "W": return "double you";
            case "Z": return "zee";

            // For all other letters (including A and X), just return as is
            default:
                return letter;
        }
    }

    /**
     * Get French pronunciation for a letter
     * Maps vowels and consonants to their proper French pronunciations
     */
    private String getFrenchLetterPronunciation(String letter) {
        if (letter == null || letter.isEmpty()) {
            return letter;
        }

        // For French, vowels need special handling
        switch (letter.toUpperCase()) {
            // Vowels - use full pronunciation words for clarity
            case "A":
                return "a";
            case "E":
                return "e";
            case "I":
                return "i";
            case "O":
                return "o";
            case "U":
                return "u";
            case "Y":
                return "i grec"; // "i grec" (Greek i) in French

            // Common consonants
            case "H":
                return "ache"; // h is silent, so say "ache"
            case "W":
                return "double v";
            case "X":
                return "iks";
            case "Z":
                return "zed";

            // For all other letters, just return as is
            default:
                return letter;
        }
    }

    private void playAudioResource(int resId) {
        if (!requestAudioFocus()) return;
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
        }
        mediaPlayer = MediaPlayer.create(this, resId);
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> {
                try {
                    mp.release();
                } catch (Exception ignored) {}
                if (mediaPlayer == mp) {
                    mediaPlayer = null;
                }
            });
            mediaPlayer.start();
        }
    }

    private void startPracticePronunciation() {
        speakCurrentLetter();

        if (!isRecordAudioPermissionGranted()) {
            requestRecordAudioPermission();
        } else {
            promptSpeechInput();
        }
    }

    private void showPronunciationTips() {
        String tips = "";
        String letter = letters[currentIndex];

        String normalizedCode = com.edulinguaghana.utils.LanguageConversionUtils.normalizeLanguageCode(languageCode);
        switch (normalizedCode) {
            case com.edulinguaghana.utils.LanguageConversionUtils.LANG_TWI:
                tips = getTwiPronunciationTip(letter);
                break;
            case com.edulinguaghana.utils.LanguageConversionUtils.LANG_EWE:
                tips = getEwePronunciationTip(letter);
                break;
            case com.edulinguaghana.utils.LanguageConversionUtils.LANG_GA:
                tips = getGaPronunciationTip(letter);
                break;
        }

        if (!tips.isEmpty()) {
            Toast.makeText(this, getString(R.string.alphabet_tip_prefix) + tips, Toast.LENGTH_LONG).show();
        }
    }

    private String getTwiPronunciationTip(String letter) {
        switch (letter) {
            case "Ɛ": return "Pronounced like 'eh' - open mouth more";
            case "Ɔ": return "Pronounced like 'aw' - round your lips";
            default: return "Listen to the audio carefully and repeat!";
        }
    }

    private String getEwePronunciationTip(String letter) {
        switch (letter) {
            case "Ɛ": return "Open e - sounds like 'eh'";
            case "Ɔ": return "Open o - sounds like 'aw'";
            case "Ɖ": return "D with hook - softer than regular D";
            case "Ƒ": return "F with hook - similar to F but softer";
            case "Ɣ": return "G with hook - guttural sound";
            case "Ŋ": return "Ng - velar nasal sound";
            case "Ʋ": return "V with hook - like a v sound";
            default: return "Listen carefully and repeat!";
        }
    }

    private String getGaPronunciationTip(String letter) {
        switch (letter) {
            case "Ɛ": return "Open e - sounds like 'eh'";
            case "Ɔ": return "Open o - sounds like 'aw'";
            case "Ŋ": return "Ng - velar nasal sound";
            default: return "Listen carefully and repeat!";
        }
    }

    private boolean isRecordAudioPermissionGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQ_CODE_RECORD_AUDIO);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getSpeechLocaleCode());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.alphabet_pronunciation_prompt));

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.alphabet_toast_speech_not_supported), Toast.LENGTH_LONG).show();
        }
    }

    private String getSpeechLocaleCode() {
        return com.edulinguaghana.utils.LanguageConversionUtils.getSpeechLocaleCode(languageCode);
    }

    private Locale getLocaleForLanguage(String code) {
        if (code == null) return Locale.ENGLISH;
        switch (code) {
            case "fr": return Locale.FRENCH;
            case "ak":
            case "twi": return new Locale("twi");
            case "ee": return new Locale("ee");
            case "gaa": return new Locale("gaa");
            default: return Locale.ENGLISH;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String recognized = result.get(0).trim();
                evaluatePronunciation(recognized);
            } else {
                Toast.makeText(this, getString(R.string.alphabet_toast_not_heard), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void evaluatePronunciation(String recognized) {
        if (recognized == null || recognized.trim().isEmpty()) {
            Toast.makeText(this, "🔇 Could not detect any sound.\n\n🎤 Please try again and speak clearly!", Toast.LENGTH_SHORT).show();
            return;
        }

        String expectedLetter = letters[currentIndex];
        String recognizedClean = recognized.trim().toLowerCase();
        String expectedLetterLower = expectedLetter.toLowerCase();

        // 1. Direct match (letter itself or case-insensitive)
        if (recognizedClean.equals(expectedLetterLower) || recognized.equalsIgnoreCase(expectedLetter)) {
            handleSuccess(recognized, expectedLetter);
            return;
        }

        // 2. First letter match (e.g., user says "Bee" for "B")
        if (recognizedClean.length() > 0 && String.valueOf(recognizedClean.charAt(0)).equalsIgnoreCase(expectedLetter)) {
            handleSuccess(recognized, expectedLetter);
            return;
        }

        // 3. Phonetic match (check if any of our accepted phonetic spellings match)
        List<String> acceptedSpellings = getAcceptedPhoneticSpellings(expectedLetter, languageCode);
        for (String spelling : acceptedSpellings) {
            if (recognizedClean.contains(spelling.toLowerCase())) {
                handleSuccess(recognized, expectedLetter);
                return;
            }
        }

        // 4. Example word match (if they say the example word instead of just the letter)
        String exampleWord = words[currentIndex].toLowerCase();
        if (recognizedClean.contains(exampleWord)) {
            handleSuccess(recognized, expectedLetter);
            return;
        }

        // 5. Special handling for common misrecognitions
        if (handleSpecialMisrecognitions(expectedLetter, recognizedClean)) {
            handleSuccess(recognized, expectedLetter);
            return;
        }

        // If all else fails
        practiceRetryCount++;
        
        if (practiceRetryCount >= MAX_RETRIES_BEFORE_TIP) {
             showPronunciationTips();
             practiceRetryCount = 0; // Reset after showing tip
        } else {
            Toast.makeText(this,
                "🎤 I heard: \"" + recognized + "\"" +
                "\n📝 Expected: \"" + expectedLetter + "\"" +
                "\n\n💡 Try to say it clearly. Listen again if you need to!",
                Toast.LENGTH_LONG).show();
        }
    }

    private void handleSuccess(String recognized, String expected) {
        practiceRetryCount = 0; // Reset retry count on success

        // Visual indication on the card
        if (letterCard != null) {
            // Store original values
            ColorStateList originalStrokeColor = letterCard.getStrokeColorStateList();
            ColorStateList originalBgColor = letterCard.getCardBackgroundColor();
            float originalElevation = letterCard.getCardElevation();

            // Success state - change colors and pop out
            letterCard.setStrokeColor(ColorStateList.valueOf(getColor(R.color.correctAnswer)));
            letterCard.setCardBackgroundColor(ColorStateList.valueOf(getColor(R.color.modeProgressStart))); // Light green success background
            letterCard.setCardElevation(originalElevation * 1.5f);

            // Revert after a delay
            letterCard.postDelayed(() -> {
                if (letterCard != null) {
                    letterCard.setStrokeColor(originalStrokeColor);
                    letterCard.setCardBackgroundColor(originalBgColor);
                    letterCard.setCardElevation(originalElevation);
                }
            }, 2500);
        }

        // Change emoji temporarily and trigger celebration
        if (celebrationEmoji != null) {
            String originalEmoji = celebrationEmoji.getText().toString();
            celebrationEmoji.setText("✅");
            celebrateAction();

            celebrationEmoji.postDelayed(() -> {
                if (celebrationEmoji != null) {
                    celebrationEmoji.setText(originalEmoji);
                }
            }, 2500);
        }

        // Play success sound
        playAudioResource(R.raw.correct);

        Toast.makeText(this,
            getString(R.string.alphabet_toast_success, recognized, expected),
            Toast.LENGTH_LONG).show();

        // Trigger haptic feedback for success
        triggerHapticFeedback(100); // Slightly longer for success
        
        // Mark progress - if they complete 50% or more, consider it a good session
        if (currentIndex > letters.length / 2) {
             checkAndLogCompletion();
        }
    }

    private boolean handleSpecialMisrecognitions(String expected, String recognized) {
        // Add common misrecognitions here that aren't strictly phonetic
        if (expected.equalsIgnoreCase("W") && (recognized.contains("double") || recognized.contains("you"))) return true;
        if (expected.equalsIgnoreCase("Ɛ") && (recognized.contains("air") || recognized.contains("eh") || recognized.contains("at"))) return true;
        if (expected.equalsIgnoreCase("Ɔ") && (recognized.contains("awe") || recognized.contains("oh") || recognized.contains("or"))) return true;
        return false;
    }

    private List<String> getAcceptedPhoneticSpellings(String letter, String language) {
        List<String> spellings = new ArrayList<>();
        String upper = letter.toUpperCase();

        if ("fr".equals(language)) {
            switch (upper) {
                case "A": spellings.add("a"); break;
                case "B": spellings.add("be"); spellings.add("bé"); break;
                case "C": spellings.add("ce"); spellings.add("cé"); break;
                case "D": spellings.add("de"); spellings.add("dé"); break;
                case "E": spellings.add("e"); break;
                case "F": spellings.add("effe"); break;
                case "G": spellings.add("ge"); spellings.add("gé"); break;
                case "H": spellings.add("ache"); break;
                case "I": spellings.add("i"); break;
                case "J": spellings.add("ji"); break;
                case "K": spellings.add("ka"); break;
                case "L": spellings.add("elle"); break;
                case "M": spellings.add("emme"); break;
                case "N": spellings.add("enne"); break;
                case "O": spellings.add("o"); break;
                case "P": spellings.add("pe"); spellings.add("pé"); break;
                case "Q": spellings.add("ku"); break;
                case "R": spellings.add("erre"); break;
                case "S": spellings.add("esse"); break;
                case "T": spellings.add("te"); spellings.add("té"); break;
                case "U": spellings.add("u"); break;
                case "V": spellings.add("ve"); spellings.add("vé"); break;
                case "W": spellings.add("double"); break;
                case "X": spellings.add("ixe"); break;
                case "Y": spellings.add("grec"); break;
                case "Z": spellings.add("zed"); break;
            }
        } else {
            // English or general phonetic
            switch (upper) {
                case "A": spellings.add("ay"); spellings.add("ey"); spellings.add("ei"); break;
                case "B": spellings.add("bee"); spellings.add("be"); break;
                case "C": spellings.add("see"); spellings.add("sea"); spellings.add("si"); break;
                case "D": spellings.add("dee"); spellings.add("di"); break;
                case "E": spellings.add("ee"); spellings.add("i"); break;
                case "F": spellings.add("ef"); spellings.add("eff"); break;
                case "G": spellings.add("gee"); spellings.add("jee"); spellings.add("gi"); break;
                case "H": spellings.add("aitch"); spellings.add("edge"); spellings.add("each"); break;
                case "I": spellings.add("eye"); spellings.add("ai"); break;
                case "J": spellings.add("jay"); break;
                case "K": spellings.add("kay"); break;
                case "L": spellings.add("el"); spellings.add("ell"); break;
                case "M": spellings.add("em"); break;
                case "N": spellings.add("en"); break;
                case "O": spellings.add("oh"); break;
                case "P": spellings.add("pee"); spellings.add("pi"); break;
                case "Q": spellings.add("cue"); spellings.add("queue"); break;
                case "R": spellings.add("are"); break;
                case "S": spellings.add("es"); spellings.add("ess"); break;
                case "T": spellings.add("tee"); spellings.add("ti"); break;
                case "U": spellings.add("you"); break;
                case "V": spellings.add("vee"); spellings.add("vi"); break;
                case "W": spellings.add("double"); break;
                case "X": spellings.add("ex"); break;
                case "Y": spellings.add("why"); break;
                case "Z": spellings.add("zee"); spellings.add("zed"); break;
                
                // Ghanaian special characters (how they might be recognized by English-trained model)
                case "Ɛ": 
                    spellings.add("eh"); spellings.add("air"); spellings.add("at"); 
                    spellings.add("end"); spellings.add("egg"); spellings.add("et");
                    break;
                case "Ɔ": 
                    spellings.add("aw"); spellings.add("oh"); spellings.add("or"); 
                    spellings.add("on"); spellings.add("awe"); spellings.add("o");
                    break;
                case "Ɖ": spellings.add("de"); spellings.add("the"); break;
                case "Ƒ": spellings.add("ef"); spellings.add("if"); break;
                case "Ɣ": spellings.add("ga"); spellings.add("her"); break;
                case "Ŋ": spellings.add("ng"); spellings.add("ink"); spellings.add("ing"); spellings.add("thing"); break;
                case "Ʋ": spellings.add("vu"); spellings.add("view"); break;
            }
        }
        return spellings;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_RECORD_AUDIO) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                promptSpeechInput();
            } else {
                Toast.makeText(this, getString(R.string.alphabet_toast_mic_needed), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Trigger haptic feedback (vibration)
     * @param durationMs Duration in milliseconds (30-50 for light feedback)
     */
    private void triggerHapticFeedback(long durationMs) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(durationMs);
            }
        }
    }

    /**
     * Show celebration animation with emoji
     */
    private void celebrateAction() {
        if (celebrationEmoji != null) {
            // Celebration emoji animation
            celebrationEmoji.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(150)
                .withEndAction(() -> {
                    celebrationEmoji.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .start();
                })
                .start();

            // Rotate emoji for extra fun
            celebrationEmoji.animate()
                .rotation(360)
                .setDuration(500)
                .start();
        }
    }

    /**
     * Show letter picker in a bottom sheet dialog with enhanced UI
     */
    private void showLetterPickerDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_letter_picker, null);
        TextView tvTitle = bottomSheetView.findViewById(R.id.tvGridTitle);
        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.letterGrid);
        
        if (tvTitle != null) tvTitle.setText(R.string.alphabet_grid_title);
        
        List<String> letterList = Arrays.asList(letters);
        
        recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_letter_picker, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                TextView textView = holder.itemView.findViewById(R.id.tvLetterItem);
                MaterialCardView card = (MaterialCardView) holder.itemView;
                
                textView.setText(letterList.get(position));
                
                if (currentIndex == position) {
                    card.setStrokeWidth(4);
                    card.setStrokeColor(ColorStateList.valueOf(getColor(R.color.colorAccent)));
                    textView.setTextColor(getColor(R.color.colorAccent));
                } else {
                    card.setStrokeWidth(0);
                    textView.setTextColor(getColor(R.color.textColorPrimary));
                }
                
                card.setOnClickListener(v -> {
                    currentIndex = holder.getAdapterPosition();
                    if (seekBarNavigation != null) seekBarNavigation.setProgress(currentIndex);
                    updateLetterWithAnimation();
                    if (isRecitalMode) speakCurrentLetter();
                    dialog.dismiss();
                });
            }

            @Override
            public int getItemCount() {
                return letterList.size();
            }
        });
        
        dialog.setContentView(bottomSheetView);
        dialog.show();
    }

    private void checkAndLogCompletion() {
        if (!lessonCompletedLogSent && currentIndex >= letters.length - 1) {
            lessonCompletedLogSent = true;
            long duration = (System.currentTimeMillis() - startTime) / 1000;
            
            String userId = null;
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
            
            progressTracker.logLessonCompletion(
                this, 
                userId, 
                languageName + " Alphabet", 
                "alphabet", 
                duration, 
                null
            );
            
            Toast.makeText(this, getString(R.string.alphabet_lesson_complete, languageName), Toast.LENGTH_LONG).show();
            celebrateAction();
        }
    }

    private boolean requestAudioFocus() {
        if (audioManager == null) return true;

        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (focusRequest == null) {
                AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(playbackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(focusChangeListener)
                        .build();
            }
            result = audioManager.requestAudioFocus(focusRequest);
        } else {
            result = audioManager.requestAudioFocus(focusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void updateModeBadge() {
        try {
            if (modeBadgeText != null) {
                if (isRecitalMode) {
                    modeBadgeIcon.setText("⭐");
                    modeBadgeText.setText(R.string.alphabet_mode_recital);
                    modeBadgeDescription.setText(R.string.alphabet_mode_recital_desc);
                    modeBadgeCard.setCardBackgroundColor(getColor(R.color.colorAccent));
                    modeBadgeText.setTextColor(getColor(android.R.color.white));
                    modeBadgeDescription.setTextColor(getColor(android.R.color.white));
                    modeBadgeDescription.setAlpha(0.8f);
                } else {
                    modeBadgeIcon.setText("🎤");
                    modeBadgeText.setText(R.string.alphabet_mode_practice);
                    modeBadgeDescription.setText(R.string.alphabet_mode_practice_desc);
                    modeBadgeCard.setCardBackgroundColor(getColor(R.color.colorPrimary));
                    modeBadgeText.setTextColor(getColor(android.R.color.white));
                    modeBadgeDescription.setTextColor(getColor(android.R.color.white));
                    modeBadgeDescription.setAlpha(0.8f);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("AlphabetActivity", "Error updating mode badge", e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
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
        if (offlineTts != null) {
            offlineTts.release();
        }
    }
}
