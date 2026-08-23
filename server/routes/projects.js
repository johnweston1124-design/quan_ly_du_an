const express = require('express');
const router = express.Router();
const Project = require('../models/Project');

// GET /api/projects - Lấy tất cả dự án
router.get('/', async (req, res) => {
  try {
    const projects = await Project.find()
      .populate('createdBy', 'name email')
      .populate('members.user', 'name email role')
      .sort({ createdAt: -1 });
    res.json({ success: true, count: projects.length, projects });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// GET /api/projects/user/:userId - Lấy danh sách dự án mà user tham gia
router.get('/user/:userId', async (req, res) => {
  try {
    const userId = req.params.userId;
    const projects = await Project.find({
      $or: [
        { createdBy: userId },
        { 'members.user': userId }
      ]
    })
    .populate('createdBy', 'name email')
    .populate('members.user', 'name email role')
    .sort({ createdAt: -1 });

    res.json({ success: true, count: projects.length, projects });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// POST /api/projects - Tạo dự án mới
router.post('/', async (req, res) => {
  try {
    const { title, description, startDate, endDate, createdBy } = req.body;
    if (!title || !createdBy) {
      return res.status(400).json({ success: false, message: 'Vui lòng nhập Tên dự án và ID người tạo' });
    }

    const project = new Project({
      title,
      description: description || '',
      startDate: startDate || '',
      endDate: endDate || '',
      createdBy,
      members: [{ user: createdBy, role: 'ADMIN' }]
    });

    await project.save();
    res.status(201).json({ success: true, message: 'Tạo dự án thành công', project });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// DELETE /api/projects/:id - Xóa dự án
router.delete('/:id', async (req, res) => {
  try {
    const project = await Project.findByIdAndDelete(req.params.id);
    if (!project) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy dự án' });
    }
    res.json({ success: true, message: 'Đã xóa dự án thành công' });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

module.exports = router;
