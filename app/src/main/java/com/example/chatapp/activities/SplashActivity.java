package com.example.chatapp.activities;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.example.chatapp.R;
import com.example.chatapp.database.AppDatabase;
import com.example.chatapp.repo.TokenClientRepo;
import com.example.chatapp.utils.Utils;

public class SplashActivity extends AppCompatActivity {

    private TokenClientRepo tokenClientRepo;
    private Utils utils;
    private String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppDatabase.getInstance(this);
        tokenClientRepo = new TokenClientRepo(this);
        utils = new Utils();

        String accessToken = utils.getAccessToken(this);
        Log.d(TAG, "onCreate: " + accessToken);

        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
            finish();
        }, 2000);
    }
}
