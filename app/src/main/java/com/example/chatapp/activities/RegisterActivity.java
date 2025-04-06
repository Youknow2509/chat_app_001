package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityRegisterMailBinding;
import com.example.chatapp.models.request.AccountModels;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseNetworkActivity {

    private ActivityRegisterMailBinding binding;

    private ApiManager apiManager;
    // network
    private View networkStatusView;
    // PURPOSE REGISTER TEST
    private final String PURPOSE_REGISTER_TEST = "TEST_USER";
    private final String PURPOSE_REGISTER_PROD = "ANDROID_APP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterMailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        networkStatusView = findViewById(R.id.network_status_view);

        initVariableUse();

        binding.nextButton.setOnClickListener(v -> next());
        binding.backToLoginButton.setOnClickListener(v -> BackToIntent());
    }

    private void BackToIntent() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // initVariableUse
    private void initVariableUse() {
        apiManager = new ApiManager(this);
    }

    // handle click next button
    private void next() {
        String emailInput = binding.mailEditText.getText().toString().trim();
        if (!validateEmail(emailInput)) {
            return;
        }
        binding.progressOverlay.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.mailEditText.setTooltipText(null);
        }
        apiManager.registerUser(
                new AccountModels.RegisterInput(emailInput, PURPOSE_REGISTER_PROD, 1),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        binding.progressOverlay.setVisibility(View.GONE);
                        if (response.body() == null) {
                            showToast("Lỗi kết nối đến máy chủ");
                            return;
                        }
                        int code = response.body().getCode();
                        if (code != Constants.CODE_SUCCESS) {
                            String message = Utils.getMessageByCode(code);
                            showToast(message);
                        } else {
                            showToast("Vui lòng kiểm tra email để nhận mã xác nhận");
                        }

                        Intent intent = new Intent(RegisterActivity.this, VerifyOTPRegisterActivity.class);
                        intent.putExtra("email", binding.mailEditText.getText().toString());
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        binding.progressOverlay.setVisibility(View.GONE);

                        showToast("Lỗi kết nối đến máy chủ");
                    }
                }
        );

        // Forward to OTP screen
//        Intent intent = new Intent(RegisterActivity.this, VerifyOTPRegisterActivity.class);
//        intent.putExtra("email", binding.mailEditText.getText().toString());
//        startActivity(intent);
//        finish();
    }

    // validate email input
    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            binding.mailEditText.setError("Vui lòng nhập email");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.mailEditText.setError("Email không hợp lệ");
            return false;
        }
        return true;
    }

    // helper show toast
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onNetworkAvailable() {
        super.onNetworkAvailable();
        networkStatusView.setVisibility(View.GONE);
        binding.nextButton.setEnabled(true);
    }

    @Override
    protected void onNetworkUnavailable() {
        super.onNetworkUnavailable();
        networkStatusView.setVisibility(View.VISIBLE);
        binding.nextButton.setEnabled(false);
    }
}
