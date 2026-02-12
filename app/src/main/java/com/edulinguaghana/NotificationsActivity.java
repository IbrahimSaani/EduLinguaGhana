package com.edulinguaghana;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity implements NotificationsAdapter.OnNotificationClickListener {

    private RecyclerView notificationsRecyclerView;
    private LinearLayout emptyStateLayout;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnStartLearning;
    private NotificationsAdapter adapter;
    private NotificationManager notificationManager;
    private List<Notification> notifications;

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
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnStartLearning = findViewById(R.id.btnStartLearning);

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

    private void setupRecyclerView() {
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setHasFixedSize(true);

        // Add fade-in animation when activity opens
        notificationsRecyclerView.setAlpha(0f);
        notificationsRecyclerView.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(100)
            .start();
    }

    private void loadNotifications() {
        notifications = notificationManager.getAllNotifications();

        if (notifications.isEmpty()) {
            // Show empty state
            emptyStateLayout.setVisibility(View.VISIBLE);
            swipeRefresh.setVisibility(View.GONE);
        } else {
            // Show notifications
            emptyStateLayout.setVisibility(View.GONE);
            swipeRefresh.setVisibility(View.VISIBLE);

            adapter = new NotificationsAdapter(this, notifications, this);
            notificationsRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read
        notificationManager.markAsRead(notification.getId());

        // Refresh the list
        loadNotifications();

        // Show details in a styled dialog
        String title = notification.getEmoji() + " " + notification.getTitle();
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(notification.getMessage())
                .setPositiveButton("Got it!", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    @Override
    public void onNotificationDelete(Notification notification) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Notification")
                .setMessage("Remove this notification?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete notification
                    notificationManager.deleteNotification(notification.getId());

                    // Refresh the list
                    loadNotifications();

                    Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
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
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Notifications")
                    .setMessage("Are you sure you want to delete all notifications?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        notificationManager.clearAllNotifications();
                        loadNotifications();
                        Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

