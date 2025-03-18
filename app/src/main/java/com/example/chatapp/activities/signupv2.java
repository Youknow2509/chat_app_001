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

public class signupv2 extends AppCompatActivity {

    private Button btnGetCode;
    private TextView tvCodeSent;
    private CountDownTimer countDownTimer;
    private EditText[] codeInputs;
    private HttpClient httpClient;
    private TextView btnNext;
    private String emailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signupv2);

        initVariableUse();

        btnGetCode = findViewById(R.id.btn_get_code);
        tvCodeSent = findViewById(R.id.tv_code_sent);
        btnNext = findViewById(R.id.btn_signup);

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

        btnNext.setOnClickListener(v -> {
            handleBtnRegister(v);
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
        // get email input
        emailInput = ((EditText)findViewById(R.id.editTextTextEmailAddress2)).getText().toString();
        // send otp and save token in device
        sendOtp();
    }

    // handle submit otp and mail register
    private void handleBtnRegister(View v) {
        String code = "";
        for (EditText codeInput : codeInputs) {
            code += codeInput.getText().toString();
        }
        if (code.length() == 6) {
            // Verify code
            Intent intent = new Intent(signupv2.this, UpdatePasswordRegisterActivity.class);
            intent.putExtra("email", emailInput);
            startActivity(intent);
            finish();
        }
        else {
            showToast("Vui long nhap dung otp");
        }
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
