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

public class AvatarClothingFragment extends Fragment {

    private Spinner spinnerClothingStyle, spinnerClothingColor;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_clothing, container, false);

        activity = (AvatarEditorActivity) getActivity();
        spinnerClothingStyle = view.findViewById(R.id.spinnerClothingStyle);
        spinnerClothingColor = view.findViewById(R.id.spinnerClothingColor);

        setupSpinners();
        setupListeners();
        updateUIFromConfig();

        return view;
    }

    private void setupSpinners() {
        // Clothing Style
        String[] clothingStyles = {"T-Shirt", "Hoodie", "Dress", "Suit", "Casual", "Traditional"};
        ArrayAdapter<String> clothingStyleAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, clothingStyles);
        clothingStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClothingStyle.setAdapter(clothingStyleAdapter);

        // Clothing Color
        String[] clothingColors = {"Red", "Blue", "Green", "Yellow", "Purple", "Orange", "Pink", "Black", "White"};
        ArrayAdapter<String> clothingColorAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, clothingColors);
        clothingColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClothingColor.setAdapter(clothingColorAdapter);
    }

    private void setupListeners() {
        // Clothing Style
        spinnerClothingStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activity.getAvatarConfig().clothingStyle = AvatarBuilder.ClothingStyle.values()[position];
                activity.updateAvatar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Clothing Color
        spinnerClothingColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activity.getAvatarConfig().clothingColor = AvatarBuilder.ClothingColor.values()[position];
                activity.updateAvatar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void updateUIFromConfig() {
        if (spinnerClothingStyle == null) return;
        spinnerClothingStyle.setSelection(activity.getAvatarConfig().clothingStyle.ordinal());
        spinnerClothingColor.setSelection(activity.getAvatarConfig().clothingColor.ordinal());
    }
}
