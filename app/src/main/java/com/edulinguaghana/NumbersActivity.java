package com.edulinguaghana;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;

import com.edulinguaghana.tracking.ProgressTracker;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.edulinguaghana.tts.OfflineGhanaLPTtsService;
import com.edulinguaghana.utils.LanguageConversionUtils;

import com.google.android.material.snackbar.Snackbar;
import android.widget.SeekBar;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NumbersActivity extends AppCompatActivity {

    private TextView tvLanguageTitleNum, tvNumber, tvNumberSpelling;
    private TextView tvProgressCounter;
    private TextView celebrationEmoji;
    private MaterialButton btnPrevNumber, btnNextNumber;
    private FloatingActionButton btnBackNumber;
    private LinearProgressIndicator progressBar;
    private MaterialCardView mainNumberCard;
    private SeekBar seekBarNavigation;  // For smooth navigation
    private MaterialButton btnShowPicker;  // For quick number picker

    private String languageCode;
    private String languageName;

    private int currentNumber = 1;  // 1..100
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CURRENT_NUMBER", currentNumber);
        outState.putBoolean("LESSON_LOGGED", lessonCompletedLogSent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numbers);

        // Set up toolbar back button
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvLanguageTitleNum = findViewById(R.id.tvLanguageTitleNum);
        tvNumber = findViewById(R.id.tvNumber);
        tvNumberSpelling = findViewById(R.id.tvNumberSpelling);
        tvProgressCounter = findViewById(R.id.tvProgressCounter);
        celebrationEmoji = findViewById(R.id.celebrationEmoji);
        btnPrevNumber = findViewById(R.id.btnPrevNumber);
        btnNextNumber = findViewById(R.id.btnNextNumber);
        btnBackNumber = findViewById(R.id.btnBackNumber);
        progressBar = findViewById(R.id.progressBar);
        mainNumberCard = findViewById(R.id.mainNumberCard);

        // Initialize navigation controls
        seekBarNavigation = findViewById(R.id.seekBarNavigation);
        btnShowPicker = findViewById(R.id.btnShowPicker);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        progressTracker = new ProgressTracker(this);
        startTime = System.currentTimeMillis();

        // Record practice for streak immediately when starting a learning session
        try {
            new StreakManager(this).recordPractice();
            PracticeTracker.recordPractice(this);
        } catch (Exception ignored) {}

        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");

        if (savedInstanceState != null) {
            currentNumber = savedInstanceState.getInt("CURRENT_NUMBER", 1);
            lessonCompletedLogSent = savedInstanceState.getBoolean("LESSON_LOGGED", false);
        }

        if (languageName == null) languageName = "Unknown";

        tvLanguageTitleNum.setText(getString(R.string.language_prefix) + " " + languageName);

        progressBar.setMax(100);
        if (seekBarNavigation != null) {
            seekBarNavigation.setMax(99);
            seekBarNavigation.setProgress(currentNumber - 1);
        }

        // Initialize Offline TTS for native languages (loads from res/raw)
        offlineTts = new OfflineGhanaLPTtsService(this);

        tts = new TextToSpeech(this, status -> {
            try {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(LanguageConversionUtils.getLocaleForLanguage(languageCode));
                    updateNumber();
                    speakCurrentNumber();
                } else {
                    updateNumber();
                }
            } catch (Exception ignored) {
            }
        });

        btnNextNumber.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Exception e) {
                android.util.Log.w("NumbersActivity", "Haptic feedback failed", e);
            }
            try {
                currentNumber++;
                if (currentNumber > 100) {
                    currentNumber = 1;
                    checkAndLogCompletion();
                }
                updateNumber();
                if (seekBarNavigation != null) seekBarNavigation.setProgress(currentNumber - 1);
                speakCurrentNumber();
            } catch (Exception ignored) {
            }
        });

        btnPrevNumber.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Exception e) {
                android.util.Log.w("NumbersActivity", "Haptic feedback failed", e);
            }
            try {
                currentNumber--;
                if (currentNumber < 1) currentNumber = 100;
                updateNumber();
                if (seekBarNavigation != null) seekBarNavigation.setProgress(currentNumber - 1);
                speakCurrentNumber();
            } catch (Exception ignored) {
            }
        });

        btnBackNumber.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            } catch (Exception e) {
                android.util.Log.w("NumbersActivity", "Haptic feedback failed", e);
            }
            finish();
        });

        // Make number card tappable to speak
        mainNumberCard.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                speakCurrentNumber();
                celebrateAction();  // Show celebration
            } catch (Exception ignored) {
            }
        });

        // SeekBar navigation for smooth traversal
        seekBarNavigation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentNumber = progress + 1;  // Progress is 0-99, numbers are 1-100
                    updateNumber();
                    speakCurrentNumber();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Quick access number picker button
        btnShowPicker.setOnClickListener(v -> {
            try {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                showNumberPickerDialog();
            } catch (Exception ignored) {
            }
        });
    }

    private void updateNumber() {
        try {
            tvNumber.setText(String.valueOf(currentNumber));
            // USE CENTRALIZED UTILITY - Remove duplicate code
            String numberWord = LanguageConversionUtils.convertNumberToWord(currentNumber, languageCode);
            tvNumberSpelling.setText(numberWord != null ? numberWord : "");
            progressBar.setProgress(currentNumber);
            updateProgressCounter();
            
            // Auto-voice accessibility feature
            if (com.edulinguaghana.AppPreferences.isAutoVoiceEnabled(this)) {
                speakCurrentNumber();
            }
        } catch (Exception ignored) {
        }
    }

    private void updateProgressCounter() {
        if (tvProgressCounter != null) {
            String progressText = getString(R.string.numbers_progress_counter, currentNumber);
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

    private void animateNumber() {
        if (!com.edulinguaghana.AppPreferences.isAnimationsEnabled(this)) return;
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.number_bounce);
        tvNumber.startAnimation(anim);
    }

    private void speakCurrentNumber() {
        if (!requestAudioFocus()) return;
        try {
            // Try to load audio from recorded files first
            int resId = getNumberAudioResId(languageCode, currentNumber);
            if (resId != 0) {
                playAudioResource(resId);  // This only plays if resId is found
            } else if (LanguageConversionUtils.isGhanaianLanguage(languageCode)) {
                // Fallback to GhanaLP TTS if no recorded audio exists
                speakWithGhanaLP(currentNumber);
            } else if (tts != null) {
                // Fallback to Android TTS for other languages
                tts.speak(String.valueOf(currentNumber), TextToSpeech.QUEUE_FLUSH, null, "NUMBER_ID");
            }
            animateNumber();
        } catch (Exception ignored) {
        }
    }

    private void speakWithGhanaLP(int number) {
        try {
            if (isGhanaLpPlaying) {
                offlineTts.stop();
            }

            // Normalize language code for audio file lookup
            final String apiLangCode = LanguageConversionUtils.normalizeLanguageCode(languageCode);

            // Use speakNumber for numbers
            offlineTts.speakNumber(
                number,
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
                        // Fallback to Android TTS if no offline audio found
                        isGhanaLpPlaying = false;
                        runOnUiThread(() -> {
                            try {
                                if (tts != null) {
                                    tts.speak(String.valueOf(number), TextToSpeech.QUEUE_FLUSH, null, "NUMBER_ID");
                                }
                            } catch (Exception ignored) {
                            }
                        });
                    }
                }
            );
        } catch (Exception e) {
            // Try TTS as ultimate fallback
            try {
                if (tts != null && tts.isSpeaking()) {
                    tts.stop();
                }
                if (tts != null) {
                    tts.speak(String.valueOf(number), TextToSpeech.QUEUE_FLUSH, null, "NUMBER_ID");
                }
            } catch (Exception ignored) {
            }
        }
    }

    private int getNumberAudioResId(String lang, int num) {
        if (lang == null) return 0;
        
        String langLower = lang.toLowerCase(Locale.ROOT);
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

        // Format number with leading zeros (001-099, 100)
        // Note: The files are named ewe_number_001.mp3 ... ewe_number_100.mp3
        String fileName = String.format(Locale.ROOT, "%s_number_%03d", filePrefix, num);

        return getResources().getIdentifier(fileName, "raw", getPackageName());
    }

    private void playAudioResource(int resId) {
        if (!requestAudioFocus()) return;
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
     * Show number picker in a bottom sheet dialog with enhanced UI
     */
    private void showNumberPickerDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_number_picker, null);
        TextView tvTitle = bottomSheetView.findViewById(R.id.tvGridTitle);
        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.numberGrid);
        
        if (tvTitle != null) tvTitle.setText(R.string.numbers_grid_title);
        
        // Create list for numbers 1-100
        List<String> numberList = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            numberList.add(String.valueOf(i));
        }
        
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
                
                textView.setText(numberList.get(position));
                
                if (currentNumber == (position + 1)) {
                    card.setStrokeWidth(4);
                    card.setStrokeColor(ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(NumbersActivity.this, R.color.colorAccent)));
                    textView.setTextColor(androidx.core.content.ContextCompat.getColor(NumbersActivity.this, R.color.colorAccent));
                } else {
                    card.setStrokeWidth(0);
                    textView.setTextColor(androidx.core.content.ContextCompat.getColor(NumbersActivity.this, R.color.textColorPrimary));
                }
                
                card.setOnClickListener(v -> {
                    currentNumber = holder.getAdapterPosition() + 1;
                    if (seekBarNavigation != null) seekBarNavigation.setProgress(currentNumber - 1);
                    updateNumber();
                    speakCurrentNumber();
                    dialog.dismiss();
                });
            }

            @Override
            public int getItemCount() {
                return numberList.size();
            }
        });
        
        dialog.setContentView(bottomSheetView);
        dialog.show();
    }

    private void checkAndLogCompletion() {
        if (!lessonCompletedLogSent && currentNumber >= 100) {
            lessonCompletedLogSent = true;
            long duration = (System.currentTimeMillis() - startTime) / 1000;
            
            String userId = null;
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
            
            progressTracker.logLessonCompletion(
                this, 
                userId, 
                languageName + " Numbers", 
                "numbers", 
                duration, 
                null
            );
            
            showMascotMessage(getString(R.string.numbers_lesson_complete, languageName));
            celebrateAction();
        }
    }

    private void showMascotMessage(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        if (mainNumberCard != null) snackbar.setAnchorView(mainNumberCard);
        snackbar.setBackgroundTint(androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary));
        snackbar.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.white));
        
        String fullMessage = "🦉 " + message;
        
        View snackbarView = snackbar.getView();
        TextView tv = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (tv != null) tv.setText(fullMessage);
        
        snackbar.show();
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
