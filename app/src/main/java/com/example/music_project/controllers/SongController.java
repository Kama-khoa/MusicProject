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

    public void getAllSongs(final OnSongOperationListener listener) {
        executorService.execute(() -> {
            try {
                List<Song> songs = database.songDao().getAllSongs();
                listener.onComplete(new Result.Success<>(songs));
            } catch (Exception e) {
                listener.onComplete(new Result.Error(e.getMessage()));
            }
        });
    }

    public void addSong(Song song, final OnSongOperationListener listener) {
        executorService.execute(() -> {
            try {
                long songId = database.songDao().insert(song);
                if (songId > 0) {
                    listener.onComplete(new Result.Success<>(null));
                } else {
                    listener.onComplete(new Result.Error("Failed to add song"));
                }
            } catch (Exception e) {
                listener.onComplete(new Result.Error(e.getMessage()));
            }
        });
    }

    public void getRecentSongs(final OnSongOperationListener listener) {
        executorService.execute(() -> {
            try {
                List<Song> recentSongs = database.songDao().getRecentSongs();
                listener.onComplete(new Result.Success<>(recentSongs));
            } catch (Exception e) {
                listener.onComplete(new Result.Error(e.getMessage()));
            }
        });
    }

    public void getPopularSongs(final OnSongOperationListener listener) {
        executorService.execute(() -> {
            try {
                List<Song> popularSongs = database.songDao().getPopularSongs();
                listener.onComplete(new Result.Success<>(popularSongs));
            } catch (Exception e) {
                listener.onComplete(new Result.Error(e.getMessage()));
            }
        });
    }

    public interface OnSongOperationListener {
        void onComplete(Result result);
    }

    public static class Result {
        private Result() {}

        public static final class Success<T> extends Result {
            public T data;

            public Success(T data) {
                this.data = data;
            }
        }

        public static final class Error extends Result {
            public String error;

            public Error(String error) {
                this.error = error;
            }
        }
    }
}