package com.example.chatapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.chatapp.R;
import com.example.chatapp.activities.SearchingActivity;
import com.example.chatapp.adapters.ContactAdapter;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class CallFragment extends Fragment {

    private RecyclerView userRecyclerView;
    private ContactAdapter contactAdapter;
    private List<User> users;
    private ImageView searchIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_call, container, false);

        // Initialize the RecyclerView
        userRecyclerView = view.findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get users list
        users = getUsers();  // Get list of users
        searchIcon = view.findViewById(R.id.search);
        // Initialize the adapter
        contactAdapter = new ContactAdapter(users, new UserListener() {
            @Override
            public void onUserClick(User user) {

            }

            @Override
            public void initiateVideoMeeting(User user) {
                // Initiate video meeting
            }

            @Override
            public void initiateAudioMeeting(User user) {
                // Initiate audio meeting
            }

            @Override
            public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
                // Hide/show action views based on selection
            }
        });
        userRecyclerView.setAdapter(contactAdapter);
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchingActivity.class);
                intent.putExtra("FRAGMENT_TYPE", "MessageFragment");
                startActivity(intent);
            }
        });
        return view;
    }

    public List<User> getUsers() {
        // Sample data for users
        List<User> users = new ArrayList<>();

        // Adding sample users
        User user1 = new User();
        user1.id = "1";
        user1.name = "Alex Linderson";
        user1.image = getImageBitmap(R.drawable.user);  // Store Bitmap directly

        User user2 = new User();
        user2.id = "2";
        user2.name = "Sabila Sayma";
        user2.image = getImageBitmap(R.drawable.user);  // Store Bitmap directly

        User user3 = new User();
        user3.id = "3";
        user3.name = "Angel Dayna";
        user3.image = getImageBitmap(R.drawable.user);  // Store Bitmap directly

        User user4 = new User();
        user4.id = "4";
        user4.name = "John Ahraham";
        user4.image = getImageBitmap(R.drawable.user);  // Store Bitmap directly

        users.add(user1);
        users.add(user2);
        users.add(user3);
        users.add(user4);

        return users;
    }

    private Bitmap getImageBitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }
}