package com.example.chatapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.chatapp.R;
import com.example.chatapp.network.NetworkMonitor;
import com.google.android.material.snackbar.Snackbar;

public abstract class BaseNetworkFragment extends Fragment implements NetworkMonitor.NetworkStateListener {

    protected NetworkMonitor networkMonitor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkMonitor = NetworkMonitor.getInstance(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    /**
     * Hiển thị Snackbar với thông báo
     */
    protected void showSnackbar(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
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
        // Lớp con có thể override để thực hiện các hành động cụ thể
    }

    /**
     * Được gọi khi mạng không khả dụng - để các lớp con override nếu cần
     */
    protected void onNetworkUnavailable() {
        // Lớp con có thể override để thực hiện các hành động cụ thể
    }

    @Override
    public void onResume() {
        super.onResume();
        networkMonitor.addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (networkMonitor != null) {
            networkMonitor.removeListener(this);
        }
    }

    /**
     * Kiểm tra trạng thái mạng hiện tại
     */
    protected boolean isNetworkAvailable() {
        return networkMonitor != null && networkMonitor.isNetworkAvailable();
    }
}