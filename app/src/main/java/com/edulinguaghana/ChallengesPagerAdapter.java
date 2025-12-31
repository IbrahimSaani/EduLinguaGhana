package com.edulinguaghana;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ChallengesPagerAdapter extends FragmentStateAdapter {
    public ChallengesPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new QuestFragment();
        return new BadgesFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

