package com.example.quan_ly_du_an.feature_project.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.quan_ly_du_an.utils.Constants;
import com.example.quan_ly_du_an.utils.RoleEnum;

public class AppDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "project_manager_team.db";
    private static final int DATABASE_VERSION = 2;

    private static volatile AppDbHelper INSTANCE;

    public static synchronized AppDbHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppDbHelper(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. TẠO BẢNG CỦA BẠN (QUẢN LÝ THÀNH VIÊN & PHÂN QUYỀN)
        MemberContract.createTable(db);

        // -------------------------------------------------------------------------
        // 2. [CHỖ CỦA ĐỒNG ĐỘI]: Hiện tại tạo tạm bảng Project & User để bạn test UI
        // Khi merge code, hãy xóa 2 lệnh execSQL này và thay bằng:
        // UserContract.createTable(db);
        // ProjectContract.createTable(db);
        // -------------------------------------------------------------------------
        db.execSQL("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, email TEXT UNIQUE);");
        db.execSQL("CREATE TABLE IF NOT EXISTS projects (project_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, description TEXT, status TEXT, start_date TEXT, end_date TEXT, expected_members INTEGER DEFAULT 1);");

        // 3. Tự động chèn dữ liệu mẫu để chạy app thấy ngay giao diện
        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MemberContract.upgradeTable(db);
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS projects");
        onCreate(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // Tạo User mẫu (Bạn là ID = 1)
        db.execSQL("INSERT INTO users (id, name, email) VALUES (1, 'Nguyễn Văn A (Bạn)', 'a.nguyen@email.com');");
        db.execSQL("INSERT INTO users (id, name, email) VALUES (2, 'Trần Thị Bích (Designer)', 'bich@email.com');");
        db.execSQL("INSERT INTO users (id, name, email) VALUES (3, 'Lê Hoàng Long (Dev)', 'long@email.com');");

        // Tạo Project mẫu
        db.execSQL("INSERT INTO projects (project_id, title, description, status) VALUES (101, 'Thiết Kế App Fitness AI', 'Nâng cấp UI/UX Pastel hiện đại', 'Đang thực hiện');");
        db.execSQL("INSERT INTO projects (project_id, title, description, status) VALUES (102, 'Hệ Thống Quản Lý Nội Bộ', 'Module Phân quyền & Thành viên SQLite', 'Đang thực hiện');");

        // Gán quyền vào bảng CỦA BẠN (MemberContract)
        // Bạn (ID=1) là ADMIN dự án 101, nhưng chỉ là MEMBER dự án 102
        insertMemberSeed(db, 101, 1, RoleEnum.ADMIN.getRoleName());
        insertMemberSeed(db, 101, 2, RoleEnum.LEADER.getRoleName());
        insertMemberSeed(db, 101, 3, RoleEnum.MEMBER.getRoleName());

        insertMemberSeed(db, 102, 1, RoleEnum.MEMBER.getRoleName()); // Test ẩn nút (+)
        insertMemberSeed(db, 102, 2, RoleEnum.ADMIN.getRoleName());
    }

    private void insertMemberSeed(SQLiteDatabase db, long pId, long uId, String role) {
        ContentValues values = new ContentValues();
        values.put(MemberContract.COLUMN_PROJECT_ID, pId);
        values.put(MemberContract.COLUMN_USER_ID, uId);
        values.put(MemberContract.COLUMN_ROLE, role);
        db.insert(MemberContract.TABLE_NAME, null, values);
    }
}