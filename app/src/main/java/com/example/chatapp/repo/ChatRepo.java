package com.example.chatapp.repo;

import static com.example.chatapp.database.AppDatabase.*;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chatapp.dao.ConversationDao;
import com.example.chatapp.dao.MessageDao;
import com.example.chatapp.dao.RelationshipQueries;
import com.example.chatapp.dao.UserDao;
import com.example.chatapp.database.AppDatabase;
import com.example.chatapp.models.relationship.ConversationWithLastMessage;
import com.example.chatapp.models.relationship.MessageWithMedia;
import com.example.chatapp.models.sqlite.MediaFile;
import com.example.chatapp.models.sqlite.Message;
import com.example.chatapp.models.sqlite.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepo {
    private UserDao userDao;
    private ConversationDao conversationDao;
    private MessageDao messageDao;
    private RelationshipQueries relationshipQueries;
    private final ExecutorService executorService;

    public ChatRepo(Application application) {
        AppDatabase db = getInstance(application);
        userDao = db.userDao();
        conversationDao = db.conversationDao();
        messageDao = db.messageDao();
        relationshipQueries = db.relationshipQueries();
        executorService = Executors.newFixedThreadPool(4);
    }

    // Users
    public void insertUser(User user) {
        executorService.execute(() -> userDao.insert(user));
    }

    public LiveData<User> getUserById(String userId) {
        MutableLiveData<User> data = new MutableLiveData<>();
        executorService.execute(() -> {
            User user = userDao.getUserById(userId);
            data.postValue(user);
        });
        return data;
    }

    // Messages
    public void sendMessage(Message message) {
        executorService.execute(() -> {
            messageDao.insert(message);
            conversationDao.updateLastMessage(
                    message.getConversationId(),
                    message.getMessageId(),
                    message.getTimestamp());
        });
    }

    public LiveData<List<MessageWithMedia>> getMessagesForConversation(String conversationId) {
        MutableLiveData<List<MessageWithMedia>> data = new MutableLiveData<>();
        executorService.execute(() -> {
            List<MessageWithMedia> messages = relationshipQueries.getMessagesWithMedia(conversationId);
            data.postValue(messages);
        });
        return data;
    }

    // Conversations
    public LiveData<List<ConversationWithLastMessage>> getConversationsForCurrentUser(String userId) {
        MutableLiveData<List<ConversationWithLastMessage>> data = new MutableLiveData<>();
        executorService.execute(() -> {
            List<ConversationWithLastMessage> conversations =
                    relationshipQueries.getConversationsWithLastMessage(userId);
            data.postValue(conversations);
        });
        return data;
    }

    public void markConversationAsRead(String conversationId) {
        executorService.execute(() -> conversationDao.resetUnreadCount(conversationId));
    }

    // Contacts
    public LiveData<List<User>> getFriends(String userId) {
        MutableLiveData<List<User>> data = new MutableLiveData<>();
        executorService.execute(() -> {
            List<User> friends = relationshipQueries.getFriends(userId);
            data.postValue(friends);
        });
        return data;
    }

    public void sendMessageWithAttachments(Message message, List<MediaFile> attachments) {
//        executorService.execute(() -> {
//            AppDatabase.BeginTransaction(() -> {
//                // Tất cả các thao tác trong này sẽ được thực hiện trong một transaction duy nhất
//                messageDao.insert(message);
//
//                for (MediaFile mediaFile : attachments) {
//                    mediaFile.setMessageId(message.getMessageId());
//                    StringBuffer mediaFileDao;
//                    mediaFileDao.insert(mediaFile);
//                }
//
//                conversationDao.updateLastMessage(
//                        message.getConversationId(),
//                        message.getMessageId(),
//                        message.getTimestamp());
//            });
//        });
    }
}