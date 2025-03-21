package com.example.chatapp.utils.token;

public interface ITokenStorage {
    void setAccessToken(String accessToken);

    String getAccessToken();

    void setRefreshToken(String refreshToken);

    String getRefreshToken();

}
