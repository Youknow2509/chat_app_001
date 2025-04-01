package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.User;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.chatapp.databinding.ActivityUserChatInformationBinding;

import com.example.chatapp.R;

public class UserChatInformationActivity extends AppCompatActivity {

    private ActivityUserChatInformationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUserChatInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v->back());
        binding.customReportLayout.setOnClickListener(v->customReport());
        binding.customBlockLayout.setOnClickListener(v->blockUser());

        getDataFromIntent();

    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Constants.KEY_USER)) {
                User user = intent.getParcelableExtra(Constants.KEY_USER);
                if (user != null) {
                    binding.userName.setText(user.name);
                    binding.emailAddress.setText(user.email);
                }
            }
        }
    }

    private void back() {
        finish();
    }

    private void customReport() {
        Snackbar.make(binding.getRoot(), "Reported", Snackbar.LENGTH_LONG).show();
    }

    private void blockUser() {
        Snackbar.make(binding.getRoot(), "Blocked", Snackbar.LENGTH_LONG).show();
    }
}