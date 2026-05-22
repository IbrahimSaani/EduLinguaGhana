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
        String[] eyeIcons = {"👀", "😊", "😉", "👓", "😎", "🤩", "😴", "😍"};
        
        eyeStyleAdapter = new AvatarSelectionAdapter(
            Arrays.asList(eyeStyles),
            Arrays.asList(eyeIcons),
            activity.getAvatarConfig().eyeStyle.ordinal(),
            position -> {
                activity.getAvatarConfig().eyeStyle = AvatarBuilder.EyeStyle.values()[position];
                // Reset expression to neutral when manual change is made
                activity.getAvatarConfig().facialExpression = AvatarBuilder.FacialExpression.NEUTRAL;
                activity.updateAvatar();
            });
        rvEyeStyle.setAdapter(eyeStyleAdapter);

        // Mouth Style
        String[] mouthStyles = {"Smile", "Laugh", "Neutral", "Smirk", "Surprised", "Tongue Out", "Whistling"};
        String[] mouthIcons = {"😊", "😄", "😐", "😏", "😲", "😛", "😙"};
        
        mouthStyleAdapter = new AvatarSelectionAdapter(
            Arrays.asList(mouthStyles),
            Arrays.asList(mouthIcons),
            activity.getAvatarConfig().mouthStyle.ordinal(),
            position -> {
                activity.getAvatarConfig().mouthStyle = AvatarBuilder.MouthStyle.values()[position];
                // Reset expression to neutral when manual change is made
                activity.getAvatarConfig().facialExpression = AvatarBuilder.FacialExpression.NEUTRAL;
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
