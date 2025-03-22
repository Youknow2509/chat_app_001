package com.example.chatapp.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.example.chatapp.models.sqlite.Message;

import java.util.Date;
import java.util.List;

@Dao
public interface OptimizedMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Message message);

    @Update
    void update(Message message);

    // Paging query for efficient large dataset handling
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isDeleted = 0 " +
            "ORDER BY timestamp DESC")
    DataSource.Factory<Integer, Message> getMessagesPaged(String conversationId);

    // Optimized query with index hint
    @Query("SELECT /*+ INDEX(messages, index_messages_timestamp) */ * FROM messages " +
            "WHERE conversationId = :conversationId AND isDeleted = 0 " +
            "AND timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY timestamp DESC")
    List<Message> getMessagesByDateRange(String conversationId, Date startDate, Date endDate);

    // Query optimization with LIMIT
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND isDeleted = 0 " +
            "ORDER BY timestamp DESC LIMIT :limit")
    List<Message> getRecentMessages(String conversationId, int limit);

    // Using JOIN only when needed
    @Transaction
    @Query("SELECT m.* FROM messages m " +
            "JOIN users u ON m.senderId = u.userId " +
            "WHERE m.conversationId = :conversationId AND u.displayName LIKE '%' || :senderName || '%' " +
            "ORDER BY m.timestamp DESC LIMIT 100")
    List<Message> findMessagesBySender(String conversationId, String senderName);

    // EXPLAIN QUERY plan monitoring
    @RawQuery
    List<Message> explainQueryPlan(SupportSQLiteQuery query);

    default List<Message> getExplainPlan(String conversationId) {
        String explainQuery = "EXPLAIN QUERY PLAN " +
                "SELECT * FROM messages " +
                "WHERE conversationId = ? AND isDeleted = 0 " +
                "ORDER BY timestamp DESC LIMIT 50";

        SimpleSQLiteQuery query = new SimpleSQLiteQuery(explainQuery, new Object[]{conversationId});
        return explainQueryPlan(query);
    }

    // Optimized subquery for stats
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND " +
            "timestamp > (SELECT COALESCE(MAX(lastReadTimestamp), 0) FROM conversation_members " +
            "WHERE conversationId = :conversationId AND userId = :userId)")
    int getUnreadCount(String conversationId, String userId);

    // TODO - Optimized full-text search (requires FTS setup)
//    @Query("SELECT m.* FROM messages m, messages_fts fts " +
//            "WHERE m.messageId = fts.rowid AND fts.messageText MATCH :searchQuery " +
//            "ORDER BY m.timestamp DESC LIMIT 100")
//    List<Message> searchMessages(String searchQuery);
//
//    // Batch operations for better performance
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insertAll(List<Message> messages);
//
//    @Query("UPDATE messages SET status = :status WHERE messageId IN (:messageIds)")
//    void updateStatusBatch(List<String> messageIds, String status);
//
//    // Optimized query with projection (only needed columns)
//    @Query("SELECT messageId, messageText, timestamp, status FROM messages " +
//            "WHERE conversationId = :conversationId AND isDeleted = 0 " +
//            "ORDER BY timestamp DESC LIMIT :limit")
//    List<MessageSummary> getMessageSummaries(String conversationId, int limit);
//
//    // For sync status queries
//    @Query("SELECT DISTINCT conversationId FROM messages WHERE syncStatus = 'pending'")
//    List<String> getConversationsWithPendingMessages();
//
//    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND syncStatus = 'pending'")
//    List<Message> getPendingMessages(String conversationId);
//
//    @Query("SELECT * FROM messages WHERE serverMessageId = :serverMessageId LIMIT 1")
//    Message getMessageByServerId(String serverMessageId);
}