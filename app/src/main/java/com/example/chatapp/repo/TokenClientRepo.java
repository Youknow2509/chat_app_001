package com.example.chatapp.repo;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.chatapp.database.AppDatabase;
import com.example.chatapp.models.ResponRepo;
import com.example.chatapp.models.TokenClient;
import com.example.chatapp.dao.TokenClientDao;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TokenClientRepo {

    private TokenClientDao tokenClientDao;
    private final String TAG = "TokenClientRepo";

    public TokenClientRepo(Context context) {
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
    public ResponRepo updateToken(String accessToken, String refreshToken, long updatedAt) {
        ResponRepo responRepo = new ResponRepo();
        //        AppDatabase.databaseWriteExecutor.execute(() -> {
//            tokenClientDao.updateToken(accessToken, refreshToken, updatedAt);
//        });
        return responRepo;
    }

    // Insert a new token into the database
    public ResponRepo insertToken(TokenClient token) {
        ResponRepo responRepo = new ResponRepo();

        ResponRepo delRepo = deleteToken();
        if (!delRepo.isStatus()) {
            responRepo.setStatus(false);
            responRepo.setMessage("delete all token failed");
            responRepo.setData(delRepo.getData());
            return responRepo;
        }

        Future<?> future = Executors.newSingleThreadExecutor().submit(() -> {
            tokenClientDao.insertToken(token);
        });
        try {
            future.get();
            Log.d(TAG, "insert success: " + token.toString());
            responRepo.setStatus(true);
            responRepo.setMessage("insert success");
        } catch (Exception e) {
            Log.d(TAG, "insert failed");
            responRepo.setStatus(false);
            responRepo.setMessage("insert failed");
            responRepo.setData(e);
        }
        return responRepo;
    }

    // Delete the token from the database
    public ResponRepo deleteToken() {
        ResponRepo responRepo = new ResponRepo();

        Future<?> future = Executors.newSingleThreadExecutor().submit(() ->
                tokenClientDao.deleteToken()
        );
        try {
            future.get();
            Log.d(TAG, "delete all token success");
            responRepo.setStatus(true);
            responRepo.setMessage("delete all token success");
        } catch (Exception e) {
            Log.d(TAG, "delete all token failed");
            responRepo.setStatus(false);
            responRepo.setMessage("delete all token failed");
            responRepo.setData(e);
        }

        return responRepo;
    }
}
