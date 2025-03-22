package com.example.chatapp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for date and time operations.
 */
public class DateUtils {
    private static final String TAG = "DateUtils";

    private static final SimpleDateFormat FILE_DATE_FORMAT =
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    private DateUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Get a date that is a specified number of days before the reference date
     *
     * @param referenceDate The reference date
     * @param daysBefore Number of days to subtract
     * @return Date object representing the calculated date
     */
    public static Date getDateBefore(Date referenceDate, int daysBefore) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(referenceDate);
        calendar.add(Calendar.DAY_OF_YEAR, -daysBefore);
        return calendar.getTime();
    }

    /**
     * Format a date for use in filenames (without special characters)
     *
     * @param date The date to format
     * @return Formatted date string
     */
    public static String formatDateForFilename(Date date) {
        return FILE_DATE_FORMAT.format(date);
    }

    /**
     * Get a user-friendly relative time string (e.g., "2 hours ago", "Yesterday")
     *
     * @param date The date to format
     * @return Formatted relative time string
     */
    public static String getRelativeTimeSpan(Date date) {
        long timeMillis = date.getTime();
        long now = System.currentTimeMillis();
        long diff = now - timeMillis;

        // Less than a minute
        if (diff < 60 * 1000) {
            return "Just now";
        }

        // Less than an hour
        if (diff < 60 * 60 * 1000) {
            int minutes = (int) (diff / (60 * 1000));
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }

        // Less than a day
        if (diff < 24 * 60 * 60 * 1000) {
            int hours = (int) (diff / (60 * 60 * 1000));
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }

        // Less than 2 days
        if (diff < 2 * 24 * 60 * 60 * 1000) {
            return "Yesterday";
        }

        // Less than a week
        if (diff < 7 * 24 * 60 * 60 * 1000) {
            int days = (int) (diff / (24 * 60 * 60 * 1000));
            return days + " days ago";
        }

        // Otherwise, return a simple date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Check if the provided date is today
     *
     * @param date The date to check
     * @return true if the date is today, false otherwise
     */
    public static boolean isToday(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        then.setTime(date);

        return now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Format time for display in chat messages
     *
     * @param date The date to format
     * @return Time string in appropriate format
     */
    public static String formatMessageTime(Date date) {
        SimpleDateFormat format;

        if (isToday(date)) {
            // Today, just show time
            format = new SimpleDateFormat("h:mm a", Locale.getDefault());
        } else {
            // Not today, show date and time
            format = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        }

        return format.format(date);
    }

    /**
     * Get current date in UTC
     *
     * @return Current UTC date
     */
    public static Date getCurrentUtcDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.getTime();
    }
}