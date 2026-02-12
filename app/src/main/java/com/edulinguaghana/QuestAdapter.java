package com.edulinguaghana;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.edulinguaghana.gamification.Quest;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {

    private List<Quest> quests;
    private OnQuestClickListener listener;

    public interface OnQuestClickListener {
        void onQuestClick(Quest quest, int position);
    }

    public QuestAdapter(List<Quest> quests) {
        this.quests = quests;
    }

    public void setOnQuestClickListener(OnQuestClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quest, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        Quest quest = quests.get(position);
        holder.bind(quest, listener);
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public void updateQuests(List<Quest> newQuests) {
        this.quests = newQuests;
        notifyDataSetChanged();
    }

    static class QuestViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestIcon;
        TextView tvQuestTitle;
        TextView tvQuestProgress;
        TextView tvQuestPoints;
        LinearProgressIndicator progressQuest;
        View claimIndicator;

        public QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestIcon = itemView.findViewById(R.id.tv_quest_icon);
            tvQuestTitle = itemView.findViewById(R.id.tv_quest_title);
            tvQuestProgress = itemView.findViewById(R.id.tv_quest_progress);
            tvQuestPoints = itemView.findViewById(R.id.tv_quest_points);
            progressQuest = itemView.findViewById(R.id.progress_quest);
            claimIndicator = itemView.findViewById(R.id.claimIndicator);
        }

        public void bind(Quest quest, OnQuestClickListener listener) {
            // Set quest icon based on type or status
            if (quest.completed) {
                tvQuestIcon.setText("✅");
            } else if (quest.id.contains("practice")) {
                tvQuestIcon.setText("📚");
            } else if (quest.id.contains("quiz")) {
                tvQuestIcon.setText("🎯");
            } else if (quest.id.contains("challenge")) {
                tvQuestIcon.setText("⚡");
            } else {
                tvQuestIcon.setText("🎮");
            }

            // Set quest title
            tvQuestTitle.setText(quest.title);

            // Calculate progress percentage
            int progressPercentage = quest.target > 0 ?
                (int) ((quest.progress * 100.0) / quest.target) : 0;

            // Ensure progress doesn't exceed 100%
            progressPercentage = Math.min(progressPercentage, 100);

            // Set progress bar
            progressQuest.setProgress(progressPercentage);

            // Set progress text and claim indicator visibility
            if (quest.completed) {
                tvQuestProgress.setText("Completed!");
                tvQuestProgress.setTextColor(itemView.getContext().getColor(R.color.correctAnswer));
                claimIndicator.setVisibility(View.GONE);
            } else if (quest.progress >= quest.target) {
                // Quest is ready to claim
                tvQuestProgress.setText(quest.progress + "/" + quest.target);
                tvQuestProgress.setTextColor(itemView.getContext().getColor(R.color.correctAnswer));
                claimIndicator.setVisibility(View.VISIBLE);
            } else {
                // Quest in progress
                tvQuestProgress.setText(quest.progress + "/" + quest.target);
                tvQuestProgress.setTextColor(itemView.getContext().getColor(R.color.colorPrimary));
                claimIndicator.setVisibility(View.GONE);
            }

            // Set points
            tvQuestPoints.setText("+" + quest.xpReward);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (quest.completed) {
                            // Already completed, just show message
                            Toast.makeText(v.getContext(),
                                "✅ Quest already completed!",
                                Toast.LENGTH_SHORT).show();
                        } else if (quest.progress >= quest.target) {
                            // Progress met, can claim reward
                            listener.onQuestClick(quest, position);
                        } else {
                            // Not enough progress
                            int remaining = quest.target - quest.progress;
                            String message = "Keep going! You need " + remaining + " more to complete this quest.";
                            Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            // Visual state for completed quests
            if (quest.completed) {
                itemView.setAlpha(0.6f);
                itemView.setClickable(true); // Still clickable to show message
            } else if (quest.progress >= quest.target) {
                // Ready to claim - make it prominent
                itemView.setAlpha(1.0f);
                itemView.setClickable(true);
            } else {
                // In progress
                itemView.setAlpha(1.0f);
                itemView.setClickable(true);
            }
        }
    }
}

