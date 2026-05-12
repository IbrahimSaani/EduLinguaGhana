package com.edulinguaghana;

import android.os.Bundle;
import android.os.Build;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.OnNotificationClickListener {

    private com.edulinguaghana.DynamicBackgroundView dynamicBackground;
    private ImageView emptyStateBellIcon;
    private View sparkleTopLeft;
    private View sparkleTopRight;
    private View sparkleBottomLeft;
    private View sparkleBottomRight;
    private RecyclerView notificationsRecyclerView;
    private LinearLayout emptyStateLayout;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnStartLearning;
    private com.google.android.material.card.MaterialCardView unreadBadge;
    private android.widget.TextView tvUnreadCount;
    private NotificationsAdapter adapter;
    private NotificationManager notificationManager;
    private List<Notification> notifications;
    private SoundPool soundPool;
    private int bellSoundId;
    private Toast bellToast;
    private boolean bellRotationInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize views
        dynamicBackground = findViewById(R.id.dynamicBackground);
        emptyStateBellIcon = findViewById(R.id.emptyStateBellIcon);
        sparkleTopLeft = findViewById(R.id.sparkleTopLeft);
        sparkleTopRight = findViewById(R.id.sparkleTopRight);
        sparkleBottomLeft = findViewById(R.id.sparkleBottomLeft);
        sparkleBottomRight = findViewById(R.id.sparkleBottomRight);
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnStartLearning = findViewById(R.id.btnStartLearning);
        unreadBadge = findViewById(R.id.unreadBadge);
        tvUnreadCount = findViewById(R.id.tvUnreadCount);

        // Setup background
        setupBackground();

        // Setup empty-state interactions
        setupEmptyStateInteractions();

        // Initialize SoundPool for bell sound
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .build();
        bellSoundId = soundPool.load(this, R.raw.bell, 1);

        // Initialize notification manager
        notificationManager = new NotificationManager(this);

        // Setup swipe to refresh
        swipeRefresh.setOnRefreshListener(() -> {
            loadNotifications();
            swipeRefresh.setRefreshing(false);
        });

        // Setup start learning button
        btnStartLearning.setOnClickListener(v -> {
            finish(); // Go back to main activity
        });

        // Check and generate notifications based on user progress
        notificationManager.checkAndGenerateNotifications();

        // Setup RecyclerView
        setupRecyclerView();

        // Load notifications
        loadNotifications();
    }

    private void setupBackground() {
        if (dynamicBackground == null) return;
        
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        int colorStart, colorMid, colorEnd;

        if (hour >= 5 && hour < 11) {
            colorStart = androidx.core.content.ContextCompat.getColor(this, R.color.bgMorningStart);
            colorMid = androidx.core.content.ContextCompat.getColor(this, R.color.bgMorningMid);
            colorEnd = androidx.core.content.ContextCompat.getColor(this, R.color.bgMorningEnd);
        } else if (hour >= 11 && hour < 17) {
            colorStart = androidx.core.content.ContextCompat.getColor(this, R.color.bgDayStart);
            colorMid = androidx.core.content.ContextCompat.getColor(this, R.color.bgDayMid);
            colorEnd = androidx.core.content.ContextCompat.getColor(this, R.color.bgDayEnd);
        } else {
            colorStart = androidx.core.content.ContextCompat.getColor(this, R.color.bgNightStart);
            colorMid = androidx.core.content.ContextCompat.getColor(this, R.color.bgNightMid);
            colorEnd = androidx.core.content.ContextCompat.getColor(this, R.color.bgNightEnd);
        }

        dynamicBackground.setColors(colorStart, colorMid, colorEnd);
    }


    private void setupRecyclerView() {
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setHasFixedSize(true);
    }

    private void setupEmptyStateInteractions() {
        if (emptyStateBellIcon == null) return;

        emptyStateBellIcon.setOnClickListener(v -> {
            playBellSparkle();
            playBellSound();

            if (!bellRotationInProgress) {
                bellRotationInProgress = true;
                v.animate()
                    .rotationBy(20f)
                    .setDuration(90)
                    .withEndAction(() -> v.animate()
                        .rotationBy(-40f)
                        .setDuration(120)
                        .withEndAction(() -> v.animate()
                            .rotationBy(20f)
                            .setDuration(90)
                            .withEndAction(() -> bellRotationInProgress = false)
                            .start())
                        .start())
                    .start();
            }

            v.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(120)
                .withEndAction(() -> v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start())
                .start();

            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    long[] pattern = {0, 60, 40, 80};
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                } else {
                    vibrator.vibrate(new long[]{0, 60, 40, 80}, -1);
                }
            }

            showBellToast("You're all caught up! We'll ring this bell when something new arrives.");
        });
    }

    private void playBellSparkle() {
        View[] sparkles = {sparkleTopLeft, sparkleTopRight, sparkleBottomLeft, sparkleBottomRight};
        float[][] offsetsDp = {
            {-10f, -14f},
            {10f, -12f},
            {-10f, 12f},
            {10f, 14f}
        };

        for (int i = 0; i < sparkles.length; i++) {
            View sparkle = sparkles[i];
            if (sparkle == null) continue;

            sparkle.animate().cancel();
            sparkle.setVisibility(View.VISIBLE);
            sparkle.setAlpha(0f);
            sparkle.setScaleX(0.5f);
            sparkle.setScaleY(0.5f);
            sparkle.setTranslationX(0f);
            sparkle.setTranslationY(0f);

            final float tx = dpToPx(offsetsDp[i][0]);
            final float ty = dpToPx(offsetsDp[i][1]);

            sparkle.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(i * 30L)
                .setDuration(90)
                .withEndAction(() -> sparkle.animate()
                    .alpha(0f)
                    .translationX(tx)
                    .translationY(ty)
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        sparkle.setVisibility(View.INVISIBLE);
                        sparkle.setTranslationX(0f);
                        sparkle.setTranslationY(0f);
                    })
                    .start())
                .start();
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void playBellSound() {
        if (soundPool != null && bellSoundId > 0) {
            soundPool.play(bellSoundId, 1f, 1f, 0, 0, 1f);
        }
    }

    private void showBellToast(String message) {
        if (isFinishing() || isDestroyed()) return;

        if (bellToast != null) {
            bellToast.cancel();
        }

        bellToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        bellToast.show();
    }

    private void loadNotifications() {
        notifications = notificationManager.getAllNotifications();

        // Update unread count badge
        updateUnreadBadge();

        if (notifications.isEmpty()) {
            // Show empty state
            emptyStateLayout.setVisibility(View.VISIBLE);
            swipeRefresh.setVisibility(View.GONE);
            
            // Animate empty state
            emptyStateLayout.setAlpha(0f);
            emptyStateLayout.setScaleX(0.8f);
            emptyStateLayout.setScaleY(0.8f);
            emptyStateLayout.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();
        } else {
            // Show notifications
            emptyStateLayout.setVisibility(View.GONE);
            swipeRefresh.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new NotificationsAdapter(this, notifications, this);
                notificationsRecyclerView.setAdapter(adapter);
                
                // Entrance animation
                notificationsRecyclerView.setAlpha(0f);
                notificationsRecyclerView.setTranslationY(50f);
                notificationsRecyclerView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .start();
            } else {
                adapter.updateNotifications(notifications);
            }
        }
        
        // Refresh menu to show/hide "Mark all as read"
        invalidateOptionsMenu();
    }

    private void updateUnreadBadge() {
        int unreadCount = notificationManager.getUnreadCount();
        if (unreadCount > 0) {
            unreadBadge.setVisibility(View.VISIBLE);
            tvUnreadCount.setText(String.valueOf(unreadCount));
            
            // Pulse animation for badge
            unreadBadge.setScaleX(0.8f);
            unreadBadge.setScaleY(0.8f);
            unreadBadge.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();
        } else {
            unreadBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read
        notificationManager.markAsRead(notification.getId());

        // Refresh the list
        loadNotifications();

        // Show details in a styled dialog
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            notification.getEmoji(),
            notification.getTitle(),
            notification.getMessage(),
            "Got it!",
            null,
            null,
            null
        );
    }

    @Override
    public void onNotificationDelete(Notification notification) {
        // Show confirmation dialog
        StyledMenuHelper.showStyledConfirmationDialog(
            this,
            "🗑️",
            "Delete Notification",
            "Remove this notification?",
            "Delete",
            "Cancel",
            () -> {
                // Delete notification
                notificationManager.deleteNotification(notification.getId());

                // Refresh the list
                loadNotifications();

                Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
            },
            null
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!notifications.isEmpty()) {
            getMenuInflater().inflate(R.menu.menu_notifications, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_mark_all_read) {
            notificationManager.markAllAsRead();
            loadNotifications();
            Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_clear_all) {
            StyledMenuHelper.showStyledConfirmationDialog(
                this,
                "🗑️",
                "Clear All Notifications",
                "Are you sure you want to delete all notifications?",
                "Clear",
                "Cancel",
                () -> {
                    notificationManager.clearAllNotifications();
                    loadNotifications();
                    Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
                },
                null
            );
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (bellToast != null) {
            bellToast.cancel();
            bellToast = null;
        }
        if (emptyStateBellIcon != null) {
            emptyStateBellIcon.animate().cancel();
        }
        bellRotationInProgress = false;
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bellToast != null) {
            bellToast.cancel();
        }
        if (emptyStateBellIcon != null) {
            emptyStateBellIcon.animate().cancel();
        }
        bellRotationInProgress = false;
    }
}

