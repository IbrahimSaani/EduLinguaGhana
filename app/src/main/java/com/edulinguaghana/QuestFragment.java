package com.edulinguaghana;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edulinguaghana.gamification.Quest;
import com.edulinguaghana.gamification.QuestManager;

import java.util.List;

public class QuestFragment extends Fragment {

    private RecyclerView rvQuests;
    private LinearLayout emptyState;
    private TextView tvCompletedCount;
    private TextView tvActiveCount;
    private TextView tvTotalPoints;
    private QuestAdapter questAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_quest, container, false);

        // Initialize views
        rvQuests = root.findViewById(R.id.rv_daily_quests);
        emptyState = root.findViewById(R.id.empty_state);
        tvCompletedCount = root.findViewById(R.id.tv_completed_count);
        tvActiveCount = root.findViewById(R.id.tv_active_count);
        tvTotalPoints = root.findViewById(R.id.tv_total_points);

        // Setup RecyclerView
        rvQuests.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load quests and display
        loadQuests();

        return root;
    }

    private void loadQuests() {
        if (getContext() == null) return;

        List<Quest> quests = QuestManager.getDailyQuests(getContext());

        if (quests.isEmpty()) {
            // Show empty state
            rvQuests.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            // Show quests
            rvQuests.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            // Setup adapter
            if (questAdapter == null) {
                questAdapter = new QuestAdapter(quests);
                questAdapter.setOnQuestClickListener(this::onQuestClicked);
                rvQuests.setAdapter(questAdapter);
            } else {
                questAdapter.updateQuests(quests);
            }

            // Update statistics
            updateStatistics(quests);
        }
    }

    private void onQuestClicked(Quest quest, int position) {
        if (quest.completed) {
            Toast.makeText(getContext(), "Quest already completed!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (quest.progress < quest.target) {
            int remaining = quest.target - quest.progress;
            Toast.makeText(getContext(),
                "You need " + remaining + " more to complete this quest!",
                Toast.LENGTH_SHORT).show();
            return;
        }

        // Quest is ready to be claimed
        showClaimDialog(quest);
    }

    private void showClaimDialog(Quest quest) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
            .setTitle("🎉 Quest Complete!")
            .setMessage("Congratulations! You've completed:\n\n" +
                       quest.title + "\n\n" +
                       "Claim your reward: +" + quest.xpReward + " XP")
            .setPositiveButton("Claim Reward", (dialog, which) -> {
                claimQuestReward(quest);
            })
            .setNegativeButton("Later", null)
            .setCancelable(true)
            .show();
    }

    private void claimQuestReward(Quest quest) {
        if (getContext() == null) return;

        boolean success = QuestManager.completeQuest(getContext(), quest.id);

        if (success) {
            Toast.makeText(getContext(),
                "✅ Quest completed! +" + quest.xpReward + " XP earned!",
                Toast.LENGTH_LONG).show();

            // Refresh the quest list
            loadQuests();
        } else {
            Toast.makeText(getContext(),
                "Could not complete quest. Progress not met.",
                Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatistics(List<Quest> quests) {
        int completedCount = 0;
        int activeCount = 0;
        int totalPoints = 0;

        for (Quest quest : quests) {
            if (quest.completed) {
                completedCount++;
                totalPoints += quest.xpReward;
            } else {
                activeCount++;
            }
        }

        tvCompletedCount.setText(String.valueOf(completedCount));
        tvActiveCount.setText(String.valueOf(activeCount));
        tvTotalPoints.setText(String.valueOf(totalPoints));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh quests when fragment becomes visible
        loadQuests();
    }
}
