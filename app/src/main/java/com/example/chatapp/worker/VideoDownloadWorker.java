package com.example.chatapp.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Worker để tải video trong background
 */
public class VideoDownloadWorker extends Worker {
    private static final String TAG = "VideoDownloadWorker";

    public VideoDownloadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String videoUrl = getInputData().getString("url");
        String token = getInputData().getString("token");

        if (videoUrl == null) {
            Log.e(TAG, "Video URL is null");
            return Result.failure();
        }

        try {
            // Tạo thư mục nếu cần
            File videoDir = new File(getApplicationContext().getFilesDir(), "videos");
            if (!videoDir.exists()) {
                videoDir.mkdirs();
            }

            // Tạo tên file
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "VIDEO_" + timeStamp + ".mp4";
            File outputFile = new File(videoDir, fileName);

            // Tải file
            downloadFile(videoUrl, outputFile, token);

            // Kết quả
            Data outputData = new Data.Builder()
                    .putString("path", outputFile.getAbsolutePath())
                    .build();

            return Result.success(outputData);
        } catch (Exception e) {
            Log.e(TAG, "Error downloading video", e);
            return Result.failure();
        }
    }

    private void downloadFile(String fileUrl, File outputFile, String token) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Thêm token nếu có
        if (token != null && !token.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + token);
        }

        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned HTTP " + connection.getResponseCode()
                    + " " + connection.getResponseMessage());
        }

        // Tải file
        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            output.flush();
            Log.d(TAG, "Video downloaded to: " + outputFile.getAbsolutePath());
        } finally {
            connection.disconnect();
        }
    }
}