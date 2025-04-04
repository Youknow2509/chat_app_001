package com.example.chatapp.activities;

import static android.content.ContentValues.TAG;
import static android.view.View.VISIBLE;
import static com.example.chatapp.consts.Constants.KEY_TYPE_CALL;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityCallOrVideoCallBinding;
import com.example.chatapp.databinding.ActivityCallReturnBinding;
import com.example.chatapp.databinding.ToolbarChatBinding;
import com.example.chatapp.fragments.ChatFragment;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.common.util.concurrent.ListenableFuture;

import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;

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

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private CameraSource cameraSource;
    private static final int CAMERA_FACING = CameraSource.CAMERA_FACING_FRONT;


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
    private void updateReturnToCallBar() {
        try {
            View returnToCallBar = findViewById(R.id.returnToCallBar);
            returnToCallBar.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e(TAG, "Error updating return to call bar: " + e.getMessage());
        }
    }

    private void switchOptionCamera() {
        if (isCameraEnabled) {
            // Tắt camera
            binding.toggleCameraButton.setBackgroundResource(R.drawable.background_chat_input);
            binding.localVideoContainer.setVisibility(View.GONE);
            stopCamera();
            isCameraEnabled = false;
        } else {
            binding.toggleCameraButton.setBackgroundResource(R.drawable.cancel_button_background);
            binding.localVideoContainer.setVisibility(VISIBLE);
            // Bật camera
            if (checkCameraPermission()) {
                startCamera();
            } else {
                requestCameraPermission();
            }

            isCameraEnabled = true;
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }




    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

            } catch (Exception e) {
                Log.e("CameraX", "Error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }



    private void stopCamera() {
        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource.release();
            cameraSource = null;
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
        Intent intent = getIntent();
        String callType = intent.getStringExtra(KEY_TYPE_CALL);
        isCallActive = true;
        if (getIntent().hasExtra("USER_NAME")) {
            userName = getIntent().getStringExtra("USER_NAME");
        }
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
            if (checkCameraPermission()) {
                startCamera();
                isCameraEnabled = true;
                binding.toggleCameraButton.setBackgroundResource(R.drawable.cancel_button_background);
            } else {
                requestCameraPermission();
            }
        }
    }

    private void setupCallUI(boolean isVideoCall) {
        if (isVideoCall) {
            // Thiết lập giao diện cho cuộc gọi video
            this.isVideoCall = true;
            binding.remoteVideoContainer.setVisibility(VISIBLE);
            binding.localVideoContainer.setVisibility(VISIBLE);
        } else {
            // Thiết lập giao diện cho cuộc gọi audio
            this.isVideoCall = false;
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

            new AlertDialog.Builder(this)
                    .setTitle("End Call")
                    .setMessage("Are you sure you want to end this call?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        updateReturnToCallBar();
                        endCall();
                        isCallActive = false;
                        super.onBackPressed();
                    })
                    .setNegativeButton("No", null)
                    .show();
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
        stopCamera();
    }
}