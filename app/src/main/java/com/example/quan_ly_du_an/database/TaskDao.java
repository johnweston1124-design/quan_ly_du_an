package com.example.quan_ly_du_an.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.quan_ly_du_an.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM tasks ORDER BY taskId DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE assignedUserId = :userId ORDER BY taskId DESC")
    LiveData<List<Task>> getAllTasksForUser(int userId);

    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    Task getTaskById(int taskId);

    @Query("SELECT * FROM tasks WHERE title LIKE :query OR description LIKE :query")
    LiveData<List<Task>> searchTasks(String query);

    @Query("SELECT * FROM tasks WHERE (assignedUserId = :userId) AND (title LIKE :query OR description LIKE :query)")
    LiveData<List<Task>> searchTasksForUser(int userId, String query);

    @Query("SELECT * FROM tasks ORDER BY taskId DESC LIMIT :limit")
    LiveData<List<Task>> getLatestTasks(int limit);

    @Query("SELECT * FROM tasks WHERE assignedUserId = :userId ORDER BY taskId DESC LIMIT :limit")
    LiveData<List<Task>> getLatestTasksForUser(int userId, int limit);

    @Query("SELECT COUNT(*) FROM tasks")
    int getTotalTasksCount();
}
