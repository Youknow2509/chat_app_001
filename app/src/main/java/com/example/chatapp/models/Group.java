package com.example.chatapp.models;

import java.io.Serializable;
import java.util.List;

public class Group implements Serializable {
    public String id;
    public String name;
    public List<User> members;

    public Group(String id, String name, List<User> members) {
        this.id = id;
        this.name = name;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public List<User> getMembers() {
        return members;
    }
}
