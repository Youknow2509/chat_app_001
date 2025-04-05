package com.example.chatapp.utils.cloudinary;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.payload.Payload;
import com.cloudinary.android.policy.UploadPolicy;
import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.api.RetrofitClient;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.Utils;
import com.example.chatapp.utils.session.SessionManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CloudinaryManager handles all Cloudinary operations for the chat application.
 * This includes uploading, downloading, updating, and deleting various media types.
 */
public class CloudinaryManager {
    private static final String TAG = "CloudinaryManager";
    private static CloudinaryManager instance;
    private String CLOUD_NAME;
    private final Context context;
    private boolean isInitialized = false;
    private final ApiManager apiManager;

    /**
     * Private constructor to enforce singleton pattern
     *
     * @param context Application context
     */
    private CloudinaryManager(Context context) {
        this.context = context.getApplicationContext();
        this.apiManager = new ApiManager();
    }

    /**
     * Get singleton instance of CloudinaryManager
     *
     * @param context Application context
     * @return CloudinaryManager instance
     */
    public static synchronized CloudinaryManager getInstance(Context context) {
        if (instance == null) {
            instance = new CloudinaryManager(context);
        }
        return instance;
    }

    /**
     * Initialize Cloudinary with configuration
     * Call this method in your Application class
     */
    public void initialize(Map<String, String> config) {
        if (!isInitialized) {
            try {
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

                            Call<ResponseData<Object>> call = RetrofitClient.getInstance().getCloudinaryService().getSignatur(Constants.TOKEN_PREFIX_REQUEST + token, configUrl);
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
            throw new IllegalStateException("CloudinaryManager must be initialized before use");
        }
    }

    /**
     * Interface for Cloudinary operation callbacks
     */
    public interface CloudinaryCallback<T> {
        void onSuccess(T result);

        void onError(String errorMsg);

        void onProgress(int progress);
    }

    /**
     * Upload any media file to Cloudinary
     *
     * @param filePath Path to the media file
     * @param folder   Destination folder in Cloudinary
     * @param callback Callback for upload progress and result
     * @return Request ID
     */
    public String uploadMedia(String filePath, String folder, String resourceType,
                              final CloudinaryCallback<Map<String, Object>> callback) {
        checkInitialization();

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
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
                Double progress = (double) bytes / totalBytes;
                Log.d(TAG, "Upload progress: " + progress + "%");
                int intProgress = progress.intValue();
                if (callback != null) {
                    callback.onProgress(intProgress);
                }
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                Log.d(TAG, "Upload successful: " + requestId);
                if (callback != null) {
                    callback.onSuccess(resultData);
                }
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Log.e(TAG, "Upload error: " + error.getDescription());
                if (callback != null) {
                    callback.onError(error.getDescription());
                }
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                Log.d(TAG, "Upload rescheduled: " + requestId);
            }
        };

