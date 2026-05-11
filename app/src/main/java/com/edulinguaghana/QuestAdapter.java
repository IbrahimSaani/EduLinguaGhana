package com.edulinguaghana;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.edulinguaghana.gamification.Quest;
import com.google.android.material.card.MaterialCardView;
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
        View completionIndicator;
        MaterialCardView iconBackground;
        View overlayColor;

        public QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestIcon = itemView.findViewById(R.id.tv_quest_icon);
            tvQuestTitle = itemView.findViewById(R.id.tv_quest_title);
            tvQuestProgress = itemView.findViewById(R.id.tv_quest_progress);
            tvQuestPoints = itemView.findViewById(R.id.tv_quest_points);
            progressQuest = itemView.findViewById(R.id.progress_quest);
            claimIndicator = itemView.findViewById(R.id.claimIndicator);
            completionIndicator = itemView.findViewById(R.id.completionIndicator);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            overlayColor = itemView.findViewById(R.id.overlayColor);
        }

        public void bind(Quest quest, OnQuestClickListener listener) {
            // Set quest icon based on quest ID with emoji mapping
            String icon = getQuestEmoji(quest.id);
            tvQuestIcon.setText(icon);

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
                completionIndicator.setVisibility(View.VISIBLE);
                itemView.setAlpha(0.7f);
                iconBackground.setCardBackgroundColor(itemView.getContext().getColor(R.color.notification_achievement_bg));
                overlayColor.setAlpha(0.2f);
            } else if (quest.progress >= quest.target) {
                // Quest is ready to claim
                tvQuestProgress.setText(quest.progress + "/" + quest.target);
                tvQuestProgress.setTextColor(itemView.getContext().getColor(R.color.correctAnswer));
                claimIndicator.setVisibility(View.VISIBLE);
                completionIndicator.setVisibility(View.GONE);
                itemView.setAlpha(1.0f);
                iconBackground.setCardBackgroundColor(itemView.getContext().getColor(R.color.white));
                overlayColor.setAlpha(0.15f);
            } else {
                // Quest in progress
                tvQuestProgress.setText(quest.progress + "/" + quest.target);
                tvQuestProgress.setTextColor(itemView.getContext().getColor(R.color.colorPrimary));
                claimIndicator.setVisibility(View.GONE);
                completionIndicator.setVisibility(View.GONE);
                itemView.setAlpha(1.0f);
                iconBackground.setCardBackgroundColor(itemView.getContext().getColor(R.color.dividerColor));
                overlayColor.setAlpha(0.1f);
            }

            // Set points
            tvQuestPoints.setText("+" + quest.xpReward + " XP");

            // Set click listener with enhanced feedback
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        if (quest.completed) {
                            Toast.makeText(v.getContext(),
                                "✅ Quest already completed! Great job!",
                                Toast.LENGTH_SHORT).show();
                        } else if (quest.progress >= quest.target) {
                            listener.onQuestClick(quest, position);
                        } else {
                            int remaining = quest.target - quest.progress;
                            String message = "Keep going! You need " + remaining + " more to complete this quest.";
                            Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        private String getQuestEmoji(String questId) {
            switch (questId) {
                case "daily_practice":
                    return "📚";
                case "daily_quiz":
                    return "🎯";
                case "daily_challenge":
                    return "⚡";
                case "practice_streak":
                    return "🔥";
                case "quiz_multiple":
                    return "🧠";
                case "speed_game":
                    return "💨";
                case "language_explorer":
                    return "🌍";
                case "marathon_learner":
                    return "🏋️";
                default:
                    return "🎮";
            }
        }
    }
}
