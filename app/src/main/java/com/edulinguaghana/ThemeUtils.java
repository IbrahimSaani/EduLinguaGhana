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
}

