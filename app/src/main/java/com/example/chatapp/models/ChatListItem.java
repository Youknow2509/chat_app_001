package com.example.chatapp.models;

public class ChatListItem {

    private User user;

    public ChatListItem(User user) {
        this.user = user;
    }


    public User getUser() {
        return user;
    }

}
