package com.example.music_project.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Album;
import com.example.music_project.models.Playlist;
import com.example.music_project.models.PlaylistSong;
import com.example.music_project.models.Song;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlbumController {
    private AppDatabase database;
    private ExecutorService executorService;
    private Context context; // Thêm context

    public AlbumController(Context context) {
        this.context = context; // Lưu context
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }



    public void getAlbums(final AlbumController.OnAlbumsLoadedListener listener) {
        executorService.execute(() -> {
            List<Album> albums = database.albumDao().getAllAlbums();
            if (albums != null && !albums.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onAlbumsLoaded(albums));
            } else {
                // Chạy trên luồng chính để thông báo lỗi
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không có danh sách phát nào"));
            }
        });
    }

//    public interface OnPlaylistCreatedListener {
//        void onSuccess();
//        void onFailure(String error);
//    }

    public interface OnAlbumsLoadedListener {
        void onAlbumsLoaded(List<Album> albums);
        void onFailure(String error);
    }

//    public interface OnSongAddedToPlaylistListener {
//        void onSuccess();
//        void onFailure(String error);
//    }

    public interface OnSongsLoadedListener {
        void onSongsLoaded(List<Song> songs);
        void onFailure(String error);
    }


}
