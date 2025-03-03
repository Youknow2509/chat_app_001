package com.example.chatapp.utilities;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class StompClientManager extends Service {
    private final IBinder binder = new LocalBinder();
    private StompClient mStompClient;
    private boolean isConnected = false;
    public static int messageCount =0 ;

    public class LocalBinder extends Binder {
        public StompClientManager getService() {
            return StompClientManager.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initStompClient();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isConnected) {
            connect();
        }
        // If service gets killed, restart it
        return START_STICKY;
    }

    private void initStompClient() {
        if (mStompClient == null) {
            mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP,
                    "ws://bream-living-llama.ngrok-free.app/ws/websocket");

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
    }

    public void connect() {
        if (mStompClient != null && !isConnected) {
            mStompClient.connect();

            // Subscribe to topics
            mStompClient.topic("/topic/123e4567-e89b-12d3-a456-426614174000").subscribe(topicMessage -> {
                Log.d(TAG, "Message received: " + topicMessage.getPayload());
                messageCount++;
                System.out.println(messageCount);
                // Process messages here or use a broadcast to notify activities
            }, throwable -> {
                Log.e(TAG, "Error in topic subscription", throwable);
            });
        }
    }

    public void sendMessage(String destination, String message) {
        if (mStompClient != null && isConnected) {
            mStompClient.send(destination, message).subscribe(() -> {
                Log.d(TAG, "Message sent successfully");
            }, throwable -> {
                Log.e(TAG, "Error sending message", throwable);
            });
        } else {
            Log.e(TAG, "Cannot send message: client not connected");
        }
    }

    public StompClient getClient() {
        return mStompClient;
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void onDestroy() {
        if (mStompClient != null && isConnected) {
            mStompClient.disconnect();
        }
        super.onDestroy();
    }
}