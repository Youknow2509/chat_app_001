package com.example.chatapp.utilities;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.observers.MessageObservable;
import com.google.gson.Gson;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.dto.StompMessage;

public class StompClientManager {
    private static StompClientManager instance;
    private StompClient mStompClient;
    private boolean isConnected = false;
    private final String SEND_MESSAGE_DESTINATION = "/app/chats";
    private final MessageObservable messageObservable;
    private final Gson gson = new Gson();
    private static final String TAG = "StompClientManager";

    private StompClientManager() {
        // Initialize your StompClient here
        messageObservable = MessageObservable.getInstance();
    }

    @SuppressLint("CheckResult")
    private void initStompClient() {
        if (mStompClient == null) {
            mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP,
                    "ws://" + Constants.HOST_SERVER + "/ws/websocket");

            // Set up lifecycle management
            mStompClient.lifecycle().subscribe(lifecycleEvent -> {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d(TAG, "Stomp connection opened");
                        isConnected = true;
                        break;
                    case ERROR:
                        Log.e(TAG, "Error", lifecycleEvent.getException());
                        break;
                    case CLOSED:
                        Log.d(TAG, "Stomp connection closed");
                        isConnected = false;
                        break;
                }
            }, throwable -> {
                Log.e(TAG, "Error in lifecycle subscription", throwable);
            });
        }

        connect();
    }

    public void connect() {
        // TODO: Replace the token with user token
        if (mStompClient != null && !isConnected) {
            StompHeader header = new StompHeader("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidXNlcl9pZCI6IjEyM2U0NTY3LWU4OWItMTJkMy1hNDU2LTQyNjYxNDE3NDAwMCIsImlhdCI6MTUxNjIzOTAyMn0.iA1Q2NbaWUhs60yei-3dQdDwuQfcO7R5y6yYD-vSAvo");
            mStompClient.connect(List.of(header));
        }
    }

    @SuppressLint("CheckResult")
    public void subscribeTopic(String userId) {
        if (mStompClient == null || !isConnected) {
            Log.e(TAG, "Cannot subscribe: client not connected. Attempting to reconnect...");
            initStompClient(); // Try to reinitialize
            return;
        }

        Log.d(TAG, "Subscribing to topic: /topic/" + userId);

        Disposable disposable = mStompClient.topic("/topic/" + userId)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received message: " + topicMessage.getPayload());
                    ChatMessage chatMessage = parseToChatMessage(topicMessage);
                    if (chatMessage != null) {
                        messageObservable.notifyMessageReceived(chatMessage);
                        Log.d(TAG, "Message notified to observers: " + chatMessage.getChatId());
                    } else {
                        Log.e(TAG, "Failed to parse message");
                    }
                }, throwable -> {
                    Log.e(TAG, "Error on subscribe topic", throwable);
                });

        Log.d(TAG, "Subscription created for: /topic/" + userId);
    }

    private ChatMessage parseToChatMessage(StompMessage stompMessage) {
        try {
            ChatMessage obj =  gson.fromJson(stompMessage.getPayload(), ChatMessage.class);

//            ChatMessage chatMessage = new ChatMessage();

            // set chat id from obj id
//            chatMessage.
            return obj;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message", e);
            return null;
        }
    }

    @SuppressLint("CheckResult")
    public void sendMessage(String message) {
        if (mStompClient != null && isConnected) {
            mStompClient.send(SEND_MESSAGE_DESTINATION, message).subscribe(() -> {
                Log.d(TAG, "Message sent successfully");
            }, throwable -> {
                Log.e(TAG, "Error sending message", throwable);
            });
        } else {
            Log.e(TAG, "Cannot send message: client not connected");
        }
    }

    public static StompClientManager getInstance() {
        if (instance == null) {
            instance = new StompClientManager();
            instance.initStompClient();
        }
        return instance;
    }



    public StompClient getClient() {
        return mStompClient;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void checkConnection() {
        Log.d(TAG, "Connection status - StompClient null: " +
                (mStompClient == null) + ", isConnected: " + isConnected);

        // Try reconnecting if not connected
        if (!isConnected) {
            Log.d(TAG, "Attempting to reconnect...");
            connect();
        }
    }

}