package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityLoginV2Binding;
import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.Utils;
import com.example.chatapp.utils.session.SessionManager;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginV2Binding binding;

    private FrameLayout progressOverlay;
    //
    private ApiManager apiManager;
    private SessionManager sessionManager;
    private String accessToken;
    private String refreshToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVariableUse();

        binding = ActivityLoginV2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressOverlay = findViewById(R.id.progress_overlay);

        // handle if activity before is register
        handleFielMail();

        binding.nextButton.setOnClickListener(v -> signIn());
        binding.registerButton.setOnClickListener(v->register());
        binding.forgotPasswordText.setOnClickListener(v -> forgotPassword());
    }

    // handle if before is register
    private void handleFielMail() {
        Intent intent = getIntent();
        String new_email_acc = intent.getStringExtra("mail");
        binding.mailEditText.setText(new_email_acc);
    }

    // init var use
    private void initVariableUse() {
        apiManager = new ApiManager();
        sessionManager = new SessionManager(this);
    }

    // handle register
    private void register() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    // handle forgot password
    private void forgotPassword() {
        progressOverlay.setVisibility(View.VISIBLE);
        String email = binding.mailEditText.getText().toString().trim();
        apiManager.forgotPassword(email, new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                progressOverlay.setVisibility(View.GONE);
                int code = response.body().getCode();
                if (code != Constants.CODE_SUCCESS) {
                    Log.e("ForgotPassword", "Forgot password failed: " + response.body().getMessage());
                    showToast("Forgot password failed: " + response.body().getMessage());
                    return;
                }
                showToast("Đã gửi yêu cầu thay đổi mật khẩu về mail của bạn!");
            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                progressOverlay.setVisibility(View.GONE);
                Log.e("ForgotPassword", "Forgot password request failed");
                showToast("Network error! Please try again.");
            }
        });
    }

    private void signIn() {
        progressOverlay.setVisibility(View.VISIBLE);
        String email = binding.mailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        // TODO handle input validation
        showToast("Logging in...");

        // TODO - use in test -> prd
        if (email.equals("admin") && password.equals("admin")) {
            navigateToHome();
        }

//        apiManager.login(
//                email,
//                password,
//                new Callback<ResponseData<Object>>() {
//                    @Override
//                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
//                        progressOverlay.setVisibility(View.GONE);
//                        int code = response.body().getCode();
//                        if (code != Constants.CODE_SUCCESS) {
//                            showToast("Login failed: " + response.body().getMessage());
//                            return;
//                        }
//
//                        accessToken = Utils.getDataBody(response.body(), "token");
//                        refreshToken = Utils.getDataBody(response.body(), "refresh_token");
//
//                        Log.i("SignIn", "AccessToken: " + accessToken);
//                        Log.i("SignIn", "RefreshToken: " + refreshToken);
//                        // save session
//                        saveSession(accessToken, refreshToken);
//                        navigateToHome();
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
//                        progressOverlay.setVisibility(View.GONE);
//                        Log.e("SignIn", "Login request failed");
//                        showToast("Network error! Please try again.");
//                    }
//                });

        navigateToHome();

    }

    /**
     * Save session user when login
     *
     * @param accessToken  String
     * @param refreshToken String
     */
    private void saveSession(String accessToken, String refreshToken) {
        getUserInfo(accessToken)
                .thenAccept(userInfo -> {
                    sessionManager.saveUserProfile(userInfo);
                    sessionManager.saveAuthData(accessToken, refreshToken, userInfo.getId());
                    Log.d("SignIn", "User info saved successfully.");
                })
                .exceptionally(e -> {
                    showToast(e.getMessage());
                    Log.e("SignIn", e.getMessage());
                    return null;
                });
    }

    /**
     * Get user info
     *
     * @param token String
     * @return UserProfileSession
     */
    private CompletableFuture<UserProfileSession> getUserInfo(String token) {
        CompletableFuture<UserProfileSession> future = new CompletableFuture<>();

        apiManager.getUserInfo(
                token, new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        int code = response.body().getCode();
                        if (code != Constants.CODE_SUCCESS) {
                            future.completeExceptionally(new Exception("Get user info failed: " + response.body().getMessage()));
                            return;
                        }

                        UserProfileSession user = new UserProfileSession();
                        user.setId(Utils.getDataBody(response.body(), "user_id"));
                        user.setName(Utils.getDataBody(response.body(), "user_nickname"));
                        user.setEmail(Utils.getDataBody(response.body(), "user_email"));
                        user.setAvatarUrl(Utils.getDataBody(response.body(), "user_avatar"));
                        user.setDisplayName(Utils.getDataBody(response.body(), "user_nickname"));

                        future.complete(user);
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        future.completeExceptionally(new Exception("Network error! Please try again."));
                    }
                });

        return future;
    }


    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
}
