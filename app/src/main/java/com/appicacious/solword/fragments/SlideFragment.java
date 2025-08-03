package com.appicacious.solword.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.appicacious.solword.R;
import com.appicacious.solword.models.IntroSlide;
import com.appicacious.solword.utilities.TopCropImageView;
import com.google.android.material.textview.MaterialTextView;

public class SlideFragment extends Fragment {

    private final static String KEY_DATA = "key_data";

    private IntroSlide mIntroSlide;

    public static SlideFragment newInstance(@NonNull IntroSlide introSlide) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_DATA, introSlide);
        SlideFragment fragment = new SlideFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) mIntroSlide = args.getParcelable(KEY_DATA);

        if (mIntroSlide == null) throw new IllegalArgumentException("No intro slide available!");


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slide, container, false);

        initializeViews(view);

        return view;
    }

    private void initializeViews(View view) {
        ConstraintLayout container = view.findViewById(R.id.cl_slide_container);
        MaterialTextView tvTitle = view.findViewById(R.id.tv_title);
        MaterialTextView tvDescription = view.findViewById(R.id.tv_description);
        TopCropImageView ivImage = view.findViewById(R.id.iv_image);

        if (mIntroSlide.getDrawable() != 0)
            ivImage.setImageResource(mIntroSlide.getDrawable());
//        container.setBackgroundColor(ContextCompat.getColor(container.getContext(),
//                mIntroSlide.getBgColorRes()));
        tvTitle.setText(mIntroSlide.getTitle());
        tvDescription.setText(mIntroSlide.getDescription());



        Fragment fragment = getParentFragment();
        if (fragment instanceof IntroFragment) {
            ((IntroFragment) fragment).applyBackgroundColor(mIntroSlide.getBgColorRes());
        }
    }
}
