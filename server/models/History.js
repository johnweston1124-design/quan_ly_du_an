const mongoose = require('mongoose');

const historySchema = new mongoose.Schema({
  title: { type: String, required: true },
  description: { type: String, default: '' },
  timestamp: { type: String, default: '' },
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true }
});

module.exports = mongoose.model('History', historySchema);
