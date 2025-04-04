package com.example.chatapp.activities;

import static android.view.View.VISIBLE;
import static com.example.chatapp.consts.Constants.KEY_TYPE_CALL;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityCallOrVideoCallBinding;
import com.example.chatapp.databinding.ActivityCallReturnBinding;
import com.example.chatapp.databinding.ToolbarChatBinding;
import com.example.chatapp.fragments.ChatFragment;

import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;

public class CallOrVideoCallActivity extends AppCompatActivity {

    private ActivityCallOrVideoCallBinding binding;

    boolean isMicrophoneEnabled = true;
    boolean isCameraEnabled = true;
    boolean isCallEnded = false;
    boolean isVideoCall = false;
    boolean isCallStarted = false;

    private static boolean isCallActive = false;
    private static String callType = "";
    private static String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallOrVideoCallBinding.inflate(getLayoutInflater());
        isCallActive = true;
        setContentView(binding.getRoot());
        binding.backButton.setOnClickListener(v -> callingResume());
        binding.toggleMicButton.setOnClickListener(v -> switchOptionMicrophone());
        binding.toggleCameraButton.setOnClickListener(v -> switchOptionCamera());
        binding.endCallButton.setOnClickListener(v -> onBackPressed());
    }

    private void switchOptionMicrophone() {
        if (isMicrophoneEnabled) {
            binding.toggleMicButton.setBackgroundResource(R.drawable.background_chat_input);
            isMicrophoneEnabled = false;
        } else {
            binding.toggleMicButton.setBackgroundResource(R.drawable.cancel_button_background);
            isMicrophoneEnabled = true;
        }
    }

    private void callingResume(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    private void switchOptionCamera() {
        if (isCameraEnabled) {
            // Tắt camera
            binding.toggleCameraButton.setBackgroundResource(R.drawable.background_chat_input);
            binding.localVideoContainer.setVisibility(View.GONE);
            isCameraEnabled = false;
        } else {
            // Bật camera
            binding.toggleCameraButton.setBackgroundResource(R.drawable.cancel_button_background);
            binding.localVideoContainer.setVisibility(VISIBLE);
            isCameraEnabled = true;
        }
    }

    private void endCall() {
        isCallEnded = true;
        // Dừng các luồng video/audio
        if (isVideoCall) {
            // Dừng video stream
            binding.remoteVideoView.setVisibility(View.GONE);
            binding.localVideoContainer.setVisibility(View.GONE);
        }

        // Hiển thị thông báo kết thúc cuộc gọi
        binding.callingText.setText("Call ended");
        isCallActive = false;
        // Đóng activity sau một khoảng thời gian
        new Handler().postDelayed(() -> {
            finish();
        }, 1000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Kiểm tra loại cuộc gọi (audio/video)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isVideoCall = extras.getBoolean("IS_VIDEO_CALL", false);
            setupCallUI(isVideoCall);
        }
        isCallActive = true;
        if (getIntent().hasExtra("USER_NAME")) {
            userName = getIntent().getStringExtra("USER_NAME");
        }
        Intent intent = getIntent();
        String callType = intent.getStringExtra(KEY_TYPE_CALL);
        if (callType != null && callType.equals("voice")) {
            this.callType = "voice";
            isVideoCall = false;
            binding.remoteVideoContainer.setVisibility(View.GONE);
            binding.localVideoContainer.setVisibility(View.GONE);
            binding.toggleCameraButton.setBackgroundResource(R.drawable.background_chat_input);
        } else {
            this.callType = "video";
            isVideoCall = true;
            binding.remoteVideoContainer.setVisibility(VISIBLE);
            binding.localVideoContainer.setVisibility(VISIBLE);
        }
    }

    private void setupCallUI(boolean isVideoCall) {
        if (isVideoCall) {
            // Thiết lập giao diện cho cuộc gọi video
            this.isVideoCall = false;
            binding.remoteVideoContainer.setVisibility(VISIBLE);
            binding.localVideoContainer.setVisibility(VISIBLE);
        } else {
            // Thiết lập giao diện cho cuộc gọi audio
            this.isVideoCall = true;
            binding.remoteVideoContainer.setVisibility(View.GONE);
            binding.localVideoContainer.setVisibility(View.GONE);
            binding.remoteAvatarContainer.setVisibility(VISIBLE);
        }

        // Bắt đầu cuộc gọi
        startCall();
    }

    private void startCall() {
        isCallStarted = true;
        binding.callingText.setText("Calling...");

        // Mô phỏng kết nối cuộc gọi sau 2 giây
        new Handler().postDelayed(() -> {
            binding.callingText.setText("Connected");
        }, 2000);
    }

    public static boolean isCallOngoing() {
        return isCallActive;
    }

    public static String getCallType() {
        return callType;
    }

    public static String getCallerName() {
        return userName;
    }


    @Override
    public void onBackPressed() {
        if (isCallStarted && !isCallEnded) {
            new AlertDialog.Builder(this)
                    .setTitle("End Call")
                    .setMessage("Are you sure you want to end this call?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        endCall();
                        isCallActive = false;
                        super.onBackPressed();
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // Nếu quay lại từ nút Return to Call
        if (intent.getBooleanExtra("RETURN_TO_CALL", false)) {
            // Khôi phục trạng thái cuộc gọi nếu cần
            if (isVideoCall) {
                binding.remoteVideoContainer.setVisibility(VISIBLE);
                if (isCameraEnabled) {
                    binding.localVideoContainer.setVisibility(VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Giải phóng tài nguyên
        if (!isCallEnded) {
            endCall();
        }
    }
}