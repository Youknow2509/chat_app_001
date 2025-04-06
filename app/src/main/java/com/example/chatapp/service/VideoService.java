package com.example.chatapp.service;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.chatapp.worker.VideoDownloadWorker;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Service quản lý tải và phát video trong ứng dụng
 */
public class VideoService {
    private static final String TAG = "VideoService";

    /**
     * Tải và phát video trong Fragment
     *
     * @param fragment Fragment hiện tại
     * @param playerView PlayerView để hiển thị video
     * @param localPath Đường dẫn video local
     * @param remoteUrl URL video từ server
     * @param token Token xác thực (nếu cần)
     * @param autoPlay Tự động phát video sau khi tải
     * @param onVideoDownloaded Callback khi tải thành công
     */
    public static void loadAndPlayVideo(
            @NonNull Fragment fragment,
            @NonNull PlayerView playerView,
            @Nullable String localPath,
            @NonNull String remoteUrl,
            @Nullable String token,
            boolean autoPlay,
            @Nullable Consumer<String> onVideoDownloaded) {

        Context context = fragment.requireContext();
        Context appContext = context.getApplicationContext();
        LifecycleOwner lifecycleOwner = fragment.getViewLifecycleOwner();

        loadAndPlayVideoInternal(
                fragment, playerView, localPath, remoteUrl, token,
                lifecycleOwner, appContext, autoPlay, onVideoDownloaded
        );
    }

    /**
     * Tải và phát video trong Activity
     *
     * @param activity Activity hiện tại
     * @param playerView PlayerView để hiển thị video
     * @param localPath Đường dẫn video local
     * @param remoteUrl URL video từ server
     * @param token Token xác thực (nếu cần)
     * @param lifecycleOwner LifecycleOwner để lắng nghe kết quả
     * @param autoPlay Tự động phát video sau khi tải
     * @param onVideoDownloaded Callback khi tải thành công
     */
    public static void loadAndPlayVideo(
            @NonNull Activity activity,
            @NonNull PlayerView playerView,
            @Nullable String localPath,
            @NonNull String remoteUrl,
            @Nullable String token,
            @NonNull LifecycleOwner lifecycleOwner,
            boolean autoPlay,
            @Nullable Consumer<String> onVideoDownloaded) {

        Context appContext = activity.getApplicationContext();

        loadAndPlayVideoInternal(
                activity, playerView, localPath, remoteUrl, token,
                lifecycleOwner, appContext, autoPlay, onVideoDownloaded
        );
    }

