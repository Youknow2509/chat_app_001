package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.chatapp.R;
import com.example.chatapp.services.StompServiceHelper;
import com.example.chatapp.utilities.StompClientManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import ua.naiksoftware.stomp.StompClient;

public class HomeActivity extends AppCompatActivity {

    private Button btnReturnToCall; // Nút quay lại cuộc gọi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StompServiceHelper.getInstance().initialize(this);
//        StompServiceHelper.getInstance().startAndBindService();


        // Thiết lập NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_message, R.id.nav_settings, R.id.nav_contact)
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
