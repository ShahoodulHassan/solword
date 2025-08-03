package com.appicacious.solword.fragments;

import static com.appicacious.solword.constants.Constants.RC_PREMIUM;
import static com.appicacious.solword.constants.Constants.TAG_DICt;
import static com.appicacious.solword.constants.Constants.TAG_PREMIUM;
import static com.appicacious.solword.constants.Constants.VIBRATE_DURATION;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.appicacious.solword.R;
import com.appicacious.solword.architecture.BillingViewModel;
import com.appicacious.solword.architecture.MainViewModel;
import com.appicacious.solword.constants.Constants;
import com.appicacious.solword.dialog_fragments.BooleanDialogFragment;
import com.appicacious.solword.dialog_fragments.DictionarySelectDialogFragment;
import com.appicacious.solword.interfaces.OnDialogInteractionListener;
import com.appicacious.solword.interfaces.OnFragmentInteractionListener;
import com.appicacious.solword.models.Dictionary;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class BaseFragment extends Fragment implements
        DictionarySelectDialogFragment.OnBottomSheetInteractionListener,
        OnDialogInteractionListener, View.OnClickListener {

    protected String TAG;

    protected AppCompatActivity mActivity;
    protected OnFragmentInteractionListener mListener;
    protected MainViewModel mainViewModel;
    protected BillingViewModel billingViewModel;

    //    MaxInterstitialAd maxInterstitial;
//    private Handler maxAdReloadHandler;
//    private int retryAttempt;
    protected Runnable internetRunnable, /*interstitialLoadRunnable, */
            interstitialRunnable;
    private Toast internetToast, mToast;

    protected NavController mNavController;

//    Bundle mArguments;

    private View mParentView;

    /**
     * This flag tells us whether a fragment is newly created (true) or is the result of a back
     * button press (false)
     */
    private boolean isFragmentNewlyCreated;

    /**
     * This is a new flag introduced to show whether a fragment is new or is coming back from
     * backStack.
     * If testing is as per expectation, it will be merged with isFragmentNewlyCreated
     *
     * true = fragment is brand new
     * false = fragment is from backStack
     */
    private boolean isFragmentSavedStateNull;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = getClass().getSimpleName();

        mainViewModel = new ViewModelProvider(mActivity).get(MainViewModel.class);
        billingViewModel = new ViewModelProvider(mActivity).get(BillingViewModel.class);

        // Assign value to this flag here
        isFragmentNewlyCreated = /*true;*/savedInstanceState == null;

//        isFragmentSavedStateNull = savedInstanceState == null;

//        mArguments = getArguments();

        // For custom handling back press
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        };
        mActivity.getOnBackPressedDispatcher().addCallback(this, callback);

        setHasOptionsMenu(true);

    }

    void initializeViews(View view) {
        mParentView = view.findViewById(R.id.cl_parent_container);
    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Visible to free users only
        MenuItem getPremium = menu.findItem(R.id.menu_get_premium);
        if (getPremium != null) getPremium.setVisible(!mListener.isPurchased_Billing());

        // Visible to premium users while testing only
        MenuItem consume = menu.findItem(R.id.menu_consume);
        if (consume != null) consume.setVisible(isDebugMode() && mListener.isPurchased());
    }

    /* For this to work, the id of this menu item should be exactly same as that of the id of the
       destination fragment in nav graph. */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            handleBackPress();
            return true;
        }
        else if (item.getItemId() == R.id.menu_get_premium) {
            showGetPremiumDialog();
            return true;
        }
        else if (item.getItemId() == R.id.menu_consume) {
            mListener.initConsumeFlow();
            return true;
        }


        else {
            return NavigationUI.onNavDestinationSelected(item, mNavController) ||
                    super.onOptionsItemSelected(item);
        }
    }

    void serveFreshToast(String message) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(mActivity, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    void navigateTo(NavDirections action) {
        if (mNavController != null) mNavController.navigate(action);
    }

    void openUrl(String url) {
        mListener.openUrl(url);
    }

    void openCustomTab(String url) {
        mListener.openCustomTab(url);
    }

    List<Dictionary> getDictionaries() {
        return mListener.getDictionaries();
    }

    @Nullable
    public Dictionary getDefaultDictionary() {
        return mListener.getDefaultDictionary();
    }

    void setDefaultDictionary(int id) {
        mListener.setDefaultDictionary(id);
    }

    void showDictionarySelectDialog() {
        DictionarySelectDialogFragment dialog = DictionarySelectDialogFragment.newInstance(
                Constants.RC_DICT, new ArrayList<>(getDictionaries()));
        dialog.show(getChildFragmentManager(), TAG_DICt);
    }

    @Override
    public void onDictionaryClicked(Dictionary dictionary) {
        setDefaultDictionary(dictionary.get_id());
    }

    @Nullable
    public View getParentView() {
        return mParentView;
    }

    void destroyHandler(@Nullable Handler handler) {
        mListener.destroyHandler(handler);
    }

    void destroyHandlers(@Nullable Handler... handlers) {
        mListener.destroyHandlers(handlers);
    }

    void vibrate(long duration) {
        if (isVibrationEnabled()) {
            Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // deprecated in API 26
                v.vibrate(duration);
            }
        }
    }

    void vibrateHeavy(long duration) {
        if (isVibrationEnabled()) {
            Vibrator v = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.EFFECT_HEAVY_CLICK));
            } else {
                vibrate(duration);
            }
        }
    }

    void vibrateHeavy() {
        vibrateHeavy(VIBRATE_DURATION);
    }

    void vibrate() {
        vibrate(VIBRATE_DURATION);
    }

    boolean isVibrationEnabled() {
        return mListener.isVibrationEnabled();
    }

