package com.example.chatapp.models.sqlite;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "settings",
        primaryKeys = {"settingKey", "userId"},
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "userId",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("userId")})
public class Setting {
    @NonNull
    private String settingKey;

    @NonNull
    private String userId;

    private String settingValue;
    private Date updatedAt;

    // Constructors, Getters and Setters

    public Setting(@NonNull String settingKey, @NonNull String userId, String settingValue) {
        this.settingKey = settingKey;
        this.userId = userId;
        this.settingValue = settingValue;
        this.updatedAt = new Date();
    }

    // Getters and Setters

    @NonNull
    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(@NonNull String settingKey) {
        this.settingKey = settingKey;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}