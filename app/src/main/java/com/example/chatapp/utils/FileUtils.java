package com.example.chatapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Utility class for file operations.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    // Size constants
    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;

    private FileUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Format file size to human-readable string
     *
     * @param sizeInBytes File size in bytes
     * @return Formatted string (e.g., "1.2 MB")
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes <= 0) {
            return "0 B";
        }

        DecimalFormat df = new DecimalFormat("0.##");

        if (sizeInBytes < KB) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < MB) {
            return df.format(sizeInBytes / (float) KB) + " KB";
        } else if (sizeInBytes < GB) {
            return df.format(sizeInBytes / (float) MB) + " MB";
        } else {
            return df.format(sizeInBytes / (float) GB) + " GB";
        }
    }

    /**
     * Delete files older than specified days in a directory
     *
     * @param directory Directory to clean
     * @param olderThanDays Delete files older than this many days
     * @return Total space freed in bytes
     */
    public static long deleteOldFiles(File directory, int olderThanDays) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return 0;
        }

        long cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L);
        long freedSpace = 0;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.lastModified() < cutoffTime) {
                    long fileSize = file.length();
                    if (file.delete()) {
                        freedSpace += fileSize;
                        Log.v(TAG, "Deleted old file: " + file.getPath());
                    }
                } else if (file.isDirectory()) {
                    // Recursively clean subdirectories
                    freedSpace += deleteOldFiles(file, olderThanDays);
                }
            }
        }

        return freedSpace;
    }

    /**
     * Get a unique filename for saving a media file
     *
     * @param extension File extension (e.g., "jpg", "mp4")
     * @return Unique filename
     */
    public static String getUniqueFilename(String extension) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return "media_" + timestamp + "_" + uuid + "." + extension;
    }

    /**
     * Get file extension from URI
     *
     * @param context Application context
     * @param uri URI of the file
     * @return File extension or empty string if not found
     */
    public static String getFileExtension(Context context, Uri uri) {
        String extension;

        if (uri.getScheme().equals("content")) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }

        return extension != null ? extension.toLowerCase(Locale.US) : "";
    }

    /**
     * Copy a file from source to destination
     *
     * @param source Source file
     * @param dest Destination file
     * @return true if successful, false otherwise
     */
    public static boolean copyFile(File source, File dest) {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {

            byte[] buffer = new byte[8192];
            int read;

            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copying file", e);
            return false;
        }
    }

    /**
     * Get file name from URI
     *
     * @param context Application context
     * @param uri URI of the file
     * @return File name or null if not found
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;

        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting filename from URI", e);
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }

    /**
     * Check if external storage is available for read and write
     *
     * @return true if external storage is writable, false otherwise
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Create a temporary file in the app's cache directory
     *
     * @param context Application context
     * @param extension File extension (e.g., "jpg")
     * @return Temporary file
     * @throws IOException If file creation fails
     */
    public static File createTempFile(Context context, String extension) throws IOException {
        String fileName = "temp_" + System.currentTimeMillis() + "." + extension;
        File cacheDir = context.getCacheDir();
        return new File(cacheDir, fileName);
    }
}