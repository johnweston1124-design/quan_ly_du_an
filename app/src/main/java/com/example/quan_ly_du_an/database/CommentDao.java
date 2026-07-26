package com.example.quan_ly_du_an.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommentDao {
    @Insert
    void insertComment(Comment comment);
    @Query("SELECT * FROM comments WHERE taskId = :taskId ORDER BY timestamp ASC")
    LiveData<List<Comment>> getCommentsByTaskId(int taskId);
}
