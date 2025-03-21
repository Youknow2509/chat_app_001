package com.example.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.chatapp.models.TokenClient;

@Dao
public interface TokenClientDao {

    // Get the latest token from the database
    @Query("SELECT * FROM token_client LIMIT 1")
    TokenClient getTokenData();

    // Get the token by id from the database
    @Query("SELECT * FROM token_client WHERE id_token = :id")
    TokenClient getTokenByIdData(String id);

    // Get the token by id from the database
    @Query("UPDATE token_client SET access_token = :accessToken, refresh_token = :refreshToken, updated_at = :updatedAt")
    void updateToken(String accessToken, String refreshToken, long updatedAt);

    // delete the token from the database
    @Query("DELETE FROM token_client")
    void deleteToken();

    // insert the token into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertToken(TokenClient token);
}

