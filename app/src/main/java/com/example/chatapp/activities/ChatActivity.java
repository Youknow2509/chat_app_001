package com.example.chatapp.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    private PreferenceManager preferenceManager;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;

    private String id_client_test = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        binding.progressBar.setVisibility(View.GONE);
        init();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                receiverUser.image,
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecycleView.setAdapter(chatAdapter);
        addSampleMessages();
    }

    private Bitmap getBitmap(Bitmap bitmapImage) {
        if (bitmapImage != null) {
            return bitmapImage;
        } else {
            return null;
        }
    }

    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        assert receiverUser != null;
        binding.textName.setText(receiverUser.name);  // Hiển thị tên người dùng
        binding.imageInfo.setImageBitmap(getBitmap(receiverUser.image));  // Hiển thị ảnh người dùng
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private void addSampleMessages() {
        // Tin nhắn nhận
        ChatMessage receivedMessage = new ChatMessage();
        receivedMessage.message = "Hello, how are you?";
        receivedMessage.senderId = receiverUser.id;
        receivedMessage.receiverId = id_client_test;
        receivedMessage.dateTime = getReadableDateTime(new Date());
        chatMessages.add(receivedMessage);

        // Tin nhắn gửi
        ChatMessage sentMessage = new ChatMessage();
        sentMessage.message = "I'm fine, thank you!";
        sentMessage.senderId = id_client_test;
        sentMessage.receiverId = receiverUser.id;
        sentMessage.dateTime = getReadableDateTime(new Date());
        chatMessages.add(sentMessage);

        // Cập nhật RecyclerView
        chatAdapter.notifyDataSetChanged();  // Đảm bảo cập nhật RecyclerView

        // Hiển thị RecyclerView sau khi tin nhắn được thêm vào
        binding.chatRecycleView.setVisibility(View.VISIBLE);
    }



    private void sendMessage() {
        String messageText = binding.inputMessage.getText().toString();
        if (messageText.isEmpty()) {
            return;
        }

        // Tạo đối tượng ChatMessage cho tin nhắn
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.senderId = id_client_test;
        chatMessage.receiverId = receiverUser.id;
        chatMessage.message = messageText;
        chatMessage.dateTime = getReadableDateTime(new Date());
        chatMessage.dateObject = new Date();

        // Thêm tin nhắn vào danh sách
        chatMessages.add(chatMessage);

        // Cập nhật giao diện ngay lập tức
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);

        // Xóa nội dung trong input
        binding.inputMessage.setText(null);

        // Giả lập việc gửi thông báo (không dùng Firebase)
        if (!isReceiverAvailable) {
            showToast("Receiver is not available, message sent.");
        }
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
