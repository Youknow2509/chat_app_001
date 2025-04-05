package com.example.chatapp.consts;

import java.util.HashMap;

public class Constants {
    public static final String URL_AVATAR_DEFAULT = "https://res.cloudinary.com/dan8zea37/image/upload/v1743573154/avatar_default_k7t5oq.png";
    public static final int DEFAULT_MEDIA_RETENTION_DAYS = 30; // 1 m
    public static final int DEFAULT_MESSAGE_RETENTION_DAYS = 90; // 3 m
    public static final String PREF_LAST_PURGE = "last_data_purge_time";
    public static final String TOKEN_PREFIX_REQUEST = "Bearer ";
    public static final String IS_LOGGED_IN_PREEFS_KEY = "is_logged_in";
    public static final String USER_ID_PREEFS_KEY = "user_id";
    public static final String REFRESH_TOKEN_KEY_PREF = "refresh_token_pref";
    public static final String ACCESS_TOKEN_KEY_PREF = "access_token_pref";
    public static final String SHARED_PREFS_FILE_TOKEN = "SessionEncryptedPrefs";
    public static final int CODE_SUCCESS = 20001;
    public static final String HOST_SERVER = "khanhdew.ddns.net:8080"; // ws
    public static final String URL_HOST_SERVER = "http://10.0.2.2:8082"; // http://10.0.2.2:8082 is local
    public static final String URL_HOST_SERVER_SIGN = "http://khanhdew.ddns.net:8261"; // http://10.0.2.2:8082 is local

    public static final int NUMBER_OF_THREADS_Write_EXECUTOR_DATABASE = 4;
    public static final String DATABASE_NAME = "chatAppDatabase";
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppReference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userID";

    public static final String KEY_USER_NAME = "userName";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME  = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_REGISTRATION_IDS = "registration_ids";
    public static final String KEY_GROUP = "group";
    public static final String KEY_GROUP_ID = "groupId";
    public static final String KEY_IS_GROUP_CHAT = "isGroupChat";

    public static final String KEY_GROUP_NAME = "groupName";

    public static final String KEY_TYPE_CALL = "callType";

    public static final String ACTION_CALL_STATE_CHANGED = "com.example.chatapp.ACTION_CALL_STATE_CHANGED";
    public static final String EXTRA_CALL_ACTIVE = "com.example.chatapp.EXTRA_CALL_ACTIVE";
    public static final String EXTRA_CALL_TYPE = "com.example.chatapp.EXTRA_CALL_TYPE";
    public static final String EXTRA_CALLER_NAME = "com.example.chatapp.EXTRA_CALLER_NAME";


    // Update this with your actual FCM server key
    private static final String FCM_SERVER_KEY = "";

    private static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(REMOTE_MSG_AUTHORIZATION, "key=" + FCM_SERVER_KEY);
            remoteMsgHeaders.put(REMOTE_MSG_CONTENT_TYPE, "application/json");
        }
        return remoteMsgHeaders;
    }
}
