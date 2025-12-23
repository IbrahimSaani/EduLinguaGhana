package com.edulinguaghana;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

public class AvatarSkinToneFragment extends Fragment {

    private RadioGroup rgSkinTone;
    private MaterialCardView cardSkinLight, cardSkinMedium, cardSkinTan, cardSkinBrown, cardSkinDark;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_skin_tone, container, false);

        activity = (AvatarEditorActivity) getActivity();
        rgSkinTone = view.findViewById(R.id.rgSkinTone);
        cardSkinLight = view.findViewById(R.id.cardSkinLight);
        cardSkinMedium = view.findViewById(R.id.cardSkinMedium);
        cardSkinTan = view.findViewById(R.id.cardSkinTan);
        cardSkinBrown = view.findViewById(R.id.cardSkinBrown);
        cardSkinDark = view.findViewById(R.id.cardSkinDark);

        setupSkinToneCardClicks();
        setupListeners();
        updateUIFromConfig();

        return view;
    }

    private void setupSkinToneCardClicks() {
        cardSkinLight.setOnClickListener(v -> rgSkinTone.check(R.id.rbSkinLight));
        cardSkinMedium.setOnClickListener(v -> rgSkinTone.check(R.id.rbSkinMedium));
        cardSkinTan.setOnClickListener(v -> rgSkinTone.check(R.id.rbSkinTan));
        cardSkinBrown.setOnClickListener(v -> rgSkinTone.check(R.id.rbSkinBrown));
        cardSkinDark.setOnClickListener(v -> rgSkinTone.check(R.id.rbSkinDark));
    }

    private void setupListeners() {
        rgSkinTone.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSkinLight) {
                activity.getAvatarConfig().skinTone = AvatarBuilder.SkinTone.LIGHT;
                updateSkinToneCardSelection(cardSkinLight);
            } else if (checkedId == R.id.rbSkinMedium) {
                activity.getAvatarConfig().skinTone = AvatarBuilder.SkinTone.MEDIUM;
                updateSkinToneCardSelection(cardSkinMedium);
            } else if (checkedId == R.id.rbSkinTan) {
                activity.getAvatarConfig().skinTone = AvatarBuilder.SkinTone.TAN;
                updateSkinToneCardSelection(cardSkinTan);
            } else if (checkedId == R.id.rbSkinBrown) {
                activity.getAvatarConfig().skinTone = AvatarBuilder.SkinTone.BROWN;
                updateSkinToneCardSelection(cardSkinBrown);
            } else if (checkedId == R.id.rbSkinDark) {
                activity.getAvatarConfig().skinTone = AvatarBuilder.SkinTone.DARK;
                updateSkinToneCardSelection(cardSkinDark);
            }
            activity.updateAvatar();
        });
    }

    private void updateSkinToneCardSelection(MaterialCardView selectedCard) {
        // Reset all cards
        cardSkinLight.setStrokeWidth(0);
        cardSkinMedium.setStrokeWidth(0);
        cardSkinTan.setStrokeWidth(0);
        cardSkinBrown.setStrokeWidth(0);
        cardSkinDark.setStrokeWidth(0);

        // Highlight selected card
        selectedCard.setStrokeWidth(8);
        selectedCard.setStrokeColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }

    public void updateUIFromConfig() {
        if (rgSkinTone == null) return;
        // Update skin tone radio buttons
        switch (activity.getAvatarConfig().skinTone) {
            case LIGHT:
                rgSkinTone.check(R.id.rbSkinLight);
                updateSkinToneCardSelection(cardSkinLight);
                break;
            case MEDIUM:
                rgSkinTone.check(R.id.rbSkinMedium);
                updateSkinToneCardSelection(cardSkinMedium);
                break;
            case TAN:
                rgSkinTone.check(R.id.rbSkinTan);
                updateSkinToneCardSelection(cardSkinTan);
                break;
            case BROWN:
                rgSkinTone.check(R.id.rbSkinBrown);
                updateSkinToneCardSelection(cardSkinBrown);
                break;
            case DARK:
                rgSkinTone.check(R.id.rbSkinDark);
                updateSkinToneCardSelection(cardSkinDark);
                break;
        }
    }
}
