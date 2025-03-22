package com.example.chatapp.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.chatapp.models.relationship.ConversationWithLastMessage;
import com.example.chatapp.models.relationship.MessageWithMedia;
import com.example.chatapp.models.sqlite.Message;
import com.example.chatapp.models.sqlite.User;
import com.example.chatapp.repo.ChatRepo;

import java.util.List;
import java.util.UUID;

public class ChatViewModel extends AndroidViewModel {
    private ChatRepo repository;

    private final MediatorLiveData<List<ConversationWithLastMessage>> conversationsLiveData = new MediatorLiveData<>();
    private LiveData<List<ConversationWithLastMessage>> currentConversationsSource;

    public ChatViewModel(Application application) {
        super(application);
        repository = new ChatRepo(application);
    }

    public LiveData<List<ConversationWithLastMessage>> getConversations(String userId) {
        if (currentConversationsSource != null) {
            conversationsLiveData.removeSource(currentConversationsSource);
        }

        currentConversationsSource = repository.getConversationsForCurrentUser(userId);
        conversationsLiveData.addSource(currentConversationsSource, conversationsLiveData::setValue);

        return conversationsLiveData;
    }

    public LiveData<List<MessageWithMedia>> getMessages(String conversationId) {
        return repository.getMessagesForConversation(conversationId);
    }

    public void sendMessage(String conversationId, String senderId, String text) {
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(messageId, conversationId, senderId, text);
        repository.sendMessage(message);
    }

    public void markAsRead(String conversationId) {
        repository.markConversationAsRead(conversationId);
    }

    public LiveData<List<User>> getFriends(String userId) {
        return repository.getFriends(userId);
    }
}