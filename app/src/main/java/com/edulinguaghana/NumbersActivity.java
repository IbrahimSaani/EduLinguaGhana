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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.edulinguaghana.tts.OfflineGhanaLPTtsService;
import com.edulinguaghana.utils.LanguageConversionUtils;

import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class NumbersActivity extends AppCompatActivity {

    private TextView tvLanguageTitleNum, tvNumber, tvNumberSpelling;
    private TextView tvProgressCounter;
    private TextView celebrationEmoji;
    private MaterialButton btnPrevNumber, btnNextNumber;
    private FloatingActionButton btnBackNumber;
    private Button btnSpeakNumber;
    private LinearProgressIndicator progressBar;
    private MaterialCardView numberCard;
    private MaterialCardView modeBadgeCard;
    private TextView modeBadgeIcon, modeBadgeText, modeBadgeDescription;
    private SeekBar seekBarNavigation;  // For smooth navigation
    private MaterialButton btnShowPicker;  // For quick number picker
    private MaterialButton btnSpeakQuick;  // For quick speak button

    private Vibrator vibrator;  // For haptic feedback

    private String languageCode;
    private String languageName;
    private String mode;           // "recital" or "practice"
    private boolean isRecitalMode;

    private int currentNumber = 1;  // 1..100
    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;

    // Offline TTS for native Ghanaian languages (loads from res/raw)
    private OfflineGhanaLPTtsService offlineTts;
    private boolean isGhanaLpPlaying = false;

    private static final int REQ_CODE_SPEECH_INPUT = 300;
    private static final int REQ_CODE_RECORD_AUDIO = 400;
    // Speech recognition retry state
    private int speechRetryCount = 0;
    private static final int MAX_SPEECH_RETRIES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numbers);

        tvLanguageTitleNum = findViewById(R.id.tvLanguageTitleNum);
        tvNumber = findViewById(R.id.tvNumber);
        tvNumberSpelling = findViewById(R.id.tvNumberSpelling);
        tvProgressCounter = findViewById(R.id.tvProgressCounter);
        celebrationEmoji = findViewById(R.id.celebrationEmoji);
        btnPrevNumber = findViewById(R.id.btnPrevNumber);
        btnNextNumber = findViewById(R.id.btnNextNumber);
        btnBackNumber = findViewById(R.id.btnBackNumber);
        btnSpeakNumber = findViewById(R.id.btnSpeakNumber);
        progressBar = findViewById(R.id.progressBar);
        numberCard = findViewById(R.id.numberCard);
        modeBadgeCard = findViewById(R.id.modeBadgeCard);
        modeBadgeIcon = findViewById(R.id.modeBadgeIcon);
        modeBadgeText = findViewById(R.id.modeBadgeText);
        modeBadgeDescription = findViewById(R.id.modeBadgeDescription);

        // Initialize navigation controls
        seekBarNavigation = findViewById(R.id.seekBarNavigation);
        btnShowPicker = findViewById(R.id.btnShowPicker);
        btnSpeakQuick = findViewById(R.id.btnSpeakQuick);

        // Initialize vibrator for haptic feedback
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        languageCode = getIntent().getStringExtra("LANG_CODE");
        languageName = getIntent().getStringExtra("LANG_NAME");
        mode = getIntent().getStringExtra("MODE");

        if (languageName == null) languageName = "Unknown";
        if (mode == null) mode = "practice";

        isRecitalMode = mode.equals("recital");

        tvLanguageTitleNum.setText("Language: " + languageName);
        btnSpeakNumber.setText(isRecitalMode ? "Repeat" : "Practice");
        updateModeBadge();

        progressBar.setMax(100);

        // Initialize Offline TTS for native languages (loads from res/raw)
        offlineTts = new OfflineGhanaLPTtsService(this);

        tts = new TextToSpeech(this, status -> {
            try {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(LanguageConversionUtils.getLocaleForLanguage(languageCode));
                    updateNumber();
                    if (isRecitalMode) {
                        speakCurrentNumber();
                    }
                } else {
                    Toast.makeText(this, "TTS init failed", Toast.LENGTH_SHORT).show();
                    updateNumber();
                }
            } catch (Exception e) {
                android.util.Log.e("NumbersActivity", "Error during TTS initialization or recital playback", e);
                updateNumber();
            }
        });

        btnNextNumber.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(30);  // Light haptic
            } catch (Exception e) {
                android.util.Log.w("NumbersActivity", "Haptic feedback failed", e);
            }
            try {
                currentNumber++;
                if (currentNumber > 100) currentNumber = 1;
                updateNumber();
                if (isRecitalMode) speakCurrentNumber();
            } catch (Exception e) {
                android.util.Log.e("NumbersActivity", "Error advancing number", e);
            }
        });

        btnPrevNumber.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(30);  // Light haptic
            } catch (Exception e) {
                android.util.Log.w("NumbersActivity", "Haptic feedback failed", e);
            }
            try {
                currentNumber--;
                if (currentNumber < 1) currentNumber = 100;
                updateNumber();
                if (isRecitalMode) speakCurrentNumber();
            } catch (Exception e) {
                android.util.Log.e("NumbersActivity", "Error going back number", e);
            }
        });

        btnBackNumber.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(50);  // Medium haptic
            } catch (Exception e) {
                android.util.Log.w("NumbersActivity", "Haptic feedback failed", e);
            }
            finish();
        });

        btnSpeakNumber.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(50);  // Medium haptic
                celebrateAction();  // Celebration animation
            } catch (Exception e) {
                android.util.Log.w("NumbersActivity", "Haptic feedback or celebration failed", e);
            }
            try {
                if (isRecitalMode) {
                    speakCurrentNumber();
                } else {
                    startPracticePronunciation();
                }
            } catch (Exception e) {
                android.util.Log.e("NumbersActivity", "Error speaking number", e);
            }
        });

        // Make number card tappable to speak
        numberCard.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(40);  // Light-medium haptic
                speakCurrentNumber();
                celebrateAction();  // Show celebration
            } catch (Exception e) {
                android.util.Log.e("NumbersActivity", "Error in number card click", e);
            }
        });

        // SeekBar navigation for smooth traversal
        seekBarNavigation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentNumber = progress + 1;  // Progress is 0-99, numbers are 1-100
                    updateNumber();
                    if (isRecitalMode) speakCurrentNumber();
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
                triggerHapticFeedback(30);
                showNumberPickerDialog();
            } catch (Exception e) {
                android.util.Log.e("NumbersActivity", "Error showing number picker", e);
            }
        });

        // Quick speak button
        btnSpeakQuick.setOnClickListener(v -> {
            try {
                triggerHapticFeedback(40);
                animateButtonPress(btnSpeakQuick);
                speakCurrentNumber();
                celebrateAction();
            } catch (Exception e) {
                android.util.Log.e("NumbersActivity", "Error in quick speak button", e);
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
        } catch (Exception e) {
            android.util.Log.e("NumbersActivity", "Error updating number display", e);
            // Ensure display is at least partially updated
            try {
                tvNumber.setText(String.valueOf(currentNumber));
            } catch (Exception ignored) {
            }
        }
    }

    private void updateProgressCounter() {
        if (tvProgressCounter != null) {
            String progressText = "Number " + currentNumber + " of 100";
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
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.number_bounce);
        tvNumber.startAnimation(anim);
    }

    private void speakCurrentNumber() {
        try {
            // Try to load audio from recorded files first
            int resId = getNumberAudioResId(languageCode, currentNumber);
            if (resId != 0) {
                playAudioResource(resId);  // This only plays if resId is found
            } else if (isGhanaianLanguage(languageCode)) {
                // Fallback to GhanaLP TTS if no recorded audio exists
                speakWithGhanaLP(currentNumber);
            } else if (tts != null) {
                // Fallback to Android TTS for other languages
                tts.speak(String.valueOf(currentNumber), TextToSpeech.QUEUE_FLUSH, null, "NUMBER_ID");
            }
            animateNumber();
        } catch (Exception e) {
            android.util.Log.e("NumbersActivity", "Error in speakCurrentNumber", e);
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

    private void speakWithGhanaLP(int number) {
        try {
            if (isGhanaLpPlaying) {
                offlineTts.stop();
            }

            // Normalize language code for audio file lookup
            String apiLangCode = normalizeLanguageCode(languageCode);

            // Disable speak button during playback
            if (btnSpeakNumber != null) {
                btnSpeakNumber.setEnabled(false);
            }

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
                        runOnUiThread(() -> {
                            if (btnSpeakNumber != null) {
                                btnSpeakNumber.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // Fallback to Android TTS if no offline audio found
                        isGhanaLpPlaying = false;
                        runOnUiThread(() -> {
                            if (btnSpeakNumber != null) {
                                btnSpeakNumber.setEnabled(true);
                            }
                            android.util.Log.w("NumbersActivity", "No offline audio found for number: " + number);
                            try {
                                if (tts != null) {
                                    tts.speak(String.valueOf(number), TextToSpeech.QUEUE_FLUSH, null, "NUMBER_ID");
                                }
                            } catch (Exception e) {
                                android.util.Log.e("NumbersActivity", "Error during TTS fallback speak", e);
                            }
                        });
                    }
                }
            );
        } catch (Exception e) {
            android.util.Log.e("NumbersActivity", "Error in speakWithGhanaLP", e);
            // Try TTS as ultimate fallback
            try {
                if (tts != null && tts.isSpeaking()) {
                    tts.stop();
                }
                if (tts != null) {
                    tts.speak(String.valueOf(number), TextToSpeech.QUEUE_FLUSH, null, "NUMBER_ID");
                }
                if (btnSpeakNumber != null) {
                    btnSpeakNumber.setEnabled(true);
                }
            } catch (Exception ttsError) {
                android.util.Log.e("NumbersActivity", "TTS fallback also failed", ttsError);
                if (btnSpeakNumber != null) {
                    btnSpeakNumber.setEnabled(true);
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

    private int getNumberAudioResId(String lang, int num) {
        if (lang == null) return 0;
        // Format number with leading zeros (001-099, 0100 for 100)
        String fileName;
        if (num == 100) {
            fileName = lang.toLowerCase(Locale.ROOT) + "_number_0100";
        } else {
            fileName = String.format(Locale.ROOT, "%s_number_%03d", lang.toLowerCase(Locale.ROOT), num);
        }
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
        speakCurrentNumber();

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
                "🎤 Try to pronounce: " + currentNumber +
                "\n\n📝 Listen carefully to the audio above and repeat it!\n\n" +
                "Tap the number again to hear it.",
                Toast.LENGTH_LONG).show();

            // Show helpful tips for Ghanaian languages
            showPronunciationTips();
        }
    }

    private void showPronunciationTips() {
        String tips = "";

        switch (languageCode) {
            case "ak": // Twi (backward compatibility)
            case "twi":
                tips = "🎤 Speak clearly and naturally in Twi";
                break;
            case "ee": // Ewe
                tips = "🎤 Remember: Ewe numbers have specific tones";
                break;
            case "gaa": // Ga
                tips = "🎤 Ga numbers require clear pronunciation";
                break;
        }

        if (!tips.isEmpty()) {
            Toast.makeText(this, "💡 Tip: " + tips, Toast.LENGTH_LONG).show();
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

        // Improve recognition behavior: allow multiple results, set brief silence timeouts
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);
        // Shorten the silence thresholds so recognition completes sooner when user pauses
        intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "PCM"); // best-effort hint
        try {
            // Some providers accept these extra keys for silence length
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 800L);
        } catch (Exception e) {
            // Ignore if provider doesn't support them
        }

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not supported on this device.", Toast.LENGTH_LONG).show();
        }
    }

    private String getSpeechLocaleCode() {
        return LanguageConversionUtils.getSpeechLocaleCode(languageCode);
    }

    private Locale getLocaleForLanguage(String code) {
        return LanguageConversionUtils.getLocaleForLanguage(code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    // Try to find an accepted candidate from the results
                    int expected = currentNumber;
                    String accepted = findAcceptedResult(result, expected);
                    if (accepted != null) {
                        // Success
                        speechRetryCount = 0;
                        evaluatePronunciation(accepted);
                    } else {
                        // No candidate matched; retry automatically up to MAX_SPEECH_RETRIES
                        if (speechRetryCount < MAX_SPEECH_RETRIES) {
                            speechRetryCount++;
                            Toast.makeText(this, "Didn't catch that — please try again.", Toast.LENGTH_SHORT).show();
                            promptSpeechInput();
                        } else {
                            speechRetryCount = 0;
                            Toast.makeText(this, "Couldn't recognize your pronunciation. Try again manually.", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (speechRetryCount < MAX_SPEECH_RETRIES) {
                        speechRetryCount++;
                        Toast.makeText(this, "Could not hear you. Try again.", Toast.LENGTH_SHORT).show();
                        promptSpeechInput();
                    } else {
                        speechRetryCount = 0;
                        Toast.makeText(this, "Could not hear you. Try again later.", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                // User cancelled or failure
                speechRetryCount = 0;
            }
        }
    }

    /**
     * Iterate recognition candidates and return the first accepted textual candidate, or null.
     */
    private String findAcceptedResult(ArrayList<String> candidates, int expectedNumber) {
        if (candidates == null || candidates.isEmpty()) return null;
        for (String cand : candidates) {
            if (cand == null) continue;
            String cleaned = cand.trim();
            Integer parsed = parseRecognizedToNumber(cleaned);
            if (parsed != null && parsed == expectedNumber) {
                return cleaned;
            }
            // Also check if the recognized string contains the expected digit textually (e.g., "four" contains "four")
            String normalized = cleaned.toLowerCase();
            String expectedWord = LanguageConversionUtils.convertNumberToWord(expectedNumber, "en");
            if (expectedWord != null && !expectedWord.isEmpty() && normalized.contains(expectedWord.toLowerCase())) {
                return cleaned;
            }
        }
        return null;
    }

    /**
     * Try to parse the recognized string into an Integer. Handles numeric words for common cases.
     */
    private Integer parseRecognizedToNumber(String recognized) {
        if (recognized == null) return null;
        String cleaned = recognized.replaceAll("[^0-9a-zA-Z\\s-]", "").trim();
        // Try direct parse
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException ignored) {}

        // Try to parse common english words (one..twenty, tens, hundred)
        String lower = cleaned.toLowerCase();
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        map.put("one", 1); map.put("two", 2); map.put("three", 3); map.put("four", 4); map.put("for", 4);
        map.put("five", 5); map.put("six", 6); map.put("seven", 7); map.put("eight", 8); map.put("nine", 9);
        map.put("ten", 10); map.put("eleven", 11); map.put("twelve", 12); map.put("thirteen", 13);
        map.put("fourteen", 14); map.put("fifteen", 15); map.put("sixteen", 16); map.put("seventeen", 17);
        map.put("eighteen", 18); map.put("nineteen", 19); map.put("twenty", 20); map.put("thirty", 30);
        map.put("forty", 40); map.put("fifty", 50); map.put("sixty", 60); map.put("seventy", 70);
        map.put("eighty", 80); map.put("ninety", 90); map.put("hundred", 100);

        // Handle compound like 'twenty one' or 'twenty-one'
        String[] parts = lower.split("[\\s-]+");
        int total = 0;
        boolean found = false;
        for (String p : parts) {
            if (map.containsKey(p)) {
                int val = map.get(p);
                if (val == 100) {
                    total = (total == 0 ? 100 : total * 100);
                } else {
                    total += val;
                }
                found = true;
            }
        }
        if (found) return total > 0 ? total : null;
        return null;
    }

    private void evaluatePronunciation(String recognized) {
        int expected = currentNumber;

        try {
            int recognizedNumber = Integer.parseInt(recognized);
            if (recognizedNumber == expected) {
                // Perfect match!
                celebrateAction();
                Toast.makeText(this,
                    "🎉 Excellent! Perfect pronunciation of " + recognized +
                    "\n✅ You got it right!",
                    Toast.LENGTH_LONG).show();
            } else {
                // Didn't match
                Toast.makeText(this,
                    "🎤 I heard: \"" + recognized + "\"" +
                    "\n📝 Expected: \"" + expected + "\"" +
                    "\n\n💡 Listen to the audio again and try once more!",
                    Toast.LENGTH_LONG).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this,
                "🎤 I heard: \"" + recognized + "\" (not a number)" +
                "\n📝 Expected number: " + expected +
                "\n\n💡 Try again and speak clearly!",
                Toast.LENGTH_LONG).show();
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

    /**
     * Show number picker in a bottom sheet dialog
     */
    private void showNumberPickerDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        
        // Create a custom grid view for numbers
        GridView gridView = new GridView(this);
        gridView.setNumColumns(10);
        gridView.setPadding(16, 16, 16, 16);
        gridView.setVerticalSpacing(8);
        gridView.setHorizontalSpacing(8);
        
        // Create adapter for numbers 1-100
        java.util.ArrayList<String> numberList = new java.util.ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            numberList.add(String.valueOf(i));
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            numberList
        ) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.widget.TextView textView = new android.widget.TextView(NumbersActivity.this);
                textView.setText(getItem(position));
                textView.setTextSize(16);
                textView.setTextColor(currentNumber == (position + 1) ? 
                    getColor(R.color.colorAccent) : getColor(R.color.textColorPrimary));
                textView.setTypeface(null, android.graphics.Typeface.BOLD);
                textView.setGravity(android.view.Gravity.CENTER);
                textView.setPadding(8, 16, 8, 16);
                
                if (currentNumber == (position + 1)) {
                    textView.setBackgroundResource(R.drawable.ripple_card_effect);
                }
                
                return textView;
            }
        };
        
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            currentNumber = position + 1;
            seekBarNavigation.setProgress(position);
            updateNumber();
            if (isRecitalMode) speakCurrentNumber();
            dialog.dismiss();
        });
        
        dialog.setContentView(gridView);
        dialog.show();
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

    private void updateModeBadge() {
        try {
            if (modeBadgeText != null) {
                if (isRecitalMode) {
                    modeBadgeIcon.setText("⭐");
                    modeBadgeText.setText("RECITAL");
                    modeBadgeDescription.setText("Listen & Learn");
                    modeBadgeCard.setCardBackgroundColor(getColor(R.color.colorAccent));
                    modeBadgeText.setTextColor(getColor(android.R.color.white));
                    modeBadgeDescription.setTextColor(getColor(android.R.color.white));
                    modeBadgeDescription.setAlpha(0.8f);
                } else {
                    modeBadgeIcon.setText("🎤");
                    modeBadgeText.setText("PRACTICE");
                    modeBadgeDescription.setText("Speak & Learn");
                    modeBadgeCard.setCardBackgroundColor(getColor(R.color.colorPrimary));
                    modeBadgeText.setTextColor(getColor(android.R.color.white));
                    modeBadgeDescription.setTextColor(getColor(android.R.color.white));
                    modeBadgeDescription.setAlpha(0.8f);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("NumbersActivity", "Error updating mode badge", e);
        }
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
