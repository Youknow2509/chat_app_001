package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.SignupBinding;

public class UpdatePasswordRegisterActivity extends AppCompatActivity {

    private SignupBinding binding;
    private String token;

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

    }

    private void setListeners() {
        binding.back.setOnClickListener(v -> onBackPressed());
        binding.btnSignup.setOnClickListener(v -> {
                signUp();
        });
    }

    // Kiểm tra tính hợp lệ của email và mật khẩu
//    private Boolean isValidSignUp() {
//        String email = binding.editTextTextEmailAddress2.getText().toString().trim();
//        String password = binding.editTextTextPassword2.getText().toString().trim();
//        String confirmPassword = binding.editTextTextPassword3.getText().toString().trim();
//
//        if (email.isEmpty()) {
//            showToast("Please enter your email");
//            return false;
//        } else if (password.isEmpty()) {
//            showToast("Please enter your password");
//            return false;
//        } else if (confirmPassword.isEmpty()) {
//            showToast("Please confirm your password");
//            return false;
//        } else if (!password.equals(confirmPassword)) {
//            showToast("Password and Confirm Password must match");
//            return false;
//        }
//        return true;
//    }

    // Xử lý đăng ký
    private void signUp() {
        String email = binding.editTextTextEmailAddress2.getText().toString().trim();
        String password = binding.editTextTextPassword2.getText().toString().trim();

        showToast("Sign Up Successful!");

        Intent intent = new Intent(UpdatePasswordRegisterActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    // get code otp
    private void getCodeOtp() {

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
