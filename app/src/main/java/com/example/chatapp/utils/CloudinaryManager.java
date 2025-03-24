package com.example.chatapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.preprocess.BitmapEncoder;
import com.cloudinary.android.preprocess.DimensionsValidator;
import com.cloudinary.android.preprocess.ImagePreprocessChain;
import com.cloudinary.android.preprocess.Preprocess;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";

    private static CloudinaryManager instance;
    private boolean isInitialized = false;
    private String cloudName;

    private CloudinaryManager() {
        // Private constructor to enforce singleton
    }

    public static synchronized CloudinaryManager getInstance() {
        if (instance == null) {
            instance = new CloudinaryManager();
        }
        return instance;
    }

    public void initialize(Context context, String cloudName, String apiKey, String apiSecret) {
        if (isInitialized) return;

        try {
            this.cloudName = cloudName; // Store the cloud name
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            config.put("secure", true);

            MediaManager.init(context, config);
            isInitialized = true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Upload media file to Cloudinary
     * @param context Application context
     * @param filePath Local file path to upload
     * @param fileType Type of media (image, video, etc.)
     * @param messageId Associated message ID
     * @param callback Callback to receive upload results
     * @return The request ID for tracking
     */
    public String uploadMedia(Context context, String filePath, final String fileType,
                              final String messageId, final CloudinaryUploadCallback callback) {
        if (!isInitialized) {
            callback.onError("Cloudinary not initialized");
            return null;
        }

        // Create upload request with options
        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", getResourceType(fileType));
        options.put("folder", "chat_media");
        options.put("use_filename", true);

        // Add custom metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("message_id", messageId);
        metadata.put("file_type", fileType);
        options.put("metadata", metadata);

        // Special handling for images
        if ("image".equals(fileType)) {
            return MediaManager.get()
                    .upload(Uri.parse(filePath))
                    .option("quality", "auto")
                    .unsigned("ml_default")
                    .preprocess(ImagePreprocessChain.limitDimensionsChain(1024, 1024)
                            .addStep(new DimensionsValidator(10, 10, 2000, 2000))
                            .addStep((Preprocess<Bitmap>) new BitmapEncoder(BitmapEncoder.Format.WEBP, 80)))
                    .options(options)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            callback.onStart(requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            callback.onProgress(requestId, bytes, totalBytes);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            callback.onSuccess(requestId, resultData);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            callback.onError("Upload failed: " + error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.d(TAG, "Upload rescheduled: " + error.getDescription());
                        }
                    })
                    .dispatch();
        } else {
            // For video and other media types
            return MediaManager.get()
                    .upload(Uri.parse(filePath))
                    .unsigned("ml_default")
                    .options(options)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            callback.onStart(requestId);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            callback.onProgress(requestId, bytes, totalBytes);
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            callback.onSuccess(requestId, resultData);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            callback.onError("Upload failed: " + error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.d(TAG, "Upload rescheduled: " + error.getDescription());
                        }
                    })
                    .dispatch();
        }
    }

    /**
     * Get a URL for an image with transformations
     * @param publicId The public ID of the image on Cloudinary
     * @param width Desired width (0 for auto)
     * @param height Desired height (0 for auto)
     * @return The transformed URL
     */
    public String getImageUrl(String publicId, int width, int height) {
        if (!isInitialized) return null;

        String transformation = "";
        if (width > 0 || height > 0) {
            transformation = "/c_fill,g_auto";
            if (width > 0) transformation += ",w_" + width;
            if (height > 0) transformation += ",h_" + height;
            transformation += ",f_auto,q_auto";
        } else {
            transformation = "/f_auto,q_auto";
        }

        return "https://res.cloudinary.com/" + cloudName + "/image/upload" + transformation + "/" + publicId;
    }

    /**
     * Get a URL for a video with transformations
     * @param publicId The public ID of the video on Cloudinary
     * @return The transformed URL
     */
    public String getVideoUrl(String publicId) {
        if (!isInitialized) return null;

        return "https://res.cloudinary.com/" + cloudName + "/video/upload/q_auto/" + publicId;
    }

    /**
     * Get the video thumbnail URL
     * @param publicId The public ID of the video
     * @param width Desired width
     * @param height Desired height
     * @return The thumbnail URL
     */
    public String getVideoThumbnailUrl(String publicId, int width, int height) {
        if (!isInitialized) return null;

        String transformation = "/c_fill,g_auto";
        if (width > 0) transformation += ",w_" + width;
        if (height > 0) transformation += ",h_" + height;
        transformation += ",f_jpg,q_auto";

        return "https://res.cloudinary.com/" + cloudName + "/video/upload" + transformation + "/" + publicId;
    }

    private String getResourceType(String fileType) {
        switch (fileType) {
            case "image": return "image";
            case "video": return "video";
            case "audio": return "raw";
            default: return "auto";
        }
    }

    public interface CloudinaryUploadCallback {
        void onStart(String requestId);
        void onProgress(String requestId, long bytes, long totalBytes);
        void onSuccess(String requestId, Map resultData);
        void onError(String errorMessage);
    }
}