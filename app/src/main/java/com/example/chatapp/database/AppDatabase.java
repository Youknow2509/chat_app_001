package com.example.chatapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.dao.TokenClientDao;
import com.example.chatapp.models.TokenClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {TokenClient.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // list dao
    public abstract TokenClientDao tokenClientDao();

    // Background thread execution
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(Constants.NUMBER_OF_THREADS_Write_EXECUTOR_DATABASE);

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, Constants.DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
