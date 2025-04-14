package com.example.chatapp.activities;

import static android.content.ContentValues.TAG;

import static com.example.chatapp.consts.Constants.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.adapters.MentionSuggestionAdapter;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityChatV2Binding;
import com.example.chatapp.databinding.ItemUserChatBinding;
import com.example.chatapp.dto.ChatDTO;
import com.example.chatapp.dto.MediaMessageDTO;
import com.example.chatapp.dto.MessageDTO;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.MentionSuggestion;
import com.example.chatapp.models.User;
import com.example.chatapp.models.sqlite.Message;
import com.example.chatapp.observers.MessageObserver;
import com.example.chatapp.observers.MessageObservable;
import com.example.chatapp.repository.ChatRepo;
import com.example.chatapp.utils.DataSync;
import com.example.chatapp.utils.PreferenceManager;
import com.example.chatapp.utils.StompClientManager;
import com.example.chatapp.utils.cloudinary.CloudinaryManager;
import com.example.chatapp.utils.file.MediaUtils;
import com.example.chatapp.utils.session.SessionManager;
import com.example.chatapp.viewmodel.ChatViewModel;
import com.example.chatapp.viewmodel.LoginViewModel;
import com.example.chatapp.viewmodel.SendMediaViewModel;
import com.google.gson.Gson;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ChatConversationActivity extends BaseNetworkActivity implements MessageObserver {

    private ActivityChatV2Binding binding;
    private View nwStatusView;

    private MessageObservable messageObservable;

    private String conversionId;
    private final String TAG = "ChatConversationActivity";

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
    private static final int PAGE_SIZE = 20;
    private boolean isLoading = false;
    private volatile boolean hasMoreMessages = true;
    private DataSync dataSync = DataSync.getInstance();
    private boolean isSynced = false;

    private String type_message = "text";


    ////////
    private SendMediaViewModel sendMediaViewModel;
    private RecyclerView mentionSuggestionRecyclerView;
    private MentionSuggestionAdapter mentionAdapter;
    private List<MentionSuggestion> mentionSuggestions;
    private boolean isMentioning = false;
    private int mentionStartIndex = -1;
    private File savedMediaFile;
    private static final String FILEPROVIDER_AUTHORITY = "com.example.chatapp.fileprovider";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Uri currentMediaUri;
    private String currentMediaType = "image";
    private String urlIntoCloud;

    private CloudinaryManager cloudinaryManager;
    private File tempPhotoFile;

    private final ActivityResultLauncher<String[]> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    currentMediaType = "image";
                    currentMediaUri = uri;
                    showImagePreviewDialog(uri);
                }
            });

    private final ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && currentMediaUri != null) {
                    showImagePreviewDialog(currentMediaUri);
                }
            });

    private void showImagePreviewDialog(Uri imageUri) {
        binding.messageEditText.setVisibility(View.GONE);
        binding.imagePreview.setVisibility(View.VISIBLE);
        binding.closeImageButton.setVisibility(View.VISIBLE);
        binding.micButton.setVisibility(View.GONE);
        binding.sendButton.setVisibility(View.VISIBLE);

        Glide.with(binding.getRoot().getContext())
                .load(imageUri)
                .fitCenter()
                .into(binding.imagePreview);

        currentMediaUri = imageUri;
        Log.d(TAG, "Image URI: " + imageUri.toString());
        type_message = "image";

        binding.closeImageButton.setOnClickListener(v -> {
            binding.messageEditText.setVisibility(View.VISIBLE);
            binding.imagePreview.setVisibility(View.GONE);
            binding.closeImageButton.setVisibility(View.GONE);
            binding.micButton.setVisibility(View.VISIBLE);
            binding.sendButton.setVisibility(View.GONE);
            type_message = "text";
            currentMediaUri = null;
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = SessionManager.getInstance();
        stompClientManager = StompClientManager.getInstance();
        binding = ActivityChatV2Binding.inflate(getLayoutInflater());
        sendMediaViewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(SendMediaViewModel.class);
        this.nwStatusView = binding.networkStatusView.getRoot();
        chatRepo = new ChatRepo(this);
        setContentView(binding.getRoot());
        setListeners();
        loadChatDetails();
        binding.progressBar.setVisibility(View.GONE);


        init();
        mentionSuggestions = new ArrayList<>();
        mentionSuggestions.add(new MentionSuggestion("GEMINI_2_FLASH", R.drawable.avatar_circle_bg));
        mentionSuggestions.add(new MentionSuggestion("DEEPSEEK_CHAT", R.drawable.avatar_circle_bg));

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
                if (isMentioning) {
                    // Nếu người dùng xóa "@", tắt chế độ mention
                    if (mentionStartIndex >= text.length() || text.charAt(mentionStartIndex) != '@') {
                        isMentioning = false;
                        hideMentionSuggestions();
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

        sendMediaViewModel.getResMediaUrlPostUpdate().observe(this ,
                result -> {
                if (result != null) {
                    runOnUiThread(() -> {
                    // Upload media to Cloudinary
                        Log.d(TAG, "Media URL: " + currentMediaUri);
                        sendImageToServer(result);
                        type_message = "image";
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setSenderId(sessionManager.getUserId());
                        chatMessage.setMediaUrl(currentMediaUri.toString());
                        chatMessage.setReceiverId(receiverUser.id);
                        chatMessage.setContent(result);
                        chatMessage.setMessageType(type_message);
                        chatMessage.setDateTime(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

                        chatMessages.add(chatMessage);

                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    });

                }
        });

    }

    private void sendImageToServer(String url) {
        urlIntoCloud = url;
        MediaMessageDTO mediaMessageDTO = new MediaMessageDTO("", conversionId, "image", url);
        Gson gson = new Gson();
        String messageJson = gson.toJson(mediaMessageDTO);
        stompClientManager.sendMessage(messageJson, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                if (throwable != null) {
                    showToast("Error sending message");
                } else {
                    Log.d(TAG, "Image sent successfully");
                }
            }
        });

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

    @Override
    protected void onDestroy() {
        messageObservable.removeObserver(this);
        super.onDestroy();
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
        mentionStartIndex = -1;
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
        binding.cameraIcon.setOnClickListener(v -> takePhoto());
        binding.attachIcon.setOnClickListener(v -> AttachFile());
        binding.chatToolbar.menuButton.setOnClickListener(v -> ShowMore());
        binding.chatToolbar.syncButton.setOnClickListener(v -> syncWithServer());
    }

    private void ShowMore() {
        Intent intent = new Intent(getApplicationContext(), UserChatInformationActivity.class);
        intent.putExtra(Constants.KEY_USER, receiverUser);
        startActivity(intent);
    }

    private void SendMessage() {
        if (type_message.equals("text") && !binding.messageEditText.getText().toString().isEmpty()){
            String messageText = binding.messageEditText.getText().toString();
            if (messageText.isEmpty()) {
                return;
            }

            MessageDTO messageDTO = new MessageDTO(messageText, conversionId, "text");

            // parse messageDTO to json string
            Gson gson = new Gson();
            String messageJson = gson.toJson(messageDTO);
            stompClientManager.sendMessage(messageJson, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    if (throwable != null) {
                        showToast("Error sending message");
                    } else {
                        Log.d(TAG, "Message sent successfully");
                    }
                }
            });

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderId(sessionManager.getUserId());
            chatMessage.setContent(messageText);
            Date currentDate = new Date();
            // Format the date to a readable string : dd/MM/yyyy HH:mm:ss
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(currentDate);
            chatMessage.setDateTime(formattedDate);

            chatMessage.setReceiverId(receiverUser.id);
            chatMessages.add(chatMessage);
        }
        else if (type_message.equals("image") && currentMediaUri != null) {
            sendMediaViewModel.saveMediaToInternalStorageAndUpload(currentMediaUri, currentMediaType);
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderId(sessionManager.getUserId());
            chatMessage.setContent(urlIntoCloud);
            chatMessage.setMessageType(type_message);
            chatMessage.setMediaUrl(currentMediaUri.toString());
            Date currentDate = new Date();
            // Format the date to a readable string : dd/MM/yyyy HH:mm:ss
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(currentDate);
            chatMessage.setDateTime(formattedDate);
            chatMessage.setReceiverId(receiverUser.id);
            MessageDTO messageDTO = new MessageDTO(urlIntoCloud, conversionId, chatMessage.getMessageType());
            // parse messageDTO to json string
            Gson gson = new Gson();
            String messageJson = gson.toJson(messageDTO);
            stompClientManager.sendMessage(messageJson, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    if (throwable != null) {
                        showToast("Error sending message");
                    } else {
                        Log.d(TAG, "Message sent successfully");
                    }
                }
            });
            Log.d("TAG", "Image sent successfully");
        }

        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
        binding.messageEditText.setText(null);

        if (!isReceiverAvailable) {
            showToast("Receiver is not available, message sent.");
        }
    }


    private void VoiceCall() {
        Intent intent = new Intent(getApplicationContext(), CallOrVideoCallActivity.class);
        intent.putExtra(KEY_TYPE_CALL, "voice");
        intent.putExtra("senderId", sessionManager.getUserId());
        intent.putExtra("chatId", conversionId);
        intent.putExtra(KEY_USER_ID, receiverUser.id);
        intent.putExtra(KEY_USER_NAME, receiverUser.name);
        startActivityForResult(intent,100);

    }

    private void VideoCall() {
        Intent intent = new Intent(getApplicationContext(), CallOrVideoCallActivity.class);
        intent.putExtra(KEY_TYPE_CALL, "video");
        intent.putExtra(KEY_USER_ID, receiverUser.id);
        intent.putExtra(KEY_USER_NAME, receiverUser.name);
        startActivity(intent);
    }

    /**
     * Create a temporary file for media capture
     */
    private File createMediaFile(String extension) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "MEDIA_" + timeStamp + "_";
        File storageDir = getFilesDir();
        return File.createTempFile(fileName, extension, storageDir);
    }
    /**
     * Take photo with camera
     */
    private void takePhoto() {
        try {
            // Create temp directory if it doesn't exist
            File tempDir = new File(getCacheDir(), "temp_photos");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // Create temporary file
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            tempPhotoFile = new File(tempDir, "TEMP_" + timeStamp + ".jpg");

            currentMediaUri = FileProvider.getUriForFile(
                    this,
                    FILEPROVIDER_AUTHORITY,
                    tempPhotoFile
            );

            Log.i(TAG, "Temporary Photo URI: " + currentMediaUri);
            takePhotoLauncher.launch(currentMediaUri);

        } catch (Exception ex) {
            Log.e(TAG, "Error creating image file", ex);
            showSnackbar("Lỗi khi tạo file ảnh: " + ex.getMessage());
        }
    }

    /**
     * Open gallery for image selection
     */
    private void openGallery() {
        pickPictureLauncher.launch(new String[]{"image/*"});
    }

    /**
     * Check camera permission
     */
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    private void AttachFile() {
        openGallery();
    }

    private void Mic() {
        // TODO: Implement Mic functionality
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();

        chatAdapter = new ChatAdapter(
                chatMessages,
                receiverUser.image,
                preferenceManager.getString(Constants.KEY_USER_ID),
                false,
                sessionManager,
                type_message
        );

        dataSync.init(new ApiManager(this), chatRepo, sessionManager);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        chatAdapter.setHasStableIds(true);
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

                    // If we received fewer messages than requested, we've reached the end
                    if (messages.size() < PAGE_SIZE) {
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

    private void syncWithServer() {
        if (isSynced) {
            showToast("Already synchronized");
            return;
        }

        binding.chatToolbar.syncButton.setEnabled(false); // Disable button during sync
        binding.progressBar.setVisibility(View.VISIBLE); // Show progress indicator

        dataSync.syncMessage(conversionId, new DataSync.SyncCallback<>() {
                @Override
                public void onComplete(List<Message> data) {
                    // Add more detailed logging to diagnose the issue
                    Log.d(TAG, "Data sync completed with " + data.size() + " messages");
                    Log.d(TAG, "Response data details: " + (data.isEmpty() ? "empty list" : "first message ID: " + data.get(0).getId()));

                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.chatToolbar.syncButton.setEnabled(true);

                        if (data.isEmpty()) {
                            // Check if this is actually an empty response or a potential error
                            showToast("No new messages update");
                        }
                    });
                }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Sync error: " + errorMessage);
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.chatToolbar.syncButton.setEnabled(true);
                    showToast("Sync failed: " + errorMessage);
                });
            }
        });
        showToast("Sync completed");
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
                if("media".equals(message.getMessageType())){
                    chatMessage.setMessageType("media");
                    chatMessage.setSenderId(message.getSenderId());
                    chatMessage.setChatId(message.getChatId());
                    chatMessage.setMediaUrl(message.getMediaUrl());
                    chatMessage.setContent(message.getContent());
                    chatMessage.setDateTime(new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(message.getCreatedAt()));
                    newMessages.add(chatMessage);
                }
                else{
                    chatMessage.setMessageType("text");
                    chatMessage.setSenderId(message.getSenderId());
                    chatMessage.setChatId(message.getChatId());
                    chatMessage.setContent(message.getContent());

                    chatMessage.setDateTime(new SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(message.getCreatedAt()));
                    newMessages.add(chatMessage);
                }
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
        // Check for actual match
        return conversionId.equals(message.getChatId());
    }


    @Override
    public void onMessageReceived(ChatMessage message) {

        if (isMessageRelevantToThisChat(message)) {
            Log.i(TAG, "New message for: " + message.getChatId() + " - " + message.getContent());

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
                chatAdapter.notifyDataSetChanged();
                binding.chatRecyclerView.smoothScrollToPosition(newPosition);
            });
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    protected void onNetworkAvailable() {
        super.onNetworkAvailable();
        nwStatusView.setVisibility(View.GONE);
    }

    @Override
    protected void onNetworkUnavailable() {
        super.onNetworkUnavailable();
        nwStatusView.setVisibility(View.VISIBLE);
    }
}
