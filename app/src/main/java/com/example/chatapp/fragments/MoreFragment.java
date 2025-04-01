package com.example.chatapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activities.AddFriendActivity;
import com.example.chatapp.activities.ChatConversationActivity;
import com.example.chatapp.activities.ChatGroupConversationActivity;
import com.example.chatapp.activities.CreateNewGroupActivity;
import com.example.chatapp.activities.SearchingActivity;
import com.example.chatapp.adapters.ChatListAdapter;
import com.example.chatapp.adapters.GroupListAdapter;
import com.example.chatapp.databinding.FragmentGroupBinding;
import com.example.chatapp.databinding.FragmentMoreBinding;
import com.example.chatapp.models.ChatListItem;
import com.example.chatapp.models.GroupListItem;
import com.example.chatapp.models.User;
import com.example.chatapp.models.Group;
import com.example.chatapp.consts.Constants;

import java.util.ArrayList;
import java.util.List;

public class MoreFragment extends Fragment {

    private GroupListAdapter chatListAdapter;

    private FragmentMoreBinding binding;
    private List<GroupListItem> chatListItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        chatListAdapter = new GroupListAdapter(chatListItems, new GroupListAdapter.ChatItemClickListener() {
            @Override
            public void onGroupClick(Group group) {
                Intent intent = new Intent(getContext(), ChatGroupConversationActivity.class);
                intent.putExtra(Constants.KEY_GROUP, group);
                startActivity(intent);
            }
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

        binding.searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchingActivity.class);
            intent.putExtra("FRAGMENT_TYPE", "MessageFragment");
            startActivity(intent);
        });

        return view;
    }
}
