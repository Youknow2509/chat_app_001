package com.example.chatapp.activities;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;
import com.example.chatapp.models.Group;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {


    private ActivityChatBinding binding;
    private User receiverUser;
    private Group receiverGroup;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    private PreferenceManager preferenceManager;
    private String conversionId = null;
    private Boolean isReceiverAvailable = false;

    private String id_client_test = "0";
    private boolean isGroupChat = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadChatDetails();
        binding.progressBar.setVisibility(View.GONE);
        init();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                isGroupChat ? null : receiverUser.image,
                isGroupChat ? preferenceManager.getString(Constants.KEY_GROUP_ID) : preferenceManager.getString(Constants.KEY_USER_ID),
                isGroupChat
        );
        binding.chatRecycleView.setAdapter(chatAdapter);
        addSampleMessages();
    }


    private Bitmap getBitmap(Bitmap bitmapImage) {
        return bitmapImage != null ? bitmapImage : null;
    }

    private void loadChatDetails() {
        if (getIntent().hasExtra(Constants.KEY_GROUP)) {
            receiverGroup = (Group) getIntent().getSerializableExtra(Constants.KEY_GROUP);
            isGroupChat = true;
            binding.textName.setText(receiverGroup.getName());
        } else {
            receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
            assert receiverUser != null;
            binding.textName.setText(receiverUser.name);
            binding.imageInfo.setImageBitmap(getBitmap(receiverUser.image));
        }
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.btnAudioCall.setOnClickListener(v -> initiateCall("audio"));
        binding.btnVideoCall.setOnClickListener(v -> initiateCall("video"));
    }

    private void initiateCall(String callType) {
        if (!isReceiverAvailable) {
            Intent intent = new Intent(ChatActivity.this, CallingActivity.class);
            intent.putExtra("CALL_TYPE", callType);

            if (isGroupChat) {
                intent.putExtra("IS_GROUP_CALL", true);
                intent.putExtra("GROUP_ID", receiverGroup.id);
                intent.putExtra("GROUP_NAME", receiverGroup.name);
            } else {
                intent.putExtra("IS_GROUP_CALL", false);
                intent.putExtra("USER_ID", receiverUser.id);
                intent.putExtra("USER_NAME", receiverUser.name);
            }

            startActivity(intent);
        } else {
            showToast("User is not available for calling.");
        }
    }


    private void addSampleMessages() {
        if (isGroupChat) {
            ChatMessage groupMessage1 = new ChatMessage();
            groupMessage1.message = "Hey everyone!";
            groupMessage1.senderId = "2";
            groupMessage1.conversionName = "Alice";
            groupMessage1.receiverId = receiverGroup.id;
            groupMessage1.dateTime = getReadableDateTime(new Date());
            chatMessages.add(groupMessage1);

            ChatMessage groupMessage2 = new ChatMessage();
            groupMessage2.message = "Hello Alice!";
            groupMessage2.senderId = "3";
            groupMessage2.conversionName = "Bob";
            groupMessage2.receiverId = receiverGroup.id;
            groupMessage2.dateTime = getReadableDateTime(new Date());
            chatMessages.add(groupMessage2);
        } else {
            ChatMessage receivedMessage = new ChatMessage();
            receivedMessage.message = "Hello, how are you?";
            receivedMessage.senderId = receiverUser.id;
            receivedMessage.receiverId = id_client_test;
            receivedMessage.dateTime = getReadableDateTime(new Date());
            chatMessages.add(receivedMessage);

            ChatMessage sentMessage = new ChatMessage();
            sentMessage.message = "I'm fine, thank you!";
            sentMessage.senderId = id_client_test;
            sentMessage.receiverId = receiverUser.id;
            sentMessage.dateTime = getReadableDateTime(new Date());
            chatMessages.add(sentMessage);
        }

        chatAdapter.notifyDataSetChanged();
        binding.chatRecycleView.setVisibility(View.VISIBLE);
    }

    private void sendMessage() {
        String messageText = binding.inputMessage.getText().toString();
        if (messageText.isEmpty()) {
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.senderId = id_client_test;
        chatMessage.message = messageText;
        chatMessage.dateTime = getReadableDateTime(new Date());
        chatMessage.dateObject = new Date();

        if (isGroupChat) {
            chatMessage.receiverId = receiverGroup.id;
            chatMessage.conversionName = "You";
        } else {
            chatMessage.receiverId = receiverUser.id;
        }

        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
        binding.inputMessage.setText(null);

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
