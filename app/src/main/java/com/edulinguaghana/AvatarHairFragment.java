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

public class AvatarHairFragment extends Fragment {

    private RecyclerView rvHairStyle, rvHairColor;
    private AvatarSelectionAdapter hairStyleAdapter, hairColorAdapter;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_hair, container, false);

        activity = (AvatarEditorActivity) getActivity();
        rvHairStyle = view.findViewById(R.id.rvHairStyle);
        rvHairColor = view.findViewById(R.id.rvHairColor);

        setupRecyclerViews();
        updateUIFromConfig();

        return view;
    }

    private void setupRecyclerViews() {
        // Hair Style
        String[] hairStyles = {"Short", "Long", "Curly", "Bald", "Afro", "Braids", "Ponytail",
            "Dreadlocks", "Mohawk", "Bun", "Side Part"};
        hairStyleAdapter = new AvatarSelectionAdapter(Arrays.asList(hairStyles), 
            activity.getAvatarConfig().hairStyle.ordinal(), 
            position -> {
                activity.getAvatarConfig().hairStyle = AvatarBuilder.HairStyle.values()[position];
                activity.updateAvatar();
            });
        rvHairStyle.setAdapter(hairStyleAdapter);

        // Hair Color
        String[] hairColors = {"Black", "Brown", "Blonde", "Red", "Gray", "Purple", "Blue", "Pink"};
        hairColorAdapter = new AvatarSelectionAdapter(Arrays.asList(hairColors), 
            activity.getAvatarConfig().hairColor.ordinal(), 
            position -> {
                activity.getAvatarConfig().hairColor = AvatarBuilder.HairColor.values()[position];
                activity.updateAvatar();
            });
        rvHairColor.setAdapter(hairColorAdapter);
    }

    public void updateUIFromConfig() {
        if (hairStyleAdapter == null) return;
        hairStyleAdapter.setSelectedPosition(activity.getAvatarConfig().hairStyle.ordinal());
        hairColorAdapter.setSelectedPosition(activity.getAvatarConfig().hairColor.ordinal());
    }
}
