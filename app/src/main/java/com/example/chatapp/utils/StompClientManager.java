package com.example.chatapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.WebRTCMessage;
import com.example.chatapp.models.sqlite.Message;
import com.example.chatapp.observers.MessageObservable;
import com.example.chatapp.observers.SignalingObserver;
import com.example.chatapp.repository.ChatRepo;
import com.example.chatapp.utils.session.SessionManager;
import com.google.gson.Gson;

import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.dto.StompMessage;

public class StompClientManager {
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(StompClientManager.class);
    private static StompClientManager instance;
    private StompClient mStompClient;
    private boolean isConnected = false;
    private final String SEND_MESSAGE_DESTINATION = "/app/chats";
    private final MessageObservable messageObservable;
    private final Gson gson = new Gson();
    private static final String TAG = "StompClientManager";
    // Add these variables
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY_MS = 5000; // 5 seconds
    private int reconnectAttempts = 0;
    private Handler reconnectHandler = new Handler(Looper.getMainLooper());
    private Runnable reconnectRunnable;
    private SessionManager sessionManager;
    private ChatRepo chatRepo;
    private Context context;

    private StompClientManager() {
        // Initialize your StompClient here
        messageObservable = MessageObservable.getInstance();
    }

    public void setSessionManager(SessionManager sessionManager, Context context) {
        this.sessionManager = sessionManager;
        this.chatRepo = new ChatRepo(context);
        initStompClient();
    }

    @SuppressLint("CheckResult")
    private void initStompClient() {
        if (mStompClient == null) {
            mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://" + Constants.HOST_SERVER + "/ws/websocket");
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
        // display log
        connect();
    }

    public void connect() {
        // TODO: Replace the token with user token
        if (mStompClient != null && !isConnected) {
            StompHeader header = new StompHeader("Authorization", "Bearer " + sessionManager.getAccessToken());
            mStompClient.connect(List.of(header));
        }
    }

    // Add a method to manually trigger reconnection
    public void reconnect() {
        reconnectAttempts = 0;
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        // Cancel any pending reconnection attempts
        if (reconnectRunnable != null) {
            reconnectHandler.removeCallbacks(reconnectRunnable);
        }

        // Create a new reconnection task
        reconnectRunnable = () -> {
            if (!isConnected && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++;
                long delay = RECONNECT_DELAY_MS * reconnectAttempts; // Exponential backoff

                Log.d(TAG, "Attempting to reconnect... Attempt #" + reconnectAttempts +
                        " (waiting " + delay/1000 + " seconds)");

                // Disconnect first if needed
                if (mStompClient != null) {
                    try {
                        mStompClient.disconnect();
                    } catch (Exception e) {
                        Log.e(TAG, "Error disconnecting before reconnect", e);
                    }
                }

                // Recreate the client and connect
                mStompClient = null;
                initStompClient();

                // Schedule next attempt if this fails
                if (!isConnected) {
                    reconnectHandler.postDelayed(this.reconnectRunnable, delay);
                }
            } else if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                Log.e(TAG, "Max reconnection attempts reached. Giving up automatic reconnection.");
            }
        };

        // Schedule first reconnection attempt
        reconnectHandler.postDelayed(reconnectRunnable, RECONNECT_DELAY_MS);
    }

