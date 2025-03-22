package com.example.chatapp.models.sqlite;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "conversations", indices = {@Index("lastMessageTimestamp"), // Để sắp xếp cuộc trò chuyện hiệu quả
        @Index(value = {"conversationType", "isPinned"}) // Cho các truy vấn lọc
})
public class Conversation {
    @PrimaryKey
    @NonNull
    private String conversationId;

    private String conversationName;
    private String conversationType;  // "individual" or "group"
    private Date creationTimestamp;
    private String lastMessageId;
    private Date lastMessageTimestamp;
    private int unreadCount;
    private String groupAvatarPath;
    private boolean isArchived;
    private boolean isPinned;
    private Date updatedAt;

    // Constructors, Getters and Setters

    public Conversation(@NonNull String conversationId, String conversationType) {
        this.conversationId = conversationId;
        this.conversationType = conversationType;
        this.creationTimestamp = new Date();
        this.unreadCount = 0;
        this.isArchived = false;
        this.isPinned = false;
        this.updatedAt = new Date();
    }

    // Getters and Setters
    @NonNull
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationName() {
        return conversationName;
    }

    public void setConversationName(String conversationName) {
        this.conversationName = conversationName;
    }

    public String getConversationType() {
        return conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Date creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public Date getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Date lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getGroupAvatarPath() {
        return groupAvatarPath;
    }

    public void setGroupAvatarPath(String groupAvatarPath) {
        this.groupAvatarPath = groupAvatarPath;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
