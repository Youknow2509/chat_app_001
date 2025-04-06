package com.example.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.ActivityRegisterNameAvatarBinding;
import com.example.chatapp.models.request.AccountModels;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.network.NetworkMonitor;
import com.example.chatapp.utils.session.SessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNameAndAvatarRegisterActivity extends BaseNetworkActivity {
    private ActivityRegisterNameAvatarBinding binding;
    private View nwStatusView;
    //
    private String mail;
    private String token;
    private String password;
    //
    private ApiManager apiManager;

    private SessionManager sessionManager;

    private String currentMediaType = "image"; // Default to image
    private static final String FILEPROVIDER_AUTHORITY = "com.example.chatapp.fileprovider";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Uri currentMediaUri;

    private String TAG = "CreateNameAndAvatarRegisterActivity";

    private Context context;


    private final ActivityResultLauncher<String[]> pickMediaLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    // Persist permission for this URI
                    context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // Hiển thị dialog xem trước ảnh
                    showImagePreviewDialog(uri);

                    Log.i(TAG, "Selected media URI: " + uri.toString());
                }
            });

    // Activity result launcher for taking photo with camera
    private final ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && currentMediaUri != null) {
                    currentMediaType = "image";
                    showImagePreviewDialog(currentMediaUri);
                }
            });

    private void showImagePreviewDialog(Uri imageUri) {
        binding.undoAvatar.setVisibility(View.VISIBLE);
        // Ánh xạ các thành phần trong dialog
        ImageView imagePreview = binding.avatarImage;

        // Hiển thị ảnh xem trước
        Glide.with(context)
                .load(imageUri)
                .into(imagePreview);
    }

    private void editAvatar() {
        // Hiển thị dialog lựa chọn
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chọn ảnh đại diện");
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Chụp ảnh
                    if (checkCameraPermission()) {
                        takePhoto();
                    }
                    break;
                case 1: // Chọn từ thư viện
                    openGallery();
                    break;
            }
        });

        builder.show();
    }

    /**
     * Open gallery for media selection
     */
    private void openGallery() {
        pickMediaLauncher.launch(new String[]{"image/*", "video/*"});
    }

    /**
     * Create a temporary file for media capture
     */
    private File createMediaFile(String extension) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "MEDIA_" + timeStamp + "_";
        File storageDir = context.getFilesDir();
        return File.createTempFile(fileName, extension, storageDir);
    }

    /**
     * Open camera to take a photo
     */
    private void takePhoto() {
        try {
            File photoFile = createMediaFile(".jpg");

            currentMediaUri = FileProvider.getUriForFile(context,
                    FILEPROVIDER_AUTHORITY,
                    photoFile);
            Log.i(TAG, "Photo URI: " + currentMediaUri.toString());
            takePhotoLauncher.launch(currentMediaUri);

        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file", ex);
            Toast.makeText(context, "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CreateNameAndAvatarRegisterActivity.this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterNameAvatarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initVariable();
        getIntentData();
        setListeners();
        setupKeyboardLayoutListener();
    }

    private void initVariable() {
        context = this;
        apiManager = new ApiManager(this);
        sessionManager = new SessionManager(this);
        networkMonitor = NetworkMonitor.getInstance(getApplicationContext());
        nwStatusView = findViewById(R.id.network_status_view);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        mail = intent.getStringExtra("mail");
        token = intent.getStringExtra("token");
        password = intent.getStringExtra("password");
    }

    private void setListeners() {
        binding.editAvatar.setOnClickListener(v -> editAvatar());
        binding.backToLoginButton.setOnClickListener(v -> backToPreviousStep());
        binding.nextButton.setOnClickListener(v -> CreateNameAccount());
        binding.undoAvatar.setOnClickListener(v->{
            binding.avatarImage.setImageResource(R.drawable.ic_about);
            binding.undoAvatar.setVisibility(View.GONE);
        });
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
        Intent intent = new Intent(CreateNameAndAvatarRegisterActivity.this, CreatePasswordRegisterActivity.class);
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
        String name = binding.nameInput.toString();
        if (!validateName(name)) {
            return;
        }
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

    private boolean validateName(String n) {
        if (n.isEmpty()) {
            binding.nameInput.setError("Vui lòng nhập tên người dùng!");
            showToast("Vui lòng nhập tên người dùng!");
            return false;
        }
        if (n.length() <= 3) {
            binding.nameInput.setError("Tên người dùng phải có ít nhất 3 ký tự!");
            return false;
        }
        return true;
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
        Intent intent = new Intent(CreateNameAndAvatarRegisterActivity.this, LoginActivity.class);
        intent.putExtra("email", mail);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
//        runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Log.d("CreateNameAndAvatarRegisterActivity", message);
    }

    @Override
    protected void onNetworkAvailable() {
        super.onNetworkAvailable();
        binding.nextButton.setEnabled(true);
        nwStatusView.setVisibility(View.GONE);
        binding.avatarImage.setEnabled(true);
        binding.editAvatar.setEnabled(true);
    }

    @Override
    protected void onNetworkUnavailable() {
        super.onNetworkUnavailable();
        binding.nextButton.setEnabled(false);
        nwStatusView.setVisibility(View.VISIBLE);
        binding.avatarImage.setEnabled(false);
        binding.editAvatar.setEnabled(false);
    }
}
