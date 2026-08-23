# 🚀 HƯỚNG DẪN CHẠY NODE.JS BACKEND SERVER CHO MONGODB

Server này được xây dựng bằng **Node.js, Express.js và Mongoose** làm trung gian kết nối ứng dụng Android **Quản Lý Dự Án** với cơ sở dữ liệu **MongoDB Atlas**.

---

## 🛠️ 1. HƯỚNG DẪN CẤU HÌNH MONGO_URI

Mở file `server/.env` và dán chuỗi kết nối từ MongoDB Atlas của bạn:

```env
PORT=5000
MONGO_URI=mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/QuanLyDuAn?retryWrites=true&w=majority
JWT_SECRET=quan_ly_du_an_secret_key_2026
```

> **Lưu ý:**
> 1. Thay `<username>` và `<password>` bằng tài khoản Database User đã tạo trên MongoDB Atlas.
> 2. Đảm bảo đã bật **Allow Access from Anywhere (`0.0.0.0/0`)** trong mục **Network Access** trên MongoDB Atlas.

---

## 💻 2. LỆNH CHẠY SERVER

Chạy lệnh sau tại thư mục `/server`:

```bash
cd server
npm start
```

Màn hình khi chạy thành công sẽ thông báo:

```text
=========================================================
🌐 Backend Server đang chạy tại: http://localhost:5000
=========================================================
---------------------------------------------------------
✅ Kết nối thành công tới MongoDB Atlas / Database!
---------------------------------------------------------
```

---

## 📡 3. BẢNG CHI TIẾT CÁC API ENDPOINTS

### 🔐 Auth (Xác thực tài khoản)
- `POST /api/auth/register`: Đăng ký tài khoản mới (`{ name, email, password, role }`)
- `POST /api/auth/login`: Đăng nhập (`{ email, password }`)

### 👤 Users (Quản lý người dùng)
- `GET /api/users`: Lấy danh sách tất cả người dùng (Dành cho Admin)
- `GET /api/users/:id`: Lấy chi tiết 1 người dùng theo ID
- `PUT /api/users/profile/:id`: Cập nhật Họ tên / Email
- `PUT /api/users/password/:id`: Đổi mật khẩu

### 📁 Projects (Quản lý dự án)
- `GET /api/projects`: Lấy tất cả dự án
- `GET /api/projects/user/:userId`: Lấy danh sách dự án mà người dùng tham gia
- `POST /api/projects`: Tạo dự án mới (`{ title, description, startDate, endDate, createdBy }`)
- `DELETE /api/projects/:id`: Xóa dự án

### 📋 Tasks (Quản lý công việc)
- `GET /api/tasks`: Lấy tất cả công việc
- `GET /api/tasks/user/:userId`: Lấy danh sách công việc theo User ID
- `POST /api/tasks`: Tạo công việc mới (`{ title, description, priority, status, deadline, assignedUserId, projectId }`)
- `PUT /api/tasks/:id`: Cập nhật trạng thái / độ ưu tiên công việc
- `DELETE /api/tasks/:id`: Xóa công việc

### 💬 Comments (Bình luận)
- `GET /api/comments/task/:taskId`: Lấy bình luận của 1 công việc
- `POST /api/comments`: Đăng bình luận mới (`{ taskId, userId, userName, content }`)
