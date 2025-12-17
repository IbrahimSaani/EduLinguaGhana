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

        // Enhanced visual styling based on read status and notification type
        if (notification.isRead()) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            holder.cardView.setCardElevation(2f);
            holder.tvTitle.setAlpha(0.7f);
            holder.tvMessage.setAlpha(0.7f);
            holder.tvEmoji.setAlpha(0.7f);
        } else {
            // Set background color based on notification type
            int backgroundColor;
            switch (notification.getType()) {
                case ACHIEVEMENT:
                    backgroundColor = R.color.notification_achievement_bg;
                    break;
                case MILESTONE:
                    backgroundColor = R.color.notification_milestone_bg;
                    break;
                case STREAK:
                    backgroundColor = R.color.notification_streak_bg;
                    break;
                case MOTIVATIONAL:
                    backgroundColor = R.color.notification_motivational_bg;
                    break;
                case REMINDER:
                    backgroundColor = R.color.notification_reminder_bg;
                    break;
                default:
                    backgroundColor = R.color.notification_unread_bg;
                    break;
            }
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, backgroundColor));
            holder.cardView.setCardElevation(4f);
            holder.tvTitle.setAlpha(1.0f);
            holder.tvMessage.setAlpha(1.0f);
            holder.tvEmoji.setAlpha(1.0f);
        }

        // Click listeners with visual feedback
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

        // Add entrance animation
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationX(100f);
        holder.itemView.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(300)
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
        TextView tvEmoji;
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTime;
        MaterialCardView btnDelete;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

