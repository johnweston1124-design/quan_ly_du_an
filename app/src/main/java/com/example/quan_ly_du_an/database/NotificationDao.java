package com.example.quan_ly_du_an.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.quan_ly_du_an.model.NotificationHistory;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {
    private final SQLiteOpenHelper dbHelper;

    public NotificationDao(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insert(NotificationHistory notification) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", notification.getTitle());
        cv.put("content", notification.getContent());
        cv.put("timestamp", notification.getTimestamp());
        db.insert("notifications", null, cv);
    }

    public List<NotificationHistory> getAllSync() {
        List<NotificationHistory> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM notifications ORDER BY timestamp DESC", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                NotificationHistory n = new NotificationHistory(
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                );
                list.add(n);
            }
            cursor.close();
        }
        return list;
    }

    public void deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("notifications", null, null);
    }
}