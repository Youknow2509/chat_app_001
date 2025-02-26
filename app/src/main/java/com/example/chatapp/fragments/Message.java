package com.example.chatapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activities.ChatActivity;
import com.example.chatapp.adapters.UserAdapter;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;

public class Message extends Fragment {

    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List<User> users;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        // Initialize the RecyclerView
        userRecyclerView = view.findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get users list
        users = getUsers();  // Get list of users

        // Set the UserAdapter
        userAdapter = new UserAdapter(users, new UserListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(Constants.KEY_USER, user); // Gửi thông tin người dùng qua Intent
                startActivity(intent);
            }

        });
        userRecyclerView.setAdapter(userAdapter);

        return view;
    }

    private List<User> getUsers() {
        // Sample data for users
        List<User> users = new ArrayList<>();

        // Adding sample users
        User user1 = new User();
        user1.id = "1";
        user1.name = "Alex Linderson";
        user1.email = "alex.linderson@example.com";
        user1.image = getImageBitmap(R.drawable.user);  // Store Bitmap directly

        User user2 = new User();
        user2.id = "2";
        user2.name = "Sabila Sayma";
        user2.email = "sabila.sayma@example.com";
        user2.image = getImageBitmap(R.drawable.user);  // Store Bitmap directly

        User user3 = new User();
        user3.id = "3";
        user3.name = "Angel Dayna";
        user3.email = "angel.dayna@example.com";
        user3.image = getImageBitmap(R.drawable.user);  // Store Bitmap directly

        User user4 = new User();
        user4.id = "4";
        user4.name = "John Ahraham";
        user4.email = "john.ahraham@example.com";
        user4.image = getImageBitmap(R.drawable.user);  // Store Bitmap directly

        // Adding to the list
        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);

        return users;
    }



    private Bitmap getImageBitmap(int resourceId) {
        // Decode the image resource directly into a Bitmap
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            return null; // Handle the case where the bitmap is null
        }

        // Convert Bitmap to Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }
}
