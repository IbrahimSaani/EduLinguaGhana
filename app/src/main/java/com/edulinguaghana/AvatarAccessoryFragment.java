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

public class AvatarAccessoryFragment extends Fragment {

    private RecyclerView rvAccessory;
    private AvatarSelectionAdapter adapter;
    private AvatarEditorActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_avatar_accessory, container, false);

        activity = (AvatarEditorActivity) getActivity();
        rvAccessory = view.findViewById(R.id.rvAccessory);

        setupRecyclerView();
        updateUIFromConfig();

        return view;
    }

    private void setupRecyclerView() {
        // Accessory
        String[] accessories = {"None", "Hat", "Crown", "Headband", "Earrings", "Necklace", "Bow Tie", "Scarf", "Flower", "Mask"};
        adapter = new AvatarSelectionAdapter(Arrays.asList(accessories),
            activity.getAvatarConfig().accessory.ordinal(),
            position -> {
                activity.getAvatarConfig().accessory = AvatarBuilder.Accessory.values()[position];
                activity.updateAvatar();
            });
        rvAccessory.setAdapter(adapter);
    }

    public void updateUIFromConfig() {
        if (adapter == null) return;
        adapter.setSelectedPosition(activity.getAvatarConfig().accessory.ordinal());
    }
}
