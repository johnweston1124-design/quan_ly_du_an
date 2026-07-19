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

    // 1. ADVANCED QUERY: Lấy danh sách dự án mà User đang tham gia kèm Quyền (INNER JOIN)
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

    // 2. Lấy danh sách thành viên trong 1 dự án (INNER JOIN)
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
                        "", // avatarUrl để trống
                        cursor.getString(cursor.getColumnIndexOrThrow("role"))
                ));
            }
            cursor.close();
        }
        return list;
    }

    // 3. Thêm thành viên bằng Email (Kiểm tra user có trong system chưa)
    public boolean addMemberByEmail(long projectId, String email, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Tìm user theo email
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ?", new String[]{email});
        long userId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
        } else {
            // Nếu chưa có, tạo user tạm để test
            if (cursor != null) cursor.close();
            ContentValues userVals = new ContentValues();
            userVals.put("name", email.substring(0, email.indexOf('@')));
            userVals.put("email", email);
            userId = db.insert("users", null, userVals);
        }

        if (userId != -1) {
            ContentValues values = new ContentValues();
            values.put(MemberContract.COLUMN_PROJECT_ID, projectId);
            values.put(MemberContract.COLUMN_USER_ID, userId);
            values.put(MemberContract.COLUMN_ROLE, role);

            long res = db.insertWithOnConflict(MemberContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            return res != -1;
        }
        return false;
    }

    // 4. Xóa thành viên
    public void removeMember(long projectId, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(MemberContract.TABLE_NAME,
                MemberContract.COLUMN_PROJECT_ID + "=? AND " + MemberContract.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)});
    }

    // 5. Lấy quyền hiện tại của User
    public String getUserRoleSync(long projectId, long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String role = "MEMBER";
        Cursor cursor = db.rawQuery("SELECT role FROM project_member_cross_ref WHERE project_id=? AND user_id=?",
                new String[]{String.valueOf(projectId), String.valueOf(userId)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                role = cursor.getString(0);
            }
            cursor.close();
        }
        return role;
    }

    // 6. Tạo dự án mới (Phục vụ nút FAB ở màn hình Home)
    public void createProject(String title, String desc, long creatorUserId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues vals = new ContentValues();
        vals.put("title", title);
        vals.put("description", desc);
        vals.put("status", "Đang thực hiện");
        long pId = db.insert("projects", null, vals);

        if (pId != -1) {
            ContentValues memVals = new ContentValues();
            memVals.put(MemberContract.COLUMN_PROJECT_ID, pId);
            memVals.put(MemberContract.COLUMN_USER_ID, creatorUserId);
            memVals.put(MemberContract.COLUMN_ROLE, "ADMIN");
            db.insert(MemberContract.TABLE_NAME, null, memVals);
        }
    }
}