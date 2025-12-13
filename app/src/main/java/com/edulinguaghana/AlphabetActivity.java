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

public class AlphabetActivity extends AppCompatActivity {

    private TextView tvLanguageTitle, tvLetter, tvLetterWord, tvLetterShadow;
    private MaterialButton btnPrev, btnNext, btnSpeak;
    private FloatingActionButton btnBack;
    private LinearProgressIndicator progressBar;
    private MaterialCardView letterCard, languageCard;
    private ImageView decorativeShape1, decorativeShape2, decorativeShape3, decorativeShape4;
    private ImageView topAccent, bottomAccent, progressIcon, languageIcon, modeIcon;

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
        topAccent = findViewById(R.id.topAccent);
        bottomAccent = findViewById(R.id.bottomAccent);
        progressIcon = findViewById(R.id.progressIcon);
        languageIcon = findViewById(R.id.languageIcon);
        modeIcon = findViewById(R.id.modeIcon);


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

        // Start background animations after views are ready
        try {
            startBackgroundAnimations();
        } catch (Exception e) {
            // Animations failed, but continue without them
            e.printStackTrace();
        }

        tts = new TextToSpeech(this, status -> {
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
        });

        btnNext.setOnClickListener(v -> {
            try {
                animateButtonPress(btnNext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentIndex++;
            if (currentIndex >= letters.length) currentIndex = 0;
            updateLetterWithAnimation();
            if (isRecitalMode) speakCurrentLetter();
        });

        btnPrev.setOnClickListener(v -> {
            try {
                animateButtonPress(btnPrev);
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentIndex--;
            if (currentIndex < 0) currentIndex = letters.length - 1;
            updateLetterWithAnimation();
            if (isRecitalMode) speakCurrentLetter();
        });

        btnBack.setOnClickListener(v -> {
            try {
                animateButtonPress(btnBack);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        });

        btnSpeak.setOnClickListener(v -> {
            try {
                animateButtonPress(btnSpeak);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isRecitalMode) {
                speakCurrentLetter();
            } else {
                startPracticePronunciation();
            }
        });
    }

    private void updateLetter() {
        // Update text with letter and shadow
        String letter = letters[currentIndex];
        tvLetter.setText(letter);
        if (tvLetterShadow != null) {
            tvLetterShadow.setText(letter);
        }
        tvLetterWord.setText(words[currentIndex]);

        // Update progress
        progressBar.setProgress(currentIndex + 1);
    }

    private void updateLetterWithAnimation() {
        // Animate card transition
        try {
            animateCardTransition();
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

        // Update progress
        progressBar.setProgress(currentIndex + 1);

        try {
            animateProgressIcon();
        } catch (Exception e) {
            e.printStackTrace();
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

            // Animate accents
            if (topAccent != null && bottomAccent != null) {
                Animation twinkle = AnimationUtils.loadAnimation(this, R.anim.star_twinkle);
                topAccent.startAnimation(twinkle);
                bottomAccent.startAnimation(twinkle);
            }
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

    private void animateCardTransition() {
        try {
            if (letterCard != null) {
                Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

                letterCard.startAnimation(slideOut);
                slideOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        letterCard.startAnimation(slideIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
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
        String letter = letters[currentIndex];
        int resId = getLetterAudioResId(languageCode, letter);
        if (resId != 0) {
            playAudioResource(resId);
        } else if (tts != null) {
            tts.speak(letter, TextToSpeech.QUEUE_FLUSH, null, "LETTER_ID");
        }
        animateLetter();
    }

    private int getLetterAudioResId(String lang, String letter) {
        if (lang == null || letter == null) return 0;
        // Sanitize special characters for resource names
        String sanitizedLetter = letter.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
        if (sanitizedLetter.isEmpty()) return 0;
        String fileName = lang.toLowerCase(Locale.ROOT) + "_" + sanitizedLetter;
        return getResources().getIdentifier(fileName, "raw", getPackageName());
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
        // Only support grading for English & French
        if (!("en".equals(languageCode) || "fr".equals(languageCode))) {
            Toast.makeText(this, "Pronunciation grading is not yet available for " + languageName, Toast.LENGTH_LONG).show();
            speakCurrentLetter(); // still let them hear audio
            return;
        }

        speakCurrentLetter();

        if (!isRecordAudioPermissionGranted()) {
            requestRecordAudioPermission();
        } else {
            promptSpeechInput();
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
                Toast.makeText(this, "Great! Pronunciation matched (" + recognized + ")", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "It heard: \"" + recognized + "\". Expected: " + expectedLetter, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Could not detect any sound. Try again.", Toast.LENGTH_SHORT).show();
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
    }
}
