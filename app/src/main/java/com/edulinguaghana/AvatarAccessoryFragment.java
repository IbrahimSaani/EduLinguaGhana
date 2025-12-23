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

public class AvatarAccessoryFragment extends Fragment {

    private Spinner spinnerAccessory;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_accessory, container, false);

        activity = (AvatarEditorActivity) getActivity();
        spinnerAccessory = view.findViewById(R.id.spinnerAccessory);

        setupSpinners();
        setupListeners();
        updateUIFromConfig();

        return view;
    }

    private void setupSpinners() {
        // Accessory
        String[] accessories = {"None", "Hat", "Crown", "Headband", "Earrings", "Necklace", "Bow Tie", "Scarf", "Flower", "Mask"};
        ArrayAdapter<String> accessoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, accessories);
        accessoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccessory.setAdapter(accessoryAdapter);
    }

    private void setupListeners() {
        // Accessory
        spinnerAccessory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activity.getAvatarConfig().accessory = AvatarBuilder.Accessory.values()[position];
                activity.updateAvatar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void updateUIFromConfig() {
        if (spinnerAccessory == null) return;
        spinnerAccessory.setSelection(activity.getAvatarConfig().accessory.ordinal());
    }
}
