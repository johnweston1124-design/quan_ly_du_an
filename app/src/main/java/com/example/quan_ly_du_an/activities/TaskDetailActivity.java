package com.example.quan_ly_du_an.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.model.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskDetailActivity extends AppCompatActivity {

    private TextInputEditText edtTaskName;
    private TextInputEditText edtDescription;
    private MaterialButton btnDeadline;
    private MaterialButton btnComments;
    private MaterialButton btnUpdate;
    private MaterialButton btnDelete;
    private ChipGroup chipGroupPriority;
    private ChipGroup chipGroupStatus;

    private String deadline = "";
    private int taskId;
    private Task currentTask;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        db = AppDatabase.getDatabase(this);
        initView();
        getIntentData();
        setupEvent();
        loadTaskData();
    }

    private void initView() {
        View backBtn = findViewById(R.id.btnBack);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        edtTaskName = findViewById(R.id.edtTaskName);
        edtDescription = findViewById(R.id.edtDescription);
        btnDeadline = findViewById(R.id.btnDeadline);
        btnComments = findViewById(R.id.btnComments);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
    }

    private void getIntentData() {
        taskId = getIntent().getIntExtra("taskId", -1);
    }

    private void setupEvent() {
        btnDeadline.setOnClickListener(v -> showDatePicker());
        if (btnComments != null) {
            btnComments.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(TaskDetailActivity.this, CommentActivity.class);
                intent.putExtra("taskId", taskId);
                startActivity(intent);
            });
        }
        btnUpdate.setOnClickListener(v -> updateTask());
        btnDelete.setOnClickListener(v -> deleteTask());
    }

    private void loadTaskData() {
        if (taskId == -1) return;

        executorService.execute(() -> {
            currentTask = db.taskDao().getTaskById(taskId);
            if (currentTask != null) {
                runOnUiThread(() -> {
                    edtTaskName.setText(currentTask.getTitle());
                    edtDescription.setText(currentTask.getDescription());
                    deadline = currentTask.getDeadline();
                    btnDeadline.setText(deadline);
                    selectChipByText(chipGroupPriority, currentTask.getPriority());
                    selectChipByText(chipGroupStatus, currentTask.getStatus());
                });
            }
        });
    }

    private void selectChipByText(ChipGroup group, String text) {
        if (group == null || text == null) return;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.getText().toString().equalsIgnoreCase(text)) {
                    chip.setChecked(true);
                    return;
                }
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    deadline = day + "/" + (month + 1) + "/" + year;
                    btnDeadline.setText(deadline);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateTask() {
        if (currentTask == null) return;

        String title = edtTaskName.getText() != null ? edtTaskName.getText().toString().trim() : "";
        String description = edtDescription.getText() != null ? edtDescription.getText().toString().trim() : "";

        if (title.isEmpty()) {
            edtTaskName.setError("Nhập tên công việc");
            return;
        }

        currentTask.setTitle(title);
        currentTask.setDescription(description);
        currentTask.setDeadline(deadline);

        int checkedPriorityId = chipGroupPriority.getCheckedChipId();
        if (checkedPriorityId != View.NO_ID) {
            currentTask.setPriority(((Chip) findViewById(checkedPriorityId)).getText().toString());
        }

        int checkedStatusId = chipGroupStatus.getCheckedChipId();
        if (checkedStatusId != View.NO_ID) {
            currentTask.setStatus(((Chip) findViewById(checkedStatusId)).getText().toString());
        }

        executorService.execute(() -> {
            db.taskDao().update(currentTask);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã cập nhật công việc", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void deleteTask() {
        if (currentTask == null) return;

        executorService.execute(() -> {
            db.taskDao().delete(currentTask);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã xóa công việc", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
