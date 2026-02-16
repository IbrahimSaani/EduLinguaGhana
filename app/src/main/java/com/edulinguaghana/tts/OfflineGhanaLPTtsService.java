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
     * Expected filename format: {language}_letter_{letter}.wav
     * Example: twi_letter_a.wav, ewe_letter_d.wav
     */
    public void speakLetter(String letter, String language, PlaybackCallback callback) {
        String sanitized = sanitizeForFilename(letter);
        String resourceName = language + "_letter_" + sanitized;
        playAudioResource(resourceName, callback);
    }

    /**
     * Play word audio from res/raw
     * Expected filename format: {language}_word_{word}.wav
     * Example: twi_word_akoko.wav
     */
    public void speakWord(String word, String language, PlaybackCallback callback) {
        String sanitized = sanitizeForFilename(word);
        String resourceName = language + "_word_" + sanitized;
        playAudioResource(resourceName, callback);
    }

    /**
     * Play number audio from res/raw
     * Expected filename format: {language}_number_{num}.wav
     * Example: twi_number_001.wav, ewe_number_025.wav
     */
    public void speakNumber(int number, String language, PlaybackCallback callback) {
        String resourceName = String.format(Locale.US, "%s_number_%03d", language, number);
        playAudioResource(resourceName, callback);
    }

    /**
     * Play any text - tries to find matching audio file
     */
    public void speak(String text, String language, PlaybackCallback callback) {
        String sanitized = sanitizeForFilename(text);

        // Try different resource name patterns
        String[] patterns = {
            language + "_" + sanitized,              // Direct match
            language + "_word_" + sanitized,         // Word pattern
            language + "_letter_" + sanitized,       // Letter pattern
        };

        for (String resourceName : patterns) {
            int resId = getResourceId(resourceName);
            if (resId != 0) {
                playAudioResource(resourceName, callback);
                return;
            }
        }

        // No audio file found
        callback.onError("No audio file found for: " + text);
    }

    /**
     * Play audio from res/raw by resource name
     */
    private void playAudioResource(String resourceName, PlaybackCallback callback) {
        try {
            int resId = getResourceId(resourceName);

            if (resId == 0) {
                Log.w(TAG, "Audio resource not found: " + resourceName);
                callback.onError("Audio not found: " + resourceName);
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
                callback.onError("Failed to create MediaPlayer for: " + resourceName);
                return;
            }

            mediaPlayer.setOnCompletionListener(mp -> {
                callback.onComplete();
                mp.release();
                mediaPlayer = null;
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                String error = "MediaPlayer error: " + what + ", " + extra;
                Log.e(TAG, error);
                callback.onError(error);
                mp.release();
                mediaPlayer = null;
                return true;
            });

            callback.onStart();
            mediaPlayer.start();
            Log.d(TAG, "Playing audio: " + resourceName);

        } catch (Exception e) {
            Log.e(TAG, "Error playing audio: " + resourceName, e);
            callback.onError("Error: " + e.getMessage());
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
     */
    private String sanitizeForFilename(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Convert to lowercase
        String result = text.toLowerCase(Locale.ROOT);

        // Replace special Ghanaian characters
        result = result.replace("ɛ", "e")
                      .replace("ɔ", "o")
                      .replace("ɖ", "d")
                      .replace("ƒ", "f")
                      .replace("ɣ", "g")
                      .replace("ŋ", "n")
                      .replace("ʋ", "v")
                      .replace("ɲ", "ny");

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
        String[] patterns = {
            language + "_" + sanitized,
            language + "_word_" + sanitized,
            language + "_letter_" + sanitized,
        };

        for (String resourceName : patterns) {
            if (getResourceId(resourceName) != 0) {
                return true;
            }
        }
        return false;
    }
}

