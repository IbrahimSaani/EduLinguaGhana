package com.edulinguaghana.games;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edulinguaghana.R;
import com.edulinguaghana.utils.LanguageConversionUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class BeatMatcherActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvScore, tvOverlayTitle;
    private View overlayLayout, startOverlay;
    private nl.dionsegijn.konfetti.xml.KonfettiView konfettiView;

    private MaterialCardView[] cards = new MaterialCardView[3];
    private String[] cardValues = new String[3];
    private boolean[] isFlipped = new boolean[3];
    
    private int score = 0;
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private boolean isProcessing = false;
    
    private String languageCode;
    private String targetChar;
    
    private TextToSpeech tts;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    private MediaPlayer correctPlayer, wrongPlayer, flipPlayer;
    
    private Map<String, String> phonemes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beat_matcher);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        if (languageCode == null) languageCode = "en";

        initPhonemes();
        initViews();
        initSounds();
        tts = new TextToSpeech(this, this);
    }

    private void initPhonemes() {
        // Simple phonetic mapping for English
        phonemes.put("A", "ah");
        phonemes.put("B", "buh");
        phonemes.put("C", "cuh");
        phonemes.put("D", "duh");
        phonemes.put("E", "eh");
        phonemes.put("F", "fff");
        phonemes.put("G", "guh");
        phonemes.put("H", "huh");
        phonemes.put("I", "ih");
        phonemes.put("J", "juh");
        phonemes.put("K", "kuh");
        phonemes.put("L", "lll");
        phonemes.put("M", "mmm");
        phonemes.put("N", "nnn");
        phonemes.put("O", "oh");
        phonemes.put("P", "puh");
        phonemes.put("Q", "kwuh");
        phonemes.put("R", "rrr");
        phonemes.put("S", "sss");
        phonemes.put("T", "tuh");
        phonemes.put("U", "uh");
        phonemes.put("V", "vvv");
        phonemes.put("W", "wuh");
        phonemes.put("X", "ks");
        phonemes.put("Y", "yuh");
        phonemes.put("Z", "zzz");
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvOverlayTitle = findViewById(R.id.tvOverlayTitle);
        overlayLayout = findViewById(R.id.overlayLayout);
        startOverlay = findViewById(R.id.startOverlay);
        konfettiView = findViewById(R.id.konfettiView);

        for (int i = 0; i < 3; i++) {
            int cardId = getResources().getIdentifier("card" + i, "id", getPackageName());
            cards[i] = findViewById(cardId);
            final int index = i;
            cards[i].setOnClickListener(v -> onCardClicked(index));
        }

        findViewById(R.id.btnStart).setOnClickListener(v -> startGame());
        findViewById(R.id.btnPause).setOnClickListener(v -> togglePause());
        findViewById(R.id.btnResume).setOnClickListener(v -> togglePause());
        findViewById(R.id.btnRestart).setOnClickListener(v -> startGame());
        findViewById(R.id.btnQuit).setOnClickListener(v -> finish());
        findViewById(R.id.btnReplay).setOnClickListener(v -> playTargetSound());
    }

    private void initSounds() {
        correctPlayer = MediaPlayer.create(this, R.raw.correct);
        wrongPlayer = MediaPlayer.create(this, R.raw.wrong);
        flipPlayer = MediaPlayer.create(this, R.raw.bell);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
            tts.setSpeechRate(0.5f);
        }
    }

    private void startGame() {
        score = 0;
        isGameOver = false;
        isPaused = false;
        isProcessing = false;
        startOverlay.setVisibility(View.GONE);
        overlayLayout.setVisibility(View.GONE);
        updateUI();
        nextRound();
    }

    private void nextRound() {
        isProcessing = false;
        for (int i = 0; i < 3; i++) {
            resetCard(i);
        }
        
        generateRoundData();
        handler.postDelayed(this::playTargetSound, 500);
    }

    private void generateRoundData() {
        String[] pool = LanguageConversionUtils.getAlphabetForLanguage(languageCode);
        List<String> list = new ArrayList<>();
        Collections.addAll(list, pool);
        Collections.shuffle(list);

        for (int i = 0; i < 3; i++) {
            cardValues[i] = list.get(i);
            TextView tv = cards[i].findViewById(R.id.tvLetter);
            tv.setText(cardValues[i]);
        }
        targetChar = cardValues[random.nextInt(3)];
    }

    private void playTargetSound() {
        if (tts != null && targetChar != null) {
            String sound = phonemes.getOrDefault(targetChar.toUpperCase(), targetChar);
            tts.speak(sound, TextToSpeech.QUEUE_FLUSH, null, "phonetic");
        }
    }

    private void onCardClicked(int index) {
        if (isProcessing || isPaused || isGameOver || isFlipped[index]) return;
        flipCard(index);
    }

    private void flipCard(int index) {
        isProcessing = true;
        if (flipPlayer != null) flipPlayer.start();
        
        cards[index].animate()
                .scaleX(0f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showCardFront(index);
                        cards[index].animate()
                                .scaleX(1f)
                                .setDuration(150)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        checkResult(index);
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    private void showCardFront(int index) {
        isFlipped[index] = true;
        cards[index].findViewById(R.id.cardBack).setVisibility(View.GONE);
        cards[index].findViewById(R.id.cardFront).setVisibility(View.VISIBLE);
        
        if (!cardValues[index].equals(targetChar)) {
            cards[index].findViewById(R.id.tvLetter).setVisibility(View.GONE);
            cards[index].findViewById(R.id.imgFunnyFace).setVisibility(View.VISIBLE);
        } else {
            cards[index].findViewById(R.id.tvLetter).setVisibility(View.VISIBLE);
            cards[index].findViewById(R.id.imgFunnyFace).setVisibility(View.GONE);
        }
    }

    private void checkResult(int index) {
        if (cardValues[index].equals(targetChar)) {
            // Correct
            score++;
            updateUI();
            if (correctPlayer != null) correctPlayer.start();
            cards[index].setStrokeWidth(8);
            celebrate();
            handler.postDelayed(this::nextRound, 2000);
        } else {
            // Wrong
            if (wrongPlayer != null) wrongPlayer.start();
            wiggleCard(index);
            handler.postDelayed(() -> {
                flipBack(index);
                isProcessing = false;
            }, 1000);
        }
    }

    private void flipBack(int index) {
        cards[index].animate()
                .scaleX(0f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        resetCardInternal(index);
                        cards[index].animate()
                                .scaleX(1f)
                                .setDuration(150)
                                .setListener(null)
                                .start();
                    }
                })
                .start();
    }

    private void resetCard(int index) {
        isFlipped[index] = false;
        cards[index].setStrokeWidth(0);
        cards[index].setScaleX(1f);
        resetCardInternal(index);
    }

    private void resetCardInternal(int index) {
        isFlipped[index] = false;
        cards[index].findViewById(R.id.cardBack).setVisibility(View.VISIBLE);
        cards[index].findViewById(R.id.cardFront).setVisibility(View.INVISIBLE);
        cards[index].findViewById(R.id.tvLetter).setVisibility(View.VISIBLE);
        cards[index].findViewById(R.id.imgFunnyFace).setVisibility(View.GONE);
    }

    private void wiggleCard(int index) {
        ObjectAnimator.ofFloat(cards[index], "translationX", 0, 15, -15, 15, -15, 10, -10, 5, -5, 0)
                .setDuration(500)
                .start();
    }

    private void updateUI() {
        tvScore.setText("⭐ " + score);
    }

    private void togglePause() {
        isPaused = !isPaused;
        overlayLayout.setVisibility(isPaused ? View.VISIBLE : View.GONE);
        tvOverlayTitle.setText("Paused");
    }

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
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (correctPlayer != null) correctPlayer.release();
        if (wrongPlayer != null) wrongPlayer.release();
        if (flipPlayer != null) flipPlayer.release();
    }
}
