package com.example.chatapp.utils;

import android.util.Log;

import com.example.chatapp.api.ApiManager;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.models.sqlite.Message;
import com.example.chatapp.repository.ChatRepo;
import com.example.chatapp.utils.session.SessionManager;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataSync {
    private static final String TAG = "DataSync";
    private ApiManager apiManager;
    private ChatRepo chatRepo;
    private SessionManager sessionManager;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(List.class, new JsonDeserializer<List>() {
                @Override
                public List deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    List response = new ArrayList();
                    if (json.isJsonArray()) {
                        Type listType = new TypeToken<List<Message>>(){}.getType();
                        List<Message> messages = context.deserialize(json, listType);
                        for (Message message : messages) {
                            response.add(message);
                        }
                    }
                    return response;
                }
            })
            .create();

    private static DataSync instance;

    public static synchronized DataSync getInstance() {
        if (instance == null) {
            instance = new DataSync();
        }
        return instance;
    }

    public void init(ApiManager apiManager, ChatRepo chatRepo, SessionManager sessionManager) {
        this.apiManager = apiManager;
        this.chatRepo = chatRepo;
        this.sessionManager = sessionManager;
    }

    private DataSync() {
        // Initialize any necessary components here
    }

    public interface SyncCallback<T> {
        void onComplete(List<T> data);

        void onError(String errorMessage);
    }

    public void syncMessage(String chatId, final SyncCallback<Message> callback) {
        Log.d(TAG, "Data synchronization started");

        // Execute database operation on background thread
        new Thread(() -> {
            try {
//                Long lastMessageTime = chatRepo.getLastMessageTimestamp(chatId);

//                if (lastMessageTime == null) {
                final int[] pageRef = {0};
                final boolean[] hasMoreRef = {true};

                fetchNextPage(chatId, pageRef[0], 50, hasMoreRef[0], pageRef, hasMoreRef, callback);
//                } else {
//                    // TODO handle case not null
//                }
            } catch (Exception e) {
                callback.onError("Database error: " + e.getMessage());
            }
        }).start();
    }

    private void fetchNextPage(String chatId, int currentPage, int pageSize, boolean continueFlag,
                               final int[] pageRef, final boolean[] hasMoreRef,
                               final SyncCallback<Message> callback) {

        List<Message> messages = new ArrayList<>();
        if (!continueFlag) {
            callback.onComplete(messages);
            return;
        }
        String userId = sessionManager.getUserId();
//        String userId = "123e4567-e89b-12d3-a456-426614174000";

        apiManager.getMessagesByChatId(userId, chatId, pageSize, currentPage, new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful()) {
                    List<Message> newMessages = response.body();
                    assert newMessages != null;
                    messages.addAll(newMessages);
                    if(newMessages.isEmpty()){
                        hasMoreRef[0] = false;
                        callback.onComplete(messages); // complete sync all
                        return;
                    }
                    if (newMessages.size() == pageSize) {
                        callback.onComplete(newMessages); // complete sync one page
                        pageRef[0]++;
                        fetchNextPage(chatId, pageRef[0], pageSize, true, pageRef, hasMoreRef, callback);
                    }
                    if( newMessages.size() < pageSize) {
                        hasMoreRef[0] = false;
                        messages.forEach(chatRepo::sendMessage);
                        callback.onComplete(messages); // complete sync all
                    }
                } else {
                    callback.onError("Error fetching messages: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                callback.onError("onFailure: Error fetching messages: " + t.getMessage());
            }

        });
    }


    public void stopSync() {
        // Implement logic to stop data synchronization
        Log.d(TAG, "Data synchronization stopped");
    }

    public ApiManager getApiManager() {
        return apiManager;
    }

    public void setApiManager(ApiManager apiManager) {
        this.apiManager = apiManager;
    }

    public ChatRepo getChatRepo() {
        return chatRepo;
    }

    public void setChatRepo(ChatRepo chatRepo) {
        this.chatRepo = chatRepo;
    }
}
