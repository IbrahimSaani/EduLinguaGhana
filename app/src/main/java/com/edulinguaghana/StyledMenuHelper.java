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
}

