package com.example.chatapp.models;

public class ChatListItem {
    public static final int TYPE_USER = 0;
    public static final int TYPE_GROUP = 1;

    private User user;
    private Group group;
    private int type;

    public ChatListItem(User user) {
        this.user = user;
        this.type = TYPE_USER;
    }

    public ChatListItem(Group group) {
        this.group = group;
        this.type = TYPE_GROUP;
    }

    public boolean isUser() {
        return type == TYPE_USER;
    }

    public boolean isGroup() {
        return type == TYPE_GROUP;
    }

    public User getUser() {
        return user;
    }

    public Group getGroup() {
        return group;
    }
}
