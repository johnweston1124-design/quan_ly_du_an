package com.example.quan_ly_du_an.utils;

public class SessionManager {
    private static final long CURRENT_LOGGED_IN_USER_ID = 1L;

    public static long getCurrentUserId() {
        return CURRENT_LOGGED_IN_USER_ID;
    }
}