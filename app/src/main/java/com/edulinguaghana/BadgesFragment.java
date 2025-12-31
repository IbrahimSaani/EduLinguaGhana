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

import java.util.ArrayList;
import java.util.List;

public class BadgesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_badges, container, false);

        RecyclerView rv = root.findViewById(R.id.rv_badges);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<SimpleListAdapter.Item> items = new ArrayList<>();
        List<Badge> badges = BadgeManager.getAllBadges(getContext());
        for (Badge b : badges) {
            int icon = b.unlocked ? R.drawable.ic_badge_unlocked : R.drawable.ic_badge_locked;
            items.add(new SimpleListAdapter.Item(icon, b.title, b.description));
        }

        SimpleListAdapter adapter = new SimpleListAdapter(items);
        rv.setAdapter(adapter);

        return root;
    }
}
