package com.example.music_project.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserController {
    private AppDatabase database;
    private ExecutorService executorService;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserPrefs";
    private static final String PREF_USER_ID = "userId";

    public UserController(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void registerUser(String username, String email, String password,String role, final OnUserRegisteredListener listener) {
        executorService.execute(() -> {
            User user = new User(username, email, password,role);
            long userId = database.userDao().insert(user);
            if (userId > 0) {
                listener.onSuccess();
            } else {
                listener.onFailure("Không thể đăng ký người dùng");
            }
        });
    }

    public void loginUser(String username, String password, final OnUserLoggedInListener listener) {
        executorService.execute(() -> {
            User user = database.userDao().getUserByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                saveUserSession(user.getUser_id());
                listener.onSuccess(user);
            } else {
                listener.onFailure("Tên đăng nhập hoặc mật khẩu không hợp lệ");
            }
        });
    }

    public void logoutUser(final OnUserLoggedOutListener listener) {
        executorService.execute(() -> {
            clearUserSession();
            listener.onSuccess();
        });
    }

    public boolean isUserLoggedIn() {
        return getUserId() != -1;
    }

    public void getCurrentUser(final OnUserFetchedListener listener) {
        long userId = getUserId();
        if (userId != -1) {
            executorService.execute(() -> {
                User user = database.userDao().getUserById(userId);
                if (user != null) {
                    listener.onSuccess(user);
                } else {
                    listener.onFailure("Không tìm thấy người dùng");
                }
            });
        } else {
            listener.onFailure("Chưa có người dùng đăng nhập");
        }
    }

    public void updateUserProfile(User updatedUser, final OnUserUpdatedListener listener) {
        executorService.execute(() -> {
            try {
                database.userDao().update(updatedUser);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onFailure("Không thể cập nhật hồ sơ người dùng: " + e.getMessage());
            }
        });
    }

    public void setUserRole(long userId, String role, final OnUserUpdatedListener listener) {
        executorService.execute(() -> {
            try {
                User user = database.userDao().getUserById(userId);
                if (user != null) {
                    user.setRole(role);
                    database.userDao().update(user);
                    listener.onSuccess();
                } else {
                    listener.onFailure("Không tìm thấy người dùng");
                }
            } catch (Exception e) {
                listener.onFailure("Không thể cập nhật vai trò người dùng: " + e.getMessage());
            }
        });
    }

    public void setUserUploadPermission(long userId, boolean canUpload, final OnUserUpdatedListener listener) {
        executorService.execute(() -> {
            try {
                User user = database.userDao().getUserById(userId);
                if (user != null) {
                    user.setCanUploadContent(canUpload);
                    database.userDao().update(user);
                    listener.onSuccess();
                } else {
                    listener.onFailure("Không tìm thấy người dùng");
                }
            } catch (Exception e) {
                listener.onFailure("Không thể cập nhật quyền đăng nội dung: " + e.getMessage());
            }
        });
    }

    public void getProfileImagePath(long userId, final OnProfileImageFetchedListener listener) {
        executorService.execute(() -> {
            User user = database.userDao().getUserById(userId);
            if (user != null) {
                listener.onSuccess(user.getProfileImagePath());
            } else {
                listener.onFailure("Không tìm thấy người dùng");
            }
        });
    }

    public void updateProfileImagePath(long userId, String imagePath, final OnUserUpdatedListener listener) {
        executorService.execute(() -> {
            try {
                User user = database.userDao().getUserById(userId);
                if (user != null) {
                    user.setProfileImagePath(imagePath);
                    database.userDao().update(user);
                    listener.onSuccess();
                } else {
                    listener.onFailure("Không tìm thấy người dùng");
                }
            } catch (Exception e) {
                listener.onFailure("Không thể cập nhật đường dẫn ảnh hồ sơ: " + e.getMessage());
            }
        });
    }

    private void saveUserSession(long userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_USER_ID, userId);
        editor.apply();
    }

    private void clearUserSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREF_USER_ID);
        editor.apply();
    }

    private long getUserId() {
        return sharedPreferences.getLong(PREF_USER_ID, -1);
    }

    public interface OnUserRegisteredListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserLoggedInListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnUserLoggedOutListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserFetchedListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnUserUpdatedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnProfileImageFetchedListener {
        void onSuccess(String imagePath);
        void onFailure(String error);
    }
}
