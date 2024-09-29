package com.example.music_project.controllers;

import android.content.Context;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.PlayHistory;
import com.example.music_project.models.Song;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerController {
    private AppDatabase database;
    private ExecutorService executorService;
    // Assume we have a MediaPlayer instance here

    public PlayerController(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void playSong(Song song, int userId) {

     //   logPlayHistory(userId, song.getSongId());
    }

    private void logPlayHistory(int userId, int songId) {
        executorService.execute(() -> {
            PlayHistory playHistory = new PlayHistory(userId, songId);
            database.playHistoryDao().insert(playHistory);
        });
    }

    public void getRecentlyPlayedSongs(int userId, int limit, final OnRecentSongsLoadedListener listener) {
        executorService.execute(() -> {
            List<Song> recentSongs = database.playHistoryDao().getRecentlyPlayedSongs(userId, limit);
            listener.onRecentSongsLoaded(recentSongs);
        });
    }

    public interface OnRecentSongsLoadedListener {
        void onRecentSongsLoaded(List<Song> songs);
    }
}
