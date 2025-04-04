package com.example.chatapp.models.sqlite;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
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
    private String messageId;

    //    @NonNull
    private String conversationId;

    //    @NonNull
    private String senderId;

    private String messageText;
    private Date timestamp;
    private String status;  // "sent", "delivered", "read"
    private String messageType;  // "text", "image", "video", "audio", "file", "location"
    private String mediaUrl;
    private String localPath;
    private String repliedToMessageId;
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
    public Message(String messageId, String conversationId,
                   String senderId, String messageText) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.messageText = messageText;
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
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
    }

    @NonNull
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    @NonNull
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(@NonNull String senderId) {
        this.senderId = senderId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
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

    public String getRepliedToMessageId() {
        return repliedToMessageId;
    }

    public void setRepliedToMessageId(String repliedToMessageId) {
        this.repliedToMessageId = repliedToMessageId;
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
}