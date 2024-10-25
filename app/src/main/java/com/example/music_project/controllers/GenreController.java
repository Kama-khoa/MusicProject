package com.example.music_project.controllers;

import android.content.Context;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Genre;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenreController {
    private AppDatabase database;
    private ExecutorService executorService;
    private Context context; // Thêm context

    public GenreController(Context context) {
        this.context = context; // Lưu context
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void getAllGenres(OnGenresLoadedListener listener) {
        executorService.execute(() -> {
            List<Genre> genres = database.genreDao().getAllGenres();
            if (genres != null && !genres.isEmpty()) {
                listener.onGenresLoaded(genres);
            } else {
                listener.onFailure("Không thể tải danh sách thể loại");
            }
        });
    }

    // Thêm hàm getGenreById
    public void getGenreById(int genreId, OnGenreLoadedListener listener) {
        new Thread(() -> {
            Genre genre = database.genreDao().getGenreById(genreId);
            if (genre != null) {
                listener.onGenreLoaded(genre);
            } else {
                listener.onFailure("Không tìm thấy thể loại với ID: " + genreId);
            }
        }).start();
    }

    public interface OnGenreLoadedListener {
        void onGenreLoaded(Genre genre);
        void onFailure(String error);
    }

    public interface OnGenresLoadedListener {
        void onGenresLoaded(List<Genre> genres);
        void onFailure(String error);
    }
}
