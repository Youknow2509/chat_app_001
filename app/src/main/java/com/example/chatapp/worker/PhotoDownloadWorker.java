package com.example.chatapp.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.chatapp.utils.file.MediaUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class PhotoDownloadWorker extends Worker {
    private static final String TAG = "PhotoDownloadWorker";

    public PhotoDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        String url = getInputData().getString("url");
        String token = getInputData().getString("token");

        Log.d(TAG, "Worker started for URL: " + (url != null ? url.substring(0, Math.min(url.length(), 30)) + "..." : "null"));

        if (url == null || token == null) {
            Log.e(TAG, "Missing required parameters. URL: " + (url == null ? "null" : "present") +
                    ", Token: " + (token == null ? "null" : "present"));
            return Result.failure();
        }

        try {
            // Kiểm tra xem file đã tồn tại và hợp lệ chưa
            String cacheFileName = getCacheFileName(url);
            File existingFile = new File(context.getFilesDir(), cacheFileName);
            Log.d(TAG, "Checking for existing file: " + existingFile.getAbsolutePath());

            if (existingFile.exists() && existingFile.length() > 0) {
                // File đã tồn tại và có vẻ hợp lệ
                Log.i(TAG, "File already exists! Path: " + existingFile.getAbsolutePath() +
                        ", Size: " + (existingFile.length() / 1024) + "KB");

                Data outputData = new Data.Builder()
                        .putString("path", existingFile.getAbsolutePath())
                        .build();

                Log.d(TAG, "Worker completed successfully with cached file");
                return Result.success(outputData);
            }

            // Thực hiện tải file
            Log.i(TAG, "Starting download from server: " + url.substring(0, Math.min(url.length(), 30)) + "...");
            long startTime = System.currentTimeMillis();

            File downloadedFile = MediaUtils.getMediaFromHost(context, url, token)
                    .get(60, TimeUnit.SECONDS);

            long endTime = System.currentTimeMillis();
            long downloadTime = endTime - startTime;

            if (downloadedFile != null && downloadedFile.exists()) {
                Log.i(TAG, "Download successful!" +
                        "\nPath: " + downloadedFile.getAbsolutePath() +
                        "\nSize: " + (downloadedFile.length() / 1024) + "KB" +
                        "\nTime taken: " + downloadTime + "ms");

                Data outputData = new Data.Builder()
                        .putString("path", downloadedFile.getAbsolutePath())
                        .build();

                Log.d(TAG, "Worker completed successfully with downloaded file");
                return Result.success(outputData);
            } else {
                Log.e(TAG, "Download completed but file is null or doesn't exist");
                return Result.failure();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error downloading avatar: " + e.getMessage(), e);
            // Log stack trace for debugging
            for (StackTraceElement element : e.getStackTrace()) {
                Log.e(TAG, "    at " + element.toString());
            }

            return Result.failure();
        }
    }

    private String getCacheFileName(String url) {
        // Tạo tên file từ URL
        try {
            Log.d(TAG, "Generating cache filename for URL: " + url.substring(0, Math.min(url.length(), 30)) + "...");

            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String fileName = "photo_" + hexString.toString();
            Log.d(TAG, "Generated filename: " + fileName);

            return fileName;
        } catch (NoSuchAlgorithmException e) {
            // Fallback nếu không có MD5
            String fileName = "photo_" + url.hashCode();
            Log.w(TAG, "MD5 unavailable, using fallback filename: " + fileName, e);
            return fileName;
        }
    }
}