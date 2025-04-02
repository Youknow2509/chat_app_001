package com.example.chatapp.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.example.chatapp.R;
import com.example.chatapp.activities.AddFriendActivity;
import com.example.chatapp.activities.CreateNewGroupActivity;
import com.example.chatapp.activities.OnboardingActivity;
import com.example.chatapp.databinding.FragmentProfileBinding;
import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.utils.session.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private UserProfileSession userProfileSession;

    private void initVariable() {
        this.sessionManager = new SessionManager(this.getContext());
        this.userProfileSession = this.sessionManager.getUserProfile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        initVariable();
        initDataToView();
        setListener();
        return view;
    }

    /**
     * Init data in view
     */
    private void initDataToView() {
        binding.profileName.setText(sessionManager.getDisplayName());
//        binding.p
        // TODO
    }

    /**
     * listen event in element
     */
    private void setListener() {
        // Thiết lập sự kiện click cho nút Edit Profile
        binding.editAvatar.setOnClickListener(v -> editAvatar());
        binding.addButton.setOnClickListener(v->showEditProfileBottomSheet());
        binding.signoutButton.setOnClickListener(v->signOut());
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

    private void signOut(){
        this.sessionManager.logout();

        // redirect to OnboardingActivity
        startActivity(new Intent(this.getContext(), OnboardingActivity.class));
        this.getActivity().finish();
    }

    private void editAvatar() {
        // TODO: Thay đổi avatar
    }

    private void showEditProfileBottomSheet() {
        // Tạo bottom sheet dialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this.getContext());

        // Inflate layout cho bottom sheet
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Ánh xạ các thành phần trong dialog
        EditText nameInput = bottomSheetView.findViewById(R.id.editName);
        EditText emailInput = bottomSheetView.findViewById(R.id.editEmail);
        Button cancelButton = bottomSheetView.findViewById(R.id.btnCancel);
        Button saveButton = bottomSheetView.findViewById(R.id.btnSave);

        // Thiết lập dữ liệu ban đầu
        nameInput.setText("John Lennon");
        emailInput.setText("john.lennon@mail.com");

        // Xử lý sự kiện nút Cancel
        cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Xử lý sự kiện nút Save
        saveButton.setOnClickListener(v -> {
            // Xử lý lưu thông tin
            bottomSheetDialog.dismiss();
        });

        // Hiển thị dialog
        bottomSheetDialog.show();

        // Thiết lập để dialog có thể vuốt xuống để đóng
        bottomSheetDialog.setDismissWithAnimation(true);

        // Lấy behavior để tùy chỉnh thêm nếu cần
        BottomSheetBehavior<FrameLayout> behavior = bottomSheetDialog.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }


}