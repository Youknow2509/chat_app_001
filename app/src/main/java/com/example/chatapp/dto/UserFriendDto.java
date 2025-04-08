package com.example.chatapp.dto;

import java.io.Serializable;

public class UserFriendDto implements Serializable {
    String user_id;
    String user_nickname; // Thay đổi từ name
    String user_email;    // Thay đổi từ email
    String user_avatar;   // Thay đổi từ image

    public UserFriendDto(String user_id, String user_nickname, String user_email, String user_avatar) {
        this.user_id = user_id;
        this.user_nickname = user_nickname;
        this.user_email = user_email;
        this.user_avatar = user_avatar;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return user_nickname;
    }

    public void setName(String user_nickname) {
        this.user_nickname = user_nickname;
    }

    public String getEmail() {
        return user_email;
    }

    public void setEmail(String user_email) {
        this.user_email = user_email;
    }

    public String getImage() {
        return user_avatar;
    }

    public void setImage(String user_avatar) {
        this.user_avatar = user_avatar;
    }

    @Override
    public String toString() {
        return "UserFriendDto{" +
                "user_id='" + user_id + '\'' +
                ", user_nickname='" + user_nickname + '\'' +
                ", user_email='" + user_email + '\'' +
                ", user_avatar='" + user_avatar + '\'' +
                '}';
    }

    // Loại bỏ phương thức này vì không cần thiết
    // public UserFriendDto getUserFriendDto() {
    //    return new UserFriendDto(user_id, name, email, image);
    // }
}

