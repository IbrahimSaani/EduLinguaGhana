package com.edulinguaghana;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;

import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AvatarEditorActivity extends AppCompatActivity {

    private AvatarView avatarPreview;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MaterialButton btnSaveAvatar, btnRandomAvatar, btnChangeBackground;

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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Avatar Editor");
        }

        initViews();
        loadCurrentAvatar();
        setupViewPager();
        setupListeners();
    }


    private void initViews() {
        avatarPreview = findViewById(R.id.avatarPreview);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnSaveAvatar = findViewById(R.id.btnSaveAvatar);
        btnRandomAvatar = findViewById(R.id.btnRandomAvatar);
        btnChangeBackground = findViewById(R.id.btnChangeBackground);
    }

    private void loadCurrentAvatar() {
        config = AvatarBuilder.loadConfig(this);
        if (config == null) {
            // Local cache missing (fresh login), sync from Firebase
            FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                AvatarBuilder.syncWithFirebase(this, user.getUid(), () -> {
                    config = AvatarBuilder.loadConfig(this);
                    if (config != null) {
                        runOnUiThread(() -> {
                            builder = new AvatarBuilder(this, config);
                            updateAvatarPreview();
                            updateUIFromConfig();
                        });
                    }
                });
            }
            // Use default while waiting for sync
            config = new AvatarBuilder.AvatarConfig();
        }

        builder = new AvatarBuilder(this, config);
        updateAvatarPreview();

        startFloatingAnimation();
    }

    private void startFloatingAnimation() {
        avatarPreview.animate()
            .translationY(-15f)
            .setDuration(2000)
            .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    avatarPreview.animate()
                        .translationY(15f)
                        .setDuration(2000)
                        .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                        .withEndAction(this)
                        .start();
                }
            }).start();
    }

    private void setupViewPager() {
        AvatarPagerAdapter adapter = new AvatarPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("🎨 Skin");
                    break;
                case 1:
                    tab.setText("💇 Hair");
                    break;
                case 2:
                    tab.setText("👀 Face");
                    break;
                case 3:
                    tab.setText("👑 Accs");
                    break;
                case 4:
                    tab.setText("👔 Style");
                    break;
                case 5:
                    tab.setText("😊 Mood");
                    break;
            }
        }).attach();
    }

    private void updateUIFromConfig() {
        // Notify all fragments to update their UI
        // We can use the ViewPager2 to find fragments or just rely on them being updated when shown
        // To be safe, we can try to find them if they're currently active
        for (int i = 0; i < 6; i++) {
            androidx.fragment.app.Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + i);
            if (fragment instanceof AvatarSkinToneFragment) {
                ((AvatarSkinToneFragment) fragment).updateUIFromConfig();
            } else if (fragment instanceof AvatarHairFragment) {
                ((AvatarHairFragment) fragment).updateUIFromConfig();
            } else if (fragment instanceof AvatarEyesMouthFragment) {
                ((AvatarEyesMouthFragment) fragment).updateUIFromConfig();
            } else if (fragment instanceof AvatarAccessoryFragment) {
                ((AvatarAccessoryFragment) fragment).updateUIFromConfig();
            } else if (fragment instanceof AvatarClothingFragment) {
                ((AvatarClothingFragment) fragment).updateUIFromConfig();
            } else if (fragment instanceof AvatarExpressionFragment) {
                ((AvatarExpressionFragment) fragment).updateUIFromConfig();
            }
        }
    }

    private void setupListeners() {
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

    private void showBackgroundColorPicker() {
        List<StyledMenuHelper.MenuItem> menuItems = new ArrayList<>();
        for (int i = 0; i < backgroundNames.length; i++) {
            final int index = i;
            menuItems.add(new StyledMenuHelper.MenuItem(
                "🎨",
                backgroundNames[i],
                () -> {
                    config.backgroundColor = backgroundColors[index];
                    updateAvatarPreview();
                    Toast.makeText(this, "Background changed to " + backgroundNames[index] + "! ✨",
                        Toast.LENGTH_SHORT).show();
                }
            ));
        }

        StyledMenuHelper.showStyledMenu(
            this,
            "🎨",
            "Background Color",
            "Choose a color for your avatar",
            menuItems,
            null
        );
    }

    private void updateAvatarPreview() {
        builder.setConfig(config);
        avatarPreview.setAvatarConfig(config);
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

        // Save avatar to database
        saveAvatarToDatabase();

        Toast.makeText(this, "Avatar saved successfully! ✨", Toast.LENGTH_SHORT).show();

        // Delay finish to show animation
        avatarPreview.postDelayed(this::finish, 500);
    }

    private void saveAvatarToDatabase() {
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        com.google.firebase.database.FirebaseDatabase db = com.google.firebase.database.FirebaseDatabase.getInstance();
        
        java.util.Map<String, Object> avatarMap = config.toMap();
        long now = System.currentTimeMillis();

        // 1. Update user profile
        com.google.firebase.database.DatabaseReference usersRef = db.getReference("users").child(uid);
        java.util.Map<String, Object> userUpdates = new java.util.HashMap<>();
        userUpdates.put("avatar", avatarMap);
        userUpdates.put("avatarUpdatedAt", now);
        usersRef.updateChildren(userUpdates);

        // 2. Update leaderboard entry if it exists
        com.google.firebase.database.DatabaseReference leaderboardRef = db.getReference("leaderboard").child(uid);
        leaderboardRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    leaderboardRef.child("avatar").setValue(avatarMap);
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    // Getter for the AvatarConfig
    public AvatarBuilder.AvatarConfig getAvatarConfig() {
        return config;
    }

    // Method to update the avatar preview from fragments
    public void updateAvatar() {
        updateAvatarPreview();
    }
}
