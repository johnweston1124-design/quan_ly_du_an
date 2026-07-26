package com.example.quan_ly_du_an.model;

public class ProjectWithRole {
    public long projectId;
    public String title;
    public String description;
    public String status;
    public String role; // Quyền của User đang đăng nhập trong dự án này

    public ProjectWithRole(long projectId, String title, String description, String status, String role) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.role = role;
    }
}
