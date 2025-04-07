package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityMyFriendBinding;
import com.google.android.material.tabs.TabLayout;

public class MyFriendActivity extends AppCompatActivity {

    private ActivityMyFriendBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.FriendInvitationText.setOnClickListener(v->openViewInvitation());


    }

    private void openViewInvitation() {
        // Open the FriendInvitationActivity
        Intent intent = new Intent(this, FriendInvitationActivity.class);
        startActivity(intent);

    }
}