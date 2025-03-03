package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;

public class CallingActivity extends AppCompatActivity {

    private TextView textCallerName;
    private TextView textCallStatus;
    private ImageView btnBack;
    private boolean isMicMuted = false;
    private boolean isCameraOff = false;
    private ImageView btnEndCall, btnMute, btnCameraOff, btnSwitchCamera;

    private static boolean isCallActive = false;

    private VideoView videoView;
    private View videoBackground;
    private static String callType = "";
    private static String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        textCallerName = findViewById(R.id.textCallerName);
        textCallStatus = findViewById(R.id.textCallStatus);
        btnBack = findViewById(R.id.btnBack);
        btnEndCall = findViewById(R.id.btnEndCall);
        btnMute = findViewById(R.id.btnMute);
        btnCameraOff = findViewById(R.id.btnCamera);
        btnSwitchCamera = findViewById(R.id.selfVideoThumbnail);
        videoView = findViewById(R.id.videoView);
        videoBackground = findViewById(R.id.videoBackground);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        callType = intent.getStringExtra("CALL_TYPE");
        userName = intent.getStringExtra("USER_NAME");
        boolean isGroupCall = intent.getBooleanExtra("IS_GROUP_CALL", false);
        if (isGroupCall) {
            userName = intent.getStringExtra("GROUP_NAME");
            textCallerName.setText("Group: " + userName);
        } else {
            userName = intent.getStringExtra("USER_NAME");
            textCallerName.setText(userName);
        }

        // Xử lý tự động tắt/mở camera theo loại cuộc gọi
        if ("audio".equals(callType)) {
            textCallStatus.setText("Đang gọi thoại...");
            isCameraOff = true;
            videoView.setVisibility(View.GONE);
            videoBackground.setVisibility(View.VISIBLE);
            btnCameraOff.setBackgroundResource(R.drawable.background_chat_input);
        } else {
            textCallStatus.setText("Đang gọi video...");
            isCameraOff = false;
            videoView.setVisibility(View.VISIBLE);
            videoBackground.setVisibility(View.GONE);
            btnCameraOff.setBackgroundResource(R.drawable.ellipse_1191);
        }

        isCallActive = true;

        // Xử lý sự kiện nút
        btnBack.setOnClickListener(v -> minimizeCall());
        btnEndCall.setOnClickListener(v -> endCall());
        btnMute.setOnClickListener(v -> muteMic());
        btnSwitchCamera.setOnClickListener(v -> switchCamera());
        btnCameraOff.setOnClickListener(v -> switchCameraOff());
    }


    private void switchCameraOff() {
        isCameraOff = !isCameraOff;

        if (isCameraOff) {
            btnCameraOff.setBackgroundResource(R.drawable.background_chat_input);
        } else {
            btnCameraOff.setBackgroundResource(R.drawable.ellipse_1191);
        }
    }

    private void minimizeCall() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("CALL_ACTIVE", true);
        intent.putExtra("CALL_TYPE", callType);
        intent.putExtra("USER_NAME", userName);
        startActivity(intent);
        finish();
    }

    private void endCall() {
        isCallActive = false;
        finish();
    }

    private void muteMic() {
        isMicMuted = !isMicMuted;

        if (isMicMuted) {
            btnMute.setBackgroundResource(R.drawable.background_chat_input); // Đổi icon tắt mic
        } else {
            btnMute.setBackgroundResource(R.drawable.ellipse_1191); // Đổi icon bật mic
        }
    }

    private void switchCamera() {
        Toast.makeText(this, "Switch Camera", Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
        isCallActive = false;  // Đánh dấu cuộc gọi kết thúc khi đóng hẳn
    }
}
