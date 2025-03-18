package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.LoginBinding;
import com.example.chatapp.network.LoginHttpClient;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public class SignInActivity extends AppCompatActivity {

    private LoginBinding binding;
    private LoginHttpClient loginHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginHttpClient = new LoginHttpClient();
        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.back.setOnClickListener(v -> back_act());
        binding.btnLogin.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        String email = binding.editTextTextEmailAddress.getText().toString().trim();
        String password = binding.editTextTextPassword.getText().toString().trim();

        showToast("Logging in...");

        CompletableFuture<JsonObject> future = loginHttpClient.login(email, password);

//        future.thenAccept(res -> runOnUiThread(() -> {
//            try {
//                if (res.has("code") && res.get("code").getAsInt() == Constants.CODE_SUCCESS) {
//                    JsonObject data = res.get("data").getAsJsonObject();
//                    String accessToken = data.get("token").getAsString();
//                    String refreshToken = data.get("refresh_token").getAsString();
//                    Log.d("SignIn", "AccessToken: " + accessToken);
//                    Log.d("SignIn", "RefreshToken: " + refreshToken);
//                    navigateToHome();
//                } else {
//                    // TODO handle new show err with code err
//                    showToast("Login failed: " + res.get("message").getAsString());
//                }
//            } catch (Exception e) {
//                Log.e("SignIn", "Error parsing response", e);
//                showToast("Error processing response");
//            }
//        })).exceptionally(e -> {
//            runOnUiThread(() -> {
//                Log.e("SignIn", "Login request failed", e);
//                showToast("Network error! Please try again.");
//            });
//            return null;
//        });
        navigateToHome();
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void back_act() {
        Intent intent = new Intent(SignInActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }
}
