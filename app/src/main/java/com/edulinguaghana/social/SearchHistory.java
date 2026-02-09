package com.edulinguaghana.social;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages search history for friend searches
 */
public class SearchHistory {
    private static final String PREFS_NAME = "friend_search_history";
    private static final String KEY_EMAIL_HISTORY = "email_history";
    private static final String KEY_UID_HISTORY = "uid_history";
    private static final String KEY_USERNAME_HISTORY = "username_history";
    private static final int MAX_HISTORY_SIZE = 10;

    private final SharedPreferences prefs;

    public SearchHistory(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void addEmailSearch(String email) {
        addToHistory(KEY_EMAIL_HISTORY, email);
    }

    public void addUidSearch(String uid) {
        addToHistory(KEY_UID_HISTORY, uid);
    }

    public void addUsernameSearch(String username) {
        addToHistory(KEY_USERNAME_HISTORY, username);
    }

    public List<String> getEmailHistory() {
        return getHistory(KEY_EMAIL_HISTORY);
    }

    public List<String> getUidHistory() {
        return getHistory(KEY_UID_HISTORY);
    }

    public List<String> getUsernameHistory() {
        return getHistory(KEY_USERNAME_HISTORY);
    }

    public void clearAll() {
        prefs.edit()
            .remove(KEY_EMAIL_HISTORY)
            .remove(KEY_UID_HISTORY)
            .remove(KEY_USERNAME_HISTORY)
            .apply();
    }

    private void addToHistory(String key, String value) {
        if (value == null || value.trim().isEmpty()) return;

        Set<String> history = prefs.getStringSet(key, new HashSet<>());
        List<String> historyList = new ArrayList<>(history);

        // Remove if already exists
        historyList.remove(value);

        // Add to beginning
        historyList.add(0, value);

        // Limit size
        if (historyList.size() > MAX_HISTORY_SIZE) {
            historyList = historyList.subList(0, MAX_HISTORY_SIZE);
        }

        prefs.edit().putStringSet(key, new HashSet<>(historyList)).apply();
    }

    private List<String> getHistory(String key) {
        Set<String> history = prefs.getStringSet(key, new HashSet<>());
        return new ArrayList<>(history);
    }
}

