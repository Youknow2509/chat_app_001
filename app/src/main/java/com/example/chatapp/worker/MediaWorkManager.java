package com.example.chatapp.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.chatapp.network.NetworkMonitor;
import com.example.chatapp.worker.AudioUploadWorker;
import com.example.chatapp.worker.PhotoDownloadWorker;
import com.example.chatapp.worker.VideoDownloadWorker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Quản lý trung tâm cho tất cả các tác vụ media cần network
 * - Theo dõi kết nối mạng và tự động điều chỉnh các công việc
 * - Tự động lên lịch lại các tác vụ đã thất bại khi có mạng
 * - Cung cấp thông tin trạng thái cho tất cả các công việc
 */
public class MediaWorkManager implements NetworkMonitor.NetworkStateListener {
    private static final String TAG = "MediaWorkManager";
    private static MediaWorkManager instance;

    private final Context context;
    private final NetworkMonitor networkMonitor;
    private final WorkManager workManager;

    // Lưu trữ các tác vụ đang chờ xử lý
    private final Map<String, PendingMediaTask> pendingTasks = new HashMap<>();

    /**
     * Tác vụ media đang chờ xử lý
     */
    private static class PendingMediaTask {
        WorkRequest workRequest;
        ExistingWorkPolicy policy;
        String workName;
        Consumer<String> callback;
        LifecycleOwner lifecycleOwner;

        PendingMediaTask(WorkRequest workRequest, ExistingWorkPolicy policy,
                         String workName, Consumer<String> callback,
                         LifecycleOwner lifecycleOwner) {
            this.workRequest = workRequest;
            this.policy = policy;
            this.workName = workName;
            this.callback = callback;
            this.lifecycleOwner = lifecycleOwner;
        }
    }

    private MediaWorkManager(Context context) {
        this.context = context.getApplicationContext();
        this.networkMonitor = NetworkMonitor.getInstance(context);
        this.workManager = WorkManager.getInstance(context);

        // Đăng ký lắng nghe sự thay đổi mạng
        networkMonitor.addListener(this);
    }

    public static synchronized MediaWorkManager getInstance(Context context) {
        if (instance == null) {
            instance = new MediaWorkManager(context);
        }
        return instance;
    }

    /**
     * Xử lý khi trạng thái mạng thay đổi
     */
    @Override
    public void onNetworkStateChanged(boolean isAvailable) {
        if (isAvailable) {
            Log.d(TAG, "Network available: scheduling pending tasks");
            processPendingTasks();
        } else {
            Log.d(TAG, "Network unavailable: tasks will be queued");
        }
    }

    /**
     * Tải ảnh từ mạng và lưu vào bộ nhớ
     */
    public void downloadImage(
            LifecycleOwner lifecycleOwner,
            String imageUrl,
            String token,
            Consumer<String> onComplete) {

        scheduleWork(
                createPhotoDownloadRequest(imageUrl, token),
                ExistingWorkPolicy.REPLACE,
                "image_download_" + imageUrl.hashCode(),
                onComplete,
                lifecycleOwner
        );
    }

    /**
     * Tải video từ mạng và lưu vào bộ nhớ
     */
    public void downloadVideo(
            LifecycleOwner lifecycleOwner,
            String videoUrl,
            String token,
            Consumer<String> onComplete) {

        scheduleWork(
                createVideoDownloadRequest(videoUrl, token),
                ExistingWorkPolicy.REPLACE,
                "video_download_" + videoUrl.hashCode(),
                onComplete,
                lifecycleOwner
        );
    }

    /**
     * Tải lên file ghi âm lên server
     */
    public void uploadAudio(
            LifecycleOwner lifecycleOwner,
            String audioFilePath,
            String token,
            Consumer<String> onComplete) {

        scheduleWork(
                createAudioUploadRequest(audioFilePath, token),
                ExistingWorkPolicy.REPLACE,
                "audio_upload_" + audioFilePath.hashCode(),
                onComplete,
                lifecycleOwner
        );
    }

    /**
     * Lập lịch tác vụ với kiểm tra mạng thông minh
     */
    private void scheduleWork(
            WorkRequest workRequest,
            ExistingWorkPolicy policy,
            String workName,
            Consumer<String> callback,
            LifecycleOwner lifecycleOwner) {

        if (networkMonitor.isNetworkAvailable()) {
            // Nếu có mạng, thực hiện ngay
            executeWork(workRequest, policy, workName, callback, lifecycleOwner);
        } else {
            // Nếu không có mạng, lưu vào danh sách chờ
            Log.d(TAG, "Network unavailable, queuing task: " + workName);
            pendingTasks.put(workName, new PendingMediaTask(
                    workRequest, policy, workName, callback, lifecycleOwner));
        }
    }

