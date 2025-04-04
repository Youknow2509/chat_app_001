package com.example.chatapp.utils.session;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.example.chatapp.models.UserProfileSession;
import com.example.chatapp.utils.Utils;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public class SessionManager {
    // Các key bảo mật
    private static final String PREF_NAME = "ChatAppPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ACCESS_TOKEN_EXPIRY = "access_token_expiry";
    private static final String KEY_REFRESH_TOKEN_EXPIRY = "refresh_token_expiry";

    // Các key thông tin người dùng không bảo mật
    private static final String USER_PREF_NAME = "ChatAppUserPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_AVATAR = "user_avatar";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_STATUS = "user_status";
    private static final String KEY_USER_DISPLAY_NAME = "user_display_name";
    private static final String KEY_USER_BIO = "user_bio";
    private static final String KEY_USER_LAST_ONLINE = "user_last_online";
    private static final String KEY_USER_CREATED_AT = "user_created_at";
    private static final String KEY_USER_GENDER = "user_gender";
    private static final String KEY_USER_BIRTHDAY = "user_birthday";
    private static final String KEY_USER_SETTINGS = "user_settings"; // Store as JSON string
    private static final String KEY_USER_AVATAR_FILE_PATH = "user_avatar_file_path";

    private SharedPreferences sharedPreferences; // Mã hóa cho thông tin nhạy cảm
    private SharedPreferences.Editor editor;

    private SharedPreferences userPreferences; // Không mã hóa cho thông tin profile
    private SharedPreferences.Editor userEditor;

    private Context context;

    private volatile static SessionManager instance;

    public SessionManager(Context context) {
        this.context = context;
        if (instance == null) {
            instance = this;
        }
        try {
            // Khởi tạo SharedPreferences có mã hóa cho thông tin xác thực
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            editor = sharedPreferences.edit();

            // Khởi tạo SharedPreferences thông thường cho thông tin không nhạy cảm
            userPreferences = context.getSharedPreferences(USER_PREF_NAME, Context.MODE_PRIVATE);
            userEditor = userPreferences.edit();

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately in your app
        }
    }

    public static SessionManager getInstance() {
        if(instance == null) {
            throw new IllegalStateException("SessionManager is not initialized, call initialize() method first.");
        }
        return instance;
    }

    // Các phương thức xử lý thông tin xác thực (giữ nguyên như cũ)
    public void saveAuthData(String accessToken, String refreshToken, String userId) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_USER_ID, userId);

        long accessTokenExpiry = getTokenExpiry(accessToken);
        long refreshTokenExpiry = getTokenExpiry(refreshToken);

        editor.putLong(KEY_ACCESS_TOKEN_EXPIRY, accessTokenExpiry);
        editor.putLong(KEY_REFRESH_TOKEN_EXPIRY, refreshTokenExpiry);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void updateTokens(String accessToken, String refreshToken) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);

        long accessTokenExpiry = getTokenExpiry(accessToken);
        long refreshTokenExpiry = getTokenExpiry(refreshToken);

        editor.putLong(KEY_ACCESS_TOKEN_EXPIRY, accessTokenExpiry);
        editor.putLong(KEY_REFRESH_TOKEN_EXPIRY, refreshTokenExpiry);
        editor.apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        // Xóa cả thông tin xác thực và thông tin người dùng
        editor.clear();
        editor.apply();

        userEditor.clear();
        userEditor.apply();
    }

    public boolean isTokenExpired(String token) {
        long expiryTime = getTokenExpiry(token);
        return System.currentTimeMillis() / 1000 >= expiryTime;
    }

    public boolean isSessionExpired() {
        long accessTokenExpiry = sharedPreferences.getLong(KEY_ACCESS_TOKEN_EXPIRY, 0);
        long refreshTokenExpiry = sharedPreferences.getLong(KEY_REFRESH_TOKEN_EXPIRY, 0);
        long currentTime = System.currentTimeMillis() / 1000;

        return currentTime >= accessTokenExpiry || currentTime >= refreshTokenExpiry;
    }

    public void refreshSession(String newAccessToken, String newRefreshToken) {
        updateTokens(newAccessToken, newRefreshToken);
    }

    private long getTokenExpiry(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return 0;
            }
            String payload = new String(Base64.decodeBase64(parts[1])); // Giải mã payload
            JSONObject jsonObject = new JSONObject(payload);
            return jsonObject.getLong("exp");
        } catch (JSONException | IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean shouldRefreshToken() {
        long accessTokenExpiry = sharedPreferences.getLong(KEY_ACCESS_TOKEN_EXPIRY, 0);
        long currentTime = System.currentTimeMillis() / 1000;

        // Refresh if the access token will expire within the next 5 minutes
        return (accessTokenExpiry - currentTime) <= (5 * 60);
    }

    /**
     * Lưu trữ thông tin cơ bản của người dùng
     * @param name Tên người dùng
     * @param email Email người dùng
     * @param avatar URL ảnh đại diện
     */
    public void saveUserBasicInfo(String name, String email, String avatar) {
        userEditor.putString(KEY_USER_NAME, name);
        userEditor.putString(KEY_USER_EMAIL, email);
        userEditor.putString(KEY_USER_AVATAR, avatar);
        userEditor.apply();
    }

    /**
     * Lưu trữ thông tin đầy đủ của người dùng
     * @param userProfile Đối tượng chứa thông tin người dùng
     */
    public void saveUserProfile(UserProfileSession userProfile) {
        userEditor.putString(KEY_USER_NAME, userProfile.getName());
        userEditor.putString(KEY_USER_EMAIL, userProfile.getEmail());
        userEditor.putString(KEY_USER_AVATAR, userProfile.getAvatarUrl());
        userEditor.putString(KEY_USER_PHONE, userProfile.getPhone());
        userEditor.putString(KEY_USER_STATUS, userProfile.getStatus());
        userEditor.putString(KEY_USER_DISPLAY_NAME, userProfile.getDisplayName());
        userEditor.putString(KEY_USER_BIO, userProfile.getBio());
        userEditor.putLong(KEY_USER_LAST_ONLINE, userProfile.getLastOnlineTimestamp());
        userEditor.putLong(KEY_USER_CREATED_AT, userProfile.getCreatedAtTimestamp());
        userEditor.putString(KEY_USER_GENDER, userProfile.getUserGender());
        userEditor.putString(KEY_USER_BIRTHDAY, userProfile.getDateOfBirth());

        // Lưu trữ settings dưới dạng JSON
        if (userProfile.getSettings() != null) {
            // Sửa đoạn này: không tạo JSONObject mới, sử dụng trực tiếp
            userEditor.putString(KEY_USER_SETTINGS, userProfile.getSettings().toString());
        }

        userEditor.apply();
    }

    /**
     * Cập nhật một trường thông tin cụ thể trong profile người dùng
     * @param key Khóa của thông tin cần cập nhật
     * @param value Giá trị mới
     */
    public void updateUserProfileField(String key, String value) {
        userEditor.putString(key, value);
        userEditor.apply();
    }

    /**
     * Cập nhật ảnh đại diện người dùng
     * @param avatarUrl URL của ảnh đại diện mới
     */
    public void updateUserAvatar(String avatarUrl) {
        userEditor.putString(KEY_USER_AVATAR, avatarUrl);
        userEditor.apply();
    }

    /**
     * Cập nhật trạng thái người dùng
     * @param status Trạng thái mới của người dùng
     */
    public void updateUserStatus(String status) {
        userEditor.putString(KEY_USER_STATUS, status);
        userEditor.apply();
    }

    /**
     * Lấy thông tin đầy đủ của người dùng
     * @return Đối tượng UserProfile chứa toàn bộ thông tin
     */
    public UserProfileSession getUserProfile() {
        UserProfileSession profile = new UserProfileSession();

        profile.setName(userPreferences.getString(KEY_USER_NAME, ""));
        profile.setEmail(userPreferences.getString(KEY_USER_EMAIL, ""));
        profile.setAvatarUrl(userPreferences.getString(KEY_USER_AVATAR, ""));
        profile.setPhone(userPreferences.getString(KEY_USER_PHONE, ""));
        profile.setStatus(userPreferences.getString(KEY_USER_STATUS, ""));
        profile.setDisplayName(userPreferences.getString(KEY_USER_DISPLAY_NAME, ""));
        profile.setBio(userPreferences.getString(KEY_USER_BIO, ""));
        profile.setLastOnlineTimestamp(userPreferences.getLong(KEY_USER_LAST_ONLINE, 0));
        profile.setCreatedAtTimestamp(userPreferences.getLong(KEY_USER_CREATED_AT, 0));
        profile.setUserGender(userPreferences.getString(KEY_USER_GENDER, ""));
        profile.setDateOfBirth(userPreferences.getString(KEY_USER_BIRTHDAY, ""));

        // Khôi phục settings từ chuỗi JSON
        String settingsJson = userPreferences.getString(KEY_USER_SETTINGS, null);
        if (settingsJson != null) {
            try {
                profile.setSettings(new JSONObject(settingsJson));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return profile;
    }

    /**
     * Lấy đường dẫn thư mục ảnh đại diện người dùng
     * @return path
     */
    public String getPathFileAvatarUser() {
        return userPreferences.getString(KEY_USER_AVATAR_FILE_PATH, "");
    }

    /**
     * Set đường dẫn thư mục ảnh đại diện người dùng
     */
    public String setPathFileAvatarUser(String path) {
        userEditor.putString(KEY_USER_AVATAR_FILE_PATH, path);
        userEditor.apply();
        return path;
    }

    /**
     * Lấy tên người dùng
     * @return Tên người dùng hoặc chuỗi rỗng nếu không tồn tại
     */
    public String getUserName() {
        return userPreferences.getString(KEY_USER_NAME, "");
    }

    /**
     * Lấy email người dùng
     * @return Email người dùng hoặc chuỗi rỗng nếu không tồn tại
     */
    public String getUserEmail() {
        return userPreferences.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Lấy URL ảnh đại diện người dùng
     * @return URL ảnh đại diện hoặc chuỗi rỗng nếu không tồn tại
     */
    public String getUserAvatar() {
        return userPreferences.getString(KEY_USER_AVATAR, "");
    }

    /**
     * Lấy tên hiển thị của người dùng
     * @return Tên hiển thị hoặc tên thường nếu không có tên hiển thị
     */
    public String getDisplayName() {
        String displayName = userPreferences.getString(KEY_USER_DISPLAY_NAME, null);
        return (displayName != null && !displayName.isEmpty()) ? displayName : getUserName();
    }

    /**
     * Xóa chỉ thông tin người dùng nhưng giữ lại thông tin xác thực
     */
    public void clearUserProfileOnly() {
        userEditor.clear();
        userEditor.apply();
    }

    /**
     * Kiểm tra xem thông tin người dùng đã được lưu trữ chưa
     * @return true nếu đã có thông tin cơ bản, false nếu chưa
     */
    public boolean hasUserInfo() {
        return !userPreferences.getString(KEY_USER_NAME, "").isEmpty() &&
                !userPreferences.getString(KEY_USER_EMAIL, "").isEmpty();
    }
}