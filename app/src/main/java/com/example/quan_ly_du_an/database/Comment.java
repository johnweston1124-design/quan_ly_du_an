package com.example.quan_ly_du_an.database;

public class Comment {
    public int commentId;
    public int taskId;
    public int userId;
    public String content;
    public long timestamp = System.currentTimeMillis();
}
