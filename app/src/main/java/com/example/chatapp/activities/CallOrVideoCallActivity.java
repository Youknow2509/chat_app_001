package com.example.chatapp.activities;

import static android.content.ContentValues.TAG;
import static android.view.View.VISIBLE;
import static com.example.chatapp.consts.Constants.KEY_TYPE_CALL;
import static com.example.chatapp.consts.Constants.KEY_USER_ID;
import static com.example.chatapp.consts.Constants.KEY_USER_NAME;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.chatapp.models.WebRTCMessage;
import com.example.chatapp.observers.AppPeerConnectionObserver;
import com.example.chatapp.observers.SdpObservable;
import com.example.chatapp.observers.SignalingObserver;
import com.example.chatapp.utils.StompClientManager;
import com.example.chatapp.utils.WebRTCManager;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.apache.commons.logging.LogFactory;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.io.IOException;
import java.util.Objects;

public class CallOrVideoCallActivity extends AppCompatActivity {
    private enum SignalingState {
        STABLE,
        HAVE_LOCAL_OFFER,
        HAVE_REMOTE_OFFER
    }

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(CallOrVideoCallActivity.class);
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
    private String TAG = "CallOrVideoCallActivity";
    private WebRTCManager webRTCManager = WebRTCManager.getInstance();
    private Gson gson = new Gson();
    private StompClientManager stompClientManager = StompClientManager.getInstance();
    private String chatId;
    private String senderId;
    private SignalingState signalingState = SignalingState.STABLE;

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

