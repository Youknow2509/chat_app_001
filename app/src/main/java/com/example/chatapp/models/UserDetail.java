package com.example.chatapp.models;

public class UserDetail {
    private String name;
    private String url_avatar;
    private String path_local_avatar;
    private String email;
    private String gender;
    private String birthday;
    private String address;
    //
    public UserDetail() {
    }

    public UserDetail(String name, String email, String gender, String birthday, String path_local_avatar) {
        this.name = name;
        this.path_local_avatar = path_local_avatar;
        this.email = email;
        this.gender = gender;
        this.birthday = birthday;
    }

    public UserDetail(String name, String url_avatar, String path_local_avatar, String email, String gender, String birthday, String address) {
        this.name = name;
        this.url_avatar = url_avatar;
        this.path_local_avatar = path_local_avatar;
        this.email = email;
        this.gender = gender;
        this.birthday = birthday;
        this.address = address;
    }

    // to string
    @Override
    public String toString() {
        return "UserDetail{" +
                "name='" + name + '\'' +
                ", url_avatar='" + url_avatar + '\'' +
                ", path_local_avatar='" + path_local_avatar + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", birthday='" + birthday + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    // getter and setter
    public String getPath_local_avatar() {
        return path_local_avatar;
    }

    public void setPath_local_avatar(String path_local_avatar) {
        this.path_local_avatar = path_local_avatar;
    }

    public String getUrl_avatar() {
        return url_avatar;
    }

    public void setUrl_avatar(String url_avatar) {
        this.url_avatar = url_avatar;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
