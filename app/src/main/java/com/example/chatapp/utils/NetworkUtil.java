package com.example.chatapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

public class NetworkUtil {
    private static final String TAG = "NetworkUtil";
    private static NetworkUtil instance;
    private final ConnectivityManager connectivityManager;
    private NetworkCallback networkCallback;
    private boolean isNetworkAvailable = false;
    private NetworkAvailabilityListener networkAvailabilityListener;

    private NetworkUtil(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        checkInitialNetworkState();
        registerNetworkCallback();
    }

    public static synchronized NetworkUtil getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkUtil(context.getApplicationContext());
        }
        return instance;
    }

    private void checkInitialNetworkState() {
        isNetworkAvailable = isInternetAvailable();
    }

    public boolean isInternetAvailable() {
        if (connectivityManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            // For older Android versions
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    private void registerNetworkCallback() {
        if (connectivityManager == null) return;

        try {
            if (networkCallback == null) {
                networkCallback = new NetworkCallback();
                NetworkRequest.Builder builder = new NetworkRequest.Builder();
                NetworkRequest networkRequest = builder.build();
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering network callback: " + e.getMessage());
        }
    }

    public void unregisterNetworkCallback() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                networkCallback = null;
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering network callback: " + e.getMessage());
            }
        }
    }

    public void setNetworkAvailabilityListener(NetworkAvailabilityListener listener) {
        this.networkAvailabilityListener = listener;
    }

    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }

    private class NetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            isNetworkAvailable = true;
            if (networkAvailabilityListener != null) {
                networkAvailabilityListener.onNetworkAvailable();
            }
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            isNetworkAvailable = false;
            if (networkAvailabilityListener != null) {
                networkAvailabilityListener.onNetworkLost();
            }
        }
    }

    public interface NetworkAvailabilityListener {
        void onNetworkAvailable();
        void onNetworkLost();
    }
}