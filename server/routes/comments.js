const express = require('express');
const router = express.Router();
const Comment = require('../models/Comment');

// GET /api/comments/task/:taskId - Lấy bình luận theo Task ID
router.get('/task/:taskId', async (req, res) => {
  try {
    const comments = await Comment.find({ taskId: req.params.taskId })
      .populate('userId', 'name email avatar')
      .sort({ timestamp: 1 });
    res.json({ success: true, count: comments.length, comments });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

// POST /api/comments - Thêm bình luận mới
router.post('/', async (req, res) => {
  try {
    const { taskId, userId, userName, content } = req.body;
    if (!taskId || !userId || !content) {
      return res.status(400).json({ success: false, message: 'Vui lòng nhập đầy đủ thông tin bình luận' });
    }

    const comment = new Comment({
      taskId,
      userId,
      userName: userName || '',
      content
    });

    await comment.save();
    res.status(201).json({ success: true, message: 'Đăng bình luận thành công', comment });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Lỗi Server: ' + err.message });
  }
});

module.exports = router;
