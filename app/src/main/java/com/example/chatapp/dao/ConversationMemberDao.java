package com.example.chatapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chatapp.models.sqlite.ConversationMember;

import java.util.Date;
import java.util.List;

@Dao
public interface ConversationMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ConversationMember member);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ConversationMember> members);

    @Update
    void update(ConversationMember member);

    @Delete
    void delete(ConversationMember member);

    @Query("SELECT * FROM conversation_members WHERE conversationId = :conversationId")
    List<ConversationMember> getMembersByConversation(String conversationId);

    @Query("SELECT * FROM conversation_members WHERE userId = :userId")
    List<ConversationMember> getConversationsByUser(String userId);

    @Query("SELECT * FROM conversation_members WHERE conversationId = :conversationId AND userId = :userId")
    ConversationMember getMember(String conversationId, String userId);

    @Query("UPDATE conversation_members SET role = :role WHERE conversationId = :conversationId AND userId = :userId")
    void updateMemberRole(String conversationId, String userId, String role);

    @Query("UPDATE conversation_members SET isMuted = :isMuted WHERE conversationId = :conversationId AND userId = :userId")
    void updateMuteStatus(String conversationId, String userId, boolean isMuted);

    @Query("UPDATE conversation_members SET lastReadTimestamp = :timestamp WHERE conversationId = :conversationId AND userId = :userId")
    void updateLastReadTimestamp(String conversationId, String userId, Date timestamp);

    @Query("DELETE FROM conversation_members WHERE conversationId = :conversationId AND userId = :userId")
    void removeMember(String conversationId, String userId);
}