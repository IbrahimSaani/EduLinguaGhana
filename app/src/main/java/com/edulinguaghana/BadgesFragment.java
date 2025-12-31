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
        items.add(new SimpleListAdapter.Item(R.drawable.ic_achievement_star, "Rising Star", "Complete 7 daily quests"));
        items.add(new SimpleListAdapter.Item(R.drawable.ic_achievement_medal, "Champion", "Top 10 on leaderboard"));
        items.add(new SimpleListAdapter.Item(R.drawable.ic_achievement_rocket, "Explorer", "Try all modes"));

        SimpleListAdapter adapter = new SimpleListAdapter(items);
        rv.setAdapter(adapter);

        return root;
    }
}
