const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const User = require('../models/User');

// GET /api/users - Lấy danh sách tất cả người dùng (Dành cho Admin)
router.get('/', async (req, res) => {
  try {
    const users = await User.find().select('-password').sort({ createdAt: -1 });
    res.json({ success: true, count: users.length, users });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// GET /api/users/:id - Lấy thông tin 1 người dùng theo ID
router.get('/:id', async (req, res) => {
  try {
    const user = await User.findById(req.params.id).select('-password');
    if (!user) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    }
    res.json({ success: true, user });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// PUT /api/users/profile/:id - Cập nhật thông tin cá nhân
router.put('/profile/:id', async (req, res) => {
  try {
    const { name, email } = req.body;
    const user = await User.findById(req.params.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    }

    if (name) user.name = name;
    if (email) user.email = email.toLowerCase();

    await user.save();
    res.json({ success: true, message: 'Cập nhật thông tin thành công', user });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// PUT /api/users/password/:id - Đổi mật khẩu
router.put('/password/:id', async (req, res) => {
  try {
    const { oldPassword, newPassword } = req.body;
    const user = await User.findById(req.params.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy người dùng' });
    }

    const isMatch = await bcrypt.compare(oldPassword, user.password);
    if (!isMatch && oldPassword !== user.password) {
      return res.status(400).json({ success: false, message: 'Mật khẩu hiện tại không chính xác' });
    }

    const salt = await bcrypt.genSalt(10);
    user.password = await bcrypt.hash(newPassword, salt);
    await user.save();

    res.json({ success: true, message: 'Đổi mật khẩu thành công' });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

module.exports = router;
