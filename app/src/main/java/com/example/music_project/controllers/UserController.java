package com.example.music_project.controllers;

import android.content.Context;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserController {
    private AppDatabase database;
    private ExecutorService executorService;

    public UserController(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
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
                listener.onSuccess(user);
            } else {
                listener.onFailure("Invalid username or password");
            }
        });
    }

    public interface OnUserRegisteredListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserLoggedInListener {
        void onSuccess(User user);
        void onFailure(String error);
    }
}
