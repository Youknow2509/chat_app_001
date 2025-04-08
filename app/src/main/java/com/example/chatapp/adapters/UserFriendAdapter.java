package com.example.chatapp.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.databinding.ItemContainerChatUserBinding;
import com.example.chatapp.databinding.ItemContainerUserBinding;
import com.example.chatapp.databinding.ItemFriendInListBinding;
import com.example.chatapp.dto.UserFriendDto;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;

import java.util.List;

public class UserFriendAdapter extends RecyclerView.Adapter<UserFriendAdapter.UserViewHolder> {
    private final List<UserFriendDto> users;

    public UserFriendAdapter(List<UserFriendDto> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public UserFriendAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFriendInListBinding itemContainerUserBinding = ItemFriendInListBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserFriendAdapter.UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size(); // Sửa từ return 0 thành return users.size()
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemFriendInListBinding binding;

        UserViewHolder(ItemFriendInListBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setData(UserFriendDto user) {
            binding.tvUsername.setText(user.getName());
            binding.tvEmail.setText(user.getEmail());

            // Thêm xử lý khi click vào nút ba chấm
            binding.imgMenu.setOnClickListener(v -> {
                showDeleteDialog(user);
            });

            Glide.with(binding.getRoot().getContext())
                    .load(user.getImage())
                    .into(binding.imgAvatar);
        }

        private void showDeleteDialog(UserFriendDto user) {
            AlertDialog.Builder builder = new AlertDialog.Builder(binding.getRoot().getContext());
            builder.setTitle("Xóa bạn bè")
                    .setMessage("Bạn có chắc chắn muốn xóa người bạn này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Thêm code xử lý xóa bạn bè ở đây
                        // Ví dụ: gọi API xóa bạn bè
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            users.remove(position);
                            notifyItemRemoved(position);
                        }
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
    }
}

