package com.edulinguaghana;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

/**
 * Animated Avatar View - Displays avatar with live animations
 * Supports blinking, smiling, hair movement, accessory animations, and clothing movement
 */
public class AnimatedAvatarView extends androidx.appcompat.widget.AppCompatImageView {

    private AvatarBuilder.AvatarConfig config;
    private AvatarBuilder builder;

    // Animation state variables
    private long animationStartTime;
    private boolean isBlinking = false;
    private float blinkProgress = 0f;
    private float smileIntensity = 1f;
    private float hairSway = 0f;
    private float accessoryGlow = 0f;
    private float clothingWave = 0f;
    private boolean animationsEnabled = true;

    // Animation parameters
    private static final float BLINK_DURATION = 200f;
    private static final long BLINK_INTERVAL = 3000; // Blink every 3 seconds
    private long lastBlinkTime = 0;

    private static final float HAIR_SWAY_SPEED = 0.002f;
    private static final float HAIR_SWAY_AMOUNT = 5f;

    private static final float GLOW_SPEED = 0.003f;
    private static final float GLOW_MAX = 0.3f;

    private static final float CLOTHING_WAVE_SPEED = 0.0015f;
    private static final float CLOTHING_WAVE_AMOUNT = 3f;

    public AnimatedAvatarView(Context context) {
        super(context);
        init(context);
    }

    public AnimatedAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnimatedAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        config = AvatarBuilder.loadConfig(context);
        builder = new AvatarBuilder(context, config);
        animationStartTime = System.currentTimeMillis();

