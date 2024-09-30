package com.example.music_project.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.controllers.UserController;
import com.example.music_project.views.activities.LoginActivity;
import com.example.music_project.views.adapters.SongAdapter;

import android.view.MenuItem;
import androidx.appcompat.widget.PopupMenu;

public class HomeFragment extends Fragment {
    private RecyclerView rvRecentSongs, rvPopularSongs;
    private SongController songController;
    private UserController userController;
    private ImageView ivUserIcon;
    private Button btnLogin;
    private Handler mainHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        songController = new SongController(getContext());
        userController = new UserController(getContext());
        mainHandler = new Handler(Looper.getMainLooper());

        rvRecentSongs = view.findViewById(R.id.rv_recent_songs);
        rvPopularSongs = view.findViewById(R.id.rv_popular_songs);
        ivUserIcon = view.findViewById(R.id.user_icon);
        btnLogin = view.findViewById(R.id.btn_login);

        rvRecentSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopularSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        updateUserInterface();
        loadRecentSongs();
        loadPopularSongs();

        return view;
    }

    private void updateUserInterface() {
        if (userController.isUserLoggedIn()) {
            ivUserIcon.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);

            // Set onClickListener for ivUserIcon to show the PopupMenu
            ivUserIcon.setOnClickListener(v -> showUserMenu(v));

        } else {
            ivUserIcon.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            btnLogin.setOnClickListener(v -> openLoginScreen());
        }
    }

    private void showUserMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenuInflater().inflate(R.menu.user_menu, popup.getMenu());

        // Handle menu item clicks
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_profile) {
                    Toast.makeText(getContext(), "Xem thông tin cá nhân", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.menu_settings) {
                    Toast.makeText(getContext(), "Mở cài đặt", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.menu_logout) {
                    Toast.makeText(getContext(), "Đăng xuất", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        });

        popup.show();
    }

    private void openLoginScreen() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    private void loadRecentSongs() {
        songController.getRecentSongs(songs -> {
            mainHandler.post(() -> {
                if (songs != null && !songs.isEmpty()) {
                    SongAdapter adapter = new SongAdapter(songs);
                    rvRecentSongs.setAdapter(adapter);
                } else {
                    handleEmptyState(rvRecentSongs, R.string.no_recent_songs);
                }
            });
        });
    }

    private void loadPopularSongs() {
        songController.getPopularSongs(songs -> {
            mainHandler.post(() -> {
                if (songs != null && !songs.isEmpty()) {
                    SongAdapter adapter = new SongAdapter(songs);
                    rvPopularSongs.setAdapter(adapter);
                } else {
                    handleEmptyState(rvPopularSongs, R.string.no_popular_songs);
                }
            });
        });
    }

    private void handleEmptyState(RecyclerView recyclerView, int messageResId) {
        recyclerView.setVisibility(View.GONE);
        Toast.makeText(getContext(), messageResId, Toast.LENGTH_SHORT).show();
    }
}
