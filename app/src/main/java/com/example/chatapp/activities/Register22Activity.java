package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityRegisterV225Binding;
import com.example.chatapp.databinding.SignupBinding;
import com.example.chatapp.models.request.AccountModels;
import com.example.chatapp.models.response.ResponseData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register22Activity extends AppCompatActivity {
    private ActivityRegisterV225Binding binding;

    private String email;

    private String token;
    private ApiManager apiManager;

    private final String TAG = "Register22Activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterV225Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initVariableUse();

        setListeners();
        binding.backToLoginButton.setOnClickListener(v-> BackToIntent());
    }

    private void BackToIntent() {
        Intent intent = new Intent(Register22Activity.this, Register2Activity.class);
        startActivity(intent);
        finish();
    }

    private void initVariableUse() {
        apiManager = new ApiManager(this);

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        token = intent.getStringExtra("token");

    }

    private void setListeners() {
        binding.backToLoginButton.setOnClickListener(v -> onBackPressed());
        binding.nextButton.setOnClickListener(v -> {
            signUp();
        });
    }

    private void signUp() {
        String password = binding.passwordInput.getText().toString().trim();
        String password_repeat = binding.passwordAgainInput.getText().toString().trim();

        if (!isValidSignUp(email, password, password_repeat)) {
            Log.e(TAG, "Input register account when update password is invalid");
            return;
        }

        binding.progressOverlay.setVisibility(View.VISIBLE);
        handleReqSignUpAccountHavePassword(password);
    }

    // handle req sign up account have password
    private void handleReqSignUpAccountHavePassword(String password) {
//        apiManager.upgradePasswordRegister(
//                new AccountModels.UpdatePasswordInput(password, token),
//                new Callback<ResponseData<Object>>() {
//                    @Override
//                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
//                        binding.progressOverlay.setVisibility(View.GONE);
//                        int code = response.body().getCode();
//                        if (code != Constants.CODE_SUCCESS) {
//                            showToast(response.body().getMessage());
//                        } else {
//                            showToast("Tạo mật khẩu người dùng thành công!");
//                            Intent intent = new Intent(Register22Activity.this, Register3Activity.class);
//                            intent.putExtra("mail", email);
//                            intent.putExtra("password", password);
//                            intent.putExtra("token", token);
//                            startActivity(intent);
//                            finish();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
//                        binding.progressOverlay.setVisibility(View.GONE);
//                        showToast("Vui lòng kiểm tra lại kết nối mạng!");
//                    }
//                }
//        );
        Intent intent = new Intent(Register22Activity.this, Register3Activity.class);
        intent.putExtra("mail", email);
        intent.putExtra("password", password);
        intent.putExtra("token", token);
        startActivity(intent);
        finish();
    }

    private Boolean isValidSignUp(String email, String password, String confirmPassword) {
        if (password.isEmpty()) {
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

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
}
