package com.edulinguaghana;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edulinguaghana.gamification.Badge;
import com.edulinguaghana.gamification.BadgeManager;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BadgesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_badges, container, false);

        RecyclerView rv = root.findViewById(R.id.rv_badges);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Get all badges
        List<Badge> badges = BadgeManager.getAllBadges(getContext());

        List<SimpleListAdapter.Item> items = new ArrayList<>();
        int unlockedCount = 0;

        for (Badge b : badges) {
            int icon = b.unlocked ? R.drawable.ic_badge_unlocked : R.drawable.ic_badge_locked;
            items.add(new SimpleListAdapter.Item(icon, b.title, b.description));
            if (b.unlocked) {
                unlockedCount++;
            }
        }

        SimpleListAdapter adapter = new SimpleListAdapter(items);
        rv.setAdapter(adapter);

        // Update progress display
        updateProgressDisplay(root, unlockedCount, badges.size());

        return root;
    }

    private void updateProgressDisplay(View root, int unlockedCount, int totalCount) {
        try {
            // Update progress text (e.g., "8/12")
            TextView tvProgress = root.findViewById(R.id.tv_badge_progress);
            if (tvProgress != null) {
                tvProgress.setText(unlockedCount + "/" + totalCount);
            }

            // Update progress bar
            LinearProgressIndicator progressBar = root.findViewById(R.id.progress_badges);
            if (progressBar != null) {
                int progressPercentage = totalCount > 0 ? (unlockedCount * 100) / totalCount : 0;
                progressBar.setProgress(progressPercentage);
            }

            // Update unlocked count
            TextView tvUnlockedCount = root.findViewById(R.id.tv_unlocked_count);
            if (tvUnlockedCount != null) {
                tvUnlockedCount.setText(String.valueOf(unlockedCount));
            }

            // Update locked count
            TextView tvLockedCount = root.findViewById(R.id.tv_locked_count);
            if (tvLockedCount != null) {
                tvLockedCount.setText(String.valueOf(totalCount - unlockedCount));
            }

            // Update percentage
            TextView tvPercentage = root.findViewById(R.id.tv_completion_percentage);
            if (tvPercentage != null) {
                int percentage = totalCount > 0 ? (unlockedCount * 100) / totalCount : 0;
                tvPercentage.setText(percentage + "%");
            }
        } catch (Exception e) {
            android.util.Log.e("BadgesFragment", "Error updating progress display", e);
        }
    }
}
