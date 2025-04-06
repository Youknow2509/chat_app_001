package com.example.chatapp.service;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.chatapp.worker.MediaWorkManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Service quản lý ghi âm và phát lại âm thanh
 */
public class AudioRecordService {
    private static final String TAG = "AudioRecordService";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    // Quản lý nhiều MediaPlayer cho các file khác nhau
    private static final Map<String, AudioPlayback> audioPlayers = new HashMap<>();
    private static MediaRecorder mediaRecorder;
    private static String currentRecordingPath;
    private static boolean isRecording = false;
    // Timer để cập nhật thời gian ghi âm
    private static Timer recordingTimer;
    private static long recordingDuration = 0; // ms
    private static Consumer<Long> durationCallback;

    /**
     * Kiểm tra và yêu cầu quyền ghi âm
     *
     * @param activity Activity hiện tại để yêu cầu quyền
     * @return true nếu đã có quyền, false nếu đang yêu cầu
     */
    public static boolean checkRecordPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
            return false;
        }
        return true;
    }

    /**
     * Bắt đầu ghi âm
     *
     * @param context          Context để tạo file
     * @param onStarted        Callback khi bắt đầu ghi âm
     * @param onDurationUpdate Callback để cập nhật thời gian ghi âm (ms)
     * @return đường dẫn file ghi âm
     */
    public static String startRecording(
            Context context,
            @Nullable Runnable onStarted,
            @Nullable Consumer<Long> onDurationUpdate) {

        if (isRecording) {
            Log.w(TAG, "Already recording");
            return currentRecordingPath;
        }

        stopAllPlayback(); // Dừng tất cả phát lại

        // Tạo đường dẫn file
        File audioDir = new File(context.getFilesDir(), "audio");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String audioFileName = "AUDIO_" + timeStamp + ".mp3";
        File audioFile = new File(audioDir, audioFileName);

        currentRecordingPath = audioFile.getAbsolutePath();

        try {
            releaseRecorder();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            // Cấu hình tốt hơn cho các phiên bản Android khác nhau
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
                mediaRecorder.setAudioChannels(1); // mono
                mediaRecorder.setAudioEncodingBitRate(128000);
                mediaRecorder.setAudioSamplingRate(44100);
            } else {
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setAudioEncodingBitRate(96000);
                mediaRecorder.setAudioSamplingRate(44100);
            }

            mediaRecorder.setOutputFile(currentRecordingPath);

            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;

            // Cài đặt timer để cập nhật thời gian
            recordingDuration = 0;
            durationCallback = onDurationUpdate;

            if (durationCallback != null) {
                startRecordingTimer();
            }

            Log.d(TAG, "Started recording to: " + currentRecordingPath);

            if (onStarted != null) {
                onStarted.run();
            }

            return currentRecordingPath;
        } catch (IOException e) {
            Log.e(TAG, "Failed to start recording", e);
            releaseRecorder();
            return null;
        }
    }

    /**
     * Bắt đầu timer để theo dõi thời gian ghi âm
     */
    private static void startRecordingTimer() {
        if (recordingTimer != null) {
            recordingTimer.cancel();
        }

        recordingTimer = new Timer();
        recordingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                recordingDuration += 100; // 100ms update interval

                if (durationCallback != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        durationCallback.accept(recordingDuration);
                    });
                }
            }
        }, 0, 100);
    }

    /**
     * Dừng ghi âm
     *
     * @param onStopped Callback khi dừng ghi âm (với đường dẫn file)
     * @return đường dẫn file ghi âm hoặc null nếu không thành công
     */
    public static String stopRecording(@Nullable Consumer<String> onStopped) {
        if (!isRecording || mediaRecorder == null) {
            Log.w(TAG, "Not recording");
            return null;
        }

        // Dừng timer
        if (recordingTimer != null) {
            recordingTimer.cancel();
            recordingTimer = null;
        }

        try {
            mediaRecorder.stop();
            Log.d(TAG, "Stopped recording. File saved at: " + currentRecordingPath);
        } catch (RuntimeException e) {
            // Xảy ra khi thời gian ghi quá ngắn
            Log.e(TAG, "Error stopping recorder - likely too short", e);
            // Xóa file không hoàn chỉnh
            File incompleteFile = new File(currentRecordingPath);
            if (incompleteFile.exists()) {
                incompleteFile.delete();
            }
            currentRecordingPath = null;
        } finally {
            releaseRecorder();
        }

        if (currentRecordingPath != null && onStopped != null) {
            onStopped.accept(currentRecordingPath);
        }

        return currentRecordingPath;
    }

    /**
     * Tạm dừng ghi âm (chỉ được hỗ trợ từ Android 8.0+)
     *
     * @return true nếu tạm dừng thành công
     */
    public static boolean pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.pause();

                // Dừng timer
                if (recordingTimer != null) {
                    recordingTimer.cancel();
                    recordingTimer = null;
                }

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error pausing recording", e);
            }
        }
        return false;
    }

    /**
     * Tiếp tục ghi âm (chỉ được hỗ trợ từ Android 8.0+)
     *
     * @return true nếu tiếp tục thành công
     */
    public static boolean resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.resume();

                // Khởi động lại timer
                if (durationCallback != null) {
                    startRecordingTimer();
                }

                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error resuming recording", e);
            }
        }
        return false;
    }

    /**
     * Phát audio
     *
     * @param context          Context
     * @param filePath         Đường dẫn file âm thanh
     * @param onCompletion     Callback khi phát xong
     * @param onProgressUpdate Callback cập nhật tiến trình (phần trăm)
     */
    public static void playAudio(
            Context context,
            String filePath,
            @Nullable Runnable onCompletion,
            @Nullable Consumer<Integer> onProgressUpdate) {

        if (isRecording) {
            stopRecording(null);
        }

        if (filePath == null || !new File(filePath).exists()) {
            Log.e(TAG, "Audio file does not exist: " + filePath);
            return;
        }

        // Dừng nếu file này đang phát
        AudioPlayback existingPlayback = audioPlayers.get(filePath);
        if (existingPlayback != null && existingPlayback.isPlaying) {
            stopPlayback(filePath);
            return;
        }

        try {
            // Dừng tất cả các file khác đang phát
            stopAllPlayback();

            MediaPlayer player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());

            player.setDataSource(context, Uri.fromFile(new File(filePath)));
            player.prepare();
            player.start();

            AudioPlayback playback = new AudioPlayback(player);
            playback.isPlaying = true;
            playback.completionCallback = onCompletion;
            playback.progressCallback = onProgressUpdate;

            audioPlayers.put(filePath, playback);

            Log.d(TAG, "Started playing: " + filePath);

            // Lắng nghe khi phát xong
            player.setOnCompletionListener(mp -> {
                Log.d(TAG, "Playback completed");

                AudioPlayback pb = audioPlayers.get(filePath);
                if (pb != null) {
                    pb.isPlaying = false;

                    if (pb.progressTimer != null) {
                        pb.progressTimer.cancel();
                        pb.progressTimer = null;
                    }

                    if (pb.completionCallback != null) {
                        pb.completionCallback.run();
                    }
                }
            });

            // Cập nhật tiến trình nếu cần
            if (onProgressUpdate != null) {
                int duration = player.getDuration();
                playback.progressTimer = new Timer();
                playback.progressTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (player != null && player.isPlaying()) {
                            int currentPosition = player.getCurrentPosition();
                            int progressPercent = (currentPosition * 100) / duration;

                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (playback.progressCallback != null) {
                                    playback.progressCallback.accept(progressPercent);
                                }
                            });
                        }
                    }
                }, 0, 100);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error playing audio", e);
            releasePlayer(filePath);
        }
    }

    /**
     * Dừng phát một file cụ thể
     */
    public static void stopPlayback(String filePath) {
        AudioPlayback playback = audioPlayers.get(filePath);
        if (playback != null) {
            playback.release();
            audioPlayers.remove(filePath);
            Log.d(TAG, "Stopped playback for: " + filePath);
        }
    }

    /**
     * Dừng tất cả đang phát
     */
    public static void stopAllPlayback() {
        for (Map.Entry<String, AudioPlayback> entry : audioPlayers.entrySet()) {
            entry.getValue().release();
            Log.d(TAG, "Stopped playback for: " + entry.getKey());
        }
        audioPlayers.clear();
    }

    /**
     * Tải lên file ghi âm lên server
     *
     * @param lifecycleOwner LifecycleOwner để lắng nghe kết quả
     * @param context        Context ứng dụng
     * @param filePath       Đường dẫn file âm thanh
     * @param token          Token xác thực (nếu cần)
     * @param onComplete     Callback khi tải lên thành công (url từ server)
     * @param onProgress     Callback cập nhật tiến trình tải lên
     */
    public static void uploadAudioFile(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Context context,
            @NonNull String filePath,
            @Nullable String token,
            @Nullable Consumer<String> onComplete,
            @Nullable Consumer<Integer> onProgress) {

        Log.d(TAG, "Scheduling audio upload for file: " + filePath);

        // Sử dụng MediaWorkManager để tải lên audio
        MediaWorkManager.getInstance(context).uploadAudio(
                lifecycleOwner,
                filePath,
                token,
                onComplete
        );

        // Tiến trình tải lên được quản lý bởi MediaWorkManager
        // Bạn có thể mở rộng để hỗ trợ cập nhật tiến trình
    }

    /**
     * Lấy thời lượng của file audio (ms)
     *
     * @param context  Context
     * @param filePath Đường dẫn file
     * @return thời lượng tính bằng ms, hoặc 0 nếu không xác định được
     */
    public static long getAudioDuration(Context context, String filePath) throws IOException {
        if (filePath == null || !new File(filePath).exists()) {
            return 0;
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, Uri.fromFile(new File(filePath)));
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return durationStr != null ? Long.parseLong(durationStr) : 0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting audio duration", e);
            return 0;
        } finally {
            retriever.release();
        }
    }

    /**
     * Chuyển đổi thời lượng ms thành chuỗi định dạng "mm:ss"
     */
    public static String formatDuration(long durationMs) {
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(durationMs),
                TimeUnit.MILLISECONDS.toSeconds(durationMs) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMs))
        );
    }

    /**
     * Kiểm tra xem có đang ghi âm không
     */
    public static boolean isRecording() {
        return isRecording;
    }

    /**
     * Kiểm tra file có đang phát không
     */
    public static boolean isPlaying(String filePath) {
        AudioPlayback playback = audioPlayers.get(filePath);
        return playback != null && playback.isPlaying;
    }

    /**
     * Kiểm tra có bất kỳ file nào đang phát không
     */
    public static boolean isAnyPlaying() {
        for (AudioPlayback playback : audioPlayers.values()) {
            if (playback.isPlaying) {
                return true;
            }
        }
        return false;
    }

    /**
     * Giải phóng MediaRecorder
     */
    private static void releaseRecorder() {
        if (recordingTimer != null) {
            recordingTimer.cancel();
            recordingTimer = null;
        }

        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaRecorder", e);
            } finally {
                mediaRecorder = null;
                isRecording = false;
            }
        }
    }

    /**
     * Giải phóng MediaPlayer cho một file
     */
    private static void releasePlayer(String filePath) {
        AudioPlayback playback = audioPlayers.get(filePath);
        if (playback != null) {
            playback.release();
            audioPlayers.remove(filePath);
        }
    }

    /**
     * Giải phóng tất cả tài nguyên
     */
    public static void releaseAll() {
        releaseRecorder();
        stopAllPlayback();
    }

    /**
     * Tạm dừng tất cả media khi ứng dụng vào background
     */
    public static void pauseAll() {
        // Tạm dừng ghi âm nếu hỗ trợ
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isRecording) {
            pauseRecording();
        }

        // Tạm dừng phát lại
        for (Map.Entry<String, AudioPlayback> entry : audioPlayers.entrySet()) {
            AudioPlayback playback = entry.getValue();
            if (playback.isPlaying && playback.player != null) {
                playback.player.pause();
            }
        }
    }

    /**
     * Tiếp tục phát lại khi ứng dụng trở lại foreground
     */
    public static void resumeAll() {
        // Tiếp tục ghi âm nếu đang tạm dừng
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isRecording) {
            resumeRecording();
        }

        // Tiếp tục phát lại
        for (Map.Entry<String, AudioPlayback> entry : audioPlayers.entrySet()) {
            AudioPlayback playback = entry.getValue();
            if (playback.isPlaying && playback.player != null) {
                playback.player.start();
            }
        }
    }

    /**
     * Lớp nội bộ để quản lý playback cho một file audio
     */
    private static class AudioPlayback {
        MediaPlayer player;
        boolean isPlaying;
        Timer progressTimer;
        Consumer<Integer> progressCallback;
        Runnable completionCallback;

        AudioPlayback(MediaPlayer player) {
            this.player = player;
            this.isPlaying = false;
        }

        void release() {
            if (progressTimer != null) {
                progressTimer.cancel();
                progressTimer = null;
            }

            if (player != null) {
                if (player.isPlaying()) {
                    player.stop();
                }
                player.release();
                player = null;
            }

            isPlaying = false;
            progressCallback = null;
            completionCallback = null;
        }
    }
}