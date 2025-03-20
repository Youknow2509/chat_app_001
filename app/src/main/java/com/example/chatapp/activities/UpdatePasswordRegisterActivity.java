package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.SignupBinding;
import com.example.chatapp.network.HttpClient;
import com.example.chatapp.utils.Utils;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public class UpdatePasswordRegisterActivity extends AppCompatActivity {

    private SignupBinding binding;
    private String token;
    private HttpClient httpClient;
    private final String TAG = "UpdatePasswordRegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVariableUse();

        binding = SignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get data intent
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        token = intent.getStringExtra("token");
        Log.d("UpdatePasswordRegisterActivity", "Email: " + email + " Token: " + token);
        binding.editTextTextEmailAddress2.setText(email);

        setListeners();
    }

    // init variable use
    private void initVariableUse() {
        httpClient = new HttpClient();
    }

    private void setListeners() {
        binding.back.setOnClickListener(v -> onBackPressed());
        binding.btnSignup.setOnClickListener(v -> {
                signUp();
        });
    }

    // Kiểm tra tính hợp lệ của email và mật khẩu
    private Boolean isValidSignUp(String email, String password, String confirmPassword) {
        if (email.isEmpty()) {
            showToast("Please enter your email");
            return false;
        } else if (password.isEmpty()) {
            showToast("Please enter your password");
            return false;
        } else if (confirmPassword.isEmpty()) {
            showToast("Please confirm your password");
            return false;
        } else if (!password.equals(confirmPassword)) {
            showToast("Password and Confirm Password must match");
            return false;
        }
        return true;
    }

    // Xử lý đăng ký
    private void signUp() {
        String email = binding.editTextTextEmailAddress2.getText().toString().trim();
        String password = binding.editTextTextPassword2.getText().toString().trim();
        String password_repeat = binding.editTextTextPassword3.getText().toString().trim();

        if (!isValidSignUp(email, password, password_repeat)) {
            Log.e(TAG, "Input register account when update password is invalid");
            showToast("Invalid input");
        }

        showToast("TODO cmp");
//        showToast("Sign Up Successful!");
//
//        Intent intent = new Intent(UpdatePasswordRegisterActivity.this, HomeActivity.class);
//        startActivity(intent);
//        finish();
    }

    // handle req sign up account have password
    private void handleReqSignUpAccountHavePassword(String password) {
        CompletableFuture<JsonObject> future = httpClient.createPassword(token, password);
        future.thenAccept(res -> runOnUiThread(() -> {
            try {
                int codeRes = 0;
                if (res.has("code")) {
                    codeRes = res.get("code").getAsInt();
                }
                if (codeRes != Utils.ErrCodeSuccess) {
                    // TODO
                }
            } catch (Exception e) {
                Log.e("SignIn", "Error parsing response", e);
                showToast("Error processing response");
            }
        })).exceptionally(e -> {
            runOnUiThread(() -> {
                Log.e(TAG, "Create password user failed: ", e);
                showToast("Network error! Please try again.");
            });
            return null;
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
