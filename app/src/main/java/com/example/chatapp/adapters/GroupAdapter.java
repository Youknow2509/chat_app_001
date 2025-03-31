package com.example.chatapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ItemGroupChatBinding;
import com.example.chatapp.listeners.GroupListener;
import com.example.chatapp.models.Group;
import java.util.List;

import com.example.chatapp.databinding.ItemContainerGroupBinding;


public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final List<Group> groups;

    private final GroupListener groupListener;

    public GroupAdapter(List<Group> groups, GroupListener groupListener) {
        this.groups = groups;
        this.groupListener = groupListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupChatBinding binding = ItemGroupChatBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new GroupViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.setGroupData(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void updateList(List<Group> newList) {
        groups.clear();
        groups.addAll(newList);
        notifyDataSetChanged();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        ItemGroupChatBinding binding;

        public GroupViewHolder(ItemGroupChatBinding itemGroupChatBinding) {
            super(itemGroupChatBinding.getRoot());
            binding = itemGroupChatBinding;
        }

        public void setGroupData(Group group) {
            binding.nameText.setText(group.name);
            binding.getRoot().setOnClickListener(v -> groupListener.onGroupClick(group));
        }
    }
}
