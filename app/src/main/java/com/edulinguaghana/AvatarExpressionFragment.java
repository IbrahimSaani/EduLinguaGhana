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
        adapter = new AvatarSelectionAdapter(Arrays.asList(expressions),
            activity.getAvatarConfig().facialExpression.ordinal(),
            position -> {
                activity.getAvatarConfig().facialExpression = AvatarBuilder.FacialExpression.values()[position];
                activity.updateAvatar();
            });
        rvExpression.setAdapter(adapter);
    }

    public void updateUIFromConfig() {
        if (adapter == null) return;
        adapter.setSelectedPosition(activity.getAvatarConfig().facialExpression.ordinal());
    }
}
