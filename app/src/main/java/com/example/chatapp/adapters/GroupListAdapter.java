package com.example.chatapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ItemContainerGroupBinding;
import com.example.chatapp.databinding.ItemContainerUserBinding;
import com.example.chatapp.databinding.ItemGroupChatBinding;
import com.example.chatapp.databinding.ItemUserChatBinding;
import com.example.chatapp.models.ChatListItem;
import com.example.chatapp.models.Group;
import com.example.chatapp.models.GroupListItem;
import com.example.chatapp.models.User;

import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<GroupListItem> chatListItems;
    private final ChatItemClickListener listener;

    public static final int VIEW_TYPE_USER = 2;

    public interface ChatItemClickListener {
        void onGroupClick(Group group);

    }

    public GroupListAdapter(List<GroupListItem> chatListItems, ChatItemClickListener listener) {
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
            return new GroupViewHolder(ItemGroupChatBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            ));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((GroupViewHolder) holder).setData(chatListItems.get(position).getGroup());
    }

    @Override
    public int getItemCount() {
        return chatListItems.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private final ItemGroupChatBinding binding;

        GroupViewHolder(ItemGroupChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(Group group) {
            binding.nameText.setText(group.getName());
            binding.getRoot().setOnClickListener(v -> listener.onGroupClick(group));
        }
    }
}
