package com.example.chatapp.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.activities.AddFriendActivity;
import com.example.chatapp.activities.CreateNewGroupActivity;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.databinding.FragmentProfileBinding;
import com.example.chatapp.models.UserDetail;
import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.models.request.UserModels;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.service.ImageService;
import com.example.chatapp.utils.cloudinary.CloudinaryManager;
import com.example.chatapp.utils.file.MediaUtils;
import com.example.chatapp.utils.session.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends BaseNetworkFragment {
    //
    private FragmentProfileBinding binding;
    private FrameLayout progressOverlay;
    //
    private SessionManager sessionManager;
    private UserProfileSession userProfileSession;
    private ApiManager apiManager;
    private final String TAG = "ProfileFragment";
    private String currentMediaType = "image"; // Default to image
    private static final String FILEPROVIDER_AUTHORITY = "com.example.chatapp.fileprovider";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Uri currentMediaUri;
    private File tempPhotoFileTakePictore;
    private CloudinaryManager cloudinaryManager;
    //
    private MutableLiveData<UserDetail> userDetailLiveData;
    private MutableLiveData<UserDetail> userDetailUpdateLiveData;
    private MutableLiveData<String> errorCallUpdateProfile;
    private MutableLiveData<File> avatarNewLive;
    //

    private final ActivityResultLauncher<String[]> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    // Persist permission for this URI
                    requireActivity().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // Determine media type
                    currentMediaType = "image";
                    //
                    currentMediaUri = uri;
                    showImagePreviewDialog(uri);
                }
            });

    // Activity result launcher for taking photo with camera
    private final ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && currentMediaUri != null) {
                    currentMediaType = "image";
                    showImagePreviewDialog(currentMediaUri);
                } else {
                    showSnackbar("Không thể chụp ảnh");
                    cleanupTempFiles();
                }
            });


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        initVariable();
        setListener();
        observeLiveData();
        return view;
    }

    /**
     * Xử lí khi không có mạng
     */
    @Override
    protected void onNetworkUnavailable() {
        super.onNetworkUnavailable();
        if (binding != null) {
            binding.editAvatar.setVisibility(View.GONE);
            binding.rightButton.setVisibility(View.GONE);
        }
    }

    /**
     * Xử lí khi có mạng
     */
    @Override
    protected void onNetworkAvailable() {
        super.onNetworkAvailable();
        if (binding != null) {
            binding.editAvatar.setVisibility(View.VISIBLE);
            binding.rightButton.setVisibility(View.VISIBLE);
        }
    }

    private void showImagePreviewDialog(Uri imageUri) {
        if (binding != null) {
            binding.saveButtonContainer.setVisibility(View.VISIBLE);

            // Hiển thị ảnh xem trước
            Glide.with(requireContext())
                    .load(imageUri)
                    .into(binding.avatarImage);
        }
    }

    private void initVariable() {
        this.apiManager = new ApiManager(requireContext());
        this.cloudinaryManager = CloudinaryManager.getInstance(requireContext());
        //
        this.sessionManager = new SessionManager(requireContext());
        this.userProfileSession = this.sessionManager.getUserProfile();
        //
        this.userDetailLiveData = new MutableLiveData<>();
        this.userDetailLiveData.setValue(new UserDetail(
                userProfileSession.getDisplayName(),
                userProfileSession.getEmail(),
                userProfileSession.getUserGender(),
                userProfileSession.getDateOfBirth(),
                sessionManager.getPathFileAvatarUser()
        ));
        //
        this.userDetailUpdateLiveData = new MutableLiveData<>();
        this.errorCallUpdateProfile = new MutableLiveData<>();
        this.avatarNewLive = new MutableLiveData<>();
        //
        this.progressOverlay = binding.progressOverlay;
    }

    /**
     * observeLiveData
     */
    private void observeLiveData() {
        // Xử lý khi chọn ảnh mới
        avatarNewLive.observe(getViewLifecycleOwner(), file -> {
            if (file != null) {
                handleAvatarUpload(file);
            } else {
                progressOverlay.setVisibility(View.GONE);
                showSnackbar("Không thể xử lý file ảnh");
            }
        });

        // Xử lý khi có dữ liệu người dùng mới
        userDetailLiveData.observe(getViewLifecycleOwner(), userDetail -> {
            if (userDetail != null) {
                initDataToView(userDetail);
            } else {
                Log.e(TAG, "Received null userDetail");
            }
        });

        // Xử lý khi cập nhật dữ liệu
        userDetailUpdateLiveData.observe(getViewLifecycleOwner(), userDetail -> {
            // update data to server
            progressOverlay.setVisibility(View.VISIBLE);
            callServerUpdateProfile();
        });

        // Xử lý kết quả cập nhật từ server
        errorCallUpdateProfile.observe(getViewLifecycleOwner(), error -> {
            progressOverlay.setVisibility(View.GONE);
            if (error != null && !error.isEmpty()) {
                showSnackbar(error);
                return;
            }
            userDetailLiveData.setValue(userDetailUpdateLiveData.getValue());
        });
    }

    /**
     * Xử lý upload ảnh lên Cloudinary
     */
    private void handleAvatarUpload(File file) {
        progressOverlay.setVisibility(View.VISIBLE);

        String path = Uri.fromFile(file).toString();
        String folder = "/users/" + sessionManager.getUserName() + "/images/";

        cloudinaryManager.uploadImage(path, folder, new CloudinaryManager.CloudinaryCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                // Get public ID and URL
                String url = (String) result.get("url");
                if (url != null) {
                    Log.d(TAG, "Upload success: " + url);

                    // Cập nhật thông tin người dùng với URL mới
                    UserDetail current = userDetailLiveData.getValue();
                    if (current != null) {
                        UserDetail updated = new UserDetail(
                                current.getName(),
                                current.getEmail(),
                                current.getGender(),
                                current.getBirthday(),
                                url  // URL Cloudinary mới // TODO fix
                        );
                        userDetailUpdateLiveData.postValue(updated);
                    } else {
                        progressOverlay.setVisibility(View.GONE);
                        showSnackbar("Không thể cập nhật thông tin người dùng");
                    }
                } else {
                    progressOverlay.setVisibility(View.GONE);
                    showSnackbar("Không nhận được URL từ server");
                }
            }

            @Override
            public void onError(String errorMsg) {
                progressOverlay.setVisibility(View.GONE);
                showSnackbar("Lỗi tải lên: " + errorMsg);
            }

            @Override
            public void onProgress(int progress) {
                // Có thể hiển thị progress bar
                Log.d(TAG, "Upload progress: " + progress + "%");
            }
        });
    }

    /**
     * Init data in view
     */
    private void initDataToView(UserDetail userDetail) {
        if (userDetail == null || binding == null) {
            Log.e(TAG, "UserDetail or binding is null");
            return;
        }
        binding.profileName.setText(userDetail.getName());
        binding.emailDetail.setText(userDetail.getEmail());
        binding.genderDetail.setText(userDetail.getGender());
        binding.birthdayDetail.setText(userDetail.getBirthday());

        ImageService.loadAndCacheImage(
                this,                       // Fragment hiện tại
                binding.avatarImage,        // ImageView để hiển thị
                userDetail.getPath_local_avatar(),  // Đường dẫn local
                sessionManager.getUserAvatar(),     // URL từ server
                sessionManager.getAccessToken(),    // Token xác thực
                newPath -> {
                    // Lưu đường dẫn mới
                    userDetail.setPath_local_avatar(newPath);
                    sessionManager.setPathFileAvatarUser(newPath);
                });
    }

    /**
     * listen event in element
     */
    private void setListener() {
        // Thiết lập sự kiện click cho nút Edit Profile
        binding.editAvatar.setOnClickListener(v -> editAvatar());
        binding.addButton.setOnClickListener(v -> {
            UserDetail userDetail = userDetailLiveData.getValue();
            if (userDetail != null) {
                showEditProfileBottomSheet(userDetail);
            } else {
                Log.e(TAG, "userDetailLiveData is null, cannot show edit profile bottom sheet");
                showSnackbar("Không thể tải thông tin người dùng");
            }
        });
        binding.addIcon.setOnClickListener(v -> {
            // Tạo popup menu
            PopupMenu popupMenu = new PopupMenu(getContext(), binding.addIcon);

            // Inflate menu resource
            popupMenu.getMenuInflater().inflate(R.menu.option_add_menu, popupMenu.getMenu());

            // Xoay icon 45 độ
            binding.addIcon.animate().rotation(45).setDuration(200);

            // Xử lý sự kiện khi đóng menu
            popupMenu.setOnDismissListener(menu -> {
                // Xoay icon về vị trí ban đầu
                binding.addIcon.animate().rotation(0).setDuration(200);
            });

            // Xử lý sự kiện khi chọn item trong menu
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_add_friend) {
                    Intent intent = new Intent(getContext(), AddFriendActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.action_create_group) {
                    Intent intent = new Intent(getContext(), CreateNewGroupActivity.class);
                    intent.putExtra("CREATE_GROUP", true);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            // Hiển thị popup menu
            popupMenu.show();
        });
        // Xử lý sự kiện nút lưu ảnh
        binding.rightButton.setOnClickListener(this::saveImageToDevice);
        // xử lí huỷ lưu ảnh
        binding.leftButton.setOnClickListener(this::cancelImageChanges);
    }

    /**
     * Xử lý huỷ thay đổi ảnh đại diện
     */
    private void cancelImageChanges(View v) {
        // Xóa ảnh đã chọn
        currentMediaUri = null;
        binding.saveButtonContainer.setVisibility(View.GONE);

        // Xóa file tạm nếu có
        cleanupTempFiles();

        // Hiển thị lại ảnh cũ
        UserDetail userDetail = userDetailLiveData.getValue();
        if (userDetail != null && userDetail.getPath_local_avatar() != null) {
            binding.avatarImage.setImageURI(Uri.fromFile(new File(userDetail.getPath_local_avatar())));
        }

        // Thông báo đã hủy
        Toast.makeText(getContext(), "Đã hủy thay đổi ảnh đại diện", Toast.LENGTH_SHORT).show();
    }

    /**
     * Xóa file tạm nếu tồn tại
     */
    private void cleanupTempFiles() {
        if (tempPhotoFileTakePictore != null && tempPhotoFileTakePictore.exists()) {
            boolean deleted = tempPhotoFileTakePictore.delete();
            if (deleted) {
                Log.d(TAG, "Temp photo file deleted successfully");
            } else {
                Log.w(TAG, "Failed to delete temp photo file");
            }
            tempPhotoFileTakePictore = null;
        }
    }

    /**
     * Save image user to device
     */
    private void saveImageToDevice(View v) {
        binding.saveButtonContainer.setVisibility(View.GONE);
        progressOverlay.setVisibility(View.VISIBLE);

        MediaUtils.saveMediaToInternalStorageAsync(
                getContext(),
                currentMediaUri,
                currentMediaType
        ).thenAccept(
                file -> {
                    if (file != null) {
                        Log.d(TAG, "Image saved to: " + file.getAbsolutePath());
                        this.avatarNewLive.postValue(file);
                        cleanupTempFiles(); // Xóa file tạm sau khi lưu thành công
                    } else {
                        progressOverlay.setVisibility(View.GONE);
                        requireActivity().runOnUiThread(() ->
                                showSnackbar("Lỗi khi lưu ảnh")
                        );
                    }
                }
        );
    }

    private void callServerUpdateProfile() {
        if (userDetailUpdateLiveData.getValue() == null) {
            progressOverlay.setVisibility(View.GONE);
            showSnackbar("Không có dữ liệu để cập nhật");
            return;
        }

        apiManager.updateUserInfo(
                sessionManager.getAccessToken(),
                new UserModels.UpdateUserInfoInput(
                        sessionManager.getUserId(),
                        userDetailUpdateLiveData.getValue().getName(),
                        userDetailUpdateLiveData.getValue().getUrl_avatar(), // Sử dụng URL mới từ Cloudinary
                        formatDate(userDetailUpdateLiveData.getValue().getBirthday()),
                        userDetailUpdateLiveData.getValue().getGender()
                ),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        if (response.body() == null) {
                            Log.e(TAG, "Error call update profile: Response body is null");
                            errorCallUpdateProfile.postValue("Lỗi cập nhật hồ sơ: Phản hồi từ máy chủ trống");
                            return;
                        }
                        int code = response.body().getCode();
                        if (code != Constants.CODE_SUCCESS) {
                            Log.e(TAG, "Error call update profile: " + response.body().getMessage());
                            errorCallUpdateProfile.postValue(response.body().getMessage());
                            return;
                        }
                        // save data to session
                        saveProfileToSession(userDetailUpdateLiveData.getValue());
                        errorCallUpdateProfile.postValue("");
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        Log.e(TAG, "Error call update profile: " + t.getMessage());
                        errorCallUpdateProfile.postValue("Lỗi kết nối: " + t.getMessage());
                    }
                }
        );
    }

    /**
     * save profile to session
     */
    private void saveProfileToSession(UserDetail userDetail) {
        if (userDetail == null) return;

        sessionManager.getUserProfile().setDisplayName(userDetail.getName());
        sessionManager.getUserProfile().setUserGender(userDetail.getGender());
        sessionManager.getUserProfile().setDateOfBirth(userDetail.getBirthday());
        sessionManager.setUserAvatar(userDetail.getUrl_avatar());

        // Kiểm tra null trước khi truy cập avatarNewLive
        if (avatarNewLive.getValue() != null) {
            sessionManager.setPathFileAvatarUser(avatarNewLive.getValue().getAbsolutePath());
        }
    }

    private void editAvatar() {
        // Hiển thị dialog lựa chọn
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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
     * Open camera to take a photo
     */
    private void takePhoto() {
        try {
            // Tạo thư mục tạm nếu chưa tồn tại
            File tempDir = new File(requireContext().getCacheDir(), "temp_photos");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // Tạo file tạm trong thư mục cache
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            tempPhotoFileTakePictore = new File(tempDir, "TEMP_" + timeStamp + ".jpg");

            currentMediaUri = FileProvider.getUriForFile(
                    requireContext(),
                    FILEPROVIDER_AUTHORITY,
                    tempPhotoFileTakePictore
            );

            Log.i(TAG, "Temporary Photo URI: " + currentMediaUri);
            takePhotoLauncher.launch(currentMediaUri);

        } catch (Exception ex) {
            Log.e(TAG, "Error creating image file", ex);
            showSnackbar("Lỗi khi tạo file ảnh: " + ex.getMessage());
        }
    }

    /**
     * Open gallery for media selection
     */
    private void openGallery() {
        pickPictureLauncher.launch(new String[]{"image/*"});
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    private void showEditProfileBottomSheet(UserDetail userDetail) {
        if (userDetail == null) {
            Log.e(TAG, "UserDetail is null when show edit profile");
            return;
        }
        // Tạo bottom sheet dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        // Inflate layout cho bottom sheet
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Ánh xạ các thành phần trong dialog
        EditText nameInput = bottomSheetView.findViewById(R.id.editName);
        EditText emailInput = bottomSheetView.findViewById(R.id.editEmail);
        emailInput.setEnabled(false);
        Button cancelButton = bottomSheetView.findViewById(R.id.btnCancel);
        Button saveButton = bottomSheetView.findViewById(R.id.btnSave);
        Spinner genderSpinner = bottomSheetView.findViewById(R.id.genderSpinner);
        EditText birthdayInput = bottomSheetView.findViewById(R.id.editBirthday);

        // Thiết lập dữ liệu ban đầu từ session hoặc user model
        nameInput.setText(userDetail.getName());
        emailInput.setText(userDetail.getEmail());
        birthdayInput.setText(userDetail.getBirthday());

        // Cài đặt gender spinner
        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                genderOptions
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Set giá trị spinner từ session (nếu có)
        String savedGender = userDetail.getGender();
        if (savedGender != null) {
            int spinnerPosition = genderAdapter.getPosition(savedGender);
            genderSpinner.setSelection(spinnerPosition);
        }

        // Xử lý chọn ngày sinh bằng DatePickerDialog
        birthdayInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            // Lấy ngày sinh hiện tại từ userDetail (nếu có)
            String currentBirthday = userDetail.getBirthday();
            if (currentBirthday != null && !currentBirthday.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date date = dateFormat.parse(currentBirthday);
                    if (date != null) {
                        calendar.setTime(date);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date", e);
                }
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year1, month1, dayOfMonth) -> {
                        String birthday = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        birthdayInput.setText(birthday);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Xử lý nút Cancel
        cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Xử lý nút Save
        saveButton.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String newEmail = emailInput.getText().toString().trim();
            String newGender = genderSpinner.getSelectedItem().toString();
            String newBirthday = birthdayInput.getText().toString().trim();

            if (!validateData(nameInput, emailInput, genderSpinner, birthdayInput)) {
                Log.d(TAG, "validateData error");
                return;
            }

            userDetailUpdateLiveData.setValue(new UserDetail(
                    newName,
                    newEmail,
                    newGender,
                    newBirthday,
                    userDetail.getUrl_avatar() // Giữ nguyên URL avatar
            ));

            bottomSheetDialog.dismiss();
        });

        // Hiển thị dialog
        bottomSheetDialog.show();
        bottomSheetDialog.setDismissWithAnimation(true);

        // Mở rộng dialog khi hiển thị
        BottomSheetBehavior<FrameLayout> behavior = bottomSheetDialog.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * Validate data save
     */
    private boolean validateData(
            EditText name,
            EditText email,
            Spinner gender,
            EditText birthday) {
        String newName = name.getText().toString().trim();
        String newEmail = email.getText().toString().trim();
        String newGender = gender.getSelectedItem().toString();
        String newBirthday = birthday.getText().toString().trim();

        // Validate name
        if (newName.isEmpty()) {
            name.setError("Name is required");
            name.requestFocus();
            return false;
        }

        // Validate email
        if (newEmail.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return false;
        }

        // Validate gender (assuming default option is "Select gender")
        if (newGender.equalsIgnoreCase("Select gender")) {
            Toast.makeText(gender.getContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
            gender.requestFocus();
            return false;
        }

        // Validate birthday
        if (newBirthday.isEmpty()) {
            birthday.setError("Birthday is required");
            birthday.requestFocus();
            return false;
        }
        try {
            Date d = new SimpleDateFormat("dd/MM/yyyy").parse(newBirthday);
            if (d.after(new Date())) {
                birthday.setError("Invalid date (must be in the past)");
                birthday.requestFocus();
                return false;
            }
        } catch (ParseException e) {
            birthday.setError("Invalid date format (dd/MM/yyyy)");
            birthday.requestFocus();
            return false;
        }

        return true;
    }

    // Convert date to string -> dd-MM-yyyy HH:mm:ss
    private String formatDate(String date) {
        try {
            Date d = new SimpleDateFormat("dd/MM/yyyy").parse(date);
            return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(d);
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting date", e);
            return "";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanupTempFiles();
        binding = null;
    }
}