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

import com.edulinguaghana.gamification.Quest;
import com.edulinguaghana.gamification.QuestManager;

import java.util.ArrayList;
import java.util.List;

public class QuestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_quest, container, false);

        RecyclerView rv = root.findViewById(R.id.rv_daily_quests);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        final List<SimpleListAdapter.Item> items = new ArrayList<>();
        final List<Quest> quests = QuestManager.getDailyQuests(getContext());
        for (Quest q : quests) {
            String subtitle = q.description + (q.completed ? " (Completed)" : "");
            items.add(new SimpleListAdapter.Item(R.drawable.ic_achievement_bolt, q.title, subtitle));
        }

        final SimpleListAdapter adapter = new SimpleListAdapter(items);
        rv.setAdapter(adapter);

        // Allow tapping items to complete them
        rv.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), rv, (view, position) -> {
            Quest q = quests.get(position);
            if (!q.completed) {
                QuestManager.completeQuest(getContext(), q.id);
                // update subtitle
                adapter.updateItem(position, new SimpleListAdapter.Item(R.drawable.ic_achievement_bolt, q.title, q.description + " (Completed)"));
            }
        }));

        return root;
    }
}
