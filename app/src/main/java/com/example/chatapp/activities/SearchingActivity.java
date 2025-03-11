package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.chatapp.R;
import com.example.chatapp.adapters.UserAdapter;
import com.example.chatapp.adapters.GroupAdapter;
import com.example.chatapp.listeners.GroupListener;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;
import com.example.chatapp.models.Group;
import com.example.chatapp.consts.Constants;

import java.util.ArrayList;
import java.util.List;

public class SearchingActivity extends AppCompatActivity {

    private androidx.appcompat.widget.SearchView searchView;
    private androidx.recyclerview.widget.RecyclerView recyclerUsers, recyclerGroups;
    private android.widget.TextView titleUsers, titleGroups;
    private UserAdapter userAdapter;
    private GroupAdapter groupAdapter;
    private List<User> allUsers = new ArrayList<>();
    private List<Group> allGroups = new ArrayList<>();
    private String fragmentType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching);

        // Ánh xạ View
        searchView = findViewById(R.id.searchView);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerGroups = findViewById(R.id.recyclerGroups);
        titleUsers = findViewById(R.id.titleUsers);
        titleGroups = findViewById(R.id.titleGroups);

        // Cấu hình RecyclerView
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerGroups.setLayoutManager(new LinearLayoutManager(this));

        // Nhận fragmentType từ Intent
        fragmentType = getIntent().getStringExtra("FRAGMENT_TYPE");

        // Lấy danh sách Users và Groups
        loadUsersAndGroups();

        // Lắng nghe sự kiện nhập vào SearchView
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterResults(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterResults(newText);
                return true;
            }
        });
    }

    private void loadUsersAndGroups() {
        // Lấy danh sách Users và Groups một cách an toàn
        allUsers = getUsersFromMessageFragmentSafely();
        allGroups = getGroupsFromFragmentSafely();

        // Khởi tạo Adapter cho Users
        userAdapter = new UserAdapter(allUsers, new UserListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(SearchingActivity.this, ChatActivity.class);
                intent.putExtra("USER_ID", user.getId());
                intent.putExtra("USER_NAME", user.getName());
                intent.putExtra("USER_IMAGE", user.getImage());
                startActivity(intent);
            }

            @Override
            public void initiateVideoMeeting(User user) {
                Intent intent = new Intent(SearchingActivity.this, CallingActivity.class);
                intent.putExtra("CALL_TYPE", "video");
                intent.putExtra("USER_ID", user.getId());
                intent.putExtra("USER_NAME", user.getName());
                startActivity(intent);
            }

            @Override
            public void initiateAudioMeeting(User user) {
                Intent intent = new Intent(SearchingActivity.this, CallingActivity.class);
                intent.putExtra("CALL_TYPE", "audio");
                intent.putExtra("USER_ID", user.getId());
                intent.putExtra("USER_NAME", user.getName());
                startActivity(intent);
            }

            @Override
            public void onMultipleUsersAction(Boolean isMultipleUsersSelected) { }
        });

        // Khởi tạo Adapter cho Groups
        groupAdapter = new GroupAdapter(allGroups, new GroupListener() {
            @Override
            public void onGroupClick(Group group) {
                Intent intent = new Intent(SearchingActivity.this, ChatActivity.class);
                intent.putExtra(Constants.KEY_GROUP, group);
                intent.putExtra("GROUP_ID", group.id);
                intent.putExtra("GROUP_NAME", group.name);
                startActivity(intent);
            }
        });

        recyclerUsers.setAdapter(userAdapter);
        recyclerGroups.setAdapter(groupAdapter);
    }
    private List<Group> getGroupsFromFragmentSafely() {
        List<Group> groups = new ArrayList<>();
        List<User> users = new ArrayList<>();

        try {
            users.add(new User("1", "Nguyễn Văn A", "image_url_1"));
            users.add(new User("2", "Trần Thị B", "image_url_2"));
            users.add(new User("3", "Lê Văn C", "image_url_3"));
            users.add(new User("4", "Phạm Thị D", "image_url_4"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            groups.add(new Group("1", "Android Developers",users));
            groups.add(new Group("2", "Java Lovers",users));
            groups.add(new Group("3", "Kotlin Enthusiasts",users));
            groups.add(new Group("4", "Flutter Community",users));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return groups;
    }

    private List<User> getUsersFromMessageFragmentSafely() {
        List<User> users = new ArrayList<>();

        try {
            users.add(new User("1", "Nguyễn Văn A", "image_url_1"));
            users.add(new User("2", "Trần Thị B", "image_url_2"));
            users.add(new User("3", "Lê Văn C", "image_url_3"));
            users.add(new User("4", "Phạm Thị D", "image_url_4"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }




    private void filterResults(String query) {
        List<User> filteredUsers = new ArrayList<>();
        List<Group> filteredGroups = new ArrayList<>();

        // Lọc danh sách Users theo từ khóa tìm kiếm
        for (User user : allUsers) {
            if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredUsers.add(user);
            }
        }

        // Lọc danh sách Groups theo từ khóa tìm kiếm
        for (Group group : allGroups) {
            if (group.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredGroups.add(group);
            }
        }

        // Kiểm tra danh sách Users có kết quả không
        if (!filteredUsers.isEmpty()) {
            userAdapter.updateList(filteredUsers);
            titleUsers.setVisibility(View.VISIBLE);
            recyclerUsers.setVisibility(View.VISIBLE);
        } else {
            titleUsers.setVisibility(View.GONE);
            recyclerUsers.setVisibility(View.GONE);
        }

        // Kiểm tra danh sách Groups có kết quả không
        if (!filteredGroups.isEmpty()) {
            groupAdapter.updateList(filteredGroups);
            titleGroups.setVisibility(View.VISIBLE);
            recyclerGroups.setVisibility(View.VISIBLE);
        } else {
            titleGroups.setVisibility(View.GONE);
            recyclerGroups.setVisibility(View.GONE);
        }
    }

}
