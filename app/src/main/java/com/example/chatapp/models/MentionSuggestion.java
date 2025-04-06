package com.example.chatapp.models;

public class MentionSuggestion {
    private String name;
    private int iconResId;

    public MentionSuggestion(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }
}
