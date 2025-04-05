package com.example.chatapp.utils.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaCacheManager {
    private static final String TAG = "MediaCacheManager";
    private static MediaCacheManager instance;
    private final ExecutorService executor;
    private final Context appContext;

    private static final long MAX_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final String CACHE_DIR_NAME = "media_cache";
    private File cacheDir;

    private MediaCacheManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.executor = Executors.newFixedThreadPool(4);

        // Initialize StoreUtils directory
        initCacheDir();
    }

    public static synchronized MediaCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new MediaCacheManager(context);
        }
        return instance;
    }

    private void initCacheDir() {
        cacheDir = new File(appContext.getExternalFilesDir(null), CACHE_DIR_NAME);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    /**
     * Download media from URL and save to local StoreUtils
     * @param url URL to download from
     * @param fileType Type of media
     * @param callback Callback to notify download status
     */
    public void downloadMedia(final String url, final String fileType, final MediaDownloadCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                File outputFile = null;
                HttpURLConnection connection = null;
                InputStream input = null;
                FileOutputStream output = null;

                try {
                    // Generate a unique filename based on the URL
                    String ext = MimeTypeMap.getFileExtensionFromUrl(url);
                    if (ext.isEmpty()) {
                        // Default extensions based on file type
                        switch (fileType) {
                            case "image": ext = "jpg"; break;
                            case "video": ext = "mp4"; break;
                            case "audio": ext = "mp3"; break;
                            default: ext = "dat"; break;
                        }
                    }

                    String filename = UUID.randomUUID().toString() + "." + ext;
                    outputFile = new File(cacheDir, filename);

                    // Open connection
                    URL urlObj = new URL(url);
                    connection = (HttpURLConnection) urlObj.openConnection();
                    connection.connect();

                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        callback.onError("Server returned HTTP " + connection.getResponseCode());
                        return;
                    }

                    int fileLength = connection.getContentLength();
                    input = connection.getInputStream();
                    output = new FileOutputStream(outputFile);

                    byte[] buffer = new byte[4096];
                    long total = 0;
                    int count;

                    while ((count = input.read(buffer)) != -1) {
                        total += count;
                        if (fileLength > 0) {
                            callback.onProgress((int) (total * 100 / fileLength));
                        }
                        output.write(buffer, 0, count);
                    }

                    // Create thumbnail for videos
                    String thumbnailPath = null;
                    if ("video".equals(fileType)) {
                        thumbnailPath = createVideoThumbnail(outputFile.getAbsolutePath());
                    }

                    // Success
                    callback.onComplete(outputFile.getAbsolutePath(), thumbnailPath);

                    // Check if we need to clean up StoreUtils
                    checkCacheSize();

                } catch (Exception e) {
                    Log.e(TAG, "Download failed: " + e.getMessage());
                    if (outputFile != null && outputFile.exists()) {
                        outputFile.delete();
                    }
                    callback.onError("Download failed: " + e.getMessage());
                } finally {
                    try {
                        if (output != null) output.close();
                        if (input != null) input.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                    if (connection != null) connection.disconnect();
                }
            }
        });
    }

    /**
     * Save a media file that was just captured/selected to the StoreUtils
     * @param sourceUri URI of the media file
     * @param fileType Type of media
     * @return Path to the cached file
     */
    public String saveToCache(Uri sourceUri, String fileType) {
        try {
            String extension;
            String mimeType = appContext.getContentResolver().getType(sourceUri);
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            } else {
                // Default extensions
                switch (fileType) {
                    case "image": extension = "jpg"; break;
                    case "video": extension = "mp4"; break;
                    case "audio": extension = "mp3"; break;
                    default: extension = "dat"; break;
                }
            }

            String filename = UUID.randomUUID().toString() + "." + extension;
            File destFile = new File(cacheDir, filename);

            InputStream in = appContext.getContentResolver().openInputStream(sourceUri);
            FileOutputStream out = new FileOutputStream(destFile);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save to StoreUtils: " + e.getMessage());
            return null;
        }
    }

    /**
     * Create a thumbnail for a video file
     * @param videoPath Path to the video file
     * @return Path to the thumbnail file
     */
    public String createVideoThumbnail(String videoPath) {
        try {
            // Create a thumbnail from the video
            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
                    videoPath, MediaStore.Images.Thumbnails.MINI_KIND);

            if (thumbnail == null) {
                return null;
            }

            // Save the thumbnail
            String thumbnailFilename = UUID.randomUUID().toString() + "_thumb.jpg";
            File thumbnailFile = new File(cacheDir, thumbnailFilename);

            FileOutputStream out = new FileOutputStream(thumbnailFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.flush();
            out.close();

            return thumbnailFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create video thumbnail: " + e.getMessage());
            return null;
        }
    }

    /**
     * Preload an image into the memory StoreUtils
     * @param context Context
     * @param url URL of the image
     */
    public void preloadImage(Context context, String url) {
        try {
            Glide.with(context)
                    .load(url)
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .preload();
        } catch (Exception e) {
            Log.e(TAG, "Error preloading image: " + e.getMessage());
        }
    }

    /**
     * Clear a specific file from StoreUtils
     * @param localPath Path to the file
     * @return True if successful
     */
    public boolean clearFileFromCache(String localPath) {
        if (localPath == null) return false;

        File file = new File(localPath);
        return file.exists() && file.delete();
    }

    /**
     * Clear all files from StoreUtils
     */
    public void clearCache() {
        if (cacheDir != null && cacheDir.exists()) {
            for (File file : cacheDir.listFiles()) {
                file.delete();
            }
        }
    }

    /**
     * Check if the StoreUtils exceeds the max size and clean up if needed
     */
    private void checkCacheSize() {
        long size = getDirSize(cacheDir);
        if (size > MAX_CACHE_SIZE) {
            cleanupCache(size - (MAX_CACHE_SIZE / 2)); // Remove oldest files until half the max size
        }
    }

    private long getDirSize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                }
            }
        }
        return size;
    }

    private void cleanupCache(long bytesToFree) {
        File[] files = cacheDir.listFiles();
        if (files == null) return;

        // Sort files by last modified time (oldest first)
        java.util.Arrays.sort(files, new java.util.Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });

        long freed = 0;
        for (File file : files) {
            if (freed >= bytesToFree) break;
            long fileSize = file.length();
            if (file.delete()) {
                freed += fileSize;
            }
        }
    }

    public interface MediaDownloadCallback {
        void onProgress(int progress);
        void onComplete(String localPath, String thumbnailPath);
        void onError(String errorMessage);
    }
}