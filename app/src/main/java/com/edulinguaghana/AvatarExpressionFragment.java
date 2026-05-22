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

public class AvatarExpressionFragment extends Fragment {

    private RecyclerView rvExpression;
    private AvatarSelectionAdapter adapter;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_expression, container, false);

        activity = (AvatarEditorActivity) getActivity();
        rvExpression = view.findViewById(R.id.rvExpression);

        setupRecyclerView();
        updateUIFromConfig();

        return view;
    }

    private void setupRecyclerView() {
        // Facial Expression
        String[] expressions = {"Neutral", "Happy", "Excited", "Cool", "Surprised", "Shy"};
        String[] icons = {"😐", "😊", "🤩", "😎", "😲", "😊"};
        
        adapter = new AvatarSelectionAdapter(
            Arrays.asList(expressions),
            Arrays.asList(icons),
            activity.getAvatarConfig().facialExpression.ordinal(),
            position -> {
                AvatarBuilder.FacialExpression expression = AvatarBuilder.FacialExpression.values()[position];
                activity.getAvatarConfig().facialExpression = expression;
                
                // When an expression is selected, it acts as a preset for eyes and mouth
                switch (expression) {
                    case HAPPY:
                        activity.getAvatarConfig().eyeStyle = AvatarBuilder.EyeStyle.HAPPY;
                        activity.getAvatarConfig().mouthStyle = AvatarBuilder.MouthStyle.SMILE;
                        break;
                    case EXCITED:
                        activity.getAvatarConfig().eyeStyle = AvatarBuilder.EyeStyle.STARRY;
                        activity.getAvatarConfig().mouthStyle = AvatarBuilder.MouthStyle.LAUGH;
                        break;
                    case COOL:
                        activity.getAvatarConfig().eyeStyle = AvatarBuilder.EyeStyle.SUNGLASSES;
                        activity.getAvatarConfig().mouthStyle = AvatarBuilder.MouthStyle.SMIRK;
                        break;
                    case SURPRISED:
                        activity.getAvatarConfig().eyeStyle = AvatarBuilder.EyeStyle.NORMAL;
                        activity.getAvatarConfig().mouthStyle = AvatarBuilder.MouthStyle.SURPRISED;
                        break;
                    case SHY:
                        activity.getAvatarConfig().eyeStyle = AvatarBuilder.EyeStyle.NORMAL;
                        activity.getAvatarConfig().mouthStyle = AvatarBuilder.MouthStyle.SMILE;
                        break;
                    case NEUTRAL:
                        activity.getAvatarConfig().eyeStyle = AvatarBuilder.EyeStyle.NORMAL;
                        activity.getAvatarConfig().mouthStyle = AvatarBuilder.MouthStyle.NEUTRAL;
                        break;
                }

                activity.updateAvatar();
            });
        rvExpression.setAdapter(adapter);
    }

    public void updateUIFromConfig() {
        if (adapter == null) return;
        adapter.setSelectedPosition(activity.getAvatarConfig().facialExpression.ordinal());
    }
}
