package com.example.chatapp.dto;

public class MessageDTO {
    private String content;
    private String chatId;
    private String type;

    public MessageDTO(String content, String chatId, String messageType) {
        this.content = content;
        this.chatId = chatId;
        this.type = messageType;
    }

    public String getMessageType() {
        return type;
    }

    public void setMessageType(String messageType) {
        this.type = messageType;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
