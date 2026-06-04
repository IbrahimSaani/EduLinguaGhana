package com.edulinguaghana;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Avatar Builder - Creates customizable avatars for users
 * Supports different styles: face shapes, hair, eyes, mouths, accessories
 */
public class AvatarBuilder {

    // Avatar Components
    public enum SkinTone {
        LIGHT("#FFE0BD"),
        MEDIUM("#F1C27D"),
        TAN("#C68642"),
        BROWN("#8D5524"),
        DARK("#5D4037");

        public final String color;
        SkinTone(String color) { this.color = color; }
    }

    public enum HairStyle {
        SHORT, LONG, CURLY, BALD, AFRO, BRAIDS, PONYTAIL, SPIKY, MOHAWK, BUN, SIDEPART
    }

    public enum HairColor {
        BLACK("#000000"),
        BROWN("#4A2511"),
        BLONDE("#F9E076"),
        RED("#D84315"),
        GRAY("#9E9E9E"),
        PURPLE("#9C27B0"),
        BLUE("#2196F3"),
        PINK("#E91E63");

        public final String color;
        HairColor(String color) { this.color = color; }
    }

    public enum EyeStyle {
        NORMAL, HAPPY, WINK, GLASSES, SUNGLASSES, STARRY, SLEEPY, HEART
    }

    public enum MouthStyle {
        SMILE, LAUGH, NEUTRAL, SMIRK, SURPRISED, TONGUE_OUT, WHISTLING
    }

    public enum Accessory {
        NONE, HAT, CROWN, HEADBAND, EARRINGS, NECKLACE, BOWTIE, SCARF, FLOWER, MASK
    }

    public enum ClothingStyle {
        TSHIRT, HOODIE, DRESS, SUIT, CASUAL, TRADITIONAL
    }

    public enum ClothingColor {
        RED("#F44336"),
        BLUE("#2196F3"),
        GREEN("#4CAF50"),
        YELLOW("#FFEB3B"),
        PURPLE("#9C27B0"),
        ORANGE("#FF9800"),
        PINK("#E91E63"),
        BLACK("#212121"),
        WHITE("#FAFAFA");

        public final String color;
        ClothingColor(String color) { this.color = color; }
    }

    public enum FacialExpression {
        NEUTRAL, HAPPY, EXCITED, COOL, SURPRISED, SHY
    }

    // Avatar Configuration
    public static class AvatarConfig {
        public SkinTone skinTone = SkinTone.MEDIUM;
        public HairStyle hairStyle = HairStyle.SHORT;
        public HairColor hairColor = HairColor.BLACK;
        public EyeStyle eyeStyle = EyeStyle.NORMAL;
        public MouthStyle mouthStyle = MouthStyle.SMILE;
        public Accessory accessory = Accessory.NONE;
        public ClothingStyle clothingStyle = ClothingStyle.TSHIRT;
        public ClothingColor clothingColor = ClothingColor.BLUE;
        public FacialExpression facialExpression = FacialExpression.NEUTRAL;
        public String backgroundColor = "#E3F2FD";

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("skinTone", skinTone.name());
            map.put("hairStyle", hairStyle.name());
            map.put("hairColor", hairColor.name());
            map.put("eyeStyle", eyeStyle.name());
            map.put("mouthStyle", mouthStyle.name());
            map.put("accessory", accessory.name());
            map.put("clothingStyle", clothingStyle.name());
            map.put("clothingColor", clothingColor.name());
            map.put("facialExpression", facialExpression.name());
            map.put("backgroundColor", backgroundColor);
            return map;
        }

