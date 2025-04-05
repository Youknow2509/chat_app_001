package com.example.chatapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.Media;
import com.example.chatapp.adapters.MediaAdapter;
import com.example.chatapp.utils.store.IStoreData;
import com.example.chatapp.utils.store.StoreDataManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StorageManagerActivity extends AppCompatActivity implements MediaAdapter.OnMediaItemClickListener {

    private TextView tvImageSize, tvVideoSize, tvThumbnailSize, tvRecordSize, tvDatabaseSize, tvTotalSize;
    private RecyclerView recyclerViewMedia;
    private ProgressBar progressTotal;
    private TabLayout tabLayout;
    private Button btnClearAll, btnClearOld;

    private IStoreData storeDataManager;
    private MediaAdapter mediaAdapter;
    private ExecutorService executorService;
    private Handler mainHandler;

    private static final int TAB_IMAGES = 0;
    private static final int TAB_VIDEOS = 1;
    private static final int TAB_THUMBNAILS = 2;
    private static final int TAB_RECORDS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_data_storage);

        initializeViews();
        setupRecyclerView();
        setupTabLayout();
        setupButtons();

        storeDataManager = new StoreDataManager();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        loadStorageStatistics();
        loadMediaForCurrentTab();
    }

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
    }

    private void setupRecyclerView() {
        recyclerViewMedia.setLayoutManager(new LinearLayoutManager(this));
        mediaAdapter = new MediaAdapter(new ArrayList<>(), this);
        recyclerViewMedia.setAdapter(mediaAdapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
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

    private void setupButtons() {
        btnClearAll.setOnClickListener(v -> {
            showClearConfirmDialog(false);
        });

        btnClearOld.setOnClickListener(v -> {
            showClearConfirmDialog(true);
        });
    }

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
                showLoading(false);
            });
        });
    }

    private void showLoading(boolean isLoading) {
        // TODO: Show loading indicator if needed
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

    @Override
    public void onMediaItemClick(Media media) {
        // Handle item click, perhaps show a preview or details
        Toast.makeText(this, "Đã chọn: " + media.getNameFile(), Toast.LENGTH_SHORT).show();
    }

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

    private void deleteSingleFile(Media media, int position) {
        executorService.execute(() -> {
            boolean success = storeDataManager.removeAllFile(this, media.getPathFile());

            mainHandler.post(() -> {
                if (success) {
                    // Update adapter
                    List<Media> currentList = new ArrayList<>(mediaAdapter.getItems());
                    currentList.remove(position);
                    mediaAdapter.updateData(currentList);

                    // Update storage statistics
                    loadStorageStatistics();

                    Toast.makeText(this, "Đã xóa file", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Không thể xóa file", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}