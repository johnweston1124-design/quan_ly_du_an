# 📚 Tài Liệu Hướng Dẫn & Giải Thích CSDL (DATABASE.md)

Tài liệu này giải thích chi tiết cấu trúc, chức năng và vai trò của các file trong thư mục Database (`com.example.quan_ly_du_an.database`) thuộc ứng dụng **Quản Lý Dự Án**.

---

## 🏗️ 1. Kiến Trúc CSDL Hướng Đối Tượng (OOP SQLite)

Hệ thống CSDL của ứng dụng được xây dựng hoàn toàn bằng **SQLite thuần** (`SQLiteOpenHelper`) theo mô hình lập trình hướng đối tượng (OOP), loại bỏ các thư viện trung gian như Room hay ORM để đạt hiệu năng tối ưu và dễ dàng tùy biến.

* **Pattern:** Singleton (Đảm bảo duy nhất 1 kết nối CSDL trong suốt ứng dụng) và Data Access Object (DAO).
* **Tên CSDL:** `task_database`
* **Phiên bản (Version):** `8`

---

## 📁 2. Giải Thích Chi Tiết Các File Trong Thư Mục `database/`

Thư mục chính: `app/src/main/java/com/example/quan_ly_du_an/database/`

### 🏢 2.1. `AppDatabase.java` — Trái Tim CSDL
* **Vai trò:** Quản lý vòng đời CSDL và đóng vai trò là Factory khởi tạo các đối tượng DAO.
* **Chức năng chính:**
  * **Singleton Pattern (`getDatabase(Context)`):** Cấp phát duy nhất 1 instance `AppDatabase` cho toàn ứng dụng để tránh lãng phí tài nguyên và xung đột file SQLite.
  * **`onCreate(SQLiteDatabase db)`:** Thực thi các câu lệnh SQL khởi tạo 6 bảng dữ liệu (`users`, `tasks`, `projects`, `project_member_cross_ref`, `comments`, `history`) và tạo Index tối ưu hóa truy vấn.
  * **`onUpgrade(...)`:** Xử lý nâng cấp phiên bản CSDL (tự động xóa và tái tạo bảng khi có thay đổi cấu trúc schema).
  * **DAO Factory Methods (`taskDao()`, `userDao()`, ...):** Khởi tạo và trả về instance của các DAO tương ứng.
  * **Hàm hỗ trợ tương thích (`query(...)`, `execQuery(...)`):** Cho phép các Repository thực hiện các câu lệnh SQL tùy biến hoặc câu lệnh ghép (JOIN).

---

### 👤 2.2. `UserDao.java` — Quản Lý Tài Khoản Người Dùng
* **Vai trò:** Thực hiện tất cả các thao tác CRUD liên quan đến bảng `users`.
* **Chức năng chính:**
  * `insertUser(User user)`: Thêm tài khoản mới vào CSDL (xử lý đăng ký).
  * `login(String email, String password)`: Kiểm tra thông tin đăng nhập (khớp email & mật khẩu).
  * `getUserByEmail(String email)`: Tìm kiếm thông tin người dùng theo địa chỉ Email (dùng khi thêm thành viên vào dự án).
  * `getUserById(int userId)`: Lấy thông tin tài khoản đang đăng nhập để hiển thị lên màn hình Hồ sơ/Profile.

---

### 📋 2.3. `TaskDao.java` — Quản Lý Thẻ Công Việc
* **Vai trò:** Quản lý danh sách các công việc thuộc các dự án trong bảng `tasks`.
* **Chức năng chính:**
  * `insert(Task task)`: Tạo thẻ công việc mới (bắt buộc phải gắn với 1 `projectId` cụ thể).
  * `update(Task task)`: Cập nhật tiêu đề, mô tả, deadline, độ ưu tiên hoặc trạng thái công việc.
  * `delete(Task task)`: Xóa công việc khỏi CSDL.
  * `getAllTasksForUser(int userId)`: Lấy toàn bộ danh sách công việc được phân công cho người dùng cụ thể.
  * `getTaskById(int taskId)`: Lấy thông tin chi tiết của 1 công việc theo ID.
  * `searchTasksForUser(int userId, String query)`: Tìm kiếm công việc theo từ khóa tiêu đề hoặc mô tả.
  * `getLatestTasksForUser(int userId, int limit)`: Lấy N công việc mới nhất để hiển thị ở trang chủ.

---

### 📁 2.4. `ProjectDao.java` — Quản Lý Dự Án
* **Vai trò:** Thao tác dữ liệu trên bảng `projects` và thực hiện các câu lệnh SQL JOIN để lấy thông tin kèm vai trò người dùng.
* **Chức năng chính:**
  * `insertProject(Project project)`: Khởi tạo một dự án mới và trả về `projectId`.
  * `updateProject(Project project)`: Sửa thông tin dự án (tên, mô tả, ngày bắt đầu, ngày kết thúc).
  * `deleteProject(long projectId)`: Xóa dự án (chỉ dành cho ADMIN dự án).
  * `getProjectsForUser(long userId)`: Thực hiện `INNER JOIN` giữa bảng `projects` và `project_member_cross_ref` để lấy tất cả dự án mà người dùng tham gia kèm theo Vai trò (`ADMIN`, `LEADER`, `MEMBER`).
  * `getLatestProjectsForUser(long userId, int limit)`: Lấy danh sách dự án mới nhất cho màn hình trang chủ.