        // Start animation loop
        if (animationsEnabled) {
            startAnimations();
        } else {
            updateAvatar();
        }
    }

    public void setAvatarConfig(AvatarBuilder.AvatarConfig config) {
        this.config = config;
        this.builder = new AvatarBuilder(getContext(), config);
        if (animationsEnabled) {
            invalidate();
        } else {
            updateAvatar();
        }
    }

    public AvatarBuilder.AvatarConfig getAvatarConfig() {
        return config;
    }

    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
        if (enabled) {
            startAnimations();
        } else {
            updateAvatar();
        }
    }

    private void startAnimations() {
        post(animationRunnable);
    }

    private final Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!animationsEnabled) return;

            updateAnimationState();
            invalidate(); // Trigger redraw

            // Continue animation loop
            postDelayed(this, 16); // ~60 FPS
        }
    };

    private void updateAnimationState() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - animationStartTime;

        // Blinking animation
        if (currentTime - lastBlinkTime > BLINK_INTERVAL) {
            isBlinking = true;
            lastBlinkTime = currentTime;
        }

        if (isBlinking) {
            long blinkElapsed = currentTime - lastBlinkTime;
            if (blinkElapsed < BLINK_DURATION / 2) {
                // Closing eyes
                blinkProgress = (float) blinkElapsed / (BLINK_DURATION / 2);
            } else if (blinkElapsed < BLINK_DURATION) {
                // Opening eyes
                blinkProgress = 1f - ((float) (blinkElapsed - BLINK_DURATION / 2) / (BLINK_DURATION / 2));
            } else {
                // Blink complete
                isBlinking = false;
                blinkProgress = 0f;
            }
        }

        // Smile intensity (subtle breathing effect)
        smileIntensity = 1f + (float) Math.sin(elapsed * 0.001) * 0.05f;

        // Hair sway
        hairSway = (float) Math.sin(elapsed * HAIR_SWAY_SPEED) * HAIR_SWAY_AMOUNT;

        // Accessory glow (pulsing effect)
        accessoryGlow = (float) Math.abs(Math.sin(elapsed * GLOW_SPEED)) * GLOW_MAX;

        // Clothing wave
        clothingWave = (float) Math.sin(elapsed * CLOTHING_WAVE_SPEED) * CLOTHING_WAVE_AMOUNT;
    }

    private void updateAvatar() {
        post(() -> {
            int size = Math.max(getWidth(), getHeight());
            if (size == 0) size = 200;

            Bitmap avatarBitmap = builder.drawAvatar(size);
            setImageBitmap(avatarBitmap);
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!animationsEnabled) {
            super.onDraw(canvas);
            return;
        }

        // Draw animated avatar directly
        int size = Math.max(getWidth(), getHeight());
        if (size == 0) {
            super.onDraw(canvas);
            return;
        }

        drawAnimatedAvatar(canvas, size);
    }

    private void drawAnimatedAvatar(Canvas canvas, int size) {
        // Draw background
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor(config.backgroundColor));
        canvas.drawRect(0, 0, size, size, bgPaint);

        float centerX = size / 2f;
        float centerY = size / 2f;
        float faceRadius = size * 0.35f;

        // Draw body/clothing with wave animation
        drawAnimatedBody(canvas, centerX, centerY, faceRadius);

        // Draw hair with sway animation
        drawAnimatedHair(canvas, centerX, centerY, faceRadius);

        // Draw face
        drawFace(canvas, centerX, centerY, faceRadius);

        // Draw eyes with blink animation
        drawAnimatedEyes(canvas, centerX, centerY, faceRadius);

        // Draw mouth with smile animation
        drawAnimatedMouth(canvas, centerX, centerY, faceRadius);

        // Draw accessory with glow animation
        drawAnimatedAccessory(canvas, centerX, centerY, faceRadius);
    }

    private void drawAnimatedBody(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint clothingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clothingPaint.setColor(Color.parseColor(config.clothingColor.color));
        clothingPaint.setStyle(Paint.Style.FILL);

        float bodyY = centerY + faceRadius * 0.7f + clothingWave * 0.5f;
        float bodyWidth = faceRadius * 1.3f;
        float bodyHeight = faceRadius * 1.8f;

        // Apply subtle wave motion to clothing
        canvas.save();
        canvas.translate(clothingWave * 0.3f, 0);

        // Draw based on clothing style (simplified for animation)
        RectF clothing = new RectF(centerX - bodyWidth, bodyY,
            centerX + bodyWidth, bodyY + bodyHeight);
        canvas.drawRoundRect(clothing, 20, 20, clothingPaint);

        canvas.restore();
    }

    private void drawAnimatedHair(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint hairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairPaint.setColor(Color.parseColor(config.hairColor.color));
        hairPaint.setStyle(Paint.Style.FILL);

        // Apply sway animation
        canvas.save();
        canvas.translate(hairSway, hairSway * 0.5f);

        // Draw simplified hair with sway
        RectF hairRect = new RectF(
            centerX - faceRadius * 1.05f,
            centerY - faceRadius * 1.05f,
            centerX + faceRadius * 1.05f,
            centerY + faceRadius * 0.1f
        );
        canvas.drawArc(hairRect, 180, 180, true, hairPaint);

        canvas.restore();
    }

    private void drawFace(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint facePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        facePaint.setColor(Color.parseColor(config.skinTone.color));
        facePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, faceRadius, facePaint);

        // Add blush
        Paint blushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blushPaint.setColor(Color.parseColor("#40FF6B9D"));
        blushPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX - faceRadius * 0.5f, centerY + faceRadius * 0.2f, faceRadius * 0.2f, blushPaint);
        canvas.drawCircle(centerX + faceRadius * 0.5f, centerY + faceRadius * 0.2f, faceRadius * 0.2f, blushPaint);
    }

    private void drawAnimatedEyes(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint eyeWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeWhitePaint.setColor(Color.WHITE);
        eyeWhitePaint.setStyle(Paint.Style.FILL);

        Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.BLACK);
        eyePaint.setStyle(Paint.Style.FILL);

        float eyeY = centerY - faceRadius * 0.15f;
        float eyeSpacing = faceRadius * 0.35f;
        float eyeRadius = faceRadius * 0.15f;
        float pupilRadius = faceRadius * 0.08f;

        // Apply blink animation
        float blinkScale = 1f - blinkProgress;

        // Left eye
        canvas.save();
        canvas.scale(1f, blinkScale, centerX - eyeSpacing, eyeY);
        canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius, eyeWhitePaint);
        canvas.drawCircle(centerX - eyeSpacing, eyeY, pupilRadius, eyePaint);
        canvas.restore();

        // Right eye
        canvas.save();
        canvas.scale(1f, blinkScale, centerX + eyeSpacing, eyeY);
        canvas.drawCircle(centerX + eyeSpacing, eyeY, eyeRadius, eyeWhitePaint);
        canvas.drawCircle(centerX + eyeSpacing, eyeY, pupilRadius, eyePaint);
        canvas.restore();
    }

    private void drawAnimatedMouth(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthPaint.setColor(Color.parseColor("#C62828"));
        mouthPaint.setStyle(Paint.Style.STROKE);
        mouthPaint.setStrokeWidth(7);
        mouthPaint.setStrokeCap(Paint.Cap.ROUND);

        float mouthY = centerY + faceRadius * 0.35f;
        float mouthWidth = faceRadius * 0.5f * smileIntensity;

        // Animated smile
        RectF smileRect = new RectF(centerX - mouthWidth, mouthY - faceRadius * 0.15f,
            centerX + mouthWidth, mouthY + faceRadius * 0.35f);
        canvas.drawArc(smileRect, 10, 160, false, mouthPaint);
    }

    private void drawAnimatedAccessory(Canvas canvas, float centerX, float centerY, float faceRadius) {
        if (config.accessory == AvatarBuilder.Accessory.NONE) return;

        Paint accessoryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accessoryPaint.setColor(Color.parseColor("#FFD700"));
        accessoryPaint.setStyle(Paint.Style.FILL);

        // Add glow effect
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(Color.parseColor("#40FFD700"));
        glowPaint.setStyle(Paint.Style.FILL);

        // Draw crown with glow (example)
        if (config.accessory == AvatarBuilder.Accessory.CROWN) {
            // Draw glow
            canvas.save();
            float glowScale = 1f + accessoryGlow;
            canvas.scale(glowScale, glowScale, centerX, centerY - faceRadius * 0.7f);

            Path crownGlow = new Path();
            crownGlow.moveTo(centerX - faceRadius * 1.1f, centerY - faceRadius * 0.75f);
            crownGlow.lineTo(centerX - faceRadius * 0.7f, centerY - faceRadius * 1.3f);
            crownGlow.lineTo(centerX - faceRadius * 0.35f, centerY - faceRadius * 0.85f);
            crownGlow.lineTo(centerX, centerY - faceRadius * 1.4f);
            crownGlow.lineTo(centerX + faceRadius * 0.35f, centerY - faceRadius * 0.85f);
            crownGlow.lineTo(centerX + faceRadius * 0.7f, centerY - faceRadius * 1.3f);
            crownGlow.lineTo(centerX + faceRadius * 1.1f, centerY - faceRadius * 0.75f);
            crownGlow.close();
            canvas.drawPath(crownGlow, glowPaint);

            canvas.restore();

            // Draw crown
            Path crown = new Path();
            crown.moveTo(centerX - faceRadius * 1.1f, centerY - faceRadius * 0.75f);
            crown.lineTo(centerX - faceRadius * 0.7f, centerY - faceRadius * 1.3f);
            crown.lineTo(centerX - faceRadius * 0.35f, centerY - faceRadius * 0.85f);
            crown.lineTo(centerX, centerY - faceRadius * 1.4f);
            crown.lineTo(centerX + faceRadius * 0.35f, centerY - faceRadius * 0.85f);
            crown.lineTo(centerX + faceRadius * 0.7f, centerY - faceRadius * 1.3f);
            crown.lineTo(centerX + faceRadius * 1.1f, centerY - faceRadius * 0.75f);
            crown.lineTo(centerX + faceRadius * 1.1f, centerY - faceRadius * 0.55f);
            crown.lineTo(centerX - faceRadius * 1.1f, centerY - faceRadius * 0.55f);
            crown.close();
            canvas.drawPath(crown, accessoryPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && !animationsEnabled) {
            updateAvatar();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Stop animations when view is detached
        removeCallbacks(animationRunnable);
    }
}

