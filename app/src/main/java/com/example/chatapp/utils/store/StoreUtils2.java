package com.example.chatapp.utils.store;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import androidx.core.content.ContextCompat;

import com.example.chatapp.consts.Constants;

import java.io.File;


public class StoreUtils2 {

    // Kiểm tra dung lượng thư mục (file images, videos, records, v.v...)
    public static long getDirectorySize(File dir) {
        long size = 0;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    size += getDirectorySize(file);
                } else {
                    size += file.length();
                }
            }
        }
        return size;
    }

    // Kiểm tra dung lượng SQLite Database
    public static long getDatabaseSize(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.length();
    }

    // Định dạng kích thước (KB, MB, GB, v.v...)
    public static String formatSize(long size) {
        if (size <= 0) return "0B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    // Tính tổng dung lượng sử dụng của ứng dụng (file images, videos, records, SQLite DB)
    public static long getTotalDataSize(Context context) {
        long totalSize = 0;

        // Tính dung lượng các file trong thư mục
        totalSize += getDirectorySize(new File(context.getExternalFilesDir(null), "my_images"));
        totalSize += getDirectorySize(new File(context.getExternalFilesDir(null), "my_videos"));
        totalSize += getDirectorySize(new File(context.getExternalFilesDir(null), "records"));
        totalSize += getDirectorySize(new File(context.getExternalFilesDir(null), "media"));

        // Tính dung lượng của cơ sở dữ liệu SQLite
        totalSize += getDatabaseSize(context, Constants.DATABASE_NAME);

        return totalSize;
    }

    // Kiểm tra dung lượng bộ nhớ trong (Internal Storage)
    public static long getInternalStorageSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long availableBlocks = stat.getAvailableBlocksLong();
        long blockSize = stat.getBlockSizeLong();
        return availableBlocks * blockSize;
    }

    // Kiểm tra dung lượng bộ nhớ ngoài (External Storage)
    public static long getExternalStorageSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long availableBlocks = stat.getAvailableBlocksLong();
        long blockSize = stat.getBlockSizeLong();
        return availableBlocks * blockSize;
    }

    // Kiểm tra dung lượng bộ nhớ tổng thể (Internal + External)
    public static long getTotalStorageSize() {
        return getInternalStorageSize() + getExternalStorageSize();
    }

    // Xóa tất cả dữ liệu của ứng dụng
    public static void clearAppCache(Context context) {
        // Xóa tất cả các file trong thư mục của ứng dụng
        deleteFilesInDir(context.getExternalFilesDir(null));
        // Xóa cơ sở dữ liệu SQLite
        context.deleteDatabase(Constants.DATABASE_NAME);
    }

    // Xóa các file trong thư mục
    private static void deleteFilesInDir(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    deleteFilesInDir(file); // Đệ quy xóa các thư mục con
                } else {
                    file.delete();
                }
            }
        }
    }
}

