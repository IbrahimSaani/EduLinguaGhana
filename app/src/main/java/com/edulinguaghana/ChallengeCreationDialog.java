package com.edulinguaghana;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Dialog for creating a challenge with language, quiz type, and duration selection
 */
public class ChallengeCreationDialog {
    private Context context;
    private String userId;
    private String targetUserId;
    private OnChallengeCreatedListener listener;

    public interface OnChallengeCreatedListener {
        void onChallengeCreated(String language, String quizType, Long durationMinutes, String targetUserId);
    }

    public ChallengeCreationDialog(Context context, String userId, String targetUserId,
                                  OnChallengeCreatedListener listener) {
        this.context = context;
        this.userId = userId;
        this.targetUserId = targetUserId;
        this.listener = listener;
    }

    public void show() {
        // Create custom view
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_challenge_creation, null);

        // Get UI elements
        Spinner languageSpinner = dialogView.findViewById(R.id.spinnerLanguage);
        Spinner quizTypeSpinner = dialogView.findViewById(R.id.spinnerQuizType);
        Spinner durationSpinner = dialogView.findViewById(R.id.spinnerDuration);

        // Setup language spinner
        String[] languages = {"Twi", "Ewe", "Ga"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);

        // Setup quiz type spinner
        String[] quizTypes = {"Vocabulary", "Grammar", "Phrases", "Numbers"};
        ArrayAdapter<String> quizAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_spinner_item, quizTypes);
        quizAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quizTypeSpinner.setAdapter(quizAdapter);

        // Setup duration spinner
        String[] durations = {"5 minutes", "10 minutes", "15 minutes", "20 minutes", "30 minutes"};
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_spinner_item, durations);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(durationAdapter);
        durationSpinner.setSelection(1);  // Default to 10 minutes

        StyledMenuHelper.showStyledCustomDialog(
            context,
            "⚔️",
            "Create Challenge",
            "Choose settings for your challenge",
            dialogView,
            "Create Challenge",
            "Cancel",
            () -> {
                String language = getLanguageCode((String) languageSpinner.getSelectedItem());
                String quizType = (String) quizTypeSpinner.getSelectedItem();
                Long duration = parseDuration((String) durationSpinner.getSelectedItem());

                if (listener != null) {
                    listener.onChallengeCreated(language, quizType, duration, targetUserId);
                }
            },
            null
        );
    }

    private String getLanguageCode(String languageName) {
        switch (languageName) {
            case "Twi":
                return "tw";
            case "Ewe":
                return "ee";
            case "Ga":
                return "ga";
            default:
                return "tw";
        }
    }

    private Long parseDuration(String durationText) {
        switch (durationText) {
            case "5 minutes":
                return 5L;
            case "10 minutes":
                return 10L;
            case "15 minutes":
                return 15L;
            case "20 minutes":
                return 20L;
            case "30 minutes":
                return 30L;
            default:
                return 10L;
        }
    }
}