    /**
     * Phương thức nội bộ để xử lý logic chung cho cả Fragment và Activity
     */
    private static void loadAndPlayVideoInternal(
            @NonNull Object context,
            @NonNull PlayerView playerView,
            @Nullable String localPath,
            @NonNull String remoteUrl,
            @Nullable String token,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Context appContext,
            boolean autoPlay,
            @Nullable Consumer<String> onVideoDownloaded) {

        // Lưu weak reference để tránh leak
        final WeakReference<Object> contextRef = new WeakReference<>(context);
        final WeakReference<PlayerView> playerViewRef = new WeakReference<>(playerView);

        // Kiểm tra nếu có file local
        boolean hasLocalFile = localPath != null && new File(localPath).exists();

        // Tạo ExoPlayer
        ExoPlayer player = new ExoPlayer.Builder(appContext).build();
        playerView.setPlayer(player);

        if (hasLocalFile) {
            // Phát từ file local
            MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(localPath)));
            player.setMediaItem(mediaItem);
            player.prepare();
            if (autoPlay) {
                player.play();
            }
            Log.d(TAG, "Playing video from local: " + localPath);
        } else {
            // Nếu không có file local, tải từ remote và phát
            if (isValidUrl(remoteUrl)) {
                // Phát trực tiếp từ URL (streaming)
                MediaItem mediaItem = MediaItem.fromUri(remoteUrl);
                player.setMediaItem(mediaItem);
                player.prepare();
                if (autoPlay) {
                    player.play();
                }
                Log.d(TAG, "Streaming video from remote: " + remoteUrl);
            }

            // Đồng thời tải trong background để lưu local
            downloadVideoInBackground(
                    lifecycleOwner,
                    appContext,
                    remoteUrl,
                    token,
                    (newPath) -> {
                        // Gọi callback với đường dẫn mới
                        if (onVideoDownloaded != null) {
                            onVideoDownloaded.accept(newPath);
                        }

                        // Cập nhật player nếu đang phát streaming
                        if (!hasLocalFile) {
                            refreshPlayerIfAvailable(contextRef, playerViewRef, newPath, player.isPlaying());
                        }
                    }
            );
        }

        // Thêm các listener cho lifecycle để release player khi không cần
        addLifecycleListeners(context, player, lifecycleOwner);
    }

    /**
     * Kiểm tra xem URL có hợp lệ không
     */
    private static boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /**
     * Cập nhật player với file mới tải nếu context vẫn còn
     */
    private static void refreshPlayerIfAvailable(
            WeakReference<Object> contextRef,
            WeakReference<PlayerView> playerViewRef,
            String videoPath,
            boolean wasPlaying) {

        Object context = contextRef.get();
        PlayerView playerView = playerViewRef.get();

        if (context == null || playerView == null) {
            Log.d(TAG, "Context or PlayerView no longer available");
            return;
        }

        Runnable updatePlayer = () -> {
            ExoPlayer player = (ExoPlayer) playerView.getPlayer();
            if (player != null) {
                // Lưu vị trí hiện tại
                long currentPosition = player.getCurrentPosition();
                boolean playWhenReady = player.getPlayWhenReady();

                // Thay đổi nguồn sang file local
                MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(videoPath)));
                player.setMediaItem(mediaItem);
                player.prepare();
                player.seekTo(currentPosition);
                player.setPlayWhenReady(playWhenReady);

                Log.d(TAG, "Updated player to use local file: " + videoPath);
            }
        };

        if (context instanceof Fragment) {
            Fragment fragment = (Fragment) context;
            if (!fragment.isAdded()) return;

            fragment.requireActivity().runOnUiThread(() -> {
                if (fragment.isAdded()) {
                    updatePlayer.run();
                }
            });
        } else if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) return;

            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    updatePlayer.run();
                }
            });
        }
    }

    /**
     * Thêm các listener để giải phóng player khi không cần
     */
    private static void addLifecycleListeners(Object context, ExoPlayer player, LifecycleOwner lifecycleOwner) {
        // This would need implementation based on your lifecycle architecture
        // For simplicity, we'll just rely on explicit release calls
    }

    /**
     * Tải video trong background
     */
    public static void downloadVideoInBackground(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Context context,
            @NonNull String remoteUrl,
            @Nullable String token,
            @Nullable Consumer<String> onComplete) {

        Log.d(TAG, "Scheduling video download for URL: " + remoteUrl);

        // Tạo input data cho worker
        Data inputData = new Data.Builder()
                .putString("url", remoteUrl)
                .putString("token", token != null ? token : "")
                .putString("type", "video")
                .build();

        // Tạo Work Request
        OneTimeWorkRequest downloadWork = new OneTimeWorkRequest.Builder(VideoDownloadWorker.class)
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag("video_download")
                .build();

        // Lên lịch công việc với unique ID
        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        "video_download_" + remoteUrl.hashCode(),
                        ExistingWorkPolicy.REPLACE,
                        downloadWork
                );

        // Lắng nghe kết quả
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(downloadWork.getId())
                .observe(lifecycleOwner, workInfo -> {
                    if (workInfo == null) return;

                    Log.d(TAG, "WorkInfo state changed: " + workInfo.getState().name());

                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        String path = workInfo.getOutputData().getString("path");
                        Log.d(TAG, "Video downloaded successfully to: " + path);

                        if (path != null && onComplete != null) {
                            onComplete.accept(path);
                        }
                    } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                        Log.e(TAG, "Video download work failed");
                    }
                });
    }

    /**
     * Giải phóng player
     */
    public static void releasePlayer(PlayerView playerView) {
        if (playerView != null && playerView.getPlayer() != null) {
            playerView.getPlayer().release();
            playerView.setPlayer(null);
        }
    }
}