        public static AvatarConfig fromMap(Map<String, Object> map) {
            AvatarConfig config = new AvatarConfig();
            if (map == null) return config;

            try {
                if (map.containsKey("skinTone"))
                    config.skinTone = SkinTone.valueOf((String) map.get("skinTone"));
                if (map.containsKey("hairStyle"))
                    config.hairStyle = HairStyle.valueOf((String) map.get("hairStyle"));
                if (map.containsKey("hairColor"))
                    config.hairColor = HairColor.valueOf((String) map.get("hairColor"));
                if (map.containsKey("eyeStyle"))
                    config.eyeStyle = EyeStyle.valueOf((String) map.get("eyeStyle"));
                if (map.containsKey("mouthStyle"))
                    config.mouthStyle = MouthStyle.valueOf((String) map.get("mouthStyle"));
                if (map.containsKey("accessory"))
                    config.accessory = Accessory.valueOf((String) map.get("accessory"));
                if (map.containsKey("clothingStyle"))
                    config.clothingStyle = ClothingStyle.valueOf((String) map.get("clothingStyle"));
                if (map.containsKey("clothingColor"))
                    config.clothingColor = ClothingColor.valueOf((String) map.get("clothingColor"));
                if (map.containsKey("facialExpression"))
                    config.facialExpression = FacialExpression.valueOf((String) map.get("facialExpression"));
                if (map.containsKey("backgroundColor"))
                    config.backgroundColor = (String) map.get("backgroundColor");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return config;
        }
    }

    private Context context;
    private AvatarConfig config;

    public AvatarBuilder(Context context) {
        this.context = context;
        this.config = loadConfig(context);
        if (this.config == null) this.config = new AvatarConfig();
    }

    public AvatarBuilder(Context context, AvatarConfig config) {
        this.context = context;
        this.config = config != null ? config : new AvatarConfig();
    }

    /**
     * Generate a random avatar configuration
     */
    public static AvatarConfig generateRandom() {
        Random random = new Random();
        AvatarConfig config = new AvatarConfig();

        SkinTone[] skins = SkinTone.values();
        config.skinTone = skins[random.nextInt(skins.length)];

        HairStyle[] hairs = HairStyle.values();
        config.hairStyle = hairs[random.nextInt(hairs.length)];

        HairColor[] hairColors = HairColor.values();
        config.hairColor = hairColors[random.nextInt(hairColors.length)];

        EyeStyle[] eyes = EyeStyle.values();
        config.eyeStyle = eyes[random.nextInt(eyes.length)];

        MouthStyle[] mouths = MouthStyle.values();
        config.mouthStyle = mouths[random.nextInt(mouths.length)];

        Accessory[] accessories = Accessory.values();
        config.accessory = accessories[random.nextInt(accessories.length)];

        ClothingStyle[] clothingStyles = ClothingStyle.values();
        config.clothingStyle = clothingStyles[random.nextInt(clothingStyles.length)];

        ClothingColor[] clothingColors = ClothingColor.values();
        config.clothingColor = clothingColors[random.nextInt(clothingColors.length)];

        config.facialExpression = FacialExpression.NEUTRAL;

        return config;
    }

