package com.example.chatapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.chatapp.models.sqlite.User;

import java.util.Date;
import java.util.List;

@Dao
public interface UserDao {
    // getUserCount - Lấy số lượng người dùng trong cơ sở dữ liệu
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE userId = :userId")
    User getUserById(String userId);

    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE username LIKE '%' || :searchQuery || '%' OR displayName LIKE '%' || :searchQuery || '%'")
    List<User> searchUsers(String searchQuery);

    @Query("UPDATE users SET status = :status, lastOnline = :lastOnline WHERE userId = :userId")
    void updateUserStatus(String userId, String status, Date lastOnline);
}