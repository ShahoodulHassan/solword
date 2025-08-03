package com.appicacious.solword.utilities;

import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;

public class NetworkCallback extends ConnectivityManager.NetworkCallback {

    private final OnNetworkCallbacksListener mListener;


    public NetworkCallback(OnNetworkCallbacksListener listener) {
        mListener = listener;
    }

    @Override
    public void onAvailable(@NonNull Network network) {
       mListener.onAvailable(network, true);
    }


    @Override
    public void onCapabilitiesChanged(@NonNull Network network,
                                      @NonNull NetworkCapabilities networkCapabilities) {
        mListener.onCapabilitiesChanged(network, networkCapabilities);
    }

    @Override
    public void onLost(@NonNull Network network) {
        mListener.onAvailable(network, false);
    }

    @Override
    public void onLinkPropertiesChanged(@NonNull Network network,
                                        @NonNull LinkProperties linkProperties) {
    }

    @Override
    public void onUnavailable() {
        mListener.onUnavailable();
    }



    public interface OnNetworkCallbacksListener {

        void onAvailable(@NonNull Network network, boolean isAvailable);

        void onUnavailable();

        void onCapabilitiesChanged(@NonNull Network network,
                                   @NonNull NetworkCapabilities networkCapabilities);
    }
}
