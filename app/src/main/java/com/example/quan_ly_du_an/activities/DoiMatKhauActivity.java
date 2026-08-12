package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.databinding.ActivityDoiMatKhauBinding;
import com.example.quan_ly_du_an.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoiMatKhauActivity extends AppCompatActivity {

    private ActivityDoiMatKhauBinding binding;
    private AppDatabase database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int currentUserId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoiMatKhauBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getDatabase(this);

        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sharedPref.getInt("USER_ID", -1);

        if (currentUserId == -1) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DoiMatKhauActivity.this, LoginActivity.class));
            finish();
            return;
        }

        loadUser();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void loadUser() {
        executorService.execute(() -> {
            currentUser = database.userDao().getUserById(currentUserId);
        });
    }

    private void handleChangePassword() {
        String currentPass = binding.edtCurrentPassword.getText() != null ? binding.edtCurrentPassword.getText().toString() : "";
        String newPass = binding.edtNewPassword.getText() != null ? binding.edtNewPassword.getText().toString() : "";
        String confirmPass = binding.edtConfirmPassword.getText() != null ? binding.edtConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(currentPass)) {
            binding.edtCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            return;
        }

        if (TextUtils.isEmpty(newPass)) {
            binding.edtNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return;
        }

        if (newPass.length() < 6) {
            binding.edtNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            binding.edtConfirmPassword.setError("Mật khẩu xác nhận không trùng khớp");
            return;
        }

        executorService.execute(() -> {
            if (currentUser != null) {
                if (!currentPass.equals(currentUser.getPassword())) {
                    runOnUiThread(() -> binding.edtCurrentPassword.setError("Mật khẩu hiện tại không chính xác!"));
                    return;
                }

                database.userDao().updatePassword(currentUserId, newPass);
                currentUser.setPassword(newPass);

                runOnUiThread(() -> {
                    Toast.makeText(DoiMatKhauActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else if (currentUserId == 999) {
                runOnUiThread(() -> {
                    Toast.makeText(DoiMatKhauActivity.this, "Đổi mật khẩu Admin mặc định thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(DoiMatKhauActivity.this, "Lỗi xác thực người dùng!", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
