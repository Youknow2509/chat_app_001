package com.example.chatapp.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class User implements Serializable {
    public String name;
    public String email;
    public String token;
    public String id;
    public Bitmap image;  // Giữ kiểu Bitmap cho thuộc tính image

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public User(String id, String name, String email, String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.token = token;
    }

    public User(String id, String name, String email, Bitmap image) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.image = image;
    }

    public User(String id, String name, String email, String token, Bitmap image) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.token = token;
        this.image = image;
    }

    public User(String id, String name, String email, String token, byte[] image) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.token = token;
        this.image = getImageFromBytes(image);
    }

    public User() {
    }

    public byte[] getImageBytes() {
        if (image == null) {
            return null;
        }
        return getBytesFromBitmap(image);
    }

    private Bitmap getImageFromBytes(byte[] image) {
        if (image == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

}
