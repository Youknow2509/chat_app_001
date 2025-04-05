package com.example.chatapp.models.sqlite;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity(tableName = "media_files",
        foreignKeys = @ForeignKey(entity = Message.class,
                parentColumns = "id",
                childColumns = "messageId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("messageId")})
public class MediaFile {
    @PrimaryKey
    @NonNull
    private String fileId;

    @NonNull
    private String messageId;

    private String fileType;  // "image", "video", "audio", "document"
    private String localPath;
    private long size;
    private String thumbnailPath;
    private String downloadStatus;  // "pending", "downloading", "completed", "failed"
    private Date createdAt;
    private String fileName;
    private String mimeType;

    // Constructors, Getters and Setters

    public MediaFile(@NonNull String fileId, @NonNull String messageId, String fileType) {
        this.fileId = fileId;
        this.messageId = messageId;
        this.fileType = fileType;
        this.downloadStatus = "pending";
        this.createdAt = new Date();
    }

    // Getters and Setters

    @NonNull
    public String getFileId() {
        return fileId;
    }

    public void setFileId(@NonNull String fileId) {
        this.fileId = fileId;
    }

    @NonNull
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(String downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}