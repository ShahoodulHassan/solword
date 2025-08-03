package com.appicacious.solword.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavDirections;
import androidx.viewpager2.widget.ViewPager2;

import com.appicacious.solword.R;
import com.appicacious.solword.adapters.SlideFragmentAdapter;
import com.appicacious.solword.models.IntroSlide;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class IntroFragment extends BaseFragment {

    private ViewPager2 vp2Intro;
    private CircleIndicator3 ciIntro;
    private int mMode;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMode = IntroFragmentArgs.fromBundle(getArguments()).getMMode();

        setHasOptionsMenu(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro, container, false);

        initializeViews(view);

        return view;
    }


    @Override
    void initializeViews(View view) {
        super.initializeViews(view);

        vp2Intro = view.findViewById(R.id.vp2_intro);
        ciIntro = view.findViewById(R.id.ci_intro);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewPager();
    }

    private void setupViewPager() {

        SlideFragmentAdapter slideAdapter;
        vp2Intro.setAdapter(slideAdapter = new SlideFragmentAdapter(this, mMode,
                getSlides()));

        vp2Intro.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        ciIntro.setViewPager(vp2Intro);
        slideAdapter.registerAdapterDataObserver(ciIntro.getAdapterDataObserver());
    }

    private List<IntroSlide> getSlides() {
        List<IntroSlide> slides = new ArrayList<>();
//        slides.add(new IntroSlide("Solwordle",
//                "A guess provider for Wordle and similar games", R.drawable.banner,
//                R.color.color_splash_background));
        slides.add(new IntroSlide("Intuitive inputs",
                    "Input words just like you would in the actual game. Click the alphabet to change colour",
                R.drawable.inputs3, R.color.color_splash_background));

        slides.add(new IntroSlide("Accurate guesses",
                "Solwordle is intelligent enough to curate a list of accurate guesses from a rich library of words",
                R.drawable.guesses2, R.color.color_splash_background));

        slides.add(new IntroSlide("Helpful filters",
                "Guesses are further narrowed down by applying filters of the alphabets that have not yet been tried in any attempt",
                R.drawable.filters2, R.color.color_splash_background));

        slides.add(new IntroSlide("Word definitions",
                "Clicking a guess word provides a list of online resources that provide the definition of the clicked word",
                R.drawable.dictionaries3, R.color.color_splash_background));

        return slides;
    }

    public void applyBackgroundColor(@ColorRes int colorRes) {
        mListener.setStatusBarColor(colorRes);
        View view = getView();
        if (view != null)
            view.setBackgroundColor(ContextCompat.getColor(view.getContext(), colorRes));
    }

    /**
     * This method is called only from the child fragment {@link ConsentFragment#initialiseApp()}
     * when terms are accepted
     */
    public void initialiseApp() {
        if (mNavController != null) {
            // This method is called only upon acceptance of terms, so we set the flag to true here.
            mListener.setTermsAccepted(true);

            // We first set InputFragment as home fragment and then navigate to it.
            NavDirections action = IntroFragmentDirections.actionIntroFragmentToInputFragment();
            mNavController.getGraph().setStartDestination(R.id.inputFragment);
            navigateTo(action);
        }
    }

    @Override
    void handleBackPress() {
        super.handleBackPress();
    }
}
