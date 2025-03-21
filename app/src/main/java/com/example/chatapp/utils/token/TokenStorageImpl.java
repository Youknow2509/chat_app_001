package com.example.chatapp.utils.token;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.chatapp.consts.Constants;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenStorageImpl implements ITokenStorage {

    private SharedPreferences encryptedSharedPreferences;

    public TokenStorageImpl(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            encryptedSharedPreferences = EncryptedSharedPreferences
                    .create(
                            Constants.SHARED_PREFS_FILE_TOKEN,
                            masterKeyAlias,
                            context,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAccessToken(String accessToken) {
        SharedPreferences.Editor editor = encryptedSharedPreferences.edit();
        editor.putString(Constants.ACCESS_TOKEN_KEY_PREF, accessToken);
        editor.apply();
    }

    @Override
    public String getAccessToken() {
        return encryptedSharedPreferences.getString(Constants.ACCESS_TOKEN_KEY_PREF, null);
    }

    @Override
    public void setRefreshToken(String refreshToken) {
        SharedPreferences.Editor editor = encryptedSharedPreferences.edit();
        editor.putString(Constants.REFRESH_TOKEN_KEY_PREF, refreshToken);
        editor.apply();
    }

    @Override
    public String getRefreshToken() {
        return encryptedSharedPreferences.getString(Constants.REFRESH_TOKEN_KEY_PREF, null);
    }
}
