package com.example.chatapp.service;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.chatapp.worker.MediaWorkManager;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Service quản lý tải và phát video trong ứng dụng
 */
public class VideoService {
    private static final String TAG = "VideoService";

    // Lưu trữ player theo PlayerView để tái sử dụng và quản lý đúng cách
    private static final Map<PlayerView, ExoPlayer> playerMap = new HashMap<>();

    /**
     * Tải và phát video trong Fragment
     *
     * @param fragment          Fragment hiện tại
     * @param playerView        PlayerView để hiển thị video
     * @param localPath         Đường dẫn video local
     * @param remoteUrl         URL video từ server
     * @param token             Token xác thực (nếu cần)
     * @param autoPlay          Tự động phát video sau khi tải
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
     * @param activity          Activity hiện tại
     * @param playerView        PlayerView để hiển thị video
     * @param localPath         Đường dẫn video local
     * @param remoteUrl         URL video từ server
     * @param token             Token xác thực (nếu cần)
     * @param lifecycleOwner    LifecycleOwner để lắng nghe kết quả
     * @param autoPlay          Tự động phát video sau khi tải
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

        // Tạo hoặc tái sử dụng ExoPlayer
        ExoPlayer player = getOrCreatePlayer(appContext, playerView);
        player.clearMediaItems();

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

                // Thêm error listener để xử lý các lỗi streaming
                player.addListener(new Player.Listener() {
                    @Override
                    public void onPlayerError(PlaybackException error) {
                        Log.e(TAG, "Player error: " + error.getMessage());
                        // Có thể xử lý fallback ở đây nếu cần
                    }
                });
            }

            // Đồng thời tải trong background để lưu local qua MediaWorkManager
            MediaWorkManager.getInstance(appContext).downloadVideo(
                    lifecycleOwner,
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
     * Lấy player hiện có hoặc tạo mới
     */
    private static ExoPlayer getOrCreatePlayer(Context context, PlayerView playerView) {
        ExoPlayer player = playerMap.get(playerView);

        if (player == null) {
            player = new ExoPlayer.Builder(context).build();
            playerView.setPlayer(player);
            playerMap.put(playerView, player);
        }

        return player;
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
            ExoPlayer player = playerMap.get(playerView);
            if (player != null) {
                // Lưu vị trí hiện tại
                long currentPosition = player.getCurrentPosition();
                boolean playWhenReady = player.getPlayWhenReady();

                // Thay đổi nguồn sang file local
                MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(videoPath)));
                player.setMediaItem(mediaItem, currentPosition);
                player.prepare();
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
        // Theo dõi lifecycle và giải phóng tài nguyên khi cần
        if (context instanceof Fragment) {
            Fragment fragment = (Fragment) context;
            fragment.getViewLifecycleOwnerLiveData().observe(fragment, viewLifecycleOwner -> {
                if (viewLifecycleOwner == null) {
                    // Fragment's view is destroyed
                    player.release();
                    PlayerView playerView = null;
                    for (Map.Entry<PlayerView, ExoPlayer> entry : playerMap.entrySet()) {
                        if (entry.getValue() == player) {
                            playerView = entry.getKey();
                            break;
                        }
                    }
                    if (playerView != null) {
                        playerMap.remove(playerView);
                    }
                }
            });
        }
    }

    /**
     * Tải video trong background (không hiển thị)
     */
    public static void downloadVideoInBackground(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Context context,
            @NonNull String remoteUrl,
            @Nullable String token,
            @Nullable Consumer<String> onComplete) {

        Log.d(TAG, "Scheduling video download for URL: " + remoteUrl);

        // Sử dụng MediaWorkManager để tải video
        MediaWorkManager.getInstance(context).downloadVideo(
                lifecycleOwner,
                remoteUrl,
                token,
                onComplete
        );
    }

    /**
     * Giải phóng một player cụ thể
     */
    public static void releasePlayer(PlayerView playerView) {
        ExoPlayer player = playerMap.get(playerView);
        if (player != null) {
            player.release();
            playerMap.remove(playerView);
            playerView.setPlayer(null);
        }
    }

    /**
     * Giải phóng tất cả player
     */
    public static void releaseAllPlayers() {
        for (Map.Entry<PlayerView, ExoPlayer> entry : playerMap.entrySet()) {
            ExoPlayer player = entry.getValue();
            PlayerView playerView = entry.getKey();

            if (player != null) {
                player.release();
                playerView.setPlayer(null);
            }
        }
        playerMap.clear();
    }

    /**
     * Tạm dừng tất cả player (ví dụ: khi ứng dụng đi vào background)
     */
    public static void pauseAllPlayers() {
        for (ExoPlayer player : playerMap.values()) {
            if (player != null && player.isPlaying()) {
                player.pause();
            }
        }
    }

    /**
     * Kiểm tra xem có player nào đang phát không
     */
    public static boolean isAnyPlayerPlaying() {
        for (ExoPlayer player : playerMap.values()) {
            if (player != null && player.isPlaying()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lấy player đang phát (hữu ích để hiển thị thông tin bài hiện tại)
     */
    @Nullable
    public static ExoPlayer getCurrentlyPlayingPlayer() {
        for (ExoPlayer player : playerMap.values()) {
            if (player != null && player.isPlaying()) {
                return player;
            }
        }
        return null;
    }
}