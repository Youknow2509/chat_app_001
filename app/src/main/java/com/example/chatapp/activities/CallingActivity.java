package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.models.WebRTCMessage;
import com.example.chatapp.observers.SdpObservable;
import com.example.chatapp.observers.SignalingObserver;
import com.example.chatapp.utils.StompClientManager;
import com.example.chatapp.utils.WebRTCManager;
import com.google.gson.Gson;

import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.Objects;

public class CallingActivity extends AppCompatActivity {

    private TextView textCallerName;
    private TextView textCallStatus;
    private ImageView btnBack;
    private boolean isMicMuted = false;
    private boolean isCameraOff = false;
    private ImageView btnEndCall, btnMute, btnCameraOff;
    private TextureView btnSwitchCamera;

    private static boolean isCallActive = false;

    private VideoView videoView;
    private View videoBackground;
    private static String callType = "";
    private static String userName = "";
    private WebRTCManager webRTCManager;
    private StompClientManager stompClientManager;
    private Gson gson = new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        setupWebRTC();

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

    private void setupWebRTC() {
        webRTCManager = WebRTCManager.getInstance();
        webRTCManager.init(this, false);
        setupSignaling();
    }

    private void setupSignaling() {
//        stompClientManager.setOnSignalingEventListener(new SignalingObserver() {
//
//            @Override
//            public void onOfferReceived(SessionDescription offer) {
//                webRTCManager.setRemoteDescription(new SdpObservable(), offer);
//            }
//
//            @Override
//            public void onAnswerReceived(SessionDescription answer) {
//                webRTCManager.setRemoteDescription(new SdpObservable(), answer);
//            }
//
//            @Override
//            public void onIceCandidateReceived(IceCandidate iceCandidate) {
//                webRTCManager.addIceCandidate(iceCandidate);
//            }
//
//            @Override
//            public void onSignalingEvent(WebRTCMessage message) {
//                if(message.getType().equals(WebRTCMessage.Type.OFFER.getType())) {
//                    // parse message payload to SessionDescription
//                    SessionDescription offer = gson.fromJson(message.getPayload(), SessionDescription.class);
//                    onOfferReceived(offer);
//                } else if (Objects.equals(message.getType(), WebRTCMessage.Type.ANSWER.getType())) {
//                    // parse message payload to SessionDescription
//                    SessionDescription answer = gson.fromJson(message.getPayload(), SessionDescription.class);
//                    onAnswerReceived(answer);
//                } else if (Objects.equals(message.getType(), WebRTCMessage.Type.CANDIDATE.getType())) {
//                    // parse message payload to IceCandidate
//                    IceCandidate iceCandidate = gson.fromJson(message.getPayload(), IceCandidate.class);
//                    onIceCandidateReceived(iceCandidate);
//                }
//            }
//        });
    }


    private void switchCameraOff() {
        isCameraOff = !isCameraOff;

        if (isCameraOff) {
            btnCameraOff.setBackgroundResource(R.drawable.background_chat_input);
        } else {
            btnCameraOff.setBackgroundResource(R.drawable.ellipse_1191);
        }
    }

    private void createOffer() {
        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        webRTCManager.createOffer(constraints, new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sdp) {
                webRTCManager.setLocalDescription(new SdpObservable(), sdp);
                SessionDescription offer = new SessionDescription(SessionDescription.Type.OFFER, sdp.description);
                WebRTCMessage message = new WebRTCMessage();
                message.setSenderId("123e4567-e89b-12d3-a456-426614174000");
                message.setChatId("789e4567-e89b-12d3-a456-426614174000");
                message.setType("offer");
                message.setPayload(gson.toJson(offer));
                stompClientManager.sendCallOffer(message);
            }

            @Override
            public void onSetSuccess() {
                // Gửi SDP đến server
                SessionDescription localDescription = webRTCManager.getLocalDescription();
                WebRTCMessage message = new WebRTCMessage();
                message.setSenderId("123e4567-e89b-12d3-a456-426614174000");
                message.setChatId("789e4567-e89b-12d3-a456-426614174000");
                message.setType("answer");
                message.setPayload(gson.toJson(localDescription));
                stompClientManager.sendCallAnswer(message);
            }

            @Override
            public void onCreateFailure(String s) {
                Toast.makeText(CallingActivity.this, "Create offer failed: " + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSetFailure(String s) {
                Toast.makeText(CallingActivity.this, "Set local description failed: " + s, Toast.LENGTH_SHORT).show();
            }
        });
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
