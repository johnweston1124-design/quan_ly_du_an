package com.example.quan_ly_du_an.database;

/**
 * Entity Comment — POJO thuần (không còn Room annotations).
 * Tương ứng bảng "comments" trong SQLite.
 */
public class Comment {
    public int commentId;
    public int taskId;
    public int userId;
    public String content;
    public long timestamp = System.currentTimeMillis();
}
