package com.edulinguaghana;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edulinguaghana.gamification.Badge;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Calendar;
import java.util.Date;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<Badge> badges;

    public BadgeAdapter(List<Badge> badges) {
        this.badges = badges;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badges.get(position);
        holder.bind(badge);
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    public void updateBadges(List<Badge> newBadges) {
        this.badges = newBadges;
        notifyDataSetChanged();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        TextView tvBadgeIcon;
        TextView tvBadgeName;
        TextView tvBadgeDescription;
        TextView tvUnlockDate;
        ImageView ivBadgeLock;
        MaterialCardView unlockBadge;
        MaterialCardView lockedBadge;
        View badgeGlow;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBadgeIcon = itemView.findViewById(R.id.tv_badge_icon);
            tvBadgeName = itemView.findViewById(R.id.tv_badge_name);
            tvBadgeDescription = itemView.findViewById(R.id.tv_badge_description);
            tvUnlockDate = itemView.findViewById(R.id.tv_unlock_date);
            ivBadgeLock = itemView.findViewById(R.id.iv_badge_lock);
            unlockBadge = itemView.findViewById(R.id.unlockBadge);
            lockedBadge = itemView.findViewById(R.id.lockedBadge);
            badgeGlow = itemView.findViewById(R.id.badge_glow);
        }

        public void bind(Badge badge) {
            // Set badge name and description
            tvBadgeName.setText(badge.title);
            tvBadgeDescription.setText(badge.description);

            // Set icon based on badge type
            String icon = getBadgeEmoji(badge.id);
            tvBadgeIcon.setText(icon);

            // Set visibility and appearance based on unlock status
            if (badge.unlocked) {
                // Unlocked badge
                ivBadgeLock.setVisibility(View.GONE);
                lockedBadge.setVisibility(View.GONE);
                unlockBadge.setVisibility(View.VISIBLE);
                badgeGlow.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f);

                // Show unlock date
                if (badge.unlockedAt > 0) {
                    String unlockDateStr = formatUnlockDate(badge.unlockedAt);
                    tvUnlockDate.setText("Unlocked: " + unlockDateStr);
                    tvUnlockDate.setVisibility(View.VISIBLE);
                } else {
                    tvUnlockDate.setVisibility(View.GONE);
                }
            } else {
                // Locked badge
                ivBadgeLock.setVisibility(View.VISIBLE);
                lockedBadge.setVisibility(View.VISIBLE);
                unlockBadge.setVisibility(View.GONE);
                tvUnlockDate.setVisibility(View.GONE);
                badgeGlow.setVisibility(View.GONE);
                itemView.setAlpha(0.6f);
            }
        }

        private String getBadgeEmoji(String badgeId) {
            switch (badgeId) {
                case "first_practice":
                    return "📚";
                case "seven_days":
                    return "🔥";
                case "thirty_days":
                    return "⭐";
                case "quiz_master":
                    return "🧠";
                case "perfect_score":
                    return "💯";
                case "multilingual":
                    return "🌍";
                case "speed_champion":
                    return "⚡";
                case "achievement_collector":
                    return "🏆";
                default:
                    return "🎖️";
            }
        }

        private String formatUnlockDate(long timestampMillis) {
            Date unlockDate = new Date(timestampMillis);
            Date today = new Date();

            // Check if unlocked today
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(unlockDate);
            cal2.setTime(today);

            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                return "Today";
            }

            // Check if unlocked yesterday
            cal1.add(Calendar.DAY_OF_YEAR, 1);
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                return "Yesterday";
            }

            // Otherwise, show date
            return (String) DateFormat.format("MMM d", unlockDate);
        }
    }
}

