package com.appicacious.solword.fragments;

import static com.appicacious.solword.constants.Constants.URL_PRIVACY;
import static com.appicacious.solword.constants.Constants.URL_TERMS;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.appicacious.solword.R;
import com.appicacious.solword.utilities.SpanUtils;

public class ConsentFragment extends Fragment implements View.OnClickListener {


    public static ConsentFragment newInstance() {
        Bundle args = new Bundle();
        ConsentFragment fragment = new ConsentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_consent, container, false);

        initializeViews(view);

        return view;
    }



    private void initializeViews(View view) {
        AppCompatTextView tvConsent = view.findViewById(R.id.tv_consent);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("Before proceeding further, \nplease read and accept the\n")
                .append(getClickableSpan(getString(R.string.text_privacy), URL_PRIVACY))
                .append("\nand\n")
                .append(getClickableSpan(getString(R.string.text_terms), URL_TERMS));
        tvConsent.setMovementMethod(new LinkMovementMethod());
        tvConsent.setText(builder);

        view.findViewById(R.id.bt_accept).setOnClickListener(this);
        view.findViewById(R.id.bt_leave).setOnClickListener(this);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        applyBackgroundColor(R.color.color_splash_background);
    }

    private Spannable getClickableSpan(final String text, final String url) {
        Runnable runnable = () -> openCustomTab(url);
        return SpanUtils.getClickableSpan(text, runnable);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_accept) {
            initialiseApp();
        } else if (view.getId() == R.id.bt_leave) {
            onBackPressed();
        }
    }

    public void onBackPressed() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof IntroFragment) ((IntroFragment) fragment).handleBackPress();
    }

    public void initialiseApp() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof IntroFragment) ((IntroFragment) fragment).initialiseApp();
    }

    private void openCustomTab(String url) {
        Fragment fragment = getParentFragment();
        if (fragment instanceof IntroFragment) ((IntroFragment) fragment).openCustomTab(url);
    }

    private void applyBackgroundColor(@ColorRes int colorRes) {
        Fragment fragment = getParentFragment();
        if (fragment instanceof IntroFragment)
            ((IntroFragment) fragment).applyBackgroundColor(colorRes);
    }


}
