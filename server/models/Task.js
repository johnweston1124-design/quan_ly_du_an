const mongoose = require('mongoose');

const taskSchema = new mongoose.Schema({
  projectId: { type: mongoose.Schema.Types.ObjectId, ref: 'Project', default: null },
  assignedUserId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  title: { type: String, required: true, trim: true },
  description: { type: String, default: '' },
  priority: { type: String, enum: ['Cao', 'High', 'Trung bình', 'Medium', 'Thấp', 'Low'], default: 'Trung bình' },
  status: { type: String, enum: ['Tự do', 'To Do', 'Đang làm', 'Doing', 'Hoàn thành', 'Done'], default: 'Tự do' },
  deadline: { type: String, default: '' },
  createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Task', taskSchema);
