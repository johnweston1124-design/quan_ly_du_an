const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Import Routes
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const projectRoutes = require('./routes/projects');
const taskRoutes = require('./routes/tasks');
const commentRoutes = require('./routes/comments');

// Register Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/projects', projectRoutes);
app.use('/api/tasks', taskRoutes);
app.use('/api/comments', commentRoutes);

// Health Check Endpoint
app.get('/', (req, res) => {
  res.json({
    status: 'ONLINE',
    message: '🚀 Node.js Express Server MongoDB Quản Lý Dự Án đang hoạt động!',
    timestamp: new Date().toISOString(),
    endpoints: [
      'POST /api/auth/register',
      'POST /api/auth/login',
      'GET /api/users',
      'GET /api/projects',
      'GET /api/tasks'
    ]
  });
});

// Database Connection
const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017/QuanLyDuAn';

mongoose.connect(MONGO_URI)
  .then(() => {
    console.log('---------------------------------------------------------');
    console.log('✅ Kết nối thành công tới MongoDB Atlas / Database!');
    console.log('---------------------------------------------------------');
  })
  .catch((err) => {
    console.error('---------------------------------------------------------');
    console.error('❌ Lỗi kết nối MongoDB Atlas:', err.message);
    console.error('💡 Gợi ý: Hãy kiểm tra IP Whitelist (0.0.0.0/0) và chuỗi MONGO_URI trong file .env');
    console.error('---------------------------------------------------------');
  });

const PORT = process.env.PORT || 5000;

app.listen(PORT, () => {
  console.log(`=========================================================`);
  console.log(`🌐 Backend Server đang chạy tại: http://localhost:${PORT}`);
  console.log(`=========================================================`);
});
