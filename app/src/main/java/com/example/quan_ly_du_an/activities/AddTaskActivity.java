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

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText edtTaskName;
    private TextInputEditText edtDescription;
    private MaterialButton btnDeadline;
    private MaterialButton btnSave;
    private ChipGroup chipGroupPriority;
    private ChipGroup chipGroupStatus;

    private String deadline = "";
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        db = AppDatabase.getDatabase(this);
        initView();
        setupEvent();
    }

    private void initView() {
        View backBtn = findViewById(R.id.btnBack);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        edtTaskName = findViewById(R.id.edtTaskName);
        edtDescription = findViewById(R.id.edtDescription);
        btnDeadline = findViewById(R.id.btnDeadline);
        btnSave = findViewById(R.id.btnSave);
        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
    }

    private void setupEvent() {
        btnDeadline.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveTask());
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

    private void saveTask() {
        String title = edtTaskName.getText() != null ? edtTaskName.getText().toString().trim() : "";
        String description = edtDescription.getText() != null ? edtDescription.getText().toString().trim() : "";

        if (title.isEmpty()) {
            edtTaskName.setError("Nhập tên công việc");
            return;
        }
        if (deadline.isEmpty()) {
            Toast.makeText(this, "Chọn Deadline", Toast.LENGTH_SHORT).show();
            return;
        }

        String priority = "Trung bình";
        int checkedPriorityId = chipGroupPriority.getCheckedChipId();
        if (checkedPriorityId != View.NO_ID) {
            priority = ((Chip) findViewById(checkedPriorityId)).getText().toString();
        }

        String status = "To Do";
        int checkedStatusId = chipGroupStatus.getCheckedChipId();
        if (checkedStatusId != View.NO_ID) {
            status = ((Chip) findViewById(checkedStatusId)).getText().toString();
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setDeadline(deadline);
        task.setPriority(priority);
        task.setStatus(status);
        task.setProjectId(1); 
        task.setAssignedUserId(1);

        executorService.execute(() -> {
            db.taskDao().insert(task);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã lưu công việc", Toast.LENGTH_SHORT).show();
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
