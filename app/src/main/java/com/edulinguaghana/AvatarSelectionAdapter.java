package com.edulinguaghana;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class AvatarSelectionAdapter extends RecyclerView.Adapter<AvatarSelectionAdapter.ViewHolder> {

    private final List<String> labels;
    private final List<String> icons; // Emojis or Icons
    private int selectedPosition = 0;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public AvatarSelectionAdapter(List<String> labels, int selectedPosition, OnItemClickListener listener) {
        this(labels, null, selectedPosition, listener);
    }

    public AvatarSelectionAdapter(List<String> labels, List<String> icons, int selectedPosition, OnItemClickListener listener) {
        this.labels = labels;
        this.icons = icons;
        this.selectedPosition = selectedPosition;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avatar_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String label = labels.get(position);
        holder.tvLabel.setText(label);

        if (icons != null && position < icons.size()) {
            holder.tvEmoji.setText(icons.get(position));
            holder.tvEmoji.setVisibility(View.VISIBLE);
            holder.ivIcon.setVisibility(View.GONE);
        } else {
            holder.tvEmoji.setVisibility(View.GONE);
        }

        if (position == selectedPosition) {
            holder.cardItem.setStrokeWidth(6);
            holder.cardItem.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccent));
            holder.cardItem.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.cardBackground));
            holder.tvLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
            holder.cardItem.setCardElevation(12);
            holder.cardItem.animate().scaleX(1.08f).scaleY(1.08f).setDuration(200).start();
        } else {
            holder.cardItem.setStrokeWidth(0);
            holder.cardItem.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.cardBackground));
            holder.tvLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.textColorSecondary));
            holder.cardItem.setCardElevation(2);
            holder.cardItem.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onItemClick(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardItem;
        TextView tvLabel, tvEmoji;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardItem);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}