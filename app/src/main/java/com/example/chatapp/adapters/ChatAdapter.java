package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.databinding.ItemMessageIncomingBinding;
import com.example.chatapp.databinding.ItemMessageIncomingWithAvatarBinding;
import com.example.chatapp.databinding.ItemMessageOutgoingBinding;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.utils.session.SessionManager;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImage;
    private final String senderId;
    private final boolean isGroupChat;
    private final String typeMessage;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static final int VIEW_TYPE_GROUP_RECEIVED = 3;

    private SessionManager sessionManager;

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId, boolean isGroupChat, SessionManager sessionManager, String typeMessage) {
        this.sessionManager = sessionManager;
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.isGroupChat = isGroupChat;
        this.typeMessage = typeMessage;
        setHasStableIds(true);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        if (message.getSenderId().equals(sessionManager.getUserId())) {
            return VIEW_TYPE_SENT;
        } else if (isGroupChat) {
            return VIEW_TYPE_GROUP_RECEIVED;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @Override
    public long getItemId(int position) {
        return chatMessages.get(position).getId() != null ? chatMessages.get(position).getId().hashCode() : position;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_SENT:
                return new SentMessageViewHolder(
                        ItemMessageOutgoingBinding.inflate(inflater, parent, false)
                );

            case VIEW_TYPE_GROUP_RECEIVED:
                return new GroupMessageViewHolder(
                        ItemMessageIncomingWithAvatarBinding.inflate(inflater, parent, false)
                );

            case VIEW_TYPE_RECEIVED:
            default:
                return new ReceiverMessageViewHolder(
                        ItemMessageIncomingBinding.inflate(inflater, parent, false)
                );
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        int viewType = getItemViewType(position);

        if (holder instanceof SentMessageViewHolder && viewType == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(message);
        } else if (holder instanceof GroupMessageViewHolder && viewType == VIEW_TYPE_GROUP_RECEIVED) {
            ((GroupMessageViewHolder) holder).setData(message, receiverProfileImage);
        } else if (holder instanceof ReceiverMessageViewHolder && viewType == VIEW_TYPE_RECEIVED) {
            ((ReceiverMessageViewHolder) holder).setData(message, receiverProfileImage);
        }
    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageOutgoingBinding binding;

        SentMessageViewHolder(ItemMessageOutgoingBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.imageMessage.setVisibility(View.GONE);
            binding.textMessage.setVisibility(View.GONE);
            if ("media".equals(chatMessage.getMessageType())) {
                Log.d("Hehe", "setData: " + chatMessage.getMediaUrl());
                binding.imageMessage.setVisibility(View.VISIBLE);
                binding.textTime.setText(chatMessage.getDateTime());
                Glide.with(binding.imageMessage.getContext())
                        .load(chatMessage.getMediaUrl())
                        .into(binding.imageMessage);
            } else {
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setText(chatMessage.getContent());
                binding.textTime.setText(chatMessage.getDateTime());
            }

        }
    }

    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemMessageIncomingBinding binding;

        ReceiverMessageViewHolder(ItemMessageIncomingBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.textMessage.setText(chatMessage.getContent());
            binding.textTime.setText(chatMessage.getDateTime());
        }
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    static class GroupMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageIncomingWithAvatarBinding binding;

        GroupMessageViewHolder(ItemMessageIncomingWithAvatarBinding itemContainerReceivedGroupBinding) {
            super(itemContainerReceivedGroupBinding.getRoot());
            binding = itemContainerReceivedGroupBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.textSenderName.setText(chatMessage.getChatId());
            binding.textMessage.setText(chatMessage.getContent());
            binding.textTime.setText(chatMessage.getDateTime());

        }
    }
}