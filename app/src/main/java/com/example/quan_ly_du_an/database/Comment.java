package com.example.quan_ly_du_an.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments")
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public int commentId;
    public int taskId;
    public int userId;
    public String content;
    public long timestamp = System.currentTimeMillis();
}