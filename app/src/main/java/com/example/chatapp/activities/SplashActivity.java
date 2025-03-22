package com.example.chatapp.activities;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.chatapp.R;
import com.example.chatapp.database.AppDatabase;
import com.example.chatapp.database.DataPurgeManager;
import com.example.chatapp.worker.DataPurgeWorker;

import java.util.concurrent.TimeUnit;


public class SplashActivity extends AppCompatActivity {

    private String TAG = "SplashActivity";
    private static final String DATA_PURGE_WORK_TAG = "data_purge_work";
    private DataPurgeManager dataPurgeManager;
    private AppDatabase chatDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo database
        chatDatabase = AppDatabase.getInstance(this);

        // Khởi tạo DataPurgeManager
        dataPurgeManager = new DataPurgeManager(this, chatDatabase);

        // Kiểm tra và chạy dọn dẹp dữ liệu khi khởi động
        checkAndRunDataPurge();

        // Lên lịch chạy dọn dẹp định kỳ
        scheduleDataPurgeWork();

        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
            finish();
        }, 2000);
    }

    private void checkAndRunDataPurge() {
        // Kiểm tra xem có cần dọn dẹp dữ liệu hay không
        // Chỉ chạy kiểm tra, không chạy dọn dẹp ngay lập tức
        dataPurgeManager.checkAndRunDataPurge();
    }

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

    // Cung cấp getter để truy cập từ các thành phần khác
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
}
