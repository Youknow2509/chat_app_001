package com.example.chatapp.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "token_client")
public class TokenClient {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id_token")
    private String IdToken;

    @ColumnInfo(name = "access_token")
    private String AccessToken;

    @ColumnInfo(name = "refresh_token")
    private String RefreshToken;

    @ColumnInfo(name = "created_at")
    private long CreatedAt;

    @ColumnInfo(name = "updated_at")
    private long UpdatedAt;

    // Default constructor (required for Room)
    public TokenClient() {
    }

    // Constructor without timestamps (auto-set)
    public TokenClient(@NonNull String idToken, String accessToken, String refreshToken) {
        this.IdToken = idToken;
        this.AccessToken = accessToken;
        this.RefreshToken = refreshToken;
        this.CreatedAt = System.currentTimeMillis(); // Initialize timestamp
        this.UpdatedAt = this.CreatedAt;
    }

    // Full constructor
    public TokenClient(@NonNull String idToken, String accessToken, String refreshToken, long createdAt, long updatedAt) {
        this.IdToken = idToken;
        this.AccessToken = accessToken;
        this.RefreshToken = refreshToken;
        this.CreatedAt = createdAt;
        this.UpdatedAt = updatedAt;
    }

    // Update timestamp when modifying the object
    public void updateTimestamps() {
        this.UpdatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "TokenClient{" +
                "IdToken='" + IdToken + '\'' +
                ", AccessToken='" + AccessToken + '\'' +
                ", RefreshToken='" + RefreshToken + '\'' +
                ", CreatedAt=" + CreatedAt +
                ", UpdatedAt=" + UpdatedAt +
                '}';
    }

    // Getters and Setters
    @NonNull
    public String getIdToken() {
        return IdToken;
    }

    public void setIdToken(@NonNull String idToken) {
        this.IdToken = idToken;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public void setAccessToken(String accessToken) {
        this.AccessToken = accessToken;
        updateTimestamps(); // Update timestamp on change
    }

    public String getRefreshToken() {
        return RefreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.RefreshToken = refreshToken;
        updateTimestamps(); // Update timestamp on change
    }

    public long getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(long createdAt) {
        this.CreatedAt = createdAt;
    }

    public long getUpdatedAt() {
        return UpdatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.UpdatedAt = updatedAt;
    }
}
