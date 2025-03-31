package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.chatapp.R;
import com.example.chatapp.database.AppDatabase;
import com.example.chatapp.database.DataPurgeManager;
import com.example.chatapp.utils.session.SessionManager;
import com.example.chatapp.worker.DataPurgeWorker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Màn hình khởi động của ứng dụng
 * Hiển thị logo và kiểm tra trạng thái phiên đăng nhập
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final String DATA_PURGE_WORK_TAG = "data_purge_work";
    private static final long MIN_SPLASH_DURATION = 1500; // Thời gian tối thiểu hiển thị splash: 1.5 giây

    private DataPurgeManager dataPurgeManager;
    private AppDatabase chatDatabase;
    private SessionManager sessionManager;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Ghi lại thời điểm bắt đầu hiển thị splash
        final long startTime = System.currentTimeMillis();

        // Khởi tạo Handler cho main thread
        mainHandler = new Handler(Looper.getMainLooper());

        // Khởi tạo ExecutorService cho các tác vụ nền
        executorService = Executors.newSingleThreadExecutor();

        // Khởi tạo database
        chatDatabase = AppDatabase.getInstance(this);

        // Khởi tạo DataPurgeManager
        dataPurgeManager = new DataPurgeManager(this, chatDatabase);

        // Khởi tạo SessionManager để kiểm tra trạng thái đăng nhập
        sessionManager = new SessionManager(this);

        // Chạy các tác vụ dọn dẹp và lên lịch
        checkAndRunDataPurge();
        scheduleDataPurgeWork();

        // Kiểm tra session trong một thread riêng để không chặn UI
        executorService.execute(() -> {
            try {
                // Đây là nơi kiểm tra session - có thể mất thời gian
                final boolean shouldGoToHome = checkIfUserSessionValid();

                // Ghi log kết quả kiểm tra
                Log.d(TAG, "Kết quả kiểm tra session: " + (shouldGoToHome ? "Session hợp lệ" : "Cần đăng nhập"));

                // Tính toán thời gian đã trôi qua
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingTime = Math.max(0, MIN_SPLASH_DURATION - elapsedTime);

                // Chờ thêm nếu chưa đạt thời gian tối thiểu
                mainHandler.postDelayed(() -> {
                    // Chuyển hướng màn hình dựa trên kết quả kiểm tra session
                    if (shouldGoToHome) {
                        startHomeActivity();
                    } else {
                        startOnboardingActivity();
                    }
                }, remainingTime);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi kiểm tra session: " + e.getMessage(), e);
                // Trong trường hợp lỗi, chuyển về màn hình đăng nhập để an toàn
                mainHandler.postDelayed(this::startOnboardingActivity,
                        Math.max(0, MIN_SPLASH_DURATION - (System.currentTimeMillis() - startTime)));
            }
        });
    }

    /**
     * Kiểm tra tính hợp lệ của phiên đăng nhập
     * @return true nếu người dùng có thể vào màn hình chính, false nếu cần đăng nhập lại
     */
    private boolean checkIfUserSessionValid() {
        // Kiểm tra người dùng đã đăng nhập chưa
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Phiên đăng nhập tồn tại, kiểm tra tính hợp lệ");

            // Kiểm tra phiên đăng nhập có hết hạn không
            if (sessionManager.isSessionExpired()) {
                Log.d(TAG, "Phiên đăng nhập đã hết hạn");

                // Kiểm tra nếu refresh token còn hạn
                String refreshToken = sessionManager.getRefreshToken();
                if (refreshToken != null && !sessionManager.isTokenExpired(refreshToken)) {
                    Log.d(TAG, "Refresh token còn hạn, sẽ tự động refresh token ở HomeActivity");
                    return true; // Cho phép vào HomeActivity để tự refresh token
                } else {
                    Log.d(TAG, "Refresh token đã hết hạn, cần đăng nhập lại");
                    return false; // Cần đăng nhập lại
                }
            } else {
                Log.d(TAG, "Phiên đăng nhập còn hạn");

                // Kiểm tra xem có cần refresh token trong thời gian tới không
                if (sessionManager.shouldRefreshToken()) {
                    Log.d(TAG, "Access token sắp hết hạn, sẽ được refresh tại HomeActivity");
                }

                return true; // Phiên đăng nhập hợp lệ
            }
        } else {
            Log.d(TAG, "Chưa đăng nhập, cần mở màn hình Onboarding");
            return false; // Chưa đăng nhập
        }
    }

    /**
     * Chuyển đến màn hình chính
     */
    private void startHomeActivity() {
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Chuyển đến màn hình onboarding
     */
    private void startOnboardingActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Kiểm tra và chạy dọn dẹp dữ liệu
     */
    private void checkAndRunDataPurge() {
        // Kiểm tra xem có cần dọn dẹp dữ liệu hay không
        // Chỉ chạy kiểm tra, không chạy dọn dẹp ngay lập tức
        dataPurgeManager.checkAndRunDataPurge();
    }

    /**
     * Lên lịch chạy dọn dẹp dữ liệu định kỳ
     */
    private void scheduleDataPurgeWork() {
        // Thiết lập điều kiện để chạy worker: khi điện thoại đang sạc và kết nối WiFi
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build();

        // Tạo công việc định kỳ chạy mỗi tuần
        PeriodicWorkRequest purgeWorkRequest = new PeriodicWorkRequest.Builder(
                DataPurgeWorker.class,
                7, TimeUnit.DAYS)  // Chạy mỗi 7 ngày
                .setConstraints(constraints)
                .addTag(DATA_PURGE_WORK_TAG)
                .build();

        // Đăng ký công việc với WorkManager
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                DATA_PURGE_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,  // Giữ lại công việc hiện có nếu đã được lên lịch
                purgeWorkRequest);
    }

    /**
     * Cung cấp getter để truy cập từ các thành phần khác
     */
    public DataPurgeManager getDataPurgeManager() {
        return dataPurgeManager;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        // Dọn dẹp cache khi thiết bị sắp hết bộ nhớ
        if (dataPurgeManager != null) {
            dataPurgeManager.cleanupCache();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Dọn dẹp tài nguyên khi Activity bị hủy
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}