package com.edulinguaghana;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

public class AvatarClothingFragment extends Fragment {

    private RecyclerView rvClothingStyle, rvClothingColor;
    private AvatarSelectionAdapter styleAdapter, colorAdapter;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_clothing, container, false);

        activity = (AvatarEditorActivity) getActivity();
        rvClothingStyle = view.findViewById(R.id.rvClothingStyle);
        rvClothingColor = view.findViewById(R.id.rvClothingColor);

        setupRecyclerViews();
        updateUIFromConfig();

        return view;
    }

    private void setupRecyclerViews() {
        // Clothing Style
        String[] clothingStyles = {"T-Shirt", "Hoodie", "Dress", "Suit", "Casual", "Traditional"};
        styleAdapter = new AvatarSelectionAdapter(Arrays.asList(clothingStyles),
            activity.getAvatarConfig().clothingStyle.ordinal(),
            position -> {
                activity.getAvatarConfig().clothingStyle = AvatarBuilder.ClothingStyle.values()[position];
                activity.updateAvatar();
            });
        rvClothingStyle.setAdapter(styleAdapter);

        // Clothing Color
        String[] clothingColors = {"Red", "Blue", "Green", "Yellow", "Purple", "Orange", "Pink", "Black", "White"};
        colorAdapter = new AvatarSelectionAdapter(Arrays.asList(clothingColors),
            activity.getAvatarConfig().clothingColor.ordinal(),
            position -> {
                activity.getAvatarConfig().clothingColor = AvatarBuilder.ClothingColor.values()[position];
                activity.updateAvatar();
            });
        rvClothingColor.setAdapter(colorAdapter);
    }

    public void updateUIFromConfig() {
        if (styleAdapter == null) return;
        styleAdapter.setSelectedPosition(activity.getAvatarConfig().clothingStyle.ordinal());
        colorAdapter.setSelectedPosition(activity.getAvatarConfig().clothingColor.ordinal());
    }
}
