package com.edulinguaghana;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class ScratchRevealView extends View {

    private Bitmap overlayBitmap;
    private Canvas overlayCanvas;
    private Paint scratchPaint;
    private Path scratchPath;
    private Paint backgroundPaint;
    private Paint textPaint;
    private String hiddenText = "";
    private RevealListener revealListener;
    private boolean isRevealed = false;
    private float pixelsScratched = 0;

    public interface RevealListener {
        void onRevealed(String text);
    }

    public ScratchRevealView(Context context) {
        super(context);
        init();
    }

    public ScratchRevealView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        scratchPath = new Path();
        scratchPaint = new Paint();
        scratchPaint.setAntiAlias(true);
        scratchPaint.setDither(true);
        scratchPaint.setStrokeJoin(Paint.Join.ROUND);
        scratchPaint.setStrokeCap(Paint.Cap.ROUND);
        scratchPaint.setStrokeWidth(100f); // Width of the rubbing stroke
        scratchPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#C2B280")); // Sand color

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(300f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    public void setHiddenText(String text, RevealListener listener) {
        this.hiddenText = text;
        this.revealListener = listener;
        this.isRevealed = false;
        this.pixelsScratched = 0;
        reset();
    }

    private void reset() {
        if (getWidth() > 0 && getHeight() > 0) {
            initOverlay();
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initOverlay();
    }

    private void initOverlay() {
        if (getWidth() <= 0 || getHeight() <= 0) return;

        overlayBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        overlayCanvas = new Canvas(overlayBitmap);
        overlayCanvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        
        // Add some "texture" to the sand (optional, simple noise)
        Paint noisePaint = new Paint();
        noisePaint.setColor(Color.parseColor("#A89968"));
        for (int i = 0; i < 200; i++) {
            float x = (float) (Math.random() * getWidth());
            float y = (float) (Math.random() * getHeight());
            overlayCanvas.drawCircle(x, y, 2, noisePaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the hidden character first
        float x = getWidth() / 2f;
        float y = getHeight() / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(hiddenText, x, y, textPaint);

        // Draw the overlay bitmap (the sand/snow)
        if (overlayBitmap != null) {
            canvas.drawBitmap(overlayBitmap, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isRevealed) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                scratchPath.reset();
                scratchPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                scratchPath.lineTo(x, y);
                if (overlayCanvas != null) {
                    overlayCanvas.drawPath(scratchPath, scratchPaint);
                }
                checkRevealProgress();
                break;
            case MotionEvent.ACTION_UP:
                performClick();
                scratchPath.lineTo(x, y);
                if (overlayCanvas != null) {
                    overlayCanvas.drawPath(scratchPath, scratchPaint);
                }
                break;
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void checkRevealProgress() {
        if (isRevealed) return;

        // Roughly estimate progress based on area rubbed
        // Real pixel counting is expensive, so we'll use a simplified check or a small-scale bitmap
        
        // For children's game, simple "count moves" or "area covered" is often enough.
        // Let's do a simple threshold check.
        pixelsScratched += 1; // Simplified increment per move
        
        if (pixelsScratched > 150) { // Threshold for reveal
            isRevealed = true;
            if (revealListener != null) {
                revealListener.onRevealed(hiddenText);
            }
        }
    }
}
