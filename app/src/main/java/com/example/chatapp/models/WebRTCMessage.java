package com.example.chatapp.models;

public class WebRTCMessage {
    private String senderId;
    private String receiverId;
    private String chatId;
    private String type; // offer, answer, candidate
    private String payload; // SDP or ICE candidate

    public enum Type {
        OFFER("offer"),
        ANSWER("answer"),
        CANDIDATE("candidate");
        private String type;
        Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
}
