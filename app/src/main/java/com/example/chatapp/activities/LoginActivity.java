package com.example.chatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityLoginV2Binding;
import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.utils.session.SessionManager;
import com.example.chatapp.viewmodel.LoginViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginV2Binding binding;
    private FrameLayout progressOverlay;
    private LoginViewModel loginViewModel;
    private Context context;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginV2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressOverlay = findViewById(R.id.progress_overlay);

        loginViewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(LoginViewModel.class);
        context = this;
        sessionManager = new SessionManager(context);

        handleFieldMail();

        observeLiveData();

        setListener();
    }

    /**
     * set observe live data from view model
     */
    private void observeLiveData() {
        loginViewModel.getResPathFileAvatar().observe(this,
                path -> {
                    if (path != null && !path.isEmpty()) {
                        saveLocalPathFileAvatar(path);
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
}
