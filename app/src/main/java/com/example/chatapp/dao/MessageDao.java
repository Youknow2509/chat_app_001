package com.example.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chatapp.models.sqlite.Message;

import java.util.Date;
import java.util.List;

@Dao
public interface MessageDao {
    // getMessageCount - Lấy số lượng tin nhắn
    @Query("SELECT COUNT(*) FROM messages")
    int getMessageCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Message message);

    @Update
    void update(Message message);

    @Delete
    void delete(Message message);

    @Query("SELECT * FROM messages WHERE id = :messageId")
    Message getMessageById(String messageId);

    @Query("SELECT * FROM messages WHERE chatId = :conversationId AND isDeleted = 0 ORDER BY createdAt ASC")
    List<Message> getMessagesByConversation(String conversationId);

    @Query("SELECT * FROM messages WHERE chatId = :conversationId AND isDeleted = 0 ORDER BY createdAt DESC LIMIT :limit")
    List<Message> getRecentMessages(String conversationId, int limit);

    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' AND isDeleted = 0 ORDER BY createdAt DESC")
    List<Message> searchMessages(String query);

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    void updateMessageStatus(String messageId, String status);

    @Query("UPDATE messages SET isDeleted = 1 WHERE id = :messageId")
    void markAsDeleted(String messageId);

    // Thêm phương thức phân trang
    @Query("SELECT * FROM messages WHERE chatId = :conversationId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    LiveData<List<Message>> getMessagesPaged(String conversationId, int limit, int offset);

    @Query("SELECT createdAt FROM messages WHERE chatId = :conversationId ORDER BY createdAt DESC LIMIT 1")
    Long getLastMessageTimestamp(String conversationId);

    // Hỗ trợ Paging Library
    @Query("SELECT * FROM messages WHERE chatId = :conversationId ORDER BY createdAt DESC")
    PagingSource<Integer, Message> getMessagesPagingSource(String conversationId);

    // getMessagesOlderThan - Lấy các tin nhắn cũ hơn một ngày cụ thể
    @Query("SELECT * FROM messages WHERE chatId = :conversationId AND createdAt < :cutoffDate AND isDeleted = 0")
    List<Message> getMessagesOlderThan(String conversationId, Date cutoffDate);

    // deleteMessagesOlderThan - Xóa các tin nhắn cũ hơn một ngày cụ thể
    @Query("DELETE FROM messages WHERE chatId = :conversationId AND createdAt < :cutoffDate AND isDeleted = 0")
    void deleteMessagesOlderThan(String conversationId, Date cutoffDate);
}