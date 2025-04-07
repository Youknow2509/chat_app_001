package com.example.chatapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapters.FriendInvitationAdapter;
import com.example.chatapp.models.FriendInvitation;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class FriendInvitationActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView recyclerViewInvitations;
    private TextView tvNoInvitations;
    private ProgressBar loadingIndicator;
    private Button btnAcceptAll, btnRejectAll;
    private FriendInvitationAdapter adapter;

    private List<FriendInvitation> receivedInvitations = new ArrayList<>();
    private List<FriendInvitation> sentInvitations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_list);

        // Khởi tạo các view
        initViews();

        // Thiết lập adapter cho RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện cho TabLayout
        setupTabLayout();

        // Thiết lập sự kiện cho các nút
        setupButtons();

        // Tải dữ liệu ban đầu (lời mời đã nhận)
        loadReceivedInvitations();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        recyclerViewInvitations = findViewById(R.id.recyclerViewInvitations);
        tvNoInvitations = findViewById(R.id.tvNoInvitations);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        btnAcceptAll = findViewById(R.id.btnAcceptAll);
        btnRejectAll = findViewById(R.id.btnRejectAll);

        // Thiết lập nút quay lại
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerViewInvitations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendInvitationAdapter(this, receivedInvitations, true);
        recyclerViewInvitations.setAdapter(adapter);
    }

    private void setupTabLayout() {
        // Thêm listener cho TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    // Tab "Đã nhận"
                    loadReceivedInvitations();
                    btnAcceptAll.setVisibility(View.VISIBLE);
                    btnRejectAll.setVisibility(View.VISIBLE);
                } else {
                    // Tab "Đã gửi"
                    loadSentInvitations();
                    btnAcceptAll.setVisibility(View.GONE);
                    btnRejectAll.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }
        });
    }

    private void setupButtons() {
        btnAcceptAll.setOnClickListener(v -> acceptAllInvitations());
        btnRejectAll.setOnClickListener(v -> rejectAllInvitations());
    }

    private void loadReceivedInvitations() {
        showLoading(true);

        // Mô phỏng việc tải dữ liệu từ server
        // Trong thực tế, bạn sẽ gọi API để lấy danh sách lời mời đã nhận
        receivedInvitations.clear();

        // Thêm dữ liệu mẫu
        receivedInvitations.add(new FriendInvitation("1", "Nguyễn Văn A", "nguyenvana@example.com", "2 giờ trước", FriendInvitation.STATUS_PENDING));
        receivedInvitations.add(new FriendInvitation("2", "Trần Thị B", "tranthib@example.com", "1 ngày trước", FriendInvitation.STATUS_PENDING));

        // Cập nhật adapter với dữ liệu mới
        adapter.updateData(receivedInvitations, true);

        // Hiển thị thông báo nếu không có lời mời
        if (receivedInvitations.isEmpty()) {
            tvNoInvitations.setVisibility(View.VISIBLE);
        } else {
            tvNoInvitations.setVisibility(View.GONE);
        }

        showLoading(false);
    }

    private void loadSentInvitations() {
        showLoading(true);

        // Mô phỏng việc tải dữ liệu từ server
        // Trong thực tế, bạn sẽ gọi API để lấy danh sách lời mời đã gửi
        sentInvitations.clear();

        // Thêm dữ liệu mẫu
        sentInvitations.add(new FriendInvitation("3", "Lê Văn C", "levanc@example.com", "3 giờ trước", FriendInvitation.STATUS_PENDING));
        sentInvitations.add(new FriendInvitation("4", "Phạm Thị D", "phamthid@example.com", "5 ngày trước", FriendInvitation.STATUS_ACCEPTED));
        sentInvitations.add(new FriendInvitation("5", "Hoàng Văn E", "hoangvane@example.com", "1 tuần trước", FriendInvitation.STATUS_REJECTED));

        // Cập nhật adapter với dữ liệu mới
        adapter.updateData(sentInvitations, false);

        // Hiển thị thông báo nếu không có lời mời
        if (sentInvitations.isEmpty()) {
            tvNoInvitations.setVisibility(View.VISIBLE);
        } else {
            tvNoInvitations.setVisibility(View.GONE);
        }

        showLoading(false);
    }

    private void acceptAllInvitations() {
        // Xử lý chấp nhận tất cả lời mời
        for (FriendInvitation invitation : receivedInvitations) {
            invitation.setStatus(FriendInvitation.STATUS_ACCEPTED);
        }
        adapter.notifyDataSetChanged();

        // Trong thực tế, bạn sẽ gọi API để chấp nhận tất cả lời mời
    }

    private void rejectAllInvitations() {
        // Xử lý từ chối tất cả lời mời
        for (FriendInvitation invitation : receivedInvitations) {
            invitation.setStatus(FriendInvitation.STATUS_REJECTED);
        }
        adapter.notifyDataSetChanged();

        // Trong thực tế, bạn sẽ gọi API để từ chối tất cả lời mời
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingIndicator.setVisibility(View.VISIBLE);
            recyclerViewInvitations.setVisibility(View.GONE);
            tvNoInvitations.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
            recyclerViewInvitations.setVisibility(View.VISIBLE);
        }
    }
}
