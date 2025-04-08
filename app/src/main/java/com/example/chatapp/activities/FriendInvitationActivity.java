package com.example.chatapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapters.FriendInvitationAdapter;
import com.example.chatapp.api.ApiManager;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.FriendInvitation;
import com.example.chatapp.models.response.FriendItemRequest;
import com.example.chatapp.models.response.FriendItemRequestSend;
import com.example.chatapp.models.response.ResponseData;
import com.example.chatapp.utils.session.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendInvitationActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView recyclerViewInvitations;
    private TextView tvNoInvitations;
    private ProgressBar loadingIndicator;
    private Button btnAcceptAll, btnRejectAll;
    private FriendInvitationAdapter adapter;

    private final String TAG = "FriendInvitationActivity";
    private ApiManager apiManager;
    private SessionManager sessionManager;
    private List<FriendInvitation> receivedInvitations = new ArrayList<>();
    private List<FriendInvitation> sentInvitations = new ArrayList<>();
    private MutableLiveData<List<FriendItemRequest>> listFriendRequestLive = new MutableLiveData<>();
    private MutableLiveData<List<FriendItemRequestSend>> listFriendRequestSendLive = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_list);

        apiManager = new ApiManager(this);
        sessionManager = SessionManager.getInstance();

        // Initialize views
        initViews();

        // Setup adapter for RecyclerView
        setupRecyclerView();

        // Setup TabLayout events
        setupTabLayout();

        // Setup LiveData observers
        setupObservers();

        // Fetch data from server
        getDataFromServer();

        // Load initial data (received invitations)
        loadReceivedInvitations();
    }

    /**
     * Setup LiveData observers
     */
    private void setupObservers() {
        // Observer for received friend requests
        listFriendRequestLive.observe(this, friendRequests -> {
            if (friendRequests != null) {
                // Convert FriendItemRequestSend to FriendInvitation
                receivedInvitations.clear();
                for (FriendItemRequest request : friendRequests) {
                    receivedInvitations.add(
                            new FriendInvitation(
                                    request.getRequest_id(),
                                    request.getFrom_user(),
                                    request.getUser_nickname(),
                                    request.getCreated_at(),
                                    request.getUser_avatar(),
                                    FriendInvitation.STATUS_PENDING)
                    );
                }

                // Update UI if currently on the Received tab
                if (tabLayout.getSelectedTabPosition() == 0) {
                    adapter.updateData(receivedInvitations, true);
                    updateEmptyStateVisibility(receivedInvitations);
                }
            }
        });

        // Observer for sent friend requests
        listFriendRequestSendLive.observe(this, friendRequests -> {
            if (friendRequests != null) {
                // Convert FriendItemRequestSend to FriendInvitation
                sentInvitations.clear();
                for (FriendItemRequestSend request : friendRequests) {
                    // Determine status based on request status
                    int status = FriendInvitation.STATUS_PENDING;
                    if (request.getStatus_request() != null) {
                        if (request.getStatus_request().equals("accepted")) {
                            status = FriendInvitation.STATUS_ACCEPTED;
                        } else if (request.getStatus_request().equals("rejected")) {
                            status = FriendInvitation.STATUS_REJECTED;
                        }
                    }

                    sentInvitations.add(
                            new FriendInvitation(
                                    request.getRequest_id(),
                                    request.getTo_user(),
                                    request.getUser_nickname(),
                                    request.getCreated_at(),
                                    request.getUser_avatar(),
                                    status)
                    );
                }

                // Update UI if currently on the Sent tab
                if (tabLayout.getSelectedTabPosition() == 1) {
                    adapter.updateData(sentInvitations, false);
                    updateEmptyStateVisibility(sentInvitations);
                }
            }
        });
    }

    /**
     * Update empty state message visibility
     */
    private void updateEmptyStateVisibility(List<FriendInvitation> list) {
        if (list.isEmpty()) {
            tvNoInvitations.setVisibility(View.VISIBLE);
            tvNoInvitations.setText(tabLayout.getSelectedTabPosition() == 0 ?
                    "Không có lời mời kết bạn nào" :
                    "Bạn chưa gửi lời mời kết bạn nào");
        } else {
            tvNoInvitations.setVisibility(View.GONE);
        }
    }

    /**
     * Fetch data from server
     */
    private void getDataFromServer() {
        showLoading(true);

        // Get received friend requests
        fetchReceivedFriendRequests();

        // Get sent friend requests
        fetchSentFriendRequests();
    }

    /**
     * Fetch received friend requests
     */
    private void fetchReceivedFriendRequests() {
        apiManager.getListFriendRequest(
                sessionManager.getAccessToken(),
                20,
                1,
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                ResponseData<Object> responseData = response.body();
                                if (responseData.getCode() == Constants.CODE_SUCCESS && responseData.getData() != null) {
                                    // Convert the data object to the expected type
                                    Gson gson = new Gson();
                                    String json = gson.toJson(responseData.getData());
                                    Type type = new TypeToken<List<FriendItemRequest>>(){}.getType();
                                    List<FriendItemRequest> requests = gson.fromJson(json, type);

                                    // Update LiveData
                                    listFriendRequestLive.setValue(requests);
                                } else {
                                    // Handle API error
                                    handleApiError(responseData.getMessage());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing response: " + e.getMessage());
                                handleApiError("Lỗi xử lý dữ liệu");
                            }
                        } else {
                            handleApiError("Lỗi kết nối máy chủ");
                        }
                        showLoading(false);
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        handleApiError("Lỗi lấy yêu cầu kết bạn");
                        showLoading(false);
                    }
                }
        );
    }

    /**
     * Fetch sent friend requests
     */
    private void fetchSentFriendRequests() {
        apiManager.getListFriendRequestSend(
                sessionManager.getAccessToken(),
                20,
                1,
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                ResponseData<Object> responseData = response.body();
                                if (responseData.getCode() == Constants.CODE_SUCCESS && responseData.getData() != null) {
                                    // Convert the data object to the expected type
                                    Gson gson = new Gson();
                                    String json = gson.toJson(responseData.getData());
                                    Type type = new TypeToken<List<FriendItemRequestSend>>(){}.getType();
                                    List<FriendItemRequestSend> requests = gson.fromJson(json, type);

                                    // Update LiveData
                                    listFriendRequestSendLive.setValue(requests);
                                } else {
                                    // Handle API error
                                    handleApiError(responseData.getMessage());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing response: " + e.getMessage());
                                handleApiError("Lỗi xử lý dữ liệu");
                            }
                        } else {
                            handleApiError("Lỗi kết nối máy chủ");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        handleApiError("Lỗi lấy danh sách kết bạn đã gửi");
                    }
                }
        );
    }

    /**
     * Handle API error
     */
    private void handleApiError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        recyclerViewInvitations = findViewById(R.id.recyclerViewInvitations);
        tvNoInvitations = findViewById(R.id.tvNoInvitations);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        btnAcceptAll = findViewById(R.id.btnAcceptAll);
        btnRejectAll = findViewById(R.id.btnRejectAll);

        // Setup back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerViewInvitations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendInvitationAdapter(this, receivedInvitations, true);
        recyclerViewInvitations.setAdapter(adapter);

        // Setup item click listeners
        adapter.setOnAcceptClickListener(position -> acceptInvitation(position));
        adapter.setOnRejectClickListener(position -> rejectInvitation(position));
        adapter.setOnCancelClickListener(position -> cancelInvitation(position));
    }

    private void setupTabLayout() {
        // Add listener for TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    // "Received" tab
                    loadReceivedInvitations();
                    btnAcceptAll.setVisibility(View.VISIBLE);
                    btnRejectAll.setVisibility(View.VISIBLE);
                } else {
                    // "Sent" tab
                    loadSentInvitations();
                    btnAcceptAll.setVisibility(View.GONE);
                    btnRejectAll.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void loadReceivedInvitations() {
        showLoading(true);

        // Use data from LiveData if available
        List<FriendItemRequest> requests = listFriendRequestLive.getValue();
        if (requests != null && !requests.isEmpty()) {
            // Use the data from LiveData (already processed in observer)
            adapter.updateData(receivedInvitations, true);
            updateEmptyStateVisibility(receivedInvitations);
        } else {
            // If no data in LiveData, show empty state message
            updateEmptyStateVisibility(receivedInvitations);
        }

        showLoading(false);
    }

    private void loadSentInvitations() {
        showLoading(true);

        // Use data from LiveData if available
        List<FriendItemRequestSend> requests = listFriendRequestSendLive.getValue();
        if (requests != null && !requests.isEmpty()) {
            // Use the data from LiveData (already processed in observer)
            adapter.updateData(sentInvitations, false);
            updateEmptyStateVisibility(sentInvitations);
        } else {
            // If no data in LiveData, show empty state message
            updateEmptyStateVisibility(sentInvitations);
        }

        showLoading(false);
    }

    private void acceptInvitation(int position) {
        showLoading(true);
        FriendInvitation invitation = receivedInvitations.get(position);

        apiManager.acceptFriendRequest(
                sessionManager.getAccessToken(),
                invitation.getRequest_id(),
                sessionManager.getUserId(),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ResponseData<Object> responseData = response.body();
                            if (responseData.getCode() == Constants.CODE_SUCCESS) {
                                Toast.makeText(FriendInvitationActivity.this,
                                        "Đã chấp nhận lời mời kết bạn", Toast.LENGTH_SHORT).show();

                                // Update local data
                                invitation.setStatus(FriendInvitation.STATUS_ACCEPTED);
                                adapter.notifyItemChanged(position);

                                // Refresh data from server after a delay
                                recyclerViewInvitations.postDelayed(() -> getDataFromServer(), 1000);
                            } else {
                                handleApiError(responseData.getMessage());
                            }
                        } else {
                            handleApiError("Lỗi kết nối máy chủ");
                        }
                        showLoading(false);
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        handleApiError("Lỗi kết nối");
                        showLoading(false);
                    }
                }
        );
    }

    private void rejectInvitation(int position) {
        showLoading(true);
        FriendInvitation invitation = receivedInvitations.get(position);

        apiManager.rejectFriendRequest(
                sessionManager.getAccessToken(),
                invitation.getRequest_id(),
                sessionManager.getUserId(),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ResponseData<Object> responseData = response.body();
                            if (responseData.getCode() == Constants.CODE_SUCCESS) {
                                Toast.makeText(FriendInvitationActivity.this,
                                        "Đã từ chối lời mời kết bạn", Toast.LENGTH_SHORT).show();

                                // Update local data
                                invitation.setStatus(FriendInvitation.STATUS_REJECTED);
                                adapter.notifyItemChanged(position);

                                // Refresh data from server after a delay
                                recyclerViewInvitations.postDelayed(() -> getDataFromServer(), 1000);
                            } else {
                                handleApiError(responseData.getMessage());
                            }
                        } else {
                            handleApiError("Lỗi kết nối máy chủ");
                        }
                        showLoading(false);
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        handleApiError("Lỗi kết nối");
                        showLoading(false);
                    }
                }
        );
    }

    private void cancelInvitation(int position) {
        showLoading(true);
        FriendInvitation invitation = sentInvitations.get(position);

        apiManager.endFriendRequest(
                sessionManager.getAccessToken(),
                invitation.getRequest_id(),
                new Callback<ResponseData<Object>>() {
                    @Override
                    public void onResponse(Call<ResponseData<Object>> call, Response<ResponseData<Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ResponseData<Object> responseData = response.body();
                            if (responseData.getCode() == Constants.CODE_SUCCESS) {
                                Toast.makeText(FriendInvitationActivity.this,
                                        "Đã hủy lời mời kết bạn", Toast.LENGTH_SHORT).show();

                                // Remove from list
                                sentInvitations.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, sentInvitations.size());

                                // Check if list is empty
                                updateEmptyStateVisibility(sentInvitations);

                                // Refresh data from server after a delay
                                recyclerViewInvitations.postDelayed(() -> getDataFromServer(), 1000);
                            } else {
                                handleApiError(responseData.getMessage());
                            }
                        } else {
                            handleApiError("Lỗi kết nối máy chủ");
                        }
                        showLoading(false);
                    }

                    @Override
                    public void onFailure(Call<ResponseData<Object>> call, Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        handleApiError("Lỗi kết nối");
                        showLoading(false);
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingIndicator.setVisibility(View.VISIBLE);
            recyclerViewInvitations.setVisibility(View.GONE);
            tvNoInvitations.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
            recyclerViewInvitations.setVisibility(View.VISIBLE);
            // The empty state text visibility is managed separately
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        getDataFromServer();
    }
}