package com.edulinguaghana;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Locale;

import com.edulinguaghana.tts.OfflineGhanaLPTtsService;

public class AlphabetActivity extends AppCompatActivity {

    private TextView tvLanguageTitle, tvLetter, tvLetterWord, tvLetterShadow;
    private TextView tvProgressCounter;
    private MaterialButton btnPrev, btnNext, btnSpeak;
    private FloatingActionButton btnBack;
    private LinearProgressIndicator progressBar;
    private MaterialCardView letterCard, languageCard;
    private ImageView decorativeShape1, decorativeShape2, decorativeShape3, decorativeShape4;
    private ImageView progressIcon;
    private TextView modeIcon;  // Changed to TextView for emoji
    private TextView celebrationEmoji;  // For celebration animations

    private Vibrator vibrator;  // For haptic feedback

    private String languageCode;
    private String languageName;
    private String mode;           // "recital" or "practice"
    private boolean isRecitalMode;

    private String[] letters;
    private String[] words;
    private int currentIndex = 0;

    // --- ALPHABET & WORD DATA ---
    private final String[] lettersEnFr = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    private final String[] wordsEn = {"Apple", "Ball", "Cat", "Dog", "Elephant", "Fish", "Goat", "Hat", "Ice Cream", "Jug", "Kite", "Lion", "Monkey", "Nose", "Orange", "Pen", "Queen", "Rainbow", "Sun", "Tiger", "Umbrella", "Violin", "Watch", "Xylophone", "Yoyo", "Zebra"};
    private final String[] wordsFr = {"Avion", "Bateau", "Chien", "Dauphin", "Éléphant", "Fleur", "Girafe", "Hibou", "Île", "Jardin", "Kangourou", "Lune", "Maison", "Nuage", "Oiseau", "Poisson", "Quatre", "Robot", "Soleil", "Train", "Uniforme", "Vache", "Wagon", "Xylophone", "Yaourt", "Zèbre"};
    private final String[] lettersAk = {"A", "B", "D", "E", "Ɛ", "F", "G", "H", "I", "K", "L", "M", "N", "O", "Ɔ", "P", "R", "S", "T", "U", "W", "Y"};
    private final String[] wordsAk = {"Akokɔ", "Borɔdeɛ", "Duku", "Etuo", "Ɛmo", "Forosie", "Gari", "Hweaa", "Isu", "Kube", "Lɔre", "Maame", "Nsuo", "Odwan", "Ɔkraman", "Pono", "Prako", "Sika", "Tɛkyerɛma", "Uniwesiti", "Wura", "Yareɛ"};
    private final String[] lettersEe = {"A", "B", "D", "Ɖ", "E", "Ɛ", "F", "Ƒ", "G", "Ɣ", "H", "X", "I", "K", "L", "M", "N", "Ŋ", "O", "Ɔ", "P", "R", "S", "T", "U", "V", "Ʋ", "W", "Y", "Z"};
    private final String[] wordsEe = {"Ati", "Baka", "Dadi", "Ɖevi", "Eku", "Ɛfu", "Fia", "Ƒo", "Gbe", "Ɣe", "Ha", "Xɔ", "Iŋk", "Kafu", "Lá", "Me", "Nɔ", "Ŋkɔ", "Oyi", "Ɔli", "Papa", "Rɛdio", "Sɔ", "Tɔ", "Unilɔ", "Vɔ", "Ʋu", "Wó", "Yevú", "Zã"};
    private final String[] lettersGaa = {"A", "B", "D", "E", "Ɛ", "F", "G", "H", "I", "K", "L", "M", "N", "Ŋ", "O", "Ɔ", "P", "S", "T", "U", "V", "W", "Y", "Z"};
    private final String[] wordsGaa = {"Akekā", "Blɔfo", "Dade", "Enɔ", "Ɛlɛ", "Fio", "Gbekɛ", "Hejɔ", "Iŋk", "Klala", "Lala", "Maŋ", "Nuu", "Ŋmã", "Okpɔtɔ", "Ɔɔso", "Papa", "Sohaa", "Tee", "Wala", "Vinɔ", "Wɔ", "Yoomo", "Zigidi"};

    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;

