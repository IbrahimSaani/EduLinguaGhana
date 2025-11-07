package com.edulinguaghana;  // <-- your package

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class NumbersActivity extends AppCompatActivity {

    private TextView tvLanguageTitleNum, tvNumber;
    private Button btnPrevNumber, btnNextNumber, btnBackNumber, btnSpeakNumber;

    private String languageCode;
    private String languageName;
    private String mode;           // "recital" or "practice"
    private boolean isRecitalMode;

    private int currentNumber = 1;  // 1..100
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numbers);

        tvLanguageTitleNum = findViewById(R.id.tvLanguageTitleNum);
        tvNumber = findViewById(R.id.tvNumber);
        btnPrevNumber = findViewById(R.id.btnPrevNumber);
        btnNextNumber = findViewById(R.id.btnNextNumber);
        btnBackNumber = findViewById(R.id.btnBackNumber);
        btnSpeakNumber = findViewById(R.id.btnSpeakNumber);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");
        mode = getIntent().getStringExtra("MODE");

        if (languageName == null) languageName = "Unknown";
        if (mode == null) mode = "practice";

        isRecitalMode = mode.equals("recital");

        tvLanguageTitleNum.setText("Language: " + languageName);
        btnSpeakNumber.setText(isRecitalMode ? "Repeat" : "Speak");

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(getLocaleForLanguage(languageCode));
                if (isRecitalMode) {
                    updateNumber();
                    speakCurrentNumber();
                } else {
                    updateNumber();
                }
            } else {
                Toast.makeText(this, "TTS init failed", Toast.LENGTH_SHORT).show();
                updateNumber();
            }
        });

        btnNextNumber.setOnClickListener(v -> {
            currentNumber++;
            if (currentNumber > 100) currentNumber = 1;
            updateNumber();
            if (isRecitalMode) speakCurrentNumber();
        });

        btnPrevNumber.setOnClickListener(v -> {
            currentNumber--;
            if (currentNumber < 1) currentNumber = 100;
            updateNumber();
            if (isRecitalMode) speakCurrentNumber();
        });

        btnBackNumber.setOnClickListener(v -> finish());

        btnSpeakNumber.setOnClickListener(v -> speakCurrentNumber());
    }

    private void updateNumber() {
        tvNumber.setText(String.valueOf(currentNumber));
    }

    private void speakCurrentNumber() {
        if (tts != null) {
            String text = String.valueOf(currentNumber);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NUMBER_ID");
        }
    }

    private Locale getLocaleForLanguage(String code) {
        if (code == null) return Locale.ENGLISH;
        switch (code) {
            case "fr":
                return Locale.FRENCH;
            default:
                return Locale.ENGLISH;
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
