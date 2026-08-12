package com.example.quan_ly_du_an.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quan_ly_du_an.model.MemberWithRole;
import com.example.quan_ly_du_an.model.ProjectMember;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng project_member_cross_ref.
 * Giữ nguyên tên method cũ từ Room interface.
 */
public class ProjectMemberDao {

    private final SQLiteOpenHelper dbHelper;

    public ProjectMemberDao(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertProjectMember(ProjectMember projectMember) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("project_id", projectMember.getProjectId());
        cv.put("user_id", projectMember.getUserId());
        cv.put("role", projectMember.getRole());
        return db.insertWithOnConflict("project_member_cross_ref", null, cv,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int removeMember(long projectId, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("project_member_cross_ref",
                "project_id = ? AND user_id = ?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)});
    }

    public List<MemberWithRole> getMembersByProjectId(long projectId) {
        List<MemberWithRole> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT u.id AS userId, u.name, u.email, '' AS avatarUrl, m.role, 0 AS taskCount " +
                "FROM users u " +
                "INNER JOIN project_member_cross_ref m ON u.id = m.user_id " +
                "WHERE m.project_id = ? " +
                "ORDER BY CASE m.role WHEN 'ADMIN' THEN 1 WHEN 'LEADER' THEN 2 ELSE 3 END",
                new String[]{String.valueOf(projectId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long userId = cursor.getLong(cursor.getColumnIndexOrThrow("userId"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow("avatarUrl"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                MemberWithRole member = new MemberWithRole(userId, name, email, avatarUrl, role);
                member.taskCount = cursor.getInt(cursor.getColumnIndexOrThrow("taskCount"));
                list.add(member);
            }
            cursor.close();
        }
        return list;
    }

    public boolean isMemberAlreadyInProject(long projectId, String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT EXISTS(SELECT 1 FROM project_member_cross_ref m " +
                "JOIN users u ON m.user_id = u.id " +
                "WHERE m.project_id = ? AND u.email = ?)",
                new String[]{String.valueOf(projectId), email});
        boolean exists = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                exists = cursor.getInt(0) == 1;
            }
            cursor.close();
        }
        return exists;
    }

    public String getUserRoleSync(long projectId, long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT role FROM project_member_cross_ref WHERE project_id = ? AND user_id = ?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)});
        String role = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                role = cursor.getString(0);
            }
            cursor.close();
        }
        return role;
    }
}
