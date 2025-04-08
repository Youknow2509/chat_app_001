package com.example.chatapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.chatapp.R;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.network.NetworkMonitor;
import com.example.chatapp.utils.session.SessionManager;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TokenRefreshService extends Service implements NetworkMonitor.NetworkStateListener {
    private static final String TAG = "TokenRefreshService";
    private static final long CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(10);
    private static final int NOTIFICATION_ID = 2001;
    private static final String CHANNEL_ID = "token_refresh_channel";

    private final Handler handler = new Handler();
    private NetworkMonitor networkMonitor;
    private SessionManager sessionManager;
    private ApiManager apiManager;
    private PowerManager.WakeLock wakeLock;
    private boolean isRefreshing = false;
    private final Runnable tokenCheckRunnable = new Runnable() {
        @Override
        public void run() {
            checkAndRefreshTokenIfNeeded();
            handler.postDelayed(this, CHECK_INTERVAL);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "TokenRefreshService created");

        // Khởi tạo các thành phần
        networkMonitor = NetworkMonitor.getInstance(this);
        sessionManager = new SessionManager(this);
        apiManager = new ApiManager(this);

        // Đăng ký lắng nghe sự thay đổi mạng
        networkMonitor.addListener(this);

        // Tạo notification channel cho Android 8.0+
        createNotificationChannel();

        // Tạo WakeLock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "TokenRefresh:WakeLock"
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "TokenRefreshService started");

        // Khởi động service như một foreground service
        startAsForeground();

        if (intent != null && "com.example.chatapp.FORCE_REFRESH_TOKEN".equals(intent.getAction())) {
            Log.d(TAG, "Force refresh token requested");
            refreshToken();
        } else {
            startTokenChecking();
        }

        return START_STICKY;
    }

    private void startAsForeground() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ChatApp")
                .setContentText("Đang duy trì kết nối")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API 34+
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Token Refresh Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Duy trì phiên đăng nhập");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "TokenRefreshService destroyed");

        try {
            // 1. Dừng các tác vụ đang chạy trước
            stopTokenChecking();

            // 2. Hủy đăng ký listener TRƯỚC KHI unregister NetworkMonitor
            if (networkMonitor != null) {
                try {
                    networkMonitor.removeListener(this);
                } catch (Exception e) {
                    Log.e(TAG, "Error removing network listener", e);
                }
            }

            // 3. Giải phóng WakeLock
            releaseWakeLock();
        } catch (Exception e) {
            Log.e(TAG, "Error during service destruction", e);
        } finally {
            // Luôn gọi super.onDestroy() trong finally để đảm bảo nó được thực thi
            super.onDestroy();
        }
    }

    @Override
    public void onNetworkStateChanged(boolean isAvailable) {
        if (isAvailable) {
            // Khi có mạng, thử refresh token nếu cần
            checkAndRefreshTokenIfNeeded();
        }
    }

    private void startTokenChecking() {
        acquireWakeLock(TimeUnit.HOURS.toMillis(1));

        // Dừng lịch trình hiện tại nếu có
        handler.removeCallbacks(tokenCheckRunnable);

        // Kiểm tra ngay lập tức
        handler.post(tokenCheckRunnable);
    }

    private void stopTokenChecking() {
        handler.removeCallbacks(tokenCheckRunnable);
        releaseWakeLock();
    }

    private void acquireWakeLock(long timeout) {
        try {
            if (!wakeLock.isHeld()) {
                if (timeout > 0) {
                    wakeLock.acquire(timeout);
                } else {
                    wakeLock.acquire();
                }
                Log.d(TAG, "WakeLock acquired");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error acquiring WakeLock", e);
        }
    }

    private void releaseWakeLock() {
        try {
            if (wakeLock.isHeld()) {
                wakeLock.release();
                Log.d(TAG, "WakeLock released");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error releasing WakeLock", e);
        }
    }

    private synchronized void checkAndRefreshTokenIfNeeded() {
        // Nếu không đăng nhập, không cần kiểm tra
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "Not logged in, skipping token check");
            return;
        }

        // Nếu đang refresh, không thực hiện lại
        if (isRefreshing) {
            Log.d(TAG, "Token refresh already in progress");
            return;
        }

        // Kiểm tra xem token có cần refresh không
        if (sessionManager.shouldRefreshToken()) {
            Log.d(TAG, "Token needs to be refreshed");

            // Kiểm tra kết nối mạng
            if (!networkMonitor.isNetworkAvailable()) {
                Log.d(TAG, "Network unavailable, cannot refresh token");
                return;
            }

            // Refresh token
            refreshToken();
        } else {
            Log.d(TAG, "Token is still valid, no refresh needed");
        }
    }

    private void refreshToken() {
        isRefreshing = true;

        // Giữ WakeLock trong quá trình refresh token
        acquireWakeLock(TimeUnit.MINUTES.toMillis(2));

        String accessToken = sessionManager.getAccessToken();
        String refreshToken = sessionManager.getRefreshToken();

        if (accessToken == null || refreshToken == null) {
            Log.e(TAG, "Cannot refresh token - tokens are null");
            isRefreshing = false;
            releaseWakeLock();
            return;
        }

        Log.d(TAG, "Starting token refresh");
        Log.d(TAG, "Access token after refresh: " + accessToken);
        Log.d(TAG, "Refresh token after refresh: " + refreshToken);

        // Gọi API refresh token
        apiManager.refreshToken(accessToken, refreshToken, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        // Xử lý response thành công
                        Object data = response.body().getData();

                        // Parse dữ liệu token mới
                        try {
                            JSONObject tokenData = new JSONObject(data.toString());
                            String newAccessToken = tokenData.getString("token");
                            String newRefreshToken = tokenData.getString("refresh_token");

                            // Cập nhật token trong SessionManager
                            sessionManager.updateTokens(newAccessToken, newRefreshToken);

                            Log.d(TAG, "Token refreshed successfully");
                            Log.d(TAG, "Access token after refresh: " + newAccessToken);
                            Log.d(TAG, "Refresh token after refresh: " + newRefreshToken);
                            // Thông báo token đã được refresh
                            broadcastTokenRefreshed();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing token response", e);
                        }
                    } else {
                        // Xử lý response lỗi
                        handleRefreshError(response);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception during token refresh", e);
                } finally {
                    isRefreshing = false;
                    releaseWakeLock();
                }
            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                Log.e(TAG, "Network error during token refresh", t);
                isRefreshing = false;
                releaseWakeLock();
            }
        });
    }

    private void handleRefreshError(Response<ResponseData<Object>> response) {
        try {
            // Xử lý các mã lỗi khác nhau
            if (response.code() == 401) {
                if (response.errorBody() != null) {
                    String errorBodyString = response.errorBody().string();
                    Log.e(TAG, "Refresh token error: " + errorBodyString);

                    try {
                        JSONObject errorJson = new JSONObject(errorBodyString);
                        int errorCode = errorJson.optInt("code", 0);

                        // Mã lỗi 40002 hoặc 40003 - token không hợp lệ hoặc hết hạn
                        if (errorCode == 40002 || errorCode == 40003) {
                            handleInvalidRefreshToken();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                    }
                }
            } else {
                Log.e(TAG, "Failed to refresh token: " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling refresh error", e);
        }
    }

    private void handleInvalidRefreshToken() {
        Log.d(TAG, "Refresh token is invalid, logging out user");

        // Đăng xuất người dùng
        sessionManager.logout();

        // Gửi broadcast thông báo session hết hạn
        Intent intent = new Intent("com.example.chatapp.SESSION_EXPIRED");
        sendBroadcast(intent);
    }

    private void broadcastTokenRefreshed() {
        Intent intent = new Intent("com.example.chatapp.TOKEN_REFRESHED");
        sendBroadcast(intent);
    }
}