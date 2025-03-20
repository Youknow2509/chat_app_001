package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        findViewById(R.id.btn_signup).setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, SignUpActivity.class));
        });

        findViewById(R.id.btn_login).setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, SignInActivity.class));
        });
    }
}
