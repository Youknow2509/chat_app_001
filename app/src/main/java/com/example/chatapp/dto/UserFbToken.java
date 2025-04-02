package com.example.chatapp.dto;

public class UserFbToken {
    String userId;
    String token;
    String notiPermission;
    boolean status;

    public UserFbToken(String userId, String token, String notiPermission, boolean status) {
        this.userId = userId;
        this.token = token;
        this.notiPermission = notiPermission;
        this.status = status;
    }
}
