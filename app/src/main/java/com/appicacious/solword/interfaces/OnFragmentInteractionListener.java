package com.appicacious.solword.interfaces;

import android.os.Handler;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

import com.android.billingclient.api.ProductDetails;
import com.appicacious.solword.fragments.BaseFragment;
import com.appicacious.solword.models.Dictionary;
import com.google.android.play.core.tasks.Task;

import java.util.List;

public interface OnFragmentInteractionListener {

    int getColCount();

    int getRowCount();

    int getCellCount();

    int getLastCol();

    int getLastRow();

    int getLastCell();

    boolean isVibrationEnabled();

    boolean isAppActive();

//    void setVibrationEnabled(boolean isEnabled);

    Task<Void> getLaunchReviewTask();

    void initRateApp();

    void initShareApp();

    void openUrl(String url);

    void openCustomTab(String url);

    List<Dictionary> getDictionaries();

    @Nullable
    Dictionary getDefaultDictionary();

    void setDefaultDictionary(int id);

    void destroyHandler(@Nullable Handler handler);

    void destroyHandlers(@Nullable Handler... handlers);

    void checkUpdateInProgress();

    BaseFragment getCurrentFragment();

    boolean isPurchased();

    boolean isPurchased_Billing();

    void destroyAdSdk();

    void checkInternet();

    void setBannerAdAllowed(boolean isAllowed);

    boolean isInterstitialReady();

    boolean showInterstitialIfReady();

    boolean isDebugMode();

    void setStatusBarColor(@ColorRes int colorRes);

    void queryPurchases();

    void removeAds();

    ProductDetails getProductDetails();

    void initConsumeFlow();

    boolean areTermsAccepted();

    void setTermsAccepted(boolean isAccepted);

    int getClickCount();

    void setClickCount(int count);

    int getUsageCount();

    void setUsageCount(int count);

    void incrementUsageCount(int count);

    void logDictionary(String dictName, String wordTitle, int wordSize);

    void logGuessFetched(int wordSize, int attemptCount, int guessCount, int filterCount,
                         String filterOperator);


//    int getDictionaryCount();
//
//    int getGuessPickCount();
//
//    int getGuessFilterCount();
//
//    void setDictionaryCount(int count);
//
//    void setGuessPickCount(int count);
//
//    void setGuessFilterCount(int count);


}
