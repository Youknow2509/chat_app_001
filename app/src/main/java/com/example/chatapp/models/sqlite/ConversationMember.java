package com.example.chatapp.models.sqlite;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "conversation_members",
        primaryKeys = {"conversationId", "userId"},
        foreignKeys = {
                @ForeignKey(entity = Conversation.class,
                        parentColumns = "conversationId",
                        childColumns = "conversationId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "userId",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("conversationId"), @Index("userId")})
public class ConversationMember {
    @NonNull
    private String conversationId;

    @NonNull
    private String userId;

    private String role;  // "admin", "member"
    private Date joinedAt;
    private String nickname;
    private boolean isMuted;
    private Date lastReadTimestamp;

    // Constructors, Getters and Setters

    public ConversationMember(@NonNull String conversationId, @NonNull String userId) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = "member";
        this.joinedAt = new Date();
        this.isMuted = false;
        this.lastReadTimestamp = new Date();
    }

    // Getters and Setters
    @NonNull
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(@NonNull String conversationId) {
        this.conversationId = conversationId;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public Date getLastReadTimestamp() {
        return lastReadTimestamp;
    }

    public void setLastReadTimestamp(Date lastReadTimestamp) {
        this.lastReadTimestamp = lastReadTimestamp;
    }
}