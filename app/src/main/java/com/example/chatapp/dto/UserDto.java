package com.example.chatapp.dto;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
public class UserDto implements Serializable {

    @SerializedName("user_id")
    private String id;

    @SerializedName("user_nickname")
    private String name;

    @SerializedName("user_email")
    private String email;

    @SerializedName("user_avatar")
    private String imageUrl;

    public UserDto(String id, String name, String email, String imageUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
