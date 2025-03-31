package com.example.chatapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ItemContainerGroupBinding;
import com.example.chatapp.databinding.ItemContainerUserBinding;
import com.example.chatapp.databinding.ItemUserChatBinding;
import com.example.chatapp.models.ChatListItem;
import com.example.chatapp.models.Group;
import com.example.chatapp.models.User;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatListItem> chatListItems;
    private final ChatItemClickListener listener;

    public static final int VIEW_TYPE_USER = 0;

    public interface ChatItemClickListener {
        void onUserClick(User user);
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
            return new UserViewHolder(ItemUserChatBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_USER) {
            ((UserViewHolder) holder).setData(chatListItems.get(position).getUser());
        }
    }

    @Override
    public int getItemCount() {
        return chatListItems.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserChatBinding binding;

        UserViewHolder(ItemUserChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(User user) {
            binding.nameText.setText(user.name);
            binding.getRoot().setOnClickListener(v -> listener.onUserClick(user));
        }
    }
}
