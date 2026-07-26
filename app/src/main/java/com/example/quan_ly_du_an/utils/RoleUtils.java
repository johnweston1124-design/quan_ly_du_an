package com.example.quan_ly_du_an.utils;

public class RoleUtils {
    public static boolean canManageMembers(String role) {
        if (role == null) return false;
        return role.equalsIgnoreCase(RoleEnum.ADMIN.getRoleName()) ||
                role.equalsIgnoreCase(RoleEnum.LEADER.getRoleName());
    }

    public static boolean canEditProject(String role) {
        return canManageMembers(role);
    }
}
