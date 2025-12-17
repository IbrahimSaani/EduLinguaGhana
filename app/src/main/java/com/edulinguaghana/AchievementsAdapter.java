package com.edulinguaghana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.AchievementViewHolder> {

    private Context context;
    private List<Achievement> achievements;

    public AchievementsAdapter(Context context, List<Achievement> achievements) {
        this.context = context;
        this.achievements = achievements;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);

        holder.tvEmoji.setText(achievement.getEmoji());
        holder.tvTitle.setText(achievement.getTitle());
        holder.tvDescription.setText(achievement.getDescription());

        if (achievement.isUnlocked()) {
            // Unlocked state
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notification_achievement_bg));
            holder.cardView.setAlpha(1.0f);
            holder.tvEmoji.setAlpha(1.0f);
            holder.tvTitle.setAlpha(1.0f);
            holder.tvDescription.setAlpha(1.0f);

            // Show unlock date
            if (achievement.getUnlockedTimestamp() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String date = sdf.format(new Date(achievement.getUnlockedTimestamp()));
                holder.tvUnlockDate.setText("Unlocked: " + date);
                holder.tvUnlockDate.setVisibility(View.VISIBLE);
            }
        } else {
            // Locked state
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            holder.cardView.setAlpha(0.5f);
            holder.tvEmoji.setAlpha(0.3f);
            holder.tvTitle.setAlpha(0.6f);
            holder.tvDescription.setAlpha(0.6f);
            holder.tvUnlockDate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvEmoji;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvUnlockDate;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvUnlockDate = itemView.findViewById(R.id.tvUnlockDate);
        }
    }
}

