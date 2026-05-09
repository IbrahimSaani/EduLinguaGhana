package com.edulinguaghana;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    
    private static final String PREF_NAME = "EduLinguaPrefs";
    private static final String KEY_SEEN_INTRO = "SEEN_INTRO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        viewPager = findViewById(R.id.viewPagerTutorial);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        tabLayout = findViewById(R.id.tabLayoutIndicator);

        List<TutorialSlide> slides = new ArrayList<>();
        slides.add(new TutorialSlide("🇬🇭", "Welcome to EduLingua", "Your journey to mastering Ghanaian languages starts here. Let's explore!"));
        slides.add(new TutorialSlide("🎤", "Recital Mode", "Listen and learn the correct pronunciation of alphabets and numbers."));
        slides.add(new TutorialSlide("📝", "Practice Mode", "Repeat after the app to perfect your accent with real-time feedback."));
        slides.add(new TutorialSlide("🎯", "Quiz & Games", "Test your knowledge with fun quizzes and fast-paced speed challenges."));
        slides.add(new TutorialSlide("📊", "Track Progress", "Monitor your learning journey and earn achievements as you grow."));

        TutorialAdapter adapter = new TutorialAdapter(slides);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == slides.size() - 1) {
                    btnNext.setText("Get Started");
                } else {
                    btnNext.setText("Next");
                }
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
        }

        @Override
        public int getItemCount() {
            return slides.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvTitle, tvDescription;
            ViewHolder(View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tvSlideEmoji);
                tvTitle = itemView.findViewById(R.id.tvSlideTitle);
                tvDescription = itemView.findViewById(R.id.tvSlideDescription);
            }
        }
    }
}
