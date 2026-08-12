package com.example.quan_ly_du_an.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quan_ly_du_an.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final SQLiteOpenHelper dbHelper;

    public UserDao(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (user.getId() > 0) {
            cv.put("id", user.getId());
        }
        cv.put("name", user.getName());
        cv.put("email", user.getEmail());
        cv.put("password", user.getPassword());
        cv.put("avatar", user.getAvatar());
        cv.put("role", user.getRole());
        long id = db.insertWithOnConflict("users", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        if (id != -1 && user.getId() == 0) {
            user.setId((int) id);
        }
    }

    public User login(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ? LIMIT 1",
                new String[]{email, password});
        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
            cursor.close();
        }
        return user;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ? LIMIT 1",
                new String[]{email});
        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
            cursor.close();
        }
        return user;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE id = ? LIMIT 1",
                new String[]{String.valueOf(userId)});
        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
            cursor.close();
        }
        return user;
    }

    public void updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        db.update("users", cv, "id = ?", new String[]{String.valueOf(userId)});
    }

    public void updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", user.getName());
        cv.put("email", user.getEmail());
        if (user.getPassword() != null) {
            cv.put("password", user.getPassword());
        }
        cv.put("avatar", user.getAvatar());
        cv.put("role", user.getRole());
        db.update("users", cv, "id = ?", new String[]{String.valueOf(user.getId())});
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToUser(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public int getTotalUsersCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM users", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    private User cursorToUser(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
        String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
        User user = new User(name, email, password);
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow("avatar")));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        return user;
    }
}
