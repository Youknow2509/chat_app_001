package com.example.chatapp.models.relationship;

import java.util.List;
import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.chatapp.models.sqlite.MediaFile;
import com.example.chatapp.models.sqlite.Message;

public class MessageWithMedia {
    @Embedded
    public Message message;

    @Relation(
            parentColumn = "messageId",
            entityColumn = "messageId"
    )
    public List<MediaFile> mediaFiles;
}