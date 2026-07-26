package com.example.quan_ly_du_an.feature_project.data.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.quan_ly_du_an.feature_project.data.model.MemberWithRole;
import com.example.quan_ly_du_an.feature_project.data.model.ProjectWithRole;
import java.util.ArrayList;
import java.util.List;

public class MemberDaoSqlite {
    private final AppDbHelper dbHelper;

    public MemberDaoSqlite(AppDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // 1. Lấy danh sách dự án của User (Có lọc/Sắp xếp mặc định)
    public List<ProjectWithRole> getProjectsForUser(long userId) {
        List<ProjectWithRole> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT p.project_id, p.title, p.description, p.status, m.role " +
                "FROM projects p " +
                "INNER JOIN project_member_cross_ref m ON p.project_id = m.project_id " +
                "WHERE m.user_id = ? " +
                "ORDER BY p.project_id DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(new ProjectWithRole(
                        cursor.getLong(cursor.getColumnIndexOrThrow("project_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role"))
                ));
            }
            cursor.close();
        }
        return list;
    }

    // 2. Tìm kiếm dự án (Đầu ra cho Phân công 4 - Search)
    public List<ProjectWithRole> searchProjects(long userId, String keyword) {
        List<ProjectWithRole> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT p.project_id, p.title, p.description, p.status, m.role " +
                "FROM projects p " +
                "INNER JOIN project_member_cross_ref m ON p.project_id = m.project_id " +
                "WHERE m.user_id = ? AND (p.title LIKE ? OR p.description LIKE ?)";

        String wildCard = "%" + keyword + "%";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), wildCard, wildCard});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(new ProjectWithRole(
                        cursor.getLong(cursor.getColumnIndexOrThrow("project_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role"))
                ));
            }
            cursor.close();
        }
        return list;
    }

    // 3. Quản lý thành viên
    public List<MemberWithRole> getMembersByProjectId(long projectId) {
        List<MemberWithRole> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT u.id, u.name, u.email, m.role " +
                "FROM users u " +
                "INNER JOIN project_member_cross_ref m ON u.id = m.user_id " +
                "WHERE m.project_id = ? " +
                "ORDER BY CASE m.role WHEN 'ADMIN' THEN 1 WHEN 'LEADER' THEN 2 ELSE 3 END";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(new MemberWithRole(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        "",
                        cursor.getString(cursor.getColumnIndexOrThrow("role"))
                ));
            }
            cursor.close();
        }
        return list;
    }

    public boolean isMemberAlreadyInProject(long projectId, String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT 1 FROM project_member_cross_ref m " +
                "JOIN users u ON m.user_id = u.id " +
                "WHERE m.project_id = ? AND u.email = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId), email});
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public long addMemberByEmail(long projectId, String email, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ?", new String[]{email});
        long userId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getLong(0);
            cursor.close();
        }
        if (userId == -1) return -2; // Mã lỗi: User không tồn tại trong hệ thống

        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        values.put("user_id", userId);
        values.put("role", role);
        return db.insertWithOnConflict("project_member_cross_ref", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void removeMember(long projectId, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("project_member_cross_ref", "project_id=? AND user_id=?", 
                new String[]{String.valueOf(projectId), String.valueOf(userId)});
    }

    // 4. CRUD Dự án
    public long createProject(String title, String desc, String status, String startDate, String endDate, int expectedMembers, long creatorUserId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues vals = new ContentValues();
        vals.put("title", title);
        vals.put("description", desc);
        vals.put("status", status != null ? status : "Đang thực hiện");
        vals.put("start_date", startDate != null ? startDate : "");
        vals.put("end_date", endDate != null ? endDate : "");
        vals.put("expected_members", expectedMembers);
        long pId = db.insert("projects", null, vals);
        if (pId != -1) {
            ContentValues memVals = new ContentValues();
            memVals.put("project_id", pId);
            memVals.put("user_id", creatorUserId);
            memVals.put("role", "ADMIN");
            db.insert("project_member_cross_ref", null, memVals);
        }
        return pId;
    }

    public boolean updateProject(long projectId, String title, String desc, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues vals = new ContentValues();
        vals.put("title", title);
        vals.put("description", desc);
        vals.put("status", status);
        return db.update("projects", vals, "project_id=?", new String[]{String.valueOf(projectId)}) > 0;
    }

    public boolean deleteProject(long projectId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("project_member_cross_ref", "project_id=?", new String[]{String.valueOf(projectId)});
        return db.delete("projects", "project_id=?", new String[]{String.valueOf(projectId)}) > 0;
    }

    public String getUserRoleSync(long projectId, long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String role = "MEMBER";
        Cursor cursor = db.rawQuery("SELECT role FROM project_member_cross_ref WHERE project_id=? AND user_id=?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) role = cursor.getString(0);
            cursor.close();
        }
        return role;
    }
}
