package com.example.music_project.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Album;
import com.example.music_project.models.AlbumSong;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Genre;
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

    public void createAlbum(int userId, Album album, final OnAlbumCreatedListener listener) {
        executorService.execute(() -> {
            // Chèn album vào database
            long albumId = database.albumDao().insert(album);

            if (albumId > 0) {
                // Nếu thành công, chuyển về Main Thread để xử lý UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onSuccess();
                    // Hiển thị Toast thông báo album đã được tạo
                    Toast.makeText(context, "Album tạo thành công!", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Xử lý khi việc tạo album thất bại
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không thể tạo Album!"));
            }
        });
    }

    public void getAlbums(final AlbumController.OnAlbumsLoadedListener listener) {
        executorService.execute(() -> {
            List<Album> albums = database.albumDao().getAllAbums();
            if (albums != null && !albums.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onAlbumsLoaded(albums));
            } else {
                // Chạy trên luồng chính để thông báo lỗi
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không có danh sách phát nào"));
            }
        });
    }

    public void getAlbumsByUserID(int userId, OnAlbumsLoadedListener listener){
        executorService.execute(()->{
            List<Album> albums = database.albumDao().getAlbumsByUser(userId);
            if (albums != null && !albums.isEmpty()) {
                listener.onAlbumsLoaded(albums);
            } else {
                listener.onFailure("Không thể tải danh sách album");
            }
        });
    }

    // Method to get songs in an album
    public void getSongsInAlbum(int albumId, OnSongsLoadedListener listener) {
        executorService.execute(()->{
            try {
                List<Song> songs = database.albumSongDao().getSongsByAlbumId(albumId);

                // If songs are found, return the result in the listener
                if (songs != null && !songs.isEmpty()) {
                    listener.onSongsLoaded(songs);
                } else {
                    listener.onFailure("No songs found in this album.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure("Failed to load songs: " + e.getMessage());
            }
        });
    }


    public interface OnAlbumCreatedListener {
        void onSuccess();
        void onFailure(String error);
    }

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
