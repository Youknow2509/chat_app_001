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

public class ProfileViewModel extends AndroidViewModel {
    private static final String TAG = "ProfileViewModel";

    // LiveData objects
    private final MutableLiveData<UserDetail> userDetailLiveData = new MutableLiveData<>();
    private final MutableLiveData<UserDetail> userDetailUpdateLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<File> avatarNewFile = new MutableLiveData<>();

    // Services and managers
    private final SessionManager sessionManager;
    private final ApiManager apiManager;
    private final CloudinaryManager cloudinaryManager;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        Context context = application.getApplicationContext();

        // Initialize services
        this.sessionManager = new SessionManager(context);
        this.apiManager = new ApiManager(context);
        this.cloudinaryManager = CloudinaryManager.getInstance(context);

        // Load initial user data
        loadUserData();
    }

    /**
     * Load initial user data from session
     */
    private void loadUserData() {
        // get user profile from session
        UserProfileSession userProfileSession = sessionManager.getUserProfile();
        // Create user detail show to view
        userDetailLiveData.setValue(new UserDetail(
                userProfileSession.getDisplayName(),
                sessionManager.getUserAvatar(),
                sessionManager.getPathFileAvatarUser(),
                userProfileSession.getEmail(),
                userProfileSession.getUserGender(),
                userProfileSession.getDateOfBirth()
        ));
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
                avatarNewFile.postValue(file);
                uploadAvatarToCloudinary(file);
            } else {
                isLoading.postValue(false);
                errorMessage.postValue("Lỗi khi lưu ảnh");
            }
        });
    }

    /**
     * Upload avatar to Cloudinary
     */
    private void uploadAvatarToCloudinary(File file) {
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

                    // Update user detail with new avatar URL
                    UserDetail current = userDetailLiveData.getValue();
                    if (current != null) {
                        UserDetail updated = new UserDetail(
                                current.getName(),
                                url,
                                file.getAbsolutePath(),
                                current.getEmail(),
                                current.getGender(),
                                current.getBirthday()
                        );
                        Log.d(TAG, "Update user detail with new avatar URL");
                        Log.d(TAG, updated.toString());
                        userDetailUpdateLiveData.setValue(updated);
                        updataUserAvatar(updated);
                    } else {
                        isLoading.postValue(false);
                        errorMessage.postValue("Không thể cập nhật thông tin người dùng");
                    }
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
                // reload image view
                userDetailLiveData.postValue(userDetailLiveData.getValue());
            }

            @Override
            public void onProgress(int progress) {
                Log.d(TAG, "Upload progress: " + progress + "%");
            }
        });
    }

    /**
     * Update user profile information
     */
    public void updateUserInfo(String name, String gender, String birthday) {
        UserDetail current = userDetailLiveData.getValue();
        if (current == null) {
            errorMessage.setValue("Không có dữ liệu người dùng");
            return;
        }

        UserDetail updated = new UserDetail(
                name,
                current.getUrl_avatar(),
                current.getPath_local_avatar(),
                current.getEmail(),
                gender,
                birthday
        );

        userDetailUpdateLiveData.setValue(updated);
        updateUserProfile();
    }

    /**
     * Call API to update user profile
     */
    private void updateUserProfile() {
        isLoading.setValue(true);

        if (userDetailUpdateLiveData.getValue() == null) {
            isLoading.setValue(false);
            errorMessage.setValue("Không có dữ liệu để cập nhật");
            return;
        }

        UserDetail updateDetail = userDetailUpdateLiveData.getValue();

        apiManager.updateUserInfo(
                sessionManager.getAccessToken(),
                new UserModels.UpdateUserInfoInput(
                        sessionManager.getUserId(),
                        updateDetail.getName(),
                        formatDate(updateDetail.getBirthday()),
                        updateDetail.getGender()
                ),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        if (response.body() == null) {
                            Log.e(TAG, "Error call update profile: Response body is null");
                            errorMessage.postValue("Lỗi cập nhật hồ sơ: Phản hồi từ máy chủ trống");
                            isLoading.postValue(false);
                            return;
                        }

                        int code = response.body().getCode();
                        if (code != Constants.CODE_SUCCESS) {
                            Log.e(TAG, "Error call update profile: " + response.body().getMessage());
                            errorMessage.postValue(response.body().getMessage());
                            isLoading.postValue(false);
                            return;
                        }

                        // Save to session
                        saveProfileToSession(updateDetail);

                        // Update LiveData
                        userDetailLiveData.postValue(updateDetail);
                        errorMessage.postValue("");
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        Log.e(TAG, "Error call update profile: " + t.getMessage());
                        errorMessage.postValue("Lỗi kết nối: " + t.getMessage());
                        isLoading.postValue(false);
                    }
                }
        );
    }

    /**
     * Call API to update avatar user
     */
    private void updataUserAvatar(UserDetail updateDetail) {
        isLoading.setValue(true);

        String url_avatar = updateDetail.getUrl_avatar();
        String path_local_avatar = updateDetail.getPath_local_avatar();

        apiManager.updateAvatar(
                sessionManager.getAccessToken(),
                sessionManager.getUserId(),
                url_avatar,
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        if (response.body() == null) {
                            Log.e(TAG, "Error call update avatar: Response body is null");
                            errorMessage.postValue("Lỗi cập nhật ảnh người dùng: Phản hồi từ máy chủ trống");
                            isLoading.postValue(false);
                            return;
                        }

                        int code = response.body().getCode();
                        if (code != Constants.CODE_SUCCESS) {
                            Log.e(TAG, "Error call update avatar profile: " + response.body().getMessage());
                            errorMessage.postValue(response.body().getMessage());
                            isLoading.postValue(false);
                            return;
                        }

                        // Save to session
                        saveProfileToSession(updateDetail);

                        // Update LiveData
                        userDetailLiveData.postValue(updateDetail);
                        errorMessage.postValue("");
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        Log.e(TAG, "Error call update profile: " + t.getMessage());
                        errorMessage.postValue("Lỗi kết nối: " + t.getMessage());
                        isLoading.postValue(false);
                    }
                }
        );
    }

    /**
     * Save profile to session
     */
    private void saveProfileToSession(UserDetail userDetail) {
        if (userDetail == null) return;

        sessionManager.getUserProfile().setDisplayName(userDetail.getName());
        sessionManager.getUserProfile().setUserGender(userDetail.getGender());
        sessionManager.getUserProfile().setDateOfBirth(userDetail.getBirthday());
        sessionManager.setUserAvatar(userDetail.getUrl_avatar());
        sessionManager.setPathFileAvatarUser(userDetail.getPath_local_avatar());
    }

    /**
     * Format date string
     */
    private String formatDate(String date) {
        try {
            Date d = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date);
            return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(d);
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting date", e);
            return "";
        }
    }

    /**
     * Validate form data
     */
    public String validateUserData(String name, String email, String gender, String birthday) {
        // Validate name
        if (name.isEmpty()) {
            return "Name is required";
        }

        // Validate email
        if (email.isEmpty()) {
            return "Email is required";
        }

        // Validate gender
        if (gender.equalsIgnoreCase("Select gender")) {
            return "Please select a gender";
        }

        // Validate birthday
        if (birthday.isEmpty()) {
            return "Birthday is required";
        }

        try {
            Date d = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(birthday);
            if (d.after(new Date())) {
                return "Invalid date (must be in the past)";
            }
        } catch (ParseException e) {
            return "Invalid date format (dd/MM/yyyy)";
        }

        return null; // No errors
    }

    /**
     * Get user avatar path
     */
    public String getUserAvatarPath() {
        return sessionManager.getPathFileAvatarUser();
    }

    /**
     * Get user avatar URL
     */
    public String getUserAvatarUrl() {
        return sessionManager.getUserAvatar();
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
    public LiveData<UserDetail> getUserDetail() {
        return userDetailLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}