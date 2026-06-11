package com.edulinguaghana.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import com.edulinguaghana.AppPreferences;
import com.edulinguaghana.R;

public class FontUtils {

    public static void applyAppFont(View root) {
        if (root == null) return;
        Context context = root.getContext();
        
        Typeface typeface;
        if (AppPreferences.isStandardFontEnabled(context)) {
            typeface = Typeface.DEFAULT;
        } else {
            try {
                typeface = ResourcesCompat.getFont(context, R.font.agbalumo);
            } catch (Exception e) {
                typeface = Typeface.DEFAULT;
            }
        }
        
        applyTypefaceRecursively(root, typeface);
    }

    private static void applyTypefaceRecursively(View view, Typeface typeface) {
        if (view instanceof TextView) {
            ((TextView) view).setTypeface(typeface);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyTypefaceRecursively(group.getChildAt(i), typeface);
            }
        }
    }
}
