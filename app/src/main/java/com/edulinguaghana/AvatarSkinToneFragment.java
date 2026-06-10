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

public class AvatarSkinToneFragment extends Fragment {

    private RecyclerView rvSkinTone;
    private AvatarSelectionAdapter skinToneAdapter;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_skin_tone, container, false);

        activity = (AvatarEditorActivity) getActivity();
        rvSkinTone = view.findViewById(R.id.rvSkinTone);

        setupRecyclerView();
        updateUIFromConfig();

        return view;
    }

    private void setupRecyclerView() {
        String[] skinTones = {"Light", "Medium", "Tan", "Brown", "Dark"};
        String[] skinIcons = {"🏻", "🏼", "🏽", "🏾", "🏿"};
        
        skinToneAdapter = new AvatarSelectionAdapter(
            Arrays.asList(skinTones), 
            Arrays.asList(skinIcons),
            activity.getAvatarConfig().skinTone.ordinal(), 
            position -> {
                activity.getAvatarConfig().skinTone = AvatarBuilder.SkinTone.values()[position];
                activity.updateAvatar();
            });
        rvSkinTone.setAdapter(skinToneAdapter);
    }

    public void updateUIFromConfig() {
        if (skinToneAdapter == null) return;
        skinToneAdapter.setSelectedPosition(activity.getAvatarConfig().skinTone.ordinal());
    }
}
