package com.example.chatapp.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.example.chatapp.utils.session.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    //
    private FragmentProfileBinding binding;
    //
    private SessionManager sessionManager;
    private UserProfileSession userProfileSession;
    private ApiManager apiManager;
    private final String TAG = "ProfileFragment";
    //
    private MutableLiveData<UserDetail> userDetailLiveData;
    private MutableLiveData<UserDetail> userDetailUpdateLiveData;
    private MutableLiveData<String> errorCallUpdateProfile;
    //
    private FrameLayout progressOverlay;
    private String currentMediaType = "image"; // Default to image
    private static final String FILEPROVIDER_AUTHORITY = "com.example.chatapp.fileprovider";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Uri currentMediaUri;

    private final ActivityResultLauncher<String[]> pickMediaLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    // Persist permission for this URI
                    requireActivity().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

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
        binding.saveButtonContainer.setVisibility(View.VISIBLE);
        // Ánh xạ các thành phần trong dialog
        ImageView imagePreview = binding.avatarImage;
        Button btnSave = binding.rightButton;
        Button btnCancel = binding.leftButton;

        // Hiển thị ảnh xem trước
        Glide.with(getContext())
                .load(imageUri)
                .into(imagePreview);

        // Xử lý sự kiện nút Lưu
        btnSave.setOnClickListener(v -> {
            binding.saveButtonContainer.setVisibility(View.GONE);

        });
        btnCancel.setOnClickListener(v -> {
            // Xóa ảnh đã chọn
            currentMediaUri = null;
            binding.saveButtonContainer.setVisibility(View.GONE);
            binding.avatarImage.setImageURI(
                    Uri.fromFile(new File(
                            userDetailLiveData.getValue().getPath_local_avatar()
                    )));

            // Thông báo đã hủy
            Toast.makeText(getContext(), "Đã hủy thay đổi ảnh đại diện", Toast.LENGTH_SHORT).show();
        });
    }


    private void initVariable() {
        this.apiManager = new ApiManager(this.getContext());
        //
        this.sessionManager = new SessionManager(this.getContext());
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
        //
        this.progressOverlay = binding.progressOverlay;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        initVariable();
//        initDataToView(userDetailLiveData.getValue());
        setListener();
        observeLiveData();
        return view;
    }

    /**
     * observeLiveData
     */
    private void observeLiveData() {
        //
        userDetailLiveData.observe(getViewLifecycleOwner(), userDetail -> {
            if (userDetail != null) {
                initDataToView(userDetail);
            } else {
                Log.e(TAG, "Received null userDetail");
            }
        });
        //
        userDetailUpdateLiveData.observe(getViewLifecycleOwner(), userDetail -> {
            // update data to server
            progressOverlay.setVisibility(View.VISIBLE);
            callServerUpdateProfile();
        });
        //
        errorCallUpdateProfile.observe(getViewLifecycleOwner(), error -> {
            progressOverlay.setVisibility(View.GONE);
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT).show();
                return;
            }
            userDetailLiveData.setValue(userDetailUpdateLiveData.getValue());
        });
    }

    /**
     * Init data in view
     */
    private void initDataToView(UserDetail userDetail) {
        if (userDetail == null) {
            Log.e(TAG, "UserDetail is null");
            return;
        }
        binding.profileName.setText(userDetail.getName());
        binding.emailDetail.setText(userDetail.getEmail());
        binding.genderDetail.setText(userDetail.getGender());
        binding.birthdayDetail.setText(userDetail.getBirthday());

        String url_avatar_local = userDetail.getPath_local_avatar();
        File avatar_file = new File(url_avatar_local);
        if (avatar_file.exists()) {
            Log.d(TAG, "Load avatar from local: " + url_avatar_local + "success");
            binding.avatarImage.setImageURI(Uri.fromFile(avatar_file));
        } else {
            Log.d(TAG, "Load avatar from local: " + url_avatar_local + "error");
        }
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
                // Bạn có thể hiển thị một thông báo cho người dùng hoặc làm gì đó khác.
            }
        });
//        binding.signoutButton.setOnClickListener(v -> signOut());
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
    }

    private void callServerUpdateProfile() {
        apiManager.updateUserInfo(
                sessionManager.getAccessToken(),
                new UserModels.UpdateUserInfoInput(
                        sessionManager.getUserId(),
                        userDetailUpdateLiveData.getValue().getName(),
                        sessionManager.getUserAvatar(),
                        formatDate(userDetailUpdateLiveData.getValue().getBirthday()),
                        userDetailUpdateLiveData.getValue().getGender()
                ),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        if (response.body() == null) {
                            Log.e(TAG, "Error call update profile: Response body is null");
                            errorCallUpdateProfile.postValue("Error call update profile: Response body is null");
                            return;
                        }
                        int code = response.body().getCode();
                        if (code != Constants.CODE_SUCCESS) {
                            Log.e(TAG, "Error call update profile: " + response.body().getMessage());
                            errorCallUpdateProfile.postValue(response.body().getMessage());
                            return;
                        }
                        errorCallUpdateProfile.postValue("");
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        Log.e(TAG, "Error call update profile: " + t.getMessage());
                        errorCallUpdateProfile.postValue(t.getMessage());
                    }
                }
        );
    }
    private void editAvatar() {
        // Hiển thị dialog lựa chọn
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
     * Create a temporary file for media capture
     */
    private File createMediaFile(String extension) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "MEDIA_" + timeStamp + "_";
        File storageDir = getContext().getFilesDir();
        return File.createTempFile(fileName, extension, storageDir);
    }

    /**
     * Open camera to take a photo
     */
    private void takePhoto() {
        try {
            File photoFile = createMediaFile(".jpg");

            currentMediaUri = FileProvider.getUriForFile(this.getContext(),
                    FILEPROVIDER_AUTHORITY,
                    photoFile);
            Log.i(TAG, "Photo URI: " + currentMediaUri.toString());
            takePhotoLauncher.launch(currentMediaUri);

        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file", ex);
            Toast.makeText(this.getContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Open gallery for media selection
     */
    private void openGallery() {
        pickMediaLauncher.launch(new String[]{"image/*", "video/*"});
    }


    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
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
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this.getContext());

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
                this.getContext(),
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
            String currentBirthday = userDetail.getBirthday(); // Giả sử format là "dd-MM-yyyy"
            if (currentBirthday != null && !currentBirthday.isEmpty()) {
                try {
                    // Chuyển đổi từ chuỗi ngày sinh sang Calendar để lấy năm, tháng, ngày
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    Date date = dateFormat.parse(currentBirthday);
                    if (date != null) {
                        calendar.setTime(date);
                    }
                } catch (ParseException e) {
                    e.printStackTrace(); // Xử lý lỗi nếu format không đúng
                }
            }

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this.getContext(),
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
                    sessionManager.getPathFileAvatarUser()
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
            // TODO: enable in prodution
//        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
//            email.setError("Invalid email format");
//            email.requestFocus();
//            return false;
        }

        // Validate gender (assuming default option is "Select gender")
        if (newGender.equalsIgnoreCase("Select gender")) {
            Toast.makeText(gender.getContext(), "Please select a gender", Toast.LENGTH_SHORT).show();
            gender.requestFocus();
            return false;
        }

        // Validate birthday (you can improve this based on date format)
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
            e.printStackTrace();
            return "";
        }
    }

}