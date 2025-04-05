package com.example.chatapp.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.file.MediaUtils;
import com.example.chatapp.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    private MutableLiveData<UserProfileSession> userProfileLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private MutableLiveData<String> accessTokenLiveData = new MutableLiveData<>();
    private MutableLiveData<String> refreshTokenLiveData = new MutableLiveData<>();
    private MutableLiveData<String> resMessageForgotPassword = new MutableLiveData<>();
    private MutableLiveData<String> resPathFileAvatar = new MutableLiveData<>();
    private final String TAG = "LoginViewModel";
    //
    private ApiManager apiManager;
    private MediaUtils mediaUtils;
    private Context context;

    // Constructor
    public LoginViewModel(@NonNull Application application) {
        super(application);
        apiManager = new ApiManager();
        mediaUtils = new MediaUtils();
        context = getApplication().getApplicationContext();
    }

    public LiveData<String> getResPathFileAvatar() {
        return resPathFileAvatar;
    }

    public LiveData<String> getResMessageForgotPassword() {
        return resMessageForgotPassword;
    }

    public LiveData<UserProfileSession> getUserProfileLiveData() {
        return userProfileLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void saveAvatarUser(String urlAvatar) {
        MediaUtils.getMediaFromHost(
                context,
                urlAvatar,
                userProfileLiveData.getValue().getAccessToken()
        ).thenAccept(file -> {
            if (file == null) {
                Log.e(TAG, "Error saving avatar");
                errorMessageLiveData.postValue("Error saving avatar");
            } else {
                String pathFile = file.getAbsolutePath();
                // Kiểm tra pathFile
                if (pathFile != null && !pathFile.isEmpty()) {
                    Log.e(TAG, "Path avatar: " + pathFile);
                    resPathFileAvatar.postValue(pathFile);
                } else {
                    Log.e(TAG, "Avatar path is empty");
                    errorMessageLiveData.postValue("Error: Empty path");
                }
            }
        });

    }

    public void signIn(String email, String password) {
        apiManager.login(email, password, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                if (response.body() == null) {
                    Log.e(TAG, "Login failed: Response body is null");
                    errorMessageLiveData.setValue("Login failed: Response body is null");
                    return;
                }
                if (response.body().getCode() == Constants.CODE_SUCCESS) {
                    String accessToken = Utils.getDataBody(response.body(), "token");
                    String refreshToken = Utils.getDataBody(response.body(), "refresh_token");
                    Log.d(TAG, "Login success: " + accessToken);
                    accessTokenLiveData.setValue(accessToken);
                    refreshTokenLiveData.setValue(refreshToken);
                    getUserInfo(accessToken);
                } else {
                    Log.e(TAG, "Login failed: " + response.body().getMessage());
                    errorMessageLiveData.setValue("Login failed: " + response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                Log.d(TAG, "Network error! Please try again.");
                errorMessageLiveData.setValue("Network error! Please try again.");
            }
        });
    }

    private void getUserInfo(String token) {
        apiManager.getUserInfo(token, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                if (response.body() == null) {
                    Log.e(TAG, "Error getting user info: Response body is null");
                    errorMessageLiveData.setValue("Error getting user info: Response body is null");
                    return;
                }
                if (response.body().getCode() == Constants.CODE_SUCCESS) {
                    UserProfileSession user = new UserProfileSession();
                    // Set user profile data
                    user.setId(Utils.getDataBody(response.body(), "user_id"));
                    user.setName(Utils.getDataBody(response.body(), "user_nickname"));
                    user.setEmail(Utils.getDataBody(response.body(), "user_email"));
                    user.setAvatarUrl(Utils.getDataBody(response.body(), "user_avatar"));
                    user.setDisplayName(Utils.getDataBody(response.body(), "user_nickname"));
                    user.setUserGender(Utils.getDataBody(response.body(), "user_gender"));
                    user.setDateOfBirth(Utils.parseBirthday(Utils.getDataBody(response.body(), "user_birthday")));
                    //
                    user.setAccessToken(accessTokenLiveData.getValue());
                    user.setRefreshToken(refreshTokenLiveData.getValue());
                    //
                    Log.d(TAG, "Get user info success: " + user.getName());
                    userProfileLiveData.setValue(user);
                } else {
                    Log.e(TAG, "Error getting user info: " + response.body().getMessage());
                    errorMessageLiveData.setValue("Error getting user info: " + response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                Log.e(TAG, "Network error! Please try again.");
                errorMessageLiveData.setValue("Network error! Please try again.");
            }
        });
    }

    public void forgotPassword(String email) {
        apiManager.forgotPassword(email, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                if (response.body() == null) {
                    Log.e(TAG, "Forgot password failed: Response body is null");
                    errorMessageLiveData.setValue("Forgot password failed: Response body is null");
                    return;
                }
                if (response.body().getCode() == Constants.CODE_SUCCESS) {
                    Log.d(TAG, "Forgot password success: " + response.body().getMessage());
                    resMessageForgotPassword.setValue("Password reset request sent to your email!");
                } else {
                    Log.e(TAG, "Forgot password failed: " + response.body().getMessage());
                    resMessageForgotPassword.setValue("Forgot password failed: " + response.body().getMessage());
                }

            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                errorMessageLiveData.setValue("Network error! Please try again.");
            }
        });
    }
}
