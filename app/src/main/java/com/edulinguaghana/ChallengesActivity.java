package com.edulinguaghana;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ChallengesActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        setTitle(getString(R.string.challenges_title));

        tabLayout = findViewById(R.id.challenges_tab_layout);
        viewPager = findViewById(R.id.challenges_view_pager);

        ChallengesPagerAdapter adapter = new ChallengesPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                if (position == 0) tab.setText(R.string.tab_quest);
                else tab.setText(R.string.tab_badges);
            }
        ).attach();
    }
}
