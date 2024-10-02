package com.example.music_project.controllers;

import android.content.Context;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Playlist;
import com.example.music_project.models.PlaylistSong;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistController {
    private AppDatabase database;
    private ExecutorService executorService;

    public PlaylistController(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void createPlaylist(int userId, String name, final OnPlaylistCreatedListener listener) {
        executorService.execute(() -> {
            Playlist playlist = new Playlist(userId, name, new Date());
            long playlistId = database.playlistDao().insert(playlist);
            if (playlistId > 0) {
                listener.onSuccess();
            } else {
                listener.onFailure("Failed to create playlist");
            }
        });
    }

    public void getPlaylistsForUser(int userId, final OnPlaylistsLoadedListener listener) {
        executorService.execute(() -> {
            List<Playlist> playlists = database.playlistDao().getPlaylistsByUser(userId);
            listener.onPlaylistsLoaded(playlists);
        });
    }

    public void addSongToPlaylist(int playlistId, int songId, final OnSongAddedToPlaylistListener listener) {
        executorService.execute(() -> {
            PlaylistSong playlistSong = new PlaylistSong(playlistId, songId);
            database.playlistSongDao().insert(playlistSong);
            listener.onSuccess();
        });
    }

    public interface OnPlaylistCreatedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnPlaylistsLoadedListener {
        void onPlaylistsLoaded(List<Playlist> playlists);
    }

    public interface OnSongAddedToPlaylistListener {
        void onSuccess();
        void onFailure(String error);
    }
}
