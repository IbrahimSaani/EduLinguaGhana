package com.edulinguaghana;

import android.content.Context;
import android.content.res.Configuration;

/**
 * Small utility helpers for theme / UI mode detection.
 */
public final class ThemeUtils {

    private ThemeUtils() {}

    public static boolean isDarkMode(Context context) {
        if (context == null) return false;
        int uiMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return uiMode == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Applies the custom branding font (Agbalumo) to a Toolbar title
     * if the user hasn't opted for "Standard Font" in accessibility settings.
     */
    public static void applyToolbarFont(androidx.appcompat.widget.Toolbar toolbar) {
        if (toolbar == null) return;
        Context context = toolbar.getContext();
        
        if (com.edulinguaghana.AppPreferences.isStandardFontEnabled(context)) {
            // Restore default font
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                android.view.View view = toolbar.getChildAt(i);
                if (view instanceof android.widget.TextView) {
                    android.widget.TextView textView = (android.widget.TextView) view;
                    if (textView.getText().equals(toolbar.getTitle())) {
                        textView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                        break;
                    }
                }
            }
            return;
        }

        try {
            android.graphics.Typeface typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.agbalumo);
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                android.view.View view = toolbar.getChildAt(i);
                if (view instanceof android.widget.TextView) {
                    android.widget.TextView textView = (android.widget.TextView) view;
                    if (textView.getText().equals(toolbar.getTitle())) {
                        textView.setTypeface(typeface);
                        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 20);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}