//    void setVibrationEnabled(boolean isEnabled) {
//        mListener.setVibrationEnabled(isEnabled);
//    }

    void hapticFeedbackLongPress(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                | HapticFeedbackConstants.LONG_PRESS);
    }

    void hapticFeedbackKeyboard(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                | HapticFeedbackConstants.KEYBOARD_TAP);
    }

    /**
     * This method may be overridden by any fragment to implement custom back navigation
     * Default behaviour is navigate up.
     */
    void handleBackPress() {
        Log.d(TAG, "handleBackPress: called");
//        if (this instanceof InputFragment) {
//            if (mActivity != null) mActivity.finish();
//        } else {
        if (mNavController == null || !mNavController.navigateUp()) {
            if (mActivity != null) mActivity.finish();
        }
//        }
    }

    /**
     * This toolbar null check is necessary for such fragment that don't have any AppBar.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        mNavController = Navigation.findNavController(view);

        // First approach
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        if (toolbar != null) {
            mActivity.setSupportActionBar(toolbar);
            NavigationUI.setupActionBarWithNavController(mActivity, mNavController);
        }

        // It fires an event whenever an interstitial is seen by the user. If there is any runnable
        // to be executed, we may execute here.
        mainViewModel.getIsInterstitialSeenLiveData().observe(getViewLifecycleOwner(), event -> {
            if (event != null && Boolean.TRUE.equals(event.getContentIfNotHandled())) {
                if (interstitialRunnable != null) {
                    if (!isPurchased()) interstitialRunnable.run();
                    interstitialRunnable = null;
                }
            }
        });

        mainViewModel.getIsInternetAvailableLiveData().observe(getViewLifecycleOwner(),
                isAvailable -> {
                    Log.d(TAG, "onViewStateRestored: isAvailable=" + isAvailable);
                    if (Boolean.TRUE.equals(isAvailable)) {
                        if (internetRunnable != null) {
                            if (!isPurchased()) internetRunnable.run();
                            internetRunnable = null;
                        }
                    } else {
                        if (internetRunnable != null) {
                            if (!isPurchased()) {
                                serveFreshToast("This feature requires internet connection!");
                            }
                            // Reset
                            internetRunnable = null;
                        }
                    }
                });
    }

    void setActionBarTitle(String title) {
        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(title);
    }

    void setActionBarSubTitle(String subTitle) {
        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) actionBar.setSubtitle(subTitle);
    }

    protected void checkInternet() {
        mListener.checkInternet();
    }

    protected void launchReviewFlow() {
        Task<Void> launchTask = mListener.getLaunchReviewTask();
        if (launchTask != null) {
            launchTask.addOnCompleteListener(task -> {
                mListener.setUsageCount(0);
                Log.d(TAG, "launchReviewFlow: isSuccessful=" + task.isSuccessful());
//                if (task.isSuccessful()) {
//                    serveFreshToast("Thanks for taking time!");
//                } else {
//                    serveFreshToast("Till next time...");
//                }
            });
        } else {
            Log.d(TAG, "launchReviewFlow: operation failed");
        }
    }

    public void showGetPremiumDialog() {
        BooleanDialogFragment dialogFragment = new BooleanDialogFragment.Builder(RC_PREMIUM)
                .setTitle("Get premium version")
//                .setMessage(HtmlCompat.fromHtml(getString(R.string.text_premium),
//                                HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)
                .setMessage("\n✔  No advertisements" +
                                "\n\n✔  No hidden guesses" +
                                "\n\n✔  No internet required *" +
                                "\n\n\n*  Internet is required for app updates, however!\n"
                )
                .setCancelable(false)
                .setNegativeButtonRequired(true)
                .setPositiveButtonText("Purchase")
                .setNegativeButtonText("Cancel")
                .build();
        dialogFragment.show(getChildFragmentManager(), TAG_PREMIUM);
    }

    @Override
    public void onDialogShown(int reqCode) {

    }

    @Override
    public void onPositivePressed(int reqCode) {
        if (reqCode == RC_PREMIUM) mListener.removeAds();
    }

    @Override
    public void onPositivePressed(int reqCode, Bundle data) {

    }

    @Override
    public void onNegativePressed(int reqCode) {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof AppCompatActivity &&
                context instanceof OnFragmentInteractionListener) {
            mActivity = (AppCompatActivity) context;
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new IllegalArgumentException("Activity not configured properly!");
        }

    }

//    boolean isBackButtonPressed() {
//        return !isFragmentNewlyCreated;
//    }

    boolean isFragmentNewlyCreated() {
        return isFragmentNewlyCreated;
    }

    public int getColCount() {
        return mListener.getColCount();
    }

    int getRowCount() {
        return mListener.getRowCount();
    }

    int getCellCount() {
        return mListener.getCellCount();
    }

    int getLastCol() {
        return mListener.getLastCol();
    }

    int getLastRow() {
        return mListener.getLastRow();
    }

    int getLastCell() {
        return mListener.getLastCell();
    }

    protected boolean isDebugMode() {
        return mListener.isDebugMode();
    }

    public boolean isPurchased() {
        return mListener.isPurchased();
    }


//    @Override
//    public void onAdLoaded(MaxAd ad) {
//        Log.d(TAG, "onInterstitialLoaded: called");
//        // Reset retry attempt
//        retryAttempt = 0;
//    }
//
//    @Override
//    public void onAdDisplayed(MaxAd ad) {
//
//    }
//
//    @Override
//    public void onAdHidden(MaxAd ad) {
//        Log.d(TAG, "onInterstitialDismissed: called");
//        // We will pre-load a new one here
//        loadFullscreenAd();
//    }
//
//    @Override
//    public void onAdClicked(MaxAd ad) {
//
//    }
//
//    @Override
//    public void onAdLoadFailed(String adUnitId, MaxError error) {
//        Log.d(TAG, "onInterstitialFailed: called with error " + error.getMessage());
//        if (!isPurchased()) {
//            if (maxInterstitial != null) {
//                // Interstitial ad failed to load
//                // AppLovin recommends that you retry with exponentially higher delays up to a
//                // maximum delay (in this case 64 seconds)
//                retryAttempt++;
//                long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2,
//                        Math.min(6, retryAttempt)));
//                destroyHandler(maxAdReloadHandler);
//                maxAdReloadHandler = new Handler();
//                maxAdReloadHandler.postDelayed(this::loadFullscreenAd, delayMillis);
//            }
//        } else {
//            destroyFullscreenAd();
//        }
//    }
//
//    @Override
//    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
//        // We will pre-load a new one here
//        if (!isPurchased()) {
//            loadFullscreenAd();
//        } else {
//            destroyFullscreenAd();
//        }
//    }

//    void initAndLoadFullscreenAd() {
//        Log.d(TAG, "initAndLoadFullscreenAd: called with fullscreen " + maxInterstitial);
//        if (!isPurchased()) {
//            if (maxInterstitial == null) {
//                maxInterstitial = new MaxInterstitialAd(INTERSTITIAL_ID, mActivity);
//                maxInterstitial.setListener(this);
//            }
//            loadFullscreenAd();
//        }
//
//        Log.d(TAG, "initAndLoadFullscreenAd: exited with fullscreen " + maxInterstitial);
//    }

//    void loadFullscreenAd() {
//        if (!isPurchased()) {
//            interstitialLoadRunnable = () -> {
//                if (maxInterstitial != null) maxInterstitial.loadAd();
//            };
//            mListener.checkInternet();
//        }
//    }

//    boolean isInterstitialReady() {
//        return maxInterstitial != null && maxInterstitial.isReady();
//    }

//    void showFullscreenAd() {
//        Log.d(TAG, "showFullscreenAd: called with fullscreen " + maxInterstitial);
//        if (maxInterstitial != null) {
//            if (maxInterstitial.isReady()) {
//                Log.d(TAG, "showFullscreenAd: showing ad");
//                maxInterstitial.showAd();
//            } else {
//                // Caching is likely already in progress if `isReady()` is false.
//                // Avoid calling `load()` here and instead rely on the callbacks.
//                Log.d(TAG, "showFullscreenAd: ad not ready yet");
////                showFullscreenAdOnLoad = true;
//            }
//        }
////        else {
////            Log.d(TAG, "showFullscreenAd: ad not initialized, initializing now");
////            initAndLoadFullscreenAd();
////        }
//    }

//    void destroyFullscreenAd() {
//        Log.d(TAG, "destroyFullscreenAd: called with fullscreen " + maxInterstitial);
//        if (maxInterstitial != null) {
//            maxInterstitial.destroy();
//            maxInterstitial = null;
//        }
//    }

    public void setStatusBarColor(@ColorRes int colorRes) {
        mListener.setStatusBarColor(colorRes);
    }

    @Override
    public void onClick(View view) {

    }

    boolean isFragmentSavedStateNull() {
        return isFragmentSavedStateNull;
    }

    @Override
    public void onDestroyView() {
        // Consume this flag here
        isFragmentNewlyCreated = false;


//        destroyHandler(maxAdReloadHandler);
//        destroyFullscreenAd();
//        interstitialLoadRunnable = null;
        interstitialRunnable = null;
        internetRunnable = null;


        super.onDestroyView();
    }


}
