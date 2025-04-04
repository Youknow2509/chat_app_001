package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.chatapp.utils.session.SessionManager;
import com.google.firebase.messaging.FirebaseMessaging;

import com.example.chatapp.R;
import com.example.chatapp.utils.StompClientManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class HomeActivity extends AppCompatActivity {

    private StompClientManager stompClientManager;
    private SessionManager sessionManager ;

    private Button btnReturnToCall; // Nút quay lại cuộc gọi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(this);
        stompClientManager = StompClientManager.getInstance(sessionManager);


        // Thiết lập NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_message,R.id.nav_group,R.id.nav_profile, R.id.nav_more)
                .build();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);

        // Kiểm tra nếu có cuộc gọi đang chạy
        btnReturnToCall = findViewById(R.id.btnReturnToCall);
        checkOngoingCall();

        // Sự kiện bấm vào "Quay lại cuộc gọi"
        btnReturnToCall.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CallingActivity.class);
            intent.putExtra("CALL_TYPE", CallingActivity.getCallType());
            intent.putExtra("USER_NAME", CallingActivity.getCallerName());
            startActivity(intent);
        });

//        stompClientManager.subscribeTopic(sessionManager.getUserId());
//        stompClientManager.subscribeTopic("123e4567-e89b-12d3-a456-426614174000");
    }

    private void checkOngoingCall() {
        if (CallingActivity.isCallOngoing()) {
            btnReturnToCall.setVisibility(View.VISIBLE);
        } else {
            btnReturnToCall.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOngoingCall();
    }
}
