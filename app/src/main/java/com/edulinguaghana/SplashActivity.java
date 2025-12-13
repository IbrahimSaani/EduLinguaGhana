package com.edulinguaghana;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private MediaPlayer startPlayer;
    private boolean hasNavigated = false;

    private ImageView ivLogo;
    private TextView tvAppNameSplash;
    private TextView tvTaglineSplash;
    private ProgressBar progressBar;
    private LinearLayout progressContainer;
    private ValueAnimator progressAnimator;
    private ObjectAnimator sparkleAnimator;

    private ImageView progressSparkle;
    private ImageView topWave;
    private ImageView bottomWave;
    private LinearLayout centerCard;

    // Decorative elements
    private ImageView decorStar1, decorStar2;
    private ImageView decorCircle1, decorCircle2;
    private ImageView decorDiamond1, decorDiamond2, decorDiamond3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Bind views
        ivLogo = findViewById(R.id.ivLogo);
        tvAppNameSplash = findViewById(R.id.tvAppNameSplash);
        tvTaglineSplash = findViewById(R.id.tvTaglineSplash);
        progressBar = findViewById(R.id.progressBar);
        progressContainer = findViewById(R.id.progressContainer);
        progressSparkle = findViewById(R.id.progressSparkle);
        topWave = findViewById(R.id.topWave);
        bottomWave = findViewById(R.id.bottomWave);
        centerCard = findViewById(R.id.centerCard);

        // Bind decorative elements
        decorStar1 = findViewById(R.id.decorStar1);
        decorStar2 = findViewById(R.id.decorStar2);
        decorCircle1 = findViewById(R.id.decorCircle1);
        decorCircle2 = findViewById(R.id.decorCircle2);
        decorDiamond1 = findViewById(R.id.decorDiamond1);
        decorDiamond2 = findViewById(R.id.decorDiamond2);
        decorDiamond3 = findViewById(R.id.decorDiamond3);

        progressBar.setMax(100);
        progressBar.setProgress(0);

        setupInitialAnimationState();
        startIntroAnimations();
        startDecorativeAnimations();
        startLoading();
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
        if (progressContainer != null) {
            progressContainer.setAlpha(0f);
            progressContainer.setTranslationY(dp16);
        }
        if (progressSparkle != null) {
            progressSparkle.setAlpha(0f);
            progressSparkle.setTranslationX(0f);
        }
    }

    private void startIntroAnimations() {
        // Bounce-in logo with rotation
        ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .rotation(360f)
                .setDuration(1100)
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

        if (progressContainer != null) {
            progressContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(700)
                    .setDuration(600)
                    .start();
        }
        if (progressSparkle != null) {
            progressSparkle.animate()
                    .alpha(1f)
                    .setStartDelay(900)
                    .setDuration(400)
                    .start();
        }
    }

    private void startDecorativeAnimations() {
        // Animate waves
        if (topWave != null) {
            topWave.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.wave_top_animation));
        }
        if (bottomWave != null) {
            bottomWave.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.wave_bottom_animation));
        }

        // Add pulse animation to center card
        if (centerCard != null) {
            centerCard.postDelayed(() -> {
                if (centerCard != null) {
                    centerCard.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.center_card_pulse));
                }
            }, 1500);
        }

        // Add shimmer effect to logo
        if (ivLogo != null) {
            ivLogo.postDelayed(() -> {
                if (ivLogo != null) {
                    ivLogo.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.logo_shimmer));
                }
            }, 1000);
        }

        // Animate decorative elements with staggered delays
        animateDecorativeElement(decorStar1, 1200, 0, true);
        animateDecorativeElement(decorStar2, 1400, 200, true);
        animateDecorativeElement(decorCircle1, 1600, 400, false);
        animateDecorativeElement(decorCircle2, 1800, 600, false);
        animateDecorativeElement(decorDiamond1, 2000, 800, false);
        animateDecorativeElement(decorDiamond2, 2200, 1000, false);
        animateDecorativeElement(decorDiamond3, 2400, 1200, false);
    }

    private void animateDecorativeElement(ImageView element, long fadeInDelay, long floatDelay, boolean isStar) {
        if (element == null) return;

        // Fade in animation
        element.animate()
                .alpha(0.8f)
                .setStartDelay(fadeInDelay)
                .setDuration(600)
                .withEndAction(() -> {
                    if (element != null) {
                        // Start special animation after fade in
                        element.postDelayed(() -> {
                            if (element != null) {
                                if (isStar) {
                                    // Stars get a twinkling effect
                                    element.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.star_twinkle));
                                } else {
                                    // Others get floating effect
                                    element.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.floating_element));
                                }
                            }
                        }, floatDelay);
                    }
                })
                .start();
    }

    private void startLoading() {
        // Define a fallback mechanism for a standard 3-second splash
        Runnable fallback = () -> {
            animateProgressBar(3000);
            new CountDownTimer(3000, 30) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Progress handled by animator for smoothness
                }
                @Override
                public void onFinish() {
                    progressBar.setProgress(100);
                    openMainScreen();
                }
            }.start();
        };

        try {
            startPlayer = MediaPlayer.create(this, R.raw.app_start);

            if (startPlayer == null) {
                fallback.run();
                return;
            }

            int soundDuration = startPlayer.getDuration();
            if (soundDuration <= 0) { // Check for invalid duration
                startPlayer.release();
                startPlayer = null;
                fallback.run();
                return;
            }

             // Navigate when sound completes
             startPlayer.setOnCompletionListener(mp -> {
                 if (startPlayer != null) {
                     mp.release();
                     startPlayer = null;
                 }
                 openMainScreen();
             });

            animateProgressBar(soundDuration);
            startPlayer.start();

        } catch (Exception e) {
            // If anything goes wrong, just use the fallback
            fallback.run();
        }
    }

    private void animateProgressBar(long durationMs) {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        progressBar.setProgress(0);
        long safeDuration = Math.max(durationMs, 300);
        progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(safeDuration);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressBar.setProgress(progress);
            animateSparkle(progress);
        });
        progressAnimator.start();
        startSparkleLoop(safeDuration);
    }

    private void startSparkleLoop(long duration) {
        if (progressSparkle == null) return;
        if (sparkleAnimator != null) {
            sparkleAnimator.cancel();
        }
        sparkleAnimator = ObjectAnimator.ofFloat(progressSparkle, "translationX", 0f, -progressSparkle.getWidth());
        sparkleAnimator.setDuration(duration);
        sparkleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        sparkleAnimator.setRepeatMode(ValueAnimator.RESTART);
        sparkleAnimator.setInterpolator(new LinearInterpolator());
        sparkleAnimator.start();
        progressSparkle.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.sparkle_bounce));
    }

    private void animateSparkle(int progress) {
        if (progressSparkle == null) {
            return;
        }
        progressSparkle.setAlpha(progress >= 5 ? 1f : 0f);
        progressSparkle.setTranslationX(progressBar.getWidth() * (progress / 100f) - progressSparkle.getWidth());
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
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator = null;
        }
        if (sparkleAnimator != null) {
            sparkleAnimator.cancel();
            sparkleAnimator = null;
        }
        if (progressSparkle != null) {
            progressSparkle.clearAnimation();
        }
    }
}
