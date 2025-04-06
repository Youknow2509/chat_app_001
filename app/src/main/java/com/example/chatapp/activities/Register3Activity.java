package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityRegisterV23Binding;
import com.example.chatapp.models.request.AccountModels;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.network.NetworkMonitor;
import com.example.chatapp.service.NetworkMonitorService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register3Activity extends AppCompatActivity implements NetworkMonitor.NetworkStateListener {
    private ActivityRegisterV23Binding binding;

    private String mail;
    private String token;
    private String password;

    private ApiManager apiManager;
    private NetworkMonitor networkMonitor;
    private View networkStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterV23Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initVariable();
        startNetworkMonitorService();
        getIntentData();
        setListeners();
        setupKeyboardLayoutListener();
    }

    private void initVariable() {
        apiManager = new ApiManager(this);
        networkMonitor = NetworkMonitor.getInstance(getApplicationContext());
        networkStatusView = findViewById(R.id.network_status_view);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        mail = intent.getStringExtra("mail");
        token = intent.getStringExtra("token");
        password = intent.getStringExtra("password");
    }

    private void setListeners() {
        binding.backToLoginButton.setOnClickListener(v -> backToPreviousStep());
        binding.nextButton.setOnClickListener(v -> CreateNameAccount());
    }

    private void setupKeyboardLayoutListener() {
        final View rootView = binding.getRoot();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                switchToCompactLayout();
            } else {
                switchToFullLayout();
            }
        });
    }

    private void backToPreviousStep() {
        Intent intent = new Intent(Register3Activity.this, Register22Activity.class);
        startActivity(intent);
        finish();
    }

    private void switchToCompactLayout() {
        binding.headerBackground.setBackgroundResource(R.drawable.bg_header_squar);
        binding.headerBackground2.setBackgroundResource(R.drawable.bg_header_squar_white);

        setLayoutMargin(binding.headerBackground, 500);
        setLayoutMargin(binding.headerBackground2, 436);
        setTopMargin(binding.avatarImage, 40);
    }

    private void switchToFullLayout() {
        binding.headerBackground.setBackgroundResource(R.drawable.bg_header_curve);
        binding.headerBackground2.setBackgroundResource(R.drawable.bg_header_curve_white);

        setLayoutMargin(binding.headerBackground, 424);
        setLayoutMargin(binding.headerBackground2, 376);
        setTopMargin(binding.avatarImage, 100);
    }

    private void setLayoutMargin(View view, int dp) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.bottomMargin = dpToPx(dp);
        view.setLayoutParams(params);
    }

    private void setTopMargin(View view, int dp) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.topMargin = dpToPx(dp);
        view.setLayoutParams(params);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void CreateNameAccount() {
        binding.progressOverlay.setVisibility(View.VISIBLE);
        apiManager.upgradePasswordRegister(
                new AccountModels.UpdatePasswordInput(password, token),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        binding.progressOverlay.setVisibility(View.GONE);
                        if (response.body() == null) {
                            showToast("Lỗi không xác định!");
                            return;
                        }

                        int code = response.body().getCode();
                        if (code != Constants.CODE_SUCCESS) {
                            showToast(response.body().getMessage());
                        } else {
                            showToast("Tạo mật khẩu người dùng thành công!");
                            UpdateNameAndAvatar();
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

    private void UpdateNameAndAvatar() {
        String name = binding.nameInput.getText().toString();
        String url_avatar = Constants.URL_AVATAR_DEFAULT;

        apiManager.upgradeNameAndAvatarRegister(
                new AccountModels.UpgradeNameAndAvatarRegisterInput(mail, token, url_avatar, name),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        binding.progressOverlay.setVisibility(View.GONE);

                        if (response.body() == null) {
                            showToast("Đã xảy ra lỗi khi cập nhật thông tin.");
                            return;
                        }

                        if (response.body().getCode() != Constants.CODE_SUCCESS) {
                            showToast(response.body().getMessage());
                        } else {
                            showToast("Tạo tài khoản thành công!");
                            goToLogin();
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

    private void goToLogin() {
        Intent intent = new Intent(Register3Activity.this, LoginActivity.class);
        intent.putExtra("email", mail);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
//        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.d("Register3Activity", message);
    }

    private void updateNetworkUI(boolean isConnected) {
        networkStatusView.setVisibility(isConnected ? View.GONE : View.VISIBLE);
        binding.nextButton.setEnabled(isConnected);
    }

    private void startNetworkMonitorService() {
        Intent serviceIntent = new Intent(this, NetworkMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    public void onNetworkStateChanged(boolean isAvailable) {
        updateNetworkUI(isAvailable);
        showToast("Mạng đã mất kết nối");

        if (isAvailable) {
            showToast("Mạng đã được kết nối");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkMonitor.addListener(this);
        updateNetworkUI(networkMonitor.isNetworkAvailable());
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkMonitor.removeListener(this);
    }
}
