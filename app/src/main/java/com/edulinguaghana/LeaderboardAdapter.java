package com.edulinguaghana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private Context context;
    private List<LeaderboardEntry> leaderboardList;

    public LeaderboardAdapter(Context context, List<LeaderboardEntry> leaderboardList) {
        this.context = context;
        this.leaderboardList = leaderboardList;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardEntry entry = leaderboardList.get(position);

        int rank = entry.getRank();
        holder.tvUserName.setText(entry.getUserName());
        holder.tvScore.setText(String.valueOf(entry.getScore()));

        // Set user avatar if available
        if (entry.getAvatarData() != null && holder.ivUserAvatar != null) {
            AvatarBuilder.AvatarConfig config = AvatarBuilder.AvatarConfig.fromMap(entry.getAvatarData());
            holder.ivUserAvatar.setAvatarConfig(config);
        } else if (holder.ivUserAvatar != null) {
            // Set a default config if none exists
            holder.ivUserAvatar.setAvatarConfig(new AvatarBuilder.AvatarConfig());
        }

        // Set rank badge - display rank number or medal emoji for top 3
        if (rank == 1) {
            holder.tvRank.setText("🥇");
        } else if (rank == 2) {
            holder.tvRank.setText("🥈");
        } else if (rank == 3) {
            holder.tvRank.setText("🥉");
        } else {
            holder.tvRank.setText(String.valueOf(rank));
        }

        // Highlight current user
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        boolean isCurrentUser = currentUser != null && currentUser.getUid().equals(entry.getUserId());

        // Style main card
        if (isCurrentUser) {
            // Highlight current user with a theme-aware color
            int highlightColor = ContextCompat.getColor(context, R.color.colorPrimaryLight);
            holder.cardView.setCardBackgroundColor(highlightColor);
            holder.cardView.setStrokeWidth(4);
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.colorPrimary));
            
            // Ensure text is visible on highlight
            holder.tvUserName.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary));
            holder.tvScore.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            // Use theme-aware background color for other players
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.leaderboard_item_bg));
            holder.cardView.setStrokeWidth(1);
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.dividerColor));
            
            // Ensure text color is theme-aware
            holder.tvUserName.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary));
            holder.tvScore.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
        
        holder.cardView.setCardElevation(isCurrentUser ? 6 : 2);
    }

    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

    public void updateList(List<LeaderboardEntry> newList) {
        this.leaderboardList = newList;
        notifyDataSetChanged();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvRank;
        TextView tvUserName;
        TextView tvScore;
        AvatarView ivUserAvatar;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvScore = itemView.findViewById(R.id.tvScore);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
        }
    }
}

