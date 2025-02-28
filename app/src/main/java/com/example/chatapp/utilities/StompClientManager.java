package com.example.chatapp.utilities;

import static android.content.ContentValues.TAG;

import android.util.Log;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class StompClientManager {
    private static StompClient mStompClient;
    public static StompClient getInstance() {
        if (mStompClient == null) {
            mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://bream-living-llama.ngrok-free.app/ws/websocket");
            mStompClient.connect();
            mStompClient.topic("/topic/123e4567-e89b-12d3-a456-426614174000").subscribe(topicMessage -> {
                Log.i(TAG, topicMessage.getPayload());
            });
        }
        return mStompClient;
    }

}
