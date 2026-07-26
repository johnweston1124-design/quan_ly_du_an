package com.example.quan_ly_du_an.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class Project {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "project_id")
    private long projectId;

    private String title;
    private String description;
    private String status;
    
    @ColumnInfo(name = "start_date")
    private String startDate;
    
    @ColumnInfo(name = "end_date")
    private String endDate;
    
    @ColumnInfo(name = "expected_members")
    private int expectedMembers;

    public Project(String title, String description, String status, String startDate, String endDate, int expectedMembers) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.expectedMembers = expectedMembers;
    }

    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getExpectedMembers() { return expectedMembers; }
    public void setExpectedMembers(int expectedMembers) { this.expectedMembers = expectedMembers; }
}
