package com.edulinguaghana;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

/**
 * Animated Avatar Demo Activity
 * Shows live animated avatars with controls
 */
public class AnimatedAvatarDemoActivity extends AppCompatActivity {

    private AnimatedAvatarView animatedAvatar1, animatedAvatar2, animatedAvatar3;
    private SwitchCompat switchAnimations;
    private MaterialButton btnRandomize1, btnRandomize2, btnRandomize3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animated_avatar_demo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Animated Avatars");
        }

        initViews();
        setupListeners();
        loadAvatars();
    }

    private void initViews() {
        animatedAvatar1 = findViewById(R.id.animatedAvatar1);
        animatedAvatar2 = findViewById(R.id.animatedAvatar2);
        animatedAvatar3 = findViewById(R.id.animatedAvatar3);
        switchAnimations = findViewById(R.id.switchAnimations);
        btnRandomize1 = findViewById(R.id.btnRandomize1);
        btnRandomize2 = findViewById(R.id.btnRandomize2);
        btnRandomize3 = findViewById(R.id.btnRandomize3);
    }

    private void setupListeners() {
        switchAnimations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                animatedAvatar1.setAnimationsEnabled(isChecked);
                animatedAvatar2.setAnimationsEnabled(isChecked);
                animatedAvatar3.setAnimationsEnabled(isChecked);
            }
        });

        btnRandomize1.setOnClickListener(v -> {
            AvatarBuilder.AvatarConfig config = AvatarBuilder.generateRandom();
            animatedAvatar1.setAvatarConfig(config);
        });

        btnRandomize2.setOnClickListener(v -> {
            AvatarBuilder.AvatarConfig config = AvatarBuilder.generateRandom();
            animatedAvatar2.setAvatarConfig(config);
        });

        btnRandomize3.setOnClickListener(v -> {
            AvatarBuilder.AvatarConfig config = AvatarBuilder.generateRandom();
            animatedAvatar3.setAvatarConfig(config);
        });
    }

    private void loadAvatars() {
        // Load current avatar
        AvatarBuilder.AvatarConfig config1 = AvatarBuilder.loadConfig(this);
        animatedAvatar1.setAvatarConfig(config1);

        // Generate two random avatars for demo
        AvatarBuilder.AvatarConfig config2 = AvatarBuilder.generateRandom();
        animatedAvatar2.setAvatarConfig(config2);

        AvatarBuilder.AvatarConfig config3 = AvatarBuilder.generateRandom();
        animatedAvatar3.setAvatarConfig(config3);

        // Enable animations by default
        switchAnimations.setChecked(true);
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

