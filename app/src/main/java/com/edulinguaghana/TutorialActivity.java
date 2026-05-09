package com.edulinguaghana;

import android.animation.ArgbEvaluator;
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
import android.widget.ImageView;
import android.widget.TextView;

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

    // Bright, kid-friendly colors for each slide background
    private final int[] colors = {
        Color.parseColor("#FFEBEE"), // Slide 1: Soft Pink
        Color.parseColor("#E3F2FD"), // Slide 2: Sky Blue
        Color.parseColor("#E8F5E9"), // Slide 3: Mint Green
        Color.parseColor("#FFFDE7"), // Slide 4: Lemon Yellow
        Color.parseColor("#F3E5F5")  // Slide 5: Soft Purple
    };

    // Primary accent colors for the buttons/bubbles
    private final int[] accentColors = {
        Color.parseColor("#E91E63"), // Pink
        Color.parseColor("#2196F3"), // Blue
        Color.parseColor("#4CAF50"), // Green
        Color.parseColor("#FBC02D"), // Yellow/Amber
        Color.parseColor("#9C27B0")  // Purple
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
        slides.add(new TutorialSlide("🇬🇭", "Hi! I'm Kojo!", "Welcome to EduLingua! I'm so excited to help you learn our beautiful languages!"));
        slides.add(new TutorialSlide("🎤", "Listen & Speak", "I'll say a word, and you repeat it. It's like talking with a friend!"));
        slides.add(new TutorialSlide("📝", "Fun Challenges", "We have cool puzzles and games. Can you get the highest score?"));
        slides.add(new TutorialSlide("🎯", "Speed Games", "Think fast! Play the Speed Challenge to become a language superstar!"));
        slides.add(new TutorialSlide("🏆", "Win Prizes!", "Earn shiny badges and unlock achievements as you learn. You're going to be great!"));

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
    }

    private void celebrateAndFinish() {
        vibrate(100);
        btnNext.animate().scaleX(1.5f).scaleY(1.5f).alpha(0f).setDuration(500).withEndAction(this::finishTutorial).start();
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
        Animation floatAnim = AnimationUtils.loadAnimation(this, R.anim.floating_element);
        for (int i = 0; i < animatedShapesContainer.getChildCount(); i++) {
            View child = animatedShapesContainer.getChildAt(i);
            floatAnim.setStartOffset(i * 500L);
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
                
                // Jump animation for the mascot
                Animation jump = AnimationUtils.loadAnimation(context, R.anim.mascot_jump);
                jump.setStartOffset(1000);
                holder.ivMascot.startAnimation(jump);
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
            ImageView ivMascot;
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
