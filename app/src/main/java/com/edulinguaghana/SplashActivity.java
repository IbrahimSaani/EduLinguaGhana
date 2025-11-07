package com.edulinguaghana;  // <-- your package name

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1800; // 1.8 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.ivLogo);
        TextView tvName = findViewById(R.id.tvAppNameSplash);
        TextView tvTagline = findViewById(R.id.tvTaglineSplash);

        // Load animations
        Animation bounceIn = AnimationUtils.loadAnimation(this, R.anim.bounce_in);
        Animation slideUpFade = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);

        // Apply bounce to logo, slide+fade to text
        logo.startAnimation(bounceIn);
        tvName.startAnimation(slideUpFade);
        tvTagline.startAnimation(slideUpFade);

        // Go to main screen after animation
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
