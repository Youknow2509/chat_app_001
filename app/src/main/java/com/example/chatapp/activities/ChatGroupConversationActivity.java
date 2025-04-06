package com.example.chatapp.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityChatGroupBinding;
import com.example.chatapp.databinding.ActivityChatV2Binding;
import com.example.chatapp.databinding.ItemGroupChatBinding;
import com.example.chatapp.databinding.ItemUserChatBinding;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.Group;
import com.example.chatapp.models.User;
import com.example.chatapp.observers.MessageObserver;
import com.example.chatapp.observers.MessageObservable;
import com.example.chatapp.utils.PreferenceManager;
import com.example.chatapp.utils.session.SessionManager;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatGroupConversationActivity extends AppCompatActivity implements MessageObserver {

    private ActivityChatGroupBinding binding;

    private MessageObservable messageObservable;

    private String conversionId;

    private Group receiverGroup;

    private List<ChatMessage> chatMessages;

    private boolean isGroupChat = false;
    private boolean isReceiverAvailable = true;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private String id_client_test = "0";
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = SessionManager.getInstance();
        binding = ActivityChatGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadChatDetails();
        binding.progressBar.setVisibility(View.GONE);
        init();

        binding.messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ChangeButtonSend();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Subscribe to message observable
        messageObservable = MessageObservable.getInstance();
        messageObservable.addObserver(this);


    }

    private void ChangeButtonSend() {
        if (binding.messageEditText.getText().toString().isEmpty()) {
            binding.micButton.setVisibility(View.VISIBLE);
            binding.sendButton.setVisibility(View.GONE);
        } else {
            binding.micButton.setVisibility(View.GONE);
            binding.sendButton.setVisibility(View.VISIBLE);
        }
    }

    private void setListeners() {
        binding.chatToolbar.backButton.setOnClickListener(v -> onBackPressed());
        binding.micButton.setOnClickListener(v -> SendMessage());
        binding.sendButton.setOnClickListener(v -> SendMessage());
        binding.userVoiceCall.setOnClickListener(v -> VoiceCall());
        binding.userVideoCall.setOnClickListener(v -> VideoCall());
        binding.cameraIcon.setOnClickListener(v -> TakeCamera());
        binding.attachIcon.setOnClickListener(v -> AttachFile());
        binding.chatToolbar.menuButton.setOnClickListener(v -> ShowMore());
    }

    private void ShowMore() {
        // TODO: Implement ShowMore functionality
    }

    private void SendMessage() {
        String messageText = binding.messageEditText.getText().toString();
        if (messageText.isEmpty()) {
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(id_client_test);
        chatMessage.setContent(messageText);
        Date currentDate = new Date();
        // Format the date to a readable string : dd/MM/yyyy HH:mm:ss
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(currentDate);
        chatMessage.setDateTime(formattedDate);

        chatMessage.setReceiverId(receiverGroup.id);

        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
        binding.messageEditText.setText(null);

        // Notify observers about the new message
        showToast("Message sent: " + messageText);

        if (!isReceiverAvailable) {
            showToast("Receiver is not available, message sent.");
        }
    }

    private void VoiceCall() {
        // TODO: Implement VoiceCall functionality
    }

    private void VideoCall() {
        // TODO: Implement VideoCall functionality
    }

    private void TakeCamera() {
        // TODO: Implement TakeCamera functionality
    }

    private void AttachFile() {
        // TODO: Implement AttachFile functionality
    }

    private void Mic() {
        // TODO: Implement Mic functionality
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void init() {
        conversionId = "789e0123-a456-42f5-b678-556655440000";
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                receiverGroup.image,
                preferenceManager.getString(Constants.KEY_GROUP_ID),
                isGroupChat,
                sessionManager
        );
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chatRecyclerView.setAdapter(chatAdapter);
        binding.chatRecyclerView.setVisibility(View.VISIBLE);
    }

    private void loadChatDetails() {
        receiverGroup = (Group) getIntent().getSerializableExtra(Constants.KEY_GROUP);
        assert receiverGroup != null;
        binding.nameGroup.setText(receiverGroup.name);
        binding.userAvatar.setImageBitmap(getBitmap(receiverGroup.image));
        binding.memberCount.setText(String.valueOf(receiverGroup.members.size()));
        // Set conversionId for one-to-one chat
//            conversionId = preferenceManager.getString(Constants.KEY_USER_ID) + "_" + receiverUser.id;
        Log.d(TAG, "Loaded one-to-one chat with ID: " + conversionId);
    }

    private Bitmap getBitmap(Bitmap bitmapImage) {
        return bitmapImage != null ? bitmapImage : null;
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


    @Override
    public void onMessageReceived(ChatMessage message) {
        Log.d(TAG, "onMessageReceived: " + message.getContent() + " for chatId: " + message.getChatId()
                + ", my conversionId: " + conversionId);

        if (isMessageRelevantToThisChat(message)) {
            Log.d(TAG, "Message is relevant to this chat, updating UI");
            runOnUiThread(() -> {
                Date currentDate = new Date();
                String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(currentDate);
                message.setDateTime(formattedDate);
                chatMessages.add(message);
                ItemGroupChatBinding binding1 = ItemGroupChatBinding.inflate(getLayoutInflater());
                binding1.dateText.setText(message.getDateTime());
                binding1.messageText.setText(message.getContent());

                chatAdapter.notifyDataSetChanged();
            });
        } else {
            Log.d(TAG, "Message is NOT relevant to this chat");
        }

    }

    @Override
    public String getChatId() {
        return conversionId;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
