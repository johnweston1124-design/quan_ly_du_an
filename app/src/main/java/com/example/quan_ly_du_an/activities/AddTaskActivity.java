package com.example.quan_ly_du_an.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText edtTaskName;
    private TextInputEditText edtDescription;
    private MaterialButton btnDeadline;
    private MaterialButton btnSave;
    private ChipGroup chipGroupPriority;
    private ChipGroup chipGroupStatus;

    private String deadline = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initView();
        setupEvent();
    }

    private void initView() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Thêm công việc");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        edtTaskName = findViewById(R.id.edtTaskName);
        edtDescription = findViewById(R.id.edtDescription);
        btnDeadline = findViewById(R.id.btnDeadline);
        btnSave = findViewById(R.id.btnSave);
        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
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
        String title = edtTaskName.getText().toString().trim();
        if (title.isEmpty()) {
            edtTaskName.setError("Nhập tên công việc");
            return;
        }
        if (deadline.isEmpty()) {
            Toast.makeText(this, "Chọn Deadline", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đã lưu: " + title, Toast.LENGTH_SHORT).show();
        finish();
    }
}
