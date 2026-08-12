package com.example.quan_ly_du_an.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DAO cho tìm kiếm comments.
 * Giữ nguyên tên method cũ từ Room interface.
 */
public class SearchDao {

    private final SQLiteOpenHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SearchDao(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public LiveData<List<Comment>> searchComments(String query) {
        MutableLiveData<List<Comment>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<Comment> comments = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM comments WHERE content LIKE '%' || ? || '%'",
                    new String[]{query});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Comment comment = new Comment();
                    comment.commentId = cursor.getInt(cursor.getColumnIndexOrThrow("commentId"));
                    comment.taskId = cursor.getInt(cursor.getColumnIndexOrThrow("taskId"));
                    comment.userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId"));
                    comment.content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                    comment.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
                    comments.add(comment);
                }
                cursor.close();
            }
            liveData.postValue(comments);
        });
        return liveData;
    }
}
