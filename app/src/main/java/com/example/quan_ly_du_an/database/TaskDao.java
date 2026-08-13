package com.example.quan_ly_du_an.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quan_ly_du_an.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskDao {

    private final SQLiteOpenHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TaskDao(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insert(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("projectId", task.getProjectId());
        cv.put("assignedUserId", task.getAssignedUserId());
        cv.put("title", task.getTitle());
        cv.put("description", task.getDescription());
        cv.put("priority", task.getPriority());
        cv.put("status", task.getStatus());
        cv.put("deadline", task.getDeadline());
        long id = db.insert("tasks", null, cv);
        if (id != -1) {
            task.setTaskId((int) id);
        }
    }

    public void update(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("projectId", task.getProjectId());
        cv.put("assignedUserId", task.getAssignedUserId());
        cv.put("title", task.getTitle());
        cv.put("description", task.getDescription());
        cv.put("priority", task.getPriority());
        cv.put("status", task.getStatus());
        cv.put("deadline", task.getDeadline());
        db.update("tasks", cv, "taskId = ?", new String[]{String.valueOf(task.getTaskId())});
    }

    public void delete(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("tasks", "taskId = ?", new String[]{String.valueOf(task.getTaskId())});
    }

    public LiveData<List<Task>> getAllTasks() {
        MutableLiveData<List<Task>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<Task> list = queryTaskList("SELECT * FROM tasks ORDER BY taskId DESC", null);
            liveData.postValue(list);
        });
        return liveData;
    }

    public LiveData<List<Task>> getAllTasksForUser(int userId) {
        MutableLiveData<List<Task>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<Task> list = queryTaskList(
                    "SELECT * FROM tasks WHERE assignedUserId = ? ORDER BY taskId DESC",
                    new String[]{String.valueOf(userId)});
            liveData.postValue(list);
        });
        return liveData;
    }

    public Task getTaskById(int taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tasks WHERE taskId = ? LIMIT 1",
                new String[]{String.valueOf(taskId)});
        Task task = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                task = cursorToTask(cursor);
            }
            cursor.close();
        }
        return task;
    }

    public LiveData<List<Task>> searchTasks(String query) {
        MutableLiveData<List<Task>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            String wildcard = "%" + query + "%";
            List<Task> list = queryTaskList(
                    "SELECT * FROM tasks WHERE title LIKE ? OR description LIKE ? ORDER BY taskId DESC",
                    new String[]{wildcard, wildcard});
            liveData.postValue(list);
        });
        return liveData;
    }

    public LiveData<List<Task>> searchTasksForUser(int userId, String query) {
        MutableLiveData<List<Task>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            String wildcard = "%" + query + "%";
            List<Task> list = queryTaskList(
                    "SELECT * FROM tasks WHERE (assignedUserId = ?) AND (title LIKE ? OR description LIKE ?) ORDER BY taskId DESC",
                    new String[]{String.valueOf(userId), wildcard, wildcard});
            liveData.postValue(list);
        });
        return liveData;
    }

    // ==========================================
    // ĐÂY LÀ HÀM TÌM KIẾM THỜI GIAN THỰC
    // ==========================================
    public List<Task> searchTasksForUserSync(int userId, String query) {
        String wildcard = "%" + query + "%";
        return queryTaskList(
                "SELECT * FROM tasks WHERE (assignedUserId = ?) AND (title LIKE ? OR description LIKE ?) ORDER BY taskId DESC",
                new String[]{String.valueOf(userId), wildcard, wildcard});
    }

    // ==========================================
    // ĐÂY LÀ HÀM MỚI CHO DEADLINE WORKER CHẠY NGẦM
    // ==========================================
    public List<Task> getAllTasksForUserSync(int userId) {
        return queryTaskList(
                "SELECT * FROM tasks WHERE assignedUserId = ? ORDER BY taskId DESC",
                new String[]{String.valueOf(userId)});
    }

    public LiveData<List<Task>> getLatestTasks(int limit) {
        MutableLiveData<List<Task>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<Task> list = queryTaskList(
                    "SELECT * FROM tasks ORDER BY taskId DESC LIMIT " + limit,
                    null);
            liveData.postValue(list);
        });
        return liveData;
    }

    public LiveData<List<Task>> getLatestTasksForUser(int userId, int limit) {
        MutableLiveData<List<Task>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            List<Task> list = queryTaskList(
                    "SELECT * FROM tasks WHERE assignedUserId = ? ORDER BY taskId DESC LIMIT " + limit,
                    new String[]{String.valueOf(userId)});
            liveData.postValue(list);
        });
        return liveData;
    }

    public int getTotalTasksCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tasks", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    private List<Task> queryTaskList(String sql, String[] args) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToTask(cursor));
            }
            cursor.close();
        }
        return list;
    }

    private Task cursorToTask(Cursor cursor) {
        Task task = new Task();
        task.setTaskId(cursor.getInt(cursor.getColumnIndexOrThrow("taskId")));
        task.setProjectId(cursor.getInt(cursor.getColumnIndexOrThrow("projectId")));
        task.setAssignedUserId(cursor.getInt(cursor.getColumnIndexOrThrow("assignedUserId")));
        task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow("priority")));
        task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        task.setDeadline(cursor.getString(cursor.getColumnIndexOrThrow("deadline")));
        return task;
    }
}