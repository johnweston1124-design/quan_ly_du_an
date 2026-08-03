const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');

const app = express();
app.use(bodyParser.json());

// 1. KẾT NỐI MONGODB ATLAS
const mongoURI = "mongodb+srv://siro0921260379_db_user:zf3om0cgnEQTW8Vy@cluster0.izbn30t.mongodb.net/quan_ly_du_an?retryWrites=true&w=majority";
mongoose.connect(mongoURI)
    .then(() => console.log("✅ Đã kết nối MongoDB Atlas thành công!"))
    .catch(err => console.log("❌ Lỗi kết nối MongoDB: " + err));

// 2. ĐỊNH NGHĨA SCHEMA (CẤU TRÚC DỮ LIỆU)
const User = mongoose.model('User', {
    name: String,
    email: String,
    role: { type: String, default: 'Member' }
});

const Task = mongoose.model('Task', {
    title: String,
    description: String,
    priority: String,
    status: String,
    deadline: String,
    assignedUserId: Number
});

// 3. CÁC API ENDPOINTS
// API Đăng ký User
app.post('/api/users', async (req, res) => {
    try {
        console.log("📥 Nhận yêu cầu tạo User:", req.body);
        const newUser = new User(req.body);
        await newUser.save();
        res.status(201).send(newUser);
    } catch (e) {
        console.error("❌ Lỗi tạo User:", e);
        res.status(400).send(e);
    }
});

// API Thêm Task
app.post('/api/tasks', async (req, res) => {
    try {
        console.log("📥 Nhận yêu cầu tạo Task:", req.body);
        const newTask = new Task(req.body);
        await newTask.save();
        res.status(201).send(newTask);
    } catch (e) {
        console.error("❌ Lỗi tạo Task:", e);
        res.status(400).send(e);
    }
});

// 4. CHẠY SERVER
const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Server đang chạy tại http://localhost:${PORT}`);
    console.log(`📱 Thiết bị Android có thể kết nối qua IP máy tính của bạn trên cổng ${PORT}`);
});
