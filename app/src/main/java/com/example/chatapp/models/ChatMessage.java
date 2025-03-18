package com.example.chatapp.models;

import android.graphics.Bitmap;

import java.util.Date;

public class ChatMessage {
    private String id;
    private String senderId, content, dateTime;
    private Date dateObject;
    private String chatId, messageType;
    private Bitmap conversionImage;
    private String mediaUrl;

    public void setId(String id) {
        this.id = id;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setConversionImage(Bitmap conversionImage) {
        this.conversionImage = conversionImage;
    }

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getContent() {
        return content;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public String getChatId() {
        return chatId;
    }

    public String getMessageType() {
        return messageType;
    }

    public Bitmap getConversionImage() {
        return conversionImage;
    }
}
