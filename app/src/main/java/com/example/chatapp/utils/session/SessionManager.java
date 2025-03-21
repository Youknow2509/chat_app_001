package com.example.chatapp.utils.session;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public class SessionManager {
    private static final String PREF_NAME = "ChatAppPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ACCESS_TOKEN_EXPIRY = "access_token_expiry";
    private static final String KEY_REFRESH_TOKEN_EXPIRY = "refresh_token_expiry";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            editor = sharedPreferences.edit();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately in your app
        }
    }

    public void saveAuthData(String accessToken, String refreshToken, String userId) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_USER_ID, userId);

        long accessTokenExpiry = getTokenExpiry(accessToken);
        long refreshTokenExpiry = getTokenExpiry(refreshToken);

        editor.putLong(KEY_ACCESS_TOKEN_EXPIRY, accessTokenExpiry);
        editor.putLong(KEY_REFRESH_TOKEN_EXPIRY, refreshTokenExpiry);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void updateTokens(String accessToken, String refreshToken) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);

        long accessTokenExpiry = getTokenExpiry(accessToken);
        long refreshTokenExpiry = getTokenExpiry(refreshToken);

        editor.putLong(KEY_ACCESS_TOKEN_EXPIRY, accessTokenExpiry);
        editor.putLong(KEY_REFRESH_TOKEN_EXPIRY, refreshTokenExpiry);
        editor.apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public boolean isTokenExpired(String token) {
        long expiryTime = getTokenExpiry(token);
        return System.currentTimeMillis() / 1000 >= expiryTime;
    }

    public boolean isSessionExpired() {
        long accessTokenExpiry = sharedPreferences.getLong(KEY_ACCESS_TOKEN_EXPIRY, 0);
        long refreshTokenExpiry = sharedPreferences.getLong(KEY_REFRESH_TOKEN_EXPIRY, 0);
        long currentTime = System.currentTimeMillis() / 1000;

        return currentTime >= accessTokenExpiry || currentTime >= refreshTokenExpiry;
    }

    public void refreshSession(String newAccessToken, String newRefreshToken) {
        updateTokens(newAccessToken, newRefreshToken);
    }

    private long getTokenExpiry(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return 0;
            }
            String payload = new String(Base64.decodeBase64(parts[1])); // Giải mã payload
            JSONObject jsonObject = new JSONObject(payload);
            return jsonObject.getLong("exp");
        } catch (JSONException | IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean shouldRefreshToken() {
        long accessTokenExpiry = sharedPreferences.getLong(KEY_ACCESS_TOKEN_EXPIRY, 0);
        long currentTime = System.currentTimeMillis() / 1000;

        // Refresh if the access token will expire within the next 5 minutes
        return (accessTokenExpiry - currentTime) <= (5 * 60);
    }
}