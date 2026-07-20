package com.example.quan_ly_du_an.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Comment.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CommentDao commentDao();
    public abstract SearchDao searchDao();

}