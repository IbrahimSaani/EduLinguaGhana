package com.edulinguaghana;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AvatarPagerAdapter extends FragmentStateAdapter {

    public AvatarPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AvatarSkinToneFragment();
            case 1:
                return new AvatarHairFragment();
            case 2:
                return new AvatarEyesMouthFragment();
            case 3:
                return new AvatarAccessoryFragment();
            case 4:
                return new AvatarClothingFragment();
            case 5:
                return new AvatarExpressionFragment();
            default:
                return new AvatarSkinToneFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }
}
