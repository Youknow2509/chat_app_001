package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityRegisterV23Binding;

public class Register3Activity extends AppCompatActivity {
    private ActivityRegisterV23Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterV23Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backToLoginButton.setOnClickListener(v -> onBackPressed());
        binding.nextButton.setOnClickListener(v -> CreateNameAccount());

        // Theo dõi sự kiện thay đổi layout (để biết khi nào bàn phím mở)
        final View rootView = binding.getRoot();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                // Bàn phím đang mở
                switchToCompactLayout();
            } else {
                // Bàn phím đã đóng
                switchToFullLayout();
            }
        });
        binding.backToLoginButton.setOnClickListener(v-> BackToIntent());
    }

    private void BackToIntent() {
        Intent intent = new Intent(Register3Activity.this, Register22Activity.class);
        startActivity(intent);
        finish();
    }

    private void switchToCompactLayout() {
        // Đổi background header
        binding.headerBackground.setBackgroundResource(R.drawable.bg_header_squar);
        binding.headerBackground2.setBackgroundResource(R.drawable.bg_header_squar_white);

        // Đổi layout_marginBottom cua 2 background
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.headerBackground.getLayoutParams();
        // Tu 424dp -> 500dp
        params.bottomMargin = dpToPx(500);

        binding.headerBackground.setLayoutParams(params);

        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) binding.headerBackground2.getLayoutParams();

        // Tu 376dp -> 436dp
        params2.bottomMargin = dpToPx(436);

        binding.headerBackground2.setLayoutParams(params2);

        // Thu nhỏ/move avatar nếu muốn
        ConstraintLayout.LayoutParams avatarParams = (ConstraintLayout.LayoutParams) binding.avatarImage.getLayoutParams();
        avatarParams.topMargin = dpToPx(40); // từ 100 -> 40
        binding.avatarImage.setLayoutParams(avatarParams);
    }

    private void switchToFullLayout() {
        binding.headerBackground.setBackgroundResource(R.drawable.bg_header_curve);
        binding.headerBackground2.setBackgroundResource(R.drawable.bg_header_curve_white);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.headerBackground.getLayoutParams();
        // Tu 500dp -> 424dp
        params.bottomMargin = dpToPx(424);
        binding.headerBackground.setLayoutParams(params);
        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) binding.headerBackground2.getLayoutParams();
        // Tu 436dp -> 376dp
        params2.bottomMargin = dpToPx(376);
        binding.headerBackground2.setLayoutParams(params2);

        ConstraintLayout.LayoutParams avatarParams = (ConstraintLayout.LayoutParams) binding.avatarImage.getLayoutParams();
        avatarParams.topMargin = dpToPx(100);
        binding.avatarImage.setLayoutParams(avatarParams);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }



    private void CreateNameAccount() {
        String name = binding.nameInput.getText().toString();
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        String token = getIntent().getStringExtra("token");

        // Call the API to create an account with the provided name, email, and password
        // You can use the apiManager to make the API call
        // Example:
        // apiManager.createAccount(name, email, password, new ApiCallback() {

        Intent intent = new Intent(Register3Activity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
