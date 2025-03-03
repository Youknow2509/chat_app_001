package com.example.chatapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ItemContainerGroupBinding;
import com.example.chatapp.databinding.ItemContainerUserBinding;
import com.example.chatapp.models.ChatListItem;
import com.example.chatapp.models.Group;
import com.example.chatapp.models.User;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatListItem> chatListItems;
    private final ChatItemClickListener listener;

    public static final int VIEW_TYPE_USER = 0;
    public static final int VIEW_TYPE_GROUP = 1;

    public interface ChatItemClickListener {
        void onUserClick(User user);
        void onGroupClick(Group group);
    }

    public ChatListAdapter(List<ChatListItem> chatListItems, ChatItemClickListener listener) {
        this.chatListItems = chatListItems;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return chatListItems.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_GROUP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            return new UserViewHolder(ItemContainerUserBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));
        } else {
            return new GroupViewHolder(ItemContainerGroupBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_USER) {
            ((UserViewHolder) holder).setData(chatListItems.get(position).getUser());
        } else {
            ((GroupViewHolder) holder).setData(chatListItems.get(position).getGroup());
        }
    }

    @Override
    public int getItemCount() {
        return chatListItems.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerUserBinding binding;

        UserViewHolder(ItemContainerUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(User user) {
            binding.textName.setText(user.name);
            binding.getRoot().setOnClickListener(v -> listener.onUserClick(user));
        }
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerGroupBinding binding;

        GroupViewHolder(ItemContainerGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(Group group) {
            binding.textName.setText(group.getName());
            binding.getRoot().setOnClickListener(v -> listener.onGroupClick(group));
        }
    }
}
