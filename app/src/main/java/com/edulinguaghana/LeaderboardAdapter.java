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
        holder.tvRank.setText(String.valueOf(rank));
        holder.tvUserName.setText(entry.getUserName());
        holder.tvScore.setText(String.valueOf(entry.getScore()));

        // Special styling for top 3
        if (rank == 1) {
            holder.tvRank.setText("🥇");
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notification_achievement_bg));
        } else if (rank == 2) {
            holder.tvRank.setText("🥈");
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notification_milestone_bg));
        } else if (rank == 3) {
            holder.tvRank.setText("🥉");
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notification_motivational_bg));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return leaderboardList.size();
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvRank;
        TextView tvUserName;
        TextView tvScore;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}

