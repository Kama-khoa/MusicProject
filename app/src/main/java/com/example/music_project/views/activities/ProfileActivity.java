package com.example.music_project.views.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.music_project.R;
import com.example.music_project.controllers.UserController;
import com.example.music_project.models.User;

public class ProfileActivity extends AppCompatActivity {
    private UserController userController;
    private TextView tvUsername, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userController = new UserController(this);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);

        long userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId != -1) {
            loadUserProfile(userId);
        } else {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserProfile(long userId) {
        userController.getCurrentUser(new UserController.OnUserFetchedListener() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    tvUsername.setText(user.getUsername());
                    tvEmail.setText(user.getEmail());
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
}