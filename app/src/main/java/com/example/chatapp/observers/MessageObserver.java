package com.example.chatapp.observers;

import com.example.chatapp.models.ChatMessage;

public interface MessageObserver {
    void onMessageReceived(ChatMessage message);
    String getChatId();
}
