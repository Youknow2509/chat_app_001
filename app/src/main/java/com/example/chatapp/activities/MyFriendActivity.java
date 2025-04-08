package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.R;
import com.example.chatapp.adapters.UserFriendAdapter;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.databinding.ActivityMyFriendBinding;
import com.example.chatapp.dto.UserFriendDto;
import com.example.chatapp.utils.session.SessionManager;
import com.example.chatapp.viewmodel.UserFriendViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MyFriendActivity extends AppCompatActivity {

    private ActivityMyFriendBinding binding;

    private UserFriendAdapter userFriendListAdapter;

    private SessionManager sessionManager;
    private ApiManager apiManager;
    private List<UserFriendDto> userFriendDtoList;

    private UserFriendViewModel userFriendViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);
        apiManager = new ApiManager(this);

        userFriendDtoList = new ArrayList<>();
        userFriendListAdapter = new UserFriendAdapter(userFriendDtoList);
        binding.recycleViewUser.setAdapter(userFriendListAdapter);

        userFriendViewModel = new ViewModelProvider(this).get(UserFriendViewModel.class);
        observeViewModel();
        binding.recycleViewUser.setLayoutManager(new LinearLayoutManager(this));


        userFriendViewModel.loadUserFriendData(apiManager,sessionManager.getAccessToken());
        binding.FriendInvitationText.setOnClickListener(v->openViewInvitation());
        binding.backIcon.setOnClickListener(v->onBackPressed());
    }

    private void observeViewModel() {
        userFriendViewModel.userFriendLiveData.observe(this, userFriendDtos -> {
            if (userFriendDtos != null && !userFriendDtos.isEmpty()) {
                updateListFriend(userFriendDtos);
                binding.emptyTextView.setVisibility(View.GONE);
                binding.recycleViewUser.setVisibility(View.VISIBLE);
            } else {
                binding.emptyTextView.setVisibility(View.VISIBLE);
                binding.recycleViewUser.setVisibility(View.GONE);
            }
        });
    }

    private void updateListFriend(List<UserFriendDto> userFriendDtos) {
        userFriendDtoList.clear();
        userFriendDtoList.addAll(userFriendDtos);
        userFriendListAdapter.notifyDataSetChanged();
    }

    private void openViewInvitation() {
        // Open the FriendInvitationActivity
        Intent intent = new Intent(this, FriendInvitationActivity.class);
        startActivity(intent);

    }
}