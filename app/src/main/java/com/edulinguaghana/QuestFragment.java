package com.edulinguaghana;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class QuestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_quest, container, false);

        RecyclerView rv = root.findViewById(R.id.rv_daily_quests);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<SimpleListAdapter.Item> items = new ArrayList<>();
        items.add(new SimpleListAdapter.Item(R.drawable.ic_achievement_bolt, "Daily Practice", "Complete 10 questions"));
        items.add(new SimpleListAdapter.Item(R.drawable.ic_achievement_book, "Word Builder", "Learn 5 new words"));
        items.add(new SimpleListAdapter.Item(R.drawable.ic_achievement_target, "Accuracy Boost", "Score 80% or higher"));

        SimpleListAdapter adapter = new SimpleListAdapter(items);
        rv.setAdapter(adapter);

        return root;
    }
}
