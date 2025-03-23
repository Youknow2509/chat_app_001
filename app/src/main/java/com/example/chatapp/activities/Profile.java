package com.example.chatapp.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityProfileBinding;

public class Profile extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toi xem lai
        binding.txtNameView.setText("Test123");
        binding.txtEmailView.setText("Test123");
        binding.txtAddressView.setText("Test123");
        binding.txtPhoneNumView.setText("Test123");
        binding.btnback.setOnClickListener(v -> back_act());
    }

    void back_act() {
        finish();
    }
}