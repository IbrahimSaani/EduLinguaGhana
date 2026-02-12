package com.edulinguaghana;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ChallengesActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ExtendedFloatingActionButton fabNewChallenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.challenges_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        tabLayout = findViewById(R.id.challenges_tab_layout);
        viewPager = findViewById(R.id.challenges_view_pager);
        fabNewChallenge = findViewById(R.id.fab_new_challenge);

        // Setup ViewPager with adapter
        ChallengesPagerAdapter adapter = new ChallengesPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                if (position == 0) {
                    tab.setText(R.string.tab_quest);
                } else {
                    tab.setText(R.string.tab_badges);
                }
            }
        ).attach();

        // FAB click listener
        fabNewChallenge.setOnClickListener(v -> {
            // TODO: Implement create challenge dialog
            Toast.makeText(this, "Create new challenge feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
