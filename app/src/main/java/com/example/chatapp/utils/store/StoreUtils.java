package com.example.chatapp.utils.store;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class để quản lý và hiển thị thông tin lưu trữ của ứng dụng
 * tương tự như Telegram.
 */
public class StoreUtils {

    private static final long MAX_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    private Context context;

    public StoreUtils(Context context) {
        this.context = context;
    }

    /**
     * Class lưu trữ thông tin về kích thước của các loại file khác nhau
     */
    public static class StorageInfo {
        public long totalSize = 0;
        public long imagesSize = 0;
        public long videosSize = 0;
        public long audioSize = 0;
        public long databaseSize = 0;
        public long cacheSize = 0;
        public long documentsSize = 0;
        public long otherSize = 0;
    }

    /**
     * Interface callback để trả về kết quả sau khi tính toán xong
     */
    public interface StorageInfoCallback {
        void onStorageInfoCalculated(StorageInfo storageInfo);
    }

    /**
     * Tính toán và lấy thông tin lưu trữ của ứng dụng không đồng bộ
     * @param callback callback sẽ được gọi khi tính toán hoàn tất
     */
    public void getStorageInfoAsync(final StorageInfoCallback callback) {
        new AsyncTask<Void, Void, StorageInfo>() {
            @Override
            protected StorageInfo doInBackground(Void... voids) {
                return calculateStorageInfo();
            }

            @Override
            protected void onPostExecute(StorageInfo storageInfo) {
                if (callback != null) {
                    callback.onStorageInfoCalculated(storageInfo);
                }
            }
        }.execute();
    }

    /**
     * Tính toán thông tin lưu trữ chi tiết
     * @return đối tượng StorageInfo chứa kích thước các loại file
     */
    public StorageInfo calculateStorageInfo() {
        StorageInfo info = new StorageInfo();

        // Lấy các thư mục chính của ứng dụng
        File dataDir = context.getFilesDir();
        File externalDir = context.getExternalFilesDir(null);
        File cacheDir = context.getCacheDir();

        // Tính toán kích thước theo loại
        info.imagesSize = getFileSizeByType(dataDir, new String[]{".jpg", ".jpeg", ".png", ".gif", ".webp"});
        info.videosSize = getFileSizeByType(dataDir, new String[]{".mp4", ".3gp", ".webm", ".mkv"});
        info.audioSize = getFileSizeByType(dataDir, new String[]{".mp3", ".wav", ".ogg", ".m4a"});
        info.documentsSize = getFileSizeByType(dataDir, new String[]{".pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt"});

        // Thêm kích thước từ bộ nhớ external nếu có
        if (externalDir != null) {
            info.imagesSize += getFileSizeByType(externalDir, new String[]{".jpg", ".jpeg", ".png", ".gif", ".webp"});
            info.videosSize += getFileSizeByType(externalDir, new String[]{".mp4", ".3gp", ".webm", ".mkv"});
            info.audioSize += getFileSizeByType(externalDir, new String[]{".mp3", ".wav", ".ogg", ".m4a"});
            info.documentsSize += getFileSizeByType(externalDir, new String[]{".pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt"});
        }

        // Lấy kích thước database
        File databaseDir = new File(context.getApplicationInfo().dataDir, "databases");
        info.databaseSize = getFolderSize(databaseDir);

        // Lấy kích thước cache
        info.cacheSize = getFolderSize(cacheDir);

        // Tính kích thước khác bằng cách trừ các kích thước đã biết từ tổng
        long calculatedSize = info.imagesSize + info.videosSize + info.audioSize +
                info.documentsSize + info.databaseSize + info.cacheSize;

        long appTotalSize = getFolderSize(new File(context.getApplicationInfo().dataDir));
        if (externalDir != null) {
            appTotalSize += getFolderSize(externalDir);
        }

        info.otherSize = Math.max(0, appTotalSize - calculatedSize);
        info.totalSize = appTotalSize;

        return info;
    }

