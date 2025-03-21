package com.example.chatapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

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
        version = 1,
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
}
