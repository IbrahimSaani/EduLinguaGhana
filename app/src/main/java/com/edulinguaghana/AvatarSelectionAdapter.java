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

    private final List<String> items;
    private int selectedPosition = 0;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public AvatarSelectionAdapter(List<String> items, int selectedPosition, OnItemClickListener listener) {
        this.items = items;
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
        String item = items.get(position);
        holder.tvLabel.setText(item);

        if (position == selectedPosition) {
            holder.cardItem.setStrokeWidth(6);
            holder.cardItem.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccent));
            holder.tvLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccent));
            holder.cardItem.setCardElevation(8);
        } else {
            holder.cardItem.setStrokeWidth(0);
            holder.tvLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.textColorPrimary));
            holder.cardItem.setCardElevation(2);
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
        return items.size();
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardItem;
        TextView tvLabel;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardItem);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}