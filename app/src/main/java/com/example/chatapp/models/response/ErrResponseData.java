package com.example.chatapp.models.response;

public class ErrResponseData {
    private int code;
    private String error;
    private Object detail;

    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public Object getDetail() {
        return detail;
    }
}