package com.edulinguaghana;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialButton btnNext;
    private MaterialButton btnSkip;
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private View viewBackground;
    private ViewGroup animatedShapesContainer;
    private nl.dionsegijn.konfetti.xml.KonfettiView konfettiView;
    private List<TutorialSlide> slides;
    
    // Bright, kid-friendly colors for each slide background
    private final int[] colors = {
        Color.parseColor("#FFF5E5"), // Slide 1: Warm Peach
        Color.parseColor("#E0F7FA"), // Slide 2: Light Cyan
        Color.parseColor("#F1F8E9"), // Slide 3: Light Green
        Color.parseColor("#F3E5F5"), // Slide 4: Light Purple
        Color.parseColor("#FFFDE7")  // Slide 5: Light Yellow
    };

    // Primary accent colors for the buttons/bubbles
    private final int[] accentColors = {
        Color.parseColor("#FFAB40"), // Accent Orange
        Color.parseColor("#00BCD4"), // Cyan
        Color.parseColor("#8BC34A"), // Green
        Color.parseColor("#9C27B0"), // Purple
        Color.parseColor("#FBC02D")  // Yellow
    };

    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        viewPager = findViewById(R.id.viewPagerTutorial);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        tabLayout = findViewById(R.id.tabLayoutIndicator);
        progressBar = findViewById(R.id.progressBarTutorial);
        viewBackground = findViewById(R.id.viewBackground);
        animatedShapesContainer = findViewById(R.id.animatedShapesContainer);
        konfettiView = findViewById(R.id.konfettiView);

        slides = new ArrayList<>();
        slides.add(new TutorialSlide("🇬🇭", "Hi! I'm Owlbert!", "Welcome to EduLingua! I'm so excited to be your guide as we explore the amazing languages of Ghana!"));
        slides.add(new TutorialSlide("🎧", "Listen & Learn", "Tap any letter or number to hear how it sounds. It's like having a talking book!"));
        slides.add(new TutorialSlide("🚀", "Play Time", "Play fun mini-games like Bubble Pop and Rocket Sort! Learning feels like playing!"));
        slides.add(new TutorialSlide("🧩", "Brain Quest", "Test your memory with cool puzzles and quizzes. Can you become a language champion?"));
        slides.add(new TutorialSlide("🏆", "Shiny Badges!", "Earn badges and stars for everything you do. Let's see how many you can collect!"));

        TutorialAdapter adapter = new TutorialAdapter(slides, this);
        viewPager.setAdapter(adapter);
        
        // Depth Page Transformer
        viewPager.setPageTransformer(new DepthPageTransformer());

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        // Background color transition logic
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < colors.length - 1) {
                    viewBackground.setBackgroundColor((int) argbEvaluator.evaluate(
                        positionOffset, colors[position], colors[position + 1]));
                    
                    int currentAccent = (int) argbEvaluator.evaluate(
                        positionOffset, accentColors[position], accentColors[position + 1]);
                    btnNext.setBackgroundColor(currentAccent);
                }
            }

            @Override
            public void onPageSelected(int position) {
                updateButtonState(position, slides.size());
                updateProgressBar(position, slides.size());
                vibrate(30);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < slides.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                celebrateAndFinish();
            }
        });

        btnSkip.setOnClickListener(v -> finishTutorial());
        
        // Initial entrance
        View tutorialRoot = findViewById(R.id.tutorialRoot);
        if (tutorialRoot != null) {
            tutorialRoot.setAlpha(0f);
            tutorialRoot.animate().alpha(1f).setDuration(1000).start();
        }
        
        startFloatingAnimations();
        
        // Welcome progress
        updateProgressBar(0, slides.size());
    }

    private void updateProgressBar(int position, int total) {
        if (progressBar == null) return;
        int progress = (int) (((float)(position + 1) / total) * 100);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            progressBar.setProgress(progress, true);
        } else {
            progressBar.setProgress(progress);
        }
    }

    private void celebrateAndFinish() {
        vibrate(100);
        
        if (konfettiView != null) {
            konfettiView.start(
                new nl.dionsegijn.konfetti.core.PartyFactory(
                    new nl.dionsegijn.konfetti.core.emitter.Emitter(1000L, java.util.concurrent.TimeUnit.MILLISECONDS).max(100)
                )
                .spread(360)
                .colors(java.util.Arrays.asList(0xfce18a, 0xff726d, 0xf48fb1, 0xafdfff))
                .setSpeedBetween(10f, 30f)
                .position(new nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.3))
                .build()
            );
        }

        // Burst animation for all background shapes
        if (animatedShapesContainer != null) {
            for (int i = 0; i < animatedShapesContainer.getChildCount(); i++) {
                View child = animatedShapesContainer.getChildAt(i);
                child.animate()
                    .scaleX(3f).scaleY(3f)
                    .alpha(0f)
                    .setDuration(800)
                    .start();
            }
        }
        
        // Final "Pop" effect for the button
        btnNext.animate()
            .scaleX(2f).scaleY(2f)
            .alpha(0f)
            .setDuration(600)
            .withEndAction(this::finishTutorial)
            .start();
    }

    private void vibrate(int ms) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(ms);
        }
    }

    private void updateButtonState(int position, int total) {
        if (position == total - 1) {
            btnNext.setText("Let's Go! 🚀");
            btnNext.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).start();
        } else {
            btnNext.setText("Next");
            btnNext.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start();
        }
    }

    private void startFloatingAnimations() {
        if (animatedShapesContainer == null) return;
        
        for (int i = 0; i < animatedShapesContainer.getChildCount(); i++) {
            View child = animatedShapesContainer.getChildAt(i);
            
            // Assign different animations based on index for a more organic feel
            int animRes;
            int type = i % 4;
            if (type == 0) animRes = R.anim.float_up_slow;
            else if (type == 1) animRes = R.anim.float_up_medium;
            else if (type == 2) animRes = R.anim.floating_element;
            else animRes = R.anim.float_up_fast;

            Animation anim = AnimationUtils.loadAnimation(this, animRes);
            anim.setStartOffset(i * 300L); // Stagger them
            child.startAnimation(anim);
            
            // Add subtle rotation to alphabets and numbers
            if (child instanceof TextView) {
                ObjectAnimator rotate = ObjectAnimator.ofFloat(child, "rotation", -15f, 15f);
                rotate.setDuration(3000 + ((long) i * 100));
                rotate.setRepeatCount(ValueAnimator.INFINITE);
                rotate.setRepeatMode(ValueAnimator.REVERSE);
                rotate.start();
            }
        }
    }

    private void finishTutorial() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREF_NAME, MODE_PRIVATE);
        boolean isFirstTime = !prefs.getBoolean(MainActivity.KEY_SEEN_INTRO, false);
        
        if (isFirstTime) {
            prefs.edit().putBoolean(MainActivity.KEY_SEEN_INTRO, true).apply();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        
        finish();
    }

    private static class TutorialSlide {
        String emoji, title, description;
        TutorialSlide(String emoji, String title, String description) {
            this.emoji = emoji;
            this.title = title;
            this.description = description;
        }
    }

    private static class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {
        private final List<TutorialSlide> slides;
        private final Context context;

        TutorialAdapter(List<TutorialSlide> slides, Context context) {
            this.slides = slides;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tutorial_slide, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TutorialSlide slide = slides.get(position);
            holder.tvEmoji.setText(slide.emoji);
            holder.tvTitle.setText(slide.title);
            holder.tvDescription.setText(slide.description);
            
            // Entrance Animations
            holder.tvEmoji.setAlpha(0f);
            holder.tvEmoji.setScaleX(0.2f);
            holder.tvEmoji.setScaleY(0.2f);
            holder.tvEmoji.animate().alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(700).setInterpolator(new OvershootInterpolator(1.4f)).start();

            // Sparkle animations
            if (holder.tvSparkle1 != null) {
                holder.tvSparkle1.setAlpha(0f);
                holder.tvSparkle1.animate().alpha(1f).setStartDelay(500).setDuration(500).start();
                Animation twinkle = AnimationUtils.loadAnimation(context, R.anim.star_twinkle);
                holder.tvSparkle1.startAnimation(twinkle);
            }
            if (holder.tvSparkle2 != null) {
                holder.tvSparkle2.setAlpha(0f);
                holder.tvSparkle2.animate().alpha(1f).setStartDelay(700).setDuration(500).start();
                Animation twinkle = AnimationUtils.loadAnimation(context, R.anim.star_twinkle);
                twinkle.setStartOffset(200);
                holder.tvSparkle2.startAnimation(twinkle);
            }

            holder.bubbleCard.setTranslationY(200f);
            holder.bubbleCard.setAlpha(0f);
            holder.bubbleCard.animate().translationY(0f).alpha(1f).setStartDelay(200).setDuration(600).setInterpolator(new OvershootInterpolator(1.1f)).start();

            if (holder.ivMascot != null) {
                holder.ivMascot.setTranslationX(-100f);
                holder.ivMascot.animate().translationX(0f).setStartDelay(400).setDuration(600).setInterpolator(new OvershootInterpolator()).start();
                
                holder.ivMascot.playAnimation();
            }

            // Interactive emoji
            holder.tvEmoji.setOnClickListener(v -> {
                v.animate().rotationBy(360).scaleX(1.4f).scaleY(1.4f).setDuration(400)
                        .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(200).start())
                        .start();
                
                if (holder.ivMascot != null) {
                    holder.ivMascot.startAnimation(AnimationUtils.loadAnimation(context, R.anim.mascot_celebrate));
                }
                
                vibrate(30);
            });
            
            // Also make mascot interactive
            if (holder.ivMascot != null) {
                holder.ivMascot.setOnClickListener(v -> {
                    holder.ivMascot.playAnimation();
                    vibrate(30);
                });
            }
        }

        private void vibrate(int ms) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (v == null) return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(ms);
            }
        }

        @Override
        public int getItemCount() {
            return slides.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvTitle, tvDescription, tvSparkle1, tvSparkle2;
            LottieAnimationView ivMascot;
            View bubbleCard;
            ViewHolder(View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvSlideEmoji);
                tvTitle = itemView.findViewById(R.id.tvSlideTitle);
                tvDescription = itemView.findViewById(R.id.tvSlideDescription);
                ivMascot = itemView.findViewById(R.id.ivMascotTutorial);
                bubbleCard = itemView.findViewById(R.id.bubbleCard);
                tvSparkle1 = itemView.findViewById(R.id.tvSparkle1);
                tvSparkle2 = itemView.findViewById(R.id.tvSparkle2);
            }
        }
    }

    public static class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            if (position < -1) { view.setAlpha(0f); }
            else if (position <= 0) {
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);
            } else if (position <= 1) {
                view.setAlpha(1 - position);
                view.setTranslationX(pageWidth * -position);
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else { view.setAlpha(0f); }
        }
    }
}
