package com.example.chatapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.BroadcastReceiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.models.WebRTCMessage;
import com.example.chatapp.network.NetworkMonitor;
import com.example.chatapp.observers.SignalingObserver;
import com.example.chatapp.utils.cloudinary.CloudinaryManager;
import com.example.chatapp.utils.session.SessionManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import com.example.chatapp.R;
import com.example.chatapp.utils.StompClientManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class HomeActivity extends AppCompatActivity implements NetworkMonitor.NetworkStateListener {

    private StompClientManager stompClientManager;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private CloudinaryManager cloudinaryManager;
    private final String TAG = "HomeActivity";
    //
    private NetworkMonitor networkMonitor;
    private View networkStatusView;
    private Gson gson = new Gson();

    // Khai báo BroadcastReceiver
    private BroadcastReceiver callStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_CALL_STATE_CHANGED.equals(intent.getAction())) {
                boolean isCallActive = intent.getBooleanExtra(Constants.EXTRA_CALL_ACTIVE, false);
                String callType = intent.getStringExtra(Constants.EXTRA_CALL_TYPE);
                String callerName = intent.getStringExtra(Constants.EXTRA_CALLER_NAME);

                updateReturnToCallBar(isCallActive, callType, callerName);
            }
        }
    };

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
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: HomeActivity");
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // nw
        networkStatusView = findViewById(R.id.network_status_view);
        networkMonitor = NetworkMonitor.getInstance(this);
        // Phần code còn lại giữ nguyên
        sessionManager = new SessionManager(this);
        stompClientManager = StompClientManager.getInstance();
        stompClientManager.setSessionManager(sessionManager, this);


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_message, R.id.nav_group, R.id.nav_profile, R.id.nav_more)
                .build();
        NavigationUI.setupWithNavController(binding.navView, navController);
        stompClientManager.subscribeTopic(sessionManager.getUserId());
        stompClientManager.setOnSignalingEventListener(new SignalingObserver() {
            @Override
            public void onOfferReceived(SessionDescription offer) {
                Log.i(TAG, "onOfferReceived: " + offer.description);
                // redirect to call activity
                Intent intent = new Intent(HomeActivity.this, CallOrVideoCallActivity.class);

                intent.putExtra("CALL_TYPE", "video");
                intent.putExtra("CALLER_NAME", sessionManager.getUserName());
                intent.putExtra("CALLER_ID", sessionManager.getUserId());

                intent.putExtra("OFFER", offer.description);

                startActivity(intent);
            }

            @Override
            public void onAnswerReceived(SessionDescription answer) {

            }

            @Override
            public void onIceCandidateReceived(IceCandidate iceCandidate) {

            }

            @Override
            public void onSignalingEvent(WebRTCMessage message) {
                if (message.getType().equals(WebRTCMessage.Type.OFFER.getType())) {
                    // parse message payload to SessionDescription
                    SessionDescription offer = gson.fromJson(message.getPayload(), SessionDescription.class);
                    onOfferReceived(offer);
                } else if (Objects.equals(message.getType(), WebRTCMessage.Type.ANSWER.getType())) {
                    // parse message payload to SessionDescription
                    SessionDescription answer = gson.fromJson(message.getPayload(), SessionDescription.class);
                    onAnswerReceived(answer);
                } else if (Objects.equals(message.getType(), WebRTCMessage.Type.CANDIDATE.getType())) {
                    // parse message payload to IceCandidate
                    IceCandidate iceCandidate = gson.fromJson(message.getPayload(), IceCandidate.class);
                    onIceCandidateReceived(iceCandidate);
                }
            }
        });
        requestCameraPermission();

    }

    /**
     * Init cloudinary
     */
    private void initCloudinary() {
        // Initialize CloudinaryManager
        cloudinaryManager = CloudinaryManager.getInstance(this);
        // Configure Cloudinary (you should replace these with your actual credentials)
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", Constants.CLOUDINARY_CLOUD_NAME);
        cloudinaryManager.initialize(config);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra trạng thái cuộc gọi khi activity được resume
        boolean isCallActive = CallOrVideoCallActivity.isCallOngoing();
        String callType = CallOrVideoCallActivity.getCallType();
        String callerName = CallOrVideoCallActivity.getCallerName();

        updateReturnToCallBar(isCallActive, callType, callerName);

        // Đăng ký nhận thông báo khi Activity hiển thị
        networkMonitor.addListener(this);

        // Cập nhật UI với trạng thái mạng hiện tại
        updateNetworkUI(networkMonitor.isNetworkAvailable());

        initCloudinary();
    }

    private void updateReturnToCallBar(boolean isCallActive, String callType, String callerName) {
        try {
            View returnToCallBar = findViewById(R.id.returnToCallBar);
            if (returnToCallBar != null) {
                if (isCallActive) {
                    returnToCallBar.setVisibility(View.VISIBLE);

                    // Có thể cập nhật UI dựa trên callType và callerName

                    returnToCallBar.setOnClickListener(v -> {
                        Intent intent = new Intent(this, CallOrVideoCallActivity.class);
                        intent.putExtra("RETURN_TO_CALL", true);
                        startActivity(intent);
                    });
                } else {
                    returnToCallBar.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating return to call bar: " + e.getMessage());
        }
    }

    // Show a Snackbar with a message
    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đăng ký khi Activity không hiển thị
        networkMonitor.removeListener(this);
    }

    @Override
    public void onNetworkStateChanged(boolean isAvailable) {
        // Được gọi mỗi khi trạng thái mạng thay đổi
        updateNetworkUI(isAvailable);

        if (isAvailable) {
            // Mạng đã được kết nối
            // Tải lại dữ liệu, gửi tin nhắn đang chờ, etc.
            showSnackbar("Mạng đã được kết nối");
        }
    }

    private void updateNetworkUI(boolean isConnected) {
        if (isConnected) {
            networkStatusView.setVisibility(View.GONE);
        } else {
            networkStatusView.setVisibility(View.VISIBLE);
        }
    }

}
