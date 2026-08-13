package com.example.quan_ly_du_an.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quan_ly_du_an.model.History;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryDao {

    private final SQLiteOpenHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<Integer, MutableLiveData<List<History>>> liveDataMap = new HashMap<>();

    public HistoryDao(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insert(History history) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", history.getTitle());
        cv.put("description", history.getDescription());
        cv.put("timestamp", history.getTimestamp());
        cv.put("userId", history.getUserId());
        long id = db.insert("history", null, cv);
        if (id != -1) {
            history.setId((int) id);
            refreshHistory(history.getUserId());
        }
    }

    public LiveData<List<History>> getAllHistoryForUser(int userId) {
        if (!liveDataMap.containsKey(userId)) {
            liveDataMap.put(userId, new MutableLiveData<>());
        }
        refreshHistory(userId);
        return liveDataMap.get(userId);
    }

    private void refreshHistory(int userId) {
        executor.execute(() -> {
            List<History> list = new ArrayList<>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM history WHERE userId = ? ORDER BY id DESC",
                    new String[]{String.valueOf(userId)});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(cursorToHistory(cursor));
                }
                cursor.close();
            }
            MutableLiveData<List<History>> liveData = liveDataMap.get(userId);
            if (liveData != null) {
                liveData.postValue(list);
            }
        });
    }

    private History cursorToHistory(Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
        String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
        int userId = cursor.getInt(cursor.getColumnIndexOrThrow("userId"));
        History history = new History(title, description, timestamp, userId);
        history.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        return history;
    }
}
