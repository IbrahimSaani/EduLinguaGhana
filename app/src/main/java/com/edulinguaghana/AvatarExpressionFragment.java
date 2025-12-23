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

public class AvatarExpressionFragment extends Fragment {

    private Spinner spinnerExpression;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_expression, container, false);

        activity = (AvatarEditorActivity) getActivity();
        spinnerExpression = view.findViewById(R.id.spinnerExpression);

        setupSpinners();
        setupListeners();
        updateUIFromConfig();

        return view;
    }

    private void setupSpinners() {
        // Facial Expression
        String[] expressions = {"Neutral", "Happy", "Excited", "Cool", "Surprised", "Shy"};
        ArrayAdapter<String> expressionAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, expressions);
        expressionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpression.setAdapter(expressionAdapter);
    }

    private void setupListeners() {
        // Facial Expression
        spinnerExpression.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activity.getAvatarConfig().facialExpression = AvatarBuilder.FacialExpression.values()[position];
                activity.updateAvatar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void updateUIFromConfig() {
        if (spinnerExpression == null) return;
        spinnerExpression.setSelection(activity.getAvatarConfig().facialExpression.ordinal());
    }
}
