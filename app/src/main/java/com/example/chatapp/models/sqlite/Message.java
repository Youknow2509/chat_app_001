package com.example.chatapp.models.sqlite;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "messages"
//        foreignKeys = {
//                @ForeignKey(entity = Conversation.class,
//                        parentColumns = "conversationId",
//                        childColumns = "conversationId",
//                        onDelete = ForeignKey.CASCADE),
//                @ForeignKey(entity = User.class,
//                        parentColumns = "userId",
//                        childColumns = "senderId",
//                        onDelete = ForeignKey.CASCADE)
//        },
//        indices = {@Index("conversationId"), @Index("senderId")}
)
public class Message {
    @PrimaryKey
    @NonNull
    private String id;

    //    @NonNull
    private String chatId;

    //    @NonNull
    private String senderId;

    private String content;
    private Date timestamp;
    private String status;  // "sent", "delivered", "read"
    private String messageType;  // "text", "image", "video", "audio", "file", "location"
    private String mediaUrl;
    private String mediaType;  // "image", "video", etc.
    private String localPath;
    private String parentMessageId;
    private boolean isDeleted;
    private boolean isEdited;
    private Date createdAt;
    private Date updatedAt;

    // Constructors, Getters and Setters

    //    public Message(@NonNull String messageId, @NonNull String conversationId,
//                   @NonNull String senderId, String messageText) {
//        this.messageId = messageId;
//        this.conversationId = conversationId;
//        this.senderId = senderId;
//        this.messageText = messageText;
//        this.timestamp = new Date();
//        this.status = "sent";
//        this.messageType = "text";
//        this.isDeleted = false;
//        this.isEdited = false;
//        this.createdAt = new Date();
//        this.updatedAt = new Date();
//    }
    public Message(String id, String chatId,
                   String senderId, String content) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = new Date();
        this.status = "sent";
        this.messageType = "text";
        this.isDeleted = false;
        this.isEdited = false;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getChatId() {
        return chatId;
    }

    public void setChatId(@NonNull String chatId) {
        this.chatId = chatId;
    }

    @NonNull
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(@NonNull String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getParentMessageId() {
        return parentMessageId;
    }

    public void setParentMessageId(String parentMessageId) {
        this.parentMessageId = parentMessageId;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
}