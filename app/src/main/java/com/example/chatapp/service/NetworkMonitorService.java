package com.example.chatapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.chatapp.activities.SplashActivity;
import com.example.chatapp.R;
import com.example.chatapp.network.NetworkMonitor;

/**
 * Theo dõi liên tục trạng thái kết nối mạng và thông báo cho các thành phần đăng ký
 */
public class NetworkMonitorService extends Service implements NetworkMonitor.NetworkStateListener {
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "network_monitor_channel";
    private NetworkMonitor networkMonitor;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        networkMonitor = NetworkMonitor.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
        networkMonitor.addListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Đối với Android 14+ (API level 34+), loại ForegroundServiceType phải khớp với manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            // Đảm bảo loại foregroundServiceType phù hợp với khai báo trong manifest
            int foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;

            // Khởi động service như một foreground service với loại xác định
            startForeground(NOTIFICATION_ID, createNotification(getString(R.string.network_monitoring_active)),
                    foregroundServiceType);
        } else {
            // Đối với các phiên bản Android cũ hơn
            startForeground(NOTIFICATION_ID, createNotification(getString(R.string.network_monitoring_active)));
        }

        // Kiểm tra trạng thái mạng hiện tại và thông báo nếu cần
        if (!networkMonitor.isNetworkAvailable()) {
            showNoNetworkNotification();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onNetworkStateChanged(boolean isAvailable) {
        if (!isAvailable) {
            Log.d("NetworkMonitorService", "No network connection");
            showNoNetworkNotification();
        } else {
            Log.d("NetworkMonitorService", "Network connection available");
            // Ẩn thông báo khi có mạng
            notificationManager.cancel(NOTIFICATION_ID + 1);
        }
    }

    private void showNoNetworkNotification() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.no_internet_connection))
                .setContentText(getString(R.string.app_functionality_limited))
                .setSmallIcon(R.drawable.ic_no_network)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManager.notify(NOTIFICATION_ID + 1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.network_monitor_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(getString(R.string.network_monitor_channel_description));
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_network_check)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        networkMonitor.removeListener(this);
        super.onDestroy();
    }
}