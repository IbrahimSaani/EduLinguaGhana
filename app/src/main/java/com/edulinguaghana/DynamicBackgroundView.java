package com.edulinguaghana;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A custom view that provides an animated gradient background with floating particles.
 * This makes the app feel more alive and engaging for kids.
 */
public class DynamicBackgroundView extends View {

    private Paint backgroundPaint;
    private Paint particlePaint;
    private List<Particle> particles;
    private float animationOffset = 0f;
    private ValueAnimator gradientAnimator;
    private Random random = new Random();

    private int colorStart, colorMid, colorEnd;

    public DynamicBackgroundView(Context context) {
        super(context);
        init(context);
    }

    public DynamicBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DynamicBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particles = new ArrayList<>();

        colorStart = ContextCompat.getColor(context, R.color.bgDayStart);
        colorMid = ContextCompat.getColor(context, R.color.bgDayMid);
        colorEnd = ContextCompat.getColor(context, R.color.bgDayEnd);

        // Animate gradient shift
        gradientAnimator = ValueAnimator.ofFloat(0f, 1f);
        gradientAnimator.setDuration(10000);
        gradientAnimator.setRepeatCount(ValueAnimator.INFINITE);
        gradientAnimator.setRepeatMode(ValueAnimator.REVERSE);
        gradientAnimator.setInterpolator(new LinearInterpolator());
        gradientAnimator.addUpdateListener(animation -> {
            animationOffset = (float) animation.getAnimatedValue();
            invalidate();
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (gradientAnimator != null && !gradientAnimator.isStarted()) {
            gradientAnimator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (gradientAnimator != null) {
            gradientAnimator.cancel();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createParticles(w, h);
    }

    private void createParticles(int width, int height) {
        particles.clear();
        int count = 15; // Number of floating particles
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(width, height));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw animated gradient
        float shift = animationOffset * height * 0.2f;
        LinearGradient gradient = new LinearGradient(
                0, -shift,
                0, height + shift,
                new int[]{colorStart, colorMid, colorEnd},
                null,
                Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        // Draw and update particles
        for (Particle p : particles) {
            p.update(width, height);
            particlePaint.setColor(p.color);
            particlePaint.setAlpha(p.alpha);
            canvas.drawCircle(p.x, p.y, p.radius, particlePaint);
        }

        // Force redraw for smooth animation
        postInvalidateOnAnimation();
    }

    private class Particle {
        float x, y;
        float radius;
        float speedY;
        float speedX;
        int alpha;
        int color;

        Particle(int width, int height) {
            reset(width, height, true);
        }

        void reset(int width, int height, boolean initial) {
            x = random.nextFloat() * width;
            y = initial ? random.nextFloat() * height : height + 50;
            radius = 5 + random.nextFloat() * 15;
            speedY = -(0.5f + random.nextFloat() * 1.5f);
            speedX = (random.nextFloat() - 0.5f) * 0.5f;
            alpha = 30 + random.nextInt(100);
            
            // Subtle pastel colors for particles
            int[] colors = {0xFFFFFFFF, 0xFFFFE0BD, 0xFFE3F2FD, 0xFFFCE4EC};
            color = colors[random.nextInt(colors.length)];
        }

        void update(int width, int height) {
            y += speedY;
            x += speedX;
            if (y < -50) {
                reset(width, height, false);
            }
        }
    }
    
    public void setColors(int start, int mid, int end) {
        this.colorStart = start;
        this.colorMid = mid;
        this.colorEnd = end;
        invalidate();
    }
}
