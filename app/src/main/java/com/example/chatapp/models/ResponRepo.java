package com.example.chatapp.models;

public class ResponRepo {
    private boolean Status; // true if success, false if failed
    private String Message;
    private Object Data;
    //
    public ResponRepo() {
    }

    public ResponRepo(boolean status, String message) {
        Status = status;
        Message = message;
    }

    public ResponRepo(boolean status, String message, Object data) {
        Status = status;
        Message = message;
        Data = data;
    }

    // getters and setters
    public boolean isStatus() {
        return Status;
    }

    public void setStatus(boolean status) {
        Status = status;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public Object getData() {
        return Data;
    }

    public void setData(Object data) {
        Data = data;
    }
}
