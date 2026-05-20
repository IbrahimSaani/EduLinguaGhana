package com.edulinguaghana;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChallengesActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private LinearLayout loginRequiredLayout;

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
        loginRequiredLayout = findViewById(R.id.loginRequiredLayout);

        // Check login status
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showLoginRequired();
            return;
        }

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
    }

    private void showLoginRequired() {
        if (loginRequiredLayout != null) {
            loginRequiredLayout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
            
            findViewById(R.id.btnGoToLogin).setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
