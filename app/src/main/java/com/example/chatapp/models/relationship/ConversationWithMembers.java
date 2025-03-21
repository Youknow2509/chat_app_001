package com.example.chatapp.models.relationship;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.chatapp.models.sqlite.Conversation;
import com.example.chatapp.models.sqlite.ConversationMember;
import com.example.chatapp.models.sqlite.User;

import java.util.List;

public class ConversationWithMembers {
    @Embedded
    public Conversation conversation;

    @Relation(
            entity = User.class,
            parentColumn = "conversationId",
            entityColumn = "userId",
            associateBy = @Junction(
                    value = ConversationMember.class,
                    parentColumn = "conversationId",
                    entityColumn = "userId"
            )
    )
    public List<User> members;
}