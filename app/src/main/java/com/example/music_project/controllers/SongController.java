package com.example.music_project.controllers;

import com.example.music_project.database.SongDao;
import com.example.music_project.models.Song;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongController {
    private SongDao songDao;
    private ExecutorService executorService;

    public SongController(SongDao songDao) {
        this.songDao = songDao;
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

    public interface Callback<T> {
        void onSuccess(T result);

        void onError(String error);
    }
}