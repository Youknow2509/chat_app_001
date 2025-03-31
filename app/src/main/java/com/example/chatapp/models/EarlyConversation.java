package com.example.chatapp.models;

public class EarlyConversation {
    private String userID;
    private String name;
    private String message;
    private String lastTime;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getLastTime() {
        return lastTime;
    }
}