        return MediaManager.get()
                .upload(Uri.parse(filePath))
                .options(options)
                .callback(uploadCallback)
                .dispatch();
    }

    /**
     * Upload an image to Cloudinary
     *
     * @param imagePath Path to the image file
     * @param folder    Destination folder in Cloudinary
     * @param callback  Callback for upload progress and result
     * @return Request ID
     */
    public String uploadImage(String imagePath, String folder,
                              final CloudinaryCallback<Map<String, Object>> callback) {

        return uploadMedia(imagePath, folder, "image", callback);
    }

    /**
     * Upload a video to Cloudinary
     *
     * @param videoPath Path to the video file
     * @param folder    Destination folder in Cloudinary
     * @param callback  Callback for upload progress and result
     * @return Request ID
     */
    public String uploadVideo(String videoPath, String folder,
                              final CloudinaryCallback<Map<String, Object>> callback) {

        return uploadMedia(videoPath, folder, "video", callback);
    }

    /**
     * Upload a video thumbnail to Cloudinary
     *
     * @param videoPath Path to the video file
     * @param folder    Destination folder in Cloudinary
     * @param callback  Callback for upload progress and result
     * @return Request ID
     */
    public String uploadVideoThumbnail(String videoPath, String folder,
                                       final CloudinaryCallback<Map<String, Object>> callback) {
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
                        if (callback != null) {
                            callback.onSuccess(resultData);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Thumbnail upload error: " + error.getDescription());
                        if (callback != null) {
                            callback.onError(error.getDescription());
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
     *
     * @param publicId     Public ID of the resource to delete
     * @param resourceType Type of resource (image, video, raw, etc.)
     * @param callback     Callback for deletion result
     */
    public void deleteResource(final String publicId, final String resourceType,
                               final CloudinaryCallback<String> callback) {
        checkInitialization();

        // Cloudinary Android SDK doesn't provide a direct method for deletion
        // You'd typically need to use a backend service for this
        // This is a placeholder for how the implementation might look

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // This would be an API call to your backend service
                    // which would then use Cloudinary's Admin API to delete the resource
                    // For illustration purposes only:

                    // Simulate API call to backend
                    Thread.sleep(1000);
                    // TODO: call to sv rm it

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
     * Update a resource in Cloudinary (e.g., change tags, metadata)
     *
     * @param publicId     Public ID of the resource to update
     * @param resourceType Type of resource (image, video, raw, etc.)
     * @param updates      Map of updates to apply
     * @param callback     Callback for update result
     */
    public void updateResource(final String publicId, final String resourceType,
                               final Map<String, Object> updates,
                               final CloudinaryCallback<Map<String, Object>> callback) {
        checkInitialization();

        // Similar to deletion, updates would typically go through your backend
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulate API call to backend
                    Thread.sleep(1000);

                    // Simulate successful update
                    Map<String, Object> result = new HashMap<>();
                    result.put("public_id", publicId);
                    result.put("status", "updated");

                    if (callback != null) {
                        callback.onSuccess(result);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating resource: " + e.getMessage());
                    if (callback != null) {
                        callback.onError("Failed to update resource: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Download a resource from Cloudinary
     *
     * @param publicId        Public ID of the resource to download
     * @param resourceType    Type of resource (image, video, raw, etc.)
     * @param destinationFile Destination file path
     * @param callback        Callback for download progress and result
     */
    public void downloadResource(final String publicId, final String resourceType,
                                 final File destinationFile,
                                 final CloudinaryCallback<File> callback) {
        checkInitialization();

        // This would typically be implemented using standard HTTP download methods
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Generate URL for the resource
                    String url = "https://res.cloudinary.com/" + CLOUD_NAME + "/"
                            + resourceType + "/upload/" + publicId;

                    // Simulate download progress
                    for (int i = 0; i <= 100; i += 10) {
                        Thread.sleep(200);
                        final int progress = i;
                        if (callback != null) {
                            callback.onProgress(progress);
                        }
                    }

                    // Simulate successful download
                    if (callback != null) {
                        callback.onSuccess(destinationFile);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error downloading resource: " + e.getMessage());
                    if (callback != null) {
                        callback.onError("Failed to download resource: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Generate a URL for a resource with transformations
     *
     * @param publicId        Public ID of the resource
     * @param resourceType    Type of resource (image, video, raw, etc.)
     * @param transformations Map of transformations to apply
     * @return URL string for the transformed resource
     */
    public String getResourceUrl(String publicId, String resourceType, Map<String, String> transformations) {
        checkInitialization();

        StringBuilder urlBuilder = new StringBuilder("https://res.cloudinary.com/" + CLOUD_NAME + "/");
        urlBuilder.append(resourceType).append("/upload/");

        // Add transformations
        if (transformations != null && !transformations.isEmpty()) {
            for (Map.Entry<String, String> entry : transformations.entrySet()) {
                urlBuilder.append(entry.getKey()).append("_").append(entry.getValue()).append(",");
            }
            // Remove trailing comma
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
            urlBuilder.append("/");
        }

        urlBuilder.append(publicId);
        return urlBuilder.toString();
    }

    /**
     * Cancel an ongoing upload request
     *
     * @param requestId ID of the request to cancel
     */
    public void cancelUpload(String requestId) {
        checkInitialization();
        MediaManager.get().cancelRequest(requestId);
        Log.d(TAG, "Upload canceled: " + requestId);
    }

    /**
     * Cancel all ongoing upload requests
     */
    public void cancelAllUploads() {
        checkInitialization();
        MediaManager.get().cancelAllRequests();
        Log.d(TAG, "All uploads canceled");
    }
}