package com.example.chatapp.utils.token;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.chatapp.activities.SignInActivity;
import com.example.chatapp.models.TokenClient;
import com.example.chatapp.network.HttpClient;
import com.example.chatapp.utils.Utils;
import com.google.gson.JsonObject;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import java.util.Date;


public class TokenUtils {
    // variables
    private final Context context;
    private final HttpClient httpClient;
    private final ITokenStorage tokenStorage;
    private final String TAG = "TokenUtils";

    // constructor
    public TokenUtils(Context context) {
        this.context = context;
        this.httpClient = new HttpClient();
        this.tokenStorage = new TokenStorageImpl(context);
    }

    // Hàm xử lý đồng bộ
    public void executeRequestWithTokenRefresh(String accessToken, String refreshToken, Runnable requestTask) {
        if (!isTokenExpired(accessToken)) {
            requestTask.run();
        } else if (isTokenExpired(accessToken) && !isTokenExpired(refreshToken)) {
            httpClient.refreshToken(accessToken, refreshToken).thenAccept(tokenClient -> {
                TokenClient tokenClientObj = parseTokenRefreshResponse(tokenClient);
                if (tokenClientObj == null) {
                    Log.e(TAG, "Token refresh failed");
                    changeToSignInActivity();
                    return;
                }
                // Cập nhật token mới
                String newAccessToken = tokenClientObj.getAccessToken();
                String newRefreshToken = tokenClientObj.getRefreshToken();
                // Save token in preference
                saveTokenInPreference(newAccessToken, newRefreshToken);

                // Thực hiện nhiệm vụ yêu cầu sau khi làm mới token thành công
                requestTask.run();
            }).exceptionally(throwable -> {
                Log.e(TAG, "Token refresh failed", throwable);
                return null;
            });
        } else {
            // Thông báo hết phiên đăng nhập và chuyển hướng đến LoginActivity
            changeToSignInActivity();
        }
    }

    // Save token in preference
    private void saveTokenInPreference(String accessToken, String refreshToken) {
        tokenStorage.setAccessToken(accessToken);
        tokenStorage.setRefreshToken(refreshToken);
    }

    // Helper change to sign in activity
    private void changeToSignInActivity() {
        Toast.makeText(context, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, SignInActivity.class);
        context.startActivity(intent);
    }

    // Helper parse token
    private TokenClient parseTokenRefreshResponse(JsonObject jo) {
        int resCode = 0;
        if (jo.has("code")) {
            resCode = jo.get("code").getAsInt();
        }
        if (resCode != Utils.ErrCodeSuccess) {
            return null;
        }
        JsonObject data = jo.get("data").getAsJsonObject();
        String accessToken = data.get("token").getAsString();
        String refreshToken = data.get("refresh_token").getAsString();

        return new TokenClient(
                accessToken,
                refreshToken
        );
    }

    // Kiểm tra xem token đã hết hạn hay chưa
    public boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\."); // Tách token thành 3 phần
            if (parts.length != 3) {
//                throw new IllegalArgumentException("Invalid JWT token format");
                return true;
            }

            String payload = new String(Base64.decodeBase64(parts[1])); // Giải mã payload
            JSONObject jsonObject = new JSONObject(payload);

            long exp = jsonObject.getLong("exp"); // Lấy giá trị "exp" từ payload
            Date expiration = new Date(exp * 1000); // Chuyển đổi giây sang milliseconds

            return expiration.before(new Date());
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Nếu có lỗi, coi như token đã hết hạn
        }
    }

    // getter and setter
    public ITokenStorage getTokenStorage() {
        return tokenStorage;
    }
}