package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityCreateGroupBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class CreateNewGroupActivity extends AppCompatActivity {
    private ActivityCreateGroupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v -> onBackPressed());
        binding.createGroupButton.setOnClickListener(v -> createGroup());
        binding.addMemberLayout.setOnClickListener(v->showAddMembersBottomSheet());
    }

    private void createGroup(){
        // TODO: Implement create group logic
    }

    private void showAddMembersBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_add_members, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Thiết lập để có thể vuốt xuống để đóng
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Ánh xạ các thành phần và xử lý sự kiện
        Button cancelButton = bottomSheetView.findViewById(R.id.cancelButton);
        Button addButton = bottomSheetView.findViewById(R.id.addButton);

        cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Hiển thị dialog
        bottomSheetDialog.show();
    }

}