        Intent intent = getIntent();
        if (intent != null) {
            String callType = intent.getStringExtra(KEY_TYPE_CALL);
            senderId = intent.getStringExtra("senderId");
            chatId = intent.getStringExtra("chatId");
            userName = intent.getStringExtra(KEY_USER_NAME);
            boolean isIncomingCall = intent.getBooleanExtra("INCOMING_CALL", false);
            isVideoCall = callType != null && callType.equals("video");

            // Setup WebRTC
            setupWebRTC();

            if (isIncomingCall) {
                // For incoming calls, set remote description first
                String remoteOfferPayload = intent.getStringExtra("remoteOfferPayload");
                if (remoteOfferPayload != null) {
                    SessionDescription remoteOffer = gson.fromJson(remoteOfferPayload, SessionDescription.class);
                    webRTCManager.setRemoteDescription(new SdpObservable() {
                        @Override
                        public void onSetSuccess() {
                            super.onSetSuccess();
                            // After remote description is set successfully, create answer
                            createAnswer();
                        }

                        @Override
                        public void onSetFailure(String s) {
                            Log.e(TAG, "Failed to set remote description: " + s);
                            super.onSetFailure(s);
                            Toast.makeText(CallOrVideoCallActivity.this,
                                    "Call setup failed", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }, remoteOffer);
                }
            } else {
                // For outgoing calls, create offer
                createOffer();
            }
        }
    }

    private void createOffer() {
        if (signalingState != SignalingState.STABLE) {
            Log.e(TAG, "Cannot create offer in state: " + signalingState);
            return;
        }

        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", isVideoCall ? "true" : "false"));

        webRTCManager.createOffer(constraints, new SdpObservable() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                super.onCreateSuccess(sdp);
                webRTCManager.setLocalDescription(new SdpObservable() {
                    @Override
                    public void onSetSuccess() {
                        super.onSetSuccess();
                        // Only send offer ONCE after local description is set
                        signalingState = SignalingState.HAVE_LOCAL_OFFER;
                        WebRTCMessage message = new WebRTCMessage();
                        message.setSenderId(senderId);
                        message.setChatId(chatId);
                        message.setType(WebRTCMessage.Type.OFFER.getType());
                        message.setPayload(gson.toJson(sdp));
                        Log.i(TAG, "Sending offer after local description set");
                        stompClientManager.sendCallOffer(message);
                    }
                }, sdp);
            }

            @Override
            public void onSetSuccess() {
                super.onSetSuccess();
                // Send SDP to server
                SessionDescription localDescription = webRTCManager.getLocalDescription();
                WebRTCMessage message = new WebRTCMessage();
                message.setSenderId(senderId);
                message.setChatId(chatId);
                message.setType(WebRTCMessage.Type.OFFER.getType());
                message.setPayload(gson.toJson(localDescription));
                Log.i(TAG, "onSetSuccess Offer: ");
                stompClientManager.sendCallOffer(message);
            }

            @Override
            public void onCreateFailure(String s) {
                super.onCreateFailure(s);
            }

            @Override
            public void onSetFailure(String s) {
                super.onSetFailure(s);
            }
        });
    }

    private void createAnswer() {
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", isVideoCall ? "true" : "false"));

        webRTCManager.createAnswer(constraints, new SdpObservable() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                super.onCreateSuccess(sdp);
                webRTCManager.setLocalDescription(new SdpObservable() {
                    @Override
                    public void onSetSuccess() {
                        super.onSetSuccess();
                        // Send answer once after local description is set
                        signalingState = SignalingState.STABLE;
                        WebRTCMessage message = new WebRTCMessage();
                        message.setSenderId(senderId);
                        message.setChatId(chatId);
                        message.setType(WebRTCMessage.Type.ANSWER.getType());
                        message.setPayload(gson.toJson(sdp));
                        Log.i(TAG, "Sending answer after local description set");
                        stompClientManager.sendCallAnswer(message);
                    }
                }, sdp);
            }

            @Override
            public void onCreateFailure(String s) {
                super.onCreateFailure(s);
            }

            @Override
            public void onSetFailure(String s) {
                super.onSetFailure(s);
            }
        });
    }

    private void setupWebRTC() {
        webRTCManager = WebRTCManager.getInstance();
        webRTCManager.init(this, false);
        setupSignaling();
        webRTCManager.startLocalMediaStream(this, binding.localVideoView, isMicrophoneEnabled, false);
        webRTCManager.createPeerConnection(new AppPeerConnectionObserver(TAG) {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                Log.d(TAG, "onIceCandidate: chatId " + chatId + ", senderId " + senderId);

                // Send the ICE candidate to the remote peer
                WebRTCMessage message = new WebRTCMessage();
                message.setSenderId(senderId);
                message.setChatId(chatId);
                message.setType(WebRTCMessage.Type.CANDIDATE.getType());
                message.setPayload(gson.toJson(iceCandidate));
                stompClientManager.sendIceCandidate(message);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

                Log.d(TAG, "ICE connection state: " + iceConnectionState);

                runOnUiThread(() -> {
                    if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
                        // Connection established successfully
                        binding.callingText.setText("Connected");

                    } else if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED ||
                            iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
                        // Connection failed
                        binding.callingText.setText("Connection failed");
                        Toast.makeText(CallOrVideoCallActivity.this,
                                "Call connection failed", Toast.LENGTH_SHORT).show();
                        endCall();
                    }
                });
                super.onIceConnectionChange(iceConnectionState);
            }


        });
    }

    private void setupSignaling() {
        stompClientManager.setOnSignalingEventListener(new SignalingObserver() {

            @Override
            public void onOfferReceived(SessionDescription offer) {
                webRTCManager.setRemoteDescription(new SdpObservable() {
                    @Override
                    public void onSetSuccess() {
                        super.onSetSuccess();
                        // After remote description is set successfully, create answer
                        boolean isIncomingCall = getIntent().getBooleanExtra("INCOMING_CALL", false);
                        if (isIncomingCall) {
                            createAnswer();
                        }
                    }
                }, offer);
            }

            @Override
            public void onAnswerReceived(SessionDescription answer) {
                webRTCManager.setRemoteDescription(new SdpObservable(), answer);
            }

            @Override
            public void onIceCandidateReceived(IceCandidate iceCandidate) {
                Log.i(TAG, "onIceCandidateReceived: ");
                webRTCManager.addIceCandidate(iceCandidate);
            }

            @Override
            public void onSignalingEvent(WebRTCMessage message) {
                if (message.getType().equals(WebRTCMessage.Type.OFFER.getType())) {
                    if (signalingState != SignalingState.STABLE) {
                        Log.e(TAG, "Received offer in non-stable state: " + signalingState);
                        return;
                    }
                    signalingState = SignalingState.HAVE_REMOTE_OFFER;
                    SessionDescription offer = gson.fromJson(message.getPayload(), SessionDescription.class);
                    onOfferReceived(offer);
                } else if (Objects.equals(message.getType(), WebRTCMessage.Type.ANSWER.getType())) {
                    if (signalingState != SignalingState.HAVE_LOCAL_OFFER) {
                        Log.e(TAG, "Received answer when not in HAVE_LOCAL_OFFER state");
                        return;
                    }
                    signalingState = SignalingState.STABLE;
                    SessionDescription answer = gson.fromJson(message.getPayload(), SessionDescription.class);
                    onAnswerReceived(answer);
                } else if (Objects.equals(message.getType(), WebRTCMessage.Type.CANDIDATE.getType())) {
                    Log.d(TAG, "Adding ice candidate from remote");
                    // Ensure candidates are properly parsed and added
                    IceCandidate candidate = gson.fromJson(message.getPayload(), IceCandidate.class);
                    webRTCManager.addIceCandidate(candidate);
                }
            }
        });
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

    private void callingResume() {
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
                Log.e(TAG, "Camera permission denied");
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