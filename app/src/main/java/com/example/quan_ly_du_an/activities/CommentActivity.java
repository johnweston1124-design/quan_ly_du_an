package com.example.quan_ly_du_an.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.worker.DeadlineWorker;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.database.Comment;
import com.example.quan_ly_du_an.ui.CommentAdapter;

import java.util.List;

public class CommentActivity extends AppCompatActivity {
    private AppDatabase db;
    private CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_comment_section);
        RecyclerView rvComments = findViewById(R.id.rvComments);
        EditText edtCommentInput = findViewById(R.id.edtCommentInput);
        Button btnSendComment = findViewById(R.id.btnSendComment);
        adapter = new CommentAdapter();
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "quan_ly_du_an_db").build();

        loadComments();
        setupSendButton(btnSendComment, edtCommentInput);
    }
    private void setupDeadlineChecker() {
        PeriodicWorkRequest deadlineWorkRequest =
                new PeriodicWorkRequest.Builder(DeadlineWorker.class, 2, TimeUnit.HOURS)
                        .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DeadlineCheckerWork",
                ExistingPeriodicWorkPolicy.KEEP,
                deadlineWorkRequest
        );
    }
    private void loadComments() {
        int testTaskId = 1;
        db.commentDao().getCommentsByTaskId(testTaskId).observe(this, new Observer<List<Comment>>() {
            @Override
            public void onChanged(List<Comment> comments) {
                adapter.submitList(comments);
            }
        });
    }
    private void setupSendButton(Button btnSendComment, EditText edtCommentInput) {
        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = edtCommentInput.getText().toString().trim();
                if (!content.isEmpty()) {
                    Comment newComment = new Comment();
                    newComment.taskId = 1;
                    newComment.userId = 99;
                    newComment.content = content;
                    newComment.timestamp = System.currentTimeMillis();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            db.commentDao().insertComment(newComment);
                        }
                    }).start();
                    edtCommentInput.setText("");
                }
            }
        });
    }
}