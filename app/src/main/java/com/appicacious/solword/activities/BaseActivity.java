package com.appicacious.solword.activities;

import static com.appicacious.solword.constants.Constants.BASE_URL_PLAY_MARKET;
import static com.appicacious.solword.constants.Constants.BASE_URL_PLAY_STORE;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.appicacious.solword.BuildConfig;
import com.appicacious.solword.R;
import com.appicacious.solword.architecture.MainViewModel;
import com.appicacious.solword.interfaces.OnFragmentInteractionListener;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.tasks.Task;

public abstract class BaseActivity extends AppCompatActivity implements
        OnFragmentInteractionListener {
    private static final String TAG = BaseActivity.class.getSimpleName();
    MainViewModel mainViewModel;
    private LifecycleEventObserver lifecycleEventObserver;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setContentView(R.layout.activity_main);


    }

    @Override
    @Nullable
    public Task<Void> getLaunchReviewTask() {
        ReviewInfo reviewInfo = mainViewModel.getReviewInfo();
        if (reviewInfo != null) {
            return mainViewModel.getReviewManager().launchReviewFlow(this, reviewInfo);
        }
        return null;
    }


    @Override
    public void initRateApp() {
        Uri uri = Uri.parse(BASE_URL_PLAY_MARKET + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        /* We need to add the following flags to the intent to make sure that we are taken back to
        our app when we press back button in Google Play.*/
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(BASE_URL_PLAY_STORE + getPackageName())));
        }
    }

    @Override
    public void initShareApp() {
        Intent shareApp = new Intent();
        shareApp.setAction(Intent.ACTION_SEND);
        String builder = "Please install " + getString(R.string.app_name) +
                " from Google Play:\n\n" + BASE_URL_PLAY_STORE + getPackageName();
        shareApp.setType("text/plain");
        shareApp.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareApp.putExtra(Intent.EXTRA_TEXT, builder);
        /* We need to add the following flags to the intent to make sure that we are taken back to
        our app when we press back button in Google Play.*/
        shareApp.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                | Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        startActivity(Intent.createChooser(shareApp, "Share " + getString(R.string.app_name)));
    }

    @Override
    public void openUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void openCustomTab(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

        CustomTabColorSchemeParams defaultColors = new CustomTabColorSchemeParams.Builder()
                .setToolbarColor(ContextCompat.getColor(this, android.R.color.white))
                .build();
        builder.setDefaultColorSchemeParams(defaultColors);

        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    @Override
    public boolean isAppActive() {
        Log.d(TAG, "isAppActive called with: " + getLifecycle().getCurrentState());
        return getLifecycle().getCurrentState() == Lifecycle.State.RESUMED;
    }

    @Override
    public boolean isVibrationEnabled() {
        return mainViewModel.isVibrationEnabled();
    }

//    @Override
//    public void setVibrationEnabled(boolean isEnabled) {
//        getDefaultSharedPreferences().edit().putBoolean(getString(R.string.pref_key_vibration),
//                (isVibrationEnabled = isEnabled)).apply();
//    }

    SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public void destroyHandler(@Nullable Handler handler) {
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void destroyHandlers(@Nullable Handler... handlers) {
        if (handlers != null) for (Handler handler : handlers) destroyHandler(handler);
    }

    @Override
    public boolean isDebugMode() {
        return BuildConfig.DEBUG;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