    /**
     * Draw avatar to bitmap
     */
    public Bitmap drawAvatar(int size) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw background
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor(config.backgroundColor));
        canvas.drawRect(0, 0, size, size, bgPaint);

        float centerX = size / 2f;
        float centerY = size / 2f;
        float faceRadius = size * 0.35f;

        // Draw components in order
        drawBody(canvas, centerX, centerY, faceRadius);
        drawHairBehind(canvas, centerX, centerY, faceRadius);
        drawFace(canvas, centerX, centerY, faceRadius);
        drawEyes(canvas, centerX, centerY, faceRadius);
        drawMouth(canvas, centerX, centerY, faceRadius);
        drawHairFront(canvas, centerX, centerY, faceRadius);
        drawAccessory(canvas, centerX, centerY, faceRadius);

        return bitmap;
    }

    private void drawBody(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint clothingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        clothingPaint.setColor(Color.parseColor(config.clothingColor.color));
        clothingPaint.setStyle(Paint.Style.FILL);

        Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.parseColor("#33000000"));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);

        float bodyY = centerY + faceRadius * 0.75f;
        float bodyWidth = faceRadius * 1.3f;
        float bodyHeight = faceRadius * 1.5f;

        // Neck
        Paint neckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        neckPaint.setColor(Color.parseColor(config.skinTone.color));
        canvas.drawRect(centerX - faceRadius * 0.25f, centerY + faceRadius * 0.6f,
            centerX + faceRadius * 0.25f, bodyY, neckPaint);

        switch (config.clothingStyle) {
            case TSHIRT: {
                RectF tshirt = new RectF(centerX - bodyWidth, bodyY, centerX + bodyWidth, bodyY + bodyHeight);
                canvas.drawRoundRect(tshirt, 30, 30, clothingPaint);
                canvas.drawRoundRect(tshirt, 30, 30, outlinePaint);
                break;
            }
            case HOODIE: {
                RectF hoodie = new RectF(centerX - bodyWidth * 1.1f, bodyY, centerX + bodyWidth * 1.1f, bodyY + bodyHeight);
                canvas.drawRoundRect(hoodie, 40, 40, clothingPaint);
                canvas.drawRoundRect(hoodie, 40, 40, outlinePaint);
                break;
            }
            case DRESS: {
                Path dress = new Path();
                dress.moveTo(centerX - bodyWidth * 0.8f, bodyY);
                dress.lineTo(centerX - bodyWidth * 1.4f, bodyY + bodyHeight);
                dress.lineTo(centerX + bodyWidth * 1.4f, bodyY + bodyHeight);
                dress.lineTo(centerX + bodyWidth * 0.8f, bodyY);
                dress.close();
                canvas.drawPath(dress, clothingPaint);
                canvas.drawPath(dress, outlinePaint);
                break;
            }
            case SUIT: {
                RectF suit = new RectF(centerX - bodyWidth * 1.05f, bodyY, centerX + bodyWidth * 1.05f, bodyY + bodyHeight);
                canvas.drawRoundRect(suit, 25, 25, clothingPaint);
                canvas.drawRoundRect(suit, 25, 25, outlinePaint);
                break;
            }
            case TRADITIONAL: {
                RectF trad = new RectF(centerX - bodyWidth * 1.2f, bodyY, centerX + bodyWidth * 1.2f, bodyY + bodyHeight);
                canvas.drawRoundRect(trad, 20, 20, clothingPaint);
                canvas.drawRoundRect(trad, 20, 20, outlinePaint);
                break;
            }
            default: {
                RectF casual = new RectF(centerX - bodyWidth, bodyY, centerX + bodyWidth, bodyY + bodyHeight);
                canvas.drawRoundRect(casual, 25, 25, clothingPaint);
                canvas.drawRoundRect(casual, 25, 25, outlinePaint);
                break;
            }
        }
    }

    private void drawFace(Canvas canvas, float centerX, float centerY, float radius) {
        Paint facePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        facePaint.setColor(Color.parseColor(config.skinTone.color));
        canvas.drawCircle(centerX, centerY, radius, facePaint);

        Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.parseColor("#40000000"));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(4);
        canvas.drawCircle(centerX, centerY, radius, outlinePaint);
    }

    private void drawHairBehind(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint hairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairPaint.setColor(Color.parseColor(config.hairColor.color));

        switch (config.hairStyle) {
            case LONG:
                RectF sides = new RectF(centerX - faceRadius * 1.1f, centerY - faceRadius * 0.3f, 
                                       centerX + faceRadius * 1.1f, centerY + faceRadius * 1.3f);
                canvas.drawRect(sides, hairPaint);
                break;
            case AFRO:
                canvas.drawCircle(centerX, centerY - faceRadius * 0.3f, faceRadius * 1.3f, hairPaint);
                break;
            case PONYTAIL:
                canvas.drawCircle(centerX, centerY - faceRadius * 1.2f, faceRadius * 0.45f, hairPaint);
                break;
            case BUN:
                canvas.drawCircle(centerX, centerY - faceRadius * 1.25f, faceRadius * 0.4f, hairPaint);
                break;
        }
    }

    private void drawHairFront(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint hairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairPaint.setColor(Color.parseColor(config.hairColor.color));
        
        Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.parseColor("#20000000"));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);

        switch (config.hairStyle) {
            case BALD:
                break;
            case SHORT:
            case LONG:
            case PONYTAIL:
            case BUN:
            case SIDEPART: {
                RectF topHair = new RectF(centerX - faceRadius * 1.02f, centerY - faceRadius * 1.05f, 
                                         centerX + faceRadius * 1.02f, centerY - faceRadius * 0.1f);
                canvas.drawArc(topHair, 180, 180, true, hairPaint);
                canvas.drawArc(topHair, 180, 180, true, outlinePaint);
                if (config.hairStyle == HairStyle.SIDEPART) {
                    Paint partPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    partPaint.setColor(Color.parseColor(config.skinTone.color));
                    canvas.drawRect(centerX - faceRadius * 0.3f, centerY - faceRadius * 1.05f, 
                                  centerX - faceRadius * 0.25f, centerY - faceRadius * 0.6f, partPaint);
                }
                break;
            }
            case CURLY:
                // Raised curls to keep eyes clear
                canvas.drawCircle(centerX, centerY - faceRadius * 1.0f, faceRadius * 0.6f, hairPaint);
                canvas.drawCircle(centerX - faceRadius * 0.7f, centerY - faceRadius * 0.8f, faceRadius * 0.5f, hairPaint);
                canvas.drawCircle(centerX + faceRadius * 0.7f, centerY - faceRadius * 0.8f, faceRadius * 0.5f, hairPaint);
                canvas.drawCircle(centerX - faceRadius * 0.95f, centerY - faceRadius * 0.4f, faceRadius * 0.4f, hairPaint);
                canvas.drawCircle(centerX + faceRadius * 0.95f, centerY - faceRadius * 0.4f, faceRadius * 0.4f, hairPaint);
                break;
            case BRAIDS: {
                RectF braidBase = new RectF(centerX - faceRadius * 0.98f, centerY - faceRadius * 1.05f, 
                                           centerX + faceRadius * 0.98f, centerY - faceRadius * 0.4f);
                canvas.drawRect(braidBase, hairPaint);
                for (int b = 0; b < 3; b++) {
                    float y = centerY - faceRadius * 0.1f + b * faceRadius * 0.35f;
                    canvas.drawCircle(centerX - faceRadius * 0.9f, y, faceRadius * 0.25f, hairPaint);
                    canvas.drawCircle(centerX + faceRadius * 0.9f, y, faceRadius * 0.25f, hairPaint);
                }
                break;
            }
            case SPIKY: {
                // Spiky hair implementation
                RectF base = new RectF(centerX - faceRadius * 1.02f, centerY - faceRadius * 1.05f, 
                                      centerX + faceRadius * 1.02f, centerY - faceRadius * 0.1f);
                canvas.drawArc(base, 180, 180, true, hairPaint);
                
                for (int i = 0; i < 7; i++) {
                    float x = centerX - faceRadius * 0.9f + i * faceRadius * 0.3f;
                    Path spike = new Path();
                    spike.moveTo(x - faceRadius * 0.15f, centerY - faceRadius * 0.8f);
                    spike.lineTo(x, centerY - faceRadius * 1.3f);
                    spike.lineTo(x + faceRadius * 0.15f, centerY - faceRadius * 0.8f);
                    spike.close();
                    canvas.drawPath(spike, hairPaint);
                }
                break;
            }
            case MOHAWK:
                // Stop mohawk higher on the head
                for (int m = 0; m < 5; m++) {
                    float y = centerY - faceRadius * 1.35f + m * faceRadius * 0.15f;
                    canvas.drawCircle(centerX, y, faceRadius * 0.22f, hairPaint);
                }
                break;
        }
    }

    private void drawEyes(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.BLACK);
        
        float eyeY = centerY - faceRadius * 0.15f;
        float eyeXOffset = faceRadius * 0.35f;
        float eyeSize = faceRadius * 0.12f;

        switch (config.eyeStyle) {
            case NORMAL:
                canvas.drawCircle(centerX - eyeXOffset, eyeY, eyeSize, eyePaint);
                canvas.drawCircle(centerX + eyeXOffset, eyeY, eyeSize, eyePaint);
                break;
            case HAPPY: {
                Paint happyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                happyPaint.setStyle(Paint.Style.STROKE);
                happyPaint.setStrokeWidth(6);
                RectF leftH = new RectF(centerX - eyeXOffset - eyeSize, eyeY - eyeSize, centerX - eyeXOffset + eyeSize, eyeY + eyeSize);
                RectF rightH = new RectF(centerX + eyeXOffset - eyeSize, eyeY - eyeSize, centerX + eyeXOffset + eyeSize, eyeY + eyeSize);
                canvas.drawArc(leftH, 180, 180, false, happyPaint);
                canvas.drawArc(rightH, 180, 180, false, happyPaint);
                break;
            }
            case WINK: {
                canvas.drawCircle(centerX - eyeXOffset, eyeY, eyeSize, eyePaint);
                Paint winkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                winkPaint.setStyle(Paint.Style.STROKE);
                winkPaint.setStrokeWidth(6);
                RectF rightW = new RectF(centerX + eyeXOffset - eyeSize, eyeY - eyeSize, centerX + eyeXOffset + eyeSize, eyeY + eyeSize);
                canvas.drawArc(rightW, 0, 180, false, winkPaint);
                break;
            }
            case GLASSES: {
                Paint glassFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                glassFramePaint.setStyle(Paint.Style.STROKE);
                glassFramePaint.setStrokeWidth(4);
                glassFramePaint.setColor(Color.BLACK);
                canvas.drawCircle(centerX - eyeXOffset, eyeY, eyeSize * 1.8f, glassFramePaint);
                canvas.drawCircle(centerX + eyeXOffset, eyeY, eyeSize * 1.8f, glassFramePaint);
                canvas.drawLine(centerX - eyeXOffset + eyeSize * 1.8f, eyeY, centerX + eyeXOffset - eyeSize * 1.8f, eyeY, glassFramePaint);
                canvas.drawCircle(centerX - eyeXOffset, eyeY, eyeSize, eyePaint);
                canvas.drawCircle(centerX + eyeXOffset, eyeY, eyeSize, eyePaint);
                break;
            }
            case SUNGLASSES: {
                Paint sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                sunPaint.setColor(Color.parseColor("#333333"));
                canvas.drawRoundRect(centerX - eyeXOffset - eyeSize * 1.5f, eyeY - eyeSize, 
                                    centerX - eyeXOffset + eyeSize * 1.5f, eyeY + eyeSize, 10, 10, sunPaint);
                canvas.drawRoundRect(centerX + eyeXOffset - eyeSize * 1.5f, eyeY - eyeSize, 
                                    centerX + eyeXOffset + eyeSize * 1.5f, eyeY + eyeSize, 10, 10, sunPaint);
                canvas.drawRect(centerX - faceRadius * 0.15f, eyeY - 2, centerX + faceRadius * 0.15f, eyeY + 2, sunPaint);
                break;
            }
            case STARRY:
                drawStar(canvas, centerX - eyeXOffset, eyeY, eyeSize * 1.5f, eyePaint);
                drawStar(canvas, centerX + eyeXOffset, eyeY, eyeSize * 1.5f, eyePaint);
                break;
            case SLEEPY: {
                Paint sleepyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                sleepyPaint.setStyle(Paint.Style.STROKE);
                sleepyPaint.setStrokeWidth(5);
                canvas.drawLine(centerX - eyeXOffset - eyeSize, eyeY, centerX - eyeXOffset + eyeSize, eyeY, sleepyPaint);
                canvas.drawLine(centerX + eyeXOffset - eyeSize, eyeY, centerX + eyeXOffset + eyeSize, eyeY, sleepyPaint);
                break;
            }
            case HEART:
                Paint heartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                heartPaint.setColor(Color.RED);
                drawHeart(canvas, centerX - eyeXOffset, eyeY, eyeSize * 2, heartPaint);
                drawHeart(canvas, centerX + eyeXOffset, eyeY, eyeSize * 2, heartPaint);
                break;
        }
    }

    private void drawMouth(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthPaint.setColor(Color.parseColor("#880000"));
        mouthPaint.setStyle(Paint.Style.STROKE);
        mouthPaint.setStrokeWidth(6);

        float mouthY = centerY + faceRadius * 0.4f;
        float mouthWidth = faceRadius * 0.4f;

        switch (config.mouthStyle) {
            case SMILE: {
                RectF smile = new RectF(centerX - mouthWidth, mouthY - faceRadius * 0.2f, centerX + mouthWidth, mouthY + faceRadius * 0.2f);
                canvas.drawArc(smile, 20, 140, false, mouthPaint);
                break;
            }
            case LAUGH: {
                mouthPaint.setStyle(Paint.Style.FILL);
                RectF laugh = new RectF(centerX - mouthWidth, mouthY, centerX + mouthWidth, mouthY + faceRadius * 0.4f);
                canvas.drawArc(laugh, 0, 180, true, mouthPaint);
                break;
            }
            case NEUTRAL:
                canvas.drawLine(centerX - mouthWidth * 0.7f, mouthY, centerX + mouthWidth * 0.7f, mouthY, mouthPaint);
                break;
            case SMIRK:
                canvas.drawLine(centerX - mouthWidth * 0.6f, mouthY, centerX + mouthWidth * 0.6f, mouthY + 10, mouthPaint);
                break;
            case SURPRISED: {
                mouthPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(centerX, mouthY + faceRadius * 0.1f, mouthWidth * 0.4f, mouthPaint);
                break;
            }
            case TONGUE_OUT: {
                RectF tongueSmile = new RectF(centerX - mouthWidth, mouthY - faceRadius * 0.2f, centerX + mouthWidth, mouthY + faceRadius * 0.2f);
                canvas.drawArc(tongueSmile, 20, 140, false, mouthPaint);
                Paint tonguePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                tonguePaint.setColor(Color.RED);
                canvas.drawCircle(centerX, mouthY + faceRadius * 0.2f, faceRadius * 0.15f, tonguePaint);
                break;
            }
            case WHISTLING:
                canvas.drawCircle(centerX, mouthY, faceRadius * 0.1f, mouthPaint);
                break;
        }
    }

    private void drawAccessory(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint accPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accPaint.setColor(Color.parseColor("#FFD700"));

        switch (config.accessory) {
            case HAT: {
                accPaint.setColor(Color.parseColor("#663300"));
                canvas.drawRect(centerX - faceRadius * 1.2f, centerY - faceRadius * 1.1f, 
                              centerX + faceRadius * 1.2f, centerY - faceRadius * 0.95f, accPaint);
                canvas.drawRect(centerX - faceRadius * 0.8f, centerY - faceRadius * 1.6f, 
                              centerX + faceRadius * 0.8f, centerY - faceRadius * 1.1f, accPaint);
                break;
            }
            case CROWN: {
                Path crown = new Path();
                crown.moveTo(centerX - faceRadius, centerY - faceRadius * 0.8f);
                crown.lineTo(centerX - faceRadius * 0.6f, centerY - faceRadius * 1.4f);
                crown.lineTo(centerX, centerY - faceRadius * 0.9f);
                crown.lineTo(centerX + faceRadius * 0.6f, centerY - faceRadius * 1.4f);
                crown.lineTo(centerX + faceRadius, centerY - faceRadius * 0.8f);
                crown.close();
                canvas.drawPath(crown, accPaint);
                break;
            }
            case HEADBAND: {
                accPaint.setColor(Color.parseColor("#E91E63"));
                accPaint.setStyle(Paint.Style.STROKE);
                accPaint.setStrokeWidth(15);
                RectF band = new RectF(centerX - faceRadius * 1.05f, centerY - faceRadius * 1.05f, centerX + faceRadius * 1.05f, centerY);
                canvas.drawArc(band, 180, 180, false, accPaint);
                break;
            }
            case EARRINGS: {
                canvas.drawCircle(centerX - faceRadius * 1.05f, centerY + faceRadius * 0.2f, 10, accPaint);
                canvas.drawCircle(centerX + faceRadius * 1.05f, centerY + faceRadius * 0.2f, 10, accPaint);
                break;
            }
            case NECKLACE: {
                accPaint.setStyle(Paint.Style.STROKE);
                accPaint.setStrokeWidth(5);
                RectF nneck = new RectF(centerX - faceRadius * 0.5f, centerY + faceRadius * 0.8f, centerX + faceRadius * 0.5f, centerY + faceRadius * 1.3f);
                canvas.drawArc(nneck, 0, 180, false, accPaint);
                break;
            }
            case BOWTIE: {
                accPaint.setColor(Color.BLACK);
                accPaint.setStyle(Paint.Style.FILL);
                Path bowtie = new Path();
                bowtie.moveTo(centerX - 30, centerY + faceRadius * 0.85f);
                bowtie.lineTo(centerX - 80, centerY + faceRadius * 0.75f);
                bowtie.lineTo(centerX - 80, centerY + faceRadius * 0.95f);
                bowtie.close();
                canvas.drawPath(bowtie, accPaint);
                Path bowtieR = new Path();
                bowtieR.moveTo(centerX + 30, centerY + faceRadius * 0.85f);
                bowtieR.lineTo(centerX + 80, centerY + faceRadius * 0.75f);
                bowtieR.lineTo(centerX + 80, centerY + faceRadius * 0.95f);
                bowtieR.close();
                canvas.drawPath(bowtieR, accPaint);
                canvas.drawCircle(centerX, centerY + faceRadius * 0.85f, 15, accPaint);
                break;
            }
            case SCARF: {
                accPaint.setColor(Color.parseColor("#FF5722"));
                canvas.drawRoundRect(centerX - faceRadius * 0.7f, centerY + faceRadius * 0.7f, centerX + faceRadius * 0.7f, centerY + faceRadius * 0.95f, 15, 15, accPaint);
                break;
            }
            case FLOWER: {
                accPaint.setColor(Color.parseColor("#E91E63"));
                canvas.drawCircle(centerX + faceRadius * 0.8f, centerY - faceRadius * 0.8f, 25, accPaint);
                accPaint.setColor(Color.YELLOW);
                canvas.drawCircle(centerX + faceRadius * 0.8f, centerY - faceRadius * 0.8f, 10, accPaint);
                break;
            }
            case MASK: {
                accPaint.setColor(Color.parseColor("#80008080"));
                RectF mask = new RectF(centerX - faceRadius * 0.85f, centerY - faceRadius * 0.3f, 
                                     centerX + faceRadius * 0.85f, centerY + faceRadius * 0.3f);
                canvas.drawRoundRect(mask, 20, 20, accPaint);
                break;
            }
        }
    }

    private void drawStar(Canvas canvas, float cx, float cy, float radius, Paint paint) {
        Path star = new Path();
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 - (2 * Math.PI * i / 10);
            float r = (i % 2 == 0) ? radius : radius * 0.45f;
            float x = cx + (float) (r * Math.cos(angle));
            float y = cy - (float) (r * Math.sin(angle));
            if (i == 0) star.moveTo(x, y); else star.lineTo(x, y);
        }
        star.close();
        canvas.drawPath(star, paint);
    }

    private void drawHeart(Canvas canvas, float cx, float cy, float size, Paint paint) {
        Path heart = new Path();
        heart.moveTo(cx, cy + size * 0.3f);
        heart.cubicTo(cx - size * 0.7f, cy - size * 0.4f, cx - size * 0.1f, cy - size * 0.9f, cx, cy - size * 0.5f);
        heart.cubicTo(cx + size * 0.1f, cy - size * 0.9f, cx + size * 0.7f, cy - size * 0.4f, cx, cy + size * 0.3f);
        heart.close();
        canvas.drawPath(heart, paint);
    }

    // SharedPreferences and Firebase persistence methods
    private static String getPrefsName(Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? "AvatarPrefs_" + user.getUid() : "AvatarPrefs_guest";
    }

    public void saveConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefsName(context), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("skinTone", config.skinTone.name());
        editor.putString("hairStyle", config.hairStyle.name());
        editor.putString("hairColor", config.hairColor.name());
        editor.putString("eyeStyle", config.eyeStyle.name());
        editor.putString("mouthStyle", config.mouthStyle.name());
        editor.putString("accessory", config.accessory.name());
        editor.putString("clothingStyle", config.clothingStyle.name());
        editor.putString("clothingColor", config.clothingColor.name());
        editor.putString("facialExpression", config.facialExpression.name());
        editor.putString("backgroundColor", config.backgroundColor);
        editor.putLong("lastUpdated", System.currentTimeMillis());
        editor.apply();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("avatar").setValue(config.toMap());
        }
    }

    public static AvatarConfig loadConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefsName(context), Context.MODE_PRIVATE);
        AvatarConfig config = new AvatarConfig();
        if (!prefs.contains("skinTone")) return null; // Indicate no local config

        try {
            config.skinTone = SkinTone.valueOf(prefs.getString("skinTone", "MEDIUM"));
            config.hairStyle = HairStyle.valueOf(prefs.getString("hairStyle", "SHORT"));
            config.hairColor = HairColor.valueOf(prefs.getString("hairColor", "BLACK"));
            config.eyeStyle = EyeStyle.valueOf(prefs.getString("eyeStyle", "NORMAL"));
            config.mouthStyle = MouthStyle.valueOf(prefs.getString("mouthStyle", "SMILE"));
            config.accessory = Accessory.valueOf(prefs.getString("accessory", "NONE"));
            config.clothingStyle = ClothingStyle.valueOf(prefs.getString("clothingStyle", "TSHIRT"));
            config.clothingColor = ClothingColor.valueOf(prefs.getString("clothingColor", "BLUE"));
            config.facialExpression = FacialExpression.valueOf(prefs.getString("facialExpression", "NEUTRAL"));
            config.backgroundColor = prefs.getString("backgroundColor", "#E3F2FD");
        } catch (Exception e) {}
        return config;
    }

    public void saveLocalOnly(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefsName(context), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("skinTone", config.skinTone.name());
        editor.putString("hairStyle", config.hairStyle.name());
        editor.putString("hairColor", config.hairColor.name());
        editor.putString("eyeStyle", config.eyeStyle.name());
        editor.putString("mouthStyle", config.mouthStyle.name());
        editor.putString("accessory", config.accessory.name());
        editor.putString("clothingStyle", config.clothingStyle.name());
        editor.putString("clothingColor", config.clothingColor.name());
        editor.putString("facialExpression", config.facialExpression.name());
        editor.putString("backgroundColor", config.backgroundColor);
        editor.putLong("lastUpdated", System.currentTimeMillis());
        editor.apply();
    }

    public static void syncWithFirebase(Context context, String userId, Runnable callback) {
        if (userId == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId).child("avatar");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    AvatarConfig config = AvatarConfig.fromMap(map);
                    AvatarBuilder builder = new AvatarBuilder(context, config);
                    builder.saveLocalOnly(context);
                }
                if (callback != null) callback.run();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    public static void clearCache(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefsName(context), Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public void setConfig(AvatarConfig config) { this.config = config; }
    public AvatarConfig getConfig() { return config; }
}
