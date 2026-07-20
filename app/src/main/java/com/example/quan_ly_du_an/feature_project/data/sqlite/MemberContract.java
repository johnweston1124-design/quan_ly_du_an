package com.example.quan_ly_du_an.feature_project.data.sqlite;

import android.database.sqlite.SQLiteDatabase;

public class MemberContract {
    // Tên bảng và các cột nhiệm vụ của bạn
    public static final String TABLE_NAME = "project_member_cross_ref";
    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_ROLE = "role"; // "ADMIN", "LEADER", "MEMBER"

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_PROJECT_ID + " INTEGER NOT NULL, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    COLUMN_ROLE + " TEXT NOT NULL, " +
                    "PRIMARY KEY (" + COLUMN_PROJECT_ID + ", " + COLUMN_USER_ID + "));";

    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static void createTable(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    public static void upgradeTable(SQLiteDatabase db) {
        db.execSQL(SQL_DROP_TABLE);
        createTable(db);
    }
}