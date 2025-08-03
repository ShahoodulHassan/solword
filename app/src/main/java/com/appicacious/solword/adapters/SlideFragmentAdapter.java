package com.appicacious.solword.adapters;

import static com.appicacious.solword.constants.Constants.MODE_INTRO;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.appicacious.solword.fragments.ConsentFragment;
import com.appicacious.solword.fragments.SlideFragment;
import com.appicacious.solword.models.IntroSlide;

import java.util.List;

public class SlideFragmentAdapter extends FragmentStateAdapter {

    private final List<IntroSlide> introSlides;
    private final int mMode;

    public SlideFragmentAdapter(@NonNull Fragment fragment, int mMode,
                                @NonNull List<IntroSlide> introSlides) {
        super(fragment);
        this.introSlides = introSlides;
        this.mMode = mMode;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position < introSlides.size()) {
            return SlideFragment.newInstance(introSlides.get(position));
        } else {
            return ConsentFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return mMode == MODE_INTRO ? introSlides.size() + 1 : introSlides.size();
    }


}
