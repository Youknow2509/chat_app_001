package com.example.chatapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Utility class for handling shared preferences.
 */
public class PreferenceUtils {
    private static final String TAG = "PreferenceUtils";

    // Preference keys
    private static final String PREF_MESSAGE_RETENTION_DAYS = "message_retention_days";
    private static final String PREF_MEDIA_RETENTION_DAYS = "media_retention_days";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_NAME = "user_name";
    private static final String PREF_PUSH_NOTIFICATIONS = "push_notifications_enabled";
    private static final String PREF_MESSAGE_PREVIEW = "message_preview_enabled";
    private static final String PREF_THEME = "app_theme";
    private static final String PREF_FONT_SIZE = "font_size";
    private static final String PREF_AUTO_DOWNLOAD_MEDIA = "auto_download_media";

    // Preference values
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_SYSTEM = "system";

    public static final String FONT_SIZE_SMALL = "small";
    public static final String FONT_SIZE_MEDIUM = "medium";
    public static final String FONT_SIZE_LARGE = "large";

    public static final String AUTO_DOWNLOAD_NEVER = "never";
    public static final String AUTO_DOWNLOAD_WIFI = "wifi";
    public static final String AUTO_DOWNLOAD_ALWAYS = "always";

    private PreferenceUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get the default shared preferences instance
     *
     * @param context Application context
     * @return SharedPreferences instance
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Get the number of days to retain messages before purging
     *
     * @param context Application context
     * @param defaultValue Default value if preference is not set
     * @return Number of days to retain messages
     */
    public static int getMessageRetentionDays(Context context, int defaultValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getInt(PREF_MESSAGE_RETENTION_DAYS, defaultValue);
    }

    /**
     * Set the number of days to retain messages
     *
     * @param context Application context
     * @param days Number of days to retain messages
     */
    public static void setMessageRetentionDays(Context context, int days) {
        getSharedPreferences(context).edit()
                .putInt(PREF_MESSAGE_RETENTION_DAYS, days)
                .apply();
    }

    /**
     * Get the number of days to retain media files before purging
     *
     * @param context Application context
     * @param defaultValue Default value if preference is not set
     * @return Number of days to retain media
     */
    public static int getMediaRetentionDays(Context context, int defaultValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getInt(PREF_MEDIA_RETENTION_DAYS, defaultValue);
    }

    /**
     * Set the number of days to retain media files
     *
     * @param context Application context
     * @param days Number of days to retain media
     */
    public static void setMediaRetentionDays(Context context, int days) {
        getSharedPreferences(context).edit()
                .putInt(PREF_MEDIA_RETENTION_DAYS, days)
                .apply();
    }

    /**
     * Get the current user ID
     *
     * @param context Application context
     * @return User ID or empty string if not set
     */
    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(PREF_USER_ID, "");
    }

    /**
     * Set the current user ID
     *
     * @param context Application context
     * @param userId User ID to set
     */
    public static void setUserId(Context context, String userId) {
        getSharedPreferences(context).edit()
                .putString(PREF_USER_ID, userId)
                .apply();
    }

    /**
     * Get the current user display name
     *
     * @param context Application context
     * @return User name or empty string if not set
     */
    public static String getUserName(Context context) {
        return getSharedPreferences(context).getString(PREF_USER_NAME, "");
    }

    /**
     * Set the current user display name
     *
     * @param context Application context
     * @param name User name to set
     */
    public static void setUserName(Context context, String name) {
        getSharedPreferences(context).edit()
                .putString(PREF_USER_NAME, name)
                .apply();
    }

    /**
     * Check if push notifications are enabled
     *
     * @param context Application context
     * @return true if push notifications are enabled, false otherwise
     */
    public static boolean arePushNotificationsEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_PUSH_NOTIFICATIONS, true);
    }

    /**
     * Set push notifications enabled state
     *
     * @param context Application context
     * @param enabled Whether push notifications should be enabled
     */
    public static void setPushNotificationsEnabled(Context context, boolean enabled) {
        getSharedPreferences(context).edit()
                .putBoolean(PREF_PUSH_NOTIFICATIONS, enabled)
                .apply();
    }

    /**
     * Check if message preview in notifications is enabled
     *
     * @param context Application context
     * @return true if message preview is enabled, false otherwise
     */
    public static boolean isMessagePreviewEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_MESSAGE_PREVIEW, true);
    }

    /**
     * Set message preview enabled state
     *
     * @param context Application context
     * @param enabled Whether message preview should be enabled
     */
    public static void setMessagePreviewEnabled(Context context, boolean enabled) {
        getSharedPreferences(context).edit()
                .putBoolean(PREF_MESSAGE_PREVIEW, enabled)
                .apply();
    }

    /**
     * Get the current app theme
     *
     * @param context Application context
     * @return Theme value (THEME_LIGHT, THEME_DARK, or THEME_SYSTEM)
     */
    public static String getTheme(Context context) {
        return getSharedPreferences(context).getString(PREF_THEME, THEME_SYSTEM);
    }

    /**
     * Set the app theme
     *
     * @param context Application context
     * @param theme Theme to set
     */
    public static void setTheme(Context context, String theme) {
        getSharedPreferences(context).edit()
                .putString(PREF_THEME, theme)
                .apply();
    }

    /**
     * Get the current font size setting
     *
     * @param context Application context
     * @return Font size value (FONT_SIZE_SMALL, FONT_SIZE_MEDIUM, or FONT_SIZE_LARGE)
     */
    public static String getFontSize(Context context) {
        return getSharedPreferences(context).getString(PREF_FONT_SIZE, FONT_SIZE_MEDIUM);
    }

    /**
     * Set the font size
     *
     * @param context Application context
     * @param fontSize Font size to set
     */
    public static void setFontSize(Context context, String fontSize) {
        getSharedPreferences(context).edit()
                .putString(PREF_FONT_SIZE, fontSize)
                .apply();
    }

    /**
     * Get the auto-download media setting
     *
     * @param context Application context
     * @return Auto-download value (AUTO_DOWNLOAD_NEVER, AUTO_DOWNLOAD_WIFI, or AUTO_DOWNLOAD_ALWAYS)
     */
    public static String getAutoDownloadMedia(Context context) {
        return getSharedPreferences(context).getString(PREF_AUTO_DOWNLOAD_MEDIA, AUTO_DOWNLOAD_WIFI);
    }

    /**
     * Set the auto-download media setting
     *
     * @param context Application context
     * @param autoDownload Auto-download setting to set
     */
    public static void setAutoDownloadMedia(Context context, String autoDownload) {
        getSharedPreferences(context).edit()
                .putString(PREF_AUTO_DOWNLOAD_MEDIA, autoDownload)
                .apply();
    }

    /**
     * Clear all preferences (for logout)
     *
     * @param context Application context
     */
    public static void clearAll(Context context) {
        // Preserve some settings that should remain after logout
        String theme = getTheme(context);
        String fontSize = getFontSize(context);
        String autoDownload = getAutoDownloadMedia(context);
        boolean pushNotifications = arePushNotificationsEnabled(context);
        boolean messagePreview = isMessagePreviewEnabled(context);

        // Clear all preferences
        getSharedPreferences(context).edit().clear().apply();

        // Restore settings that should remain
        setTheme(context, theme);
        setFontSize(context, fontSize);
        setAutoDownloadMedia(context, autoDownload);
        setPushNotificationsEnabled(context, pushNotifications);
        setMessagePreviewEnabled(context, messagePreview);
    }
}