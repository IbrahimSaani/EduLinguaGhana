package com.edulinguaghana;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
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
    private LinearLayout morphDotsContainer;

    // Morph dots
    private View dot1, dot2, dot3, dot4, dot5;

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
        morphDotsContainer = findViewById(R.id.morphDotsContainer);

        // Bind morph dots
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);
        dot5 = findViewById(R.id.dot5);

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
        // Simplified logo animation - faster and smoother
        ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .withEndAction(() -> {
                    // Start morph dot animation after logo completes
                    startMorphDotAnimation();
                })
                .start();

        // Slide + fade tagline with reduced delay
        tvTaglineSplash.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(2000)
                .setDuration(500)
                .start();

        if (progressContainer != null) {
            progressContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(2200)
                    .setDuration(500)
                    .start();
        }
        if (progressSparkle != null) {
            progressSparkle.animate()
                    .alpha(1f)
                    .setStartDelay(2300)
                    .setDuration(300)
                    .start();
        }
    }

    private void startMorphDotAnimation() {
        // Animate dots appearing one by one with pulse - faster timing
        View[] dots = {dot1, dot2, dot3, dot4, dot5};
        long baseDelay = 120; // Reduced from 200

        for (int i = 0; i < dots.length; i++) {
            final View dot = dots[i];
            final int index = i;

            if (dot != null) {
                dot.postDelayed(() -> {
                    dot.animate()
                            .alpha(1f)
                            .scaleX(1.15f)
                            .scaleY(1.15f)
                            .setDuration(250)
                            .setInterpolator(new OvershootInterpolator(1.5f))
                            .withEndAction(() -> {
                                // Pulse the dot
                                dot.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(150)
                                        .start();
                            })
                            .start();
                }, baseDelay * index);
            }
        }

        // After all dots appear, morph them into text - faster timing
        long morphDelay = baseDelay * dots.length + 300; // Reduced from 400
        morphDotsContainer.postDelayed(() -> {
            // Fade out dots container
            morphDotsContainer.animate()
                    .alpha(0f)
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(250)
                    .withEndAction(() -> {
                        morphDotsContainer.setVisibility(android.view.View.GONE);
                        // Reveal text with morph effect
                        revealAppName();
                    })
                    .start();
        }, morphDelay);
    }

    private void revealAppName() {
        tvAppNameSplash.setScaleX(0.6f);
        tvAppNameSplash.setScaleY(0.6f);
        tvAppNameSplash.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
    }

    private void startDecorativeAnimations() {
        // Animate waves with error handling
        if (topWave != null) {
            try {
                topWave.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.wave_top_animation));
            } catch (Exception e) {
                // Ignore animation errors
            }
        }
        if (bottomWave != null) {
            try {
                bottomWave.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.wave_bottom_animation));
            } catch (Exception e) {
                // Ignore animation errors
            }
        }

        // Simplified decorative animations - only animate key elements
        // Stars twinkle
        animateDecorativeElement(decorStar1, 1200, true);
        animateDecorativeElement(decorStar2, 1400, true);

        // Circles float
        animateDecorativeElement(decorCircle1, 1600, false);
        animateDecorativeElement(decorCircle2, 1800, false);

        // Only animate one diamond to reduce load
        animateDecorativeElement(decorDiamond1, 2000, false);
    }

    private void animateDecorativeElement(ImageView element, long fadeInDelay, boolean isStar) {
        if (element == null) return;

        // Simplified fade in animation
        element.animate()
                .alpha(0.7f)
                .setStartDelay(fadeInDelay)
                .setDuration(400)
                .withEndAction(() -> {
                    if (element != null) {
                        try {
                            if (isStar) {
                                // Stars get a twinkling effect
                                element.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.star_twinkle));
                            } else {
                                // Others get floating effect
                                element.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.floating_element));
                            }
                        } catch (Exception e) {
                            // Ignore animation errors
                        }
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
        // Simplified sparkle animation - just follow the progress
        sparkleAnimator = ObjectAnimator.ofFloat(progressSparkle, "alpha", 0.8f, 1.0f);
        sparkleAnimator.setDuration(400);
        sparkleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        sparkleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        sparkleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        sparkleAnimator.start();
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

        // Clean up hardware layers before transitioning
        cleanupHardwareAcceleration();

        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void cleanupHardwareAcceleration() {
        // Return to normal rendering mode to free GPU memory
        if (ivLogo != null) ivLogo.setLayerType(View.LAYER_TYPE_NONE, null);
        if (tvAppNameSplash != null) tvAppNameSplash.setLayerType(View.LAYER_TYPE_NONE, null);
        if (tvTaglineSplash != null) tvTaglineSplash.setLayerType(View.LAYER_TYPE_NONE, null);
        if (progressBar != null) progressBar.setLayerType(View.LAYER_TYPE_NONE, null);
        if (centerCard != null) centerCard.setLayerType(View.LAYER_TYPE_NONE, null);
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
