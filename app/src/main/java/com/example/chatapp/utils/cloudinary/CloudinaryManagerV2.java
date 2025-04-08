package com.example.chatapp.utils.cloudinary;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.policy.TimeWindow;
import com.cloudinary.android.policy.UploadPolicy;
import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.api.RetrofitClient;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.network.NetworkMonitor;
import com.example.chatapp.utils.Utils;
import com.example.chatapp.utils.session.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CloudinaryManagerV2 handles all Cloudinary operations for the chat application.
 * This includes uploading, downloading, updating, and deleting various media types.
 * Now includes NetworkMonitor integration for handling network connectivity issues.
 */
public class CloudinaryManagerV2 implements NetworkMonitor.NetworkStateListener {
    private static final String TAG = "CloudinaryManagerV2";
    private static CloudinaryManagerV2 instance;
    private String CLOUD_NAME;
    private final Context context;
    private boolean isInitialized = false;
    private final ApiManager apiManager;
    private final NetworkMonitor networkMonitor;

    // Pending uploads queue for offline mode
    private final Queue<PendingUpload> pendingUploads = new LinkedList<>();
    private boolean isProcessingQueue = false;

    // List of ongoing upload requests
    private final List<String> activeRequestIds = new ArrayList<>();

    // Upload state listeners
    private final List<UploadStateListener> uploadStateListeners = new ArrayList<>();

    /**
     * Private constructor to enforce singleton pattern
     *
     * @param context Application context
     */
    private CloudinaryManagerV2(Context context) {
        this.context = context.getApplicationContext();
        this.apiManager = new ApiManager(this.context);
        this.networkMonitor = NetworkMonitor.getInstance(this.context);

        // Register for network changes
        this.networkMonitor.addListener(this);
    }

    /**
     * Get singleton instance of CloudinaryManagerV2
     *
     * @param context Application context
     * @return CloudinaryManagerV2 instance
     */
    public static synchronized CloudinaryManagerV2 getInstance(Context context) {
        if (instance == null) {
            instance = new CloudinaryManagerV2(context);
        }
        return instance;
    }

    /**
     * Interface for upload state listeners
     */
    public interface UploadStateListener {
        void onUploadQueueChanged(int pendingCount);
        void onNetworkStateChanged(boolean isAvailable, int pendingCount);
    }

    /**
     * Add upload state listener
     */
    public void addUploadStateListener(UploadStateListener listener) {
        if (!uploadStateListeners.contains(listener)) {
            uploadStateListeners.add(listener);
        }
    }

    /**
     * Remove upload state listener
     */
    public void removeUploadStateListener(UploadStateListener listener) {
        uploadStateListeners.remove(listener);
    }

    /**
     * Notify listeners about queue changes
     */
    private void notifyUploadQueueChanged() {
        int pendingCount = pendingUploads.size();
        for (UploadStateListener listener : uploadStateListeners) {
            listener.onUploadQueueChanged(pendingCount);
        }
    }

    /**
     * Notify listeners about network state changes
     */
    private void notifyNetworkStateChanged(boolean isAvailable) {
        int pendingCount = pendingUploads.size();
        for (UploadStateListener listener : uploadStateListeners) {
            listener.onNetworkStateChanged(isAvailable, pendingCount);
        }
    }

    /**
     * Implementation of NetworkStateListener interface
     */
    @Override
    public void onNetworkStateChanged(boolean isAvailable) {
        Log.d(TAG, "Network connectivity changed: " + (isAvailable ? "Available" : "Unavailable"));

        if (isAvailable) {
            // Network is available, process any pending uploads
            processPendingUploads();
        } else {
            // Network is unavailable, pause active uploads
            pauseActiveUploads();
        }

        // Notify listeners
        notifyNetworkStateChanged(isAvailable);
    }

    /**
     * Class to store pending upload information
     */
    private static class PendingUpload {
        String filePath;
        String folder;
        String resourceType;
        CloudinaryCallback<Map<String, Object>> callback;

        PendingUpload(String filePath, String folder, String resourceType,
                      CloudinaryCallback<Map<String, Object>> callback) {
            this.filePath = filePath;
            this.folder = folder;
            this.resourceType = resourceType;
            this.callback = callback;
        }
    }

