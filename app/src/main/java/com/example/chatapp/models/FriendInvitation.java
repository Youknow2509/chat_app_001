package com.example.chatapp.models;
public class FriendInvitation {
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACCEPTED = 1;
    public static final int STATUS_REJECTED = 2;

    private String id;
    private String username;
    private String email;
    private String time;
    private int status;

    public FriendInvitation(String id, String username, String email, String time, int status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.time = time;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getTime() {
        return time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
