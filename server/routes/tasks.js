const express = require('express');
const router = express.Router();
const Task = require('../models/Task');

// GET /api/tasks - Lấy tất cả công việc (Admin)
router.get('/', async (req, res) => {
  try {
    const tasks = await Task.find()
      .populate('assignedUserId', 'name email')
      .populate('projectId', 'title')
      .sort({ createdAt: -1 });
    res.json({ success: true, count: tasks.length, tasks });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// GET /api/tasks/user/:userId - Lấy công việc theo User ID
router.get('/user/:userId', async (req, res) => {
  try {
    const tasks = await Task.find({ assignedUserId: req.params.userId })
      .populate('assignedUserId', 'name email')
      .populate('projectId', 'title')
      .sort({ createdAt: -1 });
    res.json({ success: true, count: tasks.length, tasks });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// POST /api/tasks - Tạo công việc mới
router.post('/', async (req, res) => {
  try {
    const { title, description, priority, status, deadline, assignedUserId, projectId } = req.body;
    if (!title || !assignedUserId) {
      return res.status(400).json({ success: false, message: 'Vui lòng nhập tiêu đề công việc và người được giao' });
    }

    const task = new Task({
      title,
      description: description || '',
      priority: priority || 'Trung bình',
      status: status || 'Tự do',
      deadline: deadline || '',
      assignedUserId,
      projectId: projectId || null
    });

    await task.save();
    res.status(201).json({ success: true, message: 'Tạo công việc thành công', task });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// PUT /api/tasks/:id - Cập nhật công việc
router.put('/:id', async (req, res) => {
  try {
    const { title, description, priority, status, deadline } = req.body;
    const task = await Task.findById(req.params.id);
    if (!task) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy công việc' });
    }

    if (title) task.title = title;
    if (description !== undefined) task.description = description;
    if (priority) task.priority = priority;
    if (status) task.status = status;
    if (deadline !== undefined) task.deadline = deadline;

    await task.save();
    res.json({ success: true, message: 'Cập nhật công việc thành công', task });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// DELETE /api/tasks/:id - Xóa công việc
router.delete('/:id', async (req, res) => {
  try {
    const task = await Task.findByIdAndDelete(req.params.id);
    if (!task) {
      return res.status(404).json({ success: false, message: 'Không tìm thấy công việc' });
    }
    res.json({ success: true, message: 'Đã xóa công việc thành công' });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

module.exports = router;
