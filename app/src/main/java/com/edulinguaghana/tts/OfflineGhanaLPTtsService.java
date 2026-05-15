package com.edulinguaghana.tts;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.Locale;

/**
 * Offline TTS Service - Loads pre-downloaded audio from res/raw
 * No API calls needed - 100% offline
 */
public class OfflineGhanaLPTtsService {
    private static final String TAG = "OfflineGhanaLPTts";

    private final Context context;
    private MediaPlayer mediaPlayer;

    public interface PlaybackCallback {
        void onStart();
        void onComplete();
        void onError(String error);
    }

    public OfflineGhanaLPTtsService(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Play letter audio from res/raw
     * Expected filename format: {language}_letter_{letter}.mp3
     * Example: ewe_letter_a.mp3, gaa_letter_b.mp3
     */
    public void speakLetter(String letter, String language, PlaybackCallback callback) {
        String sanitized = sanitizeForFilename(letter);
        String audioLangCode = normalizeLanguageForAudio(language);
        String resourceName = audioLangCode + "_letter_" + sanitized;
        Log.d(TAG, "Speaking letter: " + letter + " in language: " + language + " (audio: " + audioLangCode + ") -> resource: " + resourceName);
        playAudioResource(resourceName, callback);
    }

    /**
     * Play word audio from res/raw
     * Expected filename format: {language}_word_{word}.mp3
     * Example: ewe_word_apple.mp3
     */
    public void speakWord(String word, String language, PlaybackCallback callback) {
        String sanitized = sanitizeForFilename(word);
        String audioLangCode = normalizeLanguageForAudio(language);
        String resourceName = audioLangCode + "_word_" + sanitized;
        Log.d(TAG, "Speaking word: " + word + " in language: " + language + " (audio: " + audioLangCode + ") -> resource: " + resourceName);
        playAudioResource(resourceName, callback);
    }

    /**
     * Play number audio from res/raw
     * Expected filename format: {language}_number_{num}.mp3
     * Example: ewe_number_001.mp3, gaa_number_025.mp3
     */
    public void speakNumber(int number, String language, PlaybackCallback callback) {
        String audioLangCode = normalizeLanguageForAudio(language);
        String resourceName = String.format(Locale.US, "%s_number_%03d", audioLangCode, number);
        Log.d(TAG, "Speaking number: " + number + " in language: " + language + " (audio: " + audioLangCode + ") -> resource: " + resourceName);
        playAudioResource(resourceName, callback);
    }

    /**
     * Play any text - tries to find matching audio file
     */
    public void speak(String text, String language, PlaybackCallback callback) {
        String sanitized = sanitizeForFilename(text);
        // Normalize language code to match audio file naming convention
        String audioLangCode = normalizeLanguageForAudio(language);

        // Check if text is a number and format it with zero-padding
        String numberPadded = null;
        try {
            int num = Integer.parseInt(text.trim());
            if (num >= 1 && num <= 100) {
                numberPadded = String.format(Locale.US, "%03d", num);
                Log.d(TAG, "Detected number: " + text + " -> formatted as: " + numberPadded);
            }
        } catch (NumberFormatException e) {
            // Not a number, continue with text patterns
        }

        // Try different resource name patterns
        String[] patterns;
        if (numberPadded != null) {
            // For numbers, try number patterns first
            patterns = new String[]{
                audioLangCode + "_number_" + numberPadded,  // Number pattern with padding
                audioLangCode + "_" + sanitized,            // Direct match
                audioLangCode + "_word_" + sanitized,       // Word pattern
                audioLangCode + "_letter_" + sanitized,     // Letter pattern
            };
        } else {
            patterns = new String[]{
                audioLangCode + "_" + sanitized,              // Direct match
                audioLangCode + "_word_" + sanitized,         // Word pattern
                audioLangCode + "_letter_" + sanitized,       // Letter pattern
                audioLangCode + "_number_" + sanitized,       // Number pattern
            };
        }

        Log.d(TAG, "Attempting to speak '" + text + "' in language '" + language + "' (audio lang: '" + audioLangCode + "')");

        for (String resourceName : patterns) {
            Log.d(TAG, "Trying resource pattern: " + resourceName);
            int resId = getResourceId(resourceName);
            if (resId != 0) {
                Log.d(TAG, "Found resource: " + resourceName + " (ID: " + resId + ")");
                playAudioResource(resourceName, callback);
                return;
            } else {
                Log.w(TAG, "Resource not found: " + resourceName);
            }
        }

        // No audio file found
        Log.e(TAG, "No audio file found for: " + text + " in language: " + language);
        if (callback != null) {
            callback.onError("No audio file found for: " + text);
        }
    }

    /**
     * Play audio from res/raw by resource name
     */
    private void playAudioResource(String resourceName, PlaybackCallback callback) {
        try {
            int resId = getResourceId(resourceName);

            if (resId == 0) {
                Log.w(TAG, "Audio resource not found: " + resourceName);
                if (callback != null) {
                    callback.onError("Audio not found: " + resourceName);
                }
                return;
            }

            // Release previous MediaPlayer
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            // Create and play
            mediaPlayer = MediaPlayer.create(context, resId);

            if (mediaPlayer == null) {
                if (callback != null) {
                    callback.onError("Failed to create MediaPlayer for: " + resourceName);
                }
                return;
            }

            mediaPlayer.setOnCompletionListener(mp -> {
                if (callback != null) {
                    callback.onComplete();
                }
                mp.release();
                mediaPlayer = null;
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                String error = "MediaPlayer error: " + what + ", " + extra;
                Log.e(TAG, error);
                if (callback != null) {
                    callback.onError(error);
                }
                mp.release();
                mediaPlayer = null;
                return true;
            });

            if (callback != null) {
                callback.onStart();
            }
            mediaPlayer.start();
            Log.d(TAG, "Playing audio: " + resourceName);

        } catch (Exception e) {
            Log.e(TAG, "Error playing audio: " + resourceName, e);
            if (callback != null) {
                callback.onError("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Get resource ID from res/raw folder
     */
    private int getResourceId(String resourceName) {
        try {
            Resources resources = context.getResources();
            return resources.getIdentifier(resourceName, "raw", context.getPackageName());
        } catch (Exception e) {
            Log.e(TAG, "Error getting resource ID for: " + resourceName, e);
            return 0;
        }
    }

    /**
     * Sanitize text for filename compatibility
     * Converts special characters to safe alternatives
     * Handles Ghanaian language-specific characters
     */
    private String sanitizeForFilename(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Convert to lowercase
        String result = text.toLowerCase(Locale.ROOT);

        // Replace special Ghanaian characters with descriptive names
        // These match the actual filenames in res/raw
        result = result.replace("ɛ", "e_open")      // Open e (Ewe, Twi)
                      .replace("ɔ", "o_open")       // Open o (Ewe, Twi)
                      .replace("ɖ", "d_caron")      // D with hook (Ewe)
                      .replace("ƒ", "f_hook")       // F with hook (Ewe)
                      .replace("ɣ", "g_hook")       // G with hook (Ewe)
                      .replace("ŋ", "ng")           // Engma/Ng (Ga, Ewe)
                      .replace("ʋ", "v_hook")       // V with hook (Ewe)
                      .replace("ɲ", "ny");          // Ny digraph

        // Replace diacritics
        result = result.replace("ã", "a")
                      .replace("õ", "o")
                      .replace("ũ", "u")
                      .replace("ĩ", "i")
                      .replace("ẽ", "e")
                      .replace("ā", "a")
                      .replace("ē", "e")
                      .replace("ī", "i")
                      .replace("ō", "o")
                      .replace("ū", "u");

        // Replace spaces and special chars
        result = result.replace(" ", "_")
                      .replace("-", "_")
                      .replace("'", "")
                      .replace("\"", "");

        // Remove any remaining non-alphanumeric characters except underscore
        result = result.replaceAll("[^a-z0-9_]", "");

        return result;
    }

    /**
     * Normalize language code to match audio file naming convention in res/raw
     * Maps normalized codes to actual file prefixes used
     */
    private String normalizeLanguageForAudio(String languageCode) {
        if (languageCode == null) return "en";
        String normalized = languageCode.toLowerCase(Locale.ROOT);

        // Map language codes to audio file prefixes
        switch (normalized) {
            case "ak":
            case "twi":
                return "twi";
            case "ee":
            case "ewe":
                return "ewe";
            case "gaa":
            case "ga":
                return "gaa";
            case "fr":
                return "fr";
            case "en":
            default:
                return "en";
        }
    }

    /**
     * Stop playback and release resources
     */
    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Release all resources
     */
    public void release() {
        stop();
    }

    /**
     * Check if audio file exists for given text
     */
    public boolean hasAudio(String text, String language) {
        String sanitized = sanitizeForFilename(text);
        String audioLangCode = normalizeLanguageForAudio(language);
        String[] patterns = {
            audioLangCode + "_" + sanitized,
            audioLangCode + "_word_" + sanitized,
            audioLangCode + "_letter_" + sanitized,
            audioLangCode + "_number_" + sanitized,
        };

        for (String resourceName : patterns) {
            if (getResourceId(resourceName) != 0) {
                return true;
            }
        }
        return false;
    }
}