    // Offline TTS for native Ghanaian languages (loads from res/raw)
    private OfflineGhanaLPTtsService offlineTts;
    private boolean isGhanaLpPlaying = false;

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int REQ_CODE_RECORD_AUDIO = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alphabet);

        // Initialize text views
        tvLanguageTitle = findViewById(R.id.tvLanguageTitle);
        tvLetter = findViewById(R.id.tvLetter);
        tvLetterWord = findViewById(R.id.tvLetterWord);
        tvLetterShadow = findViewById(R.id.tvLetterShadow);
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

        // Initialize decorative elements
        decorativeShape1 = findViewById(R.id.decorativeShape1);
        decorativeShape2 = findViewById(R.id.decorativeShape2);
        decorativeShape3 = findViewById(R.id.decorativeShape3);
        decorativeShape4 = findViewById(R.id.decorativeShape4);
        progressIcon = findViewById(R.id.progressIcon);
        modeIcon = findViewById(R.id.modeIcon);

        // Initialize vibrator for haptic feedback
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");
        mode = getIntent().getStringExtra("MODE");

        if (languageName == null) languageName = "Unknown";
        if (mode == null) mode = "practice";

        isRecitalMode = mode.equals("recital");

        tvLanguageTitle.setText("Language: " + languageName);
        btnSpeak.setText(isRecitalMode ? "Repeat" : "Practice");

        // --- DYNAMICALLY SET ALPHABET & WORDS ---
        switch (languageCode) {
            case "ak":
                letters = lettersAk;
                words = wordsAk;
                break;
            case "ee":
                letters = lettersEe;
                words = wordsEe;
                break;
            case "gaa":
                letters = lettersGaa;
                words = wordsGaa;
                break;
            case "fr":
                letters = lettersEnFr;
                words = wordsFr;
                break;
            default: // "en" and any other case
                letters = lettersEnFr;
                words = wordsEn;
                break;
        }

        progressBar.setMax(letters.length);

        // Initialize Offline TTS for native languages (loads from res/raw)
        offlineTts = new OfflineGhanaLPTtsService(this);

        // Start background animations after views are ready
        try {
            startBackgroundAnimations();
        } catch (Exception e) {
            // Animations failed, but continue without them
            e.printStackTrace();
        }

        tts = new TextToSpeech(this, status -> {
            try {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(getLocaleForLanguage(languageCode));
                    updateLetter();
                    if (isRecitalMode) {
                        speakCurrentLetter();
                    }
                } else {
                    Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show();
                    updateLetter();
                }
            } catch (Exception e) {
                android.util.Log.e("AlphabetActivity", "Error during TTS initialization or recital playback", e);
                try {
                    updateLetter();
                } catch (Exception ignored) {
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(30);  // Light haptic
                animateButtonPress(btnNext);
            } catch (Exception e) {
                android.util.Log.w("AlphabetActivity", "Haptic feedback failed", e);
            }
            try {
                currentIndex++;
                if (currentIndex >= letters.length) currentIndex = 0;
                updateLetterWithAnimation();
                if (isRecitalMode) speakCurrentLetter();
            } catch (Exception e) {
                android.util.Log.e("AlphabetActivity", "Error advancing letter", e);
            }
        });

        btnPrev.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(30);  // Light haptic
                animateButtonPress(btnPrev);
            } catch (Exception e) {
                android.util.Log.w("AlphabetActivity", "Haptic feedback failed", e);
            }
            try {
                currentIndex--;
                if (currentIndex < 0) currentIndex = letters.length - 1;
                updateLetterWithAnimation();
                if (isRecitalMode) speakCurrentLetter();
            } catch (Exception e) {
                android.util.Log.e("AlphabetActivity", "Error going back letter", e);
            }
        });

        btnBack.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(50);  // Medium haptic
                animateButtonPress(btnBack);
            } catch (Exception e) {
                android.util.Log.w("AlphabetActivity", "Haptic feedback failed", e);
            }
            finish();
        });

        btnSpeak.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(50);  // Medium haptic
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
            } catch (Exception e) {
                android.util.Log.e("AlphabetActivity", "Error speaking letter", e);
            }
        });

        // Make letter card tappable to speak
        letterCard.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(40);  // Light-medium haptic
                speakCurrentLetter();
                celebrateAction();  // Show celebration
            } catch (Exception e) {
                android.util.Log.e("AlphabetActivity", "Error in letter card click", e);
            }
        });
    }

    private void updateLetter() {
        try {
            // Update text with letter and shadow
            String letter = letters[currentIndex];
            tvLetter.setText(letter);
            if (tvLetterShadow != null) {
                tvLetterShadow.setText(letter);
            }
            tvLetterWord.setText(words[currentIndex]);

            // Update progress bar and counter
            progressBar.setProgress(currentIndex + 1);
            updateProgressCounter();
        } catch (Exception e) {
            android.util.Log.e("AlphabetActivity", "Error updating letter display", e);
            // Ensure display is at least partially updated
            try {
                if (currentIndex < letters.length) {
                    tvLetter.setText(letters[currentIndex]);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void updateLetterWithAnimation() {
        // Animate letter change with smooth fade and scale
        try {
            animateLetterChange();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update text with letter and shadow
        String letter = letters[currentIndex];
        tvLetter.setText(letter);
        if (tvLetterShadow != null) {
            tvLetterShadow.setText(letter);
        }
        tvLetterWord.setText(words[currentIndex]);

        // Update progress bar and counter
        progressBar.setProgress(currentIndex + 1);
        updateProgressCounter();

        try {
            animateProgressIcon();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProgressCounter() {
        if (tvProgressCounter != null) {
            String progressText = "Letter " + (currentIndex + 1) + " of " + letters.length;
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
            if (tvLetterShadow != null) {
                tvLetterShadow.startAnimation(letterAnim);
            }

            // Animate word text
            Animation wordAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            tvLetterWord.startAnimation(wordAnim);
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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

                // Also animate shadow if available
                if (tvLetterShadow != null) {
                    tvLetterShadow.animate()
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .alpha(0.1f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            tvLetterShadow.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .alpha(0.15f)
                                .setDuration(150)
                                .start();
                        })
                        .start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateProgressIcon() {
        try {
            if (progressIcon != null) {
                Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_pop);
                progressIcon.startAnimation(bounce);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void speakCurrentLetter() {
        try {
            String letter = letters[currentIndex];

            // Try to load audio from recorded files first
            int resId = getLetterAudioResId(languageCode, letter);
            if (resId != 0) {
                playAudioResource(resId);
            } else if (isGhanaianLanguage(languageCode)) {
                // Fallback to GhanaLP TTS if no recorded audio exists
                speakWithGhanaLP(letter);
            } else if (tts != null) {
                // Use language-specific pronunciation for letters
                String textToSpeak = getFrenchLetterPronunciation(letter);
                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "LETTER_ID");
            }

            animateLetter();
        } catch (Exception e) {
            android.util.Log.e("AlphabetActivity", "Error in speakCurrentLetter", e);
            // Silently fail - don't crash the app
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
            String apiLangCode = normalizeLanguageCode(languageCode);

            // Disable speak button during playback
            if (btnSpeak != null) {
                btnSpeak.setEnabled(false);
            }

            // IMPROVEMENT: Speak the example WORD instead of the letter
            // This works better because word pronunciations are correct
            // The word demonstrates the letter sound in context
            String wordToSpeak = words[currentIndex];

            offlineTts.speakWord(
                wordToSpeak,
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

    private String normalizeLanguageCode(String code) {
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
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
            mediaPlayer = null;
        });
        mediaPlayer.start();
    }

    private void startPracticePronunciation() {
        speakCurrentLetter();

        // For English & French: Use speech recognition
        if ("en".equals(languageCode) || "fr".equals(languageCode)) {
            if (!isRecordAudioPermissionGranted()) {
                requestRecordAudioPermission();
            } else {
                promptSpeechInput();
            }
        } else {
            // For Ghanaian languages: Show friendly message and provide audio example
            Toast.makeText(this,
                "🎤 Try to pronounce: " + letters[currentIndex] +
                "\n\n📝 Listen carefully to the audio above and repeat it!\n\n" +
                "Tap the letter again to hear it.",
                Toast.LENGTH_LONG).show();

            // Show helpful tips for Ghanaian languages
            showPronunciationTips();
        }
    }

    private void showPronunciationTips() {
        String tips = "";
        String letter = letters[currentIndex];

        switch (languageCode) {
            case "ak": // Twi
                tips = getTwiPronunciationTip(letter);
                break;
            case "ee": // Ewe
                tips = getEwePronunciationTip(letter);
                break;
            case "gaa": // Ga
                tips = getGaPronunciationTip(letter);
                break;
        }

        if (!tips.isEmpty()) {
            Toast.makeText(this, "💡 Tip: " + tips, Toast.LENGTH_LONG).show();
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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please repeat the letter");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported on this device.", Toast.LENGTH_LONG).show();
        }
    }

    private String getSpeechLocaleCode() {
        if ("fr".equals(languageCode)) return "fr-FR";
        // Add other language speech codes here if supported
        return "en-US";
    }

    private Locale getLocaleForLanguage(String code) {
        if (code == null) return Locale.ENGLISH;
        switch (code) {
            case "fr": return Locale.FRENCH;
            case "ak": return new Locale("ak");
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
                Toast.makeText(this, "Could not hear you. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void evaluatePronunciation(String recognized) {
        String expectedLetter = letters[currentIndex];

        if (recognized.length() > 0) {
            String simplifiedRecognized = recognized.toUpperCase().substring(0, 1);

            if (simplifiedRecognized.equals(expectedLetter)) {
                // Perfect match!
                celebrateAction();
                Toast.makeText(this,
                    "🎉 Excellent! Perfect pronunciation of '" + recognized + "'" +
                    "\n✅ You got it right!",
                    Toast.LENGTH_LONG).show();
            } else if (recognized.equalsIgnoreCase(expectedLetter)) {
                // Case-insensitive match (still correct)
                celebrateAction();
                Toast.makeText(this,
                    "✅ Great! '" + recognized + "' matches '" + expectedLetter + "'" +
                    "\n\n🎯 Pronunciation is correct!",
                    Toast.LENGTH_LONG).show();
            } else {
                // Didn't match
                Toast.makeText(this,
                    "🎤 I heard: \"" + recognized + "\"" +
                    "\n📝 Expected: \"" + expectedLetter + "\"" +
                    "\n\n💡 Listen to the audio again and try once more!",
                    Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,
                "🔇 Could not detect any sound.\n\n🎤 Please try again and speak clearly!",
                Toast.LENGTH_SHORT).show();
        }
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
                Toast.makeText(this, "Microphone permission is needed for pronunciation practice.", Toast.LENGTH_LONG).show();
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
