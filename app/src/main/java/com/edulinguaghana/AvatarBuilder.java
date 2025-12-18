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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        SHORT, LONG, CURLY, BALD, AFRO, BRAIDS, PONYTAIL, DREADLOCKS, MOHAWK, BUN, SIDEPART
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
        public FacialExpression facialExpression = FacialExpression.HAPPY;
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
    }

    public AvatarBuilder(Context context, AvatarConfig config) {
        this.context = context;
        this.config = config;
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

        FacialExpression[] expressions = FacialExpression.values();
        config.facialExpression = expressions[random.nextInt(expressions.length)];

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

        // Draw body/clothing (behind face)
        drawBody(canvas, centerX, centerY, faceRadius);

        // Draw hair (behind face)
        drawHair(canvas, centerX, centerY, faceRadius);

        // Draw face
        drawFace(canvas, centerX, centerY, faceRadius);

        // Draw eyes
        drawEyes(canvas, centerX, centerY, faceRadius);

        // Draw mouth
        drawMouth(canvas, centerX, centerY, faceRadius);

        // Draw accessory
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

        float bodyY = centerY + faceRadius * 0.7f;
        float bodyWidth = faceRadius * 1.3f;
        float bodyHeight = faceRadius * 1.8f;

        switch (config.clothingStyle) {
            case TSHIRT:
                // Draw neck
                Paint neckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                neckPaint.setColor(Color.parseColor(config.skinTone.color));
                neckPaint.setStyle(Paint.Style.FILL);
                RectF neck = new RectF(centerX - faceRadius * 0.2f, centerY + faceRadius * 0.7f,
                    centerX + faceRadius * 0.2f, centerY + faceRadius * 1.0f);
                canvas.drawRect(neck, neckPaint);

                // T-shirt body
                RectF tshirt = new RectF(centerX - bodyWidth, bodyY,
                    centerX + bodyWidth, bodyY + bodyHeight);
                canvas.drawRoundRect(tshirt, 20, 20, clothingPaint);
                canvas.drawRoundRect(tshirt, 20, 20, outlinePaint);

                // V-neck
                Path vneck = new Path();
                vneck.moveTo(centerX - faceRadius * 0.4f, bodyY);
                vneck.lineTo(centerX, bodyY + faceRadius * 0.3f);
                vneck.lineTo(centerX + faceRadius * 0.4f, bodyY);
                Paint vneckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                vneckPaint.setColor(Color.parseColor(config.skinTone.color));
                vneckPaint.setStyle(Paint.Style.FILL);
                canvas.drawPath(vneck, vneckPaint);
                break;

            case HOODIE:
                // Draw neck
                Paint hoodieNeckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                hoodieNeckPaint.setColor(Color.parseColor(config.skinTone.color));
                hoodieNeckPaint.setStyle(Paint.Style.FILL);
                RectF hoodieNeck = new RectF(centerX - faceRadius * 0.2f, centerY + faceRadius * 0.7f,
                    centerX + faceRadius * 0.2f, centerY + faceRadius * 1.0f);
                canvas.drawRect(hoodieNeck, hoodieNeckPaint);

                // Hoodie body
                RectF hoodie = new RectF(centerX - bodyWidth * 1.1f, bodyY,
                    centerX + bodyWidth * 1.1f, bodyY + bodyHeight);
                canvas.drawRoundRect(hoodie, 25, 25, clothingPaint);
                canvas.drawRoundRect(hoodie, 25, 25, outlinePaint);

                // Hood
                RectF hood = new RectF(centerX - faceRadius * 0.9f, centerY - faceRadius * 1.1f,
                    centerX + faceRadius * 0.9f, bodyY + faceRadius * 0.2f);
                canvas.drawArc(hood, 180, 180, true, clothingPaint);
                canvas.drawArc(hood, 180, 180, true, outlinePaint);

                // Drawstring
                Paint stringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                stringPaint.setColor(Color.WHITE);
                stringPaint.setStyle(Paint.Style.STROKE);
                stringPaint.setStrokeWidth(4);
                canvas.drawLine(centerX - faceRadius * 0.15f, bodyY + faceRadius * 0.1f,
                    centerX - faceRadius * 0.15f, bodyY + faceRadius * 0.35f, stringPaint);
                canvas.drawLine(centerX + faceRadius * 0.15f, bodyY + faceRadius * 0.1f,
                    centerX + faceRadius * 0.15f, bodyY + faceRadius * 0.35f, stringPaint);
                break;

            case DRESS:
                // Draw neck
                Paint dressNeckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                dressNeckPaint.setColor(Color.parseColor(config.skinTone.color));
                dressNeckPaint.setStyle(Paint.Style.FILL);
                RectF dressNeck = new RectF(centerX - faceRadius * 0.2f, centerY + faceRadius * 0.7f,
                    centerX + faceRadius * 0.2f, centerY + faceRadius * 1.0f);
                canvas.drawRect(dressNeck, dressNeckPaint);

                // Dress body (wider at bottom)
                Path dress = new Path();
                dress.moveTo(centerX - bodyWidth * 0.8f, bodyY);
                dress.lineTo(centerX - bodyWidth * 1.3f, bodyY + bodyHeight);
                dress.lineTo(centerX + bodyWidth * 1.3f, bodyY + bodyHeight);
                dress.lineTo(centerX + bodyWidth * 0.8f, bodyY);
                dress.close();
                canvas.drawPath(dress, clothingPaint);
                canvas.drawPath(dress, outlinePaint);

                // Straps
                Paint strapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                strapPaint.setColor(Color.parseColor(config.clothingColor.color));
                strapPaint.setStyle(Paint.Style.STROKE);
                strapPaint.setStrokeWidth(12);
                canvas.drawLine(centerX - faceRadius * 0.35f, bodyY,
                    centerX - faceRadius * 0.35f, bodyY - faceRadius * 0.3f, strapPaint);
                canvas.drawLine(centerX + faceRadius * 0.35f, bodyY,
                    centerX + faceRadius * 0.35f, bodyY - faceRadius * 0.3f, strapPaint);
                break;

            case SUIT:
                // Draw neck
                Paint suitNeckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                suitNeckPaint.setColor(Color.parseColor(config.skinTone.color));
                suitNeckPaint.setStyle(Paint.Style.FILL);
                RectF suitNeck = new RectF(centerX - faceRadius * 0.2f, centerY + faceRadius * 0.7f,
                    centerX + faceRadius * 0.2f, centerY + faceRadius * 1.0f);
                canvas.drawRect(suitNeck, suitNeckPaint);

                // Suit jacket
                RectF suit = new RectF(centerX - bodyWidth * 1.05f, bodyY,
                    centerX + bodyWidth * 1.05f, bodyY + bodyHeight);
                canvas.drawRoundRect(suit, 20, 20, clothingPaint);
                canvas.drawRoundRect(suit, 20, 20, outlinePaint);

                // Shirt collar
                Paint collarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                collarPaint.setColor(Color.WHITE);
                collarPaint.setStyle(Paint.Style.FILL);
                Path leftCollar = new Path();
                leftCollar.moveTo(centerX - faceRadius * 0.2f, bodyY + faceRadius * 0.05f);
                leftCollar.lineTo(centerX - faceRadius * 0.45f, bodyY);
                leftCollar.lineTo(centerX - faceRadius * 0.2f, bodyY);
                leftCollar.close();
                canvas.drawPath(leftCollar, collarPaint);

                Path rightCollar = new Path();
                rightCollar.moveTo(centerX + faceRadius * 0.2f, bodyY + faceRadius * 0.05f);
                rightCollar.lineTo(centerX + faceRadius * 0.45f, bodyY);
                rightCollar.lineTo(centerX + faceRadius * 0.2f, bodyY);
                rightCollar.close();
                canvas.drawPath(rightCollar, collarPaint);

                // Lapels
                Paint lapelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                lapelPaint.setColor(Color.parseColor("#30000000"));
                lapelPaint.setStyle(Paint.Style.FILL);
                Path leftLapel = new Path();
                leftLapel.moveTo(centerX, bodyY + faceRadius * 0.05f);
                leftLapel.lineTo(centerX - bodyWidth * 0.3f, bodyY + bodyHeight * 0.4f);
                leftLapel.lineTo(centerX - bodyWidth * 1.05f, bodyY);
                leftLapel.lineTo(centerX, bodyY);
                leftLapel.close();
                canvas.drawPath(leftLapel, lapelPaint);

                Path rightLapel = new Path();
                rightLapel.moveTo(centerX, bodyY + faceRadius * 0.05f);
                rightLapel.lineTo(centerX + bodyWidth * 0.3f, bodyY + bodyHeight * 0.4f);
                rightLapel.lineTo(centerX + bodyWidth * 1.05f, bodyY);
                rightLapel.lineTo(centerX, bodyY);
                rightLapel.close();
                canvas.drawPath(rightLapel, lapelPaint);
                break;

            case CASUAL:
                // Draw neck
                Paint casualNeckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                casualNeckPaint.setColor(Color.parseColor(config.skinTone.color));
                casualNeckPaint.setStyle(Paint.Style.FILL);
                RectF casualNeck = new RectF(centerX - faceRadius * 0.2f, centerY + faceRadius * 0.7f,
                    centerX + faceRadius * 0.2f, centerY + faceRadius * 1.0f);
                canvas.drawRect(casualNeck, casualNeckPaint);

                // Casual shirt
                RectF casual = new RectF(centerX - bodyWidth, bodyY,
                    centerX + bodyWidth, bodyY + bodyHeight);
                canvas.drawRoundRect(casual, 20, 20, clothingPaint);
                canvas.drawRoundRect(casual, 20, 20, outlinePaint);

                // Buttons
                Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                buttonPaint.setColor(Color.WHITE);
                buttonPaint.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 3; i++) {
                    canvas.drawCircle(centerX, bodyY + faceRadius * (0.3f + i * 0.4f), faceRadius * 0.08f, buttonPaint);
                }

                // Pocket
                Paint pocketPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                pocketPaint.setColor(Color.parseColor("#20000000"));
                pocketPaint.setStyle(Paint.Style.FILL);
                RectF pocket = new RectF(centerX - bodyWidth * 0.6f, bodyY + faceRadius * 0.3f,
                    centerX - bodyWidth * 0.2f, bodyY + faceRadius * 0.7f);
                canvas.drawRoundRect(pocket, 8, 8, pocketPaint);
                break;

            case TRADITIONAL:
                // Traditional clothing with patterns
                Paint traditionalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                traditionalPaint.setColor(Color.parseColor(config.clothingColor.color));
                traditionalPaint.setStyle(Paint.Style.FILL);

                // Draw neck
                Paint traditionalNeckPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                traditionalNeckPaint.setColor(Color.parseColor(config.skinTone.color));
                traditionalNeckPaint.setStyle(Paint.Style.FILL);
                RectF traditionalNeck = new RectF(centerX - faceRadius * 0.2f, centerY + faceRadius * 0.7f,
                    centerX + faceRadius * 0.2f, centerY + faceRadius * 1.0f);
                canvas.drawRect(traditionalNeck, traditionalNeckPaint);

                // Traditional robe/tunic
                RectF traditional = new RectF(centerX - bodyWidth * 1.2f, bodyY,
                    centerX + bodyWidth * 1.2f, bodyY + bodyHeight);
                canvas.drawRoundRect(traditional, 15, 15, traditionalPaint);
                canvas.drawRoundRect(traditional, 15, 15, outlinePaint);

                // Decorative patterns
                Paint patternPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                patternPaint.setColor(Color.parseColor("#FFD700"));
                patternPaint.setStyle(Paint.Style.STROKE);
                patternPaint.setStrokeWidth(6);

                // Pattern lines
                canvas.drawLine(centerX - bodyWidth * 0.9f, bodyY + faceRadius * 0.2f,
                    centerX + bodyWidth * 0.9f, bodyY + faceRadius * 0.2f, patternPaint);
                canvas.drawLine(centerX - bodyWidth * 0.9f, bodyY + faceRadius * 0.5f,
                    centerX + bodyWidth * 0.9f, bodyY + faceRadius * 0.5f, patternPaint);

                // Pattern circles
                patternPaint.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 5; i++) {
                    float x = centerX - bodyWidth * 0.8f + i * bodyWidth * 0.4f;
                    canvas.drawCircle(x, bodyY + faceRadius * 0.35f, faceRadius * 0.08f, patternPaint);
                }
                break;
        }
    }

    private void drawFace(Canvas canvas, float centerX, float centerY, float radius) {
        Paint facePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        facePaint.setColor(Color.parseColor(config.skinTone.color));
        facePaint.setStyle(Paint.Style.FILL);

        // Draw face circle
        canvas.drawCircle(centerX, centerY, radius, facePaint);

        // Add subtle shading to make face more 3D
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.parseColor("#33000000"));
        shadowPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY + radius * 0.6f, radius * 0.4f, shadowPaint);

        // Draw face outline
        Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.parseColor("#6D4C41"));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(4);
        canvas.drawCircle(centerX, centerY, radius, outlinePaint);

        // Add blush/cheeks
        Paint blushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blushPaint.setColor(Color.parseColor("#40FF6B9D"));
        blushPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX - radius * 0.5f, centerY + radius * 0.2f, radius * 0.2f, blushPaint);
        canvas.drawCircle(centerX + radius * 0.5f, centerY + radius * 0.2f, radius * 0.2f, blushPaint);
    }

    private void drawHair(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint hairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairPaint.setColor(Color.parseColor(config.hairColor.color));
        hairPaint.setStyle(Paint.Style.FILL);

        Paint hairOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairOutlinePaint.setColor(Color.parseColor("#33000000"));
        hairOutlinePaint.setStyle(Paint.Style.STROKE);
        hairOutlinePaint.setStrokeWidth(3);

        switch (config.hairStyle) {
            case SHORT:
                // Simple hair cap on top with better shape
                RectF hairRect = new RectF(
                    centerX - faceRadius * 1.05f,
                    centerY - faceRadius * 1.05f,
                    centerX + faceRadius * 1.05f,
                    centerY + faceRadius * 0.1f
                );
                canvas.drawArc(hairRect, 180, 180, true, hairPaint);
                canvas.drawArc(hairRect, 180, 180, true, hairOutlinePaint);
                break;

            case LONG:
                // Long hair flowing down with better volume
                RectF topHair = new RectF(
                    centerX - faceRadius * 1.15f,
                    centerY - faceRadius * 1.1f,
                    centerX + faceRadius * 1.15f,
                    centerY + faceRadius * 0.2f
                );
                canvas.drawArc(topHair, 180, 180, true, hairPaint);
                canvas.drawArc(topHair, 180, 180, true, hairOutlinePaint);

                RectF longHair = new RectF(
                    centerX - faceRadius * 1.15f,
                    centerY - faceRadius * 0.3f,
                    centerX + faceRadius * 1.15f,
                    centerY + faceRadius * 1.5f
                );
                canvas.drawRect(longHair, hairPaint);

                // Add some wave details
                canvas.drawCircle(centerX - faceRadius * 0.9f, centerY + faceRadius * 0.8f, faceRadius * 0.3f, hairPaint);
                canvas.drawCircle(centerX + faceRadius * 0.9f, centerY + faceRadius * 0.8f, faceRadius * 0.3f, hairPaint);
                break;

            case AFRO:
                // Big round afro with texture
                canvas.drawCircle(centerX, centerY - faceRadius * 0.2f, faceRadius * 1.4f, hairPaint);
                canvas.drawCircle(centerX, centerY - faceRadius * 0.2f, faceRadius * 1.4f, hairOutlinePaint);

                // Add texture circles
                Paint texturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                texturePaint.setColor(Color.parseColor("#20000000"));
                texturePaint.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 8; i++) {
                    float angle = (float) (i * Math.PI / 4);
                    float x = centerX + (float) Math.cos(angle) * faceRadius * 0.8f;
                    float y = centerY - faceRadius * 0.2f + (float) Math.sin(angle) * faceRadius * 0.8f;
                    canvas.drawCircle(x, y, faceRadius * 0.25f, texturePaint);
                }
                break;

            case CURLY:
                // Multiple circles for curly effect with better arrangement
                canvas.drawCircle(centerX, centerY - faceRadius * 0.6f, faceRadius * 1.0f, hairPaint);
                canvas.drawCircle(centerX - faceRadius * 0.7f, centerY - faceRadius * 0.4f, faceRadius * 0.7f, hairPaint);
                canvas.drawCircle(centerX + faceRadius * 0.7f, centerY - faceRadius * 0.4f, faceRadius * 0.7f, hairPaint);
                canvas.drawCircle(centerX - faceRadius * 0.9f, centerY, faceRadius * 0.5f, hairPaint);
                canvas.drawCircle(centerX + faceRadius * 0.9f, centerY, faceRadius * 0.5f, hairPaint);

                // Add outlines
                canvas.drawCircle(centerX, centerY - faceRadius * 0.6f, faceRadius * 1.0f, hairOutlinePaint);
                canvas.drawCircle(centerX - faceRadius * 0.7f, centerY - faceRadius * 0.4f, faceRadius * 0.7f, hairOutlinePaint);
                canvas.drawCircle(centerX + faceRadius * 0.7f, centerY - faceRadius * 0.4f, faceRadius * 0.7f, hairOutlinePaint);
                break;

            case BRAIDS:
                // Top hair coverage
                RectF braidTop = new RectF(
                    centerX - faceRadius * 0.95f,
                    centerY - faceRadius * 1.05f,
                    centerX + faceRadius * 0.95f,
                    centerY - faceRadius * 0.2f
                );
                canvas.drawRect(braidTop, hairPaint);
                canvas.drawRect(braidTop, hairOutlinePaint);

                // Two braids on sides with segments
                for (int i = 0; i < 3; i++) {
                    float yOffset = i * faceRadius * 0.4f;
                    canvas.drawCircle(centerX - faceRadius * 0.9f, centerY + yOffset, faceRadius * 0.35f, hairPaint);
                    canvas.drawCircle(centerX + faceRadius * 0.9f, centerY + yOffset, faceRadius * 0.35f, hairPaint);
                    canvas.drawCircle(centerX - faceRadius * 0.9f, centerY + yOffset, faceRadius * 0.35f, hairOutlinePaint);
                    canvas.drawCircle(centerX + faceRadius * 0.9f, centerY + yOffset, faceRadius * 0.35f, hairOutlinePaint);
                }
                break;

            case PONYTAIL:
                // Hair on top
                RectF ponyTop = new RectF(
                    centerX - faceRadius * 1.0f,
                    centerY - faceRadius * 1.1f,
                    centerX + faceRadius * 1.0f,
                    centerY + faceRadius * 0.1f
                );
                canvas.drawArc(ponyTop, 180, 180, true, hairPaint);
                canvas.drawArc(ponyTop, 180, 180, true, hairOutlinePaint);

                // Ponytail with rounded end
                RectF ponytail = new RectF(
                    centerX - faceRadius * 0.35f,
                    centerY - faceRadius * 1.5f,
                    centerX + faceRadius * 0.35f,
                    centerY - faceRadius * 0.4f
                );
                canvas.drawRoundRect(ponytail, 30, 30, hairPaint);
                canvas.drawRoundRect(ponytail, 30, 30, hairOutlinePaint);

                // Ponytail tie
                Paint tiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                tiePaint.setColor(Color.parseColor("#F44336"));
                tiePaint.setStyle(Paint.Style.FILL);
                canvas.drawRect(centerX - faceRadius * 0.4f, centerY - faceRadius * 0.55f,
                    centerX + faceRadius * 0.4f, centerY - faceRadius * 0.35f, tiePaint);
                break;

            case BALD:
                // No hair
                break;

            case DREADLOCKS:
                // Hair base
                RectF dreadBase = new RectF(
                    centerX - faceRadius * 1.0f,
                    centerY - faceRadius * 1.1f,
                    centerX + faceRadius * 1.0f,
                    centerY + faceRadius * 0.1f
                );
                canvas.drawArc(dreadBase, 180, 180, true, hairPaint);
                canvas.drawArc(dreadBase, 180, 180, true, hairOutlinePaint);

                // Multiple dreadlock strands
                for (int i = 0; i < 7; i++) {
                    float xOffset = -faceRadius * 0.9f + i * faceRadius * 0.3f;
                    RectF dread = new RectF(
                        centerX + xOffset - faceRadius * 0.12f,
                        centerY + faceRadius * 0.1f,
                        centerX + xOffset + faceRadius * 0.12f,
                        centerY + faceRadius * 1.2f
                    );
                    canvas.drawRoundRect(dread, 15, 15, hairPaint);
                    canvas.drawRoundRect(dread, 15, 15, hairOutlinePaint);

                    // Add texture lines
                    Paint dreadTexturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    dreadTexturePaint.setColor(Color.parseColor("#20000000"));
                    dreadTexturePaint.setStyle(Paint.Style.STROKE);
                    dreadTexturePaint.setStrokeWidth(2);
                    for (int j = 0; j < 4; j++) {
                        float y = centerY + faceRadius * (0.3f + j * 0.25f);
                        canvas.drawLine(centerX + xOffset - faceRadius * 0.08f, y,
                            centerX + xOffset + faceRadius * 0.08f, y, dreadTexturePaint);
                    }
                }
                break;

            case MOHAWK:
                // Mohawk strip on top
                Paint mohawkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mohawkPaint.setColor(Color.parseColor(config.hairColor.color));
                mohawkPaint.setStyle(Paint.Style.FILL);

                // Multiple spikes
                for (int i = 0; i < 5; i++) {
                    float xPos = centerX - faceRadius * 0.4f + i * faceRadius * 0.2f;
                    Path spike = new Path();
                    spike.moveTo(xPos - faceRadius * 0.15f, centerY - faceRadius * 0.5f);
                    spike.lineTo(xPos, centerY - faceRadius * 1.3f);
                    spike.lineTo(xPos + faceRadius * 0.15f, centerY - faceRadius * 0.5f);
                    spike.close();
                    canvas.drawPath(spike, mohawkPaint);
                    canvas.drawPath(spike, hairOutlinePaint);
                }
                break;

            case BUN:
                // Hair covering head
                RectF bunBase = new RectF(
                    centerX - faceRadius * 1.0f,
                    centerY - faceRadius * 1.1f,
                    centerX + faceRadius * 1.0f,
                    centerY + faceRadius * 0.1f
                );
                canvas.drawArc(bunBase, 180, 180, true, hairPaint);
                canvas.drawArc(bunBase, 180, 180, true, hairOutlinePaint);

                // Bun on top
                canvas.drawCircle(centerX, centerY - faceRadius * 1.1f, faceRadius * 0.5f, hairPaint);
                canvas.drawCircle(centerX, centerY - faceRadius * 1.1f, faceRadius * 0.5f, hairOutlinePaint);

                // Bun detail lines
                Paint bunLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bunLinePaint.setColor(Color.parseColor("#20000000"));
                bunLinePaint.setStyle(Paint.Style.STROKE);
                bunLinePaint.setStrokeWidth(3);
                canvas.drawCircle(centerX, centerY - faceRadius * 1.1f, faceRadius * 0.35f, bunLinePaint);
                canvas.drawCircle(centerX, centerY - faceRadius * 1.1f, faceRadius * 0.2f, bunLinePaint);
                break;

            case SIDEPART:
                // Hair with side part
                RectF sideBase = new RectF(
                    centerX - faceRadius * 1.05f,
                    centerY - faceRadius * 1.1f,
                    centerX + faceRadius * 1.05f,
                    centerY + faceRadius * 0.1f
                );
                canvas.drawArc(sideBase, 180, 180, true, hairPaint);
                canvas.drawArc(sideBase, 180, 180, true, hairOutlinePaint);

                // Side part line
                Paint partPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                partPaint.setColor(Color.parseColor(config.skinTone.color));
                partPaint.setStyle(Paint.Style.STROKE);
                partPaint.setStrokeWidth(6);
                canvas.drawLine(centerX - faceRadius * 0.3f, centerY - faceRadius * 1.0f,
                    centerX - faceRadius * 0.3f, centerY - faceRadius * 0.5f, partPaint);

                // Volume on one side
                canvas.drawCircle(centerX - faceRadius * 0.6f, centerY - faceRadius * 0.6f,
                    faceRadius * 0.4f, hairPaint);
                canvas.drawCircle(centerX - faceRadius * 0.6f, centerY - faceRadius * 0.6f,
                    faceRadius * 0.4f, hairOutlinePaint);
                break;
        }
    }

    private void drawEyes(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint eyeWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeWhitePaint.setColor(Color.WHITE);
        eyeWhitePaint.setStyle(Paint.Style.FILL);

        Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.BLACK);
        eyePaint.setStyle(Paint.Style.FILL);

        Paint eyeHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyeHighlightPaint.setColor(Color.WHITE);
        eyeHighlightPaint.setStyle(Paint.Style.FILL);

        float eyeY = centerY - faceRadius * 0.15f;
        float eyeSpacing = faceRadius * 0.35f;
        float eyeRadius = faceRadius * 0.15f;
        float pupilRadius = faceRadius * 0.08f;

        switch (config.eyeStyle) {
            case NORMAL:
                // Left eye
                canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius, eyeWhitePaint);
                canvas.drawCircle(centerX - eyeSpacing, eyeY, pupilRadius, eyePaint);
                canvas.drawCircle(centerX - eyeSpacing + pupilRadius * 0.3f, eyeY - pupilRadius * 0.3f,
                    pupilRadius * 0.4f, eyeHighlightPaint);

                // Right eye
                canvas.drawCircle(centerX + eyeSpacing, eyeY, eyeRadius, eyeWhitePaint);
                canvas.drawCircle(centerX + eyeSpacing, eyeY, pupilRadius, eyePaint);
                canvas.drawCircle(centerX + eyeSpacing + pupilRadius * 0.3f, eyeY - pupilRadius * 0.3f,
                    pupilRadius * 0.4f, eyeHighlightPaint);

                // Draw eyebrows
                drawEyebrows(canvas, centerX, eyeY, eyeSpacing, faceRadius);
                break;

            case HAPPY:
                // Happy arched eyes
                Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                arcPaint.setColor(Color.BLACK);
                arcPaint.setStyle(Paint.Style.STROKE);
                arcPaint.setStrokeWidth(6);
                arcPaint.setStrokeCap(Paint.Cap.ROUND);

                RectF leftEye = new RectF(centerX - eyeSpacing - eyeRadius * 0.8f, eyeY - eyeRadius * 0.5f,
                    centerX - eyeSpacing + eyeRadius * 0.8f, eyeY + eyeRadius * 0.8f);
                canvas.drawArc(leftEye, 200, 140, false, arcPaint);

                RectF rightEye = new RectF(centerX + eyeSpacing - eyeRadius * 0.8f, eyeY - eyeRadius * 0.5f,
                    centerX + eyeSpacing + eyeRadius * 0.8f, eyeY + eyeRadius * 0.8f);
                canvas.drawArc(rightEye, 200, 140, false, arcPaint);

                // Draw eyebrows
                drawEyebrows(canvas, centerX, eyeY, eyeSpacing, faceRadius);
                break;

            case WINK:
                // Left eye open
                canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius, eyeWhitePaint);
                canvas.drawCircle(centerX - eyeSpacing, eyeY, pupilRadius, eyePaint);
                canvas.drawCircle(centerX - eyeSpacing + pupilRadius * 0.3f, eyeY - pupilRadius * 0.3f,
                    pupilRadius * 0.4f, eyeHighlightPaint);

                // Right eye winking
                Paint winkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                winkPaint.setColor(Color.BLACK);
                winkPaint.setStyle(Paint.Style.STROKE);
                winkPaint.setStrokeWidth(6);
                winkPaint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(centerX + eyeSpacing - eyeRadius, eyeY,
                    centerX + eyeSpacing + eyeRadius, eyeY, winkPaint);

                // Draw eyebrows
                drawEyebrows(canvas, centerX, eyeY, eyeSpacing, faceRadius);
                break;

            case GLASSES:
                // Eyes with pupils
                canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius * 0.6f, eyeWhitePaint);
                canvas.drawCircle(centerX - eyeSpacing, eyeY, pupilRadius * 0.7f, eyePaint);
                canvas.drawCircle(centerX + eyeSpacing, eyeY, eyeRadius * 0.6f, eyeWhitePaint);
                canvas.drawCircle(centerX + eyeSpacing, eyeY, pupilRadius * 0.7f, eyePaint);

                // Draw glasses frames
                Paint glassesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                glassesPaint.setColor(Color.parseColor("#424242"));
                glassesPaint.setStyle(Paint.Style.STROKE);
                glassesPaint.setStrokeWidth(7);

                canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius * 1.3f, glassesPaint);
                canvas.drawCircle(centerX + eyeSpacing, eyeY, eyeRadius * 1.3f, glassesPaint);
                canvas.drawLine(centerX - eyeRadius * 0.2f, eyeY,
                    centerX + eyeRadius * 0.2f, eyeY, glassesPaint);

                // Temples
                canvas.drawLine(centerX - eyeSpacing - eyeRadius * 1.3f, eyeY,
                    centerX - eyeSpacing - eyeRadius * 1.8f, eyeY, glassesPaint);
                canvas.drawLine(centerX + eyeSpacing + eyeRadius * 1.3f, eyeY,
                    centerX + eyeSpacing + eyeRadius * 1.8f, eyeY, glassesPaint);
                break;

            case SUNGLASSES:
                Paint sunglassesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                sunglassesPaint.setColor(Color.parseColor("#212121"));
                sunglassesPaint.setStyle(Paint.Style.FILL);

                RectF leftSun = new RectF(centerX - eyeSpacing - eyeRadius * 1.5f, eyeY - eyeRadius,
                    centerX - eyeSpacing + eyeRadius * 1.5f, eyeY + eyeRadius);
                canvas.drawRoundRect(leftSun, 15, 15, sunglassesPaint);

                RectF rightSun = new RectF(centerX + eyeSpacing - eyeRadius * 1.5f, eyeY - eyeRadius,
                    centerX + eyeSpacing + eyeRadius * 1.5f, eyeY + eyeRadius);
                canvas.drawRoundRect(rightSun, 15, 15, sunglassesPaint);

                // Bridge
                canvas.drawRect(centerX - eyeRadius * 0.3f, eyeY - eyeRadius * 0.3f,
                    centerX + eyeRadius * 0.3f, eyeY + eyeRadius * 0.3f, sunglassesPaint);

                // Add shine effect
                Paint shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                shinePaint.setColor(Color.parseColor("#40FFFFFF"));
                shinePaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(centerX - eyeSpacing - eyeRadius * 0.5f, eyeY - eyeRadius * 0.4f,
                    eyeRadius * 0.4f, shinePaint);
                canvas.drawCircle(centerX + eyeSpacing - eyeRadius * 0.5f, eyeY - eyeRadius * 0.4f,
                    eyeRadius * 0.4f, shinePaint);
                break;

            case STARRY:
                // Starry eyes with sparkles
                canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius, eyeWhitePaint);
                canvas.drawCircle(centerX + eyeSpacing, eyeY, eyeRadius, eyeWhitePaint);

                // Draw stars instead of pupils
                Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                starPaint.setColor(Color.parseColor("#FFD700"));
                starPaint.setStyle(Paint.Style.FILL);

                // Left star
                drawStar(canvas, centerX - eyeSpacing, eyeY, pupilRadius * 1.2f, starPaint);
                // Right star
                drawStar(canvas, centerX + eyeSpacing, eyeY, pupilRadius * 1.2f, starPaint);

                // Add sparkle lines
                Paint sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                sparklePaint.setColor(Color.parseColor("#FFC107"));
                sparklePaint.setStyle(Paint.Style.STROKE);
                sparklePaint.setStrokeWidth(3);
                sparklePaint.setStrokeCap(Paint.Cap.ROUND);

                for (int i = 0; i < 4; i++) {
                    float angle = (float) (i * Math.PI / 2);
                    float x1 = centerX - eyeSpacing + (float) Math.cos(angle) * eyeRadius * 1.4f;
                    float y1 = eyeY + (float) Math.sin(angle) * eyeRadius * 1.4f;
                    float x2 = centerX - eyeSpacing + (float) Math.cos(angle) * eyeRadius * 1.7f;
                    float y2 = eyeY + (float) Math.sin(angle) * eyeRadius * 1.7f;
                    canvas.drawLine(x1, y1, x2, y2, sparklePaint);

                    x1 = centerX + eyeSpacing + (float) Math.cos(angle) * eyeRadius * 1.4f;
                    y1 = eyeY + (float) Math.sin(angle) * eyeRadius * 1.4f;
                    x2 = centerX + eyeSpacing + (float) Math.cos(angle) * eyeRadius * 1.7f;
                    y2 = eyeY + (float) Math.sin(angle) * eyeRadius * 1.7f;
                    canvas.drawLine(x1, y1, x2, y2, sparklePaint);
                }

                drawEyebrows(canvas, centerX, eyeY, eyeSpacing, faceRadius);
                break;

            case SLEEPY:
                // Sleepy half-closed eyes
                Paint sleepyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                sleepyPaint.setColor(Color.BLACK);
                sleepyPaint.setStyle(Paint.Style.STROKE);
                sleepyPaint.setStrokeWidth(6);
                sleepyPaint.setStrokeCap(Paint.Cap.ROUND);

                RectF leftSleepy = new RectF(centerX - eyeSpacing - eyeRadius, eyeY - eyeRadius * 0.3f,
                    centerX - eyeSpacing + eyeRadius, eyeY + eyeRadius * 0.5f);
                canvas.drawArc(leftSleepy, 0, 180, false, sleepyPaint);

                RectF rightSleepy = new RectF(centerX + eyeSpacing - eyeRadius, eyeY - eyeRadius * 0.3f,
                    centerX + eyeSpacing + eyeRadius, eyeY + eyeRadius * 0.5f);
                canvas.drawArc(rightSleepy, 0, 180, false, sleepyPaint);

                // Z Z Z for sleeping
                Paint zPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                zPaint.setColor(Color.parseColor("#9E9E9E"));
                zPaint.setTextSize(faceRadius * 0.3f);
                zPaint.setStyle(Paint.Style.FILL);
                canvas.drawText("Z", centerX + faceRadius * 0.9f, eyeY - faceRadius * 0.3f, zPaint);
                zPaint.setTextSize(faceRadius * 0.25f);
                canvas.drawText("z", centerX + faceRadius * 1.1f, eyeY - faceRadius * 0.5f, zPaint);
                zPaint.setTextSize(faceRadius * 0.2f);
                canvas.drawText("z", centerX + faceRadius * 1.25f, eyeY - faceRadius * 0.7f, zPaint);

                drawEyebrows(canvas, centerX, eyeY, eyeSpacing, faceRadius);
                break;

            case HEART:
                // Heart-shaped eyes
                Paint heartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                heartPaint.setColor(Color.parseColor("#E91E63"));
                heartPaint.setStyle(Paint.Style.FILL);

                // Left heart
                drawHeart(canvas, centerX - eyeSpacing, eyeY, eyeRadius * 1.2f, heartPaint);
                // Right heart
                drawHeart(canvas, centerX + eyeSpacing, eyeY, eyeRadius * 1.2f, heartPaint);

                // Add shine to hearts
                Paint heartShinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                heartShinePaint.setColor(Color.parseColor("#80FFFFFF"));
                heartShinePaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(centerX - eyeSpacing - eyeRadius * 0.3f, eyeY - eyeRadius * 0.3f,
                    eyeRadius * 0.3f, heartShinePaint);
                canvas.drawCircle(centerX + eyeSpacing - eyeRadius * 0.3f, eyeY - eyeRadius * 0.3f,
                    eyeRadius * 0.3f, heartShinePaint);

                drawEyebrows(canvas, centerX, eyeY, eyeSpacing, faceRadius);
                break;
        }
    }

    private void drawStar(Canvas canvas, float cx, float cy, float radius, Paint paint) {
        Path star = new Path();
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 - (2 * Math.PI * i / 10);
            float r = (i % 2 == 0) ? radius : radius * 0.4f;
            float x = cx + (float) (r * Math.cos(angle));
            float y = cy - (float) (r * Math.sin(angle));
            if (i == 0) {
                star.moveTo(x, y);
            } else {
                star.lineTo(x, y);
            }
        }
        star.close();
        canvas.drawPath(star, paint);
    }

    private void drawHeart(Canvas canvas, float cx, float cy, float size, Paint paint) {
        Path heart = new Path();
        heart.moveTo(cx, cy + size * 0.3f);

        // Left curve
        heart.cubicTo(cx - size * 0.6f, cy + size * 0.3f,
            cx - size * 0.6f, cy - size * 0.3f,
            cx, cy - size * 0.5f);

        // Right curve
        heart.cubicTo(cx + size * 0.6f, cy - size * 0.3f,
            cx + size * 0.6f, cy + size * 0.3f,
            cx, cy + size * 0.3f);

        heart.close();
        canvas.drawPath(heart, paint);
    }

    private void drawEyebrows(Canvas canvas, float centerX, float eyeY, float eyeSpacing, float faceRadius) {
        Paint eyebrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyebrowPaint.setColor(Color.parseColor("#33" + config.hairColor.color.substring(1)));
        eyebrowPaint.setStyle(Paint.Style.STROKE);
        eyebrowPaint.setStrokeWidth(5);
        eyebrowPaint.setStrokeCap(Paint.Cap.ROUND);

        float eyebrowY = eyeY - faceRadius * 0.25f;
        float eyebrowWidth = faceRadius * 0.25f;

        // Left eyebrow
        Path leftBrow = new Path();
        leftBrow.moveTo(centerX - eyeSpacing - eyebrowWidth, eyebrowY);
        leftBrow.quadTo(centerX - eyeSpacing, eyebrowY - faceRadius * 0.08f,
            centerX - eyeSpacing + eyebrowWidth, eyebrowY);
        canvas.drawPath(leftBrow, eyebrowPaint);

        // Right eyebrow
        Path rightBrow = new Path();
        rightBrow.moveTo(centerX + eyeSpacing - eyebrowWidth, eyebrowY);
        rightBrow.quadTo(centerX + eyeSpacing, eyebrowY - faceRadius * 0.08f,
            centerX + eyeSpacing + eyebrowWidth, eyebrowY);
        canvas.drawPath(rightBrow, eyebrowPaint);
    }

    private void drawMouth(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthPaint.setColor(Color.parseColor("#C62828"));
        mouthPaint.setStyle(Paint.Style.STROKE);
        mouthPaint.setStrokeWidth(7);
        mouthPaint.setStrokeCap(Paint.Cap.ROUND);

        float mouthY = centerY + faceRadius * 0.35f;
        float mouthWidth = faceRadius * 0.5f;

        switch (config.mouthStyle) {
            case SMILE:
                RectF smileRect = new RectF(centerX - mouthWidth, mouthY - faceRadius * 0.15f,
                    centerX + mouthWidth, mouthY + faceRadius * 0.35f);
                canvas.drawArc(smileRect, 10, 160, false, mouthPaint);

                // Add teeth for more detail
                Paint teethPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                teethPaint.setColor(Color.WHITE);
                teethPaint.setStyle(Paint.Style.FILL);
                RectF teethRect = new RectF(centerX - mouthWidth * 0.6f, mouthY,
                    centerX + mouthWidth * 0.6f, mouthY + faceRadius * 0.12f);
                canvas.drawRoundRect(teethRect, 8, 8, teethPaint);
                break;

            case LAUGH:
                mouthPaint.setStyle(Paint.Style.FILL);
                mouthPaint.setColor(Color.parseColor("#C62828"));
                RectF laughRect = new RectF(centerX - mouthWidth * 0.8f, mouthY - faceRadius * 0.05f,
                    centerX + mouthWidth * 0.8f, mouthY + faceRadius * 0.35f);
                canvas.drawArc(laughRect, 0, 180, false, mouthPaint);

                // Add tongue
                Paint tonguePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                tonguePaint.setColor(Color.parseColor("#FF5252"));
                tonguePaint.setStyle(Paint.Style.FILL);
                RectF tongueRect = new RectF(centerX - mouthWidth * 0.3f, mouthY + faceRadius * 0.05f,
                    centerX + mouthWidth * 0.3f, mouthY + faceRadius * 0.25f);
                canvas.drawRoundRect(tongueRect, 20, 20, tonguePaint);
                break;

            case NEUTRAL:
                mouthPaint.setStrokeWidth(5);
                canvas.drawLine(centerX - mouthWidth * 0.6f, mouthY,
                    centerX + mouthWidth * 0.6f, mouthY, mouthPaint);
                break;

            case SMIRK:
                Path smirkPath = new Path();
                smirkPath.moveTo(centerX - mouthWidth * 0.6f, mouthY);
                smirkPath.quadTo(centerX + mouthWidth * 0.2f, mouthY,
                    centerX + mouthWidth * 0.7f, mouthY + faceRadius * 0.18f);
                canvas.drawPath(smirkPath, mouthPaint);
                break;

            case SURPRISED:
                // O-shaped mouth
                mouthPaint.setStyle(Paint.Style.FILL);
                mouthPaint.setColor(Color.parseColor("#C62828"));
                canvas.drawCircle(centerX, mouthY + faceRadius * 0.1f, mouthWidth * 0.4f, mouthPaint);

                // Inner darkness
                Paint darkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                darkPaint.setColor(Color.parseColor("#8B0000"));
                darkPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(centerX, mouthY + faceRadius * 0.1f, mouthWidth * 0.3f, darkPaint);
                break;

            case TONGUE_OUT:
                // Smiling mouth with tongue sticking out
                mouthPaint.setStyle(Paint.Style.STROKE);
                RectF tongueOutSmile = new RectF(centerX - mouthWidth, mouthY - faceRadius * 0.15f,
                    centerX + mouthWidth, mouthY + faceRadius * 0.35f);
                canvas.drawArc(tongueOutSmile, 10, 160, false, mouthPaint);

                // Tongue sticking out
                Paint tongueOutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                tongueOutPaint.setColor(Color.parseColor("#FF5252"));
                tongueOutPaint.setStyle(Paint.Style.FILL);

                Path tongue = new Path();
                tongue.moveTo(centerX - mouthWidth * 0.25f, mouthY + faceRadius * 0.1f);
                tongue.lineTo(centerX + mouthWidth * 0.25f, mouthY + faceRadius * 0.1f);
                tongue.quadTo(centerX + mouthWidth * 0.25f, mouthY + faceRadius * 0.45f,
                    centerX, mouthY + faceRadius * 0.5f);
                tongue.quadTo(centerX - mouthWidth * 0.25f, mouthY + faceRadius * 0.45f,
                    centerX - mouthWidth * 0.25f, mouthY + faceRadius * 0.1f);
                tongue.close();
                canvas.drawPath(tongue, tongueOutPaint);

                // Tongue center line
                Paint tongueLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                tongueLinePaint.setColor(Color.parseColor("#E53935"));
                tongueLinePaint.setStyle(Paint.Style.STROKE);
                tongueLinePaint.setStrokeWidth(3);
                canvas.drawLine(centerX, mouthY + faceRadius * 0.15f,
                    centerX, mouthY + faceRadius * 0.45f, tongueLinePaint);
                break;

            case WHISTLING:
                // Small O-shaped mouth for whistling
                mouthPaint.setStyle(Paint.Style.STROKE);
                mouthPaint.setStrokeWidth(6);
                canvas.drawCircle(centerX, mouthY + faceRadius * 0.05f, mouthWidth * 0.25f, mouthPaint);

                // Musical notes
                Paint notePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                notePaint.setColor(Color.parseColor("#2196F3"));
                notePaint.setStyle(Paint.Style.FILL);
                notePaint.setTextSize(faceRadius * 0.4f);
                canvas.drawText("♪", centerX + mouthWidth * 0.8f, mouthY - faceRadius * 0.1f, notePaint);
                notePaint.setTextSize(faceRadius * 0.35f);
                canvas.drawText("♫", centerX + mouthWidth * 1.1f, mouthY + faceRadius * 0.15f, notePaint);
                break;
        }
    }

    private void drawAccessory(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint accessoryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accessoryPaint.setColor(Color.parseColor("#FFD700")); // Gold color

        switch (config.accessory) {
            case HAT:
                accessoryPaint.setStyle(Paint.Style.FILL);
                accessoryPaint.setColor(Color.parseColor("#795548")); // Brown

                // Hat brim
                RectF hatBrim = new RectF(centerX - faceRadius * 1.3f, centerY - faceRadius * 1.15f,
                    centerX + faceRadius * 1.3f, centerY - faceRadius * 0.85f);
                canvas.drawRoundRect(hatBrim, 20, 20, accessoryPaint);

                // Hat top
                RectF hatTop = new RectF(centerX - faceRadius * 0.9f, centerY - faceRadius * 1.6f,
                    centerX + faceRadius * 0.9f, centerY - faceRadius * 1.15f);
                canvas.drawRoundRect(hatTop, 25, 25, accessoryPaint);

                // Hat band
                Paint bandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bandPaint.setColor(Color.parseColor("#3E2723"));
                bandPaint.setStyle(Paint.Style.FILL);
                canvas.drawRect(centerX - faceRadius * 0.9f, centerY - faceRadius * 1.2f,
                    centerX + faceRadius * 0.9f, centerY - faceRadius * 1.1f, bandPaint);
                break;

            case CROWN:
                accessoryPaint.setStyle(Paint.Style.FILL);
                accessoryPaint.setColor(Color.parseColor("#FFD700"));

                // Draw crown with shadow
                Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                shadowPaint.setColor(Color.parseColor("#40000000"));
                shadowPaint.setStyle(Paint.Style.FILL);

                Path crownPath = new Path();
                crownPath.moveTo(centerX - faceRadius * 1.1f, centerY - faceRadius * 0.75f);
                crownPath.lineTo(centerX - faceRadius * 0.7f, centerY - faceRadius * 1.3f);
                crownPath.lineTo(centerX - faceRadius * 0.35f, centerY - faceRadius * 0.85f);
                crownPath.lineTo(centerX, centerY - faceRadius * 1.4f);
                crownPath.lineTo(centerX + faceRadius * 0.35f, centerY - faceRadius * 0.85f);
                crownPath.lineTo(centerX + faceRadius * 0.7f, centerY - faceRadius * 1.3f);
                crownPath.lineTo(centerX + faceRadius * 1.1f, centerY - faceRadius * 0.75f);
                crownPath.lineTo(centerX + faceRadius * 1.1f, centerY - faceRadius * 0.55f);
                crownPath.lineTo(centerX - faceRadius * 1.1f, centerY - faceRadius * 0.55f);
                crownPath.close();

                canvas.drawPath(crownPath, shadowPaint);
                canvas.drawPath(crownPath, accessoryPaint);

                // Add jewels
                Paint jewelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                jewelPaint.setColor(Color.parseColor("#E91E63"));
                jewelPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(centerX, centerY - faceRadius * 0.65f, faceRadius * 0.12f, jewelPaint);

                jewelPaint.setColor(Color.parseColor("#2196F3"));
                canvas.drawCircle(centerX - faceRadius * 0.5f, centerY - faceRadius * 0.65f,
                    faceRadius * 0.1f, jewelPaint);
                canvas.drawCircle(centerX + faceRadius * 0.5f, centerY - faceRadius * 0.65f,
                    faceRadius * 0.1f, jewelPaint);
                break;

            case HEADBAND:
                accessoryPaint.setStyle(Paint.Style.STROKE);
                accessoryPaint.setStrokeWidth(12);
                accessoryPaint.setColor(Color.parseColor("#E91E63")); // Pink
                RectF headbandRect = new RectF(centerX - faceRadius * 1.0f, centerY - faceRadius * 0.9f,
                    centerX + faceRadius * 1.0f, centerY - faceRadius * 0.4f);
                canvas.drawArc(headbandRect, 180, 180, false, accessoryPaint);

                // Add decorative bow
                Paint bowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bowPaint.setColor(Color.parseColor("#E91E63"));
                bowPaint.setStyle(Paint.Style.FILL);

                Path bowLeft = new Path();
                bowLeft.moveTo(centerX - faceRadius * 0.15f, centerY - faceRadius * 0.85f);
                bowLeft.lineTo(centerX - faceRadius * 0.4f, centerY - faceRadius * 1.0f);
                bowLeft.lineTo(centerX - faceRadius * 0.25f, centerY - faceRadius * 0.75f);
                bowLeft.close();
                canvas.drawPath(bowLeft, bowPaint);

                Path bowRight = new Path();
                bowRight.moveTo(centerX + faceRadius * 0.15f, centerY - faceRadius * 0.85f);
                bowRight.lineTo(centerX + faceRadius * 0.4f, centerY - faceRadius * 1.0f);
                bowRight.lineTo(centerX + faceRadius * 0.25f, centerY - faceRadius * 0.75f);
                bowRight.close();
                canvas.drawPath(bowRight, bowPaint);

                canvas.drawCircle(centerX, centerY - faceRadius * 0.8f, faceRadius * 0.12f, bowPaint);
                break;

            case EARRINGS:
                accessoryPaint.setStyle(Paint.Style.FILL);
                accessoryPaint.setColor(Color.parseColor("#FFD700"));

                // Left earring
                canvas.drawCircle(centerX - faceRadius * 1.15f, centerY + faceRadius * 0.05f,
                    faceRadius * 0.12f, accessoryPaint);
                RectF leftDangle = new RectF(centerX - faceRadius * 1.2f, centerY + faceRadius * 0.15f,
                    centerX - faceRadius * 1.1f, centerY + faceRadius * 0.35f);
                canvas.drawOval(leftDangle, accessoryPaint);

                // Right earring
                canvas.drawCircle(centerX + faceRadius * 1.15f, centerY + faceRadius * 0.05f,
                    faceRadius * 0.12f, accessoryPaint);
                RectF rightDangle = new RectF(centerX + faceRadius * 1.1f, centerY + faceRadius * 0.15f,
                    centerX + faceRadius * 1.2f, centerY + faceRadius * 0.35f);
                canvas.drawOval(rightDangle, accessoryPaint);

                // Add sparkle
                Paint sparklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                sparklePaint.setColor(Color.WHITE);
                sparklePaint.setStyle(Paint.Style.STROKE);
                sparklePaint.setStrokeWidth(3);
                sparklePaint.setStrokeCap(Paint.Cap.ROUND);

                canvas.drawLine(centerX - faceRadius * 1.2f, centerY + faceRadius * 0.25f,
                    centerX - faceRadius * 1.1f, centerY + faceRadius * 0.25f, sparklePaint);
                canvas.drawLine(centerX + faceRadius * 1.1f, centerY + faceRadius * 0.25f,
                    centerX + faceRadius * 1.2f, centerY + faceRadius * 0.25f, sparklePaint);
                break;

            case NECKLACE:
                // Pearl necklace
                Paint necklacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                necklacePaint.setColor(Color.parseColor("#FFD700"));
                necklacePaint.setStyle(Paint.Style.STROKE);
                necklacePaint.setStrokeWidth(4);

                // Necklace chain
                RectF necklaceArc = new RectF(centerX - faceRadius * 0.6f, centerY + faceRadius * 0.7f,
                    centerX + faceRadius * 0.6f, centerY + faceRadius * 1.3f);
                canvas.drawArc(necklaceArc, 0, 180, false, necklacePaint);

                // Pearls/beads
                Paint pearlPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                pearlPaint.setColor(Color.parseColor("#ECEFF1"));
                pearlPaint.setStyle(Paint.Style.FILL);

                for (int i = 0; i < 7; i++) {
                    float angle = (float) (Math.PI * i / 6);
                    float x = centerX + (float) Math.cos(angle) * faceRadius * 0.55f;
                    float y = centerY + faceRadius * 0.9f + (float) Math.sin(angle) * faceRadius * 0.25f;
                    canvas.drawCircle(x, y, faceRadius * 0.08f, pearlPaint);
                }

                // Pendant
                Paint pendantPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                pendantPaint.setColor(Color.parseColor("#E91E63"));
                pendantPaint.setStyle(Paint.Style.FILL);
                drawHeart(canvas, centerX, centerY + faceRadius * 1.15f, faceRadius * 0.15f, pendantPaint);
                break;

            case BOWTIE:
                // Bow tie
                Paint bowtiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bowtiePaint.setColor(Color.parseColor("#212121"));
                bowtiePaint.setStyle(Paint.Style.FILL);

                // Left bow
                Path leftBowtie = new Path();
                leftBowtie.moveTo(centerX - faceRadius * 0.1f, centerY + faceRadius * 0.85f);
                leftBowtie.lineTo(centerX - faceRadius * 0.45f, centerY + faceRadius * 0.7f);
                leftBowtie.lineTo(centerX - faceRadius * 0.45f, centerY + faceRadius * 1.0f);
                leftBowtie.close();
                canvas.drawPath(leftBowtie, bowtiePaint);

                // Right bow
                Path rightBowtie = new Path();
                rightBowtie.moveTo(centerX + faceRadius * 0.1f, centerY + faceRadius * 0.85f);
                rightBowtie.lineTo(centerX + faceRadius * 0.45f, centerY + faceRadius * 0.7f);
                rightBowtie.lineTo(centerX + faceRadius * 0.45f, centerY + faceRadius * 1.0f);
                rightBowtie.close();
                canvas.drawPath(rightBowtie, bowtiePaint);

                // Center knot
                RectF bowtieCenter = new RectF(centerX - faceRadius * 0.12f, centerY + faceRadius * 0.75f,
                    centerX + faceRadius * 0.12f, centerY + faceRadius * 0.95f);
                canvas.drawRoundRect(bowtieCenter, 8, 8, bowtiePaint);

                // Red dot pattern
                Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                dotPaint.setColor(Color.parseColor("#F44336"));
                dotPaint.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 3; i++) {
                    canvas.drawCircle(centerX - faceRadius * 0.35f + i * faceRadius * 0.15f,
                        centerY + faceRadius * 0.78f, faceRadius * 0.04f, dotPaint);
                    canvas.drawCircle(centerX - faceRadius * 0.35f + i * faceRadius * 0.15f,
                        centerY + faceRadius * 0.92f, faceRadius * 0.04f, dotPaint);
                }
                break;

            case SCARF:
                // Colorful scarf
                Paint scarfPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                scarfPaint.setColor(Color.parseColor("#FF5722"));
                scarfPaint.setStyle(Paint.Style.FILL);

                // Scarf around neck
                RectF scarfRect = new RectF(centerX - faceRadius * 0.7f, centerY + faceRadius * 0.7f,
                    centerX + faceRadius * 0.7f, centerY + faceRadius * 1.0f);
                canvas.drawRoundRect(scarfRect, 15, 15, scarfPaint);

                // Scarf pattern (stripes)
                Paint stripePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                stripePaint.setColor(Color.parseColor("#FFC107"));
                stripePaint.setStyle(Paint.Style.STROKE);
                stripePaint.setStrokeWidth(6);

                for (int i = 0; i < 3; i++) {
                    float y = centerY + faceRadius * (0.75f + i * 0.08f);
                    canvas.drawLine(centerX - faceRadius * 0.65f, y,
                        centerX + faceRadius * 0.65f, y, stripePaint);
                }

                // Scarf tassels
                for (int i = 0; i < 3; i++) {
                    RectF tassel = new RectF(centerX - faceRadius * 0.6f + i * faceRadius * 0.15f,
                        centerY + faceRadius * 1.0f,
                        centerX - faceRadius * 0.55f + i * faceRadius * 0.15f,
                        centerY + faceRadius * 1.3f);
                    canvas.drawRoundRect(tassel, 8, 8, scarfPaint);
                }
                break;

            case FLOWER:
                // Flower accessory (behind ear or on head)
                Paint flowerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                flowerPaint.setColor(Color.parseColor("#E91E63"));
                flowerPaint.setStyle(Paint.Style.FILL);

                float flowerX = centerX - faceRadius * 0.9f;
                float flowerY = centerY - faceRadius * 0.3f;

                // Petals
                for (int i = 0; i < 5; i++) {
                    float angle = (float) (2 * Math.PI * i / 5);
                    float petalX = flowerX + (float) Math.cos(angle) * faceRadius * 0.2f;
                    float petalY = flowerY + (float) Math.sin(angle) * faceRadius * 0.2f;
                    canvas.drawCircle(petalX, petalY, faceRadius * 0.15f, flowerPaint);
                }

                // Flower center
                Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                centerPaint.setColor(Color.parseColor("#FFEB3B"));
                centerPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(flowerX, flowerY, faceRadius * 0.1f, centerPaint);

                // Leaf
                Paint leafPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                leafPaint.setColor(Color.parseColor("#4CAF50"));
                leafPaint.setStyle(Paint.Style.FILL);
                RectF leaf = new RectF(flowerX - faceRadius * 0.05f, flowerY + faceRadius * 0.15f,
                    flowerX + faceRadius * 0.05f, flowerY + faceRadius * 0.35f);
                canvas.drawOval(leaf, leafPaint);
                break;

            case MASK:
                // Decorative/party mask
                Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                maskPaint.setColor(Color.parseColor("#9C27B0"));
                maskPaint.setStyle(Paint.Style.FILL);

                // Mask shape
                RectF maskRect = new RectF(centerX - faceRadius * 0.8f, centerY - faceRadius * 0.35f,
                    centerX + faceRadius * 0.8f, centerY + faceRadius * 0.15f);
                canvas.drawRoundRect(maskRect, 30, 30, maskPaint);

                // Eye holes
                Paint eyeHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                eyeHolePaint.setColor(Color.parseColor(config.backgroundColor));
                eyeHolePaint.setStyle(Paint.Style.FILL);

                RectF leftEyeHole = new RectF(centerX - faceRadius * 0.55f, centerY - faceRadius * 0.25f,
                    centerX - faceRadius * 0.2f, centerY + faceRadius * 0.05f);
                canvas.drawOval(leftEyeHole, eyeHolePaint);

                RectF rightEyeHole = new RectF(centerX + faceRadius * 0.2f, centerY - faceRadius * 0.25f,
                    centerX + faceRadius * 0.55f, centerY + faceRadius * 0.05f);
                canvas.drawOval(rightEyeHole, eyeHolePaint);

                // Decorative patterns on mask
                Paint maskDecorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                maskDecorPaint.setColor(Color.parseColor("#FFD700"));
                maskDecorPaint.setStyle(Paint.Style.FILL);

                // Stars on mask
                drawStar(canvas, centerX - faceRadius * 0.65f, centerY - faceRadius * 0.1f,
                    faceRadius * 0.1f, maskDecorPaint);
                drawStar(canvas, centerX + faceRadius * 0.65f, centerY - faceRadius * 0.1f,
                    faceRadius * 0.1f, maskDecorPaint);
                break;

            case NONE:
                // No accessory
                break;
        }
    }

    /**
     * Save avatar configuration
     */
    public void saveConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AvatarPrefs", Context.MODE_PRIVATE);
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
        editor.apply();

        // Also save to Firebase if user is logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference avatarRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .child("avatar");
            avatarRef.setValue(config.toMap());
        }
    }

    /**
     * Load avatar configuration
     */
    public static AvatarConfig loadConfig(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AvatarPrefs", Context.MODE_PRIVATE);
        AvatarConfig config = new AvatarConfig();

        try {
            String skinTone = prefs.getString("skinTone", null);
            if (skinTone != null) config.skinTone = SkinTone.valueOf(skinTone);

            String hairStyle = prefs.getString("hairStyle", null);
            if (hairStyle != null) config.hairStyle = HairStyle.valueOf(hairStyle);

            String hairColor = prefs.getString("hairColor", null);
            if (hairColor != null) config.hairColor = HairColor.valueOf(hairColor);

            String eyeStyle = prefs.getString("eyeStyle", null);
            if (eyeStyle != null) config.eyeStyle = EyeStyle.valueOf(eyeStyle);

            String mouthStyle = prefs.getString("mouthStyle", null);
            if (mouthStyle != null) config.mouthStyle = MouthStyle.valueOf(mouthStyle);

            String accessory = prefs.getString("accessory", null);
            if (accessory != null) config.accessory = Accessory.valueOf(accessory);

            String clothingStyle = prefs.getString("clothingStyle", null);
            if (clothingStyle != null) config.clothingStyle = ClothingStyle.valueOf(clothingStyle);

            String clothingColor = prefs.getString("clothingColor", null);
            if (clothingColor != null) config.clothingColor = ClothingColor.valueOf(clothingColor);

            String facialExpression = prefs.getString("facialExpression", null);
            if (facialExpression != null) config.facialExpression = FacialExpression.valueOf(facialExpression);

            String bgColor = prefs.getString("backgroundColor", null);
            if (bgColor != null) config.backgroundColor = bgColor;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }

    public AvatarConfig getConfig() {
        return config;
    }

    public void setConfig(AvatarConfig config) {
        this.config = config;
    }
}

