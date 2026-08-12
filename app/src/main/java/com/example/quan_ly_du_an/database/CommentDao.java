package com.example.quan_ly_du_an.database;

import android.content.ContentValues;
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
 * DAO cho bảng comments.
 * Giữ nguyên tên method cũ từ Room interface.
 */
public class CommentDao {

    private final SQLiteOpenHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CommentDao(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insertComment(Comment comment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("taskId", comment.taskId);
        cv.put("userId", comment.userId);
        cv.put("content", comment.content);
        cv.put("timestamp", comment.timestamp);
        long id = db.insert("comments", null, cv);
        if (id != -1) {
            comment.commentId = (int) id;
        }
    }

    public LiveData<List<Comment>> getCommentsByTaskId(int taskId) {
        MutableLiveData<List<Comment>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<Comment> comments = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM comments WHERE taskId = ? ORDER BY timestamp ASC",
                    new String[]{String.valueOf(taskId)});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    comments.add(cursorToComment(cursor));
                }
                cursor.close();
            }
            liveData.postValue(comments);
        });
        return liveData;
    }

    // --- Helper methods ---

    private Comment cursorToComment(Cursor cursor) {
        Comment comment = new Comment();
        comment.commentId = cursor.getInt(cursor.getColumnIndexOrThrow("commentId"));
        comment.taskId = cursor.getInt(cursor.getColumnIndexOrThrow("taskId"));
        comment.userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId"));
        comment.content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
        comment.timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
        return comment;
    }
}
