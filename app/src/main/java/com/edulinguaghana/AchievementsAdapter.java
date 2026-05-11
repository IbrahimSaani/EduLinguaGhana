package com.edulinguaghana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

        // Set Icon or Emoji
        boolean iconSet = false;
        String iconName = achievement.getIconName();
        
        if (iconName != null && !iconName.isEmpty()) {
            // Log for debugging (will show in Logcat)
            android.util.Log.d("AchievementsAdapter", "Attempting to load icon: " + iconName + " for " + achievement.getId());
            
            int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
            if (resId != 0) {
                holder.ivBadge.setImageResource(resId);
                holder.ivBadge.setVisibility(View.VISIBLE);
                holder.tvEmoji.setVisibility(View.GONE);
                iconSet = true;
            } else {
                android.util.Log.e("AchievementsAdapter", "Resource NOT FOUND: " + iconName);
            }
        }
        
        if (!iconSet) {
            holder.tvEmoji.setText(achievement.getEmoji());
            holder.tvEmoji.setVisibility(View.VISIBLE);
            holder.ivBadge.setVisibility(View.GONE);
        }

        holder.tvTitle.setText(achievement.getTitle());
        holder.tvDescription.setText(achievement.getDescription());

        if (achievement.isUnlocked()) {
            // Unlocked state
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.correctAnswer));
            holder.cardView.setStrokeWidth(4);
            
            holder.cardView.setAlpha(1.0f);
            holder.ivBadge.setAlpha(1.0f);
            holder.ivBadge.setImageTintList(null); // Remove any tints for unlocked
            holder.tvEmoji.setAlpha(1.0f);
            holder.tvTitle.setAlpha(1.0f);
            holder.tvDescription.setAlpha(1.0f);
            
            holder.iconBackground.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notification_achievement_bg));
            holder.overlayColor.setAlpha(0.2f);

            // Show unlock date
            if (achievement.getUnlockedTimestamp() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String date = sdf.format(new Date(achievement.getUnlockedTimestamp()));
                holder.tvUnlockDate.setText("Unlocked: " + date);
                holder.unlockDateCard.setVisibility(View.VISIBLE);
            }
            
            holder.progressContainer.setVisibility(View.GONE);
        } else {
            // Locked state
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.cardBackground));
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.dividerColor));
            holder.cardView.setStrokeWidth(2);
            
            holder.cardView.setAlpha(0.7f);
            holder.ivBadge.setAlpha(0.4f);
            // Apply a gray tint to the locked icon to make it clearly "locked"
            holder.ivBadge.setImageTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(context, android.R.color.darker_gray)));
            
            holder.tvEmoji.setAlpha(0.4f);
            holder.tvTitle.setAlpha(0.8f);
            holder.tvDescription.setAlpha(0.8f);
            
            holder.iconBackground.setCardBackgroundColor(ContextCompat.getColor(context, R.color.dividerColor));
            holder.overlayColor.setAlpha(0.1f);
            
            holder.unlockDateCard.setVisibility(View.GONE);
            
            // Show progress
            holder.progressContainer.setVisibility(View.VISIBLE);
            updateProgress(holder, achievement);
        }
    }

    private void updateProgress(AchievementViewHolder holder, Achievement achievement) {
        int currentVal = 0;
        switch (achievement.getType()) {
            case QUIZ_COUNT: currentVal = ProgressManager.getTotalQuizzes(context); break;
            case HIGH_SCORE: currentVal = ProgressManager.getHighScore(context); break;
            case PERFECT_SCORE: currentVal = (ProgressManager.getHighScore(context) == 100) ? 1 : 0; break;
            case STREAK: 
                StreakManager streakManager = new StreakManager(context);
                currentVal = streakManager.getCurrentStreak(); 
                break;
            case ACCURACY: currentVal = ProgressManager.getAccuracy(context); break;
        }
        
        int target = achievement.getRequiredValue();
        int progress = (int) Math.min(100, (currentVal * 100.0) / target);
        
        holder.progressBar.setProgress(progress);
        holder.tvProgressText.setText(currentVal + "/" + target);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        MaterialCardView iconBackground;
        View overlayColor;
        ImageView ivBadge;
        TextView tvEmoji;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvUnlockDate;
        View unlockDateCard;
        View progressContainer;
        ProgressBar progressBar;
        TextView tvProgressText;

        public AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            iconBackground = itemView.findViewById(R.id.iconBackground);
            overlayColor = itemView.findViewById(R.id.overlayColor);
            ivBadge = itemView.findViewById(R.id.ivBadge);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvUnlockDate = itemView.findViewById(R.id.tvUnlockDate);
            unlockDateCard = itemView.findViewById(R.id.unlockDateCard);
            progressContainer = itemView.findViewById(R.id.progressContainer);
            progressBar = itemView.findViewById(R.id.achievementProgress);
            tvProgressText = itemView.findViewById(R.id.tvProgressText);
        }
    }
}

