package com.appicacious.solword.activities;

import static android.view.View.GONE;

import static com.appicacious.solword.constants.Constants.INTERSTITIAL_ID;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.appicacious.solword.R;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinUserService;

import java.util.concurrent.TimeUnit;

public abstract class AdActivity extends AppUpdateActivity {

    private static final String TAG = AdActivity.class.getSimpleName();
    private AppLovinSdk appLovinSdk;
    private MaxAdView maxBanner;
    private MaxInterstitialAd maxInterstitial;
    private Handler maxAdReloadHandler;
    private int retryAttempt;
    private boolean isBannerAdAllowed = true, isBannerAdLoaded;
    private Runnable bannerRunnable, interstitialRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mainViewModel.getAdNetworkInitializedLiveData().observe(this, isInitialized -> {
            Log.d(TAG, "onCreate: called with isInitialized=" + isInitialized);
            if (isInitialized != null && isInitialized) initAndLoadAds();
        });

        mainViewModel.getIsInternetAvailableLiveData().observe(this, isAvailable -> {
            Log.d(TAG, "onCreate: called with isAvailable=" + isAvailable);
            if (isAvailable != null && isAvailable) {
                if (!isPurchased()) {

                    if (bannerRunnable != null) {
                        // Run
                        bannerRunnable.run();
                        // Reset
                        bannerRunnable = null;
                    }

                    if (interstitialRunnable != null) {
                        // Run
                        interstitialRunnable.run();
                        // Reset
                        interstitialRunnable = null;
                    }
                } else {
                    // If app is purchased but there were ad runnables pending, reset them.
                    if (bannerRunnable != null) bannerRunnable = null;
                    if (interstitialRunnable != null) interstitialRunnable = null;
                }
            }
        });

    }

    protected void initAppLovinSdk() {
        if (/*!isDebugMode() && */!isPurchased() && !mainViewModel.isAdNetworkInitialized()) {
            // Make sure to set the mediation provider value to "max" to ensure proper functionality
            appLovinSdk = AppLovinSdk.getInstance(this);
            appLovinSdk.setMediationProvider("max");
            AppLovinSdk.initializeSdk(this, configuration -> {
                if (configuration.getConsentDialogState() ==
                        AppLovinSdkConfiguration.ConsentDialogState.APPLIES) {
                    Log.d(TAG, "initAppLovinSdk: consent dialog applies");
                    // Show user consent dialog
                    AppLovinUserService userService = appLovinSdk.getUserService();
                    if (userService != null) {
                        userService.showConsentDialog(AdActivity.this, () -> {
                            Log.d(TAG, "onDismiss: consent dialog dismissed");
                            mainViewModel.setAdNetworkInitialized(true);
//                            if (maxBanner != null) maxBanner.loadAd();
                        });
                    }
                } else if (configuration.getConsentDialogState() == AppLovinSdkConfiguration.
                        ConsentDialogState.DOES_NOT_APPLY) {
                    Log.d(TAG, "initAppLovinSdk: consent dialog doesn't apply");
                    // No need to show consent dialog, proceed with initialization
                    mainViewModel.setAdNetworkInitialized(true);
//                    if (maxBanner != null) maxBanner.loadAd();
                } else {
                    Log.d(TAG, "initAppLovinSdk: consent dialog state unknown");
                    // Consent dialog state is unknown. Proceed with initialization, but check if
                    // the consent dialog should be shown on the next application initialization
                    mainViewModel.setAdNetworkInitialized(true);
//                    if (maxBanner != null) maxBanner.loadAd();
                }
            });
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
                if (!isBannerAdLoaded) isBannerAdLoaded = true;
                if (!isPurchased()) {
                    if (isBannerAdAllowed()) {
//                    if (!(getCurrentFragment() instanceof RectangleAdFragment)
//                            && !appViewModel.isPromptVisible()) {
                        if (maxBanner != null && maxBanner.getVisibility() != View.VISIBLE) {
                            maxBanner.setVisibility(View.VISIBLE);
                        }
//                    }
                    } else {
                        if (maxBanner != null && maxBanner.getVisibility() == View.VISIBLE) {
                            maxBanner.setVisibility(View.GONE);
                        }
                    }
                } else {
                    /* If banner got loaded even when the user is subscribed, we should destroy
                     the whole ad setup */
                    destroyAdSdk();
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
                if (isBannerAdLoaded) isBannerAdLoaded = false;
//                if (isPurchased() && maxBanner != null && maxBanner.getVisibility() == View.VISIBLE)
//                    maxBanner.setVisibility(GONE);
                if (isPurchased()) destroyAdSdk();
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                Log.d(TAG, "onBannerDisplayFailed: error=" + error.getMessage());
            }
        };
    }

    private void initAndLoadAds() {
//        initAndLoadBannerAd();
        initAndLoadFullscreenAd();
    }

    void initAndLoadBannerAd() {
        Log.d(TAG, "initAndLoadBannerAd: called with banner=" + maxBanner);
        if (maxBanner == null) maxBanner = findViewById(R.id.max_banner);
        maxBanner.setListener(getMaxBannerAdListener());
        bannerRunnable = () -> {
            if (maxBanner != null) maxBanner.loadAd();
        };
        checkInternet();
    }

    private void initAndLoadFullscreenAd() {
        Log.d(TAG, "initAndLoadFullscreenAd: called with fullscreen " + maxInterstitial);
        if (!isPurchased()) {
            if (maxInterstitial == null) {
                maxInterstitial = new MaxInterstitialAd(INTERSTITIAL_ID, this);
                maxInterstitial.setListener(getMaxInterstitialListener());
            }
            loadFullscreenAd();
        }

        Log.d(TAG, "initAndLoadFullscreenAd: exited with fullscreen " + maxInterstitial);
    }

    private void loadFullscreenAd() {
        if (!isPurchased()) {
            interstitialRunnable = () -> {
                if (maxInterstitial != null) maxInterstitial.loadAd();
            };
            checkInternet();
        }
    }

    @Override
    public boolean showInterstitialIfReady() {
        if (isInterstitialReady()) {
            maxInterstitial.showAd();
            return true;
        } else {
            // Caching is likely already in progress if `isReady()` is false.
            // Avoid calling `load()` here and instead rely on the callbacks.
            return false;
        }
    }

    private MaxAdListener getMaxInterstitialListener() {
        return new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                Log.d(TAG, "onInterstitialLoaded: called");
                // Reset retry attempt
                retryAttempt = 0;
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {
                Log.d(TAG, "onInterstitialDismissed: called");
                mainViewModel.setInterstitialSeen(true);
                // We will pre-load a new one here
                if (!isPurchased()) loadFullscreenAd();
            }

            @Override
            public void onAdClicked(MaxAd ad) {

            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                Log.d(TAG, "onInterstitialFailed: called with error " + error.getMessage());
                if (!isPurchased()) {
                    if (maxInterstitial != null) {
                        // Interstitial ad failed to load
                        // AppLovin recommends that you retry with exponentially higher delays up to a
                        // maximum delay (in this case 64 seconds)
                        retryAttempt++;
                        long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2,
                                Math.min(6, retryAttempt)));
                        destroyHandler(maxAdReloadHandler);
                        maxAdReloadHandler = new Handler();
                        maxAdReloadHandler.postDelayed(() -> loadFullscreenAd(), delayMillis);
                    }
                } else {
                    destroyAdSdk();
                }
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                // We will pre-load a new one here
                if (!isPurchased()) {
                    loadFullscreenAd();
                } else {
                    destroyAdSdk();
                }
            }
        };
    }


    @Override
    public boolean isInterstitialReady() {
        return maxInterstitial != null && maxInterstitial.isReady();
    }

    public boolean isBannerAdAllowed() {
        return isBannerAdAllowed;
    }

    @Override
    public void setBannerAdAllowed(boolean isAllowed) {
        isBannerAdAllowed = isAllowed;
        if (isBannerAdAllowed()) {
            if (!isPurchased()) {
                if (maxBanner != null) {
                    maxBanner.setListener(getMaxBannerAdListener());
                    if (isBannerAdLoaded) {
                        if (maxBanner.getVisibility() != View.VISIBLE)
                            maxBanner.setVisibility(View.VISIBLE);
                    } else {
                        maxBanner.loadAd();
                    }
                } else {
                    initAndLoadBannerAd();
                }
            }
        } else {
            if (maxBanner != null) {
                maxBanner.setListener(null);
                if (maxBanner.getVisibility() == View.VISIBLE) maxBanner.setVisibility(GONE);
            }
        }
    }

    @Override
    public void destroyAdSdk() {
        Log.d(TAG, "destroyAdSdk: called");
        destroyBannerAd();
        destroyFullscreenAd();
        if (appLovinSdk != null) appLovinSdk = null;
    }

    private void destroyBannerAd() {
        Log.d(TAG, "destroyBannerAd: called with banner " + maxBanner);
        if (maxBanner != null) {
            maxBanner.setVisibility(GONE);
            maxBanner.destroy();
            maxBanner = null;
        }
    }

    private void destroyFullscreenAd() {
        Log.d(TAG, "destroyFullscreenAd: called with fullscreen " + maxInterstitial);
        if (maxInterstitial != null) {
            maxInterstitial.destroy();
            maxInterstitial = null;
        }
    }

    @Override
    protected void onDestroy() {
        destroyHandler(maxAdReloadHandler);
        destroyAdSdk();
        super.onDestroy();
    }
}
