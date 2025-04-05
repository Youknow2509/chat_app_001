package com.example.chatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityLoginV2Binding;
import com.example.chatapp.models.TokenClient;
import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.network.NetworkMonitor;
import com.example.chatapp.repository.TokenClientRepo;
import com.example.chatapp.service.NetworkMonitorService;
import com.example.chatapp.service.TokenRefreshService;
import com.example.chatapp.utils.session.SessionManager;
import com.example.chatapp.viewmodel.LoginViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity implements NetworkMonitor.NetworkStateListener {

    private ActivityLoginV2Binding binding;
    private FrameLayout progressOverlay;
    private LoginViewModel loginViewModel;
    private Context context;
    private SessionManager sessionManager;
    // network
    private View networkStatusView;
    private NetworkMonitor networkMonitor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginV2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressOverlay = findViewById(R.id.progress_overlay);
        networkStatusView = findViewById(R.id.network_status_view);

        loginViewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(LoginViewModel.class);
        context = this;
        sessionManager = new SessionManager(context);
        networkMonitor = NetworkMonitor.getInstance(this);

        startNetworkMonitorService();

        handleFieldMail();

        observeLiveData();

        setListener();
    }

    private void startNetworkMonitorService() {
        Intent serviceIntent = new Intent(this, NetworkMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    /**
     * set observe live data from view model
     */
    private void observeLiveData() {
        loginViewModel.getResPathFileAvatar().observe(this,
                path -> {
                    if (path != null && !path.isEmpty()) {
                        // TODO set default avatar
//                        saveLocalPathFileAvatar(path);
                        navigateToHome();
                    } else {
                        Toast.makeText(context, "Error saving avatar", Toast.LENGTH_SHORT).show();
                    }
                });

        loginViewModel.getUserProfileLiveData().observe(this, user -> {
            // Save session and save avatar
            saveSession(user);
            loginViewModel.saveAvatarUser(user.getAvatarUrl());
        });

        loginViewModel.getErrorMessageLiveData().observe(this, errorMessage -> {
            // Show error message using Snackbar
            showSnackbar(errorMessage);
        });

        loginViewModel.getResMessageForgotPassword().observe(this, resMessage -> {
            // Show error message using Snackbar
            showSnackbar(resMessage);
            progressOverlay.setVisibility(View.GONE);
        });
    }

    /**
     * set event in elements listener
     */
    private void setListener() {
        // Handle login button click
        binding.nextButton.setOnClickListener(v -> {
            String email = binding.mailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            loginViewModel.signIn(email, password);
        });

        // Handle register button click
        binding.registerButton.setOnClickListener(v -> register());

        // Handle forgot password button click
        binding.forgotPasswordText.setOnClickListener(v -> forgotPassword());
    }

    // Handle email pre-filled from previous screen (register or password reset)
    private void handleFieldMail() {
        Intent intent = getIntent();
        String new_email_acc = intent.getStringExtra("email");
        binding.mailEditText.setText(new_email_acc);
    }

    // save local path file avatar
    private void saveLocalPathFileAvatar(String path) {
        sessionManager.setPathFileAvatarUser(path);
    }

    // Handle register action
    private void register() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    // Handle forgot password action
    private void forgotPassword() {
        progressOverlay.setVisibility(View.VISIBLE);
        String email = binding.mailEditText.getText().toString().trim();
        loginViewModel.forgotPassword(email);
    }

    // Save session after successful login
    private void saveSession(UserProfileSession user) {
        // Save user data and tokens in session
        sessionManager.saveUserProfile(user);
        sessionManager.saveAuthData(
                user.getAccessToken(),
                user.getRefreshToken(),
                user.getId()
        );

        // Khởi động TokenRefreshService
        Intent serviceIntent = new Intent(this, TokenRefreshService.class);
        startService(serviceIntent);
    }

    // Navigate to home activity
    private void navigateToHome() {
        Log.d("LoginActivity", "Navigating to HomeActivity");
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    // get progress overlay
    public FrameLayout getProgressOverlay() {
        return progressOverlay;
    }

    // Show a Snackbar with a message
    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Thay đổi ui cho network
     * @param isConnected boolean
     */
    private void updateNetworkUI(boolean isConnected) {
        if (isConnected) {
            networkStatusView.setVisibility(View.GONE);
        } else {
            networkStatusView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNetworkStateChanged(boolean isAvailable) {
        // Được gọi mỗi khi trạng thái mạng thay đổi
        updateNetworkUI(isAvailable);

        if (isAvailable) {
            // Mạng đã được kết nối
            // Tải lại dữ liệu, gửi tin nhắn đang chờ, etc.
            showSnackbar("Mạng đã được kết nối");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đăng ký nhận thông báo khi Activity hiển thị
        networkMonitor.addListener(this);

        // Cập nhật UI với trạng thái mạng hiện tại
        updateNetworkUI(networkMonitor.isNetworkAvailable());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đăng ký khi Activity không hiển thị
        networkMonitor.removeListener(this);
    }
}
