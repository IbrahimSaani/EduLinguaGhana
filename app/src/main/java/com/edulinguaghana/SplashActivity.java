package com.edulinguaghana;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private MediaPlayer startPlayer;
    private boolean hasNavigated = false;

    private ImageView ivLogo;
    private TextView tvAppNameSplash;
    private TextView tvTaglineSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Bind views
        ivLogo = findViewById(R.id.ivLogo);
        tvAppNameSplash = findViewById(R.id.tvAppNameSplash);
        tvTaglineSplash = findViewById(R.id.tvTaglineSplash);

        setupInitialAnimationState();
        startIntroAnimations();
        playStartSoundAndThenOpenMain();
    }

    private void setupInitialAnimationState() {
        // Logo starts small & invisible
        ivLogo.setAlpha(0f);
        ivLogo.setScaleX(0.3f);
        ivLogo.setScaleY(0.3f);

        // Texts start slightly lower & invisible
        float dp16 = getResources().getDisplayMetrics().density * 16;

        tvAppNameSplash.setAlpha(0f);
        tvAppNameSplash.setTranslationY(dp16);

        tvTaglineSplash.setAlpha(0f);
        tvTaglineSplash.setTranslationY(dp16);
    }

    private void startIntroAnimations() {
        // Bounce-in logo
        ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(900)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // Slide + fade app name
        tvAppNameSplash.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(300)
                .setDuration(700)
                .start();

        // Slide + fade tagline
        tvTaglineSplash.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(550)
                .setDuration(700)
                .start();
    }

    private void playStartSoundAndThenOpenMain() {
        try {
            startPlayer = MediaPlayer.create(this, R.raw.app_start);

            if (startPlayer == null) {
                openMainScreen();
                return;
            }

            startPlayer.setOnCompletionListener(mp -> {
                mp.release();
                startPlayer = null;
                openMainScreen();
            });

            startPlayer.start();

        } catch (Exception e) {
            openMainScreen();
        }
    }

    private void openMainScreen() {
        if (hasNavigated) return;   // avoid double navigation
        hasNavigated = true;

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (startPlayer != null) {
            if (startPlayer.isPlaying()) {
                startPlayer.stop();
            }
            startPlayer.release();
            startPlayer = null;
        }
    }
}
