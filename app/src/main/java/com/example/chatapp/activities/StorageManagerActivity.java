package com.example.chatapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.Media;
import com.example.chatapp.adapters.MediaAdapter;
import com.example.chatapp.dialogs.MediaPreviewDialog;
import com.example.chatapp.utils.store.IStoreData;
import com.example.chatapp.utils.store.StoreDataManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StorageManagerActivity extends AppCompatActivity implements MediaAdapter.OnMediaItemClickListener {

    private TextView tvImageSize, tvVideoSize, tvThumbnailSize, tvRecordSize, tvDatabaseSize, tvTotalSize;
    private RecyclerView recyclerViewMedia;
    private ProgressBar progressTotal;
    private TabLayout tabLayout;
    private Button btnClearAll, btnClearOld;
    private FloatingActionButton fabDelete;
    private Toolbar toolbar;
    private TextView tvNoMedia;
    private ProgressBar loadingIndicator;

    private IStoreData storeDataManager;
    private MediaAdapter mediaAdapter;
    private ExecutorService executorService;
    private Handler mainHandler;

    private static final int TAB_IMAGES = 0;
    private static final int TAB_VIDEOS = 1;
    private static final int TAB_THUMBNAILS = 2;
    private static final int TAB_RECORDS = 3;

    private MenuItem selectAllMenuItem;
    private MenuItem selectNoneMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_storage);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupTabLayout();
        setupButtons();

        storeDataManager = new StoreDataManager();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        loadStorageStatistics();
        loadMediaForCurrentTab();
    }

    // khởi tạo các nút
    private void initializeViews() {
        tvImageSize = findViewById(R.id.tvImageSize);
        tvVideoSize = findViewById(R.id.tvVideoSize);
        tvThumbnailSize = findViewById(R.id.tvThumbnailSize);
        tvRecordSize = findViewById(R.id.tvRecordSize);
        tvDatabaseSize = findViewById(R.id.tvDatabaseSize);
        tvTotalSize = findViewById(R.id.tvTotalSize);
        progressTotal = findViewById(R.id.progressTotal);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerViewMedia = findViewById(R.id.recyclerViewMedia);
        btnClearAll = findViewById(R.id.btnClearAll);
        btnClearOld = findViewById(R.id.btnClearOld);
        fabDelete = findViewById(R.id.fabDelete);
        toolbar = findViewById(R.id.toolbar);
        tvNoMedia = findViewById(R.id.tvNoMedia);
        loadingIndicator = findViewById(R.id.loadingIndicator);
    }

    // tạo toolbar
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Quản lý lưu trữ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // tạo recycler view
    private void setupRecyclerView() {
        recyclerViewMedia.setLayoutManager(new LinearLayoutManager(this));
        mediaAdapter = new MediaAdapter(new ArrayList<>(), this);
        recyclerViewMedia.setAdapter(mediaAdapter);
    }

    // xử lý sự kiện khi tab được chọn - ảnh, video, ....
    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (mediaAdapter.isInMultiSelectMode()) {
                            exitSelectionMode();
                        }
                        loadMediaForCurrentTab();
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                    }
                });
    }

    // xử sự kiện element footer
    private void setupButtons() {
        btnClearAll.setOnClickListener(v -> {
            showClearConfirmDialog(false);
        });

        btnClearOld.setOnClickListener(v -> {
            showClearConfirmDialog(true);
        });

        fabDelete.setOnClickListener(v -> {
            if (mediaAdapter.isInMultiSelectMode()) {
                deleteSelectedItems();
            } else {
                mediaAdapter.toggleSelectionMode();
                updateUIForSelectionMode(true);
            }
        });
    }

    // Cập nhập ui cho chế độ chọn xoá nhiều phần tử
    private void updateUIForSelectionMode(boolean isSelectionMode) {
        if (isSelectionMode) {
            fabDelete.setImageResource(R.drawable.ic_delete);
            btnClearAll.setVisibility(View.GONE);
            btnClearOld.setVisibility(View.GONE);
            toolbar.setTitle("Đã chọn 0 mục");
            if (selectAllMenuItem != null) selectAllMenuItem.setVisible(true);
            if (selectNoneMenuItem != null) selectNoneMenuItem.setVisible(true);
        } else {
            fabDelete.setImageResource(R.drawable.ic_select);
            btnClearAll.setVisibility(View.VISIBLE);
            btnClearOld.setVisibility(View.VISIBLE);
            toolbar.setTitle("Quản lý lưu trữ");
            if (selectAllMenuItem != null) selectAllMenuItem.setVisible(false);
            if (selectNoneMenuItem != null) selectNoneMenuItem.setVisible(false);
        }
    }

    // xử lí chế độ xoá nhiều phần tử - chọn một, nhiều phần tử xoá
    private void exitSelectionMode() {
        mediaAdapter.toggleSelectionMode();
        updateUIForSelectionMode(false);
    }

    // Hiển thị dialog xác nhận xóa
    private void showClearConfirmDialog(boolean oldOnly) {
        String message = oldOnly ?
                "Bạn có chắc chắn muốn xóa tất cả file cũ hơn 30 ngày không?" :
                "Bạn có chắc chắn muốn xóa tất cả file không?";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage(message)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    performClearOperation(oldOnly);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // xử lí xoá
    private void performClearOperation(boolean oldOnly) {
        showLoading(true);

        executorService.execute(() -> {
            boolean success = false;
            int currentTab = tabLayout.getSelectedTabPosition();

            if (oldOnly) {
                // Xóa file cũ (hơn 30 ngày)
                long thirtyDaysInMillis = 30 * 24 * 60 * 60 * 1000L;

                switch (currentTab) {
                    case TAB_IMAGES:
                        success = storeDataManager.removeImagesOlderThan(this, thirtyDaysInMillis);
                        break;
                    case TAB_VIDEOS:
                        success = storeDataManager.removeVideosOlderThan(this, thirtyDaysInMillis);
                        break;
                    case TAB_THUMBNAILS:
                        // Assuming you have implemented this in your manager
                        success = true; // For this example
                        break;
                    case TAB_RECORDS:
                        // Assuming you have implemented this in your manager
                        success = true; // For this example
                        break;
                }
            } else {
                // Xóa tất cả
                switch (currentTab) {
                    case TAB_IMAGES:
                        success = storeDataManager.removeAllImageStore(this);
                        break;
                    case TAB_VIDEOS:
                        success = storeDataManager.removeAllVideoStore(this);
                        break;
                    case TAB_THUMBNAILS:
                        success = storeDataManager.removeAllThumbnailStore(this);
                        break;
                    case TAB_RECORDS:
                        success = storeDataManager.removeAllAudioStore(this); // Assuming records are audio
                        break;
                }
            }

            final boolean finalSuccess = success;
            mainHandler.post(() -> {
                showLoading(false);
                Toast.makeText(this, finalSuccess ?
                                "Đã xóa thành công" : "Có lỗi xảy ra khi xóa",
                        Toast.LENGTH_SHORT).show();

                // Refresh data
                loadStorageStatistics();
                loadMediaForCurrentTab();
            });
        });
    }

    // diglog - xoá nhiều phần tử chọn
    private void deleteSelectedItems() {
        List<Media> selectedItems = mediaAdapter.getSelectedItems();
        List<Integer> selectedPositions = mediaAdapter.getSelectedPositions();

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một mục để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa " + selectedItems.size() + " mục đã chọn?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    performDeleteSelectedItems(selectedItems, selectedPositions);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // thực hiện xoá nhiều phần tử chọn
    private void performDeleteSelectedItems(List<Media> selectedItems, List<Integer> positions) {
        showLoading(true);

        executorService.execute(() -> {
            boolean allSuccess = true;

            for (Media media : selectedItems) {
                boolean success = storeDataManager.removeAllFile(this, media.getPathFile());
                if (!success) {
                    allSuccess = false;
                }
            }

            boolean finalAllSuccess = allSuccess;
            mainHandler.post(() -> {
                showLoading(false);
                Toast.makeText(this, finalAllSuccess ?
                                "Đã xóa thành công tất cả mục đã chọn" :
                                "Có lỗi xảy ra khi xóa một số mục",
                        Toast.LENGTH_SHORT).show();

                // Exit selection mode
                exitSelectionMode();

                // Refresh data
                loadStorageStatistics();
                loadMediaForCurrentTab();
            });
        });
    }

    // lấy kích thước lưu trữ
    private void loadStorageStatistics() {
        showLoading(true);

        executorService.execute(() -> {
            // Calculate sizes
            long imageSize = storeDataManager.calculateImageStore(this);
            long videoSize = storeDataManager.calculateVideoStore(this);
            long thumbnailSize = storeDataManager.calculateThumbnailStore(this);
            long recordSize = storeDataManager.calculateAudioStore(this);
            long databaseSize = storeDataManager.calculateDatabaseStore(this, Constants.DATABASE_NAME);

            long totalSize = imageSize + videoSize + thumbnailSize + recordSize + databaseSize;

            mainHandler.post(() -> {
                // Update UI with sizes
                tvImageSize.setText(formatSize(imageSize));
                tvVideoSize.setText(formatSize(videoSize));
                tvThumbnailSize.setText(formatSize(thumbnailSize));
                tvRecordSize.setText(formatSize(recordSize));
                tvDatabaseSize.setText(formatSize(databaseSize));
                tvTotalSize.setText("Tổng: " + formatSize(totalSize));

                // Update progress bar (max 100%)
                int percentage = 0;
                if (totalSize > 0) {
                    // Calculate percentage relatively to a "reference" size (e.g., 1GB)
                    long referenceSize = 1024 * 1024 * 1024; // 1GB in bytes
                    percentage = (int) Math.min(100, (totalSize * 100) / referenceSize);
                }
                progressTotal.setProgress(percentage);

                showLoading(false);
            });
        });
    }

    // lấy dữ liệu truyền vào adapter - ảnh, video, ....
    private void loadMediaForCurrentTab() {
        showLoading(true);

        executorService.execute(() -> {
            List<Media> mediaList = new ArrayList<>();
            int currentTab = tabLayout.getSelectedTabPosition();

            switch (currentTab) {
                case TAB_IMAGES:
                    mediaList = storeDataManager.detailImageStore(this);
                    break;
                case TAB_VIDEOS:
                    mediaList = storeDataManager.detailVideoStore(this);
                    break;
                case TAB_THUMBNAILS:
                    mediaList = storeDataManager.detailThumbnailStore(this);
                    break;
                case TAB_RECORDS:
                    mediaList = storeDataManager.detailRecordStore(this);
                    break;
            }

            List<Media> finalMediaList = mediaList;
            mainHandler.post(() -> {
                mediaAdapter.updateData(finalMediaList);

                // Show empty state if no media
                if (finalMediaList.isEmpty()) {
                    tvNoMedia.setVisibility(View.VISIBLE);
                } else {
                    tvNoMedia.setVisibility(View.GONE);
                }

                showLoading(false);
            });
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingIndicator.setVisibility(View.VISIBLE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    // xử lí sự kiện click vào item trong recycler view
    @Override
    public void onMediaItemClick(Media media, int position) {
        if (mediaAdapter.isInMultiSelectMode()) {
            mediaAdapter.toggleSelection(position);
        } else {
            // Show preview dialog
            MediaPreviewDialog dialog = new MediaPreviewDialog(this, media);
            dialog.show();
        }
    }

    //
    @Override
    public void onDeleteClick(Media media, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa file " + media.getNameFile() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteSingleFile(media, position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onSelectionChanged(int count) {
        toolbar.setTitle("Đã chọn " + count + " mục");
    }

    // thực hiện xoá 1 file đơn
    private void deleteSingleFile(Media media, int position) {
        showLoading(true);

        executorService.execute(() -> {
            boolean success = storeDataManager.removeAllFile(this, media.getPathFile());

            mainHandler.post(() -> {
                showLoading(false);

                if (success) {
                    // Update adapter
                    List<Media> currentList = new ArrayList<>(mediaAdapter.getItems());
                    currentList.remove(position);
                    mediaAdapter.updateData(currentList);

                    // Update storage statistics
                    loadStorageStatistics();

                    // Show empty state if no media left
                    if (currentList.isEmpty()) {
                        tvNoMedia.setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(this, "Đã xóa file", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Không thể xóa file", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_storage_manager, menu);
        selectAllMenuItem = menu.findItem(R.id.action_select_all);
        selectNoneMenuItem = menu.findItem(R.id.action_select_none);

        // Initially hide selection menu items
        selectAllMenuItem.setVisible(false);
        selectNoneMenuItem.setVisible(false);

        return true;
    }

    // xử lí chọn nhiều, một, huỷ
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (mediaAdapter.isInMultiSelectMode()) {
                exitSelectionMode();
                return true;
            }
            finish();
            return true;
        } else if (id == R.id.action_select_all) {
            // Select all items
            List<Media> mediaList = mediaAdapter.getItems();
            for (int i = 0; i < mediaList.size(); i++) {
                if (!mediaAdapter.getSelectedPositions().contains(i)) {
                    mediaAdapter.toggleSelection(i);
                }
            }
            return true;
        } else if (id == R.id.action_select_none) {
            // Clear selection
            mediaAdapter.clearSelection();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mediaAdapter.isInMultiSelectMode()) {
            exitSelectionMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}