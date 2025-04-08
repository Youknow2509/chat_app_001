package com.example.chatapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.example.chatapp.R;
import com.example.chatapp.activities.AddFriendActivity;
import com.example.chatapp.activities.ChatGroupConversationActivity;
import com.example.chatapp.activities.CreateNewGroupActivity;

import com.example.chatapp.activities.LoginActivity;
import com.example.chatapp.activities.MyFriendActivity;
import com.example.chatapp.activities.SearchingActivity;
import com.example.chatapp.activities.StorageManagerActivity;
import com.example.chatapp.adapters.GroupListAdapter;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.databinding.FragmentMoreBinding;
import com.example.chatapp.models.GroupListItem;
import com.example.chatapp.models.Group;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.service.TokenRefreshService;
import com.example.chatapp.utils.session.SessionManager;

import java.util.List;

public class MoreFragment extends Fragment {

    private GroupListAdapter chatListAdapter;

    private FragmentMoreBinding binding;
    private List<GroupListItem> chatListItems;
    //
    private SessionManager sessionManager;
    private ApiManager apiManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        apiManager = new ApiManager(this.getContext());
        chatListAdapter = new GroupListAdapter(chatListItems, new GroupListAdapter.ChatItemClickListener() {
            @Override
            public void onGroupClick(Group group) {
                Intent intent = new Intent(getContext(), ChatGroupConversationActivity.class);
                intent.putExtra(Constants.KEY_GROUP, group);
                startActivity(intent);
            }
        });

        initVariable();
        // add listener to elemenet
        listenEventHandle();

        return view;
    }

    /**
     * Init variable use
     */
    private void initVariable() {
        sessionManager = new SessionManager(getContext());
    }

    /**
     * Handle event of element - click, ...
     */
    private void listenEventHandle() {
        // Cau hinh option menu tren toolbar
        binding.addIcon.setOnClickListener(v -> {
            // Tạo popup menu
            PopupMenu popupMenu = new PopupMenu(getContext(), binding.addIcon);

            // Inflate menu resource
            popupMenu.getMenuInflater().inflate(R.menu.option_add_menu, popupMenu.getMenu());

            // Xoay icon 45 độ
            binding.addIcon.animate().rotation(45).setDuration(200);

            // Xử lý sự kiện khi đóng menu
            popupMenu.setOnDismissListener(menu -> {
                // Xoay icon về vị trí ban đầu
                binding.addIcon.animate().rotation(0).setDuration(200);
            });

            // Xử lý sự kiện khi chọn item trong menu
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_add_friend) {
                    Intent intent = new Intent(getContext(), AddFriendActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.action_create_group) {
                    Intent intent = new Intent(getContext(), CreateNewGroupActivity.class);
                    intent.putExtra("CREATE_GROUP", true);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            // Hiển thị popup menu
            popupMenu.show();
        });

        binding.searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchingActivity.class);
            intent.putExtra("FRAGMENT_TYPE", "MessageFragment");
            startActivity(intent);
        });

        // logout
        binding.logoutLayout.setOnClickListener(v -> {
            // Dừng TokenRefreshService
            Intent serviceIntent = new Intent(this.getContext(), TokenRefreshService.class);
            this.getContext().stopService(serviceIntent);

            apiManager.deleteToken(sessionManager.getFbToken());
            this.sessionManager.logout();

            // redirect to OnboardingActivity
            startActivity(new Intent(this.getContext(), LoginActivity.class));
            this.getActivity().finish();
        });

        //Friend
        binding.inviteFriendsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MyFriendActivity.class);
            startActivity(intent);
        });

        // data and storage
        binding.dataStorageLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StorageManagerActivity.class);
            startActivity(intent);
        });

        // refresh token
        binding.refreshTokenLayout.setOnClickListener(this::handleRefreshToken);
    }

    /**
     * Refresh token
     * Call worker service to refresh token
     */
    private void handleRefreshToken(View v){
//        sessionManager.saveAuthData(
//                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NDI3MzAwODYsImp0aSI6ImJiMzI0MjYxLTg4NTMtNGI3Yy04ZDFhLWQwOTE1M2RiY2QxOCIsImlhdCI6MTc0MjcwMTI4NiwiaXNzIjoiZ28tZWNvbW1lcmNlIiwic3ViIjoiYjE1YWE1MjMtN2UyMS00YmRiLTg4YjctN2FkYjcyM2U4ZDlhOmNsaXRva2VuOjZkZWQ0MGFjMzExODRmNzVhNTE0NTQ3ZDYxYmU3ZDgxIiwidXNlcl9pZCI6ImIxNWFhNTIzLTdlMjEtNGJkYi04OGI3LTdhZGI3MjNlOGQ5YSJ9.BKrDjt7jjNgvEf1BKVUdq5gxRgWZsQvZDnEo4UpLvHM",
//                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NDQyMDkxNjMsImp0aSI6ImRhNTBjZjM1LWM0MWUtNGIxZC05ODY0LTgxZWE5ZTEzOTNkZCIsImlhdCI6MTc0MzYwNDM2MywiaXNzIjoiZ28tZWNvbW1lcmNlIiwic3ViIjoiYmQyOTZiMTAtODFjZi00NzdmLTgyMzYtNThmMjkyMjQ0NTdlIiwidXNlcl9pZCI6IiJ9.qobtDjYLhIBAvM8JMl6PUQpQiOvXNCz9ldb2NqWHPj0",
//                sessionManager.getUserId()
//        );
        Intent serviceIntent = new Intent(this.getContext(), TokenRefreshService.class);
        this.getContext().startService(serviceIntent);
    }
}
