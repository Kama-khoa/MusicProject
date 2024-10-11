package com.example.music_project.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Artist;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArtistController {
    private AppDatabase database;
    private ExecutorService executorService;
    private Context context; // Thêm context

    public ArtistController(Context context) {
        this.context = context; // Lưu context
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void createArtist(Artist artist, final OnArtistCreatedListener listener) {
        executorService.execute(() -> {
            // Chèn album vào database
            long artistId = database.artistDao().insert(artist);

            if (artistId > 0) {
                // Nếu thành công, chuyển về Main Thread để xử lý UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onSuccess();
                    // Hiển thị Toast thông báo album đã được tạo
                    Toast.makeText(context, "Ca sĩ tạo thành công!", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Xử lý khi việc tạo album thất bại
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không thể tạo ca sĩ!"));
            }
        });
    }

    public void getArtists(final OnArtistLoadedListener listener) {
        executorService.execute(() -> {
            List<Artist> artists = database.artistDao().getArtists();
            if (artists != null && !artists.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onArtistLoaded(artists));
            } else {
                // Chạy trên luồng chính để thông báo lỗi
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không có danh sách phát nào"));
            }
        });
    }

    public interface OnArtistCreatedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnArtistLoadedListener {
        void onArtistLoaded(List<Artist> artists);
        void onFailure(String error);
    }
}
