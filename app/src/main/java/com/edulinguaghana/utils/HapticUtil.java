package com.edulinguaghana.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import com.edulinguaghana.AppPreferences;

public class HapticUtil {

    public static void vibrateSuccess(Context context) {
        if (!AppPreferences.isHapticFeedbackEnabled(context)) return;
        vibrate(context, new long[]{0, 100, 50, 100});
    }

    public static void vibrateFailure(Context context) {
        if (!AppPreferences.isHapticFeedbackEnabled(context)) return;
        vibrate(context, new long[]{0, 400});
    }

    private static void vibrate(Context context, long[] pattern) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            vibrator.vibrate(pattern, -1);
        }
    }
}
