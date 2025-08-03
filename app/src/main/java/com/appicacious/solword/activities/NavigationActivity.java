package com.appicacious.solword.activities;

import static com.appicacious.solword.constants.Constants.TAG_NAV;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.appicacious.solword.R;
import com.appicacious.solword.fragments.BaseFragment;

public abstract class NavigationActivity extends BaseActivity {

    private static final String TAG = NavigationActivity.class.getSimpleName();

    private NavHostFragment navHostFragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // In case of recreation of Activity due to configuration changes or system enforced
            // app death, we change the start destination from Splash to either Input or Intro, as
            // required.
            // In case of new app install or launch (savedInstanceState == null), we do it in
            // SplashFragment in onViewStateRestored. However, in case of recreation, it couldn't
            // be done there because it would have to be done in onCreate of that fragment and there
            // is no View object available there to retrieve the NavController.
            // I mentioned onCreate() because in case of config changes or app death, we would
            // already be in some other fragment than Splash and we would already have removed
            // Splash from the back stack when the app was launched. So, no method of that fragment
            // will be called except onCreate().
            if (savedInstanceState != null) {
                if (areTermsAccepted()) {
                    Log.d(TAG + TAG_NAV, "onCreate: Activity recreated, home dest is Input");
                    navController.getGraph().setStartDestination(R.id.inputFragment);
                } else {
                    Log.d(TAG + TAG_NAV, "onCreate: Activity recreated, home dest is Intro");
                    navController.getGraph().setStartDestination(R.id.introFragment);
                }
            }


            // This is only for testing purposes
            if (isDebugMode()) {
                navController.addOnDestinationChangedListener((navController1, navDestination,
                                                               bundle) -> {
                    Log.d(TAG + TAG_NAV, String.format("onDestinationChanged: destId=%d, " +
                                    "destName=%s, homeId=%d, homeName=%s",
                            navDestination.getId(), navDestination.getDisplayName()
                                    .replace("com.appicacious.solword:", ""),
                            navController.getGraph().getStartDestinationId(),
                            navController.getGraph().getStartDestDisplayName()
                                    .replace("com.appicacious.solword", "")));

                    int bsCount = navController.getBackQueue().size();
                    Log.d(TAG + TAG_NAV, "onDestinationChanged: backStackCount=" + bsCount);
                    for (NavBackStackEntry entry : navController.getBackQueue()) {
                        Log.d(TAG + TAG_NAV, "onDestinationChanged: backStackEntry=" +
                                entry.getDestination().getDisplayName()
                                        .replace("com.appicacious.solword:", ""));
                    }
                });
            }
        } else {
            Log.d(TAG + TAG_NAV, "onCreate: navHostFragment is null");
        }
    }


    /**
     * That's the shortest and cleanest solution for finding the currently visible fragment in
     * Navigation Components
     *
     * @return null or an instance of BaseFragment
     */
    @Override
    @Nullable
    public BaseFragment getCurrentFragment() {
        if (navHostFragment != null) {
            Fragment currFragment = navHostFragment.getChildFragmentManager()
                    .getPrimaryNavigationFragment();
            if (currFragment instanceof BaseFragment) {
                return (BaseFragment) currFragment;
            } else {
                if (currFragment == null) Log.d(TAG, "getCurrentFragment: null fragment");
                else Log.d(TAG, "getCurrentFragment: fragment is not BaseFragment=" + currFragment);
            }
        }
        Log.d(TAG, "getCurrentFragment: navHostFragment is null");
        return null;
    }



}
