package com.edulinguaghana;

import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edulinguaghana.tts.OfflineGhanaLPTtsService;
import com.edulinguaghana.utils.LanguageConversionUtils;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.Random;

public class HiddenShapesActivity extends AppCompatActivity {

    private ScratchRevealView scratchView;
    private TextView tvPrompt;
    private MaterialButton btnNext, btnBack;
    private String languageCode;
    private String currentCharacter;
    
    private TextToSpeech tts;
    private OfflineGhanaLPTtsService offlineTts;
    private boolean isTtsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden_shapes);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        if (languageCode == null) languageCode = "en";

        scratchView = findViewById(R.id.scratchView);
        tvPrompt = findViewById(R.id.tvPrompt);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> generateNewCharacter());

        initTts();
        generateNewCharacter();
    }

    private void initTts() {
        if (LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
            offlineTts = new OfflineGhanaLPTtsService(this);
        }
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(LanguageConversionUtils.getLocaleForLanguage(languageCode));
                isTtsReady = true;
            }
        });
    }

    private void generateNewCharacter() {
        btnNext.setVisibility(View.INVISIBLE);
        tvPrompt.setText(R.string.hidden_shapes_prompt);

        String[] alphabet = LanguageConversionUtils.getAlphabetForLanguage(languageCode);
        boolean useNumber = new Random().nextBoolean();
        
        if (useNumber) {
            currentCharacter = String.valueOf(new Random().nextInt(20) + 1);
        } else {
            currentCharacter = alphabet[new Random().nextInt(alphabet.length)];
        }

        scratchView.setHiddenText(currentCharacter, text -> {
            // Character revealed!
            runOnUiThread(() -> {
                speakCharacter(text);
                tvPrompt.setText(getString(R.string.hidden_shapes_found, text));
                btnNext.setVisibility(View.VISIBLE);
                btnNext.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce_pop));
            });
        });
    }

    private void speakCharacter(String text) {
        if (LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
            offlineTts.speak(text, languageCode, new OfflineGhanaLPTtsService.PlaybackCallback() {
                @Override
                public void onStart() {}

                @Override
                public void onComplete() {}

                @Override
                public void onError(String error) {
                    // Fallback to Android TTS if offline audio is missing
                    if (isTtsReady) {
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hidden_shape_fallback");
                    }
                }
            });
        } else if (isTtsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hidden_shape");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) tts.shutdown();
        if (offlineTts != null) offlineTts.stop();
    }
}
