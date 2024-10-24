package com.example.music_project.controllers;

import com.example.music_project.database.AlbumSongDao;
import com.example.music_project.database.SongDao;
import com.example.music_project.models.Song;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongController {
    private SongDao songDao;
    private AlbumSongDao albumSongDao;
    private ExecutorService executorService;

    public SongController(SongDao songDao) {
        this.songDao = songDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public SongController(SongDao songDao, AlbumSongDao albumSongDao) {
        this.songDao = songDao;
        this.albumSongDao = albumSongDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void addSong(Song song, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                songDao.insert(song);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError("Không thể thêm bài hát: " + e.getMessage());
            }
        });
    }

    public void updateSong(Song song, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                songDao.update(song);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError("Không thể cập nhật bài hát: " + e.getMessage());
            }
        });
    }

    public void deleteSong(Song song, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                songDao.delete(song);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError("Không thể xóa bài hát: " + e.getMessage());
            }
        });
    }

    public void getAllSongs(Callback<List<Song>> callback) {
        executorService.execute(() -> {
            try {
                List<Song> songs = songDao.getAllSongsWithArtists();
                callback.onSuccess(songs);
            } catch (Exception e) {
                callback.onError("Không thể lấy danh sách bài hát: " + e.getMessage());
            }
        });
    }

    public void getSongById(int songId, Callback<Song> callback) {
        executorService.execute(() -> {
            try {
                Song song = songDao.getSongWithArtist(songId);
                if (song != null) {
                    callback.onSuccess(song);
                } else {
                    callback.onError("Không tìm thấy bài hát");
                }
            } catch (Exception e) {
                callback.onError("Không thể lấy thông tin bài hát: " + e.getMessage());
            }
        });
    }

    // Load songs for a specific album
    public void getSongsForAlbum(int albumId, OnSongsLoadedListener listener) {
        executorService.execute(() -> {
            List<Song> songs = albumSongDao.getSongsByAlbumId(albumId);
            if (songs != null && !songs.isEmpty()) {
                listener.onSongsLoaded(songs);
            } else {
                listener.onFailure("No songs found for the album.");
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T result);

        void onError(String error);
    }

    public interface OnSongsLoadedListener {
        void onSongsLoaded(List<Song> songs);
        void onFailure(String error);
    }

}