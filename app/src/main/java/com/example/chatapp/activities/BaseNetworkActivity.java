package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.network.NetworkMonitor;
import com.example.chatapp.service.NetworkMonitorService;
import com.google.android.material.snackbar.Snackbar;

public abstract class BaseNetworkActivity extends AppCompatActivity implements NetworkMonitor.NetworkStateListener {

    protected NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkMonitor = NetworkMonitor.getInstance(this);
        startNetworkMonitorService();
    }

    /**
     * Khởi động dịch vụ giám sát mạng
     */
    protected void startNetworkMonitorService() {
        Intent serviceIntent = new Intent(this, NetworkMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    /**
     * Hiển thị Snackbar với thông báo
     */
    protected void showSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNetworkStateChanged(boolean isAvailable) {
        if (isAvailable) {
            showSnackbar("Mạng đã được kết nối");
            onNetworkAvailable();
        } else {
            onNetworkUnavailable();
        }
    }

    /**
     * Được gọi khi mạng khả dụng - để các lớp con override nếu cần
     */
    protected void onNetworkAvailable() {
        Log.d("BaseNetworkActivity", "onNetworkAvailable");
        // Lớp con có thể override để thực hiện các hành động cụ thể
    }

    /**
     * Được gọi khi mạng không khả dụng - để các lớp con override nếu cần
     */
    protected void onNetworkUnavailable() {
        Log.d("BaseNetworkActivity", "onNetworkUnavailable");
        // Lớp con có thể override để thực hiện các hành động cụ thể
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkMonitor.addListener(this);
        if (networkMonitor.isNetworkAvailable()) {
            onNetworkAvailable();
        } else {
            onNetworkUnavailable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (networkMonitor != null) {
            networkMonitor.removeListener(this);
        }
    }
}