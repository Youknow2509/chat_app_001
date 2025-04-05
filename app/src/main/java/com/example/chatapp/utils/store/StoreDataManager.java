package com.example.chatapp.utils.store;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.models.Media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StoreDataManager implements IStoreData {
    private static final String TAG = "StoreDataManager";
    private static final String FOLDER_NAME_IMAGES = "images";
    private static final String FOLDER_NAME_VIDEOS = "videos";
    private static final String FOLDER_NAME_THUMBNAILS = "thumbnails";
    private static final String FOLDER_NAME_AUDIO = "audio";
    private static final String FOLDER_NAME_RECORDS = "records";
    private final String DATABASE_NAME = Constants.DATABASE_NAME;

    // Các constant cho loại media
    private static final String TYPE_IMAGE = "image";
    private static final String TYPE_VIDEO = "video";
    private static final String TYPE_THUMBNAIL = "thumbnail";
    private static final String TYPE_AUDIO = "audio";
    private static final String TYPE_RECORD = "record";

    // Helper methods for getting file paths
    private File getInternalDir(Context context) {
        return context.getFilesDir();
    }

    private File getImagesDir(Context context) {
        return new File(getInternalDir(context), FOLDER_NAME_IMAGES);
    }

    private File getVideosDir(Context context) {
        return new File(getInternalDir(context), FOLDER_NAME_VIDEOS);
    }

    private File getThumbnailsDir(Context context) {
        return new File(getInternalDir(context), FOLDER_NAME_THUMBNAILS);
    }

    private File getAudioDir(Context context) {
        return new File(getInternalDir(context), FOLDER_NAME_AUDIO);
    }

    private File getRecordsDir(Context context) {
        return new File(getInternalDir(context), FOLDER_NAME_RECORDS);
    }

    // [các phương thức hiện có giữ nguyên]

    // Implement các phương thức chi tiết media mới
    @Override
    public List<Media> detailImageStore(Context context) {
        return getMediaList(getImagesDir(context), TYPE_IMAGE);
    }

    @Override
    public List<Media> detailVideoStore(Context context) {
        return getMediaList(getVideosDir(context), TYPE_VIDEO);
    }

    @Override
    public List<Media> detailThumbnailStore(Context context) {
        return getMediaList(getThumbnailsDir(context), TYPE_THUMBNAIL);
    }

    @Override
    public List<Media> detailRecordStore(Context context) {
        return getMediaList(getRecordsDir(context), TYPE_RECORD);
    }

    /**
     * Helper method để lấy danh sách thông tin media từ một thư mục
     * @param directory Thư mục cần quét
     * @param type Loại media
     * @return Danh sách các đối tượng Media
     */
    private List<Media> getMediaList(File directory, String type) {
        List<Media> mediaList = new ArrayList<>();

        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            Log.w(TAG, "Directory does not exist: " + (directory != null ? directory.getAbsolutePath() : "null"));
            return mediaList;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            Log.w(TAG, "No files found in directory: " + directory.getAbsolutePath());
            return mediaList;
        }

        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                String path = file.getAbsolutePath();
                long bytes = file.length();
                long kb = bytes / 1024;

                Media media = new Media(type, name, path, bytes, kb);
                mediaList.add(media);
            } else if (file.isDirectory()) {
                // Quét đệ quy trong các thư mục con
                mediaList.addAll(getMediaList(file, type));
            }
        }

        return mediaList;
    }

    // [các phương thức còn lại của StoreDataManager...]

    @Override
    public long calculateImageStore(Context context) {
        return calculateFolder(context, getImagesDir(context).getAbsolutePath());
    }

    @Override
    public long calculateVideoStore(Context context) {
        return calculateFolder(context, getVideosDir(context).getAbsolutePath());
    }

    @Override
    public long calculateThumbnailStore(Context context) {
        return calculateFolder(context, getThumbnailsDir(context).getAbsolutePath());
    }

    @Override
    public long calculateDatabaseStore(Context context, String db) {
        File dbFile = context.getDatabasePath(db);
        return dbFile.exists() ? dbFile.length() : 0;
    }

    @Override
    public long calculateTableStore(Context context, String db, String table) {
        SQLiteDatabase database = null;
        long size = 0;

        try {
            database = context.openOrCreateDatabase(db, Context.MODE_PRIVATE, null);

            // Try to get size using SQLite's internal stats
            Cursor cursor = database.rawQuery(
                    "SELECT SUM(pgsize) FROM dbstat WHERE name = ?",
                    new String[]{table}
            );

            if (cursor != null && cursor.moveToFirst()) {
                size = cursor.getLong(0);
                cursor.close();
            } else {
                // Fallback: Count rows and estimate size
                if (cursor != null) {
                    cursor.close();
                }

                cursor = database.rawQuery("SELECT COUNT(*) FROM " + table, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int rowCount = cursor.getInt(0);
                    // Rough estimate of 1KB per row
                    size = rowCount * 1024;
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating table size: " + e.getMessage(), e);
        } finally {
            if (database != null) {
                database.close();
            }
        }

        return size;
    }

    @Override
    public long calculateFile(Context context, String pathFile) {
        File file = new File(pathFile);
        return file.exists() && file.isFile() ? file.length() : 0;
    }

    @Override
    public long calculateFolder(Context context, String pathFolder) {
        File folder = new File(pathFolder);
        return getFolderSize(folder);
    }

    private long getFolderSize(File folder) {
        long size = 0;

        if (folder == null || !folder.exists()) {
            return 0;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return 0;
        }

        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            } else {
                size += getFolderSize(file);
            }
        }

        return size;
    }

    @Override
    public boolean removeAllImageStore(Context context) {
        return removeAllFolder(context, getImagesDir(context).getAbsolutePath());
    }

    @Override
    public boolean removeAllVideoStore(Context context) {
        return removeAllFolder(context, getVideosDir(context).getAbsolutePath());
    }

    @Override
    public boolean removeAllThumbnailStore(Context context) {
        return removeAllFolder(context, getThumbnailsDir(context).getAbsolutePath());
    }

    @Override
    public boolean removeAllDatabaseStore(Context context, String db) {
        return context.deleteDatabase(db);
    }

    @Override
    public boolean removeAllTableStore(Context context, String db, String table) {
        SQLiteDatabase database = null;
        boolean success = false;

        try {
            database = context.openOrCreateDatabase(db, Context.MODE_PRIVATE, null);
            database.execSQL("DELETE FROM " + table);
            database.execSQL("VACUUM");  // Reclaim storage space
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "Error removing table data: " + e.getMessage(), e);
        } finally {
            if (database != null) {
                database.close();
            }
        }

        return success;
    }

    @Override
    public boolean removeAllFile(Context context, String pathFile) {
        File file = new File(pathFile);
        return file.exists() && file.isFile() && file.delete();
    }

    @Override
    public boolean removeAllFolder(Context context, String pathFolder) {
        File folder = new File(pathFolder);
        boolean success = deleteRecursive(folder);

        // Recreate the empty folder
        if (success && !folder.exists()) {
            folder.mkdirs();
        }

        return success;
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory == null || !fileOrDirectory.exists()) {
            return true;
        }

        // First delete contents if it's a directory
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }

        // Then delete the file/directory itself
        return fileOrDirectory.delete();
    }

    @Override
    public boolean removeImagesOlderThan(Context context, long timeInMillis) {
        long cutoffTime = System.currentTimeMillis() - timeInMillis;
        return removeFilesOlderThan(getImagesDir(context), cutoffTime);
    }

    @Override
    public boolean removeVideosOlderThan(Context context, long timeInMillis) {
        long cutoffTime = System.currentTimeMillis() - timeInMillis;
        return removeFilesOlderThan(getVideosDir(context), cutoffTime);
    }

    @Override
    public boolean removeChatMessagesOlderThan(Context context, String db, String table, long timeInMillis) {
        SQLiteDatabase database = null;
        boolean success = false;

        try {
            database = context.openOrCreateDatabase(db, Context.MODE_PRIVATE, null);
            long cutoffTime = System.currentTimeMillis() - timeInMillis;

            // Assuming the timestamp column is named "timestamp" or "createdAt"
            // Adjust this query based on your actual database schema
            database.execSQL("DELETE FROM " + table + " WHERE timestamp < ?",
                    new Object[]{cutoffTime});
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "Error removing old chat messages: " + e.getMessage(), e);

            // Try alternative column name if the first attempt failed
            try {
                if (database != null) {
                    long cutoffTime = System.currentTimeMillis() - timeInMillis;
                    database.execSQL("DELETE FROM " + table + " WHERE createdAt < ?",
                            new Object[]{cutoffTime});
                    success = true;
                }
            } catch (Exception e2) {
                Log.e(TAG, "Alternative approach also failed: " + e2.getMessage(), e2);
            }
        } finally {
            if (database != null) {
                database.close();
            }
        }

        return success;
    }

    @Override
    public long calculateAudioStore(Context context) {
        return calculateFolder(context, getAudioDir(context).getAbsolutePath());
    }

    @Override
    public boolean removeAllAudioStore(Context context) {
        return removeAllFolder(context, getAudioDir(context).getAbsolutePath());
    }

    private boolean removeFilesOlderThan(File directory, long cutoffTime) {
        boolean success = true;

        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return true;
        }

        for (File file : files) {
            if (file.isFile()) {
                if (file.lastModified() < cutoffTime) {
                    if (!file.delete()) {
                        Log.e(TAG, "Failed to delete file: " + file.getAbsolutePath());
                        success = false;
                    }
                }
            } else if (file.isDirectory()) {
                // Recursively process subdirectories
                if (!removeFilesOlderThan(file, cutoffTime)) {
                    success = false;
                }

                // Check if directory is now empty and delete if it is
                File[] remainingFiles = file.listFiles();
                if (remainingFiles != null && remainingFiles.length == 0) {
                    if (!file.delete()) {
                        Log.e(TAG, "Failed to delete empty directory: " + file.getAbsolutePath());
                        success = false;
                    }
                }
            }
        }

        return success;
    }
}