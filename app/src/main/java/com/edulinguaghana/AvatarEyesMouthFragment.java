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

public class AvatarEyesMouthFragment extends Fragment {

    private RecyclerView rvEyeStyle, rvMouthStyle;
    private AvatarSelectionAdapter eyeStyleAdapter, mouthStyleAdapter;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_eyes_mouth, container, false);

        activity = (AvatarEditorActivity) getActivity();
        rvEyeStyle = view.findViewById(R.id.rvEyeStyle);
        rvMouthStyle = view.findViewById(R.id.rvMouthStyle);

        setupRecyclerViews();
        updateUIFromConfig();

        return view;
    }

    private void setupRecyclerViews() {
        // Eye Style
        String[] eyeStyles = {"Normal", "Happy", "Wink", "Glasses", "Sunglasses", "Starry", "Sleepy", "Heart"};
        eyeStyleAdapter = new AvatarSelectionAdapter(Arrays.asList(eyeStyles),
            activity.getAvatarConfig().eyeStyle.ordinal(),
            position -> {
                activity.getAvatarConfig().eyeStyle = AvatarBuilder.EyeStyle.values()[position];
                activity.updateAvatar();
            });
        rvEyeStyle.setAdapter(eyeStyleAdapter);

        // Mouth Style
        String[] mouthStyles = {"Smile", "Laugh", "Neutral", "Smirk", "Surprised", "Tongue Out", "Whistling"};
        mouthStyleAdapter = new AvatarSelectionAdapter(Arrays.asList(mouthStyles),
            activity.getAvatarConfig().mouthStyle.ordinal(),
            position -> {
                activity.getAvatarConfig().mouthStyle = AvatarBuilder.MouthStyle.values()[position];
                activity.updateAvatar();
            });
        rvMouthStyle.setAdapter(mouthStyleAdapter);
    }

    public void updateUIFromConfig() {
        if (eyeStyleAdapter == null) return;
        eyeStyleAdapter.setSelectedPosition(activity.getAvatarConfig().eyeStyle.ordinal());
        mouthStyleAdapter.setSelectedPosition(activity.getAvatarConfig().mouthStyle.ordinal());
    }
}
