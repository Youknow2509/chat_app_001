package com.example.chatapp.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.chatapp.database.AppDatabase;
import com.example.chatapp.models.sqlite.MediaFile;
import com.example.chatapp.utils.file.MediaCacheManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MediaRepository {
    private static final String TAG = "MediaRepository";
    private static MediaRepository instance;

    private final Context appContext;
    private final AppDatabase database;
    private final CloudinaryManager cloudinaryManager;
    private final MediaCacheManager cacheManager;
    private final Executor executor;

    private MediaRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.cloudinaryManager = CloudinaryManager.getInstance();
        this.cacheManager = MediaCacheManager.getInstance(context);
        this.executor = Executors.newFixedThreadPool(4);
    }

    public static synchronized MediaRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MediaRepository(context);
        }
        return instance;
    }

    /**
     * Upload a media file to Cloudinary
     * @param localUri URI of the local file
     * @param messageId Associated message ID
     * @param fileType Media type
     * @param callback Callback for upload status
     */
    public void uploadMedia(Uri localUri, final String messageId, final String fileType,
                            final MediaUploadCallback callback) {
        // First, save to local StoreUtils
        final String localPath = cacheManager.saveToCache(localUri, fileType);
        if (localPath == null) {
            callback.onError("Failed to StoreUtils the file locally");
            return;
        }

        // Create a MediaFile entry in "pending" status
        final String fileId = UUID.randomUUID().toString();
        final MediaFile mediaFile = new MediaFile(fileId, messageId, fileType);
        mediaFile.setLocalPath(localPath);
        mediaFile.setDownloadStatus("pending");

        // For videos, create a thumbnail
        if ("video".equals(fileType)) {
            String thumbnailPath = cacheManager.createVideoThumbnail(localPath);
            mediaFile.setThumbnailPath(thumbnailPath);
        }

        // Save to database
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    database.mediaFileDao().insert(mediaFile);

                    // Now upload to Cloudinary
                    mediaFile.setDownloadStatus("uploading");
                    database.mediaFileDao().update(mediaFile);
                    cloudinaryManager.uploadMedia(appContext, localPath, fileType, messageId,
                            new CloudinaryManager.CloudinaryUploadCallback() {
                                @Override
                                public void onStart(String requestId) {
                                    callback.onStart(fileId);
                                }

                                @Override
                                public void onProgress(String requestId, long bytes, long totalBytes) {
                                    int progress = totalBytes > 0 ? (int)(100 * bytes / totalBytes) : 0;
                                    callback.onProgress(fileId, progress);
                                }

                                @Override
                                public void onSuccess(String requestId, Map resultData) {
                                    executor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Update the database with Cloudinary info
                                            MediaFile updatedFile = database.mediaFileDao().getMediaFileById(fileId);
                                            if (updatedFile != null) {
                                                updatedFile.setDownloadStatus("completed");

                                                // Store the Cloudinary public ID in the fileName field
                                                // This is used to reconstruct the URL later
                                                if (resultData.containsKey("public_id")) {
                                                    updatedFile.setFileName(resultData.get("public_id").toString());
                                                }

                                                // Store the format/extension in mimeType
                                                if (resultData.containsKey("format")) {
                                                    updatedFile.setMimeType(resultData.get("format").toString());
                                                }

                                                // Store file size
                                                if (resultData.containsKey("bytes")) {
                                                    updatedFile.setSize(Long.parseLong(resultData.get("bytes").toString()));
                                                }

                                                database.mediaFileDao().update(updatedFile);
                                                callback.onSuccess(fileId, getCloudinaryUrl(updatedFile));
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    executor.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            MediaFile updatedFile = database.mediaFileDao().getMediaFileById(fileId);
                                            if (updatedFile != null) {
                                                updatedFile.setDownloadStatus("failed");
                                                database.mediaFileDao().update(updatedFile);
                                            }
                                            callback.onError(errorMessage);
                                        }
                                    });
                                }
                            });
                } catch (Exception e) {
                    Log.e(TAG, "Error uploading media: " + e.getMessage());
                    callback.onError("Database error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Download a media file from Cloudinary
     * @param mediaFile MediaFile to download
     * @param callback Callback for download status
     */
    public void downloadMedia(final MediaFile mediaFile, final MediaDownloadCallback callback) {
        if (mediaFile == null) {
            callback.onError("MediaFile is null");
            return;
        }

        // Check if already downloaded
        if ("completed".equals(mediaFile.getDownloadStatus()) &&
                mediaFile.getLocalPath() != null &&
                new java.io.File(mediaFile.getLocalPath()).exists()) {
            callback.onComplete(mediaFile);
            return;
        }

        // Get Cloudinary URL
        final String cloudinaryUrl = getCloudinaryUrl(mediaFile);
        if (cloudinaryUrl == null) {
            callback.onError("Could not construct Cloudinary URL");
            return;
        }

        // Update status to downloading
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mediaFile.setDownloadStatus("downloading");
                database.mediaFileDao().update(mediaFile);

                // Download using MediaCacheManager
                cacheManager.downloadMedia(cloudinaryUrl, mediaFile.getFileType(),
                        new MediaCacheManager.MediaDownloadCallback() {
                            @Override
                            public void onProgress(int progress) {
                                callback.onProgress(mediaFile.getFileId(), progress);
                            }

                            @Override
                            public void onComplete(String localPath, String thumbnailPath) {
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        MediaFile updatedFile = database.mediaFileDao().getMediaFileById(mediaFile.getFileId());
                                        if (updatedFile != null) {
                                            updatedFile.setLocalPath(localPath);
                                            if (thumbnailPath != null) {
                                                updatedFile.setThumbnailPath(thumbnailPath);
                                            }
                                            updatedFile.setDownloadStatus("completed");
                                            database.mediaFileDao().update(updatedFile);
                                            callback.onComplete(updatedFile);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        MediaFile updatedFile = database.mediaFileDao().getMediaFileById(mediaFile.getFileId());
                                        if (updatedFile != null) {
                                            updatedFile.setDownloadStatus("failed");
                                            database.mediaFileDao().update(updatedFile);
                                        }
                                        callback.onError(errorMessage);
                                    }
                                });
                            }
                        });
            }
        });
    }

    /**
     * Get the Cloudinary URL for a media file
     * @param mediaFile MediaFile to get URL for
     * @return Cloudinary URL
     */
    public String getCloudinaryUrl(MediaFile mediaFile) {
        if (mediaFile == null || mediaFile.getFileName() == null) {
            return null;
        }

        String publicId = mediaFile.getFileName();

        switch (mediaFile.getFileType()) {
            case "image":
                return cloudinaryManager.getImageUrl(publicId, 0, 0);
            case "video":
                return cloudinaryManager.getVideoUrl(publicId);
            default:
                // For other types, construct a basic URL
                String cloudName = "your_cloud_name"; // Get from config
                return "https://res.cloudinary.com/" + cloudName + "/raw/upload/" + publicId;
        }
    }

    /**
     * Get a thumbnail URL for a video
     * @param mediaFile Video MediaFile
     * @param width Desired width
     * @param height Desired height
     * @return Thumbnail URL
     */
    public String getVideoThumbnailUrl(MediaFile mediaFile, int width, int height) {
        if (mediaFile == null || !"video".equals(mediaFile.getFileType()) ||
                mediaFile.getFileName() == null) {
            return null;
        }

        return cloudinaryManager.getVideoThumbnailUrl(mediaFile.getFileName(), width, height);
    }

    /**
     * Get media files for a message
     * @param messageId Message ID
     * @return LiveData list of media files
     */
    public LiveData<List<MediaFile>> getMediaForMessage(String messageId) {
        return database.mediaFileDao().getMediaFilesByMessageLiveData(messageId);
    }

    /**
     * Delete a media file
     * @param mediaFile MediaFile to delete
     */
    public void deleteMedia(final MediaFile mediaFile) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Delete local file
                if (mediaFile.getLocalPath() != null) {
                    cacheManager.clearFileFromCache(mediaFile.getLocalPath());
                }

                // Delete thumbnail if exists
                if (mediaFile.getThumbnailPath() != null) {
                    cacheManager.clearFileFromCache(mediaFile.getThumbnailPath());
                }

                // Delete from database
                database.mediaFileDao().delete(mediaFile);

                // Note: This doesn't delete from Cloudinary as that would require
                // authenticated API calls. In a production app, you might want to
                // track and batch delete unused files from Cloudinary periodically.
            }
        });

    }

    // Callbacks
    public interface MediaUploadCallback {
        void onStart(String fileId);
        void onProgress(String fileId, int progress);
        void onSuccess(String fileId, String cloudinaryUrl);
        void onError(String errorMessage);
    }

    public interface MediaDownloadCallback {
        void onProgress(String fileId, int progress);
        void onComplete(MediaFile mediaFile);
        void onError(String errorMessage);
    }
}