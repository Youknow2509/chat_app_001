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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        chatDTOList = new ArrayList<>();
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

        getUsersFromApi();

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

    private Bitmap getImageBitmap(int resourceId) {
        return BitmapFactory.decodeResource(getResources(), resourceId);
    }


    private void getUsersFromApi() {
        // Demo du lieu chat

//        users.add(new User("1", "Alex Linderson", "alex.linderson@example.com", getImageBitmap(R.drawable.user)));
//        chatListItems = getChatListItems();
//        chatListAdapter = new ChatListAdapter(chatListItems, new ChatListAdapter.ChatItemClickListener() {
//            @Override
//            public void onUserClick(User user) {
//                Intent intent = new Intent(getContext(), ChatConversationActivity.class);
//                intent.putExtra(Constants.KEY_USER, user);
//                startActivity(intent);
//            }
//        });


        // Gọi API để lấy danh sách chat

        apiManager.getListChatPrivateForUser(sessionManager.getAccessToken(), 10, 1, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResponseData<Object> responseData = response.body();
                    if (responseData.getCode() == 20001 && "success".equals(responseData.getMessage())) {
                        // Parse chat data
                        chatDTOList = new ArrayList<>();
                        Gson gson = new Gson();
                        JsonElement jsonElement = JsonParser.parseString(gson.toJson(responseData.getData()));
                        JsonArray jsonArray = jsonElement.getAsJsonArray();

                        if (jsonArray != null && jsonArray.size() > 0) {
                            Type listType = new TypeToken<List<ChatDTO>>() {}.getType();
                            chatDTOList = gson.fromJson(jsonArray, listType);
                        }

                        // Update UI with chat data
                        chatListItems = getChatListItems();

                        // Cập nhật adapter hiện tại thay vì tạo mới
                        chatListAdapter = new ChatListAdapter(chatListItems, new ChatListAdapter.ChatItemClickListener() {
                            @Override
                            public void onUserClick(ChatDTO chatDTO) {
                                Intent intent = new Intent(getContext(), ChatConversationActivity.class);
                                intent.putExtra(Constants.KEY_CHAT, chatDTO);
                                startActivity(intent);
                            }
                        });
                        binding.chatRecyclerView.setAdapter(chatListAdapter);
                        chatListAdapter.notifyDataSetChanged();

                        // Log để kiểm tra
                        Log.d("ChatFragment", "Loaded " + chatListItems.size() + " chat items");
                    } else {
                        Log.d("ChatFragment", "Error: " + responseData.getMessage());
                    }
                } else {
                    Log.d("ChatFragment", "Request failed: " + response.message());
                }
            }


            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                Log.d("ChatFragment", "Request failed: " + t.getMessage());
            }
        });
    }

}
