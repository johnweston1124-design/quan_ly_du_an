package com.example.quan_ly_du_an.utils;

public enum RoleEnum {
    ADMIN("ADMIN"),
    LEADER("LEADER"),
    MEMBER("MEMBER");

    private final String roleName;

    RoleEnum(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
