package com.example.chatapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.dao.ContactDao;
import com.example.chatapp.dao.ConversationDao;
import com.example.chatapp.dao.ConversationMemberDao;
import com.example.chatapp.dao.MediaFileDao;
import com.example.chatapp.dao.MessageDao;
import com.example.chatapp.dao.RelationshipQueries;
import com.example.chatapp.dao.SettingDao;
import com.example.chatapp.dao.TokenClientDao;
import com.example.chatapp.dao.UserDao;
import com.example.chatapp.models.TokenClient;
import com.example.chatapp.models.sqlite.Contact;
import com.example.chatapp.models.sqlite.Conversation;
import com.example.chatapp.models.sqlite.ConversationMember;
import com.example.chatapp.models.sqlite.Converters;
import com.example.chatapp.models.sqlite.MediaFile;
import com.example.chatapp.models.sqlite.Message;
import com.example.chatapp.models.sqlite.Setting;
import com.example.chatapp.models.sqlite.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        TokenClient.class,
        User.class,
        Conversation.class,
        Message.class,
        ConversationMember.class,
        Contact.class,
        MediaFile.class,
        Setting.class
        },
        version = 2,
        exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    // list dao
    public abstract TokenClientDao tokenClientDao();
    public abstract UserDao userDao();
    public abstract ConversationDao conversationDao();
    public abstract MessageDao messageDao();
    public abstract ConversationMemberDao conversationMemberDao();
    public abstract ContactDao contactDao();
    public abstract MediaFileDao mediaFileDao();
    public abstract SettingDao settingDao();
    public abstract RelationshipQueries relationshipQueries();

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

    /**
     * Override the onCreate method to populate the database.
     * For this you'll need to implement a callback method.
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);

                    // If you want to populate the database with initial data
                    // when it's first created, do it here
                    databaseWriteExecutor.execute(() -> {
                        // Example: Pre-populate the database with some data
                        // UserDao dao = INSTANCE.userDao();
                        // dao.insert(new User("system", "System", "System Account", "system@chatapp.com"));
                    });
                }

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);

                    // You can also perform operations when the database is opened
                    // This is useful for setting up PRAGMA statements or database configurations
                    // Example: db.execSQL("PRAGMA foreign_keys = ON");
                }
            };

    /**
     * Closes the database and releases all resources.
     * This should be called when the app is shutting down.
     */
    public static void destroyInstance() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
        }
        INSTANCE = null;
    }

    /**
     * Checks the size of the database file.
     * Useful for monitoring and debugging.
     *
     * @param context The application context
     * @return The size of the database in bytes
     */
    public static long getDatabaseSize(Context context) {
        return context.getDatabasePath(Constants.DATABASE_NAME).length();
    }

    /**
     * Optimizes the database by running the VACUUM command.
     * This should be done periodically to reclaim space and improve performance.
     */
    public void optimize() {
        if (INSTANCE != null) {
            databaseWriteExecutor.execute(() -> {
                INSTANCE.getOpenHelper().getWritableDatabase().execSQL("VACUUM");
            });
        }
    }
}
