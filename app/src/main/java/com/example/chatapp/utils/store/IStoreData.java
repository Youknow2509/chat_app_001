package com.example.chatapp.utils.store;

import android.content.Context;
import java.util.List;

import com.example.chatapp.models.Media;

public interface IStoreData {
    long calculateImageStore(Context context);
    long calculateVideoStore(Context context);
    long calculateThumbnailStore(Context context);
    long calculateDatabaseStore(Context context, String db);
    long calculateTableStore(Context context, String db, String table);
    long calculateFile(Context context, String pathFile);
    long calculateFolder(Context context, String pathFolder);
    //
    boolean removeAllImageStore(Context context);
    boolean removeAllVideoStore(Context context);
    boolean removeAllThumbnailStore(Context context);
    boolean removeAllDatabaseStore(Context context, String db);
    boolean removeAllTableStore(Context context, String db, String table);
    boolean removeAllFile(Context context, String pathFile);
    boolean removeAllFolder(Context context, String pathFolder);
    // Xóa dữ liệu cũ hơn một khoảng thời gian nhất định
    boolean removeImagesOlderThan(Context context, long timeInMillis);
    boolean removeVideosOlderThan(Context context, long timeInMillis);
    boolean removeChatMessagesOlderThan(Context context, String db, String table, long timeInMillis);
    //
    long calculateAudioStore(Context context);
    boolean removeAllAudioStore(Context context);
    //
    List<Media> detailImageStore(Context context);
    List<Media> detailVideoStore(Context context);
    List<Media> detailThumbnailStore(Context context);
    List<Media> detailRecordStore(Context context);
}
