package com.edulinguaghana.audio;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * AudioCacheManager - Comprehensive audio caching system for TTS and regular audio files
 *
 * Features:
 * - Cache audio files from raw resources, TTS output, and byte arrays
 * - LRU eviction policy with TTL support
 * - In-memory and disk-based caching
 * - Memory-efficient storage with automatic cleanup
 * - Fast playback from cache
 * - Support for TTS audio with language-aware caching
 * - Cache statistics and performance metrics
 *
 * Cache Structure:
 * - Individual file max: 5MB
 * - Total cache max: 100MB
 * - TTL: 7 days
 */
public class AudioCacheManager {

    private static final String TAG = "AudioCacheManager";
    private static final String CACHE_DIR = "audio_cache";
    private static final long MAX_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final long INDIVIDUAL_FILE_MAX = 5 * 1024 * 1024; // 5MB per file
    private static final long CACHE_ENTRY_TTL = 7 * 24 * 60 * 60 * 1000; // 7 days
    private static final int MAX_MEMORY_ENTRIES = 100;

    private final Context context;
    private final File cacheDirectory;
    private final Map<String, CachedAudio> audioCache;
    private final Map<String, CachedAudio> memoryCache;
    private long currentCacheSize = 0;

    // Callback interface for audio playback
    public interface AudioPlaybackCallback {
        void onPlaybackStart(String audioId);
        void onPlaybackComplete(String audioId);
        void onPlaybackError(String audioId, String error);
    }

    // Inner class to track cached audio
    private static class CachedAudio {
        String audioId;
        File cacheFile;
        long fileSize;
        long createdTime;
        long lastAccessTime;
        int accessCount;
        String languageCode; // For TTS audio

        CachedAudio(String audioId, File cacheFile, long fileSize) {
            this.audioId = audioId;
            this.cacheFile = cacheFile;
            this.fileSize = fileSize;
            this.createdTime = System.currentTimeMillis();
            this.lastAccessTime = System.currentTimeMillis();
            this.accessCount = 0;
        }

        CachedAudio(String audioId, File cacheFile, long fileSize, String languageCode) {
            this(audioId, cacheFile, fileSize);
            this.languageCode = languageCode;
        }

        void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
            this.accessCount++;
        }

