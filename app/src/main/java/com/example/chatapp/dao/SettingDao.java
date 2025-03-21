package com.example.chatapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chatapp.models.sqlite.Setting;

import java.util.Date;
import java.util.List;

@Dao
public interface SettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Setting setting);

    @Update
    void update(Setting setting);

    @Delete
    void delete(Setting setting);

    @Query("SELECT * FROM settings WHERE settingKey = :key AND userId = :userId")
    Setting getSetting(String key, String userId);

    @Query("SELECT * FROM settings WHERE userId = :userId")
    List<Setting> getSettingsForUser(String userId);

    @Query("UPDATE settings SET settingValue = :value, updatedAt = :updatedAt WHERE settingKey = :key AND userId = :userId")
    void updateSettingValue(String key, String userId, String value, Date updatedAt);

    @Query("DELETE FROM settings WHERE settingKey = :key AND userId = :userId")
    void deleteSetting(String key, String userId);

    @Query("DELETE FROM settings WHERE userId = :userId")
    void deleteAllSettingsForUser(String userId);
}