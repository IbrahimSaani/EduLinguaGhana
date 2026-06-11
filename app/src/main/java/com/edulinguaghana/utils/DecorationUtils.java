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
        if (root == null) return;
        
        boolean focusMode = AppPreferences.isFocusModeEnabled(root.getContext());
        boolean highContrast = AppPreferences.isHighContrastEnabled(root.getContext());
        
        if (focusMode || highContrast) {
            simplifyUI(root, focusMode, highContrast);
        }
    }

    private static void simplifyUI(View view, boolean focusMode, boolean highContrast) {
        if (view == null) return;

        // Check common decorative IDs
        int id = view.getId();
        String name = "";
        try {
            name = view.getContext().getResources().getResourceEntryName(id);
        } catch (Exception ignored) {}

        // Hide decorations in Focus Mode OR High Contrast
        if (name.contains("decor") || name.contains("decoration") || name.contains("decorative") || 
            name.contains("sparkle") || name.contains("dynamicBackground") || name.contains("bubble") ||
            name.contains("star") || name.contains("circle") || name.contains("triangle") || 
            name.contains("square") || name.contains("floatingElements")) {
            view.setVisibility(View.GONE);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                simplifyUI(group.getChildAt(i), focusMode, highContrast);
            }
        }
    }
}