        boolean isExpired() {
            return (System.currentTimeMillis() - createdTime) > CACHE_ENTRY_TTL;
        }
    }

    public AudioCacheManager(Context context) {
        this.context = context;
        this.audioCache = new HashMap<>();
        this.memoryCache = new HashMap<>();

        // Initialize cache directory
        File filesDir = context.getFilesDir();
        this.cacheDirectory = new File(filesDir, CACHE_DIR);

        if (!cacheDirectory.exists()) {
            if (!cacheDirectory.mkdirs()) {
                Log.e(TAG, "Failed to create cache directory");
            }
        }

        // Load existing cache from disk
        loadCacheIndex();

        // Cleanup expired entries
        cleanupExpiredCache();
    }

    /**
     * Cache audio from raw resource
     */
    public boolean cacheAudio(String audioId, InputStream inputStream) {
        try {
            // Check file size
            long fileSize = inputStream.available();
            if (fileSize > INDIVIDUAL_FILE_MAX) {
                Log.w(TAG, "Audio file too large: " + audioId);
                return false;
            }

            // Make space if needed
            ensureCacheSpace(fileSize);

            // Create cache file
            File cacheFile = new File(cacheDirectory, sanitizeFileName(audioId) + ".cache");

            // Write to cache
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // Track in disk cache
            CachedAudio cached = new CachedAudio(audioId, cacheFile, fileSize);
            audioCache.put(audioId, cached);
            currentCacheSize += fileSize;

            Log.d(TAG, "Cached audio: " + audioId + " (" + fileSize + " bytes)");
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Error caching audio: " + audioId, e);
            return false;
        }
    }

    /**
     * Cache TTS audio output with language code
     */
    public boolean cacheTtsAudio(String text, String languageCode, byte[] audioData) {
        if (text == null || text.isEmpty() || audioData == null) {
            Log.w(TAG, "Invalid parameters for TTS caching");
            return false;
        }

        if (audioData.length > INDIVIDUAL_FILE_MAX) {
            Log.w(TAG, "TTS audio file too large: " + text);
            return false;
        }

        String cacheKey = generateTtsCacheKey(text, languageCode);

        try {
            ensureCacheSpace(audioData.length);

            File cacheFile = new File(cacheDirectory, cacheKey + ".wav");

            // Write to disk
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(audioData);
            }

            // Track in caches
            CachedAudio cached = new CachedAudio(cacheKey, cacheFile, audioData.length, languageCode);
            audioCache.put(cacheKey, cached);
            if (memoryCache.size() < MAX_MEMORY_ENTRIES) {
                memoryCache.put(cacheKey, cached);
            }
            currentCacheSize += audioData.length;

            Log.d(TAG, "TTS audio cached: " + text + " (" + audioData.length + " bytes)");
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Failed to cache TTS audio: " + text, e);
            return false;
        }
    }

    /**
     * Get cached TTS audio URI
     */
    public Uri getCachedTtsAudio(String text, String languageCode) {
        String cacheKey = generateTtsCacheKey(text, languageCode);

        // Check memory cache first
        if (memoryCache.containsKey(cacheKey)) {
            CachedAudio entry = memoryCache.get(cacheKey);
            if (!entry.isExpired() && entry.cacheFile.exists()) {
                entry.updateAccessTime();
                Log.d(TAG, "TTS cache hit (memory): " + text);
                return Uri.fromFile(entry.cacheFile);
            } else {
                memoryCache.remove(cacheKey);
            }
        }

        // Check disk cache
        CachedAudio cached = audioCache.get(cacheKey);
        if (cached != null && cached.cacheFile.exists()) {
            if (!cached.isExpired()) {
                cached.updateAccessTime();
                // Add to memory cache
                if (memoryCache.size() < MAX_MEMORY_ENTRIES) {
                    memoryCache.put(cacheKey, cached);
                }
                Log.d(TAG, "TTS cache hit (disk): " + text);
                return Uri.fromFile(cached.cacheFile);
            } else {
                // Remove expired entry
                audioCache.remove(cacheKey);
                if (cached.cacheFile.delete()) {
                    currentCacheSize -= cached.fileSize;
                }
            }
        }

        Log.d(TAG, "TTS cache miss: " + text);
        return null;
    }

    /**
     * Generate cache key for TTS audio
     */
    private String generateTtsCacheKey(String text, String languageCode) {
        String combined = text + "_" + languageCode;
        return Integer.toHexString(combined.hashCode());
    }

    /**
     * Play cached audio
     */
    public void playAudio(String audioId, AudioPlaybackCallback callback) {
        CachedAudio cached = audioCache.get(audioId);

        if (cached == null || !cached.cacheFile.exists()) {
            if (callback != null) {
                callback.onPlaybackError(audioId, "Audio not found in cache");
            }
            return;
        }

        // Update access stats
        cached.updateAccessTime();

        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(cached.cacheFile.getAbsolutePath());
            mediaPlayer.setOnPreparedListener(mp -> {
                if (callback != null) {
                    callback.onPlaybackStart(audioId);
                }
                mp.start();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (callback != null) {
                    callback.onPlaybackComplete(audioId);
                }
                mp.release();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                if (callback != null) {
                    callback.onPlaybackError(audioId, "Playback error: " + what);
                }
                return true;
            });
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            Log.e(TAG, "Error playing cached audio: " + audioId, e);
            if (callback != null) {
                callback.onPlaybackError(audioId, e.getMessage());
            }
        }
    }

    /**
     * Check if audio is cached
     */
    public boolean isCached(String audioId) {
        CachedAudio cached = audioCache.get(audioId);
        return cached != null && cached.cacheFile.exists();
    }

    /**
     * Clear specific cached audio
     */
    public void clearAudio(String audioId) {
        CachedAudio cached = audioCache.remove(audioId);
        memoryCache.remove(audioId);
        if (cached != null) {
            if (cached.cacheFile.delete()) {
                currentCacheSize -= cached.fileSize;
                Log.d(TAG, "Cleared cached audio: " + audioId);
            }
        }
    }

    /**
     * Clear all cache
     */
    public void clearAllCache() {
        for (CachedAudio cached : audioCache.values()) {
            cached.cacheFile.delete();
        }
        audioCache.clear();
        memoryCache.clear();
        currentCacheSize = 0;
        Log.d(TAG, "Cleared all cache");
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_size_bytes", currentCacheSize);
        stats.put("total_size_mb", currentCacheSize / (1024.0 * 1024.0));
        stats.put("max_size_mb", MAX_CACHE_SIZE / (1024.0 * 1024.0));
        stats.put("file_count", audioCache.size());
        stats.put("memory_cache_size", memoryCache.size());
        stats.put("usage_percent", (currentCacheSize * 100) / MAX_CACHE_SIZE);
        stats.put("cache_ttl_days", CACHE_ENTRY_TTL / (24 * 60 * 60 * 1000));
        return stats;
    }

    /**
     * Cleanup expired cache entries
     */
    private void cleanupExpiredCache() {
        int removed = 0;
        java.util.Iterator<Map.Entry<String, CachedAudio>> iterator = audioCache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, CachedAudio> entry = iterator.next();
            CachedAudio cached = entry.getValue();

            if (cached.isExpired()) {
                if (cached.cacheFile.delete()) {
                    currentCacheSize -= cached.fileSize;
                    memoryCache.remove(entry.getKey());
                    iterator.remove();
                    removed++;
                }
            }
        }

        if (removed > 0) {
            Log.d(TAG, "Cleaned up " + removed + " expired cache entries");
        }
    }

    // ============ PRIVATE METHODS ============

    /**
     * Ensure sufficient cache space using LRU eviction
     */
    private void ensureCacheSpace(long requiredSize) {
        while (currentCacheSize + requiredSize > MAX_CACHE_SIZE && !audioCache.isEmpty()) {
            // Find least recently used
            CachedAudio lru = null;
            long oldestTime = Long.MAX_VALUE;

            for (CachedAudio cached : audioCache.values()) {
                if (cached.lastAccessTime < oldestTime) {
                    oldestTime = cached.lastAccessTime;
                    lru = cached;
                }
            }

            if (lru != null) {
                clearAudio(lru.audioId);
            }
        }
    }

    /**
     * Sanitize file name for safe file system usage
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    /**
     * Load cache index from disk
     */
    private void loadCacheIndex() {
        if (!cacheDirectory.exists() || !cacheDirectory.isDirectory()) {
            return;
        }

        File[] files = cacheDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".cache")) {
                    String audioId = file.getName()
                        .replaceAll("\\.cache$", "");
                    long fileSize = file.length();

                    CachedAudio cached = new CachedAudio(audioId, file, fileSize);
                    audioCache.put(audioId, cached);
                    currentCacheSize += fileSize;
                }
            }
        }

        Log.d(TAG, "Loaded cache index: " + audioCache.size() + " files, " + currentCacheSize + " bytes");
    }

    /**
     * Cleanup resources
     */
    public void release() {
        // Cleanup if needed
        Log.d(TAG, "AudioCacheManager released");
    }
}

