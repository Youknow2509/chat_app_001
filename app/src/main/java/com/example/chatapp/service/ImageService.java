package com.example.chatapp.service;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.chatapp.R;
import com.example.chatapp.worker.PhotoDownloadWorker;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Service quản lý tải và hiển thị ảnh trong ứng dụng
 */
public class ImageService {
    private static final String TAG = "ImageService";

    /**
     * Hiển thị ảnh vào ImageView trong Fragment với fallback tự động và tải về trong background
     *
     * @param fragment Fragment hiện tại, cần cho lifecycle
     * @param imageView ImageView để hiển thị ảnh
     * @param localPath Đường dẫn ảnh local
     * @param remoteUrl URL ảnh từ server
     * @param token Token xác thực (nếu cần)
     * @param onImageDownloaded Callback khi tải thành công (nhận đường dẫn local mới)
     */
    public static void loadAndCacheImage(
            @NonNull Fragment fragment,
            @NonNull ImageView imageView,
            @Nullable String localPath,
            @NonNull String remoteUrl,
            @Nullable String token,
            @Nullable Consumer<String> onImageDownloaded) {

        Context context = fragment.requireContext();
        Context appContext = context.getApplicationContext();
        LifecycleOwner lifecycleOwner = fragment.getViewLifecycleOwner();

        loadAndCacheImageInternal(
                fragment, imageView, localPath, remoteUrl, token,
                lifecycleOwner, appContext, onImageDownloaded
        );
    }

    /**
     * Hiển thị ảnh vào ImageView trong Activity với fallback tự động và tải về trong background
     *
     * @param activity Activity hiện tại, cần cho lifecycle
     * @param imageView ImageView để hiển thị ảnh
     * @param localPath Đường dẫn ảnh local
     * @param remoteUrl URL ảnh từ server
     * @param token Token xác thực (nếu cần)
     * @param lifecycleOwner LifecycleOwner để lắng nghe kết quả WorkManager
     * @param onImageDownloaded Callback khi tải thành công (nhận đường dẫn local mới)
     */
    public static void loadAndCacheImage(
            @NonNull Activity activity,
            @NonNull ImageView imageView,
            @Nullable String localPath,
            @NonNull String remoteUrl,
            @Nullable String token,
            @NonNull LifecycleOwner lifecycleOwner,
            @Nullable Consumer<String> onImageDownloaded) {

        Context appContext = activity.getApplicationContext();

        loadAndCacheImageInternal(
                activity, imageView, localPath, remoteUrl, token,
                lifecycleOwner, appContext, onImageDownloaded
        );
    }

    /**
     * Phương thức nội bộ để xử lý logic chung cho cả Fragment và Activity
     */
    private static void loadAndCacheImageInternal(
            @NonNull Object context,
            @NonNull ImageView imageView,
            @Nullable String localPath,
            @NonNull String remoteUrl,
            @Nullable String token,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Context appContext,
            @Nullable Consumer<String> onImageDownloaded) {

        // Lưu weak reference để tránh leak
        final WeakReference<Object> contextRef = new WeakReference<>(context);
        final WeakReference<ImageView> imageViewRef = new WeakReference<>(imageView);

        // Hiển thị ảnh từ local hoặc remote với fallback
        Glide.with(appContext)
                .load(localPath)
                .placeholder(R.drawable.user)
                .error(
                        // Fallback tới ảnh từ server
                        Glide.with(appContext)
                                .load(remoteUrl)
                                .placeholder(R.drawable.user)
                )
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.d(TAG, "Failed to load image from local: " + localPath);

                        // Lên lịch tải ảnh trong background
                        downloadImageInBackground(
                                lifecycleOwner,
                                appContext,
                                remoteUrl,
                                token,
                                (newPath) -> {
                                    // Gọi callback với đường dẫn mới
                                    if (onImageDownloaded != null) {
                                        onImageDownloaded.accept(newPath);
                                    }

                                    refreshImageIfAvailable(contextRef, imageViewRef, newPath);
                                }
                        );

                        return false; // Để Glide xử lý fallback
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "Loaded image from local: " + localPath);
                        return false;
                    }
                })
                .into(imageView);
    }

    /**
     * Refresh ImageView với ảnh mới nếu context vẫn còn
     */
    private static void refreshImageIfAvailable(
            WeakReference<Object> contextRef,
            WeakReference<ImageView> imageViewRef,
            String imagePath) {

        Object context = contextRef.get();
        ImageView imageView = imageViewRef.get();

        if (context == null || imageView == null) {
            Log.d(TAG, "Context or ImageView no longer available");
            return;
        }

        if (context instanceof Fragment) {
            Fragment fragment = (Fragment) context;
            if (!fragment.isAdded()) return;

            fragment.requireActivity().runOnUiThread(() -> {
                if (fragment.isAdded() && imageView.getContext() != null) {
                    // Refresh ImageView với ảnh mới tải
                    Glide.with(fragment)
                            .load(new File(imagePath))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imageView);
                }
            });
        } else if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || activity.isDestroyed()) return;

            activity.runOnUiThread(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    // Refresh ImageView với ảnh mới tải
                    Glide.with(activity)
                            .load(new File(imagePath))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(imageView);
                }
            });
        }
    }

    /**
     * Chỉ tải ảnh trong background mà không hiển thị
     *
     * @param lifecycleOwner LifecycleOwner để gắn observer
     * @param context Context ứng dụng
     * @param remoteUrl URL ảnh cần tải
     * @param token Token xác thực (nếu cần)
     * @param onComplete Callback khi tải thành công (nhận đường dẫn local)
     */
    public static void downloadImageInBackground(
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull Context context,
            @NonNull String remoteUrl,
            @Nullable String token,
            @Nullable Consumer<String> onComplete) {

        Log.d(TAG, "Scheduling image download for URL: " + remoteUrl);

        // Tạo input data cho worker
        Data inputData = new Data.Builder()
                .putString("url", remoteUrl)
                .putString("token", token != null ? token : "")
                .build();

        // Tạo Work Request
        OneTimeWorkRequest downloadWork = new OneTimeWorkRequest.Builder(PhotoDownloadWorker.class)
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag("image_download")
                .build();

        // Lên lịch công việc với unique ID
        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        "image_download_" + remoteUrl.hashCode(),
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
                        Log.d(TAG, "Image downloaded successfully to: " + path);

                        if (path != null && onComplete != null) {
                            onComplete.accept(path);
                        }
                    } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                        Log.e(TAG, "Image download work failed");
                    }
                });
    }
}