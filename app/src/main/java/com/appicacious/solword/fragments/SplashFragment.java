package com.appicacious.solword.fragments;

import static com.appicacious.solword.constants.Constants.TAG_NAV;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavDirections;

import com.appicacious.solword.BuildConfig;
import com.appicacious.solword.R;
import com.appicacious.solword.constants.Constants;

public class SplashFragment extends BaseFragment {


    private Handler splashHandler;

//    private Group appGroup, consentGroup;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG + TAG_NAV, "onCreate: called SplashFragment");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        initializeViews(view);



        return view;
    }

    @Override
    void initializeViews(View view) {
        super.initializeViews(view);

        if (mListener.areTermsAccepted()) {
            TextView tvVersion = view.findViewById(R.id.tv_about_app_version);
           tvVersion.setText(BuildConfig.VERSION_NAME);
            tvVersion.setVisibility(View.VISIBLE);
        }
//        else {
//            AppCompatTextView tvConsent = view.findViewById(R.id.tv_consent);
//            SpannableStringBuilder builder = new SpannableStringBuilder();
//            builder.append("Before proceeding further, \nplease read and accept the\n")
//                    .append(getClickableSpan(getString(R.string.text_privacy), URL_PRIVACY))
//                    .append("\nand\n")
//                    .append(getClickableSpan(getString(R.string.text_terms), URL_TERMS));
//            tvConsent.setMovementMethod(new LinkMovementMethod());
//            tvConsent.setText(builder);
//
//            view.findViewById(R.id.bt_accept).setOnClickListener(this);
//            view.findViewById(R.id.bt_leave).setOnClickListener(this);
//            view.findViewById(R.id.consent_group).setVisibility(View.VISIBLE);
//        }


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListener.setStatusBarColor(R.color.color_splash_background);
    }

    //    private Spannable getClickableSpan(final String text, final String url) {
//        Runnable runnable = () -> openCustomTab(url);
//        return SpanUtils.getClickableSpan(text, runnable);
//    }

//    @Override
//    public void onClick(View view) {
//        super.onClick(view);
//        if (view.getId() == R.id.bt_accept) {
//            mListener.setTermsAccepted(true);
//            initialiseApp();
//        } else if (view.getId() == R.id.bt_leave) {
//            super.handleBackPress();
//        }
//    }

    @Override
    void handleBackPress() {
        // Leaving it blank would disable back press for this fragment
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG + TAG_NAV, "onViewStateRestored: navController=" + mNavController + "mListener=" + mListener);

        if (mNavController != null && savedInstanceState == null) {
            if (mListener.areTermsAccepted()) {
                // We first set InputFragment as home fragment and then navigate to it.
                mNavController.getGraph().setStartDestination(R.id.inputFragment);
            } else {
                // We first set IntroFragment as home fragment and then navigate to it.
                mNavController.getGraph().setStartDestination(R.id.introFragment);
            }
        }

//        destroyHandler(splashHandler);
//        splashHandler = new Handler();
//        splashHandler.postDelayed(this::initialiseApp, Constants.SPLASH_DELAY);
    }

    @Override
    public void onResume() {
        super.onResume();
        destroyHandler(splashHandler);
        splashHandler = new Handler();
        splashHandler.postDelayed(this::initialiseApp, Constants.SPLASH_DELAY);
    }

    @Override
    public void onPause() {
        destroyHandler(splashHandler);
        super.onPause();
    }

    private void initialiseApp() {
        if (mNavController != null) {
            NavDirections action;
            if (mListener.areTermsAccepted()) {
                action = SplashFragmentDirections.actionSplashFragmentToInputFragment();
                // We first set InputFragment as home fragment and then navigate to it.
//                mNavController.getGraph().setStartDestination(R.id.inputFragment);
            } else {
                action = SplashFragmentDirections.actionSplashFragmentToIntroFragment();
                // We first set IntroFragment as home fragment and then navigate to it.
//                mNavController.getGraph().setStartDestination(R.id.introFragment);
            }
            navigateTo(action);
        }
    }

    @Override
    public void onDestroyView() {
//        destroyHandler(splashHandler);
        super.onDestroyView();
    }
}
