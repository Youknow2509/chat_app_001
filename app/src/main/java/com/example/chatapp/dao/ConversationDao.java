package com.example.chatapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.chatapp.models.sqlite.Conversation;

import java.util.Date;
import java.util.List;

@Dao
public interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Conversation conversation);

    @Update
    void update(Conversation conversation);

    @Delete
    void delete(Conversation conversation);

    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    Conversation getConversationById(String conversationId);

    @Query("SELECT * FROM conversations ORDER BY lastMessageTimestamp DESC")
    List<Conversation> getAllConversations();

    @Query("SELECT * FROM conversations WHERE isPinned = 1 ORDER BY lastMessageTimestamp DESC")
    List<Conversation> getPinnedConversations();

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE conversationId = :conversationId")
    void incrementUnreadCount(String conversationId);

    @Query("UPDATE conversations SET unreadCount = 0 WHERE conversationId = :conversationId")
    void resetUnreadCount(String conversationId);

    @Query("UPDATE conversations SET lastMessageId = :messageId, lastMessageTimestamp = :timestamp WHERE conversationId = :conversationId")
    void updateLastMessage(String conversationId, String messageId, Date timestamp);

    @Transaction
    @Query("SELECT * FROM conversations WHERE conversationId IN (SELECT conversationId FROM conversation_members WHERE userId = :userId) ORDER BY lastMessageTimestamp DESC")
    List<Conversation> getConversationsForUser(String userId);
}
