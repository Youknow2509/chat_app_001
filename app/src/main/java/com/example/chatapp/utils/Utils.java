package com.example.chatapp.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.chatapp.models.TokenClient;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.repository.TokenClientRepo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static final int ErrCodeSuccess = 20001;
    public static final int ErrCodeParamInvalid = 20003; // Email is invalid

    public static final int ErrInvalidToken = 30001; // Invalid token
    public static final int ErrInvalidOTP = 30002; // Invalid otp
    public static final int ErrSendEmailOTP = 30003; // Send email failed

    public static final int ErrCodeAuthFailed = 401; // Auth failed

    public static final int ErrCodeUnmarshalData = 40001; // Unmarshal data failed

    public static final int ErrCodeInvalidInput = 40002; // Invalid input
    public static final int ErrCodeBadRequest = 40003; // Bad request

    // Register Code
    public static final int ErrCodeUserHasExist = 50001; // User exists
    public static final int ErrCodeBindRegisterInput = 50002;
    public static final int ErrCodeBindVerifyInput = 50003;
    public static final int ErrCodeVerifyOTPFail = 50004;
    public static final int ErrCodeBindUpdatePasswordInput = 50005;
    public static final int ErrCodeBindLoginInput = 50006;

    // Login Code
    public static final int ErrCodeOTPNotExist = 60001;
    public static final int ErrCodeUserOTPNotExist = 60002;
    public static final int ErrCodeOTPDontVerify = 60003;

    public static final int ErrCodeUpdatePasswordRegister = 100000;

    // Crypto Code
    public static final int ErrCodeCryptoHash = 70001;
    public static final int ErrCodeGeneratorSalt = 70002;

    // Database Code
    public static final int ErrCodeAddUserBase = 80001;
    public static final int ErrCodeQueryUserBase = 80002;
    public static final int ErrCodeUpdateUserBase = 80003;
    public static final int ErrCodeDeleteUserBase = 80004;
    public static final int ErrCodeUserBaseNotFound = 80005;

    public static final int ErrCodeAddUserInfo = 90001;
    public static final int ErrCodeUserNotFound = 90002;
    public static final int ErrCodeDeleteCache = 90003;
    public static final int ErrCodeGetCache = 90004;

    // Two-Factor Authentication Code
    public static final int ErrCodeTwoFactorAuthSetupFailed = 9002;
    public static final int ErrCodeTwoFactorAuthFailed = 9003;
    public static final int ErrCodeUnauthorized = 9004;

    // Rate Limit Code
    public static final int ErrCodeTooManyRequests = 429;

    // Token
    public static final int ErrCodeCreateToken = 100001;
    public static final int ErrCodeCreateRefreshToken = 100002;
    public static final int ErrCodeTokenExpired = 100003;
    public static final int ErrCodeTokenInvalid = 100004;
    public static final int ErrCodeBindTokenInput = 100005;

    // Chat
    public static final int ErrCodeAddMemberToChat = 110001;
    public static final int ErrCodeCreateChatGroup = 110002;
    public static final int ErrCodeCreateChatPrivate = 110003;
    public static final int ErrCodeGetChatInfo = 110004;
    public static final int ErrCodeGetListChat = 110005;
    public static final int ErrCodeGetUserInChat = 110006;
    public static final int ErrCodeGetListChatForUser = 110007;
    public static final int ErrCodeChatPrivateExists = 110008;
    public static final int ErrCodeCreateChatGroupSuccess = 110009;
    public static final int ErrCodeCheckUserInChat = 110010;
    public static final int ErrCodeChangeAdminChat = 110011;
    public static final int ErrCodeDelMenFromChat = 110012;
    public static final int ErrCodeDelChat = 110013;
    public static final int ErrCodeUpgradeChatInfo = 110014;

    // User
    public static final int ErrCodeCheckFriendRequest = 120000;
    public static final int ErrCodeFriendRequestNotFound = 120001;
    public static final int ErrCodeUserBlockAddFriendRequest = 120002;
    public static final int ErrCodeGetUserInfo = 120003;
    public static final int ErrCodeFindUser = 120004;
    public static final int ErrCodeUpdateUserInfo = 120005;
    public static final int ErrCodeCreateFriendRequest = 120006;
    public static final int ErrCodeEndFriendRequest = 120007;
    public static final int ErrCodeDeleteFriend = 120008;
    public static final int ErrCodeGetListFriendRequest = 120009;
    public static final int ErrCodeGetPasswordSalt = 120010;
    public static final int ErrCodePasswordIncorrect = 120011;
    public static final int ErrCodeUpdatePassword = 120012;
    public static final int ErrCodeRefreshTokenFail = 120013;
    public static final int ErrCodeForgotPasswordFail = 120014;

    public static final Map<Integer, String> msg = new HashMap<>();

    static {
        msg.put(ErrCodeForgotPasswordFail, "Forgot password fail");
        msg.put(ErrCodeRefreshTokenFail, "RefreshTokenFail");
        msg.put(ErrCodeGetCache, "get cache failed");
        msg.put(ErrCodeUpdatePassword, "update password failed");
        msg.put(ErrCodePasswordIncorrect, "password incorrect");
        msg.put(ErrCodeGetPasswordSalt, "get password salt failed");
        msg.put(ErrCodeGetListFriendRequest, "get list friend request failed");
        msg.put(ErrCodeDeleteFriend, "delete friend failed");
        msg.put(ErrCodeEndFriendRequest, "best failed");
        msg.put(ErrCodeCreateFriendRequest, "create friend request failed");
        msg.put(ErrCodeUpdateUserInfo, "update user info failed");
        msg.put(ErrCodeFindUser, "find user failed");
        msg.put(ErrCodeBadRequest, "bad request");
        msg.put(ErrCodeGetUserInfo, "get user info failed");
        msg.put(ErrCodeFriendRequestNotFound, "frined request not found");
        msg.put(ErrCodeCheckFriendRequest, "check friend request failed");
        msg.put(ErrCodeUpgradeChatInfo, "upgrade chat info failed");
        msg.put(ErrCodeDelMenFromChat, "delete member from chat failed");
        msg.put(ErrCodeDelChat, "del chat failed");
        msg.put(ErrCodeChangeAdminChat, "change admin chat failed");
        msg.put(ErrCodeInvalidInput, "invalid input");
        msg.put(ErrCodeCheckUserInChat, "check user in chat failed");
        msg.put(ErrCodeCreateChatGroupSuccess, "create chat group success");
        msg.put(ErrCodeChatPrivateExists, "chat private exists");
        msg.put(ErrCodeUnauthorized, "unauthorized");
        msg.put(ErrCodeUnmarshalData, "unmarshal data failed");
        msg.put(ErrCodeDeleteCache, "delete cache failed");
        msg.put(ErrCodeGetListChatForUser, "get list chat for user failed");
        msg.put(ErrCodeGetUserInChat, "get user in chat failed");
        msg.put(ErrCodeGetListChat, "get list chat failed");
        msg.put(ErrCodeGetChatInfo, "get chat info failed");
        msg.put(ErrCodeCreateChatPrivate, "create chat private failed");
        msg.put(ErrCodeCreateChatGroup, "create chat group failed");
        msg.put(ErrCodeAddMemberToChat, "add member to chat failed");
        msg.put(ErrCodeSuccess, "success");
        msg.put(ErrCodeParamInvalid, "email is invalid");
        msg.put(ErrInvalidToken, "token is invalid");
        msg.put(ErrInvalidOTP, "otp is invalid");
        msg.put(ErrSendEmailOTP, "send email otp failed");
        msg.put(ErrCodeUserHasExist, "user has exist");
        msg.put(ErrCodeBindRegisterInput, "bind register input failed");
        msg.put(ErrCodeBindVerifyInput, "bind verify input failed");
        msg.put(ErrCodeVerifyOTPFail, "verify otp failed");
        msg.put(ErrCodeBindUpdatePasswordInput, "bind update password input failed");
        msg.put(ErrCodeOTPNotExist, "otp exists but not registered");
        msg.put(ErrCodeUserOTPNotExist, "user otp does not exist");
        msg.put(ErrCodeOTPDontVerify, "otp does not verify");
        msg.put(ErrCodeCryptoHash, "crypto hash failed");
        msg.put(ErrCodeGeneratorSalt, "generator salt failed");
        msg.put(ErrCodeAddUserBase, "add user base failed");
        msg.put(ErrCodeQueryUserBase, "query user base failed");
        msg.put(ErrCodeUpdateUserBase, "update user base failed");
        msg.put(ErrCodeDeleteUserBase, "delete user base failed");
        msg.put(ErrCodeUserBaseNotFound, "user base not found");
        msg.put(ErrCodeAddUserInfo, "add user info failed");
        msg.put(ErrCodeUpdatePasswordRegister, "update password register failed");
        msg.put(ErrCodeUserNotFound, "user not found");
        msg.put(ErrCodeAuthFailed, "auth failed");
        msg.put(ErrCodeBindLoginInput, "bind login input failed");
        msg.put(ErrCodeTwoFactorAuthSetupFailed, "two factor authentication setup failed");
        msg.put(ErrCodeTwoFactorAuthFailed, "two factor authentication failed");
        msg.put(ErrCodeTooManyRequests, "too many requests");
        msg.put(ErrCodeCreateToken, "create token failed");
        msg.put(ErrCodeCreateRefreshToken, "create refresh token failed");
        msg.put(ErrCodeTokenExpired, "token expired");
        msg.put(ErrCodeTokenInvalid, "token invalid");
        msg.put(ErrCodeBindTokenInput, "bind token input failed");
    }

    // Get message by code
    public static String getMessageByCode(int code) {
        return msg.getOrDefault(code, "Unknown error");
    }

    // Get access token
    public String getAccessToken(Context context) {
        TokenClientRepo repo = new TokenClientRepo(context);
        TokenClient tokenClient = repo.getToken();
        if (tokenClient == null){
            return "";
        }
        String res = tokenClient.getAccessToken();
        return res == null ? "" : res;
    }

    // hideKeyboard
    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    // get data filed data body
    public static String getDataBody(ResponseData<Object> responseData, String field) {
        try {
            Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree(responseData.getData());
            JsonObject dataObject = jsonElement.getAsJsonObject();

            return dataObject.get(field).getAsString();
        } catch (Exception e) {
            Log.e("GetDataBody", "Error parsing response data");
            return "";
        }
    }

    // Format token with prefix if needed
    public static String formatToken(String token) {
        String TOKEN_PREFIX = "Bearer ";
        if (token != null && !token.startsWith(TOKEN_PREFIX)) {
            return TOKEN_PREFIX + token;
        }
        return token;
    }

    // convert config map to config url
    public static String getConfigUrl(Map<String, Object> config) {
        StringBuilder configUrl = new StringBuilder();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            if (configUrl.length() > 0) {
                configUrl.append("&");
            }
            configUrl.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return configUrl.toString();
    }
}