package com.edulinguaghana;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
        void onNotificationDelete(Notification notification);
    }

    public NotificationsAdapter(Context context, List<Notification> notifications, OnNotificationClickListener listener) {
        this.context = context;
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.tvEmoji.setText(notification.getEmoji());
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());

        // Format timestamp
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
            notification.getTimestamp(),
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        );
        holder.tvTime.setText(timeAgo);

        // Styling based on unread status
        if (notification.isRead()) {
            holder.cardView.setCardElevation(2f);
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.dividerColor));
            holder.unreadDot.setVisibility(View.GONE);
            holder.tvNewTag.setVisibility(View.GONE);
            holder.tvTitle.setAlpha(0.7f);
            holder.tvMessage.setAlpha(0.7f);
            holder.emojiBg.setAlpha(0.5f);
        } else {
            holder.cardView.setCardElevation(6f);
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.unreadDot.setVisibility(View.VISIBLE);
            holder.tvNewTag.setVisibility(View.VISIBLE);
            holder.tvTitle.setAlpha(1.0f);
            holder.tvMessage.setAlpha(1.0f);
            holder.emojiBg.setAlpha(1.0f);
            
            // Pulse animation for the "NEW" tag
            holder.tvNewTag.setScaleX(0.8f);
            holder.tvNewTag.setScaleY(0.8f);
            holder.tvNewTag.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.CycleInterpolator(1))
                .start();
        }

        // Background color for emoji bubble based on type
        int emojiBgColor;
        switch (notification.getType()) {
            case ACHIEVEMENT:
                emojiBgColor = ContextCompat.getColor(context, R.color.notification_achievement_bg);
                break;
            case MILESTONE:
                emojiBgColor = ContextCompat.getColor(context, R.color.notification_milestone_bg);
                break;
            case STREAK:
                emojiBgColor = ContextCompat.getColor(context, R.color.notification_streak_bg);
                break;
            case MOTIVATIONAL:
                emojiBgColor = ContextCompat.getColor(context, R.color.notification_motivational_bg);
                break;
            case REMINDER:
                emojiBgColor = ContextCompat.getColor(context, R.color.notification_reminder_bg);
                break;
            case NEW_CONTENT:
                emojiBgColor = ContextCompat.getColor(context, R.color.notification_milestone_bg);
                break;
            case SYSTEM:
                emojiBgColor = ContextCompat.getColor(context, R.color.colorPrimaryLight);
                break;
            default:
                emojiBgColor = ContextCompat.getColor(context, R.color.colorPrimaryLight);
                break;
        }
        holder.emojiBg.setCardBackgroundColor(emojiBgColor);

        // Click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationDelete(notification);
            }
        });

        // Entrance animation
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(30f);
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(position * 50L)
            .start();
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications != null ? newNotifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        MaterialCardView emojiBg;
        TextView tvEmoji;
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTime;
        TextView tvNewTag;
        View unreadDot;
        MaterialCardView btnDelete;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            emojiBg = itemView.findViewById(R.id.emojiBg);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvNewTag = itemView.findViewById(R.id.tvNewTag);
            unreadDot = itemView.findViewById(R.id.unreadDot);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
