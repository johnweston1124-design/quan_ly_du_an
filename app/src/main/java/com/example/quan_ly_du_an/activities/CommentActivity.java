package com.example.quan_ly_du_an.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.database.Comment;
import com.example.quan_ly_du_an.ui.CommentAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentActivity extends AppCompatActivity {
    private AppDatabase db;
    private CommentAdapter adapter;
    private int taskId;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_comment_section);

        taskId = getIntent().getIntExtra("taskId", 1);
        
        RecyclerView rvComments = findViewById(R.id.rvComments);
        EditText edtCommentInput = findViewById(R.id.edtCommentInput);
        Button btnSendComment = findViewById(R.id.btnSendComment);
        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        adapter = new CommentAdapter();
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);
        
        db = AppDatabase.getDatabase(this);

        loadComments();
        setupSendButton(btnSendComment, edtCommentInput);
    }

    private void loadComments() {
        executorService.execute(() -> {
            List<com.example.quan_ly_du_an.model.User> users = db.userDao().getAllUsers();
            java.util.Map<Integer, String> userMap = new java.util.HashMap<>();
            userMap.put(999, "Quản trị viên hệ thống");
            if (users != null) {
                for (com.example.quan_ly_du_an.model.User u : users) {
                    userMap.put(u.getId(), u.getName());
                }
            }
            runOnUiThread(() -> {
                adapter.setUserMap(userMap);
                db.commentDao().getCommentsByTaskId(taskId).observe(this, comments -> {
                    adapter.submitList(comments);
                });
            });
        });
    }

    private void setupSendButton(Button btnSendComment, EditText edtCommentInput) {
        btnSendComment.setOnClickListener(v -> {
            String content = edtCommentInput.getText().toString().trim();
            if (!content.isEmpty()) {
                android.content.SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
                int currentUserId = sharedPref.getInt("USER_ID", 1);

                Comment newComment = new Comment();
                newComment.taskId = taskId;
                newComment.userId = currentUserId;
                newComment.content = content;
                newComment.timestamp = System.currentTimeMillis();
                
                executorService.execute(() -> {
                    db.commentDao().insertComment(newComment);
                });
                edtCommentInput.setText("");
            }
        });
    }
}
