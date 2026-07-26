package com.example.quan_ly_du_an.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Cầu nối với Phân công 1 (Auth)
 */
public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "USER_ID";

    public static long getCurrentUserId(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Trả về -1 nếu chưa đăng nhập. Mặc định demo là 1 nếu cần, nhưng chuẩn là lấy từ Pref.
        return sharedPref.getInt(KEY_USER_ID, 1); 
    }
    
    public static boolean isLoggedIn(Context context) {
        return getCurrentUserId(context) != -1;
    }
}
