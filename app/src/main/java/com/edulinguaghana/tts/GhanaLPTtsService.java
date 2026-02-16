package com.edulinguaghana.tts;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Service for calling GhanaLP Text-to-Speech API
 * Supports Twi, Ewe, and Ga languages
 */
public class GhanaLPTtsService {
    private static final String TAG = "GhanaLPTtsService";
    private static final String API_URL = "https://translation-api.ghananlp.org/tts/v2/synthesize";
    private static final String API_KEY = "2541515ab0984aeb8604153ad10b0d71";

    private final Context context;
    private final OkHttpClient client;
    private MediaPlayer mediaPlayer;

    public interface TtsCallback {
        void onSuccess(File audioFile);
        void onError(String error);
    }

    public interface PlaybackCallback {
        void onStart();
        void onComplete();
        void onError(String error);
    }

    public GhanaLPTtsService(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Synthesize text to speech using GhanaLP API
     *
     * @param text The text to synthesize
     * @param language The language code: "twi", "ewe", or "ga"
     * @param speakerId Speaker voice: "male_low", "male_high", "female_low", "female_high"
     * @param callback Callback for success/error
     */
    public void synthesize(String text, String language, String speakerId, TtsCallback callback) {
        if (text == null || text.trim().isEmpty()) {
            callback.onError("Text cannot be empty");
            return;
        }

        // Validate language
        String langLower = language.toLowerCase();
        if (!langLower.equals("twi") && !langLower.equals("ewe") && !langLower.equals("ga")) {
            callback.onError("Unsupported language: " + language + ". Use 'twi', 'ewe', or 'ga'");
            return;
        }

        // Create JSON request body
        try {
            JSONObject json = new JSONObject();
            json.put("text", text);
            json.put("language", langLower);
            json.put("speaker_id", speakerId != null ? speakerId : "male_low");
            json.put("stream", true);
            json.put("format", "wav");

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Ocp-Apim-Subscription-Key", API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "API request failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        String errorMsg = "API error: " + response.code();
                        try {
                            ResponseBody errorBody = response.body();
                            if (errorBody != null) {
                                errorMsg += " - " + errorBody.string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error response", e);
                        }
                        Log.e(TAG, errorMsg);
                        callback.onError(errorMsg);
                        return;
                    }

                    try {
                        ResponseBody responseBody = response.body();
                        if (responseBody == null) {
                            callback.onError("Empty response from API");
                            return;
                        }

                        // Save audio to file
                        File audioFile = saveAudioFile(responseBody.byteStream(), text, langLower);
                        callback.onSuccess(audioFile);

                    } catch (Exception e) {
                        Log.e(TAG, "Error saving audio file", e);
                        callback.onError("Error saving audio: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            callback.onError("Error creating request: " + e.getMessage());
        }
    }

    /**
     * Save audio stream to a file
     */
    private File saveAudioFile(InputStream inputStream, String text, String language) throws IOException {
        // Create cache directory for TTS audio
        File cacheDir = new File(context.getCacheDir(), "ghananlp_tts");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        // Generate filename based on text hash to cache responses
        String filename = language + "_" + Math.abs(text.hashCode()) + ".wav";
        File audioFile = new File(cacheDir, filename);

        // Write stream to file
        try (FileOutputStream fos = new FileOutputStream(audioFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        } finally {
            inputStream.close();
        }

        Log.d(TAG, "Audio saved to: " + audioFile.getAbsolutePath());
        return audioFile;
    }

    /**
     * Synthesize and play audio immediately
     */
    public void synthesizeAndPlay(String text, String language, String speakerId, PlaybackCallback callback) {
        synthesize(text, language, speakerId, new TtsCallback() {
            @Override
            public void onSuccess(File audioFile) {
                playAudio(audioFile, callback);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    /**
     * Play audio file using MediaPlayer
     */
    public void playAudio(File audioFile, PlaybackCallback callback) {
        try {
            // Release previous player if exists
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());

            mediaPlayer.setOnPreparedListener(mp -> {
                callback.onStart();
                mp.start();
            });

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

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "Error playing audio", e);
            callback.onError("Error playing audio: " + e.getMessage());
        }
    }

    /**
     * Stop current playback
     */
    public void stop() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping playback", e);
            }
            mediaPlayer = null;
        }
    }

    /**
     * Check if audio is currently playing
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * Clean up resources
     */
    public void release() {
        stop();

        // Optional: Clear cache
        // clearCache();
    }

    /**
     * Clear cached audio files
     */
    public void clearCache() {
        File cacheDir = new File(context.getCacheDir(), "ghananlp_tts");
        if (cacheDir.exists() && cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }

    /**
     * Get appropriate speaker ID based on preferences
     */
    public static String getDefaultSpeakerId(String gender, String pitch) {
        String genderLower = gender != null ? gender.toLowerCase() : "male";
        String pitchLower = pitch != null ? pitch.toLowerCase() : "low";

        if (genderLower.contains("female")) {
            return pitchLower.contains("high") ? "female_high" : "female_low";
        } else {
            return pitchLower.contains("high") ? "male_high" : "male_low";
        }
    }
}

