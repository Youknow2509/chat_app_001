package com.example.chatapp.models;

import com.example.chatapp.dto.ChatDTO;
import com.example.chatapp.fragments.ChatFragment;

public class ChatListItem {

    private ChatDTO chatDTO;

    public ChatListItem(ChatDTO chatDTO) {
        this.chatDTO = chatDTO;
    }


    public ChatDTO getChatDTO() {
        return chatDTO;
    }


}
