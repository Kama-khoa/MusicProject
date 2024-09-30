package com.example.music_project.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.*;
import java.util.List;
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

    public void registerUser(String username, String email, String password, final OnUserRegisteredListener listener) {
        executorService.execute(() -> {
            User user = new User(username, email, password);
            long userId = database.userDao().insert(user);
            if (userId > 0) {
                listener.onSuccess();
            } else {
                listener.onFailure("Failed to register user");
            }
        });
    }

    public void loginUser(String username, String password, final OnUserLoggedInListener listener) {
        executorService.execute(() -> {
            User user = database.userDao().getUserByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                saveUserSession(user.getId());
                listener.onSuccess(user);
            } else {
                listener.onFailure("Invalid username or password");
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
        return getUserId() != -1L;
    }

    public void getCurrentUser(final OnUserFetchedListener listener) {
        long userId = getUserId();
        if (userId != -1L) {
            executorService.execute(() -> {
                User user = database.userDao().getUserById(userId);
                if (user != null) {
                    listener.onSuccess(user);
                } else {
                    listener.onFailure("User not found");
                }
            });
        } else {
            listener.onFailure("No user logged in");
        }
    }

    public void updateUserProfile(User updatedUser, final OnUserUpdatedListener listener) {
        executorService.execute(() -> {
            try {
                database.userDao().update(updatedUser);
                listener.onSuccess();
            } catch (Exception e) {
                listener.onFailure("Failed to update user profile: " + e.getMessage());
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
        return sharedPreferences.getLong(PREF_USER_ID, -1L);
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
}