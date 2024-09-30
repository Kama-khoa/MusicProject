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

import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.controllers.UserController;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.database.SongDao;
import com.example.music_project.models.Song;
import com.example.music_project.models.User;
import com.example.music_project.views.activities.LoginActivity;
import com.example.music_project.views.activities.ProfileActivity;
import com.example.music_project.views.activities.SettingsActivity;
import com.example.music_project.views.adapters.SongAdapter;
import com.example.music_project.api.SpotifyApiClient;
import com.example.music_project.api.SpotifyApiService;

import android.view.MenuItem;
import androidx.appcompat.widget.PopupMenu;
import androidx.room.Room;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private RecyclerView rvRecentSongs, rvPopularSongs;
    private SongController songController;
    private UserController userController;
    private ImageView ivUserIcon;
    private Button btnLogin;
    private Handler mainHandler;
    private SpotifyApiService spotifyApiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo Room Database và SongDao
        AppDatabase db = Room.databaseBuilder(getContext(), AppDatabase.class, "database-name").build();
        SongDao songDao = db.songDao();

        // Khởi tạo SongController với SongDao
        songController = new SongController(songDao);
        userController = new UserController(getContext());
        mainHandler = new Handler(Looper.getMainLooper());

        // Khởi tạo Spotify API Service
        spotifyApiService = SpotifyApiClient.getClient().create(SpotifyApiService.class);

        rvRecentSongs = view.findViewById(R.id.rv_recent_songs);
        rvPopularSongs = view.findViewById(R.id.rv_popular_songs);
        ivUserIcon = view.findViewById(R.id.iv_user_icon);
        btnLogin = view.findViewById(R.id.btn_login);

        rvRecentSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopularSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        updateUserInterface();
        loadData();

        return view;
    }

    private void updateUserInterface() {
        if (userController.isUserLoggedIn()) {
            userController.getCurrentUser(new UserController.OnUserFetchedListener() {
                @Override
                public void onSuccess(User user) {
                    mainHandler.post(() -> {
                        ivUserIcon.setVisibility(View.VISIBLE);
                        btnLogin.setVisibility(View.GONE);
                        loadUserProfileImage(user.getProfileImagePath());
                        ivUserIcon.setOnClickListener(v -> showUserMenu(v));
                    });
                }

                @Override
                public void onFailure(String error) {
                    mainHandler.post(() -> {
                        Toast.makeText(getContext(), "Failed to load user data: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            ivUserIcon.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            btnLogin.setOnClickListener(v -> openLoginScreen());
        }
    }

    private void loadUserProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .into(ivUserIcon);
        } else {
            ivUserIcon.setImageResource(R.drawable.ic_user);
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

    private void loadData() {
//        loadRecentSongs();
        loadTopSpotifySongs();
    }

//    private void loadRecentSongs() {
//        songController.getRecentSongs(result -> {
//            mainHandler.post(() -> {
//                if (result instanceof SongController.Result.Success) {
//                    List<Song> songs = ((SongController.Result.Success<List<Song>>) result).data;
//                    if (songs != null && !songs.isEmpty()) {
//                        SongAdapter adapter = new SongAdapter(songs, this::playSong);
//                        rvRecentSongs.setAdapter(adapter);
//                    } else {
//                        handleEmptyState(rvRecentSongs, R.string.no_recent_songs);
//                    }
//                } else if (result instanceof SongController.Result.Error) {
//                    String error = ((SongController.Result.Error) result).error;
//                    Toast.makeText(getContext(), getString(R.string.failed_load_recent_songs, error), Toast.LENGTH_SHORT).show();
//                }
//            });
//        });
//    }

    private void loadTopSpotifySongs() {
        Call<List<Song>> call = spotifyApiService.getTopTracks(5); // Giả định bạn gọi 5 bài hát từ Spotify API
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> songs = response.body();
                    mainHandler.post(() -> {
                        if (!songs.isEmpty()) {
                            // Tạo SongAdapter với danh sách bài hát và hành động khi người dùng nhấn vào bài hát
                            SongAdapter adapter = new SongAdapter(songs, HomeFragment.this::playSong);
                            rvPopularSongs.setAdapter(adapter);  // Gán adapter vào RecyclerView
                        } else {
                            handleEmptyState(rvPopularSongs, R.string.no_top_spotify_songs);
                        }
                    });
                } else {
                    mainHandler.post(() -> {
                        Toast.makeText(getContext(), R.string.failed_load_spotify_songs, Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                mainHandler.post(() -> {
                    Toast.makeText(getContext(), "Failed to fetch Spotify songs", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private void playSong(Song song) {
        // Implement this method to start playing the song
        // You might want to use a PlayerController or MusicPlaybackService here
        Toast.makeText(getContext(), "Playing: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        // Example: playerController.playSong(song);
    }

    private void handleEmptyState(RecyclerView recyclerView, int messageResId) {
        recyclerView.setVisibility(View.GONE);
        Toast.makeText(getContext(), messageResId, Toast.LENGTH_SHORT).show();
    }
}