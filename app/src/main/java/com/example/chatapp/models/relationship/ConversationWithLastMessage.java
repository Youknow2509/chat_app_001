package com.example.chatapp.models.relationship;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.chatapp.models.sqlite.Conversation;
import com.example.chatapp.models.sqlite.Message;

public class ConversationWithLastMessage {
    @Embedded
    public Conversation conversation;

    @Relation(
            parentColumn = "lastMessageId",
            entityColumn = "id"
    )
    public Message lastMessage;
}