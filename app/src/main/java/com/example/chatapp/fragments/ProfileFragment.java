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
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.activities.AddFriendActivity;
import com.example.chatapp.activities.CreateNewGroupActivity;
import com.example.chatapp.databinding.FragmentProfileBinding;
import com.example.chatapp.models.UserDetail;
import com.example.chatapp.service.ImageService;
import com.example.chatapp.viewmodel.ProfileViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends BaseNetworkFragment {
    private static final String TAG = "ProfileFragment";
    private static final String FILEPROVIDER_AUTHORITY = "com.example.chatapp.fileprovider";
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    // ViewModel
    private ProfileViewModel viewModel;

    // UI Binding
    private FragmentProfileBinding binding;

    // Media handling
    private String currentMediaType = "image";
    private Uri currentMediaUri;
    private File tempPhotoFile;

    // Activity result launchers
    private final ActivityResultLauncher<String[]> pickPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    requireActivity().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    currentMediaType = "image";
                    currentMediaUri = uri;
                    showImagePreviewDialog(uri);
                }
            });

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setListeners();
        observeViewModel();
    }

    @Override
    protected void onNetworkUnavailable() {
        super.onNetworkUnavailable();
        if (binding != null) {
            binding.editAvatar.setVisibility(View.GONE);
            binding.rightButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onNetworkAvailable() {
        super.onNetworkAvailable();
        if (binding != null) {
            binding.editAvatar.setVisibility(View.VISIBLE);
            binding.rightButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up event listeners
     */
    private void setListeners() {
        // Edit avatar button
        binding.editAvatar.setOnClickListener(v -> editAvatar());

        // Edit profile button
        binding.addButton.setOnClickListener(v -> {
            UserDetail userDetail = viewModel.getUserDetail().getValue();
            if (userDetail != null) {
                showEditProfileBottomSheet(userDetail);
            } else {
                showSnackbar("Không thể tải thông tin người dùng");
            }
        });

        // Add menu button
        binding.addIcon.setOnClickListener(v -> showAddMenu());

        // Save image button
        binding.rightButton.setOnClickListener(this::saveImageToDeviceAndUpload);

        // Cancel image button
        binding.leftButton.setOnClickListener(this::cancelImageChanges);
    }

    /**
     * Observe ViewModel LiveData
     */
    private void observeViewModel() {
        // Observe user details
        viewModel.getUserDetail().observe(getViewLifecycleOwner(), this::updateUI);

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                binding.progressOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showSnackbar(error);
            }
        });
    }

    /**
     * Update UI with user details
     */
    private void updateUI(UserDetail userDetail) {
        if (userDetail == null || binding == null) return;

        binding.profileName.setText(userDetail.getName());
        binding.emailDetail.setText(userDetail.getEmail());
        binding.genderDetail.setText(userDetail.getGender());
        binding.birthdayDetail.setText(userDetail.getBirthday());

        ImageService.loadAndCacheImage(
                this,
                binding.avatarImage,
                viewModel.getUserAvatarPath(),
                viewModel.getUserAvatarUrl(),
                viewModel.getAccessToken(),
                newPath -> {
                    // Update path in UserDetail
                    userDetail.setPath_local_avatar(newPath);
                }
        );
    }

    /**
     * Show add menu options
     */
    private void showAddMenu() {
        PopupMenu popupMenu = new PopupMenu(getContext(), binding.addIcon);
        popupMenu.getMenuInflater().inflate(R.menu.option_add_menu, popupMenu.getMenu());

        // Rotate icon
        binding.addIcon.animate().rotation(45).setDuration(200);

        // Handle menu dismissal
        popupMenu.setOnDismissListener(menu ->
                binding.addIcon.animate().rotation(0).setDuration(200)
        );

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_add_friend) {
                startActivity(new Intent(getContext(), AddFriendActivity.class));
                return true;
            } else if (id == R.id.action_create_group) {
                Intent intent = new Intent(getContext(), CreateNewGroupActivity.class);
                intent.putExtra("CREATE_GROUP", true);
                startActivity(intent);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    /**
     * Show image preview
     */
    private void showImagePreviewDialog(Uri imageUri) {
        if (binding != null) {
            binding.saveButtonContainer.setVisibility(View.VISIBLE);

            Glide.with(requireContext())
                    .load(imageUri)
                    .into(binding.avatarImage);
        }
    }

    /**
     * Handle saving image
     */
    private void saveImageToDeviceAndUpload(View v) {
        if (binding != null) {
            binding.saveButtonContainer.setVisibility(View.GONE);
            viewModel.saveMediaToInternalStorageAndUpload(currentMediaUri, currentMediaType);
        }
    }

    /**
     * Handle canceling image changes
     */
    private void cancelImageChanges(View v) {
        currentMediaUri = null;

        if (binding != null) {
            binding.saveButtonContainer.setVisibility(View.GONE);

            // Reload original avatar
            UserDetail userDetail = viewModel.getUserDetail().getValue();
            if (userDetail != null && userDetail.getPath_local_avatar() != null) {
                binding.avatarImage.setImageURI(Uri.fromFile(new File(userDetail.getPath_local_avatar())));
            }
        }

        cleanupTempFiles();
        Toast.makeText(getContext(), "Đã hủy thay đổi ảnh đại diện", Toast.LENGTH_SHORT).show();
    }

    /**
     * Clean up temporary files
     */
    private void cleanupTempFiles() {
        if (tempPhotoFile != null && tempPhotoFile.exists()) {
            boolean deleted = tempPhotoFile.delete();
            if (deleted) {
                Log.d(TAG, "Temp photo file deleted successfully");
            } else {
                Log.w(TAG, "Failed to delete temp photo file");
            }
            tempPhotoFile = null;
        }
    }

    /**
     * Handle editing avatar
     */
    private void editAvatar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn ảnh đại diện");
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Take photo
                    if (checkCameraPermission()) {
                        takePhoto();
                    }
                    break;
                case 1: // Choose from gallery
                    openGallery();
                    break;
            }
        });

        builder.show();
    }

    /**
     * Take photo with camera
     */
    private void takePhoto() {
        try {
            // Create temp directory if it doesn't exist
            File tempDir = new File(requireContext().getCacheDir(), "temp_photos");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // Create temporary file
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            tempPhotoFile = new File(tempDir, "TEMP_" + timeStamp + ".jpg");

            currentMediaUri = FileProvider.getUriForFile(
                    requireContext(),
                    FILEPROVIDER_AUTHORITY,
                    tempPhotoFile
            );

            Log.i(TAG, "Temporary Photo URI: " + currentMediaUri);
            takePhotoLauncher.launch(currentMediaUri);

        } catch (Exception ex) {
            Log.e(TAG, "Error creating image file", ex);
            showSnackbar("Lỗi khi tạo file ảnh: " + ex.getMessage());
        }
    }

    /**
     * Open gallery for image selection
     */
    private void openGallery() {
        pickPictureLauncher.launch(new String[]{"image/*"});
    }

    /**
     * Check camera permission
     */
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    /**
     * Show edit profile bottom sheet
     */
    private void showEditProfileBottomSheet(UserDetail userDetail) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Get references to views
        EditText nameInput = bottomSheetView.findViewById(R.id.editName);
        EditText emailInput = bottomSheetView.findViewById(R.id.editEmail);
        emailInput.setEnabled(false);
        Button cancelButton = bottomSheetView.findViewById(R.id.btnCancel);
        Button saveButton = bottomSheetView.findViewById(R.id.btnSave);
        Spinner genderSpinner = bottomSheetView.findViewById(R.id.genderSpinner);
        EditText birthdayInput = bottomSheetView.findViewById(R.id.editBirthday);

        // Set initial values
        nameInput.setText(userDetail.getName());
        emailInput.setText(userDetail.getEmail());
        birthdayInput.setText(userDetail.getBirthday());

        // Setup gender spinner
        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                genderOptions
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Set gender selection
        String savedGender = userDetail.getGender();
        if (savedGender != null) {
            int spinnerPosition = genderAdapter.getPosition(savedGender);
            genderSpinner.setSelection(spinnerPosition);
        }

        // Handle birthday selection
        birthdayInput.setOnClickListener(v -> showDatePicker(birthdayInput, userDetail.getBirthday()));

        // Handle cancel button
        cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Handle save button
        saveButton.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String newEmail = emailInput.getText().toString().trim();
            String newGender = genderSpinner.getSelectedItem().toString();
            String newBirthday = birthdayInput.getText().toString().trim();

            // Validate data
            String validationError = viewModel.validateUserData(newName, newEmail, newGender, newBirthday);
            if (validationError != null) {
                showValidationError(validationError, nameInput, emailInput, birthdayInput, genderSpinner);
                return;
            }

            // Update user info in ViewModel
            viewModel.updateUserInfo(newName, newGender, newBirthday);
            bottomSheetDialog.dismiss();
        });

        // Show bottom sheet
        bottomSheetDialog.show();
        bottomSheetDialog.setDismissWithAnimation(true);

        // Expand bottom sheet
        BottomSheetBehavior<FrameLayout> behavior = bottomSheetDialog.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker(EditText birthdayInput, String currentBirthday) {
        Calendar calendar = Calendar.getInstance();

        // Parse current birthday if available
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
    }

    /**
     * Show validation error
     */
    private void showValidationError(String error, EditText nameInput, EditText emailInput,
                                     EditText birthdayInput, Spinner genderSpinner) {
        if (error.contains("Name")) {
            nameInput.setError(error);
            nameInput.requestFocus();
        } else if (error.contains("Email")) {
            emailInput.setError(error);
            emailInput.requestFocus();
        } else if (error.contains("gender")) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            genderSpinner.requestFocus();
        } else if (error.contains("Birthday") || error.contains("date")) {
            birthdayInput.setError(error);
            birthdayInput.requestFocus();
        } else {
            showSnackbar(error);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanupTempFiles();
        binding = null;
    }
}