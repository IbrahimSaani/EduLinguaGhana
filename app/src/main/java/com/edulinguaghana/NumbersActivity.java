package com.edulinguaghana;  // <-- your package

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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
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
    private MediaPlayer mediaPlayer;

    private static final int REQ_CODE_SPEECH_INPUT = 300;
    private static final int REQ_CODE_RECORD_AUDIO = 400;

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
        btnSpeakNumber.setText(isRecitalMode ? "Repeat" : "Practice");

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(getLocaleForLanguage(languageCode));
                updateNumber();
                if (isRecitalMode) {
                    speakCurrentNumber();
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

        btnSpeakNumber.setOnClickListener(v -> {
            if (isRecitalMode) {
                speakCurrentNumber();
            } else {
                startPracticePronunciation();
            }
        });
    }

    private void updateNumber() {
        tvNumber.setText(String.valueOf(currentNumber));
    }

    private void animateNumber() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.number_bounce);
        tvNumber.startAnimation(anim);
    }

    private void speakCurrentNumber() {
        String numberText = String.valueOf(currentNumber);
        int resId = getNumberAudioResId(languageCode, currentNumber);
        if (resId != 0) {
            playAudioResource(resId);
        } else if (tts != null) {
            tts.speak(numberText, TextToSpeech.QUEUE_FLUSH, null, "NUMBER_ID");
        }
        animateNumber();
    }

    private int getNumberAudioResId(String lang, int num) {
        if (lang == null) return 0;
        String fileName = lang.toLowerCase(Locale.ROOT) + "_" + num;
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
        if (!("en".equals(languageCode) || "fr".equals(languageCode))) {
            Toast.makeText(this, "Pronunciation grading is only available for English and French for now.", Toast.LENGTH_LONG).show();
            speakCurrentNumber();
            return;
        }

        speakCurrentNumber();

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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please repeat the number");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported on this device.", Toast.LENGTH_LONG).show();
        }
    }

    private String getSpeechLocaleCode() {
        if ("fr".equals(languageCode)) {
            return "fr-FR";
        }
        return "en-US";
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
        int expected = currentNumber;

        try {
            int recognizedNumber = Integer.parseInt(recognized);
            if (recognizedNumber == expected) {
                Toast.makeText(this, "Great! Pronunciation matched (" + recognized + ")", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "It heard: \"" + recognized + "\". Expected: " + expected, Toast.LENGTH_LONG).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "It heard: \"" + recognized + "\". Expected number: " + expected, Toast.LENGTH_LONG).show();
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
