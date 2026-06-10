package com.edulinguaghana.utils;

import android.view.View;
import android.view.ViewGroup;
import com.edulinguaghana.AppPreferences;

public class DecorationUtils {

    /**
     * Hides views tagged as decorations if Focus Mode is enabled.
     * Or hides specific views by ID if they are common decorative elements.
     */
    public static void applyFocusMode(View root) {
        if (root == null || !AppPreferences.isFocusModeEnabled(root.getContext())) return;

        hideDecorations(root);
    }

    private static void hideDecorations(View view) {
        if (view == null) return;

        // Check common decorative IDs
        int id = view.getId();
        String name = "";
        try {
            name = view.getContext().getResources().getResourceEntryName(id);
        } catch (Exception ignored) {}

        if (name.contains("decor") || name.contains("decoration") || name.contains("decorative") || 
            name.contains("sparkle") || name.contains("dynamicBackground")) {
            view.setVisibility(View.GONE);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                hideDecorations(group.getChildAt(i));
            }
        }
    }
}
