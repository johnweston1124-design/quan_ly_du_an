package com.example.quan_ly_du_an.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class History {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private String timestamp;
    private int userId;

    public History(String title, String description, String timestamp, int userId) {
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
