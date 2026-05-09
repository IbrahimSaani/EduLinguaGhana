package com.edulinguaghana;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.os.Build;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
    private View viewBackground;
    private ViewGroup animatedShapesContainer;
    
    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_SEEN_INTRO = "SEEN_INTRO";

    // Colors for each slide background
    private final int[] colors = {
        Color.parseColor("#E8EAF6"), // Slide 1: Indigo Light
        Color.parseColor("#E1F5FE"), // Slide 2: Blue Light
        Color.parseColor("#E8F5E9"), // Slide 3: Green Light
        Color.parseColor("#FFF3E0"), // Slide 4: Orange Light
        Color.parseColor("#F3E5F5")  // Slide 5: Purple Light
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
        viewBackground = findViewById(R.id.viewBackground);
        animatedShapesContainer = findViewById(R.id.animatedShapesContainer);

        List<TutorialSlide> slides = new ArrayList<>();
        slides.add(new TutorialSlide("🇬🇭", "Welcome to EduLingua", "Your journey to mastering Ghanaian languages starts here. Let's explore!"));
        slides.add(new TutorialSlide("🎤", "Recital Mode", "Listen and learn the correct pronunciation of alphabets and numbers."));
        slides.add(new TutorialSlide("📝", "Practice Mode", "Repeat after the app to perfect your accent with real-time feedback."));
        slides.add(new TutorialSlide("🎯", "Quiz & Games", "Test your knowledge with fun quizzes and fast-paced speed challenges."));
        slides.add(new TutorialSlide("📊", "Track Progress", "Monitor your learning journey and earn achievements as you grow."));

        TutorialAdapter adapter = new TutorialAdapter(slides);
        viewPager.setAdapter(adapter);
        
        // Add Depth Page Transformer for a professional feel
        viewPager.setPageTransformer(new DepthPageTransformer());

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        // Background color transition logic
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position < colors.length - 1) {
                    viewBackground.setBackgroundColor((int) argbEvaluator.evaluate(
                        positionOffset, colors[position], colors[position + 1]));
                } else {
                    viewBackground.setBackgroundColor(colors[colors.length - 1]);
                }
            }

            @Override
            public void onPageSelected(int position) {
                updateButtonState(position, slides.size());
                vibrate();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < slides.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                finishTutorial();
            }
        });

        btnSkip.setOnClickListener(v -> finishTutorial());
        
        // Initial entrance animation
        View tutorialRoot = findViewById(R.id.tutorialRoot);
        if (tutorialRoot != null) {
            tutorialRoot.setAlpha(0f);
            tutorialRoot.animate().alpha(1f).setDuration(1000).start();
        }
        
        startFloatingAnimations();
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(30);
        }
    }

    private void updateButtonState(int position, int total) {
        if (position == total - 1) {
            btnNext.setText("Get Started");
            btnNext.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300).start();
        } else {
            btnNext.setText("Next");
            btnNext.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start();
        }
    }

    private void startFloatingAnimations() {
        if (animatedShapesContainer == null) return;
        Animation floatAnim = AnimationUtils.loadAnimation(this, R.anim.floating_element);
        for (int i = 0; i < animatedShapesContainer.getChildCount(); i++) {
            View child = animatedShapesContainer.getChildAt(i);
            floatAnim.setStartOffset(i * 500);
            child.startAnimation(floatAnim);
        }
    }

    private void finishTutorial() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isFirstTime = !prefs.getBoolean(KEY_SEEN_INTRO, false);
        
        if (isFirstTime) {
            prefs.edit().putBoolean(KEY_SEEN_INTRO, true).apply();
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

        TutorialAdapter(List<TutorialSlide> slides) {
            this.slides = slides;
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
            
            // Trigger entrance animations
            holder.tvEmoji.setAlpha(0f);
            holder.tvEmoji.setScaleX(0.5f);
            holder.tvEmoji.setScaleY(0.5f);
            holder.tvEmoji.animate().alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(600).setInterpolator(new OvershootInterpolator()).start();

            if (holder.ivMascot != null) {
                holder.ivMascot.setTranslationY(50f);
                holder.ivMascot.animate().translationY(0f).setDuration(800).setInterpolator(new OvershootInterpolator()).start();
            }

            // Interactive emoji
            holder.tvEmoji.setOnClickListener(v -> {
                v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150)
                        .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(150).start())
                        .start();
            });
        }

        @Override
        public int getItemCount() {
            return slides.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvTitle, tvDescription;
            ImageView ivMascot;
            ViewHolder(View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvSlideEmoji);
                tvTitle = itemView.findViewById(R.id.tvSlideTitle);
                tvDescription = itemView.findViewById(R.id.tvSlideDescription);
                ivMascot = itemView.findViewById(R.id.ivMascotTutorial);
            }
        }
    }

    /**
     * Professional Page Transformer
     */
    public static class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }
}
