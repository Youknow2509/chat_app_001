package com.example.chatapp.activities;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;
import com.example.chatapp.models.Group;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.observers.MessageObservable;
import com.example.chatapp.observers.MessageObserver;
import com.example.chatapp.utils.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements MessageObserver {

    private MessageObservable messageObservable;
    private ActivityChatBinding binding;
    private User receiverUser;
    private Group receiverGroup;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;

    private PreferenceManager preferenceManager;
    private String conversionId = "789e0123-a456-42f5-b678-556655440000";
    private Boolean isReceiverAvailable = true;

    private String id_client_test = "0";
    private boolean isGroupChat = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "chatActivity: " + conversionId);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadChatDetails();
        binding.progressBar.setVisibility(View.GONE);
        init();

        // Subscribe to message observable
        messageObservable = MessageObservable.getInstance();
        messageObservable.addObserver(this);
    }

    private void init() {
        conversionId = "789e0123-a456-42f5-b678-556655440000";
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

    private void addSampleMessages() {
        binding.chatRecycleView.setVisibility(View.VISIBLE);
    }

    private Bitmap getBitmap(Bitmap bitmapImage) {
        return bitmapImage != null ? bitmapImage : null;
    }

    private void loadChatDetails() {
        if (getIntent().hasExtra(Constants.KEY_GROUP)) {
            receiverGroup = (Group) getIntent().getSerializableExtra(Constants.KEY_GROUP);
            isGroupChat = true;
            binding.textName.setText(receiverGroup.getName());
            // Set conversionId for group chat
            conversionId = receiverGroup.id;
            Log.d(TAG, "Loaded group chat with ID: " + conversionId);
        } else {
            receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
            assert receiverUser != null;
            binding.textName.setText(receiverUser.name);
            binding.imageInfo.setImageBitmap(getBitmap(receiverUser.image));
            // Set conversionId for one-to-one chat
//            conversionId = preferenceManager.getString(Constants.KEY_USER_ID) + "_" + receiverUser.id;
            Log.d(TAG, "Loaded one-to-one chat with ID: " + conversionId);
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


    private void sendMessage() {
        String messageText = binding.inputMessage.getText().toString();
        if (messageText.isEmpty()) {
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(id_client_test);
        chatMessage.setContent(messageText);
        chatMessage.setDateObject(new Date());

        if (isGroupChat) {
            chatMessage.setReceiverId(receiverGroup.id);
            chatMessage.setName("You");
        } else {
            chatMessage.setReceiverId(receiverUser.id);
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

    @Override
    public void onMessageReceived(ChatMessage message) {
        Log.d(TAG, "onMessageReceived: " + message.getContent() + " for chatId: " + message.getChatId()
                + ", my conversionId: " + conversionId);

        if (isMessageRelevantToThisChat(message)) {
            Log.d(TAG, "Message is relevant to this chat, updating UI");
            runOnUiThread(() -> {
                chatMessages.add(message);
                chatAdapter.notifyDataSetChanged();
            });
        } else {
            Log.d(TAG, "Message is NOT relevant to this chat");
        }

    }

    private boolean isMessageRelevantToThisChat(ChatMessage message) {
        if (message == null) {
            Log.e(TAG, "Message is null");
            return false;
        }

        // Check if conversionId is properly set
        if (conversionId == null || conversionId.isEmpty()) {
            Log.e(TAG, "conversionId is not properly set");
            return false;
        }

        // Check for actual match
        boolean isRelevant = conversionId.equals(message.getChatId());
        Log.d(TAG, "Message relevance check: " + isRelevant +
                " (conversionId=" + conversionId + ", messageChatId=" + message.getChatId() + ")");
        return isRelevant;
    }
}
