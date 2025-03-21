package com.example.chatapp.dao;


import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.chatapp.models.relationship.ConversationWithLastMessage;
import com.example.chatapp.models.relationship.ConversationWithMembers;
import com.example.chatapp.models.relationship.MessageWithMedia;
import com.example.chatapp.models.sqlite.User;

import java.util.List;

@Dao
public interface RelationshipQueries {
    // Kết quả truy vấn bao gồm cả tin nhắn cuối cùng
    @Transaction
    @Query("SELECT c.* FROM conversations c " +
            "JOIN conversation_members cm ON c.conversationId = cm.conversationId " +
            "WHERE cm.userId = :userId " +
            "ORDER BY c.lastMessageTimestamp DESC")
    List<ConversationWithLastMessage> getConversationsWithLastMessage(String userId);

    // Kết quả truy vấn kèm thông tin thành viên cuộc hội thoại
    @Transaction
    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    ConversationWithMembers getConversationWithMembers(String conversationId);

    // Lấy tất cả tin nhắn của một cuộc hội thoại kèm theo thông tin media
    @Transaction
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    List<MessageWithMedia> getMessagesWithMedia(String conversationId);

    // Lấy danh sách bạn bè
    @Transaction
    @Query("SELECT u.* FROM users u " +
            "JOIN contacts c ON u.userId = c.contactUserId " +
            "WHERE c.userId = :userId AND c.relationshipStatus = 'friend'")
    List<User> getFriends(String userId);
}