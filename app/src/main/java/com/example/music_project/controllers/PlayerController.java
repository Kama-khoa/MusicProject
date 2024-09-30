package com.example.music_project.controllers;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.PlayHistory;
import com.example.music_project.models.Song;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerController {
    private AppDatabase database;
    private ExecutorService executorService;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    public PlayerController(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
        mediaPlayer = new MediaPlayer();
    }

    public void playSong(Song song, int userId) {
        logPlayHistory(userId, song.getSongId());
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getSongUrl());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
        } catch (IOException e) {
            Log.e("PlayerController", "Error playing song: " + e.getMessage());
        }
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

    public void togglePlayPause() {
        if (isPlaying) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
        isPlaying = !isPlaying;
    }

    public void playNext(List<Song> songs, int currentSongIndex) {
        if (currentSongIndex < songs.size() - 1) {
            playSong(songs.get(currentSongIndex + 1), 0);
        }
    }

    public void playPrevious(List<Song> songs, int currentSongIndex) {
        if (currentSongIndex > 0) {
            playSong(songs.get(currentSongIndex - 1), 0);
        }
    }

    public void seekTo(int progress) {
        mediaPlayer.seekTo(progress);
    }

    public boolean isPlaying() {
        return true;
    }

    public void pause() {
    }

    public void play() {
    }

    public interface OnRecentSongsLoadedListener {
        void onRecentSongsLoaded(List<Song> songs);
    }
}
