package com.edulinguaghana;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view to display user avatar
 */
public class AvatarView extends androidx.appcompat.widget.AppCompatImageView {

    private AvatarBuilder.AvatarConfig config;
    private AvatarBuilder builder;

    public AvatarView(Context context) {
        super(context);
        init(context);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        config = AvatarBuilder.loadConfig(context);
        builder = new AvatarBuilder(context, config);
        updateAvatar();
    }

    public void setAvatarConfig(AvatarBuilder.AvatarConfig config) {
        this.config = config;
        if (config != null) {
            this.builder = new AvatarBuilder(getContext(), config);
            updateAvatar();
        }
    }

    public AvatarBuilder.AvatarConfig getAvatarConfig() {
        return config;
    }

    public void updateAvatar() {
        if (config == null) return;
        post(() -> {
            if (config == null) return;
            int size = Math.max(getWidth(), getHeight());
            if (size == 0) size = 200; // Default size

            Bitmap avatarBitmap = builder.drawAvatar(size);
            setImageBitmap(avatarBitmap);
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            updateAvatar();
        }
    }
}

