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
import com.example.chatapp.databinding.ActivityRegisterV21Binding;
import com.example.chatapp.network.HttpClient;
import com.example.chatapp.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterV21Binding binding;

    private ApiManager apiManager;
    // PURPOSE REGISTER TEST
    private final String PURPOSE_REGISTER_TEST = "TEST_USER";
    private final String PURPOSE_REGISTER_PROD = "ANDROID_APP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterV21Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initVariableUse();

        binding.nextButton.setOnClickListener(v -> next());
        binding.backToLoginButton.setOnClickListener(v-> BackToIntent());
    }

    private void BackToIntent() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // initVariableUse
    private void initVariableUse() {
        apiManager = new ApiManager();
    }

    // handle click next button
    private void next() {
        String emailInput = binding.mailEditText.getText().toString().trim();
//        apiManager.registerUser(
//                new AccountModels.RegisterInput(emailInput, PURPOSE_REGISTER_PROD, 1),
//                new Callback<ResponseData<Object>>() {
//                    @Override
//                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
//                        binding.progressOverlay.setVisibility(View.GONE);
//                        int code = response.body().getCode();
//                        if (code != Constants.CODE_SUCCESS) {
//                            String message = Utils.getMessageByCode(code);
//                            showToast(message);
//                        } else {
//                            showToast("Vui lòng kiểm tra email để nhận mã xác nhận");
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
//                        binding.progressOverlay.setVisibility(View.GONE);
//
//                        showToast("Lỗi kết nối đến máy chủ");
//                    }
//                }
//        );

        // Forward to OTP screen
        Intent intent = new Intent(RegisterActivity.this, Register2Activity.class);
        intent.putExtra("email", binding.mailEditText.getText().toString());
        startActivity(intent);
        finish();
    }


    // helper show toast
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }


}
