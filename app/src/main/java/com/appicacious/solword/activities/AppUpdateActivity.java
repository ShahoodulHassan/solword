package com.appicacious.solword.activities;

import static com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.appicacious.solword.R;
import com.appicacious.solword.fragments.BaseFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.ActivityResult;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

public abstract class AppUpdateActivity extends NetworkActivity
        implements InstallStateUpdatedListener {
    private static final String TAG = AppUpdateActivity.class.getSimpleName();
    private static final int APP_UPDATE_REQ_CODE = 101;
    private AppUpdateManager appUpdateManager;
    private AppUpdateInfo appUpdateInfo;
    private int appUpdateCount = 0;
    private Handler updateHandler;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkUpdateAvailability();
    }

    private void checkUpdateAvailability() {
        Log.i(TAG, "checkUpdateAvailability: called");
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(this);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo info) {
                Log.i(TAG, "onSuccess: called");
                appUpdateInfo = info;
                if ((appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        // For a flexible update, use AppUpdateType.FLEXIBLE
                        && appUpdateInfo.isUpdateTypeAllowed(FLEXIBLE))) {
                    // Register listener to monitor install state
                    if (appUpdateManager != null)
                        appUpdateManager.registerListener(AppUpdateActivity.this);
                    // Request the update.
                    requestAppUpdate();
                }
            }
        });
        appUpdateInfoTask.addOnFailureListener(e -> {
            Log.e(TAG, "onFailure: " + e.getLocalizedMessage());
        });
    }

    @Override
    public void onStateUpdate(InstallState installState) {
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate();
        } else if (installState.installStatus() == InstallStatus.INSTALLED) {
            appUpdateManager.unregisterListener(this);
        }
    }

    private void popupSnackbarForCompleteUpdate() {
        BaseFragment fragment = getCurrentFragment();
        View navHostView = findViewById(R.id.nav_host_fragment);
        View fragView = null;
        if (fragment != null) fragView = fragment.getParentView();
        Log.d(TAG, "popupSnackbarForCompleteUpdate: fragment parent view=" + fragView);
        Log.d(TAG, "popupSnackbarForCompleteUpdate: nav host view=" + navHostView);
        Snackbar snackbar = Snackbar.make(
                fragView == null ? navHostView : fragView,
                "Update downloaded!",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("INSTALL", v -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(getResources().getColor(R.color.teal_200));
        snackbar.show();
    }

    @Override
    public void checkUpdateInProgress() {
        if (appUpdateManager != null) {
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(
                    appUpdateInfo -> {
                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                            popupSnackbarForCompleteUpdate();
                        }
                    });
        }
    }

    private void requestAppUpdate() {
        if (appUpdateManager != null && appUpdateInfo != null) {
            try {
                appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                        FLEXIBLE,
                        // The current activity making the update request.
                        AppUpdateActivity.this,
                        // Include a request code to later monitor this update request.
                        APP_UPDATE_REQ_CODE);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                Log.e(TAG, "onSuccess: Error = " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_UPDATE_REQ_CODE) {
            appUpdateCount++;
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "onActivityResult: In-app update successful");
            } else {
                if (resultCode == RESULT_CANCELED) {
                    Log.e(TAG, "onActivityResult: In-app update " +
                            "canceled by user");
                } else if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                    Log.e(TAG, "onActivityResult: In-app update failed");
                    // If the update fails, request to start the update again in 10 sec.
                    if (appUpdateCount < 3) {
                        destroyHandler(updateHandler);
                        updateHandler = new Handler();
                        updateHandler.postDelayed(this::requestAppUpdate, 10000);
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUpdateInProgress();
    }

    @Override
    protected void onDestroy() {
        destroyHandler(updateHandler);
        super.onDestroy();
    }
}
