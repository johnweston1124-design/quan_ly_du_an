package com.example.quan_ly_du_an.model;

public class MemberWithRole {
    public long userId;
    public String name;
    public String email;
    public String avatarUrl;
    public String role;
    public int taskCount;

    public MemberWithRole(long userId, String name, String email, String avatarUrl, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.taskCount = 0;
    }
}
