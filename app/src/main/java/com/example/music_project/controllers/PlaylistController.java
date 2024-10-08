package com.example.music_project.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Playlist;
import com.example.music_project.models.PlaylistSong;
import com.example.music_project.models.Song;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistController {
    private AppDatabase database;
    private ExecutorService executorService;
    private Context context; // Thêm context

    public PlaylistController(Context context) {
        this.context = context; // Lưu context
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void createPlaylist(int userId, String name, final OnPlaylistCreatedListener listener) {
        executorService.execute(() -> {
            Playlist playlist = new Playlist(userId, name, new Date());
            long playlistId = database.playlistDao().insert(playlist);
            if (playlistId > 0) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onSuccess();
                    // Hiển thị Toast
                    Toast.makeText(context, "Playlist created successfully!", Toast.LENGTH_SHORT).show();
                });
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Failed to create playlist"));
            }
        });
    }

    public void getPlaylistsForUser(int userId, final OnPlaylistsLoadedListener listener) {
        executorService.execute(() -> {
            List<Playlist> playlists = database.playlistDao().getPlaylistsByUser(userId);
            if (playlists != null) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onPlaylistsLoaded(playlists));
            } else {
                // Chạy trên luồng chính để thông báo lỗi
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("No playlists found for user"));
            }
        });
    }

    public void addSongToPlaylist(int playlistId, int songId, final OnSongAddedToPlaylistListener listener) {
        executorService.execute(() -> {
            PlaylistSong playlistSong = new PlaylistSong(playlistId, songId);
            database.playlistSongDao().insert(playlistSong);
            new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess());
        });
    }

    public void getSongsInPlaylist(int playlistId, final OnSongsLoadedListener listener) {
        executorService.execute(() -> {
            List<Song> songs = database.playlistSongDao().getSongsByPlaylistId(playlistId);
            if (songs != null) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onSongsLoaded(songs));
            } else {
                // Chạy trên luồng chính để thông báo lỗi
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không tìm thấy bài hát trong danh sách phát"));
            }
        });
    }

    public void getPlaylists(final OnPlaylistsLoadedListener listener) {
        executorService.execute(() -> {
            List<Playlist> playlists = database.playlistDao().getAllPlaylists();
            if (playlists != null && !playlists.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onPlaylistsLoaded(playlists));
            } else {
                // Chạy trên luồng chính để thông báo lỗi
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không có danh sách phát nào"));
            }
        });
    }

    public void getPlaylistsByUserID(int userId, OnPlaylistsLoadedListener listener) {
        List<Playlist> playlists = database.playlistDao().getPlaylistsByUser(userId);
        if (playlists != null) {
            listener.onPlaylistsLoaded(playlists);
        } else {
            listener.onFailure("Không thể tải danh sách phát");
        }
    }

    public void getPlaylistByID(final OnPlaylistsLoadedListener listener) {
        executorService.execute(() -> {
            List<Playlist> playlists = database.playlistDao().getAllPlaylists();
            if (playlists != null && !playlists.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onPlaylistsLoaded(playlists));
            } else {
                // Chạy trên luồng chính để thông báo lỗi
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không có danh sách phát nào"));
            }
        });
    }

    public interface OnPlaylistCreatedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnPlaylistsLoadedListener {
        void onPlaylistsLoaded(List<Playlist> playlists);
        void onFailure(String error);
    }

    public interface OnSongAddedToPlaylistListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnSongsLoadedListener {
        void onSongsLoaded(List<Song> songs);
        void onFailure(String error);
    }

    public interface OnPlaylistFetchedListener {
        void onPlaylistFetched(List<Playlist> playlists);
        void onFailure(String error);
    }


}
