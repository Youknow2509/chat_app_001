package com.example.chatapp.activities;

import static android.content.ContentValues.TAG;

import static com.example.chatapp.consts.Constants.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.adapters.MentionSuggestionAdapter;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityChatV2Binding;
import com.example.chatapp.databinding.ItemUserChatBinding;
import com.example.chatapp.dto.ChatDTO;
import com.example.chatapp.dto.MessageDTO;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.MentionSuggestion;
import com.example.chatapp.models.User;
import com.example.chatapp.models.sqlite.Message;
import com.example.chatapp.observers.MessageObserver;
import com.example.chatapp.observers.MessageObservable;
import com.example.chatapp.repository.ChatRepo;
import com.example.chatapp.utils.PreferenceManager;
import com.example.chatapp.utils.StompClientManager;
import com.example.chatapp.utils.session.SessionManager;
import com.google.gson.Gson;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChatConversationActivity extends AppCompatActivity implements MessageObserver {

    private ActivityChatV2Binding binding;

    private MessageObservable messageObservable;

    private String conversionId = "789e0123-a456-42f5-b678-556655440000";

    private User receiverUser;

    private List<ChatMessage> chatMessages;
    private boolean isReceiverAvailable = true;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private ChatDTO chat;
    private StompClientManager stompClientManager;
    private SessionManager sessionManager;
    private ChatRepo chatRepo;
    private int currentOffset = 0;
    private static final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean hasMoreMessages = true;


    ////////
    private RecyclerView mentionSuggestionRecyclerView;
    private MentionSuggestionAdapter mentionAdapter;
    private List<MentionSuggestion> mentionSuggestions;
    private boolean isMentioning = false;
    private int mentionStartIndex = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ChatConversationActivity", "open ChatConversationActivity");
        sessionManager = SessionManager.getInstance();
        stompClientManager = StompClientManager.getInstance();
        binding = ActivityChatV2Binding.inflate(getLayoutInflater());
        chatRepo = new ChatRepo(this);
        setContentView(binding.getRoot());
        setListeners();
        loadChatDetails();
        binding.progressBar.setVisibility(View.GONE);
        init();
        Log.i(TAG, "onCreate: CREREASDSDASDAS");


        mentionSuggestions = new ArrayList<>();
        mentionSuggestions.add(new MentionSuggestion("GEMINI", R.drawable.avatar_circle_bg));
        mentionSuggestions.add(new MentionSuggestion("GROK", R.drawable.avatar_circle_bg));

        // Khởi tạo RecyclerView cho gợi ý mention
        mentionSuggestionRecyclerView = binding.mentionSuggestionRecyclerView;
        mentionSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mentionAdapter = new MentionSuggestionAdapter(mentionSuggestions, suggestion -> {
            // Xử lý khi người dùng chọn một gợi ý
            handleMentionSelection(suggestion);
        });
        mentionSuggestionRecyclerView.setAdapter(mentionAdapter);

        binding.messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ChangeButtonSend();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String text = charSequence.toString();

                // Kiểm tra xem người dùng vừa gõ "@" không
                if (count == 1 && text.charAt(start) == '@') {
                    isMentioning = true;
                    mentionStartIndex = start;
                    showMentionSuggestions();
                }
                // Nếu đang trong chế độ mention, kiểm tra xem người dùng đã gõ thêm ký tự nào không
                else if (isMentioning) {
                    // Nếu người dùng xóa "@", tắt chế độ mention
                    if (mentionStartIndex >= text.length() || text.charAt(mentionStartIndex) != '@') {
                        isMentioning = false;
                        hideMentionSuggestions();
                    }
                    // Lọc danh sách gợi ý dựa trên những gì người dùng đã gõ sau "@"
                    else if (mentionStartIndex < text.length() - 1) {
                        String query = text.substring(mentionStartIndex + 1).toLowerCase();
                        filterMentionSuggestions(query);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Subscribe to message observable
        messageObservable = MessageObservable.getInstance();
        messageObservable.addObserver(this);

    }

    private void showMentionSuggestions() {
        mentionSuggestionRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hideMentionSuggestions() {
        mentionSuggestionRecyclerView.setVisibility(View.GONE);
    }

    private void filterMentionSuggestions(String query) {
        List<MentionSuggestion> filteredList = new ArrayList<>();
        for (MentionSuggestion suggestion : mentionSuggestions) {
            if (suggestion.getName().toLowerCase().contains(query)) {
                filteredList.add(suggestion);
            }
        }

        mentionAdapter = new MentionSuggestionAdapter(filteredList, suggestion -> {
            handleMentionSelection(suggestion);
        });
        mentionSuggestionRecyclerView.setAdapter(mentionAdapter);

        // Ẩn danh sách nếu không có gợi ý nào phù hợp
        if (filteredList.isEmpty()) {
            hideMentionSuggestions();
        } else {
            showMentionSuggestions();
        }
    }

    private void handleMentionSelection(MentionSuggestion suggestion) {
        // Lấy nội dung hiện tại của EditText
        Editable editable = binding.messageEditText.getText();
        String text = editable.toString();

        // Thay thế từ vị trí @ đến vị trí con trỏ hiện tại bằng tên được chọn
        String replacement = "@" + suggestion.getName() + " ";
        String newText = text.substring(0, mentionStartIndex) + replacement;

        // Nếu còn nội dung sau vị trí con trỏ, thêm vào
        if (binding.messageEditText.getSelectionStart() < text.length()) {
            newText += text.substring(binding.messageEditText.getSelectionStart());
        }

        // Cập nhật nội dung EditText
        binding.messageEditText.setText(newText);

        // Di chuyển con trỏ đến cuối phần vừa chèn
        binding.messageEditText.setSelection(mentionStartIndex + replacement.length());

        // Ẩn danh sách gợi ý
        hideMentionSuggestions();
        isMentioning = false;
    }

    @Override
    public String getChatId() {
        return conversionId;
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
        Intent intent = new Intent(getApplicationContext(), UserChatInformationActivity.class);
        intent.putExtra(Constants.KEY_USER, receiverUser);
        startActivity(intent);
    }

    private void SendMessage() {
        String messageText = binding.messageEditText.getText().toString();
        if (messageText.isEmpty()) {
            return;
        }

        MessageDTO messageDTO = new MessageDTO(messageText, conversionId, "text");

        // parse messageDTO to json string
        Gson gson = new Gson();
        String messageJson = gson.toJson(messageDTO);
        stompClientManager.sendMessage(messageJson);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(sessionManager.getUserId());
        chatMessage.setContent(messageText);
        Date currentDate = new Date();
        // Format the date to a readable string : dd/MM/yyyy HH:mm:ss
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(currentDate);
        chatMessage.setDateTime(formattedDate);

        chatMessage.setReceiverId(receiverUser.id);

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
        Intent intent = new Intent(getApplicationContext(), CallOrVideoCallActivity.class);
        intent.putExtra(KEY_TYPE_CALL, "voice");
        intent.putExtra(KEY_USER_ID, receiverUser.id);
        intent.putExtra(KEY_USER_NAME, receiverUser.name);
        startActivity(intent);

    }

    private void VideoCall() {
        Intent intent = new Intent(getApplicationContext(), CallOrVideoCallActivity.class);
        intent.putExtra(KEY_TYPE_CALL, "video");
        intent.putExtra(KEY_USER_ID, receiverUser.id);
        intent.putExtra(KEY_USER_NAME, receiverUser.name);
        startActivity(intent);
    }

    // Activity result launcher for picking image/video from gallery
//    private final ActivityResultLauncher<String[]> pickMediaLauncher = registerForActivityResult(
//            new ActivityResultContracts.OpenDocument(),
//            uri -> {
//                if (uri != null) {
//                    // Persist permission for this URI
//                    getContentResolver().takePersistableUriPermission(uri,
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                    // Determine media type
//                    currentMediaType = MediaUtils.getMediaType(ChatConversationActivity.this, uri);
//
//                    Log.i(TAG, "Selected media type: " + currentMediaType);
//                    Log.i(TAG, "Selected media URI: " + uri.toString());
//                    handleMediaResult(uri);
//                }
//            });

    // Activity result launcher for taking photo with camera
//    private final ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(
//            new ActivityResultContracts.TakePicture(),
//            success -> {
//                if (success && currentMediaUri != null) {
//                    currentMediaType = "image";
//                    handleMediaResult(currentMediaUri);
//                }
//            });
//
//    // Activity result launcher for recording video with camera
//    private final ActivityResultLauncher<Uri> takeVideoLauncher = registerForActivityResult(
//            new ActivityResultContracts.CaptureVideo(),
//            success -> {
//                if (success && currentMediaUri != null) {
//                    currentMediaType = "video";
//                    handleMediaResult(currentMediaUri);
//                }
//            });

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
                receiverUser.image,
                preferenceManager.getString(Constants.KEY_USER_ID),
                false,
                sessionManager
        );


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Show newest messages at the bottom
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        // Configure swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadMoreMessages);
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.nav_item_color);
        // Makes swiping work only from top of list (better for chat UI)
        binding.swipeRefreshLayout.setDistanceToTriggerSync(300);

        // Initial load
        loadMessages();
    }

    private void loadMessages() {
        if (isLoading || !hasMoreMessages) return;

        isLoading = true;
        final int loadOffset = currentOffset; // Capture current offset for this request

        LiveData<List<Message>> messagesLiveData = chatRepo.getMessagesForConversation(
                conversionId, PAGE_SIZE, loadOffset);

        // Create a one-time observer that removes itself after processing
        messagesLiveData.observe(this, new androidx.lifecycle.Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                // Process the messages
                processLoadedMessages(messages, loadOffset == 0);

                // Update offset for next load
                if (messages != null) {
                    currentOffset += messages.size();

                    if (messages.isEmpty()) {
                        hasMoreMessages = false;
                    }
                }

                // Remove this observer after one use
                messagesLiveData.removeObserver(this);

                isLoading = false;
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void processLoadedMessages(List<Message> messages, boolean isFirstLoad) {
        if (binding.chatRecyclerView.getVisibility() != View.VISIBLE) {
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }

        if (messages != null && !messages.isEmpty()) {
            // Clear existing messages only if it's the first load
            if (isFirstLoad) {
                chatMessages.clear();
            }

            // Convert and add new messages
            List<ChatMessage> newMessages = new ArrayList<>();
            for (Message message : messages) {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(message.getId());
                chatMessage.setSenderId(message.getSenderId());
                chatMessage.setChatId(message.getChatId());
                chatMessage.setContent(message.getContent());

                chatMessage.setDateTime(new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(message.getCreatedAt()));
                newMessages.add(chatMessage);
            }

            // Add messages in chronological order
            Collections.reverse(newMessages);
            chatMessages.addAll(0, newMessages); // Add to the beginning of the list

            chatAdapter.notifyDataSetChanged();

            // If it's the first load, scroll to bottom
            if (isFirstLoad) {
                binding.chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        }
    }

    private void loadMoreMessages() {
        if (!isLoading && hasMoreMessages) {
            binding.swipeRefreshLayout.setRefreshing(true);
            loadMessages();
        } else {
            binding.swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void loadChatDetails() {
        try {
            chat = (ChatDTO) getIntent().getSerializableExtra(Constants.KEY_CHAT);
            binding.userName.setText(chat.getChatName());
            Glide.with(this)
                    .load(chat.getAvatar())
                    .into(binding.userAvatar);
            binding.userPhone.setText(chat.getChatId());

            // Khởi tạo receiverUser từ chat
            receiverUser = new User();
            receiverUser.id = chat.getChatId();
            receiverUser.name = chat.getChatName();
            // receiverUser.image sẽ được xử lý sau

            conversionId = chat.getChatId();
        } catch (ClassCastException e) {
            // Xử lý trường hợp nhận String thay vì ChatDTO
            String chatId = getIntent().getStringExtra(Constants.KEY_CHAT);
            conversionId = chatId;

            // Khởi tạo receiverUser với thông tin tối thiểu
            receiverUser = new User();
            receiverUser.id = chatId;
            receiverUser.name = "Chat " + chatId;
        }
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
                // Remove any temporary messages with null ID
                chatMessages.stream().filter(chatMessage -> chatMessage.getId() == null).findFirst().ifPresent(chatMessage -> {
                    chatMessages.remove(chatMessage);
                });

                // Format the date and set it on the message
                Date currentDate = new Date();
                String formattedDate = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(currentDate);
                message.setDateTime(formattedDate);

                // Add the message to the list
                chatMessages.add(message);

                // Notify adapter about the new item and scroll to it
                int newPosition = chatMessages.size() - 1;
                chatAdapter.notifyItemInserted(newPosition);
                binding.chatRecyclerView.smoothScrollToPosition(newPosition);
            });
        } else {
            Log.d(TAG, "Message is NOT relevant to this chat");
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
