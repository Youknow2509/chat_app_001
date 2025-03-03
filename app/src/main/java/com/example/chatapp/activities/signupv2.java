package com.example.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapp.R;

public class signupv2 extends AppCompatActivity {

    private Button btnGetCode;
    private TextView tvCodeSent;
    private CountDownTimer countDownTimer;
    private EditText[] codeInputs;

    private TextView btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signupv2);

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
            String code = "";
            for (EditText codeInput : codeInputs) {
                code += codeInput.getText().toString();
            }
            if (code.length() == 6) {
                // Verify code
                Intent intent = new Intent(signupv2.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                // Show error
            }
        });




        btnGetCode.setOnClickListener(v -> {
            sendVerificationCode();
        });
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
    }
}
