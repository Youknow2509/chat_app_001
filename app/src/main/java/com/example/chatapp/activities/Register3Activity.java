package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityRegisterV23Binding;
import com.example.chatapp.models.request.AccountModels;
import com.example.chatapp.models.response.ResponseData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register3Activity extends AppCompatActivity {
    private ActivityRegisterV23Binding binding;
    //
    private String mail;
    private String token;
    private String password;
    private ApiManager apiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterV23Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        initVariable();
        getIntentData();
        setListeners();
    }

    /**
     * initVariable
     */
    private void initVariable() {
        apiManager = new ApiManager();
    }

    /**
     * listen event element
     */
    private void setListeners() {
        binding.backToLoginButton.setOnClickListener(v -> BackToIntent());
//        binding.backToLoginButton.setOnClickListener(v -> onBackPressed());
        binding.nextButton.setOnClickListener(v -> CreateNameAccount());

    }

    /**
     * Get data intent
     */
    private void getIntentData() {
        Intent intent = getIntent();
        mail = intent.getStringExtra("mail");
        token = intent.getStringExtra("token");
        password = intent.getStringExtra("password");
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
        binding.progressOverlay.setVisibility(View.VISIBLE);

        String name = binding.nameInput.getText().toString();
        String url_avatar = Constants.URL_AVATAR_DEFAULT; // TODO: lấy url avatar từ API



        // Call the API
        apiManager.upgradeNameAndAvatarRegister(
                new AccountModels.UpgradeNameAndAvatarRegisterInput(mail, token, url_avatar, name),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        binding.progressOverlay.setVisibility(View.GONE);

                        int code = response.body().getCode();
                        if (code != Constants.CODE_SUCCESS) {
                            Toast.makeText(Register3Activity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register3Activity.this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Register3Activity.this, LoginActivity.class);
                            intent.putExtra("email", mail);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        binding.progressOverlay.setVisibility(View.GONE);

                        Toast.makeText(Register3Activity.this, "Vui lòng kiểm tra lại kết nối mạng!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

//        Intent intent = new Intent(Register3Activity.this, LoginActivity.class);
//        startActivity(intent);
//        finish();
    }

    /**
     * Call api create account
     */
    private void CreateAccountBase() {
        apiManager.upgradePasswordRegister(
                new AccountModels.UpdatePasswordInput(password, token),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        binding.progressOverlay.setVisibility(View.GONE);
                        int code = response.body().getCode();
                        if (code != Constants.CODE_SUCCESS) {
                            showToast(response.body().getMessage());
                        } else {
                            showToast("Tạo mật khẩu người dùng thành công!");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        binding.progressOverlay.setVisibility(View.GONE);
                        showToast("Vui lòng kiểm tra lại kết nối mạng!");
                    }
                }
        );
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
}
