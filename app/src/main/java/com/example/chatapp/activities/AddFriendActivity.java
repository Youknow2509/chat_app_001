package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.adapters.UserSearchAdapter;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.databinding.ActivityAddFriendBinding;
import com.example.chatapp.dto.UserDto;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;
import com.example.chatapp.utils.session.SessionManager;
import com.example.chatapp.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity  extends AppCompatActivity {

    private ActivityAddFriendBinding binding;

    UserViewModel userViewModel;
    private List<UserDto> users = new ArrayList<>();
    private SessionManager sessionManager;
    private UserSearchAdapter userSearchAdapter;
    private ApiManager apiManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityAddFriendBinding.inflate(getLayoutInflater());
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        sessionManager = SessionManager.getInstance();
        apiManager = new ApiManager(this);
        setContentView(binding.getRoot());
        userSearchAdapter = new UserSearchAdapter(users, new UserListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                finish();
            }

            @Override
            public void initiateVideoMeeting(User user) {

            }

            @Override
            public void initiateAudioMeeting(User user) {

            }

            @Override
            public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {

            }
        });
        binding.chatRecyclerView.setAdapter(userSearchAdapter);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupObservers();
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

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check regex email
                String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
                if (s.toString().matches(emailRegex)) {
                    binding.emailInput.setError(null);
                    searchUsers(s.toString());
                    // Tao du lieu de test
//                    addData();
                } else {
                    binding.emailInput.setError("Invalid email format");
                }
            }
        });

    }

    private void addData() {
        users.clear();
        for (int i = 0; i < 10; i++) {
            UserDto user = new UserDto(String.valueOf(i), "User " + i, "user" + i + "@example.com", "https://example.com/user" + i + ".jpg");
            users.add(user);
        }
        userSearchAdapter.notifyDataSetChanged();
    }

    private void setupObservers() {
        userViewModel.getUserListLiveData().observe(this, userList -> {
            binding.progressBar.setVisibility(View.GONE);
            if (userList != null && !userList.isEmpty()) {
                users.clear();
                users.addAll(userList); // Thêm toàn bộ danh sách
                userSearchAdapter.notifyDataSetChanged();
            }
        });
        userViewModel.getErrorMessage().observe(this, error -> {
            binding.progressBar.setVisibility(View.GONE);
            if (error != null) {
                binding.emailInput.setError(error);
            }
        });
        userViewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }



    // Search user
    private void searchUsers(String input) {
        String token = sessionManager.getAccessToken();
        if (token == null) {
            binding.emailInput.setError("Vui lòng đăng nhập lại");
            return;
        }
        userViewModel.getUser(apiManager, token, input);
        binding.progressBar.setVisibility(View.VISIBLE);
    }
}
