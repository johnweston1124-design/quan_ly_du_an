package com.example.quan_ly_du_an.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class TaskDetailActivity extends AppCompatActivity {

    private TextInputEditText edtTaskName;
    private TextInputEditText edtDescription;
    private MaterialButton btnDeadline;
    private MaterialButton btnUpdate;
    private MaterialButton btnDelete;
    private ChipGroup chipGroupPriority;
    private ChipGroup chipGroupStatus;

    private String deadline = "";
    private int taskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        initView();
        getIntentData();
        setupEvent();
        loadDemoData();
    }

    private void initView() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Chi tiết công việc");
        
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d("TaskDetail", "Back button clicked");
                finish();
            });
        }

        edtTaskName = findViewById(R.id.edtTaskName);
        edtDescription = findViewById(R.id.edtDescription);
        btnDeadline = findViewById(R.id.btnDeadline);
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
        btnUpdate.setOnClickListener(v -> updateTask());
        btnDelete.setOnClickListener(v -> deleteTask());
    }

    private void loadDemoData() {
        edtTaskName.setText("Thiết kế Login");
        edtDescription.setText("Thiết kế giao diện đăng nhập");
        deadline = "20/07/2026";
        btnDeadline.setText(deadline);
        
        Chip chipHigh = findViewById(R.id.chipHigh);
        if (chipHigh != null) chipHigh.setChecked(true);
        
        Chip chipDoing = findViewById(R.id.chipDoing);
        if (chipDoing != null) chipDoing.setChecked(true);
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
        Toast.makeText(this, "Đã cập nhật công việc", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteTask() {
        Toast.makeText(this, "Đã xóa công việc", Toast.LENGTH_SHORT).show();
        finish();
    }
}
