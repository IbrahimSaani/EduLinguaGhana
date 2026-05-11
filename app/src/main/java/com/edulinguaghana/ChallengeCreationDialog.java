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
        String[] languages = {"English", "French", "Twi", "Ewe", "Ga"};
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_spinner_item, languages);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(languageAdapter);

        // Setup quiz type spinner
        String[] quizTypes = {"Letter Quiz", "Number Sequencing", "Matching", "Mixed Mode"};
        ArrayAdapter<String> quizAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_spinner_item, quizTypes);
        quizAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quizTypeSpinner.setAdapter(quizAdapter);

        // Setup duration spinner
        String[] durations = {"30 seconds", "1 minute", "2 minutes", "3 minutes", "5 minutes"};
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(context,
            android.R.layout.simple_spinner_item, durations);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(durationAdapter);
        durationSpinner.setSelection(1);  // Default to 1 minute

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
            case "English":
                return "en";
            case "French":
                return "fr";
            case "Twi":
                return "ak";
            case "Ewe":
                return "ee";
            case "Ga":
                return "gaa";
            default:
                return "ak";
        }
    }

    private Long parseDuration(String durationText) {
        switch (durationText) {
            case "30 seconds":
                return 30L; // Interpreted as seconds
            case "1 minute":
                return 60L;
            case "2 minutes":
                return 120L;
            case "3 minutes":
                return 180L;
            case "5 minutes":
                return 300L;
            default:
                return 60L;
        }
    }
}

