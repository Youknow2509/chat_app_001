package com.example.chatapp.models;
public class FriendInvitation {
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACCEPTED = 1;
    public static final int STATUS_REJECTED = 2;

    private String request_id;
    private String user_id;
    private String nickName;
    private String time;
    private String avatarUrl;
    private int status;

    public FriendInvitation(String request_id, String user_id, String nickName, String time, String avatarUrl, int status) {
        this.request_id = request_id;
        this.user_id = user_id;
        this.nickName = nickName;
        this.time = time;
        this.avatarUrl = avatarUrl;
        this.status = status;
    }
    //
    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
