package com.edulinguaghana;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class ShadowView extends View {

    private Paint shadowPaint;
    private Bitmap characterBitmap;
    private String text = "";
    private Paint textPaint;
    private Paint glowPaint;
    
    private float pulseValue = 1.0f;
    private ValueAnimator pulseAnimator;
    private boolean isRevealed = false;
    private int revealColor = Color.parseColor("#4CAF50"); // Default green

    public ShadowView(Context context) {
        super(context);
        init();
    }

    public ShadowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Create a dark "silhouette" effect
        shadowPaint.setColorFilter(new PorterDuffColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN));
        shadowPaint.setAlpha(120);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(240f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAlpha(120);
        textPaint.setFakeBoldText(true);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(10f);
        glowPaint.setColor(Color.WHITE);
        glowPaint.setAlpha(50);

        startPulseAnimation();
    }

    private void startPulseAnimation() {
        pulseAnimator = ValueAnimator.ofFloat(0.95f, 1.05f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            pulseValue = (float) animation.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    public void setCharacter(String character) {
        this.text = character;
        this.isRevealed = false;
        textPaint.setColor(Color.DKGRAY);
        textPaint.setAlpha(120);
        invalidate();
    }

    public void setImageUrl(String url) {
        this.isRevealed = false;
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

    public void reveal(int color) {
        this.isRevealed = true;
        this.revealColor = color;
        
        // Animate the reveal
        ValueAnimator revealAnim = ValueAnimator.ofInt(120, 255);
        revealAnim.setDuration(500);
        revealAnim.addUpdateListener(animation -> {
            int alpha = (int) animation.getAnimatedValue();
            textPaint.setAlpha(alpha);
            shadowPaint.setAlpha(alpha);
            invalidate();
        });
        
        textPaint.setColor(color);
        shadowPaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        
        revealAnim.start();
        
        // Stop pulse or speed it up? Let's keep it pulsing but maybe bigger
        pulseAnimator.cancel();
        pulseAnimator = ValueAnimator.ofFloat(1.0f, 1.2f, 1.0f);
        pulseAnimator.setDuration(400);
        pulseAnimator.start();
    }

    @Override
    protected void onDraw(@androidx.annotation.NonNull Canvas canvas) {
        super.onDraw(canvas);
        
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        
        canvas.save();
        canvas.scale(pulseValue, pulseValue, centerX, centerY);

        float textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2f);

        if (characterBitmap != null) {
            float bx = (getWidth() - characterBitmap.getWidth()) / 2f;
            float by = (getHeight() - characterBitmap.getHeight()) / 2f;
            canvas.drawBitmap(characterBitmap, bx, by, isRevealed ? null : shadowPaint);
        } else if (!text.isEmpty()) {
            // Draw a subtle glow/shadow behind the text
            textPaint.setShadowLayer(15 * pulseValue, 0, 0, isRevealed ? revealColor : Color.BLACK);
            canvas.drawText(text, centerX, textY, textPaint);
            textPaint.clearShadowLayer();
        }
        
        // Draw an interactive "border" if not revealed
        if (!isRevealed) {
            glowPaint.setAlpha((int) (50 * (pulseValue - 0.9f) / 0.15f));
            canvas.drawCircle(centerX, centerY, (getWidth() / 2f - 20) * pulseValue, glowPaint);
        }

        canvas.restore();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
    }
}
