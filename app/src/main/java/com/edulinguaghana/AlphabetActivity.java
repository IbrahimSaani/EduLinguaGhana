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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Locale;

public class AlphabetActivity extends AppCompatActivity {

    private TextView tvLanguageTitle, tvLetter, tvLetterWord;
    private ImageButton btnPrev, btnNext;
    private FloatingActionButton btnBack;
    private Button btnSpeak;
    private LinearProgressIndicator progressBar;

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

        tvLanguageTitle = findViewById(R.id.tvLanguageTitle);
        tvLetter = findViewById(R.id.tvLetter);
        tvLetterWord = findViewById(R.id.tvLetterWord);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnSpeak = findViewById(R.id.btnSpeak);
        progressBar = findViewById(R.id.progressBar);

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
            currentIndex++;
            if (currentIndex >= letters.length) currentIndex = 0;
            updateLetter();
            if (isRecitalMode) speakCurrentLetter();
        });

        btnPrev.setOnClickListener(v -> {
            currentIndex--;
            if (currentIndex < 0) currentIndex = letters.length - 1;
            updateLetter();
            if (isRecitalMode) speakCurrentLetter();
        });

        btnBack.setOnClickListener(v -> finish());

        btnSpeak.setOnClickListener(v -> {
            if (isRecitalMode) {
                speakCurrentLetter();
            } else {
                startPracticePronunciation();
            }
        });
    }

    private void updateLetter() {
        tvLetter.setText(letters[currentIndex]);
        tvLetterWord.setText(words[currentIndex]);
        progressBar.setProgress(currentIndex + 1);
    }

    private void animateLetter() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.letter_bounce);
        tvLetter.startAnimation(anim);
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
