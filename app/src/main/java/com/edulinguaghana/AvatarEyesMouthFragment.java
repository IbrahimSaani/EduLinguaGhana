package com.edulinguaghana;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AvatarEyesMouthFragment extends Fragment {

    private Spinner spinnerEyeStyle, spinnerMouthStyle;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_eyes_mouth, container, false);

        activity = (AvatarEditorActivity) getActivity();
        spinnerEyeStyle = view.findViewById(R.id.spinnerEyeStyle);
        spinnerMouthStyle = view.findViewById(R.id.spinnerMouthStyle);

        setupSpinners();
        setupListeners();
        updateUIFromConfig();

        return view;
    }

    private void setupSpinners() {
        // Eye Style
        String[] eyeStyles = {"Normal", "Happy", "Wink", "Glasses", "Sunglasses", "Starry", "Sleepy", "Heart"};
        ArrayAdapter<String> eyeStyleAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, eyeStyles);
        eyeStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEyeStyle.setAdapter(eyeStyleAdapter);

        // Mouth Style
        String[] mouthStyles = {"Smile", "Laugh", "Neutral", "Smirk", "Surprised", "Tongue Out", "Whistling"};
        ArrayAdapter<String> mouthStyleAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, mouthStyles);
        mouthStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMouthStyle.setAdapter(mouthStyleAdapter);
    }

    private void setupListeners() {
        // Eye Style
        spinnerEyeStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activity.getAvatarConfig().eyeStyle = AvatarBuilder.EyeStyle.values()[position];
                activity.updateAvatar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Mouth Style
        spinnerMouthStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activity.getAvatarConfig().mouthStyle = AvatarBuilder.MouthStyle.values()[position];
                activity.updateAvatar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void updateUIFromConfig() {
        if (spinnerEyeStyle == null) return;
        spinnerEyeStyle.setSelection(activity.getAvatarConfig().eyeStyle.ordinal());
        spinnerMouthStyle.setSelection(activity.getAvatarConfig().mouthStyle.ordinal());
    }
}
