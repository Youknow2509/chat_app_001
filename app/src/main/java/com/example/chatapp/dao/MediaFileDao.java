package com.example.chatapp.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chatapp.models.sqlite.MediaFile;

import java.util.Date;
import java.util.List;


@Dao
public interface MediaFileDao {

    @Query("SELECT * FROM media_files WHERE messageId = :messageId")
    LiveData<List<MediaFile>> getMediaFilesForMessage(String messageId);

    @Query("SELECT * FROM media_files WHERE downloadStatus = 'pending' OR downloadStatus = 'failed'")
    List<MediaFile> getPendingDownloads();

    @Query("SELECT * FROM media_files WHERE downloadStatus = 'downloading'")
    List<MediaFile> getActiveDownloads();
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

    @Query("SELECT * FROM media_files WHERE messageId = :messageId")
    LiveData<List<MediaFile>> getMediaFilesByMessageLiveData(String messageId);

    @Query("SELECT * FROM media_files WHERE fileType = :fileType ORDER BY createdAt DESC")
    List<MediaFile> getMediaFilesByType(String fileType);

    @Query("UPDATE media_files SET downloadStatus = :status WHERE fileId = :fileId")
    void updateDownloadStatus(String fileId, String status);

    @Query("UPDATE media_files SET localPath = :localPath WHERE fileId = :fileId")
    void updateLocalPath(String fileId, String localPath);

    @Query("UPDATE media_files SET thumbnailPath = :thumbnailPath WHERE fileId = :fileId")
    void updateThumbnailPath(String fileId, String thumbnailPath);

    @Query("SELECT * FROM media_files WHERE messageId IN (SELECT messageId FROM messages WHERE chatId = :conversationId) ORDER BY createdAt DESC")
    List<MediaFile> getMediaFilesForConversation(String conversationId);

    @Query("DELETE FROM media_files WHERE messageId = :messageId")
    void deleteMediaFilesForMessage(String messageId);

    // getMediaFilesOlderThan - Lấy danh sách các file media cũ hơn ngày cutoffDate
    @Query("SELECT * FROM media_files WHERE createdAt < :cutoffDate")
    List<MediaFile> getMediaFilesOlderThan(Date cutoffDate);

    // getMediaCount - Lấy số lượng file media
    @Query("SELECT COUNT(*) FROM media_files")
    int getMediaCount();
}
