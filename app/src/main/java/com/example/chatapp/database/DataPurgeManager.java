package com.example.chatapp.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.sqlite.MediaFile;
import com.example.chatapp.models.sqlite.Message;
import com.example.chatapp.utils.DateUtils;
import com.example.chatapp.utils.file.FileUtils;
import com.example.chatapp.utils.PreferenceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages purging old data to prevent the database from growing too large.
 */
public class DataPurgeManager {
    private final String TAG = "DataPurgeManager";
    private final String PREF_LAST_PURGE;
    private final int DEFAULT_MESSAGE_RETENTION_DAYS; // 3 months
    private final int DEFAULT_MEDIA_RETENTION_DAYS; // 1 month

    private final Context context;
    private final AppDatabase database;
    private final SharedPreferences preferences;
    private final Gson gson;

    public DataPurgeManager(Context context, AppDatabase database) {
        this.PREF_LAST_PURGE = Constants.PREF_LAST_PURGE;
        this.DEFAULT_MESSAGE_RETENTION_DAYS = Constants.DEFAULT_MESSAGE_RETENTION_DAYS;
        this.DEFAULT_MEDIA_RETENTION_DAYS = Constants.DEFAULT_MEDIA_RETENTION_DAYS;

        this.context = context.getApplicationContext();
        this.database = database;
        this.preferences = PreferenceUtils.getSharedPreferences(context);
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .setPrettyPrinting()
                .create();
    }

    /**
     * Check if it's time to run data purge operations.
     * This should be called periodically, e.g., when the app starts.
     */
    public void checkAndRunDataPurge() {
        long lastPurgeTime = preferences.getLong(PREF_LAST_PURGE, 0);
        long currentTime = System.currentTimeMillis();

        // Run purge if it's been more than 7 days since last purge
        if (lastPurgeTime == 0 || currentTime - lastPurgeTime > TimeUnit.DAYS.toMillis(7)) {
            runDataPurge();
        }
    }