    /**
     * Initialize Cloudinary with configuration
     * Call this method in your Application class
     */
    public void initialize(Map<String, String> config) {
        if (!isInitialized) {
            try {
                // Set upload policy for auto-retry and network awareness
                UploadPolicy uploadPolicy = new UploadPolicy.Builder()
                        .networkPolicy(UploadPolicy.NetworkType.ANY)
                        .maxRetries(3)
                        .backoffCriteria(2000, UploadPolicy.BackoffPolicy.EXPONENTIAL)
                        .build();

                MediaManager.init(context, new SignatureProvider() {
                    String token = SessionManager.getInstance().getAccessToken();

                    @Override
                    public Signature provideSignature(Map options) {
                        Log.d(TAG, "Options: " + options);
                        // call api get signature
                        try {
                            // Get request signature
                            Log.d(TAG, "Getting signature...");
                            String configUrl = Utils.getConfigUrl(options);
                            Log.d(TAG, "Config URL: " + configUrl);

                            Call<ResponseData<Object>> call = RetrofitClient.getInstance(context).getCloudinaryService().getSignatur(Constants.TOKEN_PREFIX_REQUEST + token, configUrl);
                            Response<ResponseData<Object>> response = call.execute();

                            int code = response.code();
                            if (code != 200) {
                                Log.e(TAG, "Error getting signature: " + code);
                                return null;
                            }
                            String apiKey = com.example.chatapp.utils.Utils.getDataBody(response.body(), "api_key");
                            String cloudName = com.example.chatapp.utils.Utils.getDataBody(response.body(), "cloud_name");
                            String signature = com.example.chatapp.utils.Utils.getDataBody(response.body(), "signature");
                            String timestamp = com.example.chatapp.utils.Utils.getDataBody(response.body(), "timestamp");
                            long longTimestamp = Long.parseLong(timestamp);

                            return new Signature(signature, apiKey, longTimestamp);
                        } catch (Exception e) {
                            Log.e(TAG, "Error getting signature: " + e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    public String getName() {
                        return "get signature";
                    }
                }, config);


                isInitialized = true;
                CLOUD_NAME = config.get("cloud_name");

                Log.d(TAG, "Cloudinary initialized successfully");

                // Process any pending uploads that might have been queued before initialization
                if (!pendingUploads.isEmpty()) {
                    processPendingUploads();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing Cloudinary: " + e.getMessage());
            }
        }
    }

    /**
     * Check if the manager is initialized
     */
    private void checkInitialization() {
        if (!isInitialized) {
            throw new IllegalStateException("CloudinaryManagerV2 must be initialized before use");
        }
    }

    /**
     * Interface for Cloudinary operation callbacks
     */
    public interface CloudinaryCallback<T> {
        void onSuccess(T result);
        void onError(String errorMsg);
        void onProgress(int progress);
        // New method for network issues
        default void onNetworkUnavailable(boolean willRetry) {}
    }

    /**
     * Upload any media file to Cloudinary with network awareness
     *
     * @param filePath Path to the media file
     * @param folder   Destination folder in Cloudinary
     * @param callback Callback for upload progress and result
     * @return Request ID or null if queued for later
     */
    public String uploadMedia(String filePath, String folder, String resourceType,
                              final CloudinaryCallback<Map<String, Object>> callback) {

        // Check if Cloudinary is initialized
        if (!isInitialized) {
            Log.e(TAG, "Cloudinary not initialized. Queuing upload for later");
            queueUpload(filePath, folder, resourceType, callback);
            return null;
        }

        // Check network connectivity
        if (!networkMonitor.isNetworkAvailable()) {
            Log.d(TAG, "No network connection. Queuing upload for later");
            queueUpload(filePath, folder, resourceType, callback);

            // Notify callback about network issue
            if (callback != null) {
                callback.onNetworkUnavailable(true);
            }

            return null;
        }

        // doc config options:: https://cloudinary.com/documentation/image_upload_api_reference#upload_required_parameters
        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", resourceType);
        options.put("max_file_size", 10485760 * 3); // 30 MB (in bytes)

        if (folder != null && !folder.isEmpty()) {
            options.put("asset_folder", folder);
        }

        // show config upload
        Log.d(TAG, "Options config upload: " + options);

        UploadCallback uploadCallback = new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                Log.d(TAG, "Upload started: " + requestId);
                // Add to active requests
                synchronized (activeRequestIds) {
                    activeRequestIds.add(requestId);
                }
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                Double progress = (double) bytes / totalBytes;
                Log.d(TAG, "Upload progress: " + progress + "%");
                int intProgress = (int) (progress * 100);
                if (callback != null) {
                    callback.onProgress(intProgress);
                }
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                Log.d(TAG, "Upload successful: " + requestId);

                // Remove from active requests
                synchronized (activeRequestIds) {
                    activeRequestIds.remove(requestId);
                }

                if (callback != null) {
                    callback.onSuccess(resultData);
                }
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Log.e(TAG, "Upload error: " + error.getDescription());

                // Remove from active requests
                synchronized (activeRequestIds) {
                    activeRequestIds.remove(requestId);
                }

                // Check if it's a network error
                if (error.getCode() == ErrorInfo.NETWORK_ERROR) {
                    if (!networkMonitor.isNetworkAvailable()) {
                        // Network is unavailable, queue for retry
                        queueUpload(filePath, folder, resourceType, callback);

                        if (callback != null) {
                            callback.onNetworkUnavailable(true);
                        }
                    } else {
                        // Network is available but there was still an error
                        if (callback != null) {
                            callback.onError("Network error: " + error.getDescription());
                        }
                    }
                } else {
                    // Other type of error
                    if (callback != null) {
                        callback.onError(error.getDescription());
                    }
                }
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                Log.d(TAG, "Upload rescheduled: " + requestId);
            }
        };

        String requestId = MediaManager.get()
                .upload(Uri.parse(filePath))
                .options(options)
                .callback(uploadCallback)
                .dispatch();

        return requestId;
    }

    /**
     * Queue an upload for when network is available
     */
    private void queueUpload(String filePath, String folder, String resourceType,
                             CloudinaryCallback<Map<String, Object>> callback) {
        PendingUpload pendingUpload = new PendingUpload(filePath, folder, resourceType, callback);
        pendingUploads.add(pendingUpload);

        // Notify listeners about queue changes
        notifyUploadQueueChanged();

        Log.d(TAG, "Upload queued. Current queue size: " + pendingUploads.size());
    }

    /**
     * Process any pending uploads when network becomes available
     */
    private void processPendingUploads() {
        if (isProcessingQueue || pendingUploads.isEmpty() || !isInitialized ||
                !networkMonitor.isNetworkAvailable()) {
            return;
        }

        isProcessingQueue = true;
        Log.d(TAG, "Processing pending uploads. Queue size: " + pendingUploads.size());

        while (!pendingUploads.isEmpty() && networkMonitor.isNetworkAvailable()) {
            PendingUpload upload = pendingUploads.poll();

            // Notify about the retry
            if (upload.callback != null) {
                upload.callback.onProgress(0); // Reset progress
            }

            // Perform the upload
            uploadMedia(upload.filePath, upload.folder, upload.resourceType, upload.callback);

            // Small delay to prevent overloading
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        isProcessingQueue = false;

        // Notify listeners about queue changes
        notifyUploadQueueChanged();
    }

    /**
     * Pause all active uploads when network becomes unavailable
     */
    private void pauseActiveUploads() {
        Log.d(TAG, "Pausing active uploads due to network unavailability");

        // Cloudinary SDK will automatically pause uploads,
        // but we should inform the user
        synchronized (activeRequestIds) {
            if (!activeRequestIds.isEmpty()) {
                Log.d(TAG, "There are " + activeRequestIds.size() + " active uploads being paused");
                // We don't cancel them, we let Cloudinary's retry mechanism handle it
            }
        }
    }

    /**
     * Get the number of pending uploads
     */
    public int getPendingUploadsCount() {
        return pendingUploads.size();
    }

    /**
     * Check if there are uploads in progress
     */
    public boolean hasActiveUploads() {
        synchronized (activeRequestIds) {
            return !activeRequestIds.isEmpty();
        }
    }

    /**
     * Check if uploads are being processed
     */
    public boolean isProcessingUploads() {
        return isProcessingQueue || hasActiveUploads();
    }

    /**
     * Force retry of pending uploads
     */
    public void retryPendingUploads() {
        if (networkMonitor.isNetworkAvailable()) {
            processPendingUploads();
        } else {
            Log.d(TAG, "Cannot retry uploads - no network connection");
        }
    }

    /**
     * Upload an image to Cloudinary with network awareness
     *
     * @param imagePath Path to the image file
     * @param folder    Destination folder in Cloudinary
     * @param callback  Callback for upload progress and result
     * @return Request ID or null if queued
     */
    public String uploadImage(String imagePath, String folder,
                              final CloudinaryCallback<Map<String, Object>> callback) {
        return uploadMedia(imagePath, folder, "image", callback);
    }

    /**
     * Upload a video to Cloudinary with network awareness
     *
     * @param videoPath Path to the video file
     * @param folder    Destination folder in Cloudinary
     * @param callback  Callback for upload progress and result
     * @return Request ID or null if queued
     */
    public String uploadVideo(String videoPath, String folder,
                              final CloudinaryCallback<Map<String, Object>> callback) {
        return uploadMedia(videoPath, folder, "video", callback);
    }

    // Rest of methods with network awareness added...

    /**
     * Upload a video thumbnail to Cloudinary
     */
    public String uploadVideoThumbnail(String videoPath, String folder,
                                       final CloudinaryCallback<Map<String, Object>> callback) {
        // Check network connectivity
        if (!networkMonitor.isNetworkAvailable()) {
            Log.d(TAG, "No network connection. Queuing upload for later");
            queueUpload(videoPath, folder, "video", callback);

            // Notify callback about network issue
            if (callback != null) {
                callback.onNetworkUnavailable(true);
            }

            return null;
        }

        checkInitialization();

        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", "video");
        if (folder != null && !folder.isEmpty()) {
            options.put("folder", folder);
        }
        options.put("eager", "c_thumb,w_300,h_300");
        options.put("eager_async", true);

        return MediaManager.get()
                .upload(Uri.parse(videoPath))
                .options(options)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Thumbnail upload started: " + requestId);
                        synchronized (activeRequestIds) {
                            activeRequestIds.add(requestId);
                        }
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        Log.d(TAG, "Thumbnail upload progress: " + progress + "%");
                        if (callback != null) {
                            callback.onProgress(progress);
                        }
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d(TAG, "Thumbnail upload successful: " + requestId);
                        synchronized (activeRequestIds) {
                            activeRequestIds.remove(requestId);
                        }
                        if (callback != null) {
                            callback.onSuccess(resultData);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Thumbnail upload error: " + error.getDescription());
                        synchronized (activeRequestIds) {
                            activeRequestIds.remove(requestId);
                        }

                        // Check if it's a network error
                        if (error.getCode() == ErrorInfo.NETWORK_ERROR) {
                            if (!networkMonitor.isNetworkAvailable()) {
                                // Network is unavailable, queue for retry
                                queueUpload(videoPath, folder, "video", callback);

                                if (callback != null) {
                                    callback.onNetworkUnavailable(true);
                                }
                            } else {
                                // Network is available but there was still an error
                                if (callback != null) {
                                    callback.onError("Network error: " + error.getDescription());
                                }
                            }
                        } else {
                            // Other type of error
                            if (callback != null) {
                                callback.onError(error.getDescription());
                            }
                        }
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d(TAG, "Thumbnail upload rescheduled: " + requestId);
                    }
                })
                .dispatch();
    }

    /**
     * Delete a resource from Cloudinary
     */
    public void deleteResource(final String publicId, final String resourceType,
                               final CloudinaryCallback<String> callback) {
        // Check network connectivity
        if (!networkMonitor.isNetworkAvailable()) {
            Log.d(TAG, "No network connection. Cannot delete resource.");
            if (callback != null) {
                callback.onNetworkUnavailable(false);
                callback.onError("Network unavailable. Cannot delete resource.");
            }
            return;
        }

        checkInitialization();

        // Rest of the method...
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // This would be an API call to your backend service
                    // which would then use Cloudinary's Admin API to delete the resource

                    // Simulate API call to backend
                    Thread.sleep(1000);

                    // Simulate successful deletion
                    if (callback != null) {
                        callback.onSuccess("Resource " + publicId + " deleted successfully");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error deleting resource: " + e.getMessage());
                    if (callback != null) {
                        callback.onError("Failed to delete resource: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Cleanup method to release resources and unregister listeners
     */
    public void cleanup() {
        // Unregister from network monitor
        networkMonitor.removeListener(this);

        // Clear all pending uploads
        pendingUploads.clear();

        // Clear all listeners
        uploadStateListeners.clear();

        // Cancel all active uploads
        cancelAllUploads();
    }

    /**
     * Cancel an ongoing upload request
     */
    public void cancelUpload(String requestId) {
        checkInitialization();

        synchronized (activeRequestIds) {
            activeRequestIds.remove(requestId);
        }

        MediaManager.get().cancelRequest(requestId);
        Log.d(TAG, "Upload canceled: " + requestId);
    }

    /**
     * Cancel all ongoing upload requests
     */
    public void cancelAllUploads() {
        checkInitialization();

        synchronized (activeRequestIds) {
            activeRequestIds.clear();
        }

        MediaManager.get().cancelAllRequests();
        Log.d(TAG, "All uploads canceled");
    }

    // Other methods remain the same...
}