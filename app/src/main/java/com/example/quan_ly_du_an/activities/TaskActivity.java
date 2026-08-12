package com.example.quan_ly_du_an.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.adapter.TaskAdapter;
import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.model.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskActivity extends AppCompatActivity
        implements TaskAdapter.OnTaskClickListener {

    private RecyclerView rvTask;
    private FloatingActionButton fabAdd;
    private TextInputEditText edtSearch;
    private ChipGroup chipGroupStatus, chipGroupPriority;
    private LinearLayout layoutEmpty;

    private List<Task> fullTaskList = new ArrayList<>();
    private TaskAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        db = AppDatabase.getDatabase(this);
        initView();
        setupRecyclerView();
        observeTasks();
        setupEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        observeTasks();
    }

    private void initView() {
        rvTask = findViewById(R.id.rvTask);
        fabAdd = findViewById(R.id.fabAdd);
        edtSearch = findViewById(R.id.edtSearch);
        chipGroupStatus = findViewById(R.id.chipGroup);
        chipGroupPriority = findViewById(R.id.chipGroupPriorityFilter);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        rvTask.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(new ArrayList<>(), this);
        rvTask.setAdapter(adapter);
    }

    private void observeTasks() {
        db.taskDao().getAllTasks().observe(this, tasks -> {
            if (tasks != null) {
                fullTaskList = tasks;
                adapter.updateData(tasks);
                updateEmptyState();
            }
        });
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

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
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

        if (chipGroupPriority != null) {
            chipGroupPriority.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    adapter.filterByPriority("Tất cả ưu tiên");
                } else {
                    Chip chip = findViewById(checkedIds.get(0));
                    if (chip != null) {
                        adapter.filterByPriority(chip.getText().toString());
                    }
                }
                updateEmptyState();
            });
        }

        findViewById(R.id.btnSort).setOnClickListener(v -> {
            List<Task> sortedList = new ArrayList<>(fullTaskList);
            Collections.sort(sortedList, (t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
            adapter.updateData(sortedList);
            Toast.makeText(this, "Đã sắp xếp theo tên", Toast.LENGTH_SHORT).show();
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
