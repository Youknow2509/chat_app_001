package com.example.chatapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.PopupMenu;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activities.AddFriendActivity;
import com.example.chatapp.activities.ChatConversationActivity;
import com.example.chatapp.activities.CreateNewGroupActivity;
import com.example.chatapp.activities.SearchingActivity;
import com.example.chatapp.adapters.ChatListAdapter;
import com.example.chatapp.databinding.FragmentChatV2Binding;
import com.example.chatapp.models.ChatListItem;
import com.example.chatapp.models.User;
import com.example.chatapp.models.Group;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.request.ChatModels;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private ChatListAdapter chatListAdapter;
    private List<ChatListItem> chatListItems;

    private FragmentChatV2Binding binding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentChatV2Binding.inflate(getLayoutInflater());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = binding.getRoot();

        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // Lấy dữ liệu Users & Groups chung vào danh sách ChatListItem
        chatListItems = getChatListItems();

        // Khởi tạo Adapter chung
        chatListAdapter = new ChatListAdapter(chatListItems, new ChatListAdapter.ChatItemClickListener() {
            @Override
            public void onUserClick(User user) {
                Intent intent = new Intent(getContext(), ChatConversationActivity.class);
                intent.putExtra(Constants.KEY_USER, user);
                startActivity(intent);
            }
        });

        binding.chatRecyclerView.setAdapter(chatListAdapter);

        binding.searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchingActivity.class);
            intent.putExtra("FRAGMENT_TYPE", "MessageFragment");
            startActivity(intent);
        });

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


        return view;
    }

    private List<ChatListItem> getChatListItems() {
        List<ChatListItem> chatListItems = new ArrayList<>();

        // Thêm Users vào danh sách chung
        for (User user : getUsers()) {
            chatListItems.add(new ChatListItem(user));
        }

        return chatListItems;
    }

    private List<User> getUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("1", "Alex Linderson", "alex.linderson@example.com", getImageBitmap(R.drawable.user)));
        return users;
    }

    private Bitmap getImageBitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }
}
