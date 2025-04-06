package com.example.chatapp.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.dto.UserFbToken;
import com.example.chatapp.models.request.AccountModels.*;
import com.example.chatapp.models.request.ChatModels.*;
import com.example.chatapp.models.request.TokenModels.*;
import com.example.chatapp.models.request.UserModels.*;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.network.NetworkConnectionInterceptor;
import com.example.chatapp.service.TokenRefreshService;
import com.example.chatapp.utils.Utils;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiManager {
    private final String TOKEN_PREFIX;
    private final ChatAppService apiService;
    private final CloudinaryService apiCloudinaryService;
    private final Context context;
    private final RetrofitClient retrofitClient;

    public ApiManager(Context context) {
        this.context = context;
        TOKEN_PREFIX = Constants.TOKEN_PREFIX_REQUEST;

        // Khởi tạo RetrofitClient với context
        retrofitClient = RetrofitClient.getInstance(context);
        apiService = retrofitClient.getService();
        apiCloudinaryService = retrofitClient.getCloudinaryService();
    }

    private <T> Callback<T> wrapCallback(final Call<T> call, final Callback<T> originalCallback) {
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.code() == 401) {
                    try {
                        ResponseBody errorBody = response.errorBody();
                        if (errorBody != null) {
                            String errorBodyString = errorBody.string();
                            JSONObject errorJson = new JSONObject(errorBodyString);
                            int errorCode = errorJson.optInt("code", 0);

                            if (errorCode == 40002) {
                                // Token không hợp lệ, khởi động service để refresh token
                                Intent intent = new Intent(context, TokenRefreshService.class);
                                intent.setAction("com.example.chatapp.FORCE_REFRESH_TOKEN");
                                context.startService(intent);

                                originalCallback.onFailure(call, new Exception("Token expired, refresh initiated"));
                                return;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("APIManager", "Error parsing error response", e);
                    }
                }

                originalCallback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                originalCallback.onFailure(call, t);
            }
        };
    }

    private String formatToken(String token) {
        if (token != null && !token.startsWith(TOKEN_PREFIX)) {
            return TOKEN_PREFIX + token;
        }
        return token;
    }

    /**
     * Kiểm tra kết nối mạng trước khi thực hiện API call
     * @return true nếu có kết nối mạng, false nếu không
     */
    private boolean checkNetworkConnection() {
        if (!retrofitClient.isNetworkAvailable()) {
            Toast.makeText(context, "Không có kết nối internet", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ======== Cloudinary Management ========
    public void getSignatur(String token, Map<String, Object> config, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        String tokenWithPrefix = Utils.formatToken(token);
        String configUrl = Utils.getConfigUrl(config);
        Call<ResponseData<Object>> call = apiCloudinaryService.getSignatur(tokenWithPrefix, configUrl);
        call.enqueue(wrapCallback(call, callback));

    }

    // ======== Account Management ========
    public void upgradeNameAndAvatarRegister(UpgradeNameAndAvatarRegisterInput input, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.upgradeNameAndAvatarRegister(input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void registerUser(RegisterInput input, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.registerUser(input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void verifyAccount(VerifyInput input, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.verifyAccount(input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void upgradePasswordRegister(UpdatePasswordInput input, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.upgradePasswordRegister(input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void login(String userAccount, String userPassword, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        LoginInput input = new LoginInput(userAccount, userPassword);
        Call<ResponseData<Object>> call = apiService.login(input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void refreshToken(String accessToken, String refreshToken, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        RefreshTokenInput input = new RefreshTokenInput(accessToken, refreshToken);
        Call<ResponseData<Object>> call = apiService.refreshToken(input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void logout(String accessToken, String refreshToken, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        RefreshTokenInput input = new RefreshTokenInput(accessToken, refreshToken);
        Call<ResponseData<Object>> call = apiService.logout(input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void forgotPassword(String email, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.forgotPassword(email);
        call.enqueue(wrapCallback(call, callback));

    }

    // ======== User Management ========

    public void getUserInfo(String token, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.getUserInfo(formatToken(token));
        call.enqueue(wrapCallback(call, callback));

    }

    public void updateUserInfo(String token, UpdateUserInfoInput input, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.updateUserInfo(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void updatePassword(String token, String userId, String oldPassword, String newPassword, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        UserChangePasswordInput input = new UserChangePasswordInput(userId, oldPassword, newPassword);
        Call<ResponseData<Object>> call = apiService.updatePassword(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void findUser(String token, String email, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.findUser(formatToken(token), email);
        call.enqueue(wrapCallback(call, callback));

    }

    // ======== Friend Management ========

    public void createFriendRequest(String token, String userId, String friendEmail, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        CreateFriendRequestInput input = new CreateFriendRequestInput(userId, friendEmail);
        Call<ResponseData<Object>> call = apiService.createFriendRequest(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void getListFriendRequest(String token, int limit, int page, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.getListFriendRequest(formatToken(token), limit, page);
        call.enqueue(wrapCallback(call, callback));

    }

    public void acceptFriendRequest(String token, String requestId, String userAcceptId, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        AcceptFriendRequestInput input = new AcceptFriendRequestInput(requestId, userAcceptId);
        Call<ResponseData<Object>> call = apiService.acceptFriendRequest(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void rejectFriendRequest(String token, String requestId, String userAcceptId, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        RejectFriendRequestInput input = new RejectFriendRequestInput(requestId, userAcceptId);
        Call<ResponseData<Object>> call = apiService.rejectFriendRequest(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void endFriendRequest(String token, String requestId, String userId, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        EndFriendRequestInput input = new EndFriendRequestInput(requestId, userId);
        Call<ResponseData<Object>> call = apiService.endFriendRequest(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void deleteFriend(String token, String userId, String friendEmail, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        DeleteFriendInput input = new DeleteFriendInput(userId, friendEmail);
        Call<ResponseData<Object>> call = apiService.deleteFriend(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    // ======== Chat Management ========

    public void createChatPrivate(String token, String user1, String user2, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        CreateChatPrivateInput input = new CreateChatPrivateInput(user1, user2);
        Call<ResponseData<Object>> call = apiService.createChatPrivate(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void createChatGroup(String token, String userId, String groupName, List<String> members, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        CreateChatGroupInput input = new CreateChatGroupInput(userId, groupName, members);
        Call<ResponseData<Object>> call = apiService.createChatGroup(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void getChatInfo(String token, String chatId, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.getChatInfo(formatToken(token), chatId);
        call.enqueue(wrapCallback(call, callback));

    }

    public void upgradeChatInfo(String token, String adminId, String chatId, String newName, String newAvatar, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        UpgradeChatInfoInput input = new UpgradeChatInfoInput(adminId, chatId, newName, newAvatar);
        Call<ResponseData<Object>> call = apiService.upgradeChatInfo(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void addMemberToChat(String token, String adminId, String chatId, String userAddId, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        AddMemberToChatInput input = new AddMemberToChatInput(adminId, chatId, userAddId);
        Call<ResponseData<Object>> call = apiService.addMemberToChat(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void deleteMemberFromChat(String token, String adminId, String chatId, String userDelId, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        DelMenForChatInput input = new DelMenForChatInput(adminId, chatId, userDelId);
        Call<ResponseData<Object>> call = apiService.deleteMemberFromChat(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void changeAdminGroupChat(String token, String oldAdminId, String chatId, String newAdminId, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        ChangeAdminGroupChatInput input = new ChangeAdminGroupChatInput(oldAdminId, chatId, newAdminId);
        Call<ResponseData<Object>> call = apiService.changeAdminGroupChat(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void deleteChat(String token, String adminId, String chatId, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        DelChatInput input = new DelChatInput(adminId, chatId);
        Call<ResponseData<Object>> call = apiService.deleteChat(formatToken(token), input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void getListChatForUser(String token, int limit, int page, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.getListChatForUser(formatToken(token), limit, page);
        call.enqueue(wrapCallback(call, callback));

    }

    public void getListChatPrivateForUser(String token, int limit, int page, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.getListChatPrivateForUser(formatToken(token), limit, page);
        call.enqueue(wrapCallback(call, callback));

    }

    public void getUserInChat(String token, String chatId, int limit, int page, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.getUserInChat(formatToken(token), chatId, limit, page);
        call.enqueue(wrapCallback(call, callback));

    }

    // ======== Token Management ========

    public void createToken(String data, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        JwtInput input = new JwtInput(data);
        Call<ResponseData<Object>> call = apiService.createToken(input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void createRefreshToken(String data, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        JwtInput input = new JwtInput(data);
        Call<ResponseData<Object>> call = apiService.createRefreshToken(input);
        call.enqueue(wrapCallback(call, callback));

    }

    public void validateToken(String data, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        JwtInput input = new JwtInput(data);
        Call<ResponseData<Object>> call = apiService.validateToken(input);
        call.enqueue(wrapCallback(call, callback));

    }

    // ======== Microservice ========

    public void getMicroserviceUserInChat(String token, String chatId, int limit, int page, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.getMicroserviceUserInChat(formatToken(token), chatId, limit, page);
        call.enqueue(wrapCallback(call, callback));

    }

    // ======== Notification Management ========
    public void sendToken(UserFbToken userFbToken, Callback<ResponseData<Object>> callback) {
        if (!checkNetworkConnection()) return;

        Call<ResponseData<Object>> call = apiService.sendToken(userFbToken);
        call.enqueue(wrapCallback(call, callback));

    }
}