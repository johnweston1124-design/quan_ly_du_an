package com.example.quan_ly_du_an.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quan_ly_du_an.database.AppDatabase;
import com.example.quan_ly_du_an.model.User;
import com.example.quan_ly_du_an.databinding.ActivityLoginBinding;

// Nhớ Import MainActivity từ đúng package nếu MainActivity nằm ở package khác
import com.example.quan_ly_du_an.activities.MainActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AppDatabase database;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        com.example.quan_ly_du_an.utils.ThemeAndLocaleManager.applyThemeAndLocale(this);
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getDatabase(this);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("610134536543-iepm19bs8du5kjjel17ujssfl3guv3j8.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false);
        if (isLoggedIn) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> handleLogin());

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        binding.btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        binding.btnFacebook.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đăng nhập Facebook đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            
            // Lấy thông tin từ Google
            String email = account.getEmail();
            String displayName = account.getDisplayName();
            
            // Xử lý đăng nhập vào hệ thống
            handleSocialLogin(email, displayName);

        } catch (ApiException e) {
            int statusCode = e.getStatusCode();
            String errorMsg = "Lỗi Google (" + statusCode + "): ";
            
            switch (statusCode) {
                case 10: errorMsg += "Mã SHA-1 hoặc Client ID không khớp."; break;
                case 12500: errorMsg += "Lỗi dịch vụ Google Play."; break;
                case 12501: errorMsg += "Người dùng đã hủy đăng nhập."; break;
                case 7: errorMsg += "Không có kết nối mạng."; break;
                default: errorMsg += e.getMessage(); break;
            }
            
            android.util.Log.e("GOOGLE_LOGIN", "signInResult:failed code=" + statusCode);
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void handleSocialLogin(String email, String name) {
        executorService.execute(() -> {
            User user = database.userDao().getUserByEmail(email);
            if (user == null) {
                // Tạo user mới nếu chưa tồn tại
                user = new User(name, email, "social_login_no_password");
                database.userDao().insertUser(user);
                user = database.userDao().getUserByEmail(email);
            }
            
            final int userId = user.getId();
            runOnUiThread(() -> saveSessionAndNavigate(userId));
        });
    }

    private void handleLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        if (email.isEmpty()) {
            binding.tilEmail.setError("Vui lòng nhập email");
            return;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (email.equals("admin") && password.equals("123456")) {
            executorService.execute(() -> {
                User adminUser = database.userDao().getUserById(999);
                if (adminUser == null) {
                    adminUser = new User("Quản trị viên hệ thống", "admin@system.com", "123456");
                    adminUser.setId(999);
                    adminUser.setRole("Admin");
                    database.userDao().insertUser(adminUser);
                } else if ("Demo User".equals(adminUser.getName()) || (adminUser.getEmail() != null && adminUser.getEmail().contains("demo999"))) {
                    adminUser.setName("Quản trị viên hệ thống");
                    adminUser.setEmail("admin@system.com");
                    adminUser.setRole("Admin");
                    database.userDao().updateUser(adminUser);
                }
                runOnUiThread(() -> saveSessionAndNavigate(999));
            });
            return;
        }

        executorService.execute(() -> {
            User user = database.userDao().login(email, password);

            runOnUiThread(() -> {
                if (user != null) {
                    saveSessionAndNavigate(user.getId());
                } else {
                    binding.tilPassword.setError("Sai email hoặc mật khẩu!");
                }
            });
        });
    }

    private void saveSessionAndNavigate(int userId) {
        SharedPreferences sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.putInt("USER_ID", userId);
        editor.apply();

        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
