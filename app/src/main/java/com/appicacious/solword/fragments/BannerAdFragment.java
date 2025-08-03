package com.appicacious.solword.fragments;

import static android.view.View.GONE;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appicacious.solword.R;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;

public class BannerAdFragment extends BaseFragment {

    private MaxAdView maxBanner;
    private Runnable bannerRunnable;

    @Override
    void initializeViews(View view) {
        super.initializeViews(view);

        if (!mListener.isPurchased()) maxBanner = view.findViewById(R.id.max_banner);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        billingViewModel.getIsPurchasedLiveData().observe(getViewLifecycleOwner(), isPurchased -> {
            Log.d(TAG, "onViewCreated: isPurchased=" + isPurchased);
            if (Boolean.TRUE.equals(isPurchased)) {
                destroyBannerAd();
            } else {
                initAndLoadBannerAd();
            }
        });

//        mainViewModel.getAdNetworkInitializedLiveData().observe(getViewLifecycleOwner(), isInitialized -> {
//            if (Boolean.TRUE.equals(isInitialized)) {
//                initAndLoadBannerAd();
//            } else {
//                destroyBannerAd();
//            }
//        });


        mainViewModel.getIsInternetAvailableLiveData().observe(getViewLifecycleOwner(),
                isAvailable -> {
                    if (isAvailable != null && isAvailable) {
                        if (!mListener.isPurchased()) {
                            if (bannerRunnable != null) {
                                // Run
                                bannerRunnable.run();
                                // Reset
                                bannerRunnable = null;
                            }
                        } else {
                            // If app is purchased but there was a runnable pending, reset it.
                            if (bannerRunnable != null) bannerRunnable = null;
                        }
                    }
                });
    }

    void initAndLoadBannerAd() {
        Log.d(TAG, "initAndLoadBannerAd: called with banner=" + maxBanner);
        if (!mListener.isPurchased() && maxBanner != null) {
            maxBanner.setListener(getMaxBannerAdListener());
            bannerRunnable = () -> {
                if (maxBanner != null) maxBanner.loadAd();
            };
            mListener.checkInternet();
        }
    }

    private MaxAdViewAdListener getMaxBannerAdListener() {
        return new MaxAdViewAdListener() {
            @Override
            public void onAdExpanded(MaxAd ad) {

            }

            @Override
            public void onAdCollapsed(MaxAd ad) {

            }

            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onBannerLoaded: called");
                if (!mListener.isPurchased()) {
                    if (maxBanner != null && maxBanner.getVisibility() != View.VISIBLE) {
                        maxBanner.setVisibility(View.VISIBLE);
                    }
                } else {
                    // TODO: 19/08/2022 Remove if this is redundant
                    /* If banner got loaded even when the user is subscribed, we should destroy
                     the whole ad setup */
                    destroyBannerAd();
                }
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {
                Log.d(TAG, "onBannerDisplayed: called");
            }

            @Override
            public void onAdHidden(MaxAd ad) {

            }

            @Override
            public void onAdClicked(MaxAd ad) {
                Log.d(TAG, "onBannerClicked: called");

            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.d(TAG, "onBannerFailed: " + error.getMessage());
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.d(TAG, "onBannerDisplayFailed: error=" + error.getMessage());
            }
        };
    }

    void destroyBannerAd() {
        Log.d(TAG, "destroyBannerAd: called with banner=" + maxBanner);
        if (maxBanner != null) {
            maxBanner.setVisibility(GONE);
            maxBanner.destroy();
            maxBanner = null;
        }
    }

    @Override
    public void onDestroyView() {
        destroyBannerAd();
        super.onDestroyView();
    }
}
