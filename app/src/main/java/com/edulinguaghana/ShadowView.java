package com.edulinguaghana;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class ShadowView extends View {

    private Paint shadowPaint;
    private Bitmap characterBitmap;
    private String text = "";
    private Paint textPaint;

    public ShadowView(Context context) {
        super(context);
        init();
    }

    public ShadowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        shadowPaint = new Paint();
        // Create a "shadow" effect by tinting everything gray
        shadowPaint.setColorFilter(new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN));
        shadowPaint.setAlpha(80); // Semi-transparent

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.LTGRAY);
        textPaint.setTextSize(200f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAlpha(80);
        textPaint.setFakeBoldText(true);
    }

    public void setCharacter(String character) {
        this.text = character;
        // In a real app, you'd load an image from a URL or resource using Glide
        // Glide.with(this).asBitmap().load(imageUrl).into(...)
        
        // For now, we render the text as a "shadow" on the canvas
        invalidate();
    }

    public void setImageUrl(String url) {
        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@androidx.annotation.NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        characterBitmap = resource;
                        invalidate();
                    }

                    @Override
                    public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                        characterBitmap = null;
                        invalidate();
                    }
                });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float x = getWidth() / 2f;
        float y = getHeight() / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f);

        if (characterBitmap != null) {
            // Center the bitmap
            float bx = (getWidth() - characterBitmap.getWidth()) / 2f;
            float by = (getHeight() - characterBitmap.getHeight()) / 2f;
            canvas.drawBitmap(characterBitmap, bx, by, shadowPaint);
        } else if (!text.isEmpty()) {
            canvas.drawText(text, x, y, textPaint);
        }
    }
}
