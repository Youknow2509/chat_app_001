package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.network.HttpClient;
import com.example.chatapp.utils.Utils;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public class SignUpActivity extends AppCompatActivity {

    private Button btnGetCode;
    private TextView tvCodeSent;
    private CountDownTimer countDownTimer;
    private EditText[] codeInputs;
    private HttpClient httpClient;
    private String emailInput;
    private String tokenVerifyOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signupv2);

        initVariableUse();

        btnGetCode = findViewById(R.id.btn_get_code);
        tvCodeSent = findViewById(R.id.tv_code_sent);

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

        findViewById(R.id.btn_signup).setOnClickListener(v -> {
            handleBtnRegister();
        });

        btnGetCode.setOnClickListener(v -> {
            sendVerificationCode();
        });
    }

    // initVariableUse
    private void initVariableUse(){
        httpClient = new HttpClient();
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
            verifyOtp(emailInput, code).thenAccept(verified -> runOnUiThread(() -> {
                if (verified && tokenVerifyOTP != null && !tokenVerifyOTP.isEmpty()) {
                    // OTP hợp lệ, điều hướng sang màn hình tiếp theo
                    Intent intent = new Intent(SignUpActivity.this, UpdatePasswordRegisterActivity.class);
                    intent.putExtra("email", emailInput);
                    intent.putExtra("token", tokenVerifyOTP);
                    startActivity(intent);
                    finish();
                } else {
                    // OTP không hợp lệ
                    showToast("Vui lòng nhập đúng OTP");
                }
            }));
        } else {
            showToast("Vui lòng nhập đủ 6 số OTP");
        }
    }


    // verify otp
    private CompletableFuture<Boolean> verifyOtp(String email, String otp) {
        CompletableFuture<Boolean> verifyFuture = new CompletableFuture<>();

        CompletableFuture<JsonObject> future = httpClient.verifyOtp(email, otp);
        future.thenAccept(res -> runOnUiThread(() -> {
            try {
                int codeRes = res.has("code") ? res.get("code").getAsInt() : -1;
                if (codeRes == Utils.ErrCodeSuccess) {
                    JsonObject data = res.get("data").getAsJsonObject();
                    this.tokenVerifyOTP = data.get("token").getAsString();
                    Log.d("VerifyOTP", "Token: " + tokenVerifyOTP);
                    verifyFuture.complete(true);
                } else {
                    Log.e("VerifyOTP", "Verify failed: " + Utils.getMessageByCode(codeRes));
                    showToast("Vui lòng nhập đúng OTP");
                    verifyFuture.complete(false);
                }
            } catch (Exception e) {
                Log.e("VerifyOTP", "Error parsing response", e);
                showToast("Error processing response");
                verifyFuture.complete(false);
            }
        })).exceptionally(e -> {
            runOnUiThread(() -> {
                Log.e("VerifyOTP", "Network request failed: ", e);
                showToast("Network error! Please try again.");
                verifyFuture.complete(false);
            });
            return null;
        });

        return verifyFuture;
    }


    // send otp
    private void sendOtp() {
        CompletableFuture<JsonObject> future = httpClient.register(emailInput);
        future.thenAccept(res -> runOnUiThread(() -> {
            try {
                int codeRes = 0;
                if (res.has("code")) {
                    codeRes = res.get("code").getAsInt();
                }
                if (codeRes != Utils.ErrCodeSuccess) {
                    Log.e("RequestOTP", "RequestOTP failed: " + Utils.getMessageByCode(codeRes));
                }
            } catch (Exception e) {
                Log.e("SignIn", "Error parsing response", e);
                showToast("Error processing response");
            }
        })).exceptionally(e -> {
            runOnUiThread(() -> {
                Log.e("RequestOTP", "RequestOTP failed: ", e);
                showToast("Network error! Please try again.");
            });
            return null;
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

}