    /**
     * Lấy tổng kích thước của tất cả files trong một thư mục và các thư mục con
     * @param directory thư mục cần tính kích thước
     * @return kích thước tổng tính bằng byte
     */
    public static long getFolderSize(File directory) {
        long length = 0;
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        length += file.length();
                    } else if (file.isDirectory()) {
                        length += getFolderSize(file);
                    }
                }
            }
        }
        return length;
    }

    /**
     * Lấy kích thước theo loại file dựa trên phần mở rộng
     * @param directory thư mục cần quét
     * @param extensions danh sách các phần mở rộng cần tính kích thước
     * @return kích thước tổng tính bằng byte
     */
    public static long getFileSizeByType(File directory, String[] extensions) {
        long length = 0;
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        for (String extension : extensions) {
                            if (file.getName().toLowerCase().endsWith(extension)) {
                                length += file.length();
                                break;
                            }
                        }
                    } else if (file.isDirectory()) {
                        length += getFileSizeByType(file, extensions);
                    }
                }
            }
        }
        return length;
    }

    /**
     * Chuyển đổi byte sang định dạng dễ đọc (KB, MB, GB, ...)
     * @param size kích thước tính bằng byte
     * @return chuỗi đã định dạng với đơn vị thích hợp
     */
    public static String formatSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
    }

    /**
     * Lấy kích thước của database SQLite
     * @param dbName tên file database
     * @return kích thước tính bằng byte
     */
    public long getDatabaseSize(String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists() ? dbFile.length() : 0;
    }

    /**
     * Lấy số lượng record theo bảng
     * @param db database SQLite
     * @param tableName tên bảng
     * @return số lượng record
     */
    public static int getTableCount(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        int count = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    /**
     * Tính dung lượng trung bình cho mỗi loại dữ liệu trong database
     * @param db database SQLite
     * @param tableToQuery map chứa tên bảng và câu truy vấn để lấy kích thước
     * @return map chứa tên bảng và kích thước trung bình của mỗi record
     */
    public static Map<String, Long> calculateAverageSize(SQLiteDatabase db, Map<String, String> tableToQuery) {
        Map<String, Long> result = new HashMap<>();

        for (Map.Entry<String, String> entry : tableToQuery.entrySet()) {
            String tableName = entry.getKey();
            String sizeQuery = entry.getValue();

            Cursor cursor = db.rawQuery(sizeQuery, null);
            if (cursor != null) {
                cursor.moveToFirst();
                long totalSize = cursor.getLong(0);
                int count = getTableCount(db, tableName);

                if (count > 0) {
                    result.put(tableName, totalSize / count);
                } else {
                    result.put(tableName, 0L);
                }
                cursor.close();
            }
        }

        return result;
    }

    /**
     * Kiểm tra và xóa cache nếu quá kích thước tối đa
     * @return true nếu cache được xóa, false nếu không
     */
    public boolean checkAndClearCache() {
        File cacheDir = context.getCacheDir();
        long cacheSize = getFolderSize(cacheDir);

        if (cacheSize > MAX_CACHE_SIZE) {
            clearCache();
            return true;
        }
        return false;
    }

    /**
     * Xóa cache của ứng dụng
     */
    public void clearCache() {
        try {
            File cacheDir = context.getCacheDir();
            deleteDirectory(cacheDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Xóa tất cả file trong một thư mục và các thư mục con
     * @param directory thư mục cần xóa
     * @return true nếu thành công, false nếu thất bại
     */
    private boolean deleteDirectory(File directory) {
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
    }

    /**
     * Xóa các bản ghi cũ trong database nếu số lượng vượt quá giới hạn
     * @param db database SQLite
     * @param tableName tên bảng
     * @param maxEntries số lượng bản ghi tối đa được phép
     * @return số lượng bản ghi đã xóa
     */
    public int trimDatabase(SQLiteDatabase db, String tableName, int maxEntries) {
        int count = getTableCount(db, tableName);
        if (count > maxEntries) {
            int deleteCount = count - maxEntries;
            db.execSQL("DELETE FROM " + tableName + " WHERE id IN " +
                    "(SELECT id FROM " + tableName + " ORDER BY timestamp ASC LIMIT " + deleteCount + ")");
            return deleteCount;
        }
        return 0;
    }

    /**
     * Xóa các file media không được sử dụng (orphaned files)
     * @param mediaDirectory thư mục chứa media files
     * @param db database SQLite
     * @param mediaTable tên bảng lưu thông tin media
     * @param pathColumn tên cột chứa đường dẫn file
     * @return số lượng file đã xóa
     */
    public int cleanupOrphanedMediaFiles(File mediaDirectory, SQLiteDatabase db,
                                         String mediaTable, String pathColumn) {
        int count = 0;
        if (!mediaDirectory.exists() || !mediaDirectory.isDirectory()) {
            return count;
        }

        File[] files = mediaDirectory.listFiles();
        if (files == null) {
            return count;
        }

        for (File file : files) {
            if (file.isFile()) {
                // Kiểm tra file có tồn tại trong database không
                String fileName = file.getName();
                Cursor cursor = db.rawQuery(
                        "SELECT COUNT(*) FROM " + mediaTable +
                                " WHERE " + pathColumn + " LIKE '%" + fileName + "'", null);

                if (cursor != null) {
                    cursor.moveToFirst();
                    int fileCount = cursor.getInt(0);
                    cursor.close();

                    if (fileCount == 0) {
                        // File không tồn tại trong database, xóa nó
                        if (file.delete()) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    /**
     * Kiểm tra xem một file có phải là hình ảnh không
     * @param fileName tên file
     * @return true nếu là hình ảnh, false nếu không
     */
    public static boolean isImageFile(String fileName) {
        String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        return hasExtension(fileName, extensions);
    }

    /**
     * Kiểm tra xem một file có phải là video không
     * @param fileName tên file
     * @return true nếu là video, false nếu không
     */
    public static boolean isVideoFile(String fileName) {
        String[] extensions = {".mp4", ".3gp", ".webm", ".mkv", ".avi", ".mov"};
        return hasExtension(fileName, extensions);
    }

    /**
     * Kiểm tra xem một file có phải là file ghi âm không
     * @param fileName tên file
     * @return true nếu là file ghi âm, false nếu không
     */
    public static boolean isAudioFile(String fileName) {
        String[] extensions = {".mp3", ".wav", ".ogg", ".m4a", ".aac"};
        return hasExtension(fileName, extensions);
    }

    /**
     * Kiểm tra xem một file có phải là file tài liệu không
     * @param fileName tên file
     * @return true nếu là file tài liệu, false nếu không
     */
    public static boolean isDocumentFile(String fileName) {
        String[] extensions = {".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt"};
        return hasExtension(fileName, extensions);
    }

    /**
     * Kiểm tra xem một file có phần mở rộng nằm trong danh sách các phần mở rộng được cho không
     * @param fileName tên file
     * @param extensions danh sách các phần mở rộng
     * @return true nếu file có phần mở rộng nằm trong danh sách, false nếu không
     */
    private static boolean hasExtension(String fileName, String[] extensions) {
        if (fileName == null) return false;

        String lowerCaseFileName = fileName.toLowerCase();
        for (String extension : extensions) {
            if (lowerCaseFileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}