package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.bumptech.glide.Glide;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.databinding.ItemUserAddfriendBinding;
import com.example.chatapp.dto.UserDto;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.TokenClient;
import com.example.chatapp.models.User;
import com.example.chatapp.models.response.ResponseData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {
    private final List<UserDto> users;
    private final UserListener userListener;

    private final ApiManager apiManager;

    private final String token;
    ItemUserAddfriendBinding binding;

    public UserSearchAdapter(List<UserDto> users,ApiManager apiManager, String token, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
        this.apiManager = apiManager;
        this.token = token;

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

        UserViewHolder(ItemUserAddfriendBinding itemContainerUserBinding) {
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(UserDto user, int position) {
            binding.nameText.setText(user.getName());
            binding.emailText.setText(user.getEmail());
            Glide.with(binding.getRoot().getContext())
                    .load(user.getImageUrl())
                    .into(binding.avatarImage);

            binding.addIcon.setOnClickListener(v -> {
                binding.addIcon.setVisibility(View.GONE);
                binding.revokeIcon.setVisibility(View.VISIBLE);
                createFriendRequest(user);
            });
            binding.revokeIcon.setOnClickListener(v -> {
                binding.revokeIcon.setVisibility(View.GONE);
                binding.addIcon.setVisibility(View.VISIBLE);
                revokeFriendRequest(user);
            });
        }
    }

    private void createFriendRequest(UserDto user) {
        apiManager.createFriendRequest(token, user.getId(), user.getEmail(), new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResponseData<Object> responseData = response.body();
                    if (responseData.getCode() == 20001) {
                        Toast.makeText(binding.getRoot().getContext(), "Gửi lời mời kết bạn thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(binding.getRoot().getContext(), "Gửi lời mời kết bạn thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {

            }
        });
    }

    private void revokeFriendRequest(UserDto user) {
        apiManager.endFriendRequest(token, user.getId(), new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResponseData<Object> responseData = response.body();
                    if (responseData.getCode() == 20001) {
                        Toast.makeText(binding.getRoot().getContext(), "Hủy lời mời kết bạn thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(binding.getRoot().getContext(), "Hủy lời mời kết bạn thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {

            }
        });
    }

    public void updateList(List<UserDto> newList) {
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
