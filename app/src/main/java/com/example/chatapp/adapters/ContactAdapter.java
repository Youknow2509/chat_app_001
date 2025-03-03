package com.example.chatapp.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.chatapp.activities.CallingActivity;
import com.example.chatapp.databinding.ItemContactAdapterBinding;
import com.example.chatapp.databinding.ItemContainerUserBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.UserViewHolder> {
    private final List<User> users;
    private final UserListener userListener;

    public ContactAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContactAdapterBinding itemContainerUserBinding = ItemContactAdapterBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContactAdapterBinding binding;

        UserViewHolder(ItemContactAdapterBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.imageProfile.setImageBitmap(user.image);

            // Xử lý sự kiện khi nhấn vào nút gọi thoại
            binding.buttoncall.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), CallingActivity.class);
                intent.putExtra("CALL_TYPE", "audio"); // Truyền kiểu cuộc gọi
                intent.putExtra("USER_NAME", user.name);
                v.getContext().startActivity(intent);
            });

            // Xử lý sự kiện khi nhấn vào nút gọi video
            binding.buttonvideo.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), CallingActivity.class);
                intent.putExtra("CALL_TYPE", "video"); // Truyền kiểu cuộc gọi
                intent.putExtra("USER_NAME", user.name);
                v.getContext().startActivity(intent);
            });
        }
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