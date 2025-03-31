package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.SignupBinding;
import com.example.chatapp.models.request.AccountModels;
import com.example.chatapp.models.response.ResponseData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePasswordRegisterActivity extends AppCompatActivity {

    private SignupBinding binding;
    private FrameLayout progressBar;

    //
    private String token;
    private ApiManager apiManager;


    private final String TAG = "UpdatePasswordRegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = SignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressBar = binding.progressOverlay;

        initVariableUse();

        setListeners();
    }

    // init variable use
    private void initVariableUse() {
        apiManager = new ApiManager();

        // get data intent
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        token = intent.getStringExtra("token");

        // set data to view
        Log.d("UpdatePasswordRegisterActivity", "Email: " + email + " Token: " + token);
        binding.editTextTextEmailAddress2.setText(email);
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
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        handleReqSignUpAccountHavePassword(password);
    }

    // change to login activity
    private void back_act() {
        Intent intent = new Intent(UpdatePasswordRegisterActivity.this, LoginActivity.class);
        String email = binding.editTextTextEmailAddress2.getText().toString().trim();
        intent.putExtra("mail", email);
        startActivity(intent);
        finish();
    }

    // handle req sign up account have password
    private void handleReqSignUpAccountHavePassword(String password) {
        apiManager.upgradePasswordRegister(
                new AccountModels.UpdatePasswordInput(password, token),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        progressBar.setVisibility(View.GONE);
                        int code = response.body().getCode();
                        if (code != Constants.CODE_SUCCESS) {
                            showToast(response.body().getMessage());
                        } else {
                            showToast("Tạo mật khẩu người dùng thành công!");
                            back_act();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        showToast("Vui lòng kiểm tra lại kết nối mạng!");
                    }
                }
        );
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
}
