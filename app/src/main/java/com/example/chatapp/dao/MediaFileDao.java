package com.example.chatapp.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chatapp.models.sqlite.MediaFile;

import java.util.List;


@Dao
public interface MediaFileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MediaFile mediaFile);

    @Update
    void update(MediaFile mediaFile);

    @Delete
    void delete(MediaFile mediaFile);

    @Query("SELECT * FROM media_files WHERE fileId = :fileId")
    MediaFile getMediaFileById(String fileId);

    @Query("SELECT * FROM media_files WHERE messageId = :messageId")
    List<MediaFile> getMediaFilesByMessage(String messageId);

    @Query("SELECT * FROM media_files WHERE fileType = :fileType ORDER BY createdAt DESC")
    List<MediaFile> getMediaFilesByType(String fileType);

    @Query("UPDATE media_files SET downloadStatus = :status WHERE fileId = :fileId")
    void updateDownloadStatus(String fileId, String status);

    @Query("UPDATE media_files SET localPath = :localPath WHERE fileId = :fileId")
    void updateLocalPath(String fileId, String localPath);

    @Query("UPDATE media_files SET thumbnailPath = :thumbnailPath WHERE fileId = :fileId")
    void updateThumbnailPath(String fileId, String thumbnailPath);

    @Query("SELECT * FROM media_files WHERE messageId IN (SELECT messageId FROM messages WHERE conversationId = :conversationId) ORDER BY createdAt DESC")
    List<MediaFile> getMediaFilesForConversation(String conversationId);

    @Query("DELETE FROM media_files WHERE messageId = :messageId")
    void deleteMediaFilesForMessage(String messageId);
}