package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityRegisterOtpBinding;
import com.example.chatapp.models.request.AccountModels;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOTPRegisterActivity extends BaseNetworkActivity {

    private EditText[] codeInputs;
    private View networkStatusView;
    private ActivityRegisterOtpBinding binding;
    //
    private String tokenVerifyOTP;
    private String emailRegister;
    private ApiManager apiManager;
    private final String PURPOSE_REGISTER_PROD = "ANDROID_APP";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: ui time and block send otp dup
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initVariableUse();
        initView();

        codeInputs = new EditText[]{binding.otp1, binding.otp2, binding.otp3, binding.otp4, binding.otp5, binding.otp6};

        for (EditText codeInput : codeInputs) {
            codeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            codeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        setupOtpInputs();
        binding.nextButton.setOnClickListener(v -> handleBtnRegister());
        binding.backToLoginButton.setOnClickListener(v -> BackToIntent());
        binding.resendText.setOnClickListener(l -> {
            callResendOTP();
        });
    }

    /**
     * call api resend otp
     */
    private void callResendOTP() {
        apiManager.registerUser(
                new AccountModels.RegisterInput(
                        emailRegister,
                        PURPOSE_REGISTER_PROD,
                        1
                ),
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
                    }
                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        binding.progressOverlay.setVisibility(View.GONE);
                        showToast("Lỗi kết nối đến máy chủ");
                    }
                }
        );
    }

    /**
     * initView from activity before
     */
    private void initView() {
        Intent intent = getIntent();
        emailRegister = intent.getStringExtra("email");
        binding.sendToMail.setText("Sent to: " + emailRegister);
    }

    private void BackToIntent() {
        Intent intent = new Intent(VerifyOTPRegisterActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void initVariableUse() {
        networkStatusView = findViewById(R.id.network_status_view);

        apiManager = new ApiManager(this);
    }

    private void setupOtpInputs() {
        for (int i = 0; i < codeInputs.length; i++) {
            final int currentIndex = i;
            codeInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Khi nhập một ký tự, chuyển sang ô tiếp theo
                    if (s.length() == 1 && currentIndex < codeInputs.length - 1) {
                        codeInputs[currentIndex + 1].requestFocus();
                    }

                    // Nếu nhập đủ 6 ký tự, ẩn bàn phím
                    if (currentIndex == codeInputs.length - 1 && s.length() == 1) {
                        Utils.hideKeyboard(VerifyOTPRegisterActivity.this);
                        handleBtnRegister();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            // Xử lý sự kiện xóa để quay lại ô trước đó
            codeInputs[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (codeInputs[currentIndex].getText().toString().isEmpty() && currentIndex > 0) {
                        codeInputs[currentIndex - 1].requestFocus();
                        codeInputs[currentIndex - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    // verify otp to server
    private void verifyOtp(String email, String otp) {
        apiManager.verifyAccount(new AccountModels.VerifyInput(otp, email), new Callback<ResponseData<Object>>() {
            @Override
            public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                binding.progressOverlay.setVisibility(View.GONE);
                int code = response.body().getCode();
                if (code != Constants.CODE_SUCCESS) {
                    String message = Utils.getMessageByCode(code);
                    showToast(message);
                    deleteOtp();
                    return;
                }
                try {
                    tokenVerifyOTP = Utils.getDataBody(response.body(), "token");
                    if (tokenVerifyOTP.isEmpty()) {
                        showToast("Lỗi xử lý dữ liệu");
                        deleteOtp();
                        return;
                    }
                    Log.d("SignUp", "Token verify otp: " + tokenVerifyOTP);
                    // OTP hợp lệ, điều hướng sang màn hình tiếp theo
                    Intent intent = new Intent(VerifyOTPRegisterActivity.this, CreatePasswordRegisterActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("token", tokenVerifyOTP);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e("VerifyOTP", "Error parsing response data", e);
                    showToast("Lỗi xử lý dữ liệu");
                    deleteOtp();
                }
            }

            @Override
            public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                binding.progressOverlay.setVisibility(View.GONE);
                showToast("Lỗi kết nối đến máy chủ");
            }
        });

//        Intent intent = new Intent(VerifyOTPRegisterActivity.this, CreatePasswordRegisterActivity.class);
//        intent.putExtra("email", email);
//        intent.putExtra("token", tokenVerifyOTP);
//        startActivity(intent);
//        finish();
    }

    private void handleBtnRegister() {
        Intent intent = getIntent();
        String emailInput = intent.getStringExtra("email");
        String code = "";
        for (EditText codeInput : codeInputs) {
            code += codeInput.getText().toString();
        }
        if (code.length() == 6) {
            binding.progressOverlay.setVisibility(View.VISIBLE);
            verifyOtp(emailInput, code);
        } else {
            showToast("Vui lòng nhập đủ 6 số OTP");
        }
    }


    // helper delete all number otp
    private void deleteOtp() {
        for (EditText codeInput : codeInputs) {
            codeInput.setText("");
        }
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
        binding.resendText.setEnabled(true);
    }

    @Override
    protected void onNetworkUnavailable() {
        super.onNetworkUnavailable();
        networkStatusView.setVisibility(View.VISIBLE);
        binding.nextButton.setEnabled(false);
        binding.resendText.setEnabled(false);
    }
}
