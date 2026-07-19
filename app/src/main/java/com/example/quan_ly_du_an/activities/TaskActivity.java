package com.example.quan_ly_du_an.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.adapter.TaskAdapter;
import com.example.quan_ly_du_an.model.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class TaskActivity extends AppCompatActivity
        implements TaskAdapter.OnTaskClickListener {

    private RecyclerView rvTask;
    private FloatingActionButton fabAdd;
    private TextInputEditText edtSearch;
    private ChipGroup chipGroup;
    private LinearLayout layoutEmpty;

    private ArrayList<Task> taskList;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        initView();
        setupRecyclerView();
        loadDemoData();
        setupEvent();
    }

    private void initView() {
        rvTask = findViewById(R.id.rvTask);
        fabAdd = findViewById(R.id.fabAdd);
        edtSearch = findViewById(R.id.edtSearch);
        chipGroup = findViewById(R.id.chipGroup);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        rvTask.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, this);
        rvTask.setAdapter(adapter);
    }

    private void loadDemoData() {
        ArrayList<Task> demoList = new ArrayList<>();
        demoList.add(new Task(1, 1, 2, "Thiết kế Login", "Thiết kế giao diện đăng nhập", "Cao", "Đang làm", "20/07/2026"));
        demoList.add(new Task(2, 1, 3, "Thiết kế Database", "Tạo bảng Task", "Trung bình", "To Do", "22/07/2026"));
        demoList.add(new Task(3, 1, 4, "Hoàn thành API", "Viết CRUD Task", "Thấp", "Hoàn thành", "25/07/2026"));
        
        taskList.clear();
        taskList.addAll(demoList);
        adapter.updateData(taskList);
        updateEmptyState();
    }

    private void setupEvent() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(TaskActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                adapter.filterByStatus("Tất cả");
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                if (chip != null) {
                    adapter.filterByStatus(chip.getText().toString());
                }
            }
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
            rvTask.setVisibility(View.GONE);
        } else {
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
            rvTask.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(TaskActivity.this, TaskDetailActivity.class);
        intent.putExtra("taskId", task.getTaskId());
        startActivity(intent);
    }
}
