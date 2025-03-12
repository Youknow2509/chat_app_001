package com.example.chatapp.reponsive;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.chatapp.database.AppDatabase;
import com.example.chatapp.models.TokenClient;
import com.example.chatapp.dao.TokenClientDao;

public class TokenClientResponsive {

    private TokenClientDao tokenClientDao;

    public TokenClientResponsive(Context context) {
        // Initialize the DAO using the database instance
        AppDatabase db = AppDatabase.getInstance(context);
        tokenClientDao = db.tokenClientDao();
    }

    // Get the latest token from the database
    public LiveData<TokenClient> getToken() {
        return tokenClientDao.getTokenLiveData();
    }

    // Get token by id from the database
    public LiveData<TokenClient> getTokenById(String id) {
        return tokenClientDao.getTokenByIdLiveData(id);
    }

    // Update token information in the database
    public void updateToken(String accessToken, String refreshToken, long updatedAt) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            tokenClientDao.updateToken(accessToken, refreshToken, updatedAt);
        });
    }

    // Insert a new token into the database
    public void insertToken(TokenClient token) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            tokenClientDao.insertToken(token);
        });
    }

    // Delete the token from the database
    public void deleteToken() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            tokenClientDao.deleteToken();
        });
    }
}
