package com.example.chatapp.utils.file;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MediaUtils {
    private static final String TAG = "MediaUtils";
    private static final String IMAGE_FOLDER = "images";
    private static final String VIDEO_FOLDER = "videos";
    private static final String THUMBNAIL_FOLDER = "thumbnails";
    private static final int BUFFER_SIZE = 4096;

    // Executor for background operations
    private static final Executor executor = Executors.newCachedThreadPool();

    /**
     * Determine if a URI is an image or video
     *
     * @param context Application context
     * @param uri Media URI to check
     * @return "image", "video", or null if not determined
     */
    @Nullable
    public static String getMediaType(@NonNull Context context, @NonNull Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String mimeType = contentResolver.getType(uri);

        if (mimeType == null) {
            String extension = getFileExtension(context, uri);
            if (extension != null && !extension.isEmpty()) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.replace(".", ""));
            }
        }

        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return "image";
            } else if (mimeType.startsWith("video/")) {
                return "video";
            }
        }
        return null;
    }

    /**
     * Asynchronously save media file from Uri to internal storage
     *
     * @param context Application context
     * @param mediaUri Source media Uri
     * @param mediaType "image" or "video"
     * @return CompletableFuture with File object of the saved media, or null if saving failed
     */
    @NonNull
    public static CompletableFuture<File> saveMediaToInternalStorageAsync(
            @NonNull Context context,
            @NonNull Uri mediaUri,
            @NonNull String mediaType) {

        return CompletableFuture.supplyAsync(() -> {
            return saveMediaToInternalStorage(context, mediaUri, mediaType);
        }, executor);
    }

    /**
     * Save media file from Uri to internal storage
     *
     * @param context Application context
     * @param mediaUri Source media Uri
     * @param mediaType "image" or "video"
     * @return File object of the saved media, or null if saving failed
     */
    @Nullable
    public static File saveMediaToInternalStorage(
            @NonNull Context context,
            @NonNull Uri mediaUri,
            @NonNull String mediaType) {

        File destinationFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            // Generate filename based on timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName;
            String folderName;

            switch (mediaType) {
                case "image":
                    fileName = "IMG_" + timeStamp + ".jpg";
                    folderName = IMAGE_FOLDER;
                    break;
                case "video":
                    fileName = "VID_" + timeStamp + getFileExtension(context, mediaUri);
                    folderName = VIDEO_FOLDER;
                    break;
                default:
                    Log.e(TAG, "Unsupported media type: " + mediaType);
                    return null;
            }

            // Create directory in app's files directory
            File directory = new File(context.getFilesDir(), folderName);
            if (!directory.exists() && !directory.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + directory.getAbsolutePath());
                return null;
            }

            destinationFile = new File(directory, fileName);

            // Process based on media type
            if ("image".equals(mediaType)) {
                inputStream = context.getContentResolver().openInputStream(mediaUri);
                if (inputStream == null) {
                    Log.e(TAG, "Failed to open input stream for: " + mediaUri);
                    return null;
                }

                // Decode image to check dimensions and possibly resize
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                // Reopen the stream
                inputStream = context.getContentResolver().openInputStream(mediaUri);
                if (inputStream == null) {
                    return null;
                }

                // Calculate sample size if image is too large
                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateInSampleSize(options, 1280, 1280);

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();

                // Handle image rotation based on EXIF
                try {
                    inputStream = context.getContentResolver().openInputStream(mediaUri);
                    if (inputStream != null) {
                        ExifInterface exif = new ExifInterface(inputStream);
                        int orientation = exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL);

                        if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                            Matrix matrix = new Matrix();
                            switch (orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    matrix.postRotate(90);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    matrix.postRotate(180);
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    matrix.postRotate(270);
                                    break;
                            }

                            bitmap = Bitmap.createBitmap(
                                    bitmap,
                                    0, 0,
                                    bitmap.getWidth(), bitmap.getHeight(),
                                    matrix, true);
                        }
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error processing EXIF data: " + e.getMessage());
                }

                // Save the processed bitmap
                outputStream = new FileOutputStream(destinationFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
                bitmap.recycle(); // Explicitly recycle bitmap

            } else if ("video".equals(mediaType)) {
                inputStream = context.getContentResolver().openInputStream(mediaUri);
                if (inputStream == null) {
                    return null;
                }

                outputStream = new FileOutputStream(destinationFile);
                BufferedInputStream bis = new BufferedInputStream(inputStream);

                byte[] buffer = new byte[BUFFER_SIZE];
                int length;
                while ((length = bis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            Log.d(TAG, "Media saved successfully: " + destinationFile.getAbsolutePath());
            return destinationFile;

        } catch (IOException e) {
            Log.e(TAG, "Error saving media: " + e.getMessage(), e);
            if (destinationFile != null && destinationFile.exists()) {
                destinationFile.delete();
            }
            return null;

        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
    }

    /**
     * Create a thumbnail from a video file asynchronously
     *
     * @param context Application context
     * @param videoFile Video file to create thumbnail from
     * @return CompletableFuture with File object of the thumbnail, or null if creation failed
     */
    @NonNull
    public static CompletableFuture<File> createVideoThumbnailAsync(
            @NonNull Context context,
            @NonNull File videoFile) {

        return CompletableFuture.supplyAsync(() -> {
            return createVideoThumbnail(context, videoFile);
        }, executor);
    }

    /**
     * Create a thumbnail from a video file
     *
     * @param context Application context
     * @param videoFile Video file to create thumbnail from
     * @return File object of the saved thumbnail, or null if creation failed
     */
    @Nullable
    public static File createVideoThumbnail(@NonNull Context context, @NonNull File videoFile) {
        FileOutputStream fos = null;
        MediaMetadataRetriever retriever = null;

        try {
            // Generate filename for thumbnail
            String videoName = videoFile.getName();
            String thumbnailName = videoName.substring(0, videoName.lastIndexOf(".")) + "_thumb.jpg";

            // Create directory
            File directory = new File(context.getFilesDir(), THUMBNAIL_FOLDER);
            if (!directory.exists() && !directory.mkdirs()) {
                Log.e(TAG, "Failed to create thumbnail directory: " + directory);
                return null;
            }

            File thumbnailFile = new File(directory, thumbnailName);

            // Create thumbnail
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoFile.getAbsolutePath());

            // Try to get the duration to extract a frame from the middle
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = 0;
            if (durationStr != null) {
                try {
                    duration = Long.parseLong(durationStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing video duration", e);
                }
            }

            // Extract frame from the middle of the video if possible
            long frameTime = duration > 0 ? duration / 2 * 1000 : 1000000;
            Bitmap bitmap = retriever.getFrameAtTime(
                    frameTime,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

            if (bitmap == null) {
                Log.e(TAG, "Failed to extract video frame");
                return null;
            }

            // Resize thumbnail maintaining aspect ratio
            int targetWidth = 512;
            int targetHeight = (targetWidth * bitmap.getHeight()) / bitmap.getWidth();

            bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);

            // Save thumbnail
            fos = new FileOutputStream(thumbnailFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            bitmap.recycle(); // Explicitly recycle bitmap

            Log.d(TAG, "Video thumbnail created: " + thumbnailFile.getAbsolutePath());
            return thumbnailFile;

        } catch (Exception e) {
            Log.e(TAG, "Error creating video thumbnail: " + e.getMessage(), e);
            return null;

        } finally {
            closeQuietly(fos);
            if (retriever != null) {
                try {
                    retriever.release();
                } catch (Exception e) {
                    Log.e(TAG, "Error releasing MediaMetadataRetriever", e);
                }
            }
        }
    }

    /**
     * Get file extension from URI including the dot (e.g., ".mp4")
     */
    @Nullable
    private static String getFileExtension(@NonNull Context context, @NonNull Uri uri) {
        String extension = null;
        ContentResolver contentResolver = context.getContentResolver();

        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(
                    contentResolver.getType(uri));
        }

        if (extension == null) {
            // Try to get it from the last path segment
            String path = uri.getPath();
            if (path != null) {
                int dotPos = path.lastIndexOf(".");
                if (dotPos >= 0) {
                    extension = path.substring(dotPos + 1);
                }
            }
        }

        if (extension == null || extension.isEmpty()) {
            // Default extensions if we can't determine
            String mimeType = contentResolver.getType(uri);
            if (mimeType != null) {
                if (mimeType.startsWith("video/")) {
                    extension = "mp4";
                } else if (mimeType.startsWith("image/")) {
                    extension = "jpg";
                }
            }
        }

        return extension != null ? "." + extension : "";
    }

    /**
     * Delete media file and optionally its related thumbnail
     *
     * @param file File to delete
     * @param deleteThumbnail Whether to also delete associated thumbnail
     * @return True if deleted successfully
     */
    public static boolean deleteMedia(@Nullable File file, boolean deleteThumbnail) {
        if (file == null || !file.exists()) {
            return false;
        }

        boolean result = file.delete();

        // If it's a video and we want to delete the thumbnail
        if (deleteThumbnail && file.getName().startsWith("VID_")) {
            String videoName = file.getName();
            String thumbnailName = videoName.substring(0, videoName.lastIndexOf(".")) + "_thumb.jpg";
            File thumbnailFile = new File(new File(file.getParentFile().getParentFile(), THUMBNAIL_FOLDER),
                    thumbnailName);

            if (thumbnailFile.exists()) {
                boolean thumbResult = thumbnailFile.delete();
                Log.d(TAG, "Thumbnail deleted: " + thumbResult + " - " + thumbnailFile.getAbsolutePath());
            }
        }

        Log.d(TAG, "Media deleted: " + result + " - " + file.getAbsolutePath());
        return result;
    }

    /**
     * Get a sharable URI for the media file using FileProvider
     *
     * @param context Application context
     * @param file Media file
     * @return Sharable URI or null if error
     */
    @Nullable
    public static Uri getShareableUri(@NonNull Context context, @Nullable File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        try {
            // Using your app's FileProvider
            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error getting sharable URI: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Save media to MediaStore for Android 10+ (scoped storage)
     *
     * @param context Application context
     * @param sourceFile The file to save to MediaStore
     * @param mediaType "image" or "video"
     * @return Uri of the saved media in MediaStore or null on failure
     */
    @Nullable
    public static Uri saveToMediaStore(
            @NonNull Context context,
            @NonNull File sourceFile,
            @NonNull String mediaType) {

        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        // Set basic metadata
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.getName());
        contentValues.put(MediaStore.MediaColumns.SIZE, sourceFile.length());

        Uri externalContentUri;
        String mimeType;

        if ("image".equals(mediaType)) {
            externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            mimeType = "image/jpeg";
        } else if ("video".equals(mediaType)) {
            externalContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String extension = sourceFile.getName().substring(sourceFile.getName().lastIndexOf(".") + 1);
            mimeType = "video/" + (extension.equals("mp4") ? "mp4" : extension);
        } else {
            Log.e(TAG, "Unsupported media type for MediaStore: " + mediaType);
            return null;
        }

        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

        // For Android 10+ set relative path
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if ("image".equals(mediaType)) {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/MyApp");
            } else {
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/MyApp");
            }
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
        }

        Uri uri = null;
        OutputStream os = null;
        InputStream is = null;

        try {
            uri = resolver.insert(externalContentUri, contentValues);
            if (uri == null) {
                Log.e(TAG, "Failed to create new MediaStore record");
                return null;
            }

            os = resolver.openOutputStream(uri);
            if (os == null) {
                Log.e(TAG, "Failed to open output stream for MediaStore");
                return null;
            }

            is = new java.io.FileInputStream(sourceFile);

            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            // Clear pending flag for Android 10+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(uri, contentValues, null, null);
            }

            return uri;
        } catch (Exception e) {
            Log.e(TAG, "Error saving to MediaStore: " + e.getMessage(), e);
            if (uri != null) {
                resolver.delete(uri, null, null);
            }
            return null;
        } finally {
            closeQuietly(is);
            closeQuietly(os);
        }
    }

    /**
     * Get media from remote server
     *
     * @param context Application context
     * @param url URL of the media on remote server
     * @param authToken Authentication token (if required)
     * @return CompletableFuture with File path of downloaded media
     */
    @NonNull
    public static CompletableFuture<File> getMediaFromHost(
            @NonNull Context context,
            @NonNull String url,
            @Nullable String authToken) {

        return CompletableFuture.supplyAsync(() -> {
            // Implementation of network request using HttpURLConnection or OkHttp
            // This is a simplified example - in production, consider using a library like Retrofit

            InputStream inputStream = null;
            FileOutputStream outputStream = null;

            try {
                java.net.URL mediaUrl = new java.net.URL(url);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) mediaUrl.openConnection();

                // Add authentication if provided
                if (authToken != null && !authToken.isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + authToken);
                }

                connection.setDoInput(true);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != java.net.HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + responseCode);
                    return null;
                }

                // Determine media type from content type
                String contentType = connection.getContentType();
                String mediaType = null;
                String extension = null;

                if (contentType != null) {
                    if (contentType.startsWith("image/")) {
                        mediaType = "image";
                        extension = contentType.replace("image/", "");
                        if (extension.equals("jpeg")) extension = "jpg";
                    } else if (contentType.startsWith("video/")) {
                        mediaType = "video";
                        extension = contentType.replace("video/", "");
                    }
                }

                if (mediaType == null) {
                    Log.e(TAG, "Unknown content type: " + contentType);
                    return null;
                }

                // Create appropriate folder
                String folderName = "image".equals(mediaType) ? IMAGE_FOLDER : VIDEO_FOLDER;
                File directory = new File(context.getFilesDir(), folderName);
                if (!directory.exists() && !directory.mkdirs()) {
                    Log.e(TAG, "Could not create directory: " + directory);
                    return null;
                }

                // Create filename based on the URL and timestamp
                String fileName = "REMOTE_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date()) + "." + extension;
                File outputFile = new File(directory, fileName);

                // Download the file
                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(outputFile);

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                Log.d(TAG, "Downloaded media to: " + outputFile.getAbsolutePath());

                // If it's a video, create a thumbnail
                if ("video".equals(mediaType)) {
                    createVideoThumbnail(context, outputFile);
                }

                return outputFile;

            } catch (IOException e) {
                Log.e(TAG, "Error downloading media: " + e.getMessage(), e);
                return null;
            } finally {
                closeQuietly(inputStream);
                closeQuietly(outputStream);
            }
        }, executor);
    }

    /**
     * Calculate optimal scaling factor for loading bitmaps efficiently
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Quietly close a closeable resource
     */
    private static void closeQuietly(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing resource", e);
            }
        }
    }
}