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
        SHORT, LONG, CURLY, BALD, AFRO, BRAIDS, PONYTAIL
    }

    public enum HairColor {
        BLACK("#000000"),
        BROWN("#4A2511"),
        BLONDE("#F9E076"),
        RED("#D84315"),
        GRAY("#9E9E9E");

        public final String color;
        HairColor(String color) { this.color = color; }
    }

    public enum EyeStyle {
        NORMAL, HAPPY, WINK, GLASSES, SUNGLASSES
    }

    public enum MouthStyle {
        SMILE, LAUGH, NEUTRAL, SMIRK
    }

    public enum Accessory {
        NONE, HAT, CROWN, HEADBAND, EARRINGS
    }

    // Avatar Configuration
    public static class AvatarConfig {
        public SkinTone skinTone = SkinTone.MEDIUM;
        public HairStyle hairStyle = HairStyle.SHORT;
        public HairColor hairColor = HairColor.BLACK;
        public EyeStyle eyeStyle = EyeStyle.NORMAL;
        public MouthStyle mouthStyle = MouthStyle.SMILE;
        public Accessory accessory = Accessory.NONE;
        public String backgroundColor = "#E3F2FD";

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("skinTone", skinTone.name());
            map.put("hairStyle", hairStyle.name());
            map.put("hairColor", hairColor.name());
            map.put("eyeStyle", eyeStyle.name());
            map.put("mouthStyle", mouthStyle.name());
            map.put("accessory", accessory.name());
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

    private void drawFace(Canvas canvas, float centerX, float centerY, float radius) {
        Paint facePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        facePaint.setColor(Color.parseColor(config.skinTone.color));
        facePaint.setStyle(Paint.Style.FILL);

        // Draw face circle
        canvas.drawCircle(centerX, centerY, radius, facePaint);

        // Draw face outline
        Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(Color.parseColor("#8D6E63"));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3);
        canvas.drawCircle(centerX, centerY, radius, outlinePaint);
    }

    private void drawHair(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint hairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairPaint.setColor(Color.parseColor(config.hairColor.color));
        hairPaint.setStyle(Paint.Style.FILL);

        switch (config.hairStyle) {
            case SHORT:
                // Simple hair cap on top
                RectF hairRect = new RectF(
                    centerX - faceRadius,
                    centerY - faceRadius,
                    centerX + faceRadius,
                    centerY
                );
                canvas.drawArc(hairRect, 180, 180, true, hairPaint);
                break;

            case LONG:
                // Long hair flowing down
                canvas.drawCircle(centerX, centerY - faceRadius * 0.3f, faceRadius * 1.1f, hairPaint);
                RectF longHair = new RectF(
                    centerX - faceRadius * 1.1f,
                    centerY - faceRadius,
                    centerX + faceRadius * 1.1f,
                    centerY + faceRadius * 1.3f
                );
                canvas.drawRect(longHair, hairPaint);
                break;

            case AFRO:
                // Big round afro
                canvas.drawCircle(centerX, centerY - faceRadius * 0.2f, faceRadius * 1.3f, hairPaint);
                break;

            case CURLY:
                // Multiple circles for curly effect
                canvas.drawCircle(centerX, centerY - faceRadius * 0.5f, faceRadius * 0.9f, hairPaint);
                canvas.drawCircle(centerX - faceRadius * 0.6f, centerY - faceRadius * 0.3f, faceRadius * 0.6f, hairPaint);
                canvas.drawCircle(centerX + faceRadius * 0.6f, centerY - faceRadius * 0.3f, faceRadius * 0.6f, hairPaint);
                break;

            case BRAIDS:
                // Two braids on sides
                canvas.drawCircle(centerX - faceRadius * 0.8f, centerY, faceRadius * 0.4f, hairPaint);
                canvas.drawCircle(centerX + faceRadius * 0.8f, centerY, faceRadius * 0.4f, hairPaint);
                RectF topHair = new RectF(
                    centerX - faceRadius * 0.8f,
                    centerY - faceRadius,
                    centerX + faceRadius * 0.8f,
                    centerY - faceRadius * 0.3f
                );
                canvas.drawRect(topHair, hairPaint);
                break;

            case PONYTAIL:
                // Hair on top with ponytail
                canvas.drawCircle(centerX, centerY - faceRadius * 0.5f, faceRadius * 0.8f, hairPaint);
                RectF ponytail = new RectF(
                    centerX - faceRadius * 0.3f,
                    centerY - faceRadius * 1.3f,
                    centerX + faceRadius * 0.3f,
                    centerY - faceRadius * 0.5f
                );
                canvas.drawRoundRect(ponytail, 20, 20, hairPaint);
                break;

            case BALD:
                // No hair
                break;
        }
    }

    private void drawEyes(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eyePaint.setColor(Color.BLACK);
        eyePaint.setStyle(Paint.Style.FILL);

        float eyeY = centerY - faceRadius * 0.2f;
        float eyeSpacing = faceRadius * 0.4f;
        float eyeRadius = faceRadius * 0.12f;

        switch (config.eyeStyle) {
            case NORMAL:
                canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius, eyePaint);
                canvas.drawCircle(centerX + eyeSpacing, eyeY, eyeRadius, eyePaint);
                break;

            case HAPPY:
                // Happy arched eyes
                Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                arcPaint.setColor(Color.BLACK);
                arcPaint.setStyle(Paint.Style.STROKE);
                arcPaint.setStrokeWidth(5);
                arcPaint.setStrokeCap(Paint.Cap.ROUND);

                RectF leftEye = new RectF(centerX - eyeSpacing - eyeRadius, eyeY - eyeRadius,
                    centerX - eyeSpacing + eyeRadius, eyeY + eyeRadius);
                canvas.drawArc(leftEye, 200, 140, false, arcPaint);

                RectF rightEye = new RectF(centerX + eyeSpacing - eyeRadius, eyeY - eyeRadius,
                    centerX + eyeSpacing + eyeRadius, eyeY + eyeRadius);
                canvas.drawArc(rightEye, 200, 140, false, arcPaint);
                break;

            case WINK:
                canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius, eyePaint);
                // Winking eye (line)
                Paint winkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                winkPaint.setColor(Color.BLACK);
                winkPaint.setStyle(Paint.Style.STROKE);
                winkPaint.setStrokeWidth(5);
                winkPaint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(centerX + eyeSpacing - eyeRadius, eyeY,
                    centerX + eyeSpacing + eyeRadius, eyeY, winkPaint);
                break;

            case GLASSES:
                canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius, eyePaint);
                canvas.drawCircle(centerX + eyeSpacing, eyeY, eyeRadius, eyePaint);

                // Draw glasses
                Paint glassesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                glassesPaint.setColor(Color.parseColor("#424242"));
                glassesPaint.setStyle(Paint.Style.STROKE);
                glassesPaint.setStrokeWidth(6);

                canvas.drawCircle(centerX - eyeSpacing, eyeY, eyeRadius * 2, glassesPaint);
                canvas.drawCircle(centerX + eyeSpacing, eyeY, eyeRadius * 2, glassesPaint);
                canvas.drawLine(centerX - eyeRadius * 0.5f, eyeY,
                    centerX + eyeRadius * 0.5f, eyeY, glassesPaint);
                break;

            case SUNGLASSES:
                Paint sunglassesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                sunglassesPaint.setColor(Color.parseColor("#212121"));
                sunglassesPaint.setStyle(Paint.Style.FILL);

                RectF leftSun = new RectF(centerX - eyeSpacing - eyeRadius * 2, eyeY - eyeRadius * 1.5f,
                    centerX - eyeSpacing + eyeRadius * 2, eyeY + eyeRadius * 1.5f);
                canvas.drawRoundRect(leftSun, 10, 10, sunglassesPaint);

                RectF rightSun = new RectF(centerX + eyeSpacing - eyeRadius * 2, eyeY - eyeRadius * 1.5f,
                    centerX + eyeSpacing + eyeRadius * 2, eyeY + eyeRadius * 1.5f);
                canvas.drawRoundRect(rightSun, 10, 10, sunglassesPaint);
                break;
        }
    }

    private void drawMouth(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint mouthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mouthPaint.setColor(Color.parseColor("#D84315"));
        mouthPaint.setStyle(Paint.Style.STROKE);
        mouthPaint.setStrokeWidth(6);
        mouthPaint.setStrokeCap(Paint.Cap.ROUND);

        float mouthY = centerY + faceRadius * 0.3f;
        float mouthWidth = faceRadius * 0.6f;

        switch (config.mouthStyle) {
            case SMILE:
                RectF smileRect = new RectF(centerX - mouthWidth, mouthY - faceRadius * 0.2f,
                    centerX + mouthWidth, mouthY + faceRadius * 0.3f);
                canvas.drawArc(smileRect, 0, 180, false, mouthPaint);
                break;

            case LAUGH:
                mouthPaint.setStyle(Paint.Style.FILL);
                RectF laughRect = new RectF(centerX - mouthWidth, mouthY - faceRadius * 0.1f,
                    centerX + mouthWidth, mouthY + faceRadius * 0.3f);
                canvas.drawArc(laughRect, 0, 180, false, mouthPaint);
                break;

            case NEUTRAL:
                canvas.drawLine(centerX - mouthWidth * 0.7f, mouthY,
                    centerX + mouthWidth * 0.7f, mouthY, mouthPaint);
                break;

            case SMIRK:
                Path smirkPath = new Path();
                smirkPath.moveTo(centerX - mouthWidth * 0.7f, mouthY);
                smirkPath.lineTo(centerX + mouthWidth * 0.3f, mouthY);
                smirkPath.lineTo(centerX + mouthWidth * 0.7f, mouthY + faceRadius * 0.15f);
                canvas.drawPath(smirkPath, mouthPaint);
                break;
        }
    }

    private void drawAccessory(Canvas canvas, float centerX, float centerY, float faceRadius) {
        Paint accessoryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        accessoryPaint.setColor(Color.parseColor("#FFD700")); // Gold color

        switch (config.accessory) {
            case HAT:
                accessoryPaint.setStyle(Paint.Style.FILL);
                RectF hatBrim = new RectF(centerX - faceRadius * 1.2f, centerY - faceRadius * 1.1f,
                    centerX + faceRadius * 1.2f, centerY - faceRadius * 0.9f);
                canvas.drawRect(hatBrim, accessoryPaint);

                RectF hatTop = new RectF(centerX - faceRadius * 0.8f, centerY - faceRadius * 1.5f,
                    centerX + faceRadius * 0.8f, centerY - faceRadius * 1.1f);
                canvas.drawRoundRect(hatTop, 20, 20, accessoryPaint);
                break;

            case CROWN:
                accessoryPaint.setStyle(Paint.Style.FILL);
                // Draw crown points
                Path crownPath = new Path();
                crownPath.moveTo(centerX - faceRadius, centerY - faceRadius * 0.8f);
                crownPath.lineTo(centerX - faceRadius * 0.6f, centerY - faceRadius * 1.2f);
                crownPath.lineTo(centerX - faceRadius * 0.3f, centerY - faceRadius * 0.9f);
                crownPath.lineTo(centerX, centerY - faceRadius * 1.3f);
                crownPath.lineTo(centerX + faceRadius * 0.3f, centerY - faceRadius * 0.9f);
                crownPath.lineTo(centerX + faceRadius * 0.6f, centerY - faceRadius * 1.2f);
                crownPath.lineTo(centerX + faceRadius, centerY - faceRadius * 0.8f);
                crownPath.lineTo(centerX + faceRadius, centerY - faceRadius * 0.6f);
                crownPath.lineTo(centerX - faceRadius, centerY - faceRadius * 0.6f);
                crownPath.close();
                canvas.drawPath(crownPath, accessoryPaint);
                break;

            case HEADBAND:
                accessoryPaint.setStyle(Paint.Style.STROKE);
                accessoryPaint.setStrokeWidth(10);
                RectF headbandRect = new RectF(centerX - faceRadius * 0.9f, centerY - faceRadius * 0.8f,
                    centerX + faceRadius * 0.9f, centerY - faceRadius * 0.5f);
                canvas.drawArc(headbandRect, 180, 180, false, accessoryPaint);
                break;

            case EARRINGS:
                accessoryPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(centerX - faceRadius * 1.1f, centerY, faceRadius * 0.15f, accessoryPaint);
                canvas.drawCircle(centerX + faceRadius * 1.1f, centerY, faceRadius * 0.15f, accessoryPaint);
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

