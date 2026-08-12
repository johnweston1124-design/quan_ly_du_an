package com.example.quan_ly_du_an.model;

public class ProjectMember {

    private long projectId;
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
