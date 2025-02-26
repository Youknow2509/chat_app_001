package com.example.chatapp.models;

import android.graphics.Bitmap;
import java.io.Serializable;

public class User implements Serializable {
    public String name;
    public String email;
    public String token;
    public String id;
    public Bitmap image;  // Giữ kiểu Bitmap cho thuộc tính image
}