    /**
     * Force a data purge operation to run immediately.
     */
    public void runDataPurge() {
        Log.i(TAG, "Starting data purge operation");

        // Use database executor to run in background
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // 1. Archive and purge old messages
                purgeOldMessages();

                // 2. Clean up media files
                purgeOldMedia();

                // 3. Update last purge time
                preferences.edit()
                        .putLong(PREF_LAST_PURGE, System.currentTimeMillis())
                        .apply();

                Log.i(TAG, "Data purge completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error during data purge", e);
            }
        });
    }

    /**
     * Archive and delete messages older than retention period
     */
    private void purgeOldMessages() {
        // Calculate cutoff date
        int retentionDays = PreferenceUtils.getMessageRetentionDays(context, DEFAULT_MESSAGE_RETENTION_DAYS);
        Date cutoffDate = DateUtils.getDateBefore(Calendar.getInstance().getTime(), retentionDays);

        // Get conversations
        List<String> conversationIds = database.conversationDao().getAllConversationIds();

        for (String conversationId : conversationIds) {
            // First, archive messages to backup storage
            List<Message> oldMessages = database.messageDao().getMessagesOlderThan(conversationId, cutoffDate);

            if (!oldMessages.isEmpty()) {
                // Archive messages before deleting
                archiveMessages(conversationId, oldMessages);

                // Delete old messages
                database.messageDao().deleteMessagesOlderThan(conversationId, cutoffDate);

                Log.i(TAG, String.format("Purged %d old messages from conversation %s",
                        oldMessages.size(), conversationId));
            }
        }
    }

    /**
     * Archive messages to JSON files before deleting
     */
    private void archiveMessages(String conversationId, List<Message> messages) {
        if (messages.isEmpty()) return;

        try {
            // Create archives directory if it doesn't exist
            File archiveDir = new File(context.getFilesDir(), "archives");
            if (!archiveDir.exists()) {
                archiveDir.mkdirs();
            }

            // Create a date-based filename
            String dateStr = DateUtils.formatDateForFilename(new Date());
            File archiveFile = new File(archiveDir, String.format("messages_%s_%s.json",
                    conversationId, dateStr));

            // Write messages to JSON file
            try (FileWriter writer = new FileWriter(archiveFile)) {
                gson.toJson(messages, writer);
            }

            Log.i(TAG, "Archived " + messages.size() + " messages to " + archiveFile.getPath());

        } catch (IOException e) {
            Log.e(TAG, "Error archiving messages", e);
        }
    }

    /**
     * Clean up old media files
     */
    private void purgeOldMedia() {
        // Calculate cutoff date
        int retentionDays = PreferenceUtils.getMediaRetentionDays(context, DEFAULT_MEDIA_RETENTION_DAYS);
        Date cutoffDate = DateUtils.getDateBefore(Calendar.getInstance().getTime(), retentionDays);

        // Get media files older than cutoff date
        List<MediaFile> oldMediaFiles = database.mediaFileDao().getMediaFilesOlderThan(cutoffDate);

        if (oldMediaFiles.isEmpty()) {
            return;
        }

        int deletedCount = 0;
        long freedSpace = 0;

        for (MediaFile mediaFile : oldMediaFiles) {
            // Delete the actual file if it exists
            if (mediaFile.getLocalPath() != null && !mediaFile.getLocalPath().isEmpty()) {
                File file = new File(mediaFile.getLocalPath());
                if (file.exists()) {
                    long fileSize = file.length();
                    if (file.delete()) {
                        freedSpace += fileSize;
                        deletedCount++;
                    }
                }

                // Delete thumbnail if it exists
                if (mediaFile.getThumbnailPath() != null && !mediaFile.getThumbnailPath().isEmpty()) {
                    File thumbnailFile = new File(mediaFile.getThumbnailPath());
                    if (thumbnailFile.exists()) {
                        thumbnailFile.delete();
                    }
                }
            }

            // Update the record to show it's been deleted from local storage
            mediaFile.setLocalPath(null);
            mediaFile.setThumbnailPath(null);
            mediaFile.setDownloadStatus("deleted_expired");
            database.mediaFileDao().update(mediaFile);
        }

        Log.i(TAG, String.format("Purged %d media files, freed %s of storage",
                deletedCount, FileUtils.formatFileSize(freedSpace)));
    }

    /**
     * Clean unused StoreUtils files
     */
    public void cleanupCache() {
        File cacheDir = context.getCacheDir();
        long freedSpace = FileUtils.deleteOldFiles(cacheDir, 7); // Delete files older than 7 days

        Log.i(TAG, "Cache cleanup freed " + FileUtils.formatFileSize(freedSpace));
    }

    /**
     * Get database size information
     */
    public DatabaseSizeInfo getDatabaseSizeInfo() {
        DatabaseSizeInfo info = new DatabaseSizeInfo();

        // Get file size
        File dbFile = context.getDatabasePath("chat_database");
        info.databaseSize = dbFile.length();

        // Get related files size (WAL, SHM)
        File walFile = new File(dbFile.getPath() + "-wal");
        if (walFile.exists()) {
            info.walSize = walFile.length();
        }

        File shmFile = new File(dbFile.getPath() + "-shm");
        if (shmFile.exists()) {
            info.shmSize = shmFile.length();
        }

        // Get count statistics
        AppDatabase.databaseWriteExecutor.execute(() -> {
            info.messageCount = database.messageDao().getMessageCount();
            info.conversationCount = database.conversationDao().getConversationCount();
            info.mediaCount = database.mediaFileDao().getMediaCount();
            info.userCount = database.userDao().getUserCount();
        });

        return info;
    }

    /**
     * Database size information
     */
    public static class DatabaseSizeInfo {
        public long databaseSize;
        public long walSize;
        public long shmSize;
        public int messageCount;
        public int conversationCount;
        public int mediaCount;
        public int userCount;

        public long getTotalSize() {
            return databaseSize + walSize + shmSize;
        }

        @Override
        public String toString() {
            return String.format("DB: %s, WAL: %s, Messages: %d, Conversations: %d, Media: %d, Users: %d",
                    FileUtils.formatFileSize(databaseSize),
                    FileUtils.formatFileSize(walSize),
                    messageCount,
                    conversationCount,
                    mediaCount,
                    userCount);
        }
    }
}