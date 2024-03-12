package com.crifan.keepaliveandroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

public class NetworkMonitoringUtil extends ConnectivityManager.NetworkCallback {
    private static final String TAG = "NetworkMonitoringUtil";

    private final NetworkRequest mNetworkRequest;
    private final ConnectivityManager mConnectivityManager;

    // Constructor
    public NetworkMonitoringUtil(Context context) {
        mNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        Log.d(TAG, "onAvailable() called: Connected to network");
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        Log.e(TAG, "onLost() called: Lost network connection");
    }

    /**
     * Registers the Network-Request callback
     * (Note: Register only once to prevent duplicate callbacks)
     */
    public void registerNetworkCallbackEvents() {
        Log.d(TAG, "registerNetworkCallbackEvents() called");
        mConnectivityManager.registerNetworkCallback(mNetworkRequest, this);
    }
}