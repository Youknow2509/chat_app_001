package com.example.chatapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.UserDetail;
import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.models.request.UserModels;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.cloudinary.CloudinaryManager;
import com.example.chatapp.utils.file.MediaUtils;
import com.example.chatapp.utils.session.SessionManager;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewModel extends AndroidViewModel {
    private static final String TAG = "ChatViewModel";

    // LiveData objects
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> resMediaUrlPostUpdate = new MutableLiveData<>();
    private final MutableLiveData<File> imageNewFile = new MutableLiveData<>();

    // Services and managers
    private final SessionManager sessionManager;
    private final ApiManager apiManager;
    private final CloudinaryManager cloudinaryManager;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        Context context = application.getApplicationContext();

        // Initialize services
        this.sessionManager = new SessionManager(context);
        this.apiManager = new ApiManager(context);
        this.cloudinaryManager = CloudinaryManager.getInstance(context);

        // Load initial user data
    }

    /**
     * Process media from URI and save to internal storage
     */
    public void saveMediaToInternalStorageAndUpload(Uri mediaUri, String mediaType) {
        isLoading.setValue(true);

        MediaUtils.saveMediaToInternalStorageAsync(
                getApplication().getApplicationContext(),
                mediaUri,
                mediaType
        ).thenAccept(file -> {
            if (file != null) {
                Log.d(TAG, "Image saved to: " + file.getAbsolutePath());
                imageNewFile.postValue(file);
                uploadImageToCloudinary(file);
            } else {
                isLoading.postValue(false);
                errorMessage.postValue("Lỗi khi lưu ảnh");
            }
        });
    }

    /**
     * Upload image to Cloudinary
     */
    private void uploadImageToCloudinary(File file) {
        if (file == null) {
            isLoading.setValue(false);
            errorMessage.setValue("Không thể xử lý file ảnh");
            return;
        }

        String path = Uri.fromFile(file).toString();
        String folder = "/users/" + sessionManager.getUserName() + "/images/";
        Log.d(TAG, "Upload avatar to Cloudinary: " + path + " to folder: " + folder);


        cloudinaryManager.uploadImage(path, folder, new CloudinaryManager.CloudinaryCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                String url = (String) result.get("url");
                if (url != null) {
                    Log.d(TAG, "Upload success: " + url);
                    resMediaUrlPostUpdate.setValue(url);
                } else {
                    isLoading.postValue(false);
                    errorMessage.postValue("Không nhận được URL từ server");
                }
            }

            @Override
            public void onError(String errorMsg) {
                isLoading.postValue(false);
                errorMessage.postValue("Lỗi tải lên: " + errorMsg);
                Log.e(TAG, "Upload error: " + errorMsg);
            }

            @Override
            public void onProgress(int progress) {
                Log.d(TAG, "Upload progress: " + progress + "%");
            }
        });
    }

    /**
     * Save profile to session
     */
    private void saveAvatarToSQLite(UserDetail userDetail) {
        // TODO: saveAvatarToSQLite
    }

    /**
     * Get access token
     */
    public String getAccessToken() {
        return sessionManager.getAccessToken();
    }

    /**
     * Public getters for LiveData objects
     */

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getResMediaUrlPostUpdate() {
        return resMediaUrlPostUpdate;
    }

    public LiveData<File> getImageNewFile() {
        return imageNewFile;
    }
}