package com.edulinguaghana;  // <-- your package

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class AlphabetActivity extends AppCompatActivity {

    private TextView tvLanguageTitle, tvLetter;
    private Button btnPrev, btnNext, btnBack, btnSpeak;

    private String languageCode;
    private String languageName;
    private String mode;           // "recital" or "practice"
    private boolean isRecitalMode;

    private String[] letters;
    private int currentIndex = 0;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alphabet);

        tvLanguageTitle = findViewById(R.id.tvLanguageTitle);
        tvLetter = findViewById(R.id.tvLetter);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnSpeak = findViewById(R.id.btnSpeak);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");
        mode = getIntent().getStringExtra("MODE");

        if (languageName == null) languageName = "Unknown";
        if (mode == null) mode = "practice";

        isRecitalMode = mode.equals("recital");

        tvLanguageTitle.setText("Language: " + languageName);
        btnSpeak.setText(isRecitalMode ? "Repeat" : "Speak");

        // Simple A-Z alphabet
        letters = new String[]{
                "A","B","C","D","E","F","G","H","I","J","K","L","M",
                "N","O","P","Q","R","S","T","U","V","W","X","Y","Z"
        };

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(getLocaleForLanguage(languageCode));
                if (isRecitalMode) {
                    // Auto speak first letter
                    updateLetter();
                    speakCurrentLetter();
                } else {
                    updateLetter();
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

        btnSpeak.setOnClickListener(v -> speakCurrentLetter());
    }

    private void updateLetter() {
        tvLetter.setText(letters[currentIndex]);
    }

    private void speakCurrentLetter() {
        if (tts != null) {
            String text = letters[currentIndex];
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "LETTER_ID");
        }
    }

    private Locale getLocaleForLanguage(String code) {
        if (code == null) return Locale.ENGLISH;
        switch (code) {
            case "fr":
                return Locale.FRENCH;
            default:
                return Locale.ENGLISH; // fallback for Twi/Ewe/Ga
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
