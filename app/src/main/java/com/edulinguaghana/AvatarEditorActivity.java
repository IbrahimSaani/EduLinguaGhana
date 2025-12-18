package com.edulinguaghana;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Avatar Editor Activity - Allows users to create and customize their avatar
 */
public class AvatarEditorActivity extends AppCompatActivity {

    private AvatarView avatarPreview;
    private RadioGroup rgSkinTone;
    private Spinner spinnerHairStyle, spinnerHairColor, spinnerEyeStyle, spinnerMouthStyle, spinnerAccessory;
    private Spinner spinnerClothingStyle, spinnerClothingColor, spinnerExpression;
    private MaterialButton btnSaveAvatar, btnRandomAvatar, btnChangeBackground;
    private MaterialCardView cardSkinLight, cardSkinMedium, cardSkinTan, cardSkinBrown, cardSkinDark;

    private AvatarBuilder.AvatarConfig config;
    private AvatarBuilder builder;

    private final String[] backgroundColors = {
        "#E3F2FD", "#FCE4EC", "#F3E5F5", "#E8F5E9", "#FFF3E0",
        "#E0F2F1", "#F1F8E9", "#FFF9C4", "#FFEBEE", "#E1F5FE"
    };

    private final String[] backgroundNames = {
        "Sky Blue", "Pink Blush", "Lavender", "Mint Green", "Peach",
        "Aqua", "Lime", "Yellow", "Rose", "Azure"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Avatar Editor");
        }

        initViews();
        loadCurrentAvatar();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        avatarPreview = findViewById(R.id.avatarPreview);
        rgSkinTone = findViewById(R.id.rgSkinTone);
        spinnerHairStyle = findViewById(R.id.spinnerHairStyle);
        spinnerHairColor = findViewById(R.id.spinnerHairColor);
        spinnerEyeStyle = findViewById(R.id.spinnerEyeStyle);
        spinnerMouthStyle = findViewById(R.id.spinnerMouthStyle);
        spinnerAccessory = findViewById(R.id.spinnerAccessory);
        spinnerClothingStyle = findViewById(R.id.spinnerClothingStyle);
        spinnerClothingColor = findViewById(R.id.spinnerClothingColor);
        spinnerExpression = findViewById(R.id.spinnerExpression);
        btnSaveAvatar = findViewById(R.id.btnSaveAvatar);
        btnRandomAvatar = findViewById(R.id.btnRandomAvatar);
        btnChangeBackground = findViewById(R.id.btnChangeBackground);

