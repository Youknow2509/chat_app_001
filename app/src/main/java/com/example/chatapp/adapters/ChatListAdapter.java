package com.example.chatapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.databinding.ItemContainerGroupBinding;
import com.example.chatapp.databinding.ItemContainerUserBinding;
import com.example.chatapp.databinding.ItemUserChatBinding;
import com.example.chatapp.dto.ChatDTO;
import com.example.chatapp.models.ChatListItem;
import com.example.chatapp.models.Group;
import com.example.chatapp.models.User;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatListItem> chatListItems;
    private final ChatItemClickListener listener;

    public static final int VIEW_TYPE_USER = 0;

    public interface ChatItemClickListener {
        void onUserClick(ChatDTO chatDTO);
    }

    public ChatListAdapter(List<ChatListItem> chatListItems, ChatItemClickListener listener) {
        this.chatListItems = chatListItems;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ChatDTOViewHolder(ItemUserChatBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_USER) {
            ((ChatDTOViewHolder) holder).setData(chatListItems.get(position).getChatDTO());
        }
    }

    @Override
    public int getItemCount() {
        return chatListItems.size();
    }

//    class UserViewHolder extends RecyclerView.ViewHolder {
//        private final ItemUserChatBinding binding;
//
//        UserViewHolder(ItemUserChatBinding binding) {
//            super(binding.getRoot());
//            this.binding = binding;
//        }
//
//        void setData(User user) {
//            binding.nameText.setText(user.name);
//            binding.getRoot().setOnClickListener(v -> listener.onUserClick(user));
//        }
//    }

    class ChatDTOViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserChatBinding binding;

        ChatDTOViewHolder(ItemUserChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatDTO chatDTO){
            binding.nameText.setText(chatDTO.getChatName());
            Glide.with(binding.getRoot().getContext())
                    .load(chatDTO.getAvatar())
                    .into(binding.avatarImage);
            binding.getRoot().setOnClickListener(v -> listener.onUserClick(chatDTO));
        }
    }
}
