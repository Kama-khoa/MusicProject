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
import com.example.music_project.models.Song;
import com.example.music_project.models.User;
import com.example.music_project.views.activities.LoginActivity;
import com.example.music_project.views.activities.ProfileActivity;
import com.example.music_project.views.activities.SettingsActivity;
import com.example.music_project.views.adapters.SongAdapter;

import android.view.MenuItem;
import androidx.appcompat.widget.PopupMenu;

import java.util.List;

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
        ivUserIcon = view.findViewById(R.id.iv_user_icon);
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

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.menu_profile) {
                    openProfileScreen();
                    return true;
                } else if (itemId == R.id.menu_settings) {
                    openSettingsScreen();
                    return true;
                } else if (itemId == R.id.menu_logout) {
                    performLogout();
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

    private void openProfileScreen() {
        userController.getCurrentUser(new UserController.OnUserFetchedListener() {
            @Override
            public void onSuccess(User user) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("USER_ID", user.getId());
                startActivity(intent);
            }

            @Override
            public void onFailure(String error) {
                mainHandler.post(() -> {
                    Toast.makeText(getContext(), getString(R.string.failed_load_profile, error), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openSettingsScreen() {
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
    }

    private void performLogout() {
        userController.logoutUser(new UserController.OnUserLoggedOutListener() {
            @Override
            public void onSuccess() {
                mainHandler.post(() -> {
                    updateUserInterface();
                    Toast.makeText(getContext(), R.string.logout_successful, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                mainHandler.post(() -> {
                    Toast.makeText(getContext(), getString(R.string.logout_failed, error), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadRecentSongs() {
        songController.getRecentSongs(result -> {
            mainHandler.post(() -> {
                if (result instanceof SongController.Result.Success) {
                    List<Song> songs = ((SongController.Result.Success<List<Song>>) result).data;
                    if (songs != null && !songs.isEmpty()) {
                        SongAdapter adapter = new SongAdapter(songs);
                        rvRecentSongs.setAdapter(adapter);
                    } else {
                        handleEmptyState(rvRecentSongs, R.string.no_recent_songs);
                    }
                } else if (result instanceof SongController.Result.Error) {
                    String error = ((SongController.Result.Error) result).error;
                    Toast.makeText(getContext(), getString(R.string.failed_load_recent_songs, error), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadPopularSongs() {
        songController.getPopularSongs(result -> {
            mainHandler.post(() -> {
                if (result instanceof SongController.Result.Success) {
                    List<Song> songs = ((SongController.Result.Success<List<Song>>) result).data;
                    if (songs != null && !songs.isEmpty()) {
                        SongAdapter adapter = new SongAdapter(songs);
                        rvPopularSongs.setAdapter(adapter);
                    } else {
                        handleEmptyState(rvPopularSongs, R.string.no_popular_songs);
                    }
                } else if (result instanceof SongController.Result.Error) {
                    String error = ((SongController.Result.Error) result).error;
                    Toast.makeText(getContext(), getString(R.string.failed_load_popular_songs, error), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void handleEmptyState(RecyclerView recyclerView, int messageResId) {
        recyclerView.setVisibility(View.GONE);
        Toast.makeText(getContext(), messageResId, Toast.LENGTH_SHORT).show();
    }
}