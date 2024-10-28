package com.example.music_project.controllers;

import android.content.Context;
import android.net.Uri;
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
            Playlist playlist = new Playlist(userId, name, new Date(), null);
            long playlistId = database.playlistDao().insert(playlist);
            if (playlistId > 0) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onSuccess();
                    // Hiển thị Toast
                    //Toast.makeText(context, "Playlist created successfully!", Toast.LENGTH_SHORT).show();
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
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không có danh sách phát nào"));
            }
        });
    }

    // Sửa phương thức này để sử dụng executorService
    public void getPlaylistsByUserID(int userId, OnPlaylistsLoadedListener listener) {
        executorService.execute(() -> {
            List<Playlist> playlists = database.playlistDao().getPlaylistsByUser(userId);
            if (playlists != null) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onPlaylistsLoaded(playlists));
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không thể tải danh sách phát"));
            }
        });
    }

    public void getPlaylistByID(final OnPlaylistsLoadedListener listener) {
        executorService.execute(() -> {
            List<Playlist> playlists = database.playlistDao().getAllPlaylists();
            if (playlists != null && !playlists.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onPlaylistsLoaded(playlists));
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không có danh sách phát nào"));
            }
        });
    }

    public void getPlaylistDetails(int playlistId, final OnPlaylistDetailsLoadedListener listener) {
        executorService.execute(() -> {
            Playlist playlist = database.playlistDao().getPlaylistById(playlistId);
            if (playlist != null) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onPlaylistDetailsLoaded(playlist));
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không tìm thấy playlist"));
            }
        });
    }

    public void updatePlaylist(int playlistId, String name, String description, String image, OnPlaylistUpdatedListener listener) {
        executorService.execute(() -> {
            Playlist playlist = database.playlistDao().getPlaylistById(playlistId);
            if (playlist != null) {
                playlist.setTitle(name);
                playlist.setDetails(description);
                if (image != null) {
                    playlist.setImageResource(image);
                } else {
                    playlist.setImageResource(null);
                }

                database.playlistDao().update(playlist);
                new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess());
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Playlist not found"));
            }
        });
    }

    public interface OnPlaylistUpdatedListener {
        void onSuccess();
        void onFailure(String error);
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

    public interface OnSongAddedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnPlaylistDeletedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public void deletePlaylist(int playlistId, OnPlaylistDeletedListener listener) {
        executorService.execute(() -> {
            Playlist playlist = database.playlistDao().getPlaylistById(playlistId);
            if (playlist != null) {
                // Xóa playlist
                database.playlistDao().delete(playlist);
                // Gọi listener.onSuccess() trên luồng chính
                new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess());
            } else {
                // Gọi listener.onFailure() nếu không tìm thấy playlist
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không thấy playlist khi xóa"));
            }
        });
    }

    public void deleteSongFromPlaylist(int playlistId, int songId, OnSongDeletedListener listener) {
        executorService.execute(() -> {
            // Get the PlaylistSong object that links the playlist and the song
            PlaylistSong playlistSong = database.playlistSongDao().getPlaylistSong(playlistId, songId);

            if (playlistSong != null) {
                // If the association exists, delete it
                database.playlistSongDao().delete(playlistSong);

                // Notify success on the main thread
                new Handler(Looper.getMainLooper()).post(() -> listener.onSongDeleted(null));
            } else {
                // If no association is found, notify failure
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không tìm thấy bài hát trong playlist"));
            }
        });
    }

    public void addSongToPlaylist(int playlistId, Song song, OnSongAddedListener listener) {
        // Thực hiện thêm bài hát và gọi listener.onSuccess() hoặc listener.onFailure(error)
    }

    public interface OnPlaylistDetailsLoadedListener {
        void onPlaylistDetailsLoaded(Playlist playlist);
        void onFailure(String error);
    }

    public interface OnSongDeletedListener {
        void onSongDeleted(Song song);  // Called when the song is successfully deleted
        void onFailure(String error);   // Called if there was an error
    }

    public void searchPlaylists(String query, final OnPlaylistsLoadedListener listener) {
        executorService.execute(() -> {
            List<Playlist> playlists = database.playlistDao().searchPlaylists(query);
            if (playlists != null && !playlists.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onPlaylistsLoaded(playlists));
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không tìm thấy danh sách phát nào"));
            }
        });
    }
}
