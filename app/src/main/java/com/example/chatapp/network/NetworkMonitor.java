package com.example.chatapp.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import com.example.chatapp.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Theo dõi liên tục trạng thái kết nối mạng và thông báo cho các thành phần đăng ký
 * - Tự động cập nhật trạng thái mạng theo thời gian thực
 * - Cung cấp cơ chế đăng ký/hủy đăng ký dễ sử dụng
 */
public class NetworkMonitor {
    private static NetworkMonitor instance;
    private final Context context;
    private final List<NetworkStateListener> listeners = new ArrayList<>();
    private boolean isNetworkAvailable;
    private final BroadcastReceiver networkReceiver;

    public interface NetworkStateListener {
        void onNetworkStateChanged(boolean isAvailable);
    }

    private NetworkMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.isNetworkAvailable = NetworkUtil.isNetworkAvailable(context);

        // Tạo BroadcastReceiver để lắng nghe thay đổi kết nối
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean newNetworkState = NetworkUtil.isNetworkAvailable(context);
                if (isNetworkAvailable != newNetworkState) {
                    Log.d("NetworkMonitor", "Network state changed: " + newNetworkState);
                    isNetworkAvailable = newNetworkState;
                    notifyListeners();
                }
            }
        };

        // Đăng ký BroadcastReceiver
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkReceiver, filter);
    }

    public static synchronized NetworkMonitor getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkMonitor(context);
        }
        return instance;
    }

    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }

    public void addListener(NetworkStateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(NetworkStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (NetworkStateListener listener : listeners) {
            listener.onNetworkStateChanged(isNetworkAvailable);
        }
    }

    public void unregister() {
        if (networkReceiver != null) {
            context.unregisterReceiver(networkReceiver);
        }
    }
}