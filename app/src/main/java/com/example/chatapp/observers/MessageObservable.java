package com.example.chatapp.observers;

import android.util.Log;

import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.sqlite.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageObservable {
    private static MessageObservable instance;
    private final List<MessageObserver> observers = new ArrayList<>();

    private MessageObservable() {}

    public static synchronized MessageObservable getInstance() {
        if (instance == null) {
            instance = new MessageObservable();
        }
        return instance;
    }

    public void addObserver(MessageObserver observer) {
        if (!observers.contains(observer) && checkObserver(observer)) {
            observers.add(observer);
        }
    }

    private boolean checkObserver(MessageObserver observer) {
        Optional<MessageObserver> temp = observers.stream().filter(o -> o.getChatId().equals(observer.getChatId())).findFirst();
        return temp.isEmpty();
    }

    public void removeObserver(MessageObserver observer) {
        observers.remove(observer);
    }

    public void notifyMessageReceived(ChatMessage message) {
        Log.d("MessageObservable", "Notifying " + observers.size() + " observers of message: " + message.getId());
        for (MessageObserver observer : observers) {
            Log.d("MessageObservable", "Notifying observer: " + observer.getClass().getSimpleName());
            observer.onMessageReceived(message);
        }
    }
}
