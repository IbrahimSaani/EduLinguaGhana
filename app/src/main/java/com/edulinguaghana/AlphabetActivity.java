package com.edulinguaghana;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.edulinguaghana.tracking.ProgressTracker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.SeekBar;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.edulinguaghana.tts.OfflineGhanaLPTtsService;

public class AlphabetActivity extends AppCompatActivity {

    private TextView tvLanguageTitle, tvLetter, tvLetterWord;
    private TextView tvProgressCounter;
    private MaterialButton btnPrev, btnNext;
    private FloatingActionButton btnBack;
    private LinearProgressIndicator progressBar;
    private MaterialCardView letterCard, languageCard;
    private ImageView decorativeShape1, decorativeShape2, decorativeShape3, decorativeShape4;
    private ImageView progressIcon;
    private TextView modeIcon;  // Changed to TextView for emoji
    private TextView celebrationEmoji;  // For celebration animations
    private SeekBar seekBarNavigation;  // For smooth navigation
    private MaterialButton btnShowGrid;  // For quick access grid

    private String languageCode;
    private String languageName;

    private String[] letters;
    private String[] words;
    private int currentIndex = 0;


    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private final AudioManager.OnAudioFocusChangeListener focusChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
                if (tts != null) tts.stop();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.2f, 0.2f);
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    // Optionally resume if it was paused by transient loss
                }
                break;
        }
    };

    // Progress tracking
    private ProgressTracker progressTracker;
    private long startTime;
    private boolean lessonCompletedLogSent = false;

    // Offline TTS for native Ghanaian languages (loads from res/raw)
    private OfflineGhanaLPTtsService offlineTts;
    private boolean isGhanaLpPlaying = false;

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

        // Initialize other views
        progressBar = findViewById(R.id.progressBar);
        letterCard = findViewById(R.id.letterCard);
        languageCard = findViewById(R.id.languageCard);

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

        // Initialize vibrator for haptic feedback
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        progressTracker = new ProgressTracker(this);
        startTime = System.currentTimeMillis();

        // Record practice for streak in background to avoid blocking main thread
        new Thread(() -> {
            try {
                new StreakManager(getApplicationContext()).recordPractice();
                PracticeTracker.recordPractice(getApplicationContext());
            } catch (Exception ignored) {}
        }).start();

        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");

        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt("CURRENT_INDEX", 0);
            lessonCompletedLogSent = savedInstanceState.getBoolean("LESSON_LOGGED", false);
        }

        if (languageName == null) languageName = "Unknown";

        tvLanguageTitle.setText(getString(R.string.language_prefix) + " " + languageName);
        
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
                    speakCurrentLetter();
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
                speakCurrentLetter();
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
                speakCurrentLetter();
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
                    speakCurrentLetter();
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
    }

    private void updateLetter() {
        try {
            // Update text with letter
            String letter = letters[currentIndex];
            tvLetter.setText(letter);
            tvLetterWord.setText(words[currentIndex]);

            // Update progress bar and counter
            progressBar.setProgress(currentIndex + 1);
            updateProgressCounter();

            // Auto-voice accessibility feature
            if (com.edulinguaghana.AppPreferences.isAutoVoiceEnabled(this)) {
                speakCurrentLetter();
            }
        } catch (Exception ignored) {
        }
    }

    private void updateLetterWithAnimation() {
        if (com.edulinguaghana.AppPreferences.isReducedMotionEnabled(this)) {
            updateLetter();
            return;
        }
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

        // Auto-voice accessibility feature
        if (com.edulinguaghana.AppPreferences.isAutoVoiceEnabled(this)) {
            speakCurrentLetter();
        }

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
        if (com.edulinguaghana.AppPreferences.isReducedMotionEnabled(this) || 
            com.edulinguaghana.AppPreferences.isFocusModeEnabled(this)) return;
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

    private void speakWithGhanaLP(String text) {
        try {
            if (isGhanaLpPlaying) {
                offlineTts.stop();
            }

            // Normalize language code for audio file lookup
            final String apiLangCode = com.edulinguaghana.utils.LanguageConversionUtils.normalizeLanguageCode(languageCode);

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
                                }

                                @Override
                                public void onError(String letterError) {
                                    // Final fallback to Android TTS
                                    isGhanaLpPlaying = false;
                                    runOnUiThread(() -> {
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
                                if (tts != null) {
                                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LETTER_ID");
                                }
                            } catch (Exception ttsError) {
                                android.util.Log.e("AlphabetActivity", "Final TTS fallback also failed", ttsError);
                            }
                        }
                    }
                }
            );
        } catch (Exception e) {
            android.util.Log.e("AlphabetActivity", "Error in speakWithGhanaLP", e);
            isGhanaLpPlaying = false;
            try {
                if (tts != null && tts.isSpeaking()) {
                    tts.stop();
                }
                if (tts != null) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LETTER_ID");
                }
            } catch (Exception ttsError) {
                android.util.Log.e("AlphabetActivity", "TTS ultimate fallback failed", ttsError);
            }
        }
    }

    private int getLetterAudioResId(String lang, String letter) {
        if (lang == null || letter == null) return 0;

        String langLower = lang.toLowerCase(Locale.ROOT);
        String letterLower = letter.toLowerCase(Locale.ROOT);

        // Normalize language prefix for file lookup
        String filePrefix;
        if ("ee".equals(langLower) || "ewe".equals(langLower)) {
            filePrefix = "ewe";
        } else if ("ak".equals(langLower) || "twi".equals(langLower) || "akan".equals(langLower)) {
            filePrefix = "twi";
        } else if ("gaa".equals(langLower) || "ga".equals(langLower)) {
            filePrefix = "gaa";
        } else {
            filePrefix = langLower;
        }

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

        String fileName = filePrefix + "_letter_" + sanitizedLetter;
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
        
        // Use a background thread for MediaPlayer creation to keep UI responsive
        new Thread(() -> {
            try {
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    } catch (Exception ignored) {}
                }
                
                MediaPlayer mp = MediaPlayer.create(AlphabetActivity.this, resId);
                if (mp != null) {
                    mediaPlayer = mp;
                    mp.setOnCompletionListener(player -> {
                        try {
                            player.release();
                        } catch (Exception ignored) {}
                        if (mediaPlayer == player) {
                            mediaPlayer = null;
                        }
                    });
                    mp.start();
                }
            } catch (Exception e) {
                android.util.Log.e("AlphabetActivity", "Error playing audio resource", e);
            }
        }).start();
    }

    private void showMascotMessage(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        if (letterCard != null) snackbar.setAnchorView(letterCard);
        snackbar.setBackgroundTint(androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary));
        snackbar.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.white));
        
        // Add a "Help" emoji to make it feel like Owlbert is talking
        message = "🦉 " + message;
        
        // Find the textview in snackbar to update the text with emoji
        View snackbarView = snackbar.getView();
        TextView tv = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (tv != null) tv.setText(message);
        
        snackbar.show();
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
                    card.setStrokeColor(ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(AlphabetActivity.this, R.color.colorAccent)));
                    textView.setTextColor(androidx.core.content.ContextCompat.getColor(AlphabetActivity.this, R.color.colorAccent));
                } else {
                    card.setStrokeWidth(0);
                    textView.setTextColor(androidx.core.content.ContextCompat.getColor(AlphabetActivity.this, R.color.textColorPrimary));
                }
                
                card.setOnClickListener(v -> {
                    currentIndex = holder.getAdapterPosition();
                    if (seekBarNavigation != null) seekBarNavigation.setProgress(currentIndex);
                    updateLetterWithAnimation();
                    speakCurrentLetter();
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
            
            showMascotMessage(getString(R.string.alphabet_lesson_complete, languageName));
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
