package com.example.chatapp.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityCallOrVideoCallBinding;
import android.os.Handler;
import android.view.View;
import androidx.appcompat.app.AlertDialog;

public class CallOrVideoCallActivity extends AppCompatActivity {

    private ActivityCallOrVideoCallBinding binding;

    boolean isMicrophoneEnabled = true;
    boolean isCameraEnabled = true;
    boolean isCallEnded = false;
    boolean isVideoCall = false;
    boolean isCallStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityCallOrVideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backButton.setOnClickListener(v -> onBackPressed());
        binding.toggleMicButton.setOnClickListener(v -> switchOptionMicrophone());
        binding.toggleCameraButton.setOnClickListener(v -> switchOptionCamera());
        binding.endCallButton.setOnClickListener(v -> endCall());
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



    private void switchOptionCamera() {
        if (isCameraEnabled) {
            // Tắt camera
            binding.toggleCameraButton.setBackgroundResource(R.drawable.background_chat_input);
            binding.localVideoContainer.setVisibility(View.GONE);
            isCameraEnabled = false;
        } else {
            // Bật camera
            binding.toggleCameraButton.setBackgroundResource(R.drawable.cancel_button_background);
            binding.localVideoContainer.setVisibility(View.VISIBLE);
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
    }

    private void setupCallUI(boolean isVideoCall) {
        if (isVideoCall) {
            // Thiết lập giao diện cho cuộc gọi video
            binding.toggleCameraButton.setVisibility(View.VISIBLE);
            binding.remoteVideoContainer.setVisibility(View.VISIBLE);
            binding.localVideoContainer.setVisibility(View.VISIBLE);
        } else {
            // Thiết lập giao diện cho cuộc gọi audio
            binding.toggleCameraButton.setVisibility(View.GONE);
            binding.remoteVideoContainer.setVisibility(View.GONE);
            binding.localVideoContainer.setVisibility(View.GONE);
            // Hiển thị avatar người gọi
            binding.remoteAvatarContainer.setVisibility(View.VISIBLE);
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

        // Khởi tạo các thành phần cần thiết cho cuộc gọi
        // Ví dụ: kết nối WebRTC, khởi tạo stream, v.v.
    }

    @Override
    public void onBackPressed() {
        // Hiển thị dialog xác nhận kết thúc cuộc gọi
        if (isCallStarted && !isCallEnded) {
            new AlertDialog.Builder(this)
                    .setTitle("End Call")
                    .setMessage("Are you sure you want to end this call?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        endCall();
                        super.onBackPressed();
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            super.onBackPressed();
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