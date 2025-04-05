package com.example.chatapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.example.chatapp.R;
import com.example.chatapp.activities.AddFriendActivity;
import com.example.chatapp.activities.ChatGroupConversationActivity;
import com.example.chatapp.activities.CreateNewGroupActivity;
import com.example.chatapp.activities.DataStoreFragment;
import com.example.chatapp.activities.LoginActivity;
import com.example.chatapp.activities.SearchingActivity;
import com.example.chatapp.adapters.GroupListAdapter;
import com.example.chatapp.databinding.FragmentMoreBinding;
import com.example.chatapp.models.GroupListItem;
import com.example.chatapp.models.Group;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.utils.session.SessionManager;

import java.util.List;

public class MoreFragment extends Fragment {

    private GroupListAdapter chatListAdapter;

    private FragmentMoreBinding binding;
    private List<GroupListItem> chatListItems;
    //
    private SessionManager sessionManager;


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
            this.sessionManager.logout();

            // redirect to OnboardingActivity
            startActivity(new Intent(this.getContext(), LoginActivity.class));
            this.getActivity().finish();
        });

        // data and storage
        binding.dataStorageLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DataStoreFragment.class);
            startActivity(intent);
        });
    }
}
