package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.chatapp.databinding.ItemContainerRecentConversionBinding;
import com.example.chatapp.listeners.ConversionListener;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;

    private final ConversionListener conversionListener;
    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }


    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;
        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(ChatMessage chatMessage) {
            // Sử dụng trực tiếp Bitmap cho hình ảnh
            binding.imageProfile.setImageBitmap(chatMessage.getConversionImage());
//            binding.textName.setText(chatMessage.get);
//            binding.textRecentMessage.setText(chatMessage.content);
//
//            binding.getRoot().setOnClickListener(v -> {
//                // Tạo đối tượng User và gọi listener khi người dùng nhấn vào một cuộc trò chuyện
//                User user = new User();
//                user.id = chatMessage.chatId;
//                user.name = chatMessage.conversionName;
//                user.image = chatMessage.conversionImage;  // Sử dụng trực tiếp Bitmap
//                conversionListener.onConversionClicked(user);
//            });
        }

    }

    private Bitmap getConversionImage(String encodedImage) {
        Log.d("ImageDebug", "Encoded Image: " + encodedImage);

        if (encodedImage == null || encodedImage.isEmpty()) {
            // Handle the case where the encodedImage is null or empty
            return null; // or return a default Bitmap
        }

        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
