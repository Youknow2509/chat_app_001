package com.example.chatapp.api;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.dto.UserFbToken;
import com.example.chatapp.models.request.AccountModels.*;
import com.example.chatapp.models.request.ChatModels.*;
import com.example.chatapp.models.request.TokenModels.*;
import com.example.chatapp.models.request.UserModels.*;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.Utils;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class ApiManager {
    private final String TOKEN_PREFIX;
    private final ChatAppService apiService;
    private final CloudinaryService apiCloudinaryService;

    public ApiManager() {
        TOKEN_PREFIX = Constants.TOKEN_PREFIX_REQUEST;
        apiService = RetrofitClient.getInstance().getService();
        apiCloudinaryService = RetrofitClient.getInstance().getCloudinaryService();
    }

    private String formatToken(String token) {
        if (token != null && !token.startsWith(TOKEN_PREFIX)) {
            return TOKEN_PREFIX + token;
        }
        return token;
    }
    // ======== Cloudinary Management ========
    public void getSignatur(String token, Map<String, Object> config, Callback<ResponseData<Object>> callback) {
        String tokenWithPrefix = Utils.formatToken(token);
        String configUrl = Utils.getConfigUrl(config);
        Call<ResponseData<Object>> call = apiCloudinaryService.getSignatur(tokenWithPrefix, configUrl);
        call.enqueue(callback);
    }

    // ======== Account Management ========
    public void upgradeNameAndAvatarRegister(UpgradeNameAndAvatarRegisterInput input, Callback<ResponseData<Object>> callback){
        Call<ResponseData<Object>> call = apiService.upgradeNameAndAvatarRegister(input);
        call.enqueue(callback);
    }

    public void registerUser(RegisterInput input, Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.registerUser(input);
        call.enqueue(callback);
    }

    public void verifyAccount(VerifyInput input, Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.verifyAccount(input);
        call.enqueue(callback);
    }

    public void upgradePasswordRegister(UpdatePasswordInput input, Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.upgradePasswordRegister(input);
        call.enqueue(callback);
    }

    public void login(String userAccount, String userPassword, Callback<ResponseData<Object>> callback) {
        LoginInput input = new LoginInput(userAccount, userPassword);
        Call<ResponseData<Object>> call = apiService.login(input);
        call.enqueue(callback);
    }

    public void refreshToken(String accessToken, String refreshToken, Callback<ResponseData<Object>> callback) {
        RefreshTokenInput input = new RefreshTokenInput(accessToken, refreshToken);
        Call<ResponseData<Object>> call = apiService.refreshToken(input);
        call.enqueue(callback);
    }

    public void logout(String accessToken, String refreshToken, Callback<ResponseData<Object>> callback) {
        RefreshTokenInput input = new RefreshTokenInput(accessToken, refreshToken);
        Call<ResponseData<Object>> call = apiService.logout(input);
        call.enqueue(callback);
    }

    public void forgotPassword(String email, Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.forgotPassword(email);
        call.enqueue(callback);
    }

    // ======== User Management ========

    public void getUserInfo(String token, Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.getUserInfo(formatToken(token));
        call.enqueue(callback);
    }

    public void updateUserInfo(String token, String userId, String nickname, String avatar,
                               Callback<ResponseData<Object>> callback) {
        UpdateUserInfoInput input = new UpdateUserInfoInput(userId, nickname, avatar);
        Call<ResponseData<Object>> call = apiService.updateUserInfo(formatToken(token), input);
        call.enqueue(callback);
    }

    public void updatePassword(String token, String userId, String oldPassword, String newPassword,
                               Callback<ResponseData<Object>> callback) {
        UserChangePasswordInput input = new UserChangePasswordInput(userId, oldPassword, newPassword);
        Call<ResponseData<Object>> call = apiService.updatePassword(formatToken(token), input);
        call.enqueue(callback);
    }

    public void findUser(String token, String email, Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.findUser(formatToken(token), email);
        call.enqueue(callback);
    }

    // ======== Friend Management ========

    public void createFriendRequest(String token, String userId, String friendEmail,
                                    Callback<ResponseData<Object>> callback) {
        CreateFriendRequestInput input = new CreateFriendRequestInput(userId, friendEmail);
        Call<ResponseData<Object>> call = apiService.createFriendRequest(formatToken(token), input);
        call.enqueue(callback);
    }

    public void getListFriendRequest(String token, int limit, int page,
                                     Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.getListFriendRequest(formatToken(token), limit, page);
        call.enqueue(callback);
    }

    public void acceptFriendRequest(String token, String requestId, String userAcceptId,
                                    Callback<ResponseData<Object>> callback) {
        AcceptFriendRequestInput input = new AcceptFriendRequestInput(requestId, userAcceptId);
        Call<ResponseData<Object>> call = apiService.acceptFriendRequest(formatToken(token), input);
        call.enqueue(callback);
    }

    public void rejectFriendRequest(String token, String requestId, String userAcceptId,
                                    Callback<ResponseData<Object>> callback) {
        RejectFriendRequestInput input = new RejectFriendRequestInput(requestId, userAcceptId);
        Call<ResponseData<Object>> call = apiService.rejectFriendRequest(formatToken(token), input);
        call.enqueue(callback);
    }

    public void endFriendRequest(String token, String requestId, String userId,
                                 Callback<ResponseData<Object>> callback) {
        EndFriendRequestInput input = new EndFriendRequestInput(requestId, userId);
        Call<ResponseData<Object>> call = apiService.endFriendRequest(formatToken(token), input);
        call.enqueue(callback);
    }

    public void deleteFriend(String token, String userId, String friendEmail,
                             Callback<ResponseData<Object>> callback) {
        DeleteFriendInput input = new DeleteFriendInput(userId, friendEmail);
        Call<ResponseData<Object>> call = apiService.deleteFriend(formatToken(token), input);
        call.enqueue(callback);
    }

    // ======== Chat Management ========

    public void createChatPrivate(String token, String user1, String user2,
                                  Callback<ResponseData<Object>> callback) {
        CreateChatPrivateInput input = new CreateChatPrivateInput(user1, user2);
        Call<ResponseData<Object>> call = apiService.createChatPrivate(formatToken(token), input);
        call.enqueue(callback);
    }

    public void createChatGroup(String token, String userId, String groupName, List<String> members,
                                Callback<ResponseData<Object>> callback) {
        CreateChatGroupInput input = new CreateChatGroupInput(userId, groupName, members);
        Call<ResponseData<Object>> call = apiService.createChatGroup(formatToken(token), input);
        call.enqueue(callback);
    }

    public void getChatInfo(String token, String chatId, Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.getChatInfo(formatToken(token), chatId);
        call.enqueue(callback);
    }

    public void upgradeChatInfo(String token, String adminId, String chatId,
                                String newName, String newAvatar,
                                Callback<ResponseData<Object>> callback) {
        UpgradeChatInfoInput input = new UpgradeChatInfoInput(adminId, chatId, newName, newAvatar);
        Call<ResponseData<Object>> call = apiService.upgradeChatInfo(formatToken(token), input);
        call.enqueue(callback);
    }

    public void addMemberToChat(String token, String adminId, String chatId,
                                String userAddId, Callback<ResponseData<Object>> callback) {
        AddMemberToChatInput input = new AddMemberToChatInput(adminId, chatId, userAddId);
        Call<ResponseData<Object>> call = apiService.addMemberToChat(formatToken(token), input);
        call.enqueue(callback);
    }

    public void deleteMemberFromChat(String token, String adminId, String chatId,
                                     String userDelId, Callback<ResponseData<Object>> callback) {
        DelMenForChatInput input = new DelMenForChatInput(adminId, chatId, userDelId);
        Call<ResponseData<Object>> call = apiService.deleteMemberFromChat(formatToken(token), input);
        call.enqueue(callback);
    }

    public void changeAdminGroupChat(String token, String oldAdminId, String chatId,
                                     String newAdminId, Callback<ResponseData<Object>> callback) {
        ChangeAdminGroupChatInput input = new ChangeAdminGroupChatInput(oldAdminId, chatId, newAdminId);
        Call<ResponseData<Object>> call = apiService.changeAdminGroupChat(formatToken(token), input);
        call.enqueue(callback);
    }

    public void deleteChat(String token, String adminId, String chatId,
                           Callback<ResponseData<Object>> callback) {
        DelChatInput input = new DelChatInput(adminId, chatId);
        Call<ResponseData<Object>> call = apiService.deleteChat(formatToken(token), input);
        call.enqueue(callback);
    }

    public void getListChatForUser(String token, int limit, int page,
                                   Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.getListChatForUser(formatToken(token), limit, page);
        call.enqueue(callback);
    }

    public void getUserInChat(String token, String chatId, int limit, int page,
                              Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.getUserInChat(formatToken(token), chatId, limit, page);
        call.enqueue(callback);
    }

    // ======== Token Management ========

    public void createToken(String data, Callback<ResponseData<Object>> callback) {
        JwtInput input = new JwtInput(data);
        Call<ResponseData<Object>> call = apiService.createToken(input);
        call.enqueue(callback);
    }

    public void createRefreshToken(String data, Callback<ResponseData<Object>> callback) {
        JwtInput input = new JwtInput(data);
        Call<ResponseData<Object>> call = apiService.createRefreshToken(input);
        call.enqueue(callback);
    }

    public void validateToken(String data, Callback<ResponseData<Object>> callback) {
        JwtInput input = new JwtInput(data);
        Call<ResponseData<Object>> call = apiService.validateToken(input);
        call.enqueue(callback);
    }

    // ======== Microservice ========

    public void getMicroserviceUserInChat(String token, String chatId, int limit, int page,
                                          Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.getMicroserviceUserInChat(
                formatToken(token), chatId, limit, page);
        call.enqueue(callback);
    }

    // ======== Notification Management ========
    public void sendToken(UserFbToken userFbToken, Callback<ResponseData<Object>> callback) {
        Call<ResponseData<Object>> call = apiService.sendToken(userFbToken);
        call.enqueue(callback);
    }
}