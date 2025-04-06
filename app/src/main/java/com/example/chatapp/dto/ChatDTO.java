package com.example.chatapp.dto;

public class ChatDTO {
    private String chat_id;
    private String chat_name;
    private String avatar;

    // Getters và setters
    public String getChatId() {
        return chat_id;
    }
    public void setChatId(String chat_id) {
        this.chat_id = chat_id;
    }
    public String getChatName() {
        return chat_name;
    }
    public void setChatName(String chat_name) {
        this.chat_name = chat_name;
    }
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
