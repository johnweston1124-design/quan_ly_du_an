package com.example.quan_ly_du_an.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quan_ly_du_an.model.Project;
import com.example.quan_ly_du_an.model.ProjectWithRole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectDao {

    private final SQLiteOpenHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ProjectDao(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertProject(Project project) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", project.getTitle());
        cv.put("description", project.getDescription());
        cv.put("status", project.getStatus());
        cv.put("start_date", project.getStartDate());
        cv.put("end_date", project.getEndDate());
        cv.put("expected_members", project.getExpectedMembers());
        long id = db.insertWithOnConflict("projects", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        if (id != -1) {
            project.setProjectId(id);
        }
        return id;
    }

    public int updateProject(Project project) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", project.getTitle());
        cv.put("description", project.getDescription());
        cv.put("status", project.getStatus());
        cv.put("start_date", project.getStartDate());
        cv.put("end_date", project.getEndDate());
        cv.put("expected_members", project.getExpectedMembers());
        return db.update("projects", cv, "project_id = ?",
                new String[]{String.valueOf(project.getProjectId())});
    }

    public int deleteProject(long projectId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("projects", "project_id = ?",
                new String[]{String.valueOf(projectId)});
    }

    public List<ProjectWithRole> getProjectsForUser(long userId) {
        String sql = "SELECT p.project_id AS projectId, p.title, p.description, p.status, m.role " +
                     "FROM projects p " +
                     "INNER JOIN project_member_cross_ref m ON p.project_id = m.project_id " +
                     "WHERE m.user_id = ? " +
                     "ORDER BY p.project_id DESC";
        return queryProjectWithRoleList(sql, new String[]{String.valueOf(userId)});
    }

    public List<ProjectWithRole> getAllProjectsForAdmin() {
        String sql = "SELECT p.project_id AS projectId, p.title, p.description, p.status, 'ADMIN' AS role " +
                     "FROM projects p " +
                     "ORDER BY p.project_id DESC";
        return queryProjectWithRoleList(sql, null);
    }

    public List<ProjectWithRole> searchProjects(long userId, String keyword) {
        String sql = "SELECT p.project_id AS projectId, p.title, p.description, p.status, m.role " +
                     "FROM projects p " +
                     "INNER JOIN project_member_cross_ref m ON p.project_id = m.project_id " +
                     "WHERE m.user_id = ? AND (p.title LIKE ? OR p.description LIKE ?) " +
                     "ORDER BY p.project_id DESC";
        return queryProjectWithRoleList(sql, new String[]{String.valueOf(userId), keyword, keyword});
    }

    public LiveData<List<ProjectWithRole>> getLatestProjectsForUser(long userId, int limit) {
        MutableLiveData<List<ProjectWithRole>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            String sql = "SELECT p.project_id AS projectId, p.title, p.description, p.status, m.role " +
                         "FROM projects p " +
                         "INNER JOIN project_member_cross_ref m ON p.project_id = m.project_id " +
                         "WHERE m.user_id = ? " +
                         "ORDER BY p.project_id DESC LIMIT " + limit;
            List<ProjectWithRole> list = queryProjectWithRoleList(sql, new String[]{String.valueOf(userId)});
            liveData.postValue(list);
        });
        return liveData;
    }

    public LiveData<List<ProjectWithRole>> getAllLatestProjectsForAdmin(int limit) {
        MutableLiveData<List<ProjectWithRole>> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            String sql = "SELECT p.project_id AS projectId, p.title, p.description, p.status, 'ADMIN' AS role " +
                         "FROM projects p " +
                         "ORDER BY p.project_id DESC LIMIT " + limit;
            List<ProjectWithRole> list = queryProjectWithRoleList(sql, null);
            liveData.postValue(list);
        });
        return liveData;
    }

    public int getTotalProjectsCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM projects", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    private List<ProjectWithRole> queryProjectWithRoleList(String sql, String[] args) {
        List<ProjectWithRole> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long projectId = cursor.getLong(cursor.getColumnIndexOrThrow("projectId"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                list.add(new ProjectWithRole(projectId, title, description, status, role));
            }
            cursor.close();
        }
        return list;
    }
}
