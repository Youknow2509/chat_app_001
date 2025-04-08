package com.example.chatapp.fragments;

import static com.example.chatapp.consts.Constants.KEY_TYPE_CALL;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.chatapp.dto.ChatDTO;
import com.example.chatapp.models.request.AccountModels.*;
import com.example.chatapp.models.request.ChatModels.*;
import com.example.chatapp.models.request.TokenModels.*;
import com.example.chatapp.models.request.UserModels.*;
import com.example.chatapp.models.response.ResponseData;
import androidx.appcompat.widget.PopupMenu;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activities.AddFriendActivity;
import com.example.chatapp.activities.CallOrVideoCallActivity;
import com.example.chatapp.activities.ChatConversationActivity;
import com.example.chatapp.activities.CreateNewGroupActivity;
import com.example.chatapp.activities.SearchingActivity;
import com.example.chatapp.adapters.ChatListAdapter;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.databinding.FragmentChatV2Binding;
import com.example.chatapp.models.ChatListItem;
import com.example.chatapp.models.User;
import com.example.chatapp.models.Group;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.request.ChatModels;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.session.SessionManager;
import com.example.chatapp.viewmodel.ChatViewModel;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {
    private ChatListAdapter chatListAdapter;
    private List<ChatListItem> chatListItems;
    private ApiManager apiManager;
    private SessionManager sessionManager;
    List<ChatListItem> users;
    private List<ChatDTO> chatDTOList;
    private FragmentChatV2Binding binding;
    private ChatViewModel viewModel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        chatDTOList = new ArrayList<>();
    }

    private void observeViewModel() {
        viewModel.chatList.observe(getViewLifecycleOwner(), chatList -> {
            if (chatList != null) {
                updateChatList(chatList);
                binding.chatRecyclerView.setAdapter(chatListAdapter);
                chatListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateChatList(List<ChatDTO> chatList) {
        chatDTOList = chatList;
        chatListItems = getChatListItems();
        chatListAdapter = new ChatListAdapter(chatListItems, new ChatListAdapter.ChatItemClickListener() {
            @Override
            public void onUserClick(ChatDTO chatDTO) {
                Intent intent = new Intent(getContext(), ChatConversationActivity.class);
                intent.putExtra(Constants.KEY_CHAT, chatDTO);
                startActivity(intent);
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatV2Binding.inflate(inflater, container, false);
        View view = binding.getRoot();
        apiManager = new ApiManager(getContext());


        chatListItems = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(chatListItems, new ChatListAdapter.ChatItemClickListener() {
            @Override
            public void onUserClick(ChatDTO chatDTO) {
                Intent intent = new Intent(getContext(), ChatConversationActivity.class);
                intent.putExtra(Constants.KEY_CHAT, chatDTO);
                startActivity(intent);
            }
        });

        binding.chatRecyclerView.setAdapter(chatListAdapter);

        binding.searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchingActivity.class);
            intent.putExtra("FRAGMENT_TYPE", "MessageFragment");
            startActivity(intent);
        });

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        observeViewModel();

        // Thay thế getUsersFromApi() bằng:
        viewModel.loadChatData(apiManager, sessionManager.getAccessToken());

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
        for (ChatDTO chatDTO : chatDTOList) {
            ChatListItem chatListItem = new ChatListItem(chatDTO);
            chatListItems.add(chatListItem);
        }


        return chatListItems;
    }

}
