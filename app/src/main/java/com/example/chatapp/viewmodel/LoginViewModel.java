package com.example.chatapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<UserProfileSession> userProfileLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private MutableLiveData<String> accessTokenLiveData = new MutableLiveData<>();
    private MutableLiveData<String> refreshTokenLiveData = new MutableLiveData<>();
    private MutableLiveData<String> resMessageForgotPassword = new MutableLiveData<>();
    private ApiManager apiManager;

    public LoginViewModel() {
        apiManager = new ApiManager();
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

    public void signIn(String email, String password) {
        apiManager.login(email, password, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                if (response.body().getCode() == Constants.CODE_SUCCESS) {
                    String accessToken = Utils.getDataBody(response.body(), "token");
                    String refreshToken = Utils.getDataBody(response.body(), "refresh_token");
                    accessTokenLiveData.setValue(accessToken);
                    refreshTokenLiveData.setValue(refreshToken);

                    getUserInfo(accessToken);
                } else {
                    errorMessageLiveData.setValue("Login failed: " + response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                errorMessageLiveData.setValue("Network error! Please try again.");
            }
        });
    }

    private void getUserInfo(String token) {
        apiManager.getUserInfo(token, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
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
                    userProfileLiveData.setValue(user);
                } else {
                    errorMessageLiveData.setValue("Error getting user info: " + response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                errorMessageLiveData.setValue("Network error! Please try again.");
            }
        });
    }

    public void forgotPassword(String email) {
        apiManager.forgotPassword(email, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                if (response.body().getCode() == Constants.CODE_SUCCESS) {
                    resMessageForgotPassword.setValue("Password reset request sent to your email!");
                } else {
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
