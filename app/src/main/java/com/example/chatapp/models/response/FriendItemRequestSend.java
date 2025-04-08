package com.example.chatapp.models.response;

public class FriendItemRequestSend {
    private String request_id;
    private String to_user;
    private String user_nickname;
    private String user_avatar;
    private String status_request;
    private String created_at;
    //
    public FriendItemRequestSend(String request_id, String to_user, String user_nickname, String user_avatar, String status_request, String created_at) {
        this.request_id = request_id;
        this.to_user = to_user;
        this.user_nickname = user_nickname;
        this.user_avatar = user_avatar;
        this.status_request = status_request;
        this.created_at = created_at;
    }
    //
    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getTo_user() {
        return to_user;
    }

    public void setTo_user(String to_user) {
        this.to_user = to_user;
    }

    public String getUser_nickname() {
        return user_nickname;
    }

    public void setUser_nickname(String user_nickname) {
        this.user_nickname = user_nickname;
    }

    public String getUser_avatar() {
        return user_avatar;
    }

    public void setUser_avatar(String user_avatar) {
        this.user_avatar = user_avatar;
    }

    public String getStatus_request() {
        return status_request;
    }

    public void setStatus_request(String status_request) {
        this.status_request = status_request;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
