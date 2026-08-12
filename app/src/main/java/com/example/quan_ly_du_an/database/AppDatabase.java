package com.example.quan_ly_du_an.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database chính của ứng dụng Quản lý dự án.
 * Sử dụng SQLiteOpenHelper (OOP) thay cho Room.
 * Singleton pattern để đảm bảo chỉ có 1 instance.
 */
public class AppDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "task_database";
    private static final int DATABASE_VERSION = 8;

    private static volatile AppDatabase INSTANCE;

    // DAO instances (lazy initialization)
    private TaskDao _taskDao;
    private UserDao _userDao;
    private CommentDao _commentDao;
    private ProjectDao _projectDao;
    private ProjectMemberDao _projectMemberDao;
    private HistoryDao _historyDao;
    private SearchDao _searchDao;

    // SQL tạo bảng users
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT, " +
            "email TEXT, " +
            "password TEXT, " +
            "avatar TEXT, " +
            "role TEXT DEFAULT 'Member'" +
            ")";

    // SQL tạo bảng tasks
    private static final String CREATE_TABLE_TASKS =
            "CREATE TABLE IF NOT EXISTS tasks (" +
            "taskId INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "projectId INTEGER, " +
            "assignedUserId INTEGER, " +
            "title TEXT, " +
            "description TEXT, " +
            "priority TEXT, " +
            "status TEXT, " +
            "deadline TEXT" +
            ")";

    // SQL tạo bảng comments
    private static final String CREATE_TABLE_COMMENTS =
            "CREATE TABLE IF NOT EXISTS comments (" +
            "commentId INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "taskId INTEGER, " +
            "userId INTEGER, " +
            "content TEXT, " +
            "timestamp INTEGER" +
            ")";

    // SQL tạo bảng projects
    private static final String CREATE_TABLE_PROJECTS =
            "CREATE TABLE IF NOT EXISTS projects (" +
            "project_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "title TEXT, " +
            "description TEXT, " +
            "status TEXT, " +
            "start_date TEXT, " +
            "end_date TEXT, " +
            "expected_members INTEGER" +
            ")";

    // SQL tạo bảng project_member_cross_ref
    private static final String CREATE_TABLE_PROJECT_MEMBERS =
            "CREATE TABLE IF NOT EXISTS project_member_cross_ref (" +
            "project_id INTEGER NOT NULL, " +
            "user_id INTEGER NOT NULL, " +
            "role TEXT, " +
            "PRIMARY KEY (project_id, user_id), " +
            "FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE, " +
            "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
            ")";

    // SQL tạo bảng history
    private static final String CREATE_TABLE_HISTORY =
            "CREATE TABLE IF NOT EXISTS history (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "title TEXT, " +
            "description TEXT, " +
            "timestamp TEXT, " +
            "userId INTEGER" +
            ")";

    // SQL tạo index cho user_id trong project_member_cross_ref
    private static final String CREATE_INDEX_PROJECT_MEMBER_USER =
            "CREATE INDEX IF NOT EXISTS index_project_member_cross_ref_user_id " +
            "ON project_member_cross_ref(user_id)";

    private AppDatabase(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Singleton: Lấy instance duy nhất của AppDatabase.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppDatabase(context);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_COMMENTS);
        db.execSQL(CREATE_TABLE_PROJECTS);
        db.execSQL(CREATE_TABLE_PROJECT_MEMBERS);
        db.execSQL(CREATE_TABLE_HISTORY);
        db.execSQL(CREATE_INDEX_PROJECT_MEMBER_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS project_member_cross_ref");
        db.execSQL("DROP TABLE IF EXISTS comments");
        db.execSQL("DROP TABLE IF EXISTS tasks");
        db.execSQL("DROP TABLE IF EXISTS history");
        db.execSQL("DROP TABLE IF EXISTS projects");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // --- DAO Accessors (giữ nguyên tên method cũ) ---

    public TaskDao taskDao() {
        if (_taskDao == null) {
            _taskDao = new TaskDao(this);
        }
        return _taskDao;
    }

    public UserDao userDao() {
        if (_userDao == null) {
            _userDao = new UserDao(this);
        }
        return _userDao;
    }

    public CommentDao commentDao() {
        if (_commentDao == null) {
            _commentDao = new CommentDao(this);
        }
        return _commentDao;
    }

    public ProjectDao projectDao() {
        if (_projectDao == null) {
            _projectDao = new ProjectDao(this);
        }
        return _projectDao;
    }

    public ProjectMemberDao projectMemberDao() {
        if (_projectMemberDao == null) {
            _projectMemberDao = new ProjectMemberDao(this);
        }
        return _projectMemberDao;
    }

    public HistoryDao historyDao() {
        if (_historyDao == null) {
            _historyDao = new HistoryDao(this);
        }
        return _historyDao;
    }

    public SearchDao searchDao() {
        if (_searchDao == null) {
            _searchDao = new SearchDao(this);
        }
        return _searchDao;
    }

    /**
     * Thực hiện raw query (tương thích với code cũ dùng roomDb.query()).
     * Trả về Cursor để đọc kết quả.
     */
    public Cursor query(String sql, Object[] bindArgs) {
        String[] stringArgs = null;
        if (bindArgs != null) {
            stringArgs = new String[bindArgs.length];
            for (int i = 0; i < bindArgs.length; i++) {
                stringArgs[i] = String.valueOf(bindArgs[i]);
            }
        }
        return getReadableDatabase().rawQuery(sql, stringArgs);
    }

    /**
     * Thực hiện exec SQL (cho DELETE, UPDATE không cần kết quả).
     */
    public void execQuery(String sql, Object[] bindArgs) {
        if (bindArgs != null) {
            getWritableDatabase().execSQL(sql, bindArgs);
        } else {
            getWritableDatabase().execSQL(sql);
        }
    }
}
