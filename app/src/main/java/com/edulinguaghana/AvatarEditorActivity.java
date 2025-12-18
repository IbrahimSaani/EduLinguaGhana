package com.edulinguaghana;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

/**
 * Avatar Editor Activity - Allows users to create and customize their avatar
 */
public class AvatarEditorActivity extends AppCompatActivity {

    private AvatarView avatarPreview;
    private RadioGroup rgSkinTone;
    private Spinner spinnerHairStyle, spinnerHairColor, spinnerEyeStyle, spinnerMouthStyle, spinnerAccessory;
    private MaterialButton btnSaveAvatar, btnRandomAvatar;

    private AvatarBuilder.AvatarConfig config;
    private AvatarBuilder builder;

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
        btnSaveAvatar = findViewById(R.id.btnSaveAvatar);
        btnRandomAvatar = findViewById(R.id.btnRandomAvatar);
    }

    private void loadCurrentAvatar() {
        config = AvatarBuilder.loadConfig(this);
        builder = new AvatarBuilder(this, config);
        updateAvatarPreview();
        updateUIFromConfig();
    }

    private void setupSpinners() {
        // Hair Style
        String[] hairStyles = {"Short", "Long", "Curly", "Bald", "Afro", "Braids", "Ponytail"};
        ArrayAdapter<String> hairStyleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hairStyles);
        hairStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHairStyle.setAdapter(hairStyleAdapter);

        // Hair Color
        String[] hairColors = {"Black", "Brown", "Blonde", "Red", "Gray"};
        ArrayAdapter<String> hairColorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hairColors);
        hairColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHairColor.setAdapter(hairColorAdapter);

        // Eye Style
        String[] eyeStyles = {"Normal", "Happy", "Wink", "Glasses", "Sunglasses"};
        ArrayAdapter<String> eyeStyleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, eyeStyles);
        eyeStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEyeStyle.setAdapter(eyeStyleAdapter);

        // Mouth Style
        String[] mouthStyles = {"Smile", "Laugh", "Neutral", "Smirk"};
        ArrayAdapter<String> mouthStyleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mouthStyles);
        mouthStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMouthStyle.setAdapter(mouthStyleAdapter);

        // Accessory
        String[] accessories = {"None", "Hat", "Crown", "Headband", "Earrings"};
        ArrayAdapter<String> accessoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, accessories);
        accessoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccessory.setAdapter(accessoryAdapter);
    }

    private void setupListeners() {
        // Skin Tone
        rgSkinTone.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSkinLight) {
                config.skinTone = AvatarBuilder.SkinTone.LIGHT;
            } else if (checkedId == R.id.rbSkinMedium) {
                config.skinTone = AvatarBuilder.SkinTone.MEDIUM;
            } else if (checkedId == R.id.rbSkinTan) {
                config.skinTone = AvatarBuilder.SkinTone.TAN;
            } else if (checkedId == R.id.rbSkinBrown) {
                config.skinTone = AvatarBuilder.SkinTone.BROWN;
            } else if (checkedId == R.id.rbSkinDark) {
                config.skinTone = AvatarBuilder.SkinTone.DARK;
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

        // Random Avatar Button
        btnRandomAvatar.setOnClickListener(v -> {
            config = AvatarBuilder.generateRandom();
            updateUIFromConfig();
            updateAvatarPreview();
            Toast.makeText(this, "Random avatar generated! 🎲", Toast.LENGTH_SHORT).show();
        });

        // Save Avatar Button
        btnSaveAvatar.setOnClickListener(v -> saveAvatar());
    }

    private void updateUIFromConfig() {
        // Update skin tone radio buttons
        switch (config.skinTone) {
            case LIGHT:
                ((android.widget.RadioButton) findViewById(R.id.rbSkinLight)).setChecked(true);
                break;
            case MEDIUM:
                ((android.widget.RadioButton) findViewById(R.id.rbSkinMedium)).setChecked(true);
                break;
            case TAN:
                ((android.widget.RadioButton) findViewById(R.id.rbSkinTan)).setChecked(true);
                break;
            case BROWN:
                ((android.widget.RadioButton) findViewById(R.id.rbSkinBrown)).setChecked(true);
                break;
            case DARK:
                ((android.widget.RadioButton) findViewById(R.id.rbSkinDark)).setChecked(true);
                break;
        }

        // Update spinners
        spinnerHairStyle.setSelection(config.hairStyle.ordinal());
        spinnerHairColor.setSelection(config.hairColor.ordinal());
        spinnerEyeStyle.setSelection(config.eyeStyle.ordinal());
        spinnerMouthStyle.setSelection(config.mouthStyle.ordinal());
        spinnerAccessory.setSelection(config.accessory.ordinal());
    }

    private void updateAvatarPreview() {
        builder.setConfig(config);
        avatarPreview.setAvatarConfig(config);
    }

    private void saveAvatar() {
        builder.saveConfig(this);
        Toast.makeText(this, "Avatar saved successfully! ✨", Toast.LENGTH_SHORT).show();
        finish();
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

