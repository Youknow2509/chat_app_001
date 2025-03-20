package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.LoginBinding;
import com.example.chatapp.models.ResponRepo;
import com.example.chatapp.models.TokenClient;
import com.example.chatapp.network.HttpClient;
import com.example.chatapp.repo.TokenClientRepo;
import com.example.chatapp.utils.Utils;
import com.google.gson.JsonObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SignInActivity extends AppCompatActivity {

    private LoginBinding binding;
    private HttpClient loginHttpClient;
    private TokenClientRepo tokenClientRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVariableUse();

        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.back.setOnClickListener(v -> back_act());
        binding.btnLogin.setOnClickListener(v -> signIn());
        binding.tvForgotPassword.setOnClickListener(v -> forgotPassword());
    }

    // init var use
    private void initVariableUse() {
        loginHttpClient = new HttpClient();
        tokenClientRepo = new TokenClientRepo(this);
    }

    // handle forgot password
    private void forgotPassword() {
        // TODO: dont have view forgot password
    }

    private void signIn() {
        String email = binding.editTextTextEmailAddress.getText().toString().trim();
        String password = binding.editTextTextPassword.getText().toString().trim();
        // TODO handle input validation
        showToast("Logging in...");

        // TODO - use in test -> prd
        if (email == "admin" && password == "admin") {
            navigateToHome();
        }

        CompletableFuture<JsonObject> future = loginHttpClient.login(email, password);

        future.thenAccept(res -> runOnUiThread(() -> {
            try {
                int codeRes = 0;
                if (res.has("code")) {
                    codeRes = res.get("code").getAsInt();
                }
                if (codeRes == Utils.ErrCodeSuccess) {
                    JsonObject data = res.get("data").getAsJsonObject();
                    String accessToken = data.get("token").getAsString();
                    String refreshToken = data.get("refresh_token").getAsString();
                    Log.d("SignIn", "AccessToken: " + accessToken);
                    Log.d("SignIn", "RefreshToken: " + refreshToken);
                    // sqlite
                    ResponRepo responRepo = tokenClientRepo.insertToken(new TokenClient(
                            UUID.randomUUID().toString(),
                            accessToken,
                            refreshToken
                    ));
                    if (!responRepo.isStatus()) {
                        showToast("Login failed: " + responRepo.getMessage());
                        return;
                    }
                    navigateToHome();
                } else {
                    // TODO handle new show err with code err
                    Log.e("SignIn", "Login failed: " + Utils.getMessageByCode(codeRes));
                    showToast("Login failed: " + Utils.getMessageByCode(codeRes));
                }
            } catch (Exception e) {
                Log.e("SignIn", "Error parsing response", e);
                showToast("Error processing response");
            }
        })).exceptionally(e -> {
            runOnUiThread(() -> {
                Log.e("SignIn", "Login request failed", e);
                showToast("Network error! Please try again.");
            });
            return null;
        });
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
