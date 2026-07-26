package com.example.quan_ly_du_an.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.quan_ly_du_an.model.Task;
import com.example.quan_ly_du_an.model.User;

import com.example.quan_ly_du_an.model.Project;
import com.example.quan_ly_du_an.model.ProjectMember;

@Database(entities = {Task.class, User.class, Comment.class, Project.class, ProjectMember.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TaskDao taskDao();
    public abstract UserDao userDao();
    public abstract CommentDao commentDao();
    public abstract ProjectDao projectDao();
    public abstract ProjectMemberDao projectMemberDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "task_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