    /**
     * Thực thi tác vụ và theo dõi kết quả
     */
    private void executeWork(
            WorkRequest workRequest,
            ExistingWorkPolicy policy,
            String workName,
            Consumer<String> callback,
            LifecycleOwner lifecycleOwner) {

        Log.d(TAG, "Scheduling work: " + workName);

        // Lên lịch tác vụ
        if (workRequest instanceof OneTimeWorkRequest) {
            workManager.enqueueUniqueWork(
                    workName,
                    policy,
                    (OneTimeWorkRequest) workRequest
            );

            // Lắng nghe kết quả
            workManager.getWorkInfoByIdLiveData(workRequest.getId())
                    .observe(lifecycleOwner, workInfo -> {
                        if (workInfo != null) {
                            Log.d(TAG, "WorkInfo state changed: " + workInfo.getState().name() + " for " + workName);

                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                String result = null;

                                // Xác định loại kết quả dựa trên loại worker
                                if (workName.startsWith("image_download_") ||
                                        workName.startsWith("video_download_")) {
                                    result = workInfo.getOutputData().getString("path");
                                } else if (workName.startsWith("audio_upload_")) {
                                    result = workInfo.getOutputData().getString("serverUrl");
                                }

                                Log.d(TAG, "Work completed successfully: " + workName + ", result: " + result);

                                // Phản hồi kết quả
                                if (result != null && callback != null) {
                                    callback.accept(result);
                                }

                                // Xóa khỏi danh sách chờ nếu có
                                pendingTasks.remove(workName);

                            } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                                Log.e(TAG, "Work failed: " + workName);

                                // Nếu thất bại do mạng, thêm vào hàng đợi chờ
                                if (!networkMonitor.isNetworkAvailable()) {
                                    Log.d(TAG, "Network unavailable, work will be retried when network returns");
                                    pendingTasks.put(workName, new PendingMediaTask(
                                            workRequest, policy, workName, callback, lifecycleOwner));
                                }
                            }
                        }
                    });
        } else {
            // Xử lý cho PeriodicWorkRequest nếu cần
            Log.d(TAG, "PeriodicWorkRequest không được hỗ trợ trong quản lý này");
        }
    }

    /**
     * Xử lý các tác vụ đang chờ khi có mạng trở lại
     */
    private void processPendingTasks() {
        Log.d(TAG, "Processing " + pendingTasks.size() + " pending tasks");

        List<String> processedTasks = new ArrayList<>();

        for (Map.Entry<String, PendingMediaTask> entry : pendingTasks.entrySet()) {
            PendingMediaTask task = entry.getValue();

            // Kiểm tra xem LifecycleOwner còn tồn tại không
            if (task.lifecycleOwner == null || !isLifecycleValid(task.lifecycleOwner)) {
                Log.d(TAG, "Skipping task with invalid lifecycle: " + task.workName);
                processedTasks.add(entry.getKey());
                continue;
            }

            // Thực thi tác vụ
            executeWork(
                    task.workRequest,
                    task.policy,
                    task.workName,
                    task.callback,
                    task.lifecycleOwner
            );

            processedTasks.add(entry.getKey());
        }

        // Xóa các tác vụ đã xử lý khỏi danh sách chờ
        for (String taskName : processedTasks) {
            pendingTasks.remove(taskName);
        }
    }

    /**
     * Kiểm tra LifecycleOwner còn hợp lệ không
     */
    private boolean isLifecycleValid(LifecycleOwner lifecycleOwner) {
        // Đơn giản chỉ kiểm tra null, có thể mở rộng để kiểm tra trạng thái
        return lifecycleOwner != null;
    }

    /**
     * Tạo yêu cầu tải ảnh
     */
    private OneTimeWorkRequest createPhotoDownloadRequest(String imageUrl, String token) {
        Data inputData = new Data.Builder()
                .putString("url", imageUrl)
                .putString("token", token != null ? token : "")
                .build();

        return new OneTimeWorkRequest.Builder(PhotoDownloadWorker.class)
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag("image_download")
                .build();
    }

    /**
     * Tạo yêu cầu tải video
     */
    private OneTimeWorkRequest createVideoDownloadRequest(String videoUrl, String token) {
        Data inputData = new Data.Builder()
                .putString("url", videoUrl)
                .putString("token", token != null ? token : "")
                .putString("type", "video")
                .build();

        return new OneTimeWorkRequest.Builder(VideoDownloadWorker.class)
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag("video_download")
                .build();
    }

    /**
     * Tạo yêu cầu tải lên audio
     */
    private OneTimeWorkRequest createAudioUploadRequest(String audioFilePath, String token) {
        Data inputData = new Data.Builder()
                .putString("filePath", audioFilePath)
                .putString("token", token != null ? token : "")
                .build();

        return new OneTimeWorkRequest.Builder(AudioUploadWorker.class)
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag("audio_upload")
                .build();
    }

    /**
     * Hủy tác vụ
     */
    public void cancelWorkById(String workName) {
        workManager.cancelUniqueWork(workName);
        pendingTasks.remove(workName);
    }

    /**
     * Hủy tất cả tác vụ
     */
    public void cancelAllWork() {
        workManager.cancelAllWork();
        pendingTasks.clear();
    }

    /**
     * Giải phóng tài nguyên
     */
    public void release() {
        networkMonitor.removeListener(this);
    }
}