package com.example.chatapp.service;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import com.example.chatapp.worker.AudioUploadWorker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Service quản lý ghi âm và phát lại
 */
public class AudioRecordService {
    private static final String TAG = "AudioRecordService";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;
    private static String currentRecordingPath;
    private static boolean isRecording = false;
    private static boolean isPlaying = false;

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
     * @param context Context để tạo file
     * @param onStarted Callback khi bắt đầu ghi âm
     * @return đường dẫn file ghi âm
     */
    public static String startRecording(Context context, @Nullable Runnable onStarted) {
        if (isRecording) {
            Log.w(TAG, "Already recording");
            return currentRecordingPath;
        }

        stopPlaying(); // Dừng phát nếu đang phát

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
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(currentRecordingPath);

            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;

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
     * Phát audio
     *
     * @param context Context
     * @param filePath Đường dẫn file âm thanh
     * @param onCompletion Callback khi phát xong
     */
    public static void playAudio(Context context, String filePath, @Nullable Runnable onCompletion) {
        if (isPlaying) {
            stopPlaying();
        }

        if (isRecording) {
            stopRecording(null);
        }

        if (filePath == null || !new File(filePath).exists()) {
            Log.e(TAG, "Audio file does not exist: " + filePath);
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, Uri.fromFile(new File(filePath)));
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;

            Log.d(TAG, "Started playing: " + filePath);

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                if (onCompletion != null) {
                    onCompletion.run();
                }
                Log.d(TAG, "Playback completed");
            });
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio", e);
            releasePlayer();
        }
    }

    /**
     * Dừng phát
     */
    public static void stopPlaying() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.stop();
            releasePlayer();
            isPlaying = false;
            Log.d(TAG, "Stopped playback");
        }
    }

    /**
     * Tải lên file ghi âm lên server
     *
     * @param lifecycleOwner LifecycleOwner để lắng nghe kết quả
     * @param context Context ứng dụng
     * @param filePath Đường dẫn file âm thanh
     * @param token Token xác thực (nếu cần)
     * @param onComplete Callback khi tải lên thành công (url từ server)
     */
    public static void uploadAudioFile(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Context context,
            @NonNull String filePath,
            @Nullable String token,
            @Nullable Consumer<String> onComplete) {

        Log.d(TAG, "Scheduling audio upload for file: " + filePath);

        // Tạo input data cho worker
        Data inputData = new Data.Builder()
                .putString("filePath", filePath)
                .putString("token", token != null ? token : "")
                .build();

        // Tạo Work Request
        OneTimeWorkRequest uploadWork = new OneTimeWorkRequest.Builder(AudioUploadWorker.class)
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag("audio_upload")
                .build();

        // Lên lịch công việc với unique ID
        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        "audio_upload_" + filePath.hashCode(),
                        ExistingWorkPolicy.REPLACE,
                        uploadWork
                );

        // Lắng nghe kết quả
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(uploadWork.getId())
                .observe(lifecycleOwner, workInfo -> {
                    if (workInfo == null) return;

                    Log.d(TAG, "WorkInfo state changed: " + workInfo.getState().name());

                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        String serverUrl = workInfo.getOutputData().getString("serverUrl");
                        Log.d(TAG, "Audio uploaded successfully. Server URL: " + serverUrl);

                        if (serverUrl != null && onComplete != null) {
                            onComplete.accept(serverUrl);
                        }
                    } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                        Log.e(TAG, "Audio upload work failed");
                    }
                });
    }

    /**
     * Giải phóng MediaRecorder
     */
    private static void releaseRecorder() {
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
     * Giải phóng MediaPlayer
     */
    private static void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaPlayer", e);
            } finally {
                mediaPlayer = null;
                isPlaying = false;
            }
        }
    }

    /**
     * Giải phóng tất cả tài nguyên
     */
    public static void releaseAll() {
        releaseRecorder();
        releasePlayer();
    }

    /**
     * Kiểm tra trạng thái ghi âm
     */
    public static boolean isRecording() {
        return isRecording;
    }

    /**
     * Kiểm tra trạng thái phát
     */
    public static boolean isPlaying() {
        return isPlaying;
    }
}