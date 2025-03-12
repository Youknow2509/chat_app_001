package com.example.chatapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activities.ChatActivity;
import com.example.chatapp.activities.SearchingActivity;
import com.example.chatapp.adapters.ChatListAdapter;
import com.example.chatapp.models.ChatListItem;
import com.example.chatapp.models.User;
import com.example.chatapp.models.Group;
import com.example.chatapp.consts.Constants;

import java.util.ArrayList;
import java.util.List;

public class Message extends Fragment {

    private RecyclerView chatListRecyclerView;
    private ChatListAdapter chatListAdapter;
    private ImageView searchIcon;
    private List<ChatListItem> chatListItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        chatListRecyclerView = view.findViewById(R.id.userRecyclerView);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchIcon = view.findViewById(R.id.search);

        // Lấy dữ liệu Users & Groups chung vào danh sách ChatListItem
        chatListItems = getChatListItems();

        // Khởi tạo Adapter chung
        chatListAdapter = new ChatListAdapter(chatListItems, new ChatListAdapter.ChatItemClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(Constants.KEY_USER, user);
                startActivity(intent);
            }

            @Override
            public void onGroupClick(Group group) {
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(Constants.KEY_GROUP, group);
                intent.putExtra("GROUP_ID", group.id);
                intent.putExtra("GROUP_NAME", group.name);
                startActivity(intent);
            }
        });

        chatListRecyclerView.setAdapter(chatListAdapter);

        searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchingActivity.class);
            intent.putExtra("FRAGMENT_TYPE", "MessageFragment");
            startActivity(intent);
        });

        return view;
    }

    private List<ChatListItem> getChatListItems() {
        List<ChatListItem> chatListItems = new ArrayList<>();

        // Thêm Users vào danh sách chung
        for (User user : getUsers()) {
            chatListItems.add(new ChatListItem(user));
        }

        // Thêm Groups vào danh sách chung
        for (Group group : getGroups()) {
            chatListItems.add(new ChatListItem(group));
        }

        return chatListItems;
    }

    private List<User> getUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("1", "Alex Linderson", "alex.linderson@example.com", getImageBitmap(R.drawable.user)));
        users.add(new User("2", "Sabila Sayma", "sabila.sayma@example.com", getImageBitmap(R.drawable.user)));
        users.add(new User("3", "Angel Dayna", "angel.dayna@example.com", getImageBitmap(R.drawable.user)));
        users.add(new User("4", "John Ahraham", "john.ahraham@example.com", getImageBitmap(R.drawable.user)));
        return users;
    }

    private List<Group> getGroups() {
        List<Group> groups = new ArrayList<>();
        List<User> group1Members = new ArrayList<>();
        group1Members.add(new User("1", "Alex Linderson", "alex.linderson@example.com", getImageBitmap(R.drawable.user)));
        group1Members.add(new User("2", "Sabila Sayma", "sabila.sayma@example.com", getImageBitmap(R.drawable.user)));

        List<User> group2Members = new ArrayList<>();
        group2Members.add(new User("3", "Angel Dayna", "angel.dayna@example.com", getImageBitmap(R.drawable.user)));
        group2Members.add(new User("4", "John Ahraham", "john.ahraham@example.com", getImageBitmap(R.drawable.user)));

        groups.add(new Group("101", "Android Dev Group", group1Members));
        groups.add(new Group("102", "Java Enthusiasts", group2Members));

        return groups;
    }

    private Bitmap getImageBitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }
}
