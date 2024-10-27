package com.example.music_project.views.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.music_project.R;
import com.example.music_project.controllers.UserController;
import com.example.music_project.models.User;

import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private UserController userController;
    private TextView tvUsername, tvEmail,tvRole;
    private EditText etNewPassword, etConfirmPassword;
    private ImageView ivProfilePicture,btnBack;
    private ImageButton btnChangeProfilePicture;
    private Button btnChangePassword;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userController = new UserController(this);
        initializeViews();
        setupListeners();

        loadUserProfile();
    }

    private void initializeViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvRole = findViewById(R.id.tv_role);
        tvEmail = findViewById(R.id.tv_email);
        btnChangePassword= findViewById(R.id.btn_change_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        btnBack = findViewById(R.id.btn_back);
        btnChangeProfilePicture = findViewById(R.id.btn_change_profile_picture);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnChangeProfilePicture.setOnClickListener(v -> openImageChooser());
    }

    private void loadUserProfile() {
        userController.getCurrentUser(new UserController.OnUserFetchedListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                runOnUiThread(() -> {
                    tvUsername.setText(user.getUsername());
                    tvEmail.setText(user.getEmail());
                    tvRole.setText(user.getRole());
                    if (user.getProfileImagePath() != null) {
                        ivProfilePicture.setImageURI(Uri.parse(user.getProfileImagePath()));
                    } else {
                        ivProfilePicture.setImageResource(R.drawable.ic_user);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Không thể tải hồ sơ: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void changePassword() {
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please enter and confirm the new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setPassword(newPassword);
        updateUserProfile();
    }

    private void updateUserProfile() {
        userController.updateUserProfile(currentUser, new UserController.OnUserUpdatedListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    loadUserProfile(); // Reload the profile to reflect changes
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            String imagePath = saveProfileImage(imageUri);
            if (imagePath != null) {
                ivProfilePicture.setImageURI(Uri.parse(imagePath));
                currentUser.setProfileImagePath(imagePath);
                updateUserProfile();
            } else {
                Toast.makeText(this, "Không thể lưu ảnh. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String saveProfileImage(Uri imageUri) {
        try {
            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);

            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);

            fos.close();
            inputStream.close();
            return getFilesDir() + "/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}