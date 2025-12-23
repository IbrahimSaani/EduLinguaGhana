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

public class AvatarHairFragment extends Fragment {

    private Spinner spinnerHairStyle, spinnerHairColor;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_hair, container, false);

        activity = (AvatarEditorActivity) getActivity();
        spinnerHairStyle = view.findViewById(R.id.spinnerHairStyle);
        spinnerHairColor = view.findViewById(R.id.spinnerHairColor);

        setupSpinners();
        setupListeners();
        updateUIFromConfig();

        return view;
    }

    private void setupSpinners() {
        // Hair Style
        String[] hairStyles = {"Short", "Long", "Curly", "Bald", "Afro", "Braids", "Ponytail",
            "Dreadlocks", "Mohawk", "Bun", "Side Part"};
        ArrayAdapter<String> hairStyleAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, hairStyles);
        hairStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHairStyle.setAdapter(hairStyleAdapter);

        // Hair Color
        String[] hairColors = {"Black", "Brown", "Blonde", "Red", "Gray", "Purple", "Blue", "Pink"};
        ArrayAdapter<String> hairColorAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, hairColors);
        hairColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHairColor.setAdapter(hairColorAdapter);
    }

    private void setupListeners() {
        // Hair Style
        spinnerHairStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activity.getAvatarConfig().hairStyle = AvatarBuilder.HairStyle.values()[position];
                activity.updateAvatar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Hair Color
        spinnerHairColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activity.getAvatarConfig().hairColor = AvatarBuilder.HairColor.values()[position];
                activity.updateAvatar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void updateUIFromConfig() {
        if (spinnerHairStyle == null) return;
        spinnerHairStyle.setSelection(activity.getAvatarConfig().hairStyle.ordinal());
        spinnerHairColor.setSelection(activity.getAvatarConfig().hairColor.ordinal());
    }
}
