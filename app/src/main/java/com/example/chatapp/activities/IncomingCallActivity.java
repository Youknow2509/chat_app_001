package com.example.chatapp.activities;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.WebRTCMessage;
import com.example.chatapp.service.CallNotificationService;
import com.example.chatapp.utils.StompClientManager;

public class IncomingCallActivity extends AppCompatActivity {
    private String callerName;
    private String chatId;
    private String senderId;
    private boolean isVideoCall;
    private String payload;
    private StompClientManager stompClientManager = StompClientManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        // Turn screen on for incoming call
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // Get call details
        Intent intent = getIntent();
        callerName = intent.getStringExtra("callerName");
        chatId = intent.getStringExtra("chatId");
        senderId = intent.getStringExtra("senderId");
        isVideoCall = intent.getBooleanExtra("isVideoCall", false);
        payload = intent.getStringExtra("payload");

        // Set up UI
        TextView tvCallerName = findViewById(R.id.tvCallerName);
        TextView tvCallType = findViewById(R.id.tvCallType);
        Button btnAccept = findViewById(R.id.btnAccept);
        Button btnReject = findViewById(R.id.btnReject);

        tvCallerName.setText(callerName);
        tvCallType.setText(isVideoCall ? "Video Call" : "Voice Call");

        // Start ringtone
        Ringtone ringtone = RingtoneManager.getRingtone(
                this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        ringtone.play();

        btnAccept.setOnClickListener(v -> {
            ringtone.stop();
            acceptCall();
        });

        btnReject.setOnClickListener(v -> {
            ringtone.stop();
            rejectCall();
        });
    }

    private void acceptCall() {
        // Stop the call notification service
        stopService(new Intent(this, CallNotificationService.class));

        // Start CallOrVideoCallActivity
        Intent intent = new Intent(this, CallOrVideoCallActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("senderId", senderId);
        intent.putExtra("callerName", callerName);
        intent.putExtra(Constants.KEY_TYPE_CALL, isVideoCall ? "video" : "voice");
        intent.putExtra("INCOMING_CALL", true);
        intent.putExtra("remoteOfferPayload", payload);
        startActivity(intent);
        finish();
    }

    private void rejectCall() {
        // Send reject message
        WebRTCMessage rejectMessage = new WebRTCMessage();
        rejectMessage.setSenderId(senderId); // Use your actual user ID here
        rejectMessage.setChatId(chatId);
        rejectMessage.setType("reject");
//        stompClientManager.sendCallReject(rejectMessage);

        // Stop the call notification service
        stopService(new Intent(this, CallNotificationService.class));
        finish();
    }
}