---

### 👥 2.5. `ProjectMemberDao.java` — Quản Lý Thành Viên Dự Án
* **Vai trò:** Quản lý mối quan hệ Nhiều - Nhiều giữa **Dự án** (`projects`) và **Người dùng** (`users`) thông qua bảng trung gian `project_member_cross_ref`.
* **Chức năng chính:**
  * `insertProjectMember(ProjectMember projectMember)`: Thêm một người dùng vào dự án với vai trò xác định.
  * `removeMember(long projectId, long userId)`: Xóa thành viên khỏi dự án.
  * `getMembersByProjectId(long projectId)`: Lấy danh sách tất cả thành viên trong dự án, sắp xếp ưu tiên hiển thị `ADMIN` $\rightarrow$ `LEADER` $\rightarrow$ `MEMBER`.
  * `isMemberAlreadyInProject(long projectId, String email)`: Kiểm tra xem email này đã có trong dự án chưa để tránh thêm trùng.
  * `getUserRoleSync(long projectId, long userId)`: Lấy vai trò của người dùng hiện tại trong dự án để phân quyền giao diện (ADMIN có quyền xóa dự án, LEADER có quyền thêm thành viên...).

---

### 💬 2.6. `CommentDao.java` — Quản Lý Bình Luận
* **Vai trò:** Lưu trữ và truy vấn bình luận dưới các thẻ công việc trong bảng `comments`.
* **Chức năng chính:**
  * `insertComment(Comment comment)`: Lưu bình luận mới của người dùng vào thẻ công việc.
  * `getCommentsByTaskId(int taskId)`: Lấy toàn bộ bình luận của 1 task cụ thể theo thứ tự thời gian tăng dần (`timestamp ASC`).

---

### 📜 2.7. `HistoryDao.java` — Quản Lý Lịch Sử Hoạt Động
* **Vai trò:** Lưu lại nhật ký hoạt động của người dùng trong bảng `history`.
* **Chức năng chính:**
  * `insert(History history)`: Ghi nhận 1 hành động mới (ví dụ: *Tạo dự án mới*, *Tạo công việc mới*).
  * `getAllHistoryForUser(int userId)`: Lấy danh sách nhật ký hoạt động của tài khoản để hiển thị lên màn hình **Lịch sử hoạt động**.

---

### 🔍 2.8. `SearchDao.java` — Tìm Kiếm Bình Luận
* **Vai trò:** Hỗ trợ truy vấn tìm kiếm bình luận chứa từ khóa cần tìm trong CSDL.
* **Chức năng chính:**
  * `searchComments(String query)`: Tìm các bình luận chứa chuỗi ký tự theo câu lệnh SQL `LIKE '%query%'`.

---

### 🧩 2.9. `Comment.java` — Đối Tượng Thực Thể (Entity)
* **Vai trò:** Class POJO chứa thông tin bản ghi bình luận (`commentId`, `taskId`, `userId`, `content`, `timestamp`).

---

## 📊 3. Sơ Đồ Cấu Trúc Các Bảng SQL (Schema Summary)

| Tên Bảng | Khóa Chính (Primary Key) | Các Cột Chính | Ghi Chú |
| :--- | :--- | :--- | :--- |
| **`users`** | `id` (AUTOINCREMENT) | `name`, `email`, `password`, `avatar`, `role` | Lưu thông tin người dùng toàn hệ thống |
| **`projects`** | `project_id` (AUTOINCREMENT) | `title`, `description`, `status`, `start_date`, `end_date`, `expected_members` | Lưu danh sách các dự án |
| **`project_member_cross_ref`** | `(project_id, user_id)` (Composite) | `project_id`, `user_id`, `role` | Bảng quan hệ nhiều-nhiều giữa User và Project |
| **`tasks`** | `taskId` (AUTOINCREMENT) | `projectId`, `assignedUserId`, `title`, `description`, `priority`, `status`, `deadline` | Lưu thẻ công việc (khóa ngoại `projectId` & `assignedUserId`) |
| **`comments`** | `commentId` (AUTOINCREMENT) | `taskId`, `userId`, `content`, `timestamp` | Lưu bình luận trong từng Task |
| **`history`** | `id` (AUTOINCREMENT) | `title`, `description`, `timestamp`, `userId` | Lưu nhật ký thao tác người dùng |

---

## 💡 4. Hướng Dẫn Sử Dụng Trong Code Java

Để thực hiện thao tác với CSDL ở bất kỳ màn hình nào, bạn chỉ cần gọi theo cú pháp:

```java
// 1. Lấy Instance Database
AppDatabase db = AppDatabase.getDatabase(context);

// 2. Thao tác thông qua DAO tương ứng
// Ví dụ: Lấy danh sách task của User
db.taskDao().getAllTasksForUser(userId).observe(this, tasks -> {
    // Cập nhật lên RecyclerView
});

// Ví dụ: Đăng nhập
executorService.execute(() -> {
    User user = db.userDao().login(email, password);
});
```
