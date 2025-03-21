package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.chatapp.R;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.request.AccountModels;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.network.HttpClient;
import com.example.chatapp.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    private Button btnGetCode;
    private TextView tvCodeSent;
    private CountDownTimer countDownTimer;
    private EditText[] codeInputs;
    private FrameLayout progressBar;

    private String emailInput;
    private String tokenVerifyOTP;
    private ApiManager apiManager;
    // PURPOSE REGISTER TEST
    private final String PURPOSE_REGISTER_TEST = "TEST_USER";
    private final String PURPOSE_REGISTER_PROD = "ANDROID_APP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signupv2);

        initVariableUse();

        btnGetCode = findViewById(R.id.btn_get_code);
        tvCodeSent = findViewById(R.id.tv_code_sent);
        progressBar = findViewById(R.id.progress_overlay);

        codeInputs = new EditText[]{
                findViewById(R.id.code_digit_1),
                findViewById(R.id.code_digit_2),
                findViewById(R.id.code_digit_3),
                findViewById(R.id.code_digit_4),
                findViewById(R.id.code_digit_5),
                findViewById(R.id.code_digit_6)
        };

        for (EditText codeInput : codeInputs) {
            codeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            codeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        // Thiết lập xử lý nhập OTP
        setupOtpInputs();

        findViewById(R.id.btn_signup).setOnClickListener(v -> {
            handleBtnRegister();
        });

        btnGetCode.setOnClickListener(v -> {
            sendVerificationCode();
        });
    }

    // initVariableUse
    private void initVariableUse(){
        apiManager = new ApiManager();
    }

    private void sendVerificationCode() {

        // get email input
        emailInput = ((EditText)findViewById(R.id.editTextTextEmailAddress2)).getText().toString();

        if (emailInput.isEmpty() || emailInput == "") {
            showToast("Vui long nhap email");
            return;
        }

        tvCodeSent.setVisibility(View.VISIBLE);
        btnGetCode.setEnabled(false);
        btnGetCode.setBackgroundColor(getResources().getColor(R.color.nav_item_color)); // Đổi màu nút khi bị vô hiệu hóa

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnGetCode.setText("Resend in " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                btnGetCode.setEnabled(true);
                btnGetCode.setText("Get Code");
                btnGetCode.setBackgroundColor(getResources().getColor(R.color.primary)); // Trả lại màu ban đầu
            }
        }.start();

        // send otp and save token in device
        progressBar.setVisibility(View.VISIBLE);
        sendOtp();
    }

    // handle submit otp and mail register
    private void handleBtnRegister() {
        String code = "";
        for (EditText codeInput : codeInputs) {
            code += codeInput.getText().toString();
        }
        emailInput = ((EditText)findViewById(R.id.editTextTextEmailAddress2)).getText().toString();
        Log.d("handleBtnRegister", "Code: " + code + " Email: " + emailInput);

        if (code.length() == 6) {
            progressBar.setVisibility(View.VISIBLE);
            verifyOtp(emailInput, code);
        } else {
            showToast("Vui lòng nhập đủ 6 số OTP");
        }
    }

    // verify otp to server
    private void verifyOtp(String email, String otp) {
        apiManager.verifyAccount(
                new AccountModels.VerifyInput(otp, email),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        progressBar.setVisibility(View.GONE);
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
                            Intent intent = new Intent(SignUpActivity.this, UpdatePasswordRegisterActivity.class);
                            intent.putExtra("email", emailInput);
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
                        progressBar.setVisibility(View.GONE);
                        showToast("Lỗi kết nối đến máy chủ");
                    }
                });
    }

    // send otp
    private void sendOtp() {
        apiManager.registerUser(
                new AccountModels.RegisterInput(emailInput, PURPOSE_REGISTER_PROD, 1),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        progressBar.setVisibility(View.GONE);
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
                        progressBar.setVisibility(View.GONE);

                        showToast("Lỗi kết nối đến máy chủ");
                    }
                }
        );
    }

    // helper show toast
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    // handle event input otp
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
                        Utils.hideKeyboard(SignUpActivity.this);
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

    // helper delete all number otp
    private void deleteOtp() {
        for (EditText codeInput : codeInputs) {
            codeInput.setText("");
        }
    }

}
