package com.edulinguaghana;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScratchRevealView extends View {

    private Bitmap overlayBitmap;
    private Canvas overlayCanvas;
    private Paint scratchPaint;
    private Path scratchPath;
    private Paint backgroundPaint;
    private Paint textPaint;
    private Paint particlePaint;
    private Paint overlayPaint;
    
    private String hiddenText = "";
    private RevealListener revealListener;
    private boolean isRevealed = false;
    private float pixelsScratched = 0;
    private final Random random = new Random();
    private int overlayAlpha = 255;
    
    private final List<Particle> particles = new ArrayList<>();
    private static final int MAX_PARTICLES = 40;

    public interface RevealListener {
        void onRevealed(String text);
    }

    private static class Particle {
        float x, y, vx, vy, alpha, size;
        int color;
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
        scratchPaint.setStrokeWidth(120f);
        scratchPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#1A237E")); // primary color
        textPaint.setTextSize(320f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setShadowLayer(10, 0, 0, Color.LTGRAY);

        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setStyle(Paint.Style.FILL);

        overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setHiddenText(String text, RevealListener listener) {
        this.hiddenText = text;
        this.revealListener = listener;
        this.isRevealed = false;
        this.pixelsScratched = 0;
        this.overlayAlpha = 255;
        this.particles.clear();
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
        
        LinearGradient gradient = new LinearGradient(0, 0, getWidth(), getHeight(),
                new int[]{Color.parseColor("#D4C491"), Color.parseColor("#C2B280"), Color.parseColor("#B1A170")},
                null, Shader.TileMode.CLAMP);
        backgroundPaint.setShader(gradient);
        overlayCanvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        backgroundPaint.setShader(null);
        
        Paint noisePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        noisePaint.setColor(Color.parseColor("#33000000"));
        for (int i = 0; i < 400; i++) {
            float x = random.nextFloat() * getWidth();
            float y = random.nextFloat() * getHeight();
            float size = random.nextFloat() * 3 + 1;
            overlayCanvas.drawCircle(x, y, size, noisePaint);
        }
    }

    @Override
    protected void onDraw(@androidx.annotation.NonNull Canvas canvas) {
        float x = getWidth() / 2f;
        float y = getHeight() / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(hiddenText, x, y, textPaint);

        if (overlayBitmap != null) {
            overlayPaint.setAlpha(overlayAlpha);
            canvas.drawBitmap(overlayBitmap, 0, 0, overlayPaint);
        }
        
        drawParticles(canvas);
    }

    private void drawParticles(Canvas canvas) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            particlePaint.setColor(p.color);
            particlePaint.setAlpha((int) (p.alpha * 255));
            canvas.drawCircle(p.x, p.y, p.size, particlePaint);
            
            p.x += p.vx;
            p.y += p.vy;
            p.alpha -= 0.04f;
            if (p.alpha <= 0) {
                particles.remove(i);
            }
        }
        if (!particles.isEmpty()) {
            invalidate();
        }
    }

    private void addParticles(float x, float y) {
        for (int i = 0; i < 4; i++) {
            if (particles.size() >= MAX_PARTICLES) break;
            Particle p = new Particle();
            p.x = x;
            p.y = y;
            p.vx = (random.nextFloat() - 0.5f) * 12;
            p.vy = (random.nextFloat() - 0.5f) * 12;
            p.alpha = 1.0f;
            p.size = random.nextFloat() * 12 + 6;
            p.color = Color.parseColor("#C2B280");
            particles.add(p);
        }
        invalidate();
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
                addParticles(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                scratchPath.lineTo(x, y);
                if (overlayCanvas != null) {
                    overlayCanvas.drawPath(scratchPath, scratchPaint);
                }
                addParticles(x, y);
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

        pixelsScratched += 3.0f;
        
        if (pixelsScratched > 180) {
            isRevealed = true;
            revealFully();
        }
    }

    private void revealFully() {
        ValueAnimator fadeOut = ValueAnimator.ofInt(255, 0);
        fadeOut.setDuration(600);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOut.addUpdateListener(animation -> {
            overlayAlpha = (int) animation.getAnimatedValue();
            if (overlayAlpha == 0) {
                overlayBitmap = null;
            }
            invalidate();
        });
        fadeOut.start();

        if (revealListener != null) {
            revealListener.onRevealed(hiddenText);
        }
    }
}
