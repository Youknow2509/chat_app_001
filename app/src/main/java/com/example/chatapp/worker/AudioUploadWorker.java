package com.example.chatapp.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.chatapp.network.ApiClient;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Worker để tải file ghi âm lên server
 */
public class AudioUploadWorker extends Worker {
    private static final String TAG = "AudioUploadWorker";

    public AudioUploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String filePath = getInputData().getString("filePath");
        String token = getInputData().getString("token");

        if (filePath == null) {
            Log.e(TAG, "Audio file path is null");
            return Result.failure();
        }

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            Log.e(TAG, "Audio file does not exist: " + filePath);
            return Result.failure();
        }

        try {
            // Đây là giả định về cách gọi API tải lên
            // Bạn sẽ cần điều chỉnh phần này để phù hợp với API thực tế của bạn

            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicReference<String> serverUrlRef = new AtomicReference<>();
            final AtomicReference<Boolean> successRef = new AtomicReference<>(false);

            // Gọi API để tải lên
            // TODO
//            ApiClient.getInstance()
//                    .uploadAudioFile(audioFile, token, new ApiClient.ApiCallback<ApiResponse>() {
//                        @Override
//                        public void onSuccess(ApiResponse response) {
//                            // Giả sử response có URL trỏ tới audio trên server
//                            serverUrlRef.set(response.getUrl());
//                            successRef.set(true);
//                            latch.countDown();
//                        }
//
//                        @Override
//                        public void onError(Exception e) {
//                            Log.e(TAG, "Error uploading audio file", e);
//                            latch.countDown();
//                        }
//                    });

            // Đợi kết quả
            latch.await();

            if (successRef.get() && serverUrlRef.get() != null) {
                Data outputData = new Data.Builder()
                        .putString("serverUrl", serverUrlRef.get())
                        .build();
                return Result.success(outputData);
            } else {
                return Result.failure();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in AudioUploadWorker", e);
            return Result.failure();
        }
    }
}