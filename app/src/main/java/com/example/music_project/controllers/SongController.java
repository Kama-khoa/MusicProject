package com.example.music_project.controllers;

import android.content.Context;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Song;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongController {
    private AppDatabase database;
    private ExecutorService executorService;

    public SongController(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void getAllSongs(final OnSongsLoadedListener listener) {
        executorService.execute(() -> {
            List<Song> songs = database.songDao().getAllSongs();
            listener.onSongsLoaded(songs);
        });
    }

    public void addSong(Song song, final OnSongAddedListener listener) {
        executorService.execute(() -> {
            long songId = database.songDao().insert(song);
            if (songId > 0) {
                listener.onSuccess();
            } else {
                listener.onFailure("Failed to add song");
            }
        });
    }

    public void getRecentSongs(Object o) {
    }

    public void getPopularSongs(Object o) {
    }

    public interface OnSongsLoadedListener {
        void onSongsLoaded(List<Song> songs);
    }

    public interface OnSongAddedListener {
        void onSuccess();
        void onFailure(String error);
    }
}
