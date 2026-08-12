package com.example.quan_ly_du_an.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import java.util.Locale;

public class ThemeAndLocaleManager {
    private static final String PREF_NAME = "AppSettings";
    private static final String KEY_DARK_MODE = "DARK_MODE_ENABLED";
    private static final String KEY_LANGUAGE = "APP_LANGUAGE";
    private static final String KEY_LOCALE = "APP_LOCALE";

    public static void applyThemeAndLocale(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 1. Apply Dark Mode
        boolean isDarkMode = pref.getBoolean(KEY_DARK_MODE, false);
        int targetMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }

        // 2. Apply Locale
        String langCode = pref.getString(KEY_LOCALE, "vi");
        applyLocale(context, langCode);
    }

    public static void setDarkMode(Context context, boolean isDark) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_DARK_MODE, isDark).apply();
        AppCompatDelegate.setDefaultNightMode(isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_DARK_MODE, false);
    }

    public static void setLanguage(Context context, String langName, String langCode) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_LANGUAGE, langName).putString(KEY_LOCALE, langCode).apply();
        applyLocale(context, langCode);
        try {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getLanguageName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_LANGUAGE, "Tiếng Việt");
    }

    private static void applyLocale(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}