        // Get skin tone cards
        cardSkinLight = findViewById(R.id.cardSkinLight);
        cardSkinMedium = findViewById(R.id.cardSkinMedium);
        cardSkinTan = findViewById(R.id.cardSkinTan);
        cardSkinBrown = findViewById(R.id.cardSkinBrown);
        cardSkinDark = findViewById(R.id.cardSkinDark);
    }

    private void loadCurrentAvatar() {
        config = AvatarBuilder.loadConfig(this);
        builder = new AvatarBuilder(this, config);
        updateAvatarPreview();
        updateUIFromConfig();

        // Add pulse animation on load
        avatarPreview.postDelayed(() -> {
            avatarPreview.setScaleX(0.95f);
            avatarPreview.setScaleY(0.95f);
            avatarPreview.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(300)
                .withEndAction(() -> {
                    avatarPreview.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300)
                        .start();
                }).start();
        }, 200);
    }

    private void setupSpinners() {
        // Hair Style
        String[] hairStyles = {"Short", "Long", "Curly", "Bald", "Afro", "Braids", "Ponytail",
            "Dreadlocks", "Mohawk", "Bun", "Side Part"};
        ArrayAdapter<String> hairStyleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hairStyles);
        hairStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHairStyle.setAdapter(hairStyleAdapter);

        // Hair Color
        String[] hairColors = {"Black", "Brown", "Blonde", "Red", "Gray", "Purple", "Blue", "Pink"};
        ArrayAdapter<String> hairColorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hairColors);
        hairColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHairColor.setAdapter(hairColorAdapter);

        // Eye Style
        String[] eyeStyles = {"Normal", "Happy", "Wink", "Glasses", "Sunglasses", "Starry", "Sleepy", "Heart"};
        ArrayAdapter<String> eyeStyleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, eyeStyles);
        eyeStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEyeStyle.setAdapter(eyeStyleAdapter);

        // Mouth Style
        String[] mouthStyles = {"Smile", "Laugh", "Neutral", "Smirk", "Surprised", "Tongue Out", "Whistling"};
        ArrayAdapter<String> mouthStyleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mouthStyles);
        mouthStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMouthStyle.setAdapter(mouthStyleAdapter);

        // Accessory
        String[] accessories = {"None", "Hat", "Crown", "Headband", "Earrings", "Necklace", "Bow Tie", "Scarf", "Flower", "Mask"};
        ArrayAdapter<String> accessoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, accessories);
        accessoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccessory.setAdapter(accessoryAdapter);

        // Clothing Style
        String[] clothingStyles = {"T-Shirt", "Hoodie", "Dress", "Suit", "Casual", "Traditional"};
        ArrayAdapter<String> clothingStyleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, clothingStyles);
        clothingStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClothingStyle.setAdapter(clothingStyleAdapter);

        // Clothing Color
        String[] clothingColors = {"Red", "Blue", "Green", "Yellow", "Purple", "Orange", "Pink", "Black", "White"};
        ArrayAdapter<String> clothingColorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, clothingColors);
        clothingColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClothingColor.setAdapter(clothingColorAdapter);

        // Facial Expression
        String[] expressions = {"Neutral", "Happy", "Excited", "Cool", "Surprised", "Shy"};
        ArrayAdapter<String> expressionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, expressions);
        expressionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExpression.setAdapter(expressionAdapter);
    }

    private void setupListeners() {
        // Skin Tone
        rgSkinTone.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSkinLight) {
                config.skinTone = AvatarBuilder.SkinTone.LIGHT;
                updateSkinToneCardSelection(cardSkinLight);
            } else if (checkedId == R.id.rbSkinMedium) {
                config.skinTone = AvatarBuilder.SkinTone.MEDIUM;
                updateSkinToneCardSelection(cardSkinMedium);
            } else if (checkedId == R.id.rbSkinTan) {
                config.skinTone = AvatarBuilder.SkinTone.TAN;
                updateSkinToneCardSelection(cardSkinTan);
            } else if (checkedId == R.id.rbSkinBrown) {
                config.skinTone = AvatarBuilder.SkinTone.BROWN;
                updateSkinToneCardSelection(cardSkinBrown);
            } else if (checkedId == R.id.rbSkinDark) {
                config.skinTone = AvatarBuilder.SkinTone.DARK;
                updateSkinToneCardSelection(cardSkinDark);
            }
            updateAvatarPreview();
        });

        // Hair Style
        spinnerHairStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.hairStyle = AvatarBuilder.HairStyle.values()[position];
                updateAvatarPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Hair Color
        spinnerHairColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.hairColor = AvatarBuilder.HairColor.values()[position];
                updateAvatarPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Eye Style
        spinnerEyeStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.eyeStyle = AvatarBuilder.EyeStyle.values()[position];
                updateAvatarPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Mouth Style
        spinnerMouthStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.mouthStyle = AvatarBuilder.MouthStyle.values()[position];
                updateAvatarPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Accessory
        spinnerAccessory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.accessory = AvatarBuilder.Accessory.values()[position];
                updateAvatarPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Clothing Style
        spinnerClothingStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.clothingStyle = AvatarBuilder.ClothingStyle.values()[position];
                updateAvatarPreviewWithAnimation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Clothing Color
        spinnerClothingColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.clothingColor = AvatarBuilder.ClothingColor.values()[position];
                updateAvatarPreviewWithAnimation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Facial Expression
        spinnerExpression.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.facialExpression = AvatarBuilder.FacialExpression.values()[position];
                updateAvatarPreviewWithAnimation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Random Avatar Button
        btnRandomAvatar.setOnClickListener(v -> {
            // Rotate and bounce animation
            avatarPreview.animate()
                .rotationBy(360f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(300)
                .withEndAction(() -> {
                    config = AvatarBuilder.generateRandom();
                    updateUIFromConfig();
                    builder.setConfig(config);
                    avatarPreview.setAvatarConfig(config);

                    // Bounce back
                    avatarPreview.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            avatarPreview.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .start();
                        }).start();
                }).start();

            Toast.makeText(this, "Random avatar generated! 🎲", Toast.LENGTH_SHORT).show();
        });

        // Change Background Button
        btnChangeBackground.setOnClickListener(v -> showBackgroundColorPicker());

        // Save Avatar Button
        btnSaveAvatar.setOnClickListener(v -> saveAvatar());
    }

    private void updateSkinToneCardSelection(MaterialCardView selectedCard) {
        // Reset all cards
        cardSkinLight.setStrokeWidth(0);
        cardSkinMedium.setStrokeWidth(0);
        cardSkinTan.setStrokeWidth(0);
        cardSkinBrown.setStrokeWidth(0);
        cardSkinDark.setStrokeWidth(0);

        // Highlight selected card
        selectedCard.setStrokeWidth(8);
        selectedCard.setStrokeColor(Color.parseColor("#4CAF50"));
    }

    private void showBackgroundColorPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎨 Choose Background Color");

        builder.setItems(backgroundNames, (dialog, which) -> {
            config.backgroundColor = backgroundColors[which];
            updateAvatarPreview();
            Toast.makeText(this, "Background changed to " + backgroundNames[which] + "! ✨",
                Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateUIFromConfig() {
        // Update skin tone radio buttons
        switch (config.skinTone) {
            case LIGHT:
                ((android.widget.RadioButton) findViewById(R.id.rbSkinLight)).setChecked(true);
                updateSkinToneCardSelection(cardSkinLight);
                break;
            case MEDIUM:
                ((android.widget.RadioButton) findViewById(R.id.rbSkinMedium)).setChecked(true);
                updateSkinToneCardSelection(cardSkinMedium);
                break;
            case TAN:
                ((android.widget.RadioButton) findViewById(R.id.rbSkinTan)).setChecked(true);
                updateSkinToneCardSelection(cardSkinTan);
                break;
            case BROWN:
                ((android.widget.RadioButton) findViewById(R.id.rbSkinBrown)).setChecked(true);
                updateSkinToneCardSelection(cardSkinBrown);
                break;
            case DARK:
                ((android.widget.RadioButton) findViewById(R.id.rbSkinDark)).setChecked(true);
                updateSkinToneCardSelection(cardSkinDark);
                break;
        }

        // Update spinners
        spinnerHairStyle.setSelection(config.hairStyle.ordinal());
        spinnerHairColor.setSelection(config.hairColor.ordinal());
        spinnerEyeStyle.setSelection(config.eyeStyle.ordinal());
        spinnerMouthStyle.setSelection(config.mouthStyle.ordinal());
        spinnerAccessory.setSelection(config.accessory.ordinal());
        spinnerClothingStyle.setSelection(config.clothingStyle.ordinal());
        spinnerClothingColor.setSelection(config.clothingColor.ordinal());
        spinnerExpression.setSelection(config.facialExpression.ordinal());
    }

    private void updateAvatarPreview() {
        builder.setConfig(config);
        avatarPreview.setAvatarConfig(config);
    }

    private void updateAvatarPreviewWithAnimation() {
        // Fade out
        avatarPreview.animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction(() -> {
                // Update avatar
                builder.setConfig(config);
                avatarPreview.setAvatarConfig(config);

                // Fade in with scale animation
                avatarPreview.setScaleX(0.9f);
                avatarPreview.setScaleY(0.9f);
                avatarPreview.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
            }).start();
    }

    private void saveAvatar() {
        // Success animation
        avatarPreview.animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .alpha(0.7f)
            .setDuration(150)
            .withEndAction(() -> {
                avatarPreview.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(150)
                    .start();
            }).start();

        builder.saveConfig(this);
        Toast.makeText(this, "Avatar saved successfully! ✨", Toast.LENGTH_SHORT).show();

        // Delay finish to show animation
        avatarPreview.postDelayed(this::finish, 500);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

