package com.example.chatapp.models;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    public String id;
    public String name;
    public List<User> members;
    public Bitmap image;

    public Group(String id, String name, List<User> members, Bitmap image) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public List<User> getMembers() {
        return members;
    }
}
