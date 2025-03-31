package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.chatapp.databinding.ItemContainerUserBinding;
import com.example.chatapp.databinding.ItemUserAddfriendBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {
    private final List<User> users;
    private final UserListener userListener;

    public UserSearchAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserAddfriendBinding itemContainerUserBinding = ItemUserAddfriendBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position), position);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserAddfriendBinding binding;

        UserViewHolder(ItemUserAddfriendBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user, int position) {
            binding.nameText.setText(user.name);
            binding.emailText.setText(user.email);
            binding.avatarImage.setImageBitmap(user.image);

            binding.addIcon.setOnClickListener(v -> {
                binding.addIcon.setVisibility(View.GONE);
                binding.revokeIcon.setVisibility(View.VISIBLE);
            });
            binding.revokeIcon.setOnClickListener(v -> {
                binding.revokeIcon.setVisibility(View.GONE);
                binding.addIcon.setVisibility(View.VISIBLE);
            });
        }
    }

    public void updateList(List<User> newList) {
        users.clear();
        users.addAll(newList);
        notifyDataSetChanged();
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = 150;
        int height = 150;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return resizedBitmap;
    }
}
