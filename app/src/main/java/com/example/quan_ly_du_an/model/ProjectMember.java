package com.example.quan_ly_du_an.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "project_member_cross_ref",
    primaryKeys = {"project_id", "user_id"},
    foreignKeys = {
        @ForeignKey(
            entity = Project.class,
            parentColumns = "project_id",
            childColumns = "project_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = User.class,
            parentColumns = "id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {@Index("user_id")}
)
public class ProjectMember {

    @ColumnInfo(name = "project_id")
    private long projectId;

    @ColumnInfo(name = "user_id")
    private long userId;

    private String role;

    public ProjectMember(long projectId, long userId, String role) {
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
    }

    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