    @SuppressLint("CheckResult")
    public void subscribeTopic(String userId) {

        if (mStompClient == null ) {
            Log.e(TAG, "Cannot subscribe: client not connected. Attempting to reconnect...");
            initStompClient(); // Try to reinitialize
            int attempts = 0;
            while (!mStompClient.isConnected()){
                reconnect();
                attempts++;
                Log.d(TAG, "Attempting to reconnect... Attempt #" + attempts);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        Log.d(TAG, "Subscribing to topic: /topic/" + userId);

        Disposable disposable = mStompClient.topic("/topic/" + userId)
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received message: " + topicMessage.getPayload());
                    ChatMessage chatMessage = parseToChatMessage(topicMessage);
                    Message message = new Message(
                            chatMessage.getChatId(),
                            chatMessage.getSenderId(),
                            chatMessage.getReceiverId(),
                            "test"
                    );
                    chatRepo.sendMessage(message);
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
            ChatMessage obj = gson.fromJson(stompMessage.getPayload(), ChatMessage.class);

            ChatMessage chatMessage = new ChatMessage();

//             set chat id from obj id
//            chatMessage.
            return obj;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing message", e);
            return null;
        }
    }

    @SuppressLint("CheckResult")
    public void sendMessage(String message) {
        if(!mStompClient.isConnected()){
            Log.e(TAG, "Cannot send message: client not connected. Attempting to reconnect...");
            initStompClient(); // Try to reinitialize
            int attempts = 0;
            while (!mStompClient.isConnected()){
                reconnect();
                attempts++;
                Log.d(TAG, "Attempting to reconnect... Attempt #" + attempts);
            }
        }

        mStompClient.send(SEND_MESSAGE_DESTINATION, message).subscribe(() -> {
            Log.d(TAG, "Message sent successfully");
        }, throwable -> {
            Log.e(TAG, "Error sending message", throwable);
        });
    }

    public void sendCallOffer(WebRTCMessage webRTCMessage) {
        if (mStompClient != null && isConnected) {
            JSONObject callSignal = new JSONObject();
            try {
                callSignal.put("type", "offer");
                callSignal.put("payload", webRTCMessage.getPayload());
                callSignal.put("chatId", webRTCMessage.getChatId());
                callSignal.put("senderId", "YOUR_USER_ID"); // Replace with actual user ID

                mStompClient.send("/app/call/offer", callSignal.toString()).subscribe(() -> {
                    Log.d(TAG, "Call offer sent successfully");
                }, throwable -> {
                    Log.e(TAG, "Error sending call offer", throwable);
                });
            } catch (JSONException e) {
                Log.e(TAG, "Error creating call offer JSON", e);
            }
        }
    }

    public void sendCallAnswer(WebRTCMessage webRTCMessage) {
        if (mStompClient != null && isConnected) {
            JSONObject callSignal = new JSONObject();
            try {
                callSignal.put("type", "answer");
                callSignal.put("payload", webRTCMessage.getPayload());
                callSignal.put("chatId", webRTCMessage.getChatId());
                callSignal.put("senderId", "YOUR_USER_ID"); // Replace with actual user ID

                mStompClient.send("/app/call/answer", callSignal.toString()).subscribe(() -> {
                    Log.d(TAG, "Call answer sent successfully");
                }, throwable -> {
                    Log.e(TAG, "Error sending call answer", throwable);
                });
            } catch (JSONException e) {
                Log.e(TAG, "Error creating call answer JSON", e);
            }
        }
    }

    public void setOnSignalingEventListener(SignalingObserver listener){
        if (mStompClient != null && isConnected) {
            mStompClient.topic("/topic/call/123e4567-e89b-12d3-a456-426614174000").subscribe(topicMessage -> {
                Log.d(TAG, "Received signaling message: " + topicMessage.getPayload());
                WebRTCMessage webRTCMessage = gson.fromJson(topicMessage.getPayload(), WebRTCMessage.class);
                listener.onSignalingEvent(webRTCMessage);
            }, throwable -> {
                Log.e(TAG, "Error on subscribe signaling topic", throwable);
            });
        }
    }

    public void sendIceCandidate(String receiverId, IceCandidate candidate) {
        if (mStompClient != null && isConnected) {
            JSONObject callSignal = new JSONObject();
            try {
                JSONObject candidateJson = new JSONObject();
                candidateJson.put("sdpMid", candidate.sdpMid);
                candidateJson.put("sdpMLineIndex", candidate.sdpMLineIndex);
                candidateJson.put("sdp", candidate.sdp);

                callSignal.put("type", "ice_candidate");
                callSignal.put("candidate", candidateJson);
                callSignal.put("receiverId", receiverId);
                callSignal.put("senderId", "YOUR_USER_ID"); // Replace with actual user ID

                mStompClient.send("/app/call/candidate", callSignal.toString()).subscribe(() -> {
                    Log.d(TAG, "ICE candidate sent successfully");
                }, throwable -> {
                    Log.e(TAG, "Error sending ICE candidate", throwable);
                });
            } catch (JSONException e) {
                Log.e(TAG, "Error creating ICE candidate JSON", e);
            }
        }
    }

    public static StompClientManager getInstance() {
        if (instance == null) {
            instance = new StompClientManager();
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