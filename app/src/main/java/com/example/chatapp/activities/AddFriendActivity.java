package com.example.chatapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.adapters.UserSearchAdapter;
import com.example.chatapp.databinding.ActivityAddFriendBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity  extends AppCompatActivity {

    private ActivityAddFriendBinding binding;

    private UserSearchAdapter userSearchAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityAddFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v-> {
            onBackPressed();
        });

        // Search lang nghe thay doi input de tim
        binding.emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call search function here
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }


    // Add methods to handle adding friends, searching users, etc.

    // Search user
    private void searchUsers(String input) {
        // Mô phỏng tìm kiếm người dùng
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.name = "User " + (i + 1);
            user.email = "user" + (i + 1) + "@example.com";
            userList.add(user);
        }

        // Đẩy vào adapter item_user_addfriend.xml
        if (userSearchAdapter == null) {
            userSearchAdapter = new UserSearchAdapter(userList, new UserListener() {
                @Override
                public void onUserClick(User user) {
                    // Xử lý khi người dùng nhấp vào user
                    // Không cần thêm logic ở đây vì đã xử lý trong adapter
                }

                @Override
                public void initiateVideoMeeting(User user) {
                    // Xử lý nếu cần
                }

                @Override
                public void initiateAudioMeeting(User user) {
                    // Xử lý nếu cần
                }

                @Override
                public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
                    // Xử lý nếu cần
                }
            });
            binding.chatRecyclerView.setAdapter(userSearchAdapter);
        } else {
            userSearchAdapter.updateList(userList);
        }
    }
}
