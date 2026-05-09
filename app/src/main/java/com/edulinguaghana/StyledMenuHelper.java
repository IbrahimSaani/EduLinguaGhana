package com.edulinguaghana;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Helper class to create beautifully styled menu dialogs
 */
public class StyledMenuHelper {

    public static class MenuItem {
        public String icon;
        public String title;
        public String subtitle;
        public Runnable action;

        public MenuItem(String icon, String title, Runnable action) {
            this.icon = icon;
            this.title = title;
            this.action = action;
        }

        public MenuItem(String icon, String title, String subtitle, Runnable action) {
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.action = action;
        }
    }

    public static class MenuAdapter extends ArrayAdapter<MenuItem> {
        private final Context context;
        private final List<MenuItem> items;

        public MenuAdapter(Context context, List<MenuItem> items) {
            super(context, 0, items);
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_menu_option, parent, false);
            }

            MenuItem item = items.get(position);

            TextView tvIcon = convertView.findViewById(R.id.tvMenuIcon);
            TextView tvTitle = convertView.findViewById(R.id.tvMenuTitle);
            TextView tvSubtitle = convertView.findViewById(R.id.tvMenuSubtitle);
            ImageView ivArrow = convertView.findViewById(R.id.ivMenuArrow);

            tvIcon.setText(item.icon);
            tvTitle.setText(item.title);

            if (item.subtitle != null && !item.subtitle.isEmpty()) {
                tvSubtitle.setText(item.subtitle);
                tvSubtitle.setVisibility(View.VISIBLE);
            } else {
                tvSubtitle.setVisibility(View.GONE);
            }

            ivArrow.setVisibility(View.VISIBLE);

            return convertView;
        }
    }

    public static AlertDialog showStyledMenu(
            Context context,
            String headerIcon,
            String title,
            String subtitle,
            List<MenuItem> menuItems,
            Runnable onDismiss
    ) {
        // Create custom header
        View headerView = LayoutInflater.from(context).inflate(R.layout.dialog_menu_header, null);
        TextView tvDialogIcon = headerView.findViewById(R.id.tvDialogIcon);
        TextView tvDialogTitle = headerView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogSubtitle = headerView.findViewById(R.id.tvDialogSubtitle);

        tvDialogIcon.setText(headerIcon);
        tvDialogTitle.setText(title);

        if (subtitle != null && !subtitle.isEmpty()) {
            tvDialogSubtitle.setText(subtitle);
            tvDialogSubtitle.setVisibility(View.VISIBLE);
        }

        // Create adapter
        MenuAdapter adapter = new MenuAdapter(context, menuItems);

        // Build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setCustomTitle(headerView);
        builder.setAdapter(adapter, (dialog, which) -> {
            MenuItem item = menuItems.get(which);
            if (item.action != null) {
                item.action.run();
            }
        });
        builder.setNegativeButton("Cancel", null);

        if (onDismiss != null) {
            builder.setOnDismissListener(d -> onDismiss.run());
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static AlertDialog showStyledMenu(
            Context context,
            String headerIcon,
            String title,
            List<MenuItem> menuItems
    ) {
        return showStyledMenu(context, headerIcon, title, null, menuItems, null);
    }

    /**
     * Show a styled confirmation dialog with icon, title, message, and action buttons
     */
    public static AlertDialog showStyledConfirmationDialog(
            Context context,
            String headerIcon,
            String title,
            String message,
            String positiveButtonText,
            String negativeButtonText,
            Runnable onPositive,
            Runnable onNegative
    ) {
        // Create custom header
        View headerView = LayoutInflater.from(context).inflate(R.layout.dialog_menu_header, null);
        TextView tvDialogIcon = headerView.findViewById(R.id.tvDialogIcon);
        TextView tvDialogTitle = headerView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogSubtitle = headerView.findViewById(R.id.tvDialogSubtitle);

        tvDialogIcon.setText(headerIcon);
        tvDialogTitle.setText(title);

        if (message != null && !message.isEmpty()) {
            tvDialogSubtitle.setText(message);
            tvDialogSubtitle.setVisibility(View.VISIBLE);
        }

        // Build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setCustomTitle(headerView);

        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                if (onPositive != null) {
                    onPositive.run();
                }
            });
        }

        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, (dialog, which) -> {
                if (onNegative != null) {
                    onNegative.run();
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    /**
     * Show a styled confirmation dialog (simple version with just Yes/No)
     */
    public static AlertDialog showStyledConfirmationDialog(
            Context context,
            String headerIcon,
            String title,
            String message,
            Runnable onYes,
            Runnable onNo
    ) {
        return showStyledConfirmationDialog(context, headerIcon, title, message, "Yes", "No", onYes, onNo);
    }

    /**
     * Show a styled dialog with a custom view (e.g., for EditText input)
     */
    public static AlertDialog showStyledCustomDialog(
            Context context,
            String headerIcon,
            String title,
            String message,
            View customView,
            String positiveButtonText,
            String negativeButtonText,
            String neutralButtonText,
            Runnable onPositive,
            Runnable onNegative,
            Runnable onNeutral
    ) {
        // Create custom header
        View headerView = LayoutInflater.from(context).inflate(R.layout.dialog_menu_header, null);
        TextView tvDialogIcon = headerView.findViewById(R.id.tvDialogIcon);
        TextView tvDialogTitle = headerView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogSubtitle = headerView.findViewById(R.id.tvDialogSubtitle);

        tvDialogIcon.setText(headerIcon);
        tvDialogTitle.setText(title);

        if (message != null && !message.isEmpty()) {
            tvDialogSubtitle.setText(message);
            tvDialogSubtitle.setVisibility(View.VISIBLE);
        }

        // Build dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setCustomTitle(headerView);
        
        if (customView != null) {
            // Wrap custom view in a container for consistent padding
            android.widget.FrameLayout container = new android.widget.FrameLayout(context);
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = (int) (24 * context.getResources().getDisplayMetrics().density);
            params.leftMargin = margin;
            params.rightMargin = margin;
            params.topMargin = (int) (8 * context.getResources().getDisplayMetrics().density);
            params.bottomMargin = margin;
            customView.setLayoutParams(params);
            container.addView(customView);
            builder.setView(container);
        }

        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
                if (onPositive != null) {
                    onPositive.run();
                }
            });
        }

        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, (dialog, which) -> {
                if (onNegative != null) {
                    onNegative.run();
                }
            });
        }

        if (neutralButtonText != null) {
            builder.setNeutralButton(neutralButtonText, (dialog, which) -> {
                if (onNeutral != null) {
                    onNeutral.run();
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static AlertDialog showStyledCustomDialog(
            Context context,
            String headerIcon,
            String title,
            String message,
            View customView,
            String positiveButtonText,
            String negativeButtonText,
            Runnable onPositive,
            Runnable onNegative
    ) {
        return showStyledCustomDialog(context, headerIcon, title, message, customView, positiveButtonText, negativeButtonText, null, onPositive, onNegative, null);
    }
}

