package com.example.quan_ly_du_an.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.quan_ly_du_an.feature_project.ui.home.HomeFragment;

public class MainActivity_Ui extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        container.setId(android.R.id.content);
        setContentView(container);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new HomeFragment())
                    .commit();
        }
    }
}
