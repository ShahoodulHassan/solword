package com.appicacious.solword.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appicacious.solword.utilities.NetworkCallback;

public abstract class NetworkActivity extends NavigationActivity implements
        NetworkCallback.OnNetworkCallbacksListener {
    private static final String TAG = NetworkActivity.class.getSimpleName();


    private ConnectivityManager cm;
    private NetworkCallback networkCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cm = (ConnectivityManager) getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);

        registerNetworkCallback();


    }

    @Override
    public void checkInternet() {
        mainViewModel.checkInternet(isConnected());
    }

    public boolean isConnected() {
        return getConnectionType() > 0;
    }

    @IntRange(from = 0, to = 3)
    public int getConnectionType() {
        int result = 0; // Returns connection type. 0: none; 1: mobile data; 2: wifi; 3: VPN

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = 3;
                    }
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = 2;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = 1;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_VPN) {
                        result = 3;
                    }
                }
            }
        }
        Log.d(TAG, "getConnectionType: " + result);
        return result;
    }

    private void registerNetworkCallback() {
        networkCallback = new NetworkCallback(this);
        cm.registerNetworkCallback(new NetworkRequest.Builder().build(), networkCallback);
    }

    private void unregisterNetworkCallback() {
        if (cm != null && networkCallback != null) {
            cm.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
        }
    }

    @Override
    public void onAvailable(@NonNull Network network, boolean isAvailable) {
        mainViewModel.checkInternet(isAvailable);
    }

    @Override
    public void onUnavailable() {
        mainViewModel.checkInternet(false);
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, 
                                      @NonNull NetworkCapabilities networkCapabilities) {

    }

    @Override
    protected void onDestroy() {
        unregisterNetworkCallback();
        super.onDestroy();
    }
}
