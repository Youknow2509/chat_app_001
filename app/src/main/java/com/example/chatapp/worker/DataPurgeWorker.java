package com.example.chatapp.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.chatapp.activities.SplashActivity;
import com.example.chatapp.database.AppDatabase;
import com.example.chatapp.database.DataPurgeManager;

public class DataPurgeWorker extends Worker {
    private static final String TAG = "DataPurgeWorker";

    public DataPurgeWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        Log.i(TAG, "Bắt đầu công việc dọn dẹp dữ liệu định kỳ");

        try {
            // Lấy instance của DataPurgeManager từ Application
            DataPurgeManager purgeManager = ((SplashActivity) getApplicationContext())
                    .getDataPurgeManager();

            // Chạy dọn dẹp dữ liệu
            purgeManager.runDataPurge();

            Log.i(TAG, "Dọn dẹp dữ liệu hoàn tất thành công");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi dọn dẹp dữ liệu", e);
            return Result.failure();
        }
    }
}