package com.example.quan_ly_du_an.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.quan_ly_du_an.model.User;

import androidx.room.Update;

@Dao
public interface UserDao {
    @Insert
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    void updatePassword(int userId, String newPassword);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    // THÊM HÀM NÀY ĐỂ TÌM USER THEO ID ĐANG ĐĂNG NHẬP
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    @Query("SELECT COUNT(*) FROM users")
    int getTotalUsersCount();

    @Query("SELECT * FROM users ORDER BY id DESC")
    java.util.List<User> getAllUsers();
